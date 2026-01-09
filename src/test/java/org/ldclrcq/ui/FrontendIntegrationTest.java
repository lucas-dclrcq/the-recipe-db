package org.ldclrcq.ui;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.quarkiverse.playwright.InjectPlaywright;
import io.quarkiverse.playwright.WithPlaywright;
import io.quarkiverse.quinoa.testing.QuinoaTestProfiles;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static io.restassured.RestAssured.given;

@QuarkusTest
@TestProfile(QuinoaTestProfiles.Enable.class)
@WithPlaywright(headless = true)
class FrontendIntegrationTest {

    @InjectPlaywright
    BrowserContext context;

    Page page;

    @TestHTTPResource("/")
    URL rootUrl;

    private String cookbookId;

    @BeforeEach
    void setUp() {
        // Create a new page for each test
        page = context.newPage();
        // Create a cookbook with test recipes for the tests
        cookbookId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "title": "UI Test Cookbook",
                            "author": "Test Chef"
                        }
                        """)
                .when()
                .post("/api/cookbooks")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Import test recipes
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "recipes": [
                                {
                                    "recipeName": "Chocolate Cake",
                                    "pageNumber": 15,
                                    "ingredient": "chocolate",
                                    "keep": true
                                },
                                {
                                    "recipeName": "Chocolate Cake",
                                    "pageNumber": 15,
                                    "ingredient": "flour",
                                    "keep": true
                                },
                                {
                                    "recipeName": "Chocolate Cake",
                                    "pageNumber": 15,
                                    "ingredient": "sugar",
                                    "keep": true
                                },
                                {
                                    "recipeName": "Vanilla Ice Cream",
                                    "pageNumber": 42,
                                    "ingredient": "vanilla",
                                    "keep": true
                                },
                                {
                                    "recipeName": "Vanilla Ice Cream",
                                    "pageNumber": 42,
                                    "ingredient": "cream",
                                    "keep": true
                                },
                                {
                                    "recipeName": "Apple Pie",
                                    "pageNumber": 78,
                                    "ingredient": "apple",
                                    "keep": true
                                },
                                {
                                    "recipeName": "Apple Pie",
                                    "pageNumber": 78,
                                    "ingredient": "flour",
                                    "keep": true
                                },
                                {
                                    "recipeName": "Apple Pie",
                                    "pageNumber": 78,
                                    "ingredient": "sugar",
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

    // =============== Recipes Page Tests ===============

    @Test
    @DisplayName("Recipes: should display recipes page with header")
    void recipesPage_shouldDisplayHeader() {
        page.navigate(rootUrl.toString() + "recipes");

        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Recipes"))).isVisible();
    }

    @Test
    @DisplayName("Recipes: should show loading state or content")
    void recipesPage_shouldShowLoadingOrContent() {
        page.navigate(rootUrl.toString() + "recipes");

        // Wait for page to be fully loaded
        page.waitForLoadState();

        // The page should have either:
        // - A grid with recipe cards
        // - A "No recipes found" message
        // - A loading skeleton (if still loading)
        // Any of these indicate the recipes page is functioning
        var recipeGrid = page.locator(".grid").first();
        assertThat(recipeGrid).isVisible();
    }

    @Test
    @DisplayName("Recipes: should have search functionality")
    void recipesPage_shouldHaveSearchFunctionality() {
        page.navigate(rootUrl.toString() + "recipes");

        // Wait for initial load
        page.waitForSelector("text=Chocolate Cake");

        // Find and use search input
        var searchInput = page.getByPlaceholder("Search recipes...");
        assertThat(searchInput).isVisible();

        searchInput.fill("Vanilla");
        searchInput.press("Enter");

        // Wait for filtered results
        page.waitForSelector("text=Vanilla Ice Cream");

        assertThat(page.getByText("Vanilla Ice Cream").first()).isVisible();
    }

    @Test
    @DisplayName("Recipes: should show page number on recipe cards")
    void recipesPage_shouldShowPageNumber() {
        page.navigate(rootUrl.toString() + "recipes");

        page.waitForSelector("text=Chocolate Cake");

        // Recipe cards show page number like "p. 15"
        assertThat(page.getByText("p. 15").first()).isVisible();
    }

    @Test
    @DisplayName("Recipes: should show ingredient tags on recipe cards")
    void recipesPage_shouldShowIngredientTags() {
        page.navigate(rootUrl.toString() + "recipes");

        page.waitForSelector("text=Chocolate Cake");

        // Chocolate Cake has chocolate, flour, sugar ingredients
        assertThat(page.getByText("chocolate").first()).isVisible();
    }

    @Test
    @DisplayName("Recipes: should have functional search input")
    void recipesPage_shouldHaveSearchInput() {
        page.navigate(rootUrl.toString() + "recipes");

        // Wait for page to be fully loaded
        page.waitForLoadState();

        // Verify search input is present and functional
        var searchInput = page.getByPlaceholder("Search recipes...");
        assertThat(searchInput).isVisible();

        // Can type into search
        searchInput.fill("test");
        assertThat(searchInput).hasValue("test");
    }

    @Test
    @DisplayName("Recipes: should have filters toggle button")
    void recipesPage_shouldHaveFiltersToggle() {
        page.navigate(rootUrl.toString() + "recipes");

        // The PageSearchBar has a filters toggle button
        var filtersButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Filters"));
        assertThat(filtersButton).isVisible();
    }

    // =============== Cookbooks Page Tests ===============

    @Test
    @DisplayName("Cookbooks: should display cookbooks page with header")
    void cookbooksPage_shouldDisplayHeader() {
        page.navigate(rootUrl.toString() + "cookbooks");

        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Cookbooks"))).isVisible();
    }

    @Test
    @DisplayName("Cookbooks: should display cookbook cards")
    void cookbooksPage_shouldDisplayCookbookCards() {
        page.navigate(rootUrl.toString() + "cookbooks");

        // Wait for cookbooks to load
        page.waitForSelector("text=UI Test Cookbook");

        assertThat(page.getByText("UI Test Cookbook").first()).isVisible();
    }

    @Test
    @DisplayName("Cookbooks: should show author on cookbook cards")
    void cookbooksPage_shouldShowAuthor() {
        page.navigate(rootUrl.toString() + "cookbooks");

        page.waitForSelector("text=UI Test Cookbook");

        assertThat(page.getByText("by Test Chef").first()).isVisible();
    }

    @Test
    @DisplayName("Cookbooks: should show recipe count on cookbook cards")
    void cookbooksPage_shouldShowRecipeCount() {
        page.navigate(rootUrl.toString() + "cookbooks");

        page.waitForSelector("text=UI Test Cookbook");

        // Should show "3 recipes" (Chocolate Cake, Vanilla Ice Cream, Apple Pie)
        assertThat(page.getByText("3 recipes").first()).isVisible();
    }

    @Test
    @DisplayName("Cookbooks: should have search functionality")
    void cookbooksPage_shouldHaveSearchFunctionality() {
        page.navigate(rootUrl.toString() + "cookbooks");

        page.waitForSelector("text=UI Test Cookbook");

        var searchInput = page.getByPlaceholder("Search cookbooks by title or author...");
        assertThat(searchInput).isVisible();

        searchInput.fill("UI Test");
        searchInput.press("Enter");

        page.waitForTimeout(500); // Allow search to complete

        assertThat(page.getByText("UI Test Cookbook").first()).isVisible();
    }

    @Test
    @DisplayName("Cookbooks: should navigate to cookbook detail when clicking a cookbook")
    void cookbooksPage_shouldNavigateToCookbookDetail() {
        page.navigate(rootUrl.toString() + "cookbooks");

        page.waitForSelector("text=UI Test Cookbook");

        page.getByText("UI Test Cookbook").first().click();

        page.waitForURL("**/cookbooks/**");

        // Use heading role to be more specific (detail page has H1 with cookbook title)
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("UI Test Cookbook"))).isVisible();
    }

    // =============== Ingredients Page Tests ===============

    @Test
    @DisplayName("Ingredients: should display ingredients page with header")
    void ingredientsPage_shouldDisplayHeader() {
        page.navigate(rootUrl.toString() + "ingredients");

        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Ingredients"))).isVisible();
    }

    @Test
    @DisplayName("Ingredients: should display ingredient cards")
    void ingredientsPage_shouldDisplayIngredientCards() {
        page.navigate(rootUrl.toString() + "ingredients");

        // Wait for ingredients to load
        page.waitForSelector("text=chocolate");

        assertThat(page.getByText("chocolate").first()).isVisible();
    }

    @Test
    @DisplayName("Ingredients: should have search functionality")
    void ingredientsPage_shouldHaveSearchFunctionality() {
        page.navigate(rootUrl.toString() + "ingredients");

        page.waitForSelector("text=chocolate");

        var searchInput = page.getByPlaceholder("Search ingredients...");
        assertThat(searchInput).isVisible();

        searchInput.fill("vanilla");
        searchInput.press("Enter");

        page.waitForSelector("text=vanilla");

        assertThat(page.getByText("vanilla").first()).isVisible();
    }

    @Test
    @DisplayName("Ingredients: should have filters toggle button")
    void ingredientsPage_shouldHaveFiltersToggle() {
        page.navigate(rootUrl.toString() + "ingredients");

        var filtersButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Filters"));
        assertThat(filtersButton).isVisible();
    }

    @Test
    @DisplayName("Ingredients: should navigate to ingredient detail when clicking an ingredient")
    void ingredientsPage_shouldNavigateToIngredientDetail() {
        page.navigate(rootUrl.toString() + "ingredients");

        page.waitForSelector("text=chocolate");

        page.getByText("chocolate").first().click();

        page.waitForURL("**/ingredients/**");

        // Use heading role to be more specific (detail page has H1 with ingredient name)
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("chocolate"))).isVisible();
    }

    // =============== Navigation Tests ===============

    @Test
    @DisplayName("Navigation: should have navigation sidebar")
    void navigation_shouldHaveNavigationSidebar() {
        page.navigate(rootUrl.toString());

        // Check sidebar navigation links exist
        assertThat(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Recipes"))).isVisible();
        assertThat(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Ingredients"))).isVisible();
        assertThat(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Cookbooks"))).isVisible();
    }

    @Test
    @DisplayName("Navigation: should navigate from recipes to cookbooks")
    void navigation_shouldNavigateFromRecipesToCookbooks() {
        page.navigate(rootUrl.toString() + "recipes");

        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Cookbooks")).click();

        page.waitForURL("**/cookbooks");

        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Cookbooks"))).isVisible();
    }

    @Test
    @DisplayName("Navigation: should navigate from cookbooks to ingredients")
    void navigation_shouldNavigateFromCookbooksToIngredients() {
        page.navigate(rootUrl.toString() + "cookbooks");

        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Ingredients")).click();

        page.waitForURL("**/ingredients");

        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Ingredients"))).isVisible();
    }

    @Test
    @DisplayName("Navigation: should navigate from ingredients to recipes")
    void navigation_shouldNavigateFromIngredientsToRecipes() {
        page.navigate(rootUrl.toString() + "ingredients");

        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Recipes")).click();

        page.waitForURL("**/recipes");

        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Recipes"))).isVisible();
    }

    @Test
    @DisplayName("Navigation: should have Import New Cookbook button in sidebar")
    void navigation_shouldHaveImportButton() {
        page.navigate(rootUrl.toString());

        assertThat(page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Import New Cookbook"))).isVisible();
    }

    @Test
    @DisplayName("Navigation: should navigate to import page when clicking import button")
    void navigation_shouldNavigateToImportPage() {
        page.navigate(rootUrl.toString());

        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Import New Cookbook")).click();

        page.waitForURL("**/import");
    }

    @Test
    @DisplayName("Navigation: should show app title in sidebar")
    void navigation_shouldShowAppTitle() {
        page.navigate(rootUrl.toString());

        // Use first() since the title appears in the sidebar (may be duplicated in multiple places)
        assertThat(page.getByText("The Recipe DB").first()).isVisible();
    }

    @Test
    @DisplayName("Navigation: root URL should redirect to recipes page")
    void navigation_rootShouldRedirectToRecipes() {
        page.navigate(rootUrl.toString());

        page.waitForURL("**/recipes");

        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Recipes"))).isVisible();
    }
}
