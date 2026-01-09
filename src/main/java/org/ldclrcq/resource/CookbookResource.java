package org.ldclrcq.resource;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.ldclrcq.dto.*;
import org.ldclrcq.entity.*;
import org.ldclrcq.service.OcrService;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Path("/api/cookbooks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CookbookResource {

    @Inject
    OcrService ocrService;

    @POST
    @Transactional
    public Response createCookbook(CreateCookbookRequest request) {
        Cookbook cookbook = Cookbook.create(request.title(), request.author());
        cookbook.persist();

        CookbookResponse response = new CookbookResponse(
                cookbook.id,
                cookbook.title,
                cookbook.author,
                cookbook.createdAt
        );

        return Response.status(Response.Status.CREATED).entity(response).build();
    }
    
    @DELETE
    @Path("/{id}")
    public Response deleteCookbook(@PathParam("id") UUID id) {
        Cookbook.deleteById(id);
        return Response.ok().build();
    }

    @POST
    @Path("/{id}/index-pages")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    public Response uploadIndexPages(
            @PathParam("id") UUID cookbookId,
            @RestForm("files") List<FileUpload> files) {

        if (files == null || files.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("At least one file is required")
                    .build();
        }

        Optional<Cookbook> cookbook = Cookbook.findByIdOptional(cookbookId);
        if (cookbook.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Cookbook not found: " + cookbookId)
                    .build();
        }

        try {
            int pageOrder = 0;
            for (FileUpload file : files) {
                String contentType = file.contentType();
                if (!isValidImageType(contentType)) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Invalid image type: " + contentType + ". Only JPEG and PNG are allowed.")
                            .build();
                }

                byte[] imageData = Files.readAllBytes(file.uploadedFile());
                CookbookIndexPage page = CookbookIndexPage.create(cookbookId, pageOrder++, imageData, contentType);
                page.persist();
            }

            return Response.ok(new UploadIndexPagesResponse(cookbookId, pageOrder)).build();

        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to read uploaded files: " + e.getMessage())
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/{id}/ocr/start")
    @Transactional
    public Response startOcrProcessing(@PathParam("id") UUID cookbookId) {
        Optional<Cookbook> cookbookOpt = Cookbook.findByIdOptional(cookbookId);
        if (cookbookOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Cookbook not found: " + cookbookId)
                    .build();
        }

        Cookbook cookbook = cookbookOpt.get();

        // Check if already processing
        if (cookbook.ocrStatus == Cookbook.OcrStatus.PROCESSING) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("OCR processing is already in progress for this cookbook")
                    .build();
        }

        // Check if there are index pages
        List<CookbookIndexPage> pages = CookbookIndexPage.findByCookbookIdOrdered(cookbookId);
        if (pages.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("No index pages found for cookbook")
                    .build();
        }

        // Set status to PROCESSING
        cookbook.ocrStatus = Cookbook.OcrStatus.PROCESSING;
        cookbook.ocrErrorMessage = null;

        // Start async processing (will run after transaction commits)
        ocrService.startAsyncProcessing(cookbookId);

        return Response.accepted(Map.of(
                "message", "OCR processing started",
                "cookbookId", cookbookId,
                "status", "PROCESSING"
        )).build();
    }

    @GET
    @Path("/{id}/ocr/results")
    public Response getOcrResults(@PathParam("id") UUID cookbookId) {
        Optional<Cookbook> cookbookOpt = Cookbook.findByIdOptional(cookbookId);
        if (cookbookOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Cookbook not found: " + cookbookId)
                    .build();
        }

        Cookbook cookbook = cookbookOpt.get();
        List<OcrResultEntity> results = OcrResultEntity.findByCookbookId(cookbookId);

        List<OcrResultDto> resultDtos = results.stream()
                .map(r -> new OcrResultDto(
                        r.ingredient,
                        r.recipeName,
                        r.pageNumber,
                        r.confidence,
                        r.needsReview
                ))
                .toList();

        return Response.ok(new OcrProgressResponse(
                mapOcrStatus(cookbook.ocrStatus),
                0,
                0,
                resultDtos,
                cookbook.ocrErrorMessage
        )).build();
    }

    private OcrProgressResponse.Status mapOcrStatus(Cookbook.OcrStatus status) {
        return switch (status) {
            case NONE -> OcrProgressResponse.Status.PENDING;
            case PROCESSING -> OcrProgressResponse.Status.IN_PROGRESS;
            case COMPLETED -> OcrProgressResponse.Status.COMPLETED;
            case COMPLETED_WITH_ERRORS -> OcrProgressResponse.Status.COMPLETED_WITH_ERRORS;
            case FAILED -> OcrProgressResponse.Status.FAILED;
        };
    }

    @POST
    @Path("/{id}/confirm")
    @Transactional
    public Response confirmImport(
            @PathParam("id") UUID cookbookId,
            ConfirmImportRequest request) {

        if (request.getRecipes() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Recipes list is required")
                    .build();
        }

        Optional<Cookbook> cookbook = Cookbook.findByIdOptional(cookbookId);
        if (cookbook.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Cookbook not found: " + cookbookId)
                    .build();
        }

        // Filter out skipped recipes and group by name+page
        Map<String, Recipe> recipesByKey = new HashMap<>();

        for (ConfirmImportRequest.ConfirmedRecipe confirmedRecipe : request.getRecipes()) {
            if (!confirmedRecipe.keep()) {
                continue;
            }

            String normalizedIngredientName = Ingredient.normalize(confirmedRecipe.ingredient());

            // Find or create ingredient
            Ingredient ingredient = Ingredient.findByName(normalizedIngredientName)
                    .orElseGet(() -> {
                        Ingredient newIngredient = Ingredient.create(confirmedRecipe.ingredient());
                        newIngredient.persist();
                        return newIngredient;
                    });

            // Create unique key for recipe (name + page)
            String recipeKey = confirmedRecipe.recipeName() + ":" + confirmedRecipe.pageNumber();

            Recipe recipe = recipesByKey.get(recipeKey);
            if (recipe == null) {
                recipe = Recipe.create(cookbookId, confirmedRecipe.recipeName(), confirmedRecipe.pageNumber());
            }
            recipe.addIngredient(ingredient);
            recipesByKey.put(recipeKey, recipe);
        }

        // Persist all recipes
        for (Recipe recipe : recipesByKey.values()) {
            recipe.persist();
        }

        // Clear OCR results and reset status after successful import
        OcrResultEntity.deleteByCookbookId(cookbookId);
        Cookbook c = cookbook.get();
        c.ocrStatus = Cookbook.OcrStatus.NONE;
        c.ocrErrorMessage = null;

        return Response.ok(new ConfirmImportResponse(recipesByKey.size())).build();
    }

    @GET
    public Response listCookbooks(@QueryParam("q") String q) {
        List<Cookbook> cookbooks;
        if (q != null && !q.isBlank()) {
            String query = "LOWER(title) LIKE ?1 OR LOWER(author) LIKE ?1";
            String param = "%" + q.toLowerCase().trim() + "%";
            cookbooks = Cookbook.list(query, param);
        } else {
            cookbooks = Cookbook.listAll();
        }

        List<CookbookDetailResponse> responses = cookbooks.stream()
                .map(c -> new CookbookDetailResponse(
                        c.id,
                        c.title,
                        c.author,
                        c.createdAt,
                        c.countRecipes(),
                        c.hasCover,
                        c.ocrStatus.name(),
                        c.ocrErrorMessage
                ))
                .toList();

        return Response.ok(responses).build();
    }

    @GET
    @Path("/{id}")
    public Response getCookbook(@PathParam("id") UUID id) {
        Optional<Cookbook> cookbook = Cookbook.findByIdOptional(id);

        if (cookbook.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Cookbook not found")
                    .build();
        }

        Cookbook c = cookbook.get();
        CookbookDetailResponse response = new CookbookDetailResponse(
                c.id,
                c.title,
                c.author,
                c.createdAt,
                c.countRecipes(),
                c.hasCover,
                c.ocrStatus.name(),
                c.ocrErrorMessage
        );

        return Response.ok(response).build();
    }

    @GET
    @Path("/{id}/cover")
    @Produces({"image/jpeg", "image/png"})
    @Transactional
    public Response getCover(@PathParam("id") UUID id) {
        Optional<Cookbook> cookbook = Cookbook.findByIdOptional(id);
        if (cookbook.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Cookbook not found")
                    .build();
        }
        Cookbook c = cookbook.get();
        if (!c.hasCover) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(c.coverData)
                .type(c.coverContentType)
                .build();
    }

    @POST
    @Path("/{id}/cover")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    public Response uploadCover(
            @PathParam("id") UUID id,
            @RestForm("file") FileUpload file
    ) {
        if (file == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("A file is required")
                    .build();
        }

        String contentType = file.contentType();
        if (!isValidImageType(contentType)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid image type: " + contentType + ". Only JPEG and PNG are allowed.")
                    .build();
        }

        byte[] imageData;
        try {
            imageData = java.nio.file.Files.readAllBytes(file.uploadedFile());
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to read uploaded file: " + e.getMessage())
                    .build();
        }

        // Use update query to avoid LOB issues with PostgreSQL auto-commit
        int updated = Cookbook.update("coverData = ?1, coverContentType = ?2, hasCover = true where id = ?3",
                imageData, contentType, id);

        if (updated == 0) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Cookbook not found: " + id)
                    .build();
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("id", id);
        resp.put("hasCover", true);
        return Response.ok(resp).build();
    }

    @GET
    @Path("/{id}/recipes")
    public Response getCookbookRecipes(@PathParam("id") UUID id) {
        Optional<Cookbook> cookbook = Cookbook.findByIdOptional(id);

        if (cookbook.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Cookbook not found")
                    .build();
        }

        List<Recipe> recipes = Recipe.findByCookbookId(id);
        Cookbook c = cookbook.get();

        List<RecipeResponse> recipeResponses = recipes.stream()
                .map(r -> new RecipeResponse(
                        r.id,
                        r.name,
                        r.pageNumber,
                        r.cookbookId,
                        c.title,
                        c.author,
                        r.ingredients.stream()
                                .map(i -> i.name)
                                .sorted()
                                .toList()
                ))
                .toList();

        return Response.ok(recipeResponses).build();
    }

    private boolean isValidImageType(String contentType) {
        return "image/jpeg".equals(contentType) || "image/png".equals(contentType);
    }
}
