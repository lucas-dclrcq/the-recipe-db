package org.ldclrcq.service;

import dev.langchain4j.data.image.Image;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.ldclrcq.dto.OcrProgressResponse;
import org.ldclrcq.dto.OcrResult;
import org.ldclrcq.dto.OcrResultDto;
import org.ldclrcq.entity.CookbookIndexPage;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class OcrService {

    @Inject
    OcrAiService aiService;

    public OcrProgressResponse processPages(UUID cookbookId, List<CookbookIndexPage> pages) {
        Log.infof("Starting OCR processing for cookbook %s", cookbookId);

        if (pages.isEmpty()) {
            return createFailedResponse("No index pages found for cookbook");
        }

        int totalPages = pages.size();
        List<OcrResultDto> allResults = new ArrayList<>();
        int currentPage = 0;

        try {
            for (CookbookIndexPage page : pages) {
                OcrResult result = extractRecipes(page);
                currentPage = page.pageOrder;

                for (OcrResult.ExtractedRecipe recipe : result.recipes()) {
                    allResults.add(new OcrResultDto(
                            recipe.ingredient(),
                            recipe.recipeName(),
                            recipe.pageNumber(),
                            recipe.confidence(),
                            recipe.needsReview()
                    ));
                }
            }

            return new OcrProgressResponse(
                    OcrProgressResponse.Status.COMPLETED,
                    currentPage,
                    totalPages,
                    allResults,
                    null
            );
        } catch (Exception e) {
            Log.error("OCR processing failed", e);
            return new OcrProgressResponse(
                    OcrProgressResponse.Status.FAILED,
                    currentPage,
                    totalPages,
                    allResults,
                    e.getMessage()
            );
        }
    }

    private OcrResult extractRecipes(CookbookIndexPage page) {
        String base64 = Base64.getEncoder().encodeToString(page.imageData);

        Image image = Image.builder()
                .base64Data(base64)
                .mimeType(page.contentType)
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
