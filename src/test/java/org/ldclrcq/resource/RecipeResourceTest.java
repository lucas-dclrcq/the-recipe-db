package org.ldclrcq.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class RecipeResourceTest {

    private String cookbookId;

    @BeforeEach
    void setUp() {
        // Create a cookbook for test recipes
        cookbookId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "title": "Test Cookbook for Recipes",
                            "author": "Test Author"
                        }
                        """)
                .when()
                .post("/api/cookbooks")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Import some test recipes
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "recipes": [
                                {
                                    "recipeName": "Spaghetti Bolognese",
                                    "pageNumber": 10,
                                    "ingredient": "ground beef",
                                    "keep": true
                                },
                                {
                                    "recipeName": "Spaghetti Bolognese",
                                    "pageNumber": 10,
                                    "ingredient": "tomato sauce",
                                    "keep": true
                                },
                                {
                                    "recipeName": "Spaghetti Bolognese",
                                    "pageNumber": 10,
                                    "ingredient": "pasta",
                                    "keep": true
                                },
                                {
                                    "recipeName": "Caesar Salad",
                                    "pageNumber": 20,
                                    "ingredient": "romaine lettuce",
                                    "keep": true
                                },
                                {
                                    "recipeName": "Caesar Salad",
                                    "pageNumber": 20,
                                    "ingredient": "parmesan",
                                    "keep": true
                                },
                                {
                                    "recipeName": "Chicken Parmesan",
                                    "pageNumber": 30,
                                    "ingredient": "chicken breast",
                                    "keep": true
                                },
                                {
                                    "recipeName": "Chicken Parmesan",
                                    "pageNumber": 30,
                                    "ingredient": "parmesan",
                                    "keep": true
                                }
                            ]
                        }
                        """)
                .when()
                .post("/api/cookbooks/{id}/confirm", cookbookId)
                .then()
                .statusCode(200);
    }

    @Test
    void listRecipes_shouldReturnAllRecipes() {
        given()
                .when()
                .get("/api/recipes")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("recipes", not(empty()))
                .body("hasMore", instanceOf(Boolean.class));
    }

    @Test
    void listRecipes_shouldFilterByNameQuery() {
        given()
                .queryParam("q", "Spaghetti")
                .when()
                .get("/api/recipes")
                .then()
                .statusCode(200)
                .body("recipes.size()", greaterThanOrEqualTo(1))
                .body("recipes.every { it.name.toLowerCase().contains('spaghetti') }", equalTo(true));
    }

    @Test
    void listRecipes_shouldFilterByIngredient() {
        given()
                .queryParam("ingredient", "parmesan")
                .when()
                .get("/api/recipes")
                .then()
                .statusCode(200)
                .body("recipes.size()", greaterThanOrEqualTo(2)) // Caesar Salad and Chicken Parmesan
                .body("recipes.every { it.ingredients.contains('parmesan') }", equalTo(true));
    }

    @Test
    void listRecipes_shouldFilterByCookbookId() {
        given()
                .queryParam("cookbookId", cookbookId)
                .when()
                .get("/api/recipes")
                .then()
                .statusCode(200)
                .body("recipes.size()", equalTo(3)) // 3 recipes in our test cookbook
                .body("recipes.every { it.cookbookId == '%s' }".formatted(cookbookId), equalTo(true));
    }

    @Test
    void listRecipes_shouldSupportCombinedFilters() {
        given()
                .queryParam("q", "Chicken")
                .queryParam("cookbookId", cookbookId)
                .when()
                .get("/api/recipes")
                .then()
                .statusCode(200)
                .body("recipes.size()", greaterThanOrEqualTo(1))
                .body("recipes.find { it.name == 'Chicken Parmesan' }", notNullValue());
    }

    @Test
    void listRecipes_shouldSupportPagination() {
        given()
                .queryParam("limit", 1)
                .when()
                .get("/api/recipes")
                .then()
                .statusCode(200)
                .body("recipes.size()", equalTo(1))
                .body("hasMore", equalTo(true))
                .body("nextCursor", notNullValue());
    }

    @Test
    void listRecipes_shouldSupportCursorPagination() {
        // Get first page
        String nextCursor = given()
                .queryParam("limit", 1)
                .when()
                .get("/api/recipes")
                .then()
                .statusCode(200)
                .body("recipes.size()", equalTo(1))
                .extract()
                .path("nextCursor");

        // Get second page using cursor
        given()
                .queryParam("limit", 1)
                .queryParam("cursor", nextCursor)
                .when()
                .get("/api/recipes")
                .then()
                .statusCode(200)
                .body("recipes.size()", equalTo(1));
    }

    @Test
    void getRecipe_shouldReturnRecipeById() {
        // First, get a recipe ID from the list
        String recipeId = given()
                .queryParam("q", "Spaghetti")
                .when()
                .get("/api/recipes")
                .then()
                .statusCode(200)
                .extract()
                .path("recipes[0].id");

        // Get the recipe by ID
        given()
                .when()
                .get("/api/recipes/{id}", recipeId)
                .then()
                .statusCode(200)
                .body("id", equalTo(recipeId))
                .body("name", equalTo("Spaghetti Bolognese"))
                .body("pageNumber", equalTo(10))
                .body("cookbookId", equalTo(cookbookId))
                .body("cookbookTitle", equalTo("Test Cookbook for Recipes"))
                .body("cookbookAuthor", equalTo("Test Author"))
                .body("ingredients", hasItems("ground beef", "tomato sauce", "pasta"));
    }

    @Test
    void getRecipe_shouldReturn404ForNonExistentRecipe() {
        UUID randomId = UUID.randomUUID();

        given()
                .when()
                .get("/api/recipes/{id}", randomId)
                .then()
                .statusCode(404);
    }

    @Test
    void listRecipes_shouldReturnEmptyWhenNoMatch() {
        given()
                .queryParam("q", "NonExistentRecipeName12345")
                .when()
                .get("/api/recipes")
                .then()
                .statusCode(200)
                .body("recipes", empty())
                .body("hasMore", equalTo(false))
                .body("nextCursor", nullValue());
    }

    @Test
    void listRecipes_shouldRespectMaxLimit() {
        // Try to request more than MAX_LIMIT (100)
        given()
                .queryParam("limit", 200)
                .when()
                .get("/api/recipes")
                .then()
                .statusCode(200)
                // Should only return up to MAX_LIMIT recipes
                .body("recipes.size()", lessThanOrEqualTo(100));
    }
}

