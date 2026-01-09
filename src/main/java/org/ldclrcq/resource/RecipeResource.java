package org.ldclrcq.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.ldclrcq.dto.RecipeListResponse;
import org.ldclrcq.dto.RecipeResponse;
import org.ldclrcq.entity.Cookbook;
import org.ldclrcq.entity.Ingredient;
import org.ldclrcq.entity.Recipe;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Path("/api/recipes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RecipeResource {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    @GET
    public Response listRecipes(
            @QueryParam("q") String nameQuery,
            @QueryParam("ingredient") String ingredient,
            @QueryParam("availableNow") Boolean availableNow,
            @QueryParam("cookbookId") UUID cookbookId,
            @QueryParam("cursor") String cursor,
            @QueryParam("limit") Integer limit) {

        int effectiveLimit = limit != null ? Math.min(limit, MAX_LIMIT) : DEFAULT_LIMIT;
        UUID cursorId = cursor != null ? UUID.fromString(cursor) : null;

        // Look up ingredient ID by name if provided
        UUID ingredientId = null;
        if (ingredient != null && !ingredient.isBlank()) {
            Optional<Ingredient> ingredientEntity = Ingredient.findByName(ingredient);
            if (ingredientEntity.isPresent()) {
                ingredientId = ingredientEntity.get().id;
            } else {
                // No matching ingredient found, return empty results
                return Response.ok(new RecipeListResponse(List.of(), null, false)).build();
            }
        }

        Recipe.SearchResult result = Recipe.search(nameQuery, ingredientId, availableNow, cookbookId, cursorId, effectiveLimit);

        List<RecipeResponse> recipeResponses = result.recipes().stream()
                .map(this::toRecipeResponse)
                .toList();

        return Response.ok(new RecipeListResponse(recipeResponses, result.nextCursor(), result.hasMore())).build();
    }

    @GET
    @Path("/{id}")
    public Response getRecipe(@PathParam("id") UUID id) {
        Optional<Recipe> recipe = Recipe.findByIdOptional(id);

        if (recipe.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Recipe not found")
                    .build();
        }

        return Response.ok(toRecipeResponse(recipe.get())).build();
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
