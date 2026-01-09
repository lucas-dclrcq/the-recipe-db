# Feature: Viewing & Searching Recipes

## What

Allow users to browse, search, and view recipes across all imported cookbooks. Users can:
1. Browse all recipes with infinite scroll
2. Search by recipe name, ingredient, or cookbook
3. Filter results by cookbook or ingredient
4. View detailed recipe information on a dedicated page
5. View all recipes within a specific cookbook

## Why

After importing cookbooks (Feature 001), users need a way to find recipes. The core value proposition of the-recipe-db is answering: "Which cookbook has a recipe with ingredient X?" or "Where was that recipe I remember?"

### User Problems Solved
- "I want to cook with chicken tonight - which of my cookbooks have chicken recipes?"
- "I remember a pasta recipe but can't recall which book it's in"
- "I want to browse what recipes I have from a specific cookbook"

### Value Delivered
- Quick ingredient-based search across entire cookbook collection
- Recipe discovery through browsing
- Easy lookup of page numbers to find recipes in physical books

## Scope

### In Scope
- Home page with recipe browse/search functionality
- Search by: recipe name, ingredient (with autocomplete), cookbook title
- Filter panel for cookbook and ingredient selection
- Recipe card layout showing: name, page number, cookbook, ingredients preview
- Recipe detail page with full information
- Cookbook detail page showing all recipes in that book
- Infinite scroll pagination
- Ingredient autocomplete dropdown

### Out of Scope
- Full-text search within recipe instructions (we only have index data)
- Recipe rating/favoriting
- Shopping list generation
- User accounts
- Recipe editing (covered in separate feature)
- Advanced filters (page range, date added, etc.)

## Data Model

No new entities required. Uses existing:
- **Cookbook**: id, title, author, created_at
- **Recipe**: id, cookbook_id, name, page_number
- **Ingredient**: id, name (normalized)
- **RecipeIngredient**: recipe_id, ingredient_id (join table)

### Query Patterns Needed
| Query | Description |
|-------|-------------|
| List all recipes | Paginated, sorted by name or date added |
| Search recipes by name | ILIKE pattern match on recipe.name |
| Search recipes by ingredient | Join through recipe_ingredients, filter by ingredient.name |
| Search recipes by cookbook | Filter by cookbook_id or ILIKE on cookbook.title |
| Combined filters | AND combination of above filters |
| List ingredients (autocomplete) | Prefix match on ingredient.name, return top N |
| Get recipe by ID | Single recipe with cookbook + ingredients |
| List recipes by cookbook | All recipes where cookbook_id matches |
| Get cookbook by ID | Single cookbook with recipe count |
| List all cookbooks | For filter dropdown |

## User Flow

```
[Home / Browse]
    - Shows recipe cards (infinite scroll)
    - Search bar at top
    - Filter panel (sidebar or collapsible)

    → Type in search bar
        - If ingredient-like: show autocomplete dropdown
        - Press enter or select: filter results

    → Click filter (cookbook dropdown)
        - Select cookbook: filter to that cookbook's recipes

    → Click recipe card
        → [Recipe Detail Page]
            - Recipe name
            - Cookbook title + author
            - Page number
            - All ingredients
            - "Back to search" button

    → Click cookbook name (from card or detail)
        → [Cookbook Detail Page]
            - Cookbook title + author
            - Recipe count
            - All recipes in cookbook (cards)
            - Back button
```

## API Endpoints

### Recipes
| Method | Path | Description |
|--------|------|-------------|
| GET | /api/recipes | List recipes with pagination, search, filters |
| GET | /api/recipes/{id} | Get single recipe with cookbook + ingredients |

**GET /api/recipes Query Parameters:**
- `q` (string): General search term (searches recipe name)
- `ingredient` (string): Filter by exact ingredient name
- `cookbookId` (UUID): Filter by cookbook
- `cursor` (string): Pagination cursor (last seen ID or offset)
- `limit` (int): Page size (default 20)

### Ingredients
| Method | Path | Description |
|--------|------|-------------|
| GET | /api/ingredients | List ingredients for autocomplete |

**GET /api/ingredients Query Parameters:**
- `prefix` (string): Autocomplete prefix match
- `limit` (int): Max results (default 10)

### Cookbooks
| Method | Path | Description |
|--------|------|-------------|
| GET | /api/cookbooks | List all cookbooks (for filter dropdown) |
| GET | /api/cookbooks/{id} | Get cookbook details |
| GET | /api/cookbooks/{id}/recipes | Get all recipes in cookbook |

## UI Components

### Pages
1. **HomePage** - Main browse/search interface
2. **RecipeDetailPage** - Single recipe view (`/recipes/:id`)
3. **CookbookDetailPage** - Single cookbook with its recipes (`/cookbooks/:id`)

### Components
1. **SearchBar** - Text input with search icon, handles search submission
2. **IngredientAutocomplete** - Dropdown showing matching ingredients as user types
3. **FilterPanel** - Sidebar/collapsible with cookbook dropdown, active filters display
4. **RecipeCard** - Card showing recipe name, page, cookbook, ingredient tags
5. **RecipeList** - Container managing recipe cards + infinite scroll
6. **CookbookSelector** - Dropdown for cookbook filter
7. **ActiveFilters** - Pills showing current filters with remove buttons

### Composables
1. **useRecipeSearch** - State management for search query, filters, results
2. **useIngredientAutocomplete** - Debounced ingredient prefix search
3. **useInfiniteScroll** - Intersection observer for loading more results

## Implementation Phases

### Phase 1: Backend API
- [x] Recipe list endpoint with pagination
- [x] Recipe search by name (q parameter)
- [x] Recipe filter by cookbook
- [x] Recipe filter by ingredient
- [x] Combined filters support
- [x] Single recipe endpoint with relations
- [x] Ingredient autocomplete endpoint
- [x] Cookbook list endpoint
- [x] Cookbook detail endpoint
- [x] Cookbook recipes endpoint

### Phase 2: Core Frontend
- [x] Recipe search composable (useRecipeSearch)
- [x] HomePage with SearchBar
- [x] RecipeCard component
- [x] RecipeList with results display
- [x] Basic search functionality (by recipe name)

### Phase 3: Advanced Search
- [x] IngredientAutocomplete component
- [x] useIngredientAutocomplete composable
- [x] FilterPanel component
- [x] CookbookSelector dropdown
- [x] ActiveFilters display
- [x] Combined filter logic

### Phase 4: Detail Pages
- [x] RecipeDetailPage
- [x] CookbookDetailPage
- [x] Navigation between pages
- [x] Back button handling

### Phase 5: Infinite Scroll
- [x] useInfiniteScroll composable
- [x] Integration with RecipeList
- [x] Loading states
- [x] "No more results" handling

### Phase 6: Polish
- [x] Empty states (no recipes, no results)
- [x] Loading skeletons
- [x] Error handling
- [x] Mobile responsive layout
- [x] Keyboard navigation for autocomplete

## Technical Notes

### Pagination Strategy
Using cursor-based pagination with recipe ID as cursor. Benefits:
- Stable results when new recipes added
- Efficient for infinite scroll
- Avoids offset performance issues

### Search Implementation
- Recipe name: SQL ILIKE with wildcards
- Ingredient: Exact match after autocomplete selection (ingredient names are normalized)
- Cookbook: Filter by ID (from dropdown selection)

### Autocomplete Debouncing
- 300ms debounce on ingredient autocomplete
- Minimum 2 characters before triggering
- Cache recent results client-side

### State Management
Keep search state in URL query params for:
- Shareable search URLs
- Browser back/forward support
- Refresh persistence
