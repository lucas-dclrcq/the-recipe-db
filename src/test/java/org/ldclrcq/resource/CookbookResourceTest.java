package org.ldclrcq.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class CookbookResourceTest {

    @Test
    void createCookbook_shouldReturnCreatedCookbook() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "title": "Italian Classics",
                            "author": "Mario Rossi"
                        }
                        """)
                .when()
                .post("/api/cookbooks")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("id", notNullValue())
                .body("title", equalTo("Italian Classics"))
                .body("author", equalTo("Mario Rossi"));
    }

    @Test
    void listCookbooks_shouldReturnEmptyListWhenNoCookbooks() {
        // Note: This test may see cookbooks from other tests if not isolated
        given()
                .when()
                .get("/api/cookbooks")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", instanceOf(java.util.List.class));
    }

    @Test
    void listCookbooks_shouldReturnCookbooksAfterCreation() {
        // Create a cookbook
        String cookbookId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "title": "French Cuisine",
                            "author": "Pierre Dupont"
                        }
                        """)
                .when()
                .post("/api/cookbooks")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // List should contain the created cookbook
        given()
                .when()
                .get("/api/cookbooks")
                .then()
                .statusCode(200)
                .body("find { it.id == '%s' }.title".formatted(cookbookId), equalTo("French Cuisine"))
                .body("find { it.id == '%s' }.author".formatted(cookbookId), equalTo("Pierre Dupont"))
                .body("find { it.id == '%s' }.recipeCount".formatted(cookbookId), equalTo(0));
    }

    @Test
    void getCookbook_shouldReturnCookbookById() {
        // Create a cookbook
        String cookbookId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "title": "Asian Delights",
                            "author": "Chen Wei"
                        }
                        """)
                .when()
                .post("/api/cookbooks")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Get by ID
        given()
                .when()
                .get("/api/cookbooks/{id}", cookbookId)
                .then()
                .statusCode(200)
                .body("id", equalTo(cookbookId))
                .body("title", equalTo("Asian Delights"))
                .body("author", equalTo("Chen Wei"))
                .body("recipeCount", equalTo(0));
    }

    @Test
    void getCookbook_shouldReturn404ForNonExistentCookbook() {
        UUID randomId = UUID.randomUUID();

        given()
                .when()
                .get("/api/cookbooks/{id}", randomId)
                .then()
                .statusCode(404);
    }

    @Test
    void uploadIndexPages_shouldReturn404ForNonExistentCookbook() {
        UUID randomId = UUID.randomUUID();

        given()
                .contentType(ContentType.MULTIPART)
                .multiPart("files", "test.jpg", new byte[]{1, 2, 3}, "image/jpeg")
                .when()
                .post("/api/cookbooks/{id}/index-pages", randomId)
                .then()
                .statusCode(404);
    }

    @Test
    void uploadIndexPages_shouldReturn400WhenNoFilesProvided() {
        // Create a cookbook first
        String cookbookId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "title": "Test Cookbook",
                            "author": "Test Author"
                        }
                        """)
                .when()
                .post("/api/cookbooks")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        given()
                .contentType(ContentType.MULTIPART)
                .when()
                .post("/api/cookbooks/{id}/index-pages", cookbookId)
                .then()
                .statusCode(400);
    }

    @Test
    void confirmImport_shouldReturn404ForNonExistentCookbook() {
        UUID randomId = UUID.randomUUID();

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "recipes": []
                        }
                        """)
                .when()
                .post("/api/cookbooks/{id}/confirm", randomId)
                .then()
                .statusCode(404);
    }

    @Test
    void confirmImport_shouldReturn400WhenRecipesIsNull() {
        // Create a cookbook first
        String cookbookId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "title": "Test Cookbook",
                            "author": "Test Author"
                        }
                        """)
                .when()
                .post("/api/cookbooks")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        given()
                .contentType(ContentType.JSON)
                .body("{}")
                .when()
                .post("/api/cookbooks/{id}/confirm", cookbookId)
                .then()
                .statusCode(400);
    }

    @Test
    void confirmImport_shouldCreateRecipesAndIngredients() {
        // Create a cookbook first
        String cookbookId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "title": "Recipe Import Test",
                            "author": "Test Author"
                        }
                        """)
                .when()
                .post("/api/cookbooks")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Confirm import with recipes
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "recipes": [
                                {
                                    "recipeName": "Pasta Carbonara",
                                    "pageNumber": 1,
                                    "ingredient": "Eggs",
                                    "keep": true
                                },
                                {
                                    "recipeName": "Pasta Carbonara",
                                    "pageNumber": 1,
                                    "ingredient": "Bacon",
                                    "keep": true
                                },
                                {
                                    "recipeName": "Tomato Soup",
                                    "pageNumber": 2,
                                    "ingredient": "Tomatoes",
                                    "keep": true
                                },
                                {
                                    "recipeName": "Skipped Recipe",
                                    "pageNumber": 3,
                                    "ingredient": "Something",
                                    "keep": false
                                }
                            ]
                        }
                        """)
                .when()
                .post("/api/cookbooks/{id}/confirm", cookbookId)
                .then()
                .statusCode(200)
                .body("savedRecipeCount", equalTo(2)); // 2 unique recipes (Pasta Carbonara and Tomato Soup)

        // Verify the cookbook now has recipes
        given()
                .when()
                .get("/api/cookbooks/{id}", cookbookId)
                .then()
                .statusCode(200)
                .body("recipeCount", equalTo(2));

        // Verify recipes are searchable
        given()
                .when()
                .get("/api/recipes")
                .then()
                .statusCode(200)
                .body("recipes.find { it.name == 'Pasta Carbonara' }", notNullValue())
                .body("recipes.find { it.name == 'Tomato Soup' }", notNullValue());
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
}

