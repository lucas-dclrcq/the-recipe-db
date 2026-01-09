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
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
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
    void startOcrProcessing_shouldReturnBadRequestWhenNoIndexPages() {
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
                .statusCode(400)
                .body(containsString("No index pages found"));
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

        // Start OCR processing - returns 202 Accepted
        given()
                .contentType(ContentType.JSON)
                .when()
                .post("/api/cookbooks/{id}/ocr/start", cookbookId)
                .then()
                .statusCode(202)
                .body("status", equalTo("PROCESSING"));

        // Wait for processing to complete and verify results
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            given()
                    .when()
                    .get("/api/cookbooks/{id}/ocr/results", cookbookId)
                    .then()
                    .statusCode(200)
                    .body("status", equalTo("COMPLETED"))
                    .body("results.size()", equalTo(3))
                    .body("results.find { it.recipeName == 'Chocolate Cake' && it.ingredient == 'chocolate' }", notNullValue())
                    .body("results.find { it.recipeName == 'Chocolate Cake' && it.ingredient == 'flour' }", notNullValue())
                    .body("results.find { it.recipeName == 'Vanilla Ice Cream' && it.ingredient == 'vanilla' }", notNullValue());
        });
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
                .statusCode(202);

        // Wait for processing to complete
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            given()
                    .when()
                    .get("/api/cookbooks/{id}/ocr/results", cookbookId)
                    .then()
                    .statusCode(200)
                    .body("status", equalTo("COMPLETED"))
                    .body("results.size()", equalTo(2))
                    .body("results.find { it.recipeName == 'Apple Pie' }", notNullValue())
                    .body("results.find { it.recipeName == 'Banana Bread' }", notNullValue());
        });
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

        // Start OCR processing
        given()
                .contentType(ContentType.JSON)
                .when()
                .post("/api/cookbooks/{id}/ocr/start", cookbookId)
                .then()
                .statusCode(202);

        // Wait for processing and verify low confidence items for review
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            given()
                    .when()
                    .get("/api/cookbooks/{id}/ocr/results", cookbookId)
                    .then()
                    .statusCode(200)
                    .body("status", equalTo("COMPLETED"))
                    .body("results[0].confidence", equalTo(0.50f))
                    .body("results[0].needsReview", equalTo(true));
        });
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

        // Start OCR processing
        given()
                .contentType(ContentType.JSON)
                .when()
                .post("/api/cookbooks/{id}/ocr/start", cookbookId)
                .then()
                .statusCode(202);

        // Wait for processing to fail
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            given()
                    .when()
                    .get("/api/cookbooks/{id}/ocr/results", cookbookId)
                    .then()
                    .statusCode(200)
                    .body("status", equalTo("FAILED"));
        });
    }

    @Test
    void startOcrProcessing_shouldContinueProcessingWhenOnePageFails() {
        // Mock first page to succeed, second page to fail
        OcrResult successResult = new OcrResult(List.of(
                new OcrResult.ExtractedRecipe("Apple Pie", 5, "apple", 0.90)
        ));

        Mockito.when(ocrAiService.extract(Mockito.any(Image.class)))
                .thenReturn(successResult)
                .thenThrow(new RuntimeException("AI service unavailable"));

        // Create a cookbook
        String cookbookId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "title": "Partial Success Test",
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
                .statusCode(202);

        // Wait for processing - should return COMPLETED_WITH_ERRORS with partial results
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            given()
                    .when()
                    .get("/api/cookbooks/{id}/ocr/results", cookbookId)
                    .then()
                    .statusCode(200)
                    .body("status", equalTo("COMPLETED_WITH_ERRORS"))
                    .body("results.size()", equalTo(1))
                    .body("results[0].recipeName", equalTo("Apple Pie"));
        });
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
                .statusCode(202);

        // Wait for processing to complete
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            given()
                    .when()
                    .get("/api/cookbooks/{id}/ocr/results", cookbookId)
                    .then()
                    .statusCode(200)
                    .body("status", equalTo("COMPLETED"))
                    .body("results", empty());
        });
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

    @Test
    void startOcrProcessing_shouldReturn409WhenAlreadyProcessing() {
        // Mock AI service to take a while
        OcrResult mockResult = new OcrResult(List.of(
                new OcrResult.ExtractedRecipe("Test Recipe", 1, "test", 0.95)
        ));
        Mockito.when(ocrAiService.extract(Mockito.any(Image.class)))
                .thenAnswer(invocation -> {
                    Thread.sleep(2000); // Simulate slow processing
                    return mockResult;
                });

        // Create a cookbook
        String cookbookId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "title": "Concurrent Test Cookbook",
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

        // Start OCR processing first time
        given()
                .contentType(ContentType.JSON)
                .when()
                .post("/api/cookbooks/{id}/ocr/start", cookbookId)
                .then()
                .statusCode(202);

        // Try to start again while still processing - should return 409
        given()
                .contentType(ContentType.JSON)
                .when()
                .post("/api/cookbooks/{id}/ocr/start", cookbookId)
                .then()
                .statusCode(409)
                .body(containsString("already in progress"));
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
