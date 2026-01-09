package org.ldclrcq.resource;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.ldclrcq.dto.*;
import org.ldclrcq.entity.Cookbook;
import org.ldclrcq.entity.Ingredient;
import org.ldclrcq.entity.Recipe;

import java.util.*;

@Path("/api/ingredients")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class IngredientResource {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;
    private static final int RECIPE_PREVIEW_LIMIT = 5;

    @GET
    public Response listIngredients(
            @QueryParam("q") String query,
            @QueryParam("minRecipeCount") Integer minRecipeCount,
            @QueryParam("hasDisambiguations") Boolean hasDisambiguations,
            @QueryParam("availableNow") Boolean availableNow,
            @QueryParam("cursor") UUID cursor,
            @QueryParam("limit") Integer limit) {

        int effectiveLimit = limit != null ? Math.min(limit, MAX_LIMIT) : DEFAULT_LIMIT;

        List<Ingredient> ingredients = Ingredient.findWithFilters(
                query, minRecipeCount, hasDisambiguations, availableNow, cursor, effectiveLimit);

        boolean hasMore = Ingredient.hasMore(query, minRecipeCount, hasDisambiguations, availableNow, cursor, effectiveLimit);

        List<IngredientListResponse.IngredientSummary> summaries = ingredients.stream()
                .map(i -> new IngredientListResponse.IngredientSummary(
                        i.id,
                        i.name,
                        i.getDisambiguationNames(),
                        i.countRecipes(),
                        i.availableMonths.stream().sorted().toList()
                ))
                .toList();

        String nextCursor = hasMore && !ingredients.isEmpty()
                ? ingredients.getLast().id.toString()
                : null;

        return Response.ok(new IngredientListResponse(summaries, nextCursor, hasMore)).build();
    }

    @GET
    @Path("/{id}")
    public Response getIngredient(@PathParam("id") UUID id) {
        Optional<Ingredient> ingredientOpt = Ingredient.findByIdOptional(id);
        if (ingredientOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Ingredient not found"))
                    .build();
        }

        Ingredient ingredient = ingredientOpt.get();
        return Response.ok(toDetailResponse(ingredient)).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response updateIngredient(@PathParam("id") UUID id, UpdateIngredientRequest request) {
        Optional<Ingredient> ingredientOpt = Ingredient.findByIdOptional(id);
        if (ingredientOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Ingredient not found"))
                    .build();
        }

        Ingredient ingredient = ingredientOpt.get();

        // Validate name
        if (request.name() == null || request.name().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Name cannot be empty"))
                    .build();
        }

        String normalizedName = Ingredient.normalize(request.name());

        // Check if name is available (not used by another ingredient or as disambiguation)
        if (!ingredient.name.equals(normalizedName) && !Ingredient.isNameAvailable(request.name(), id)) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", "Name is already in use by another ingredient or disambiguation"))
                    .build();
        }

        // Validate disambiguations
        List<String> newDisambiguations = request.disambiguations() != null
                ? request.disambiguations()
                : List.of();

        for (String disambiguation : newDisambiguations) {
            String normalizedDisambiguation = Ingredient.normalize(disambiguation);
            // Skip if it's already a disambiguation for this ingredient
            if (ingredient.getDisambiguationNames().contains(normalizedDisambiguation)) {
                continue;
            }
            if (!Ingredient.isDisambiguationAvailable(disambiguation, id)) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(Map.of("error", "Disambiguation '" + disambiguation + "' is already in use"))
                        .build();
            }
        }

        // Update name
        ingredient.name = normalizedName;

        // Update disambiguations - remove old ones, add new ones
        Set<String> normalizedNewDisambiguations = new HashSet<>();
        for (String d : newDisambiguations) {
            normalizedNewDisambiguations.add(Ingredient.normalize(d));
        }

        // Remove disambiguations that are no longer in the list
        Set<String> currentDisambiguations = new HashSet<>(ingredient.getDisambiguationNames());
        for (String current : currentDisambiguations) {
            if (!normalizedNewDisambiguations.contains(current)) {
                ingredient.removeDisambiguation(current);
            }
        }

        // Add new disambiguations
        for (String newD : normalizedNewDisambiguations) {
            if (!currentDisambiguations.contains(newD)) {
                ingredient.addDisambiguation(newD);
            }
        }

        // Update available months
        ingredient.availableMonths.clear();
        if (request.availableMonths() != null) {
            for (Integer m : request.availableMonths()) {
                if (m == null || m < 1 || m > 12) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(Map.of("error", "availableMonths must contain values between 1 and 12"))
                            .build();
                }
                ingredient.availableMonths.add(m);
            }
        }

        ingredient.persist();

        return Response.ok(toDetailResponse(ingredient)).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteIngredient(@PathParam("id") UUID id) {
        Optional<Ingredient> ingredientOpt = Ingredient.findByIdOptional(id);
        if (ingredientOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Ingredient not found"))
                    .build();
        }

        Ingredient ingredient = ingredientOpt.get();

        // Check if ingredient has recipe associations
        long recipeCount = ingredient.countRecipes();
        if (recipeCount > 0) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", "Cannot delete ingredient with " + recipeCount + " recipe associations"))
                    .build();
        }

        ingredient.delete();

        return Response.noContent().build();
    }

    @POST
    @Path("/merge")
    @Transactional
    public Response mergeIngredients(MergeIngredientsRequest request) {
        // Validate request
        if (request.targetId() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Target ID is required"))
                    .build();
        }

        if (request.sourceIds() == null || request.sourceIds().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "At least one source ID is required"))
                    .build();
        }

        // Find target ingredient
        Optional<Ingredient> targetOpt = Ingredient.findByIdOptional(request.targetId());
        if (targetOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Target ingredient not found"))
                    .build();
        }

        Ingredient target = targetOpt.get();

        // Find all source ingredients
        List<Ingredient> sources = new ArrayList<>();
        for (UUID sourceId : request.sourceIds()) {
            if (sourceId.equals(request.targetId())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Target cannot be in source list"))
                        .build();
            }
            Optional<Ingredient> sourceOpt = Ingredient.findByIdOptional(sourceId);
            if (sourceOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Source ingredient not found: " + sourceId))
                        .build();
            }
            sources.add(sourceOpt.get());
        }

        // Merge each source into target
        for (Ingredient source : sources) {
            // Collect disambiguations to transfer before clearing them from source
            List<String> disambiguationsToTransfer = new ArrayList<>(source.getDisambiguationNames());
            String sourceName = source.name;

            // Clear source disambiguations first to avoid unique constraint violations
            source.disambiguations.clear();
            source.persist();
            Ingredient.flush();

            // Add source name as disambiguation on target (if not already the target name)
            if (!sourceName.equals(target.name) &&
                    !target.getDisambiguationNames().contains(sourceName)) {
                target.addDisambiguation(sourceName);
            }

            // Add all source disambiguations to target
            for (String disambiguation : disambiguationsToTransfer) {
                if (!disambiguation.equals(target.name) &&
                        !target.getDisambiguationNames().contains(disambiguation)) {
                    target.addDisambiguation(disambiguation);
                }
            }

            // Update all recipe associations: change source to target
            Recipe.updateIngredientReferences(source.id, target.id);

            // Delete source
            source.delete();
        }

        target.persist();

        return Response.ok(toDetailResponse(target)).build();
    }

    @GET
    @Path("/{id}/recipes")
    public Response getIngredientRecipes(
            @PathParam("id") UUID id,
            @QueryParam("q") String nameQuery,
            @QueryParam("cookbookId") UUID cookbookId,
            @QueryParam("cursor") String cursor,
            @QueryParam("limit") Integer limit
    ) {
        Optional<Ingredient> ingredient = Ingredient.findByIdOptional(id);
        if (ingredient.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Ingredient not found"))
                    .build();
        }

        int effectiveLimit = limit != null ? Math.min(limit, MAX_LIMIT) : DEFAULT_LIMIT;
        UUID cursorId = cursor != null ? UUID.fromString(cursor) : null;

        Recipe.SearchResult result = Recipe.search(nameQuery, ingredient.get().id, null, cookbookId, cursorId, effectiveLimit);

        List<RecipeResponse> recipeResponses = result.recipes().stream()
                .map(this::toRecipeResponse)
                .toList();

        return Response.ok(new RecipeListResponse(recipeResponses, result.nextCursor(), result.hasMore())).build();
    }

    private IngredientDetailResponse toDetailResponse(Ingredient ingredient) {
        // Get recipe preview (first 5 recipes) by ingredient ID
        List<Recipe> recipes = Recipe.findByIngredientId(ingredient.id, RECIPE_PREVIEW_LIMIT);

        List<IngredientDetailResponse.RecipeSummary> recipeSummaries = recipes.stream()
                .map(r -> {
                    Optional<Cookbook> cookbook = Cookbook.findByIdOptional(r.cookbookId);
                    return new IngredientDetailResponse.RecipeSummary(
                            r.id,
                            r.name,
                            cookbook.map(c -> c.title).orElse(null),
                            r.pageNumber
                    );
                })
                .toList();

        return new IngredientDetailResponse(
                ingredient.id,
                ingredient.name,
                ingredient.getDisambiguationNames(),
                ingredient.countRecipes(),
                recipeSummaries,
                ingredient.availableMonths.stream().sorted().toList(),
                ingredient.createdAt,
                ingredient.updatedAt
        );
    }

    private RecipeResponse toRecipeResponse(Recipe recipe) {
        Optional<Cookbook> cookbook = Cookbook.findByIdOptional(recipe.cookbookId);

        List<String> ingredientNames = recipe.ingredients.stream()
                .map(i -> i.name)
                .sorted()
                .toList();

        return new RecipeResponse(
                recipe.id,
                recipe.name,
                recipe.pageNumber,
                recipe.cookbookId,
                cookbook.map(c -> c.title).orElse(null),
                cookbook.map(c -> c.author).orElse(null),
                ingredientNames
        );
    }
}
