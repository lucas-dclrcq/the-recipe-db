package org.ldclrcq.service;

import dev.langchain4j.data.image.Image;
import io.quarkus.arc.Arc;
import io.quarkus.arc.ManagedContext;
import io.quarkus.logging.Log;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.virtual.threads.VirtualThreads;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.ldclrcq.dto.OcrProgressResponse;
import org.ldclrcq.dto.OcrResult;
import org.ldclrcq.dto.OcrResultDto;
import org.ldclrcq.dto.PageError;
import org.ldclrcq.entity.Cookbook;
import org.ldclrcq.entity.CookbookIndexPage;
import org.ldclrcq.entity.OcrResultEntity;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

@ApplicationScoped
public class OcrService {

    @Inject
    OcrAiService aiService;

    @Inject
    @VirtualThreads
    ExecutorService virtualThreadExecutor;

    /**
     * Starts async OCR processing for a cookbook.
     * Updates cookbook status and persists results to the database.
     */
    public void startAsyncProcessing(UUID cookbookId) {
        Log.infof("Starting async OCR processing for cookbook %s", cookbookId);

        virtualThreadExecutor.submit(() -> {
            // Activate request context for async execution (needed for RequestScoped beans like OcrAiService)
            ManagedContext requestContext = Arc.container().requestContext();
            requestContext.activate();
            try {
                processAndPersist(cookbookId);
            } catch (Exception e) {
                Log.errorf(e, "Unexpected error in async OCR processing for cookbook %s", cookbookId);
                QuarkusTransaction.requiringNew().run(() -> {
                    Cookbook.updateOcrStatus(cookbookId, Cookbook.OcrStatus.FAILED, e.getMessage());
                });
            } finally {
                requestContext.terminate();
            }
        });
    }

    // Helper record to hold page data outside transaction
    private record PageData(UUID id, int pageOrder, byte[] imageData, String contentType) {}

    private void processAndPersist(UUID cookbookId) {
        // Get pages and their image data in a transaction
        List<PageData> pageDataList = QuarkusTransaction.requiringNew().call(() -> {
            List<CookbookIndexPage> pages = CookbookIndexPage.findByCookbookIdOrdered(cookbookId);
            // Eagerly fetch image data while in transaction
            return pages.stream()
                    .map(p -> new PageData(p.id, p.pageOrder, p.imageData, p.contentType))
                    .toList();
        });

        if (pageDataList.isEmpty()) {
            QuarkusTransaction.requiringNew().run(() -> {
                Cookbook.updateOcrStatus(cookbookId, Cookbook.OcrStatus.FAILED, "No index pages found for cookbook");
            });
            return;
        }

        // Clear any existing OCR results
        QuarkusTransaction.requiringNew().run(() -> {
            OcrResultEntity.deleteByCookbookId(cookbookId);
        });

        int totalPages = pageDataList.size();
        List<PageError> failedPages = new ArrayList<>();

        for (PageData pageData : pageDataList) {
            try {
                // Process page (AI call - outside transaction)
                OcrResult result = extractRecipesFromData(pageData.imageData, pageData.contentType);

                // Persist results in a new transaction
                QuarkusTransaction.requiringNew().run(() -> {
                    for (OcrResult.ExtractedRecipe recipe : result.recipes()) {
                        OcrResultEntity entity = OcrResultEntity.create(
                                cookbookId,
                                recipe.ingredient(),
                                recipe.recipeName(),
                                recipe.pageNumber(),
                                recipe.confidence(),
                                recipe.needsReview()
                        );
                        entity.persist();
                    }
                });

                Log.infof("Processed page %d of cookbook %s: found %d recipes",
                        pageData.pageOrder, cookbookId, result.recipes().size());

            } catch (Exception e) {
                Log.errorf(e, "OCR processing failed for page %d of cookbook %s", pageData.pageOrder, cookbookId);
                failedPages.add(new PageError(pageData.pageOrder, e.getMessage()));
            }
        }

        // Determine final status
        Cookbook.OcrStatus finalStatus;
        String errorMessage = null;

        if (failedPages.size() == totalPages) {
            finalStatus = Cookbook.OcrStatus.FAILED;
            errorMessage = "All pages failed to process";
        } else if (!failedPages.isEmpty()) {
            finalStatus = Cookbook.OcrStatus.COMPLETED_WITH_ERRORS;
            errorMessage = String.format("%d of %d pages failed", failedPages.size(), totalPages);
        } else {
            finalStatus = Cookbook.OcrStatus.COMPLETED;
        }

        // Update cookbook status
        final Cookbook.OcrStatus status = finalStatus;
        final String errMsg = errorMessage;
        QuarkusTransaction.requiringNew().run(() -> {
            Cookbook.updateOcrStatus(cookbookId, status, errMsg);
        });

        Log.infof("OCR processing completed for cookbook %s with status %s", cookbookId, finalStatus);
    }

    /**
     * Synchronous processing - returns results directly.
     * Used for tests and backward compatibility.
     */
    public OcrProgressResponse processPages(UUID cookbookId, List<CookbookIndexPage> pages) {
        Log.infof("Starting OCR processing for cookbook %s", cookbookId);

        if (pages.isEmpty()) {
            return createFailedResponse("No index pages found for cookbook");
        }

        int totalPages = pages.size();
        List<OcrResultDto> allResults = new ArrayList<>();
        List<PageError> failedPages = new ArrayList<>();
        int currentPage = 0;

        for (CookbookIndexPage page : pages) {
            currentPage = page.pageOrder;
            try {
                OcrResult result = extractRecipes(page);

                for (OcrResult.ExtractedRecipe recipe : result.recipes()) {
                    allResults.add(new OcrResultDto(
                            recipe.ingredient(),
                            recipe.recipeName(),
                            recipe.pageNumber(),
                            recipe.confidence(),
                            recipe.needsReview()
                    ));
                }
            } catch (Exception e) {
                Log.errorf(e, "OCR processing failed for page %d of cookbook %s", page.pageOrder, cookbookId);
                failedPages.add(new PageError(page.pageOrder, e.getMessage()));
            }
        }

        OcrProgressResponse.Status status;
        if (failedPages.size() == totalPages) {
            status = OcrProgressResponse.Status.FAILED;
        } else if (!failedPages.isEmpty()) {
            status = OcrProgressResponse.Status.COMPLETED_WITH_ERRORS;
        } else {
            status = OcrProgressResponse.Status.COMPLETED;
        }

        return new OcrProgressResponse(
                status,
                currentPage,
                totalPages,
                allResults,
                null,
                failedPages
        );
    }

    private OcrResult extractRecipes(CookbookIndexPage page) {
        return extractRecipesFromData(page.imageData, page.contentType);
    }

    private OcrResult extractRecipesFromData(byte[] imageData, String contentType) {
        String base64 = Base64.getEncoder().encodeToString(imageData);

        Image image = Image.builder()
                .base64Data(base64)
                .mimeType(contentType)
                .build();

        return aiService.extract(image);
    }

    private OcrProgressResponse createFailedResponse(String message) {
        return new OcrProgressResponse(
                OcrProgressResponse.Status.FAILED,
                0,
                0,
                List.of(),
                message
        );
    }
}
