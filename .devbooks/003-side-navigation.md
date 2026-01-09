 # Feature: Side Navigation

 ## What

 Introduce a persistent side navigation panel to switch between application sections and relocate the "Import new cookbook" entry point into this panel.

 Primary destinations:
 - Recipes: current home page with recipe browse/search
 - Ingredients: list and search ingredients; each ingredient card shows how many recipes use it; clicking opens ingredient details
 - Cookbooks: list and search cookbooks; each cookbook card shows how many recipes it contains; clicking opens cookbook details
 
 Secondary action:
 - Import new cookbook: move the existing import action to a primary button anchored at the bottom of the side panel

 ## Why

 - Provide a clear, consistent way to navigate between core domains (Recipes, Ingredients, Cookbooks)
 - Make the import workflow discoverable from anywhere
 - Establish a scalable information architecture for future sections

 ## Scope

 ### In Scope
 - Left-aligned side panel on desktop/tablet; collapsible/hidden behind a menu on mobile
 - Navigation items: Recipes, Ingredients, Cookbooks
 - Import New Cookbook button anchored to bottom of the side panel
 - New list and detail experiences:
   - Ingredients list with search; cards show recipeCount; card click opens Ingredient detail
   - Ingredient detail shows metadata and the list of recipes using it
   - Cookbooks list with search; cards show recipeCount; card click opens Cookbook detail
   - Cookbook detail (exists) remains; ensure recipeCount available/displayed
 - Active route highlighting, keyboard navigation, and basic accessibility

 ### Out of Scope
 - Visual theming overhaul
 - Favorites/tags, grouping, or custom sections
 - Authorization and multi-user roles

 ## Navigation & Routes

 - SideNav (left panel)
   - Items:
     - Recipes → route: `/` or `/recipes`
     - Ingredients → route: `/ingredients`
     - Cookbooks → route: `/cookbooks`
   - Bottom button:
     - Import New Cookbook → opens existing import flow
 - Behavior
   - Active item highlighted based on current route
   - Collapses to an icon-only rail ≥768px if requested (see Open Questions)
   - Hidden behind a hamburger on small screens (<768px); overlays content when opened

 ## UI/UX Specifications

 ### Side Panel
 - Width: 240px (expanded), 64px (collapsed rail), full-screen overlay on small screens
 - Contents order (top → bottom):
   1) App brand/logo (optional)
   2) Nav items: Recipes, Ingredients, Cookbooks
   3) Flexible spacer
   4) Primary button: Import New Cookbook (full width)
 - Keyboard: Tab order follows visual order; Enter or Space to activate
 - ARIA: `nav` landmark, each item is a `button` or anchor with `aria-current="page"` when active

 ### List Pages
 - Shared layout: page title, search input, results grid/list, empty/loading states
 - Ingredient card: name, recipeCount badge
 - Cookbook card: title, author (if available), recipeCount badge
 - Clicking a card navigates to the corresponding detail page

 ### Detail Pages
 - Ingredient Detail `/ingredients/:id`
   - Header: ingredient name, recipeCount
   - Body: list of recipes using this ingredient (cards or list)
 - Cookbook Detail `/cookbooks/:id`
   - Already exists; ensure recipeCount visible and route accessible from side nav and list cards

 ## Data Model & API Requirements

 Existing entities likely sufficient: Recipe, Cookbook, Ingredient, RecipeIngredient.

 Required API capabilities (new or extended):
 - Ingredients
   - GET `/api/ingredients` with parameters: `q` (search by name), `cursor`, `limit`
   - Return fields per item: `id`, `name`, `recipeCount`
   - GET `/api/ingredients/{id}` returns: `id`, `name`, `recipeCount`
   - GET `/api/ingredients/{id}/recipes` returns paginated list of recipes using the ingredient
 - Cookbooks
   - GET `/api/cookbooks` should include `recipeCount` per cookbook for list display
   - GET `/api/cookbooks/{id}` should include `recipeCount`
   - GET `/api/cookbooks/{id}/recipes` exists (ensure pagination)

 Performance notes:
 - `recipeCount` may be precomputed or computed via count queries; consider caching or materialized counts if needed
 - Use pagination for large lists; consistent `limit` default (e.g., 20)

 ## Components (Frontend)

 - SideNav.vue: renders nav, active states, bottom Import button
 - NavItem.vue: single item with icon + label
 - IngredientCard.vue: shows `name` + `recipeCount`
 - CookbookCard.vue: shows `title` + `author` + `recipeCount`
 - IngredientsListPage.vue: search + result list
 - IngredientDetailPage.vue: header + recipe list
 - CookbooksListPage.vue: search + result list

 ## Composables
 - useSideNavState: expanded/collapsed, mobile open/close
 - useIngredientSearch: term, results, pagination
 - useCookbookSearch: term, results, pagination

 ## Accessibility
 - Nav uses role="navigation" and `aria-current="page"` on active link
 - All interactive elements reachable via keyboard, focus outline visible
 - Sufficient color contrast for active and hover states

 ## Migration/Placement
 - Move current "Import new cookbook" trigger into SideNav as a primary button at the bottom
   - On mobile, place this button inside the slide-out nav; also expose a secondary entry point in page content if nav might be hidden

 ## Acceptance Criteria
 - Side navigation is visible on desktop/tablet and hidden behind a menu on mobile
 - Clicking Recipes, Ingredients, Cookbooks navigates to their routes and highlights the active item
 - Ingredients List shows a search input and cards with `recipeCount`; clicking opens Ingredient Detail
 - Cookbooks List shows a search input and cards with `recipeCount`; clicking opens Cookbook Detail
 - Ingredient Detail shows the ingredient name and all recipes using it (paginated)
 - Cookbook Detail reachable from list and shows `recipeCount`
 - The "Import New Cookbook" button is anchored at the bottom of the side panel and opens the existing import flow
 - Basic a11y and keyboard navigation works

 ## Open Questions
 1) Side panel behavior on desktop:
    - A. Fixed expanded 240px
    - B. Collapsible to 64px rail with icons
    - C. Auto-collapses based on window width only
 2) Mobile behavior:
    - A. Slide-over drawer with overlay
    - B. Push content aside (no overlay)
 3) Iconography:
    - A. Use existing icon set (if any)
    - B. Use a minimal SVG set bundled locally
    - C. No icons, text-only
 4) Default route for Recipes:
    - A. `/`
    - B. `/recipes`
 5) Where else should the Import action appear on mobile when the nav is closed?
    - A. Only inside the drawer
    - B. Also as a floating action button (FAB)
    - C. Also as a top-bar button
 6) Sorting for list pages:
    - A. Alphabetical by name/title
    - B. By `recipeCount` desc
    - C. Recently added (if available)
 7) Should `recipeCount` be exact or eventually consistent (cached) for performance?
    - A. Exact per request
    - B. Cached/materialized and refreshed asynchronously

 ## Implementation Phases

 1) Backend
 - [x] Extend/implement endpoints to return `recipeCount` for ingredients and cookbooks
 - [x] Add ingredient search and pagination
 - [x] Provide ingredient detail and `/recipes` for that ingredient

 2) Frontend
 - [x] Implement SideNav with routes and bottom Import button
 - [x] Implement Ingredients list and detail pages
 - [x] Implement Cookbooks list (if not present) and ensure detail sufficiency
 - [x] Wire composables for search and pagination
 - [x] Responsive and a11y polish

 3) QA
 - [ ] Cross-browser test desktop/tablet/mobile
 - [ ] Verify navigation, counts, and import entry point
 - [ ] Accessibility checks (keyboard, contrast)

 ## Technical Notes
 - Reuse existing infinite scroll and search patterns where possible (e.g., `useInfiniteScroll`, `useIngredientAutocomplete` for inspiration)
 - Avoid breaking existing routes; add redirects if default home route changes
 - Ensure OpenAPI spec stays in sync with any new/extended endpoints