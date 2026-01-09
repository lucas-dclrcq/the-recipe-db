package org.ldclrcq.resource;

import dev.langchain4j.data.image.Image;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ldclrcq.dto.OcrResult;
import org.ldclrcq.service.OcrAiService;
import org.mockito.Mockito;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class OcrProcessingTest {

    @InjectMock
    OcrAiService ocrAiService;

    @BeforeEach
    void setUp() {
        Mockito.reset(ocrAiService);
    }

    @Test
    void startOcrProcessing_shouldReturnFailedWhenNoIndexPages() {
        // Create a cookbook without uploading any index pages
        String cookbookId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "title": "Empty Cookbook",
                            "author": "Test Author"
                        }
                        """)
                .when()
                .post("/api/cookbooks")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Start OCR - should fail because no index pages
        given()
                .contentType(ContentType.JSON)
                .when()
                .post("/api/cookbooks/{id}/ocr/start", cookbookId)
                .then()
                .statusCode(200)
                .body("status", equalTo("FAILED"))
                .body("errorMessage", equalTo("No index pages found for cookbook"));
    }

    @Test
    void startOcrProcessing_shouldProcessIndexPagesAndReturnResults() {
        // Mock the AI service to return extracted recipes
        OcrResult mockResult = new OcrResult(List.of(
                new OcrResult.ExtractedRecipe("Chocolate Cake", 15, "chocolate", 0.95),
                new OcrResult.ExtractedRecipe("Chocolate Cake", 15, "flour", 0.92),
                new OcrResult.ExtractedRecipe("Vanilla Ice Cream", 20, "vanilla", 0.88)
        ));
        Mockito.when(ocrAiService.extract(Mockito.any(Image.class))).thenReturn(mockResult);

        // Create a cookbook
        String cookbookId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "title": "OCR Test Cookbook",
                            "author": "Test Chef"
                        }
                        """)
                .when()
                .post("/api/cookbooks")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Upload an index page (simple JPEG header bytes for testing)
        byte[] fakeJpegData = createMinimalJpegBytes();
        given()
                .contentType(ContentType.MULTIPART)
                .multiPart("files", "index1.jpg", fakeJpegData, "image/jpeg")
                .when()
                .post("/api/cookbooks/{id}/index-pages", cookbookId)
                .then()
                .statusCode(200)
                .body("pageCount", equalTo(1));

        // Start OCR processing
        given()
                .contentType(ContentType.JSON)
                .when()
                .post("/api/cookbooks/{id}/ocr/start", cookbookId)
                .then()
                .statusCode(200)
                .body("status", equalTo("COMPLETED"))
                .body("totalPages", equalTo(1))
                .body("results.size()", equalTo(3))
                .body("results.find { it.recipeName == 'Chocolate Cake' && it.ingredient == 'chocolate' }", notNullValue())
                .body("results.find { it.recipeName == 'Chocolate Cake' && it.ingredient == 'flour' }", notNullValue())
                .body("results.find { it.recipeName == 'Vanilla Ice Cream' && it.ingredient == 'vanilla' }", notNullValue())
                .body("errorMessage", nullValue());
    }

    @Test
    void startOcrProcessing_shouldProcessMultipleIndexPages() {
        // Mock different results for each page
        OcrResult page1Result = new OcrResult(List.of(
                new OcrResult.ExtractedRecipe("Apple Pie", 5, "apple", 0.90)
        ));
        OcrResult page2Result = new OcrResult(List.of(
                new OcrResult.ExtractedRecipe("Banana Bread", 10, "banana", 0.85)
        ));

        Mockito.when(ocrAiService.extract(Mockito.any(Image.class)))
                .thenReturn(page1Result)
                .thenReturn(page2Result);

        // Create a cookbook
        String cookbookId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "title": "Multi-Page OCR Test",
                            "author": "Test Chef"
                        }
                        """)
                .when()
                .post("/api/cookbooks")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Upload two index pages
        byte[] fakeJpegData = createMinimalJpegBytes();
        given()
                .contentType(ContentType.MULTIPART)
                .multiPart("files", "index1.jpg", fakeJpegData, "image/jpeg")
                .multiPart("files", "index2.jpg", fakeJpegData, "image/jpeg")
                .when()
                .post("/api/cookbooks/{id}/index-pages", cookbookId)
                .then()
                .statusCode(200)
                .body("pageCount", equalTo(2));

        // Start OCR processing
        given()
                .contentType(ContentType.JSON)
                .when()
                .post("/api/cookbooks/{id}/ocr/start", cookbookId)
                .then()
                .statusCode(200)
                .body("status", equalTo("COMPLETED"))
                .body("totalPages", equalTo(2))
                .body("results.size()", equalTo(2))
                .body("results.find { it.recipeName == 'Apple Pie' }", notNullValue())
                .body("results.find { it.recipeName == 'Banana Bread' }", notNullValue());
    }

    @Test
    void startOcrProcessing_shouldIncludeNeedsReviewFlag() {
        // Mock result with low confidence (needs review)
        OcrResult mockResult = new OcrResult(List.of(
                new OcrResult.ExtractedRecipe("Mystery Dish", 1, "unknown ingredient", 0.50) // Low confidence
        ));
        Mockito.when(ocrAiService.extract(Mockito.any(Image.class))).thenReturn(mockResult);

        // Create a cookbook
        String cookbookId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "title": "Low Confidence Test",
                            "author": "Test Chef"
                        }
                        """)
                .when()
                .post("/api/cookbooks")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Upload an index page
        byte[] fakeJpegData = createMinimalJpegBytes();
        given()
                .contentType(ContentType.MULTIPART)
                .multiPart("files", "index1.jpg", fakeJpegData, "image/jpeg")
                .when()
                .post("/api/cookbooks/{id}/index-pages", cookbookId)
                .then()
                .statusCode(200);

        // Start OCR processing - should mark low confidence items for review
        given()
                .contentType(ContentType.JSON)
                .when()
                .post("/api/cookbooks/{id}/ocr/start", cookbookId)
                .then()
                .statusCode(200)
                .body("status", equalTo("COMPLETED"))
                .body("results[0].confidence", equalTo(0.50f))
                .body("results[0].needsReview", equalTo(true));
    }

    @Test
    void startOcrProcessing_shouldHandleAiServiceException() {
        // Mock AI service to throw an exception
        Mockito.when(ocrAiService.extract(Mockito.any(Image.class)))
                .thenThrow(new RuntimeException("AI service unavailable"));

        // Create a cookbook
        String cookbookId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "title": "Exception Test Cookbook",
                            "author": "Test Chef"
                        }
                        """)
                .when()
                .post("/api/cookbooks")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Upload an index page
        byte[] fakeJpegData = createMinimalJpegBytes();
        given()
                .contentType(ContentType.MULTIPART)
                .multiPart("files", "index1.jpg", fakeJpegData, "image/jpeg")
                .when()
                .post("/api/cookbooks/{id}/index-pages", cookbookId)
                .then()
                .statusCode(200);

        // Start OCR processing - should return FAILED status
        given()
                .contentType(ContentType.JSON)
                .when()
                .post("/api/cookbooks/{id}/ocr/start", cookbookId)
                .then()
                .statusCode(200)
                .body("status", equalTo("FAILED"))
                .body("errorMessage", containsString("AI service unavailable"));
    }

    @Test
    void startOcrProcessing_shouldReturnEmptyResultsWhenNoRecipesExtracted() {
        // Mock AI service to return empty results
        OcrResult emptyResult = new OcrResult(List.of());
        Mockito.when(ocrAiService.extract(Mockito.any(Image.class))).thenReturn(emptyResult);

        // Create a cookbook
        String cookbookId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "title": "Empty Results Test",
                            "author": "Test Chef"
                        }
                        """)
                .when()
                .post("/api/cookbooks")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Upload an index page
        byte[] fakeJpegData = createMinimalJpegBytes();
        given()
                .contentType(ContentType.MULTIPART)
                .multiPart("files", "index1.jpg", fakeJpegData, "image/jpeg")
                .when()
                .post("/api/cookbooks/{id}/index-pages", cookbookId)
                .then()
                .statusCode(200);

        // Start OCR processing
        given()
                .contentType(ContentType.JSON)
                .when()
                .post("/api/cookbooks/{id}/ocr/start", cookbookId)
                .then()
                .statusCode(200)
                .body("status", equalTo("COMPLETED"))
                .body("results", empty())
                .body("errorMessage", nullValue());
    }

    @Test
    void startOcrProcessing_shouldReturn404ForNonExistentCookbook() {
        UUID randomId = UUID.randomUUID();

        given()
                .contentType(ContentType.JSON)
                .when()
                .post("/api/cookbooks/{id}/ocr/start", randomId)
                .then()
                .statusCode(404);
    }

    /**
     * Creates minimal bytes that represent valid-ish image data for testing.
     * The actual content doesn't matter since we're mocking the AI service.
     */
    private byte[] createMinimalJpegBytes() {
        // JPEG magic bytes + some padding
        return new byte[]{
                (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
                0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01,
                0x01, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00
        };
    }
}
