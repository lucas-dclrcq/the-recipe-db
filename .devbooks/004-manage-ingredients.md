# Feature 004: Manage Ingredients

## Overview

Allow users to view, edit, and manage ingredients in the database. Users can see all ingredients as a searchable/filterable list with recipe counts, edit ingredient names, add disambiguations (aliases), merge duplicate ingredients, and delete unused ingredients.

## User Stories

1. **As a user**, I want to see all ingredients in my database so I can understand what's available
2. **As a user**, I want to search and filter ingredients so I can find specific ones quickly
3. **As a user**, I want to edit an ingredient's name so I can fix typos or normalize naming
4. **As a user**, I want to add disambiguations to an ingredient so searches for "Carotte" find "Carrot"
5. **As a user**, I want to merge duplicate ingredients so my database stays clean
6. **As a user**, I want to delete unused ingredients so I can remove mistakes

## Feature Specifications

### Ingredient List Page (`/ingredients`)

- **Layout**: Grid of ingredient cards (similar to CookbooksPage)
- **Default sort**: Alphabetical (A-Z) by primary name
- **Pagination**: Infinite scroll (load more as user scrolls)
- **Search**: Text search filtering by name AND disambiguations
- **Filters**:
  - Minimum recipe count (e.g., "Show only ingredients with 5+ recipes")
  - Has disambiguations (yes/no/all)

### Ingredient Card

- Display ingredient name (primary/canonical)
- Show recipe count badge
- Show disambiguations inline: "Also: Carotte, Carrots"
- Checkbox for multi-select (merge functionality)
- Click navigates to detail page

### Ingredient Detail Page (`/ingredients/:id`)

- **View mode**:
  - Primary name (large)
  - List of disambiguations (aliases)
  - Recipe count
  - Preview of 5-10 recipes using this ingredient with "View all" link
  - Edit button
  - Delete button (only shown if recipe count = 0)
- **Edit mode**:
  - Editable primary name field
  - Editable disambiguations list (add/remove)
  - Save/Cancel buttons

### Merge Functionality

- **Trigger**: Select 2+ ingredients via checkboxes, click "Merge" button
- **Merge dialog**:
  - Select which ingredient becomes the primary (target)
  - Preview: shows what will happen
  - Confirm button
- **Merge operation**:
  - Target ingredient keeps its name as primary
  - All other selected ingredients' names become disambiguations on target
  - All disambiguations from merged ingredients are combined onto target
  - All recipe associations are updated to point to target ingredient
  - Merged ingredients are deleted
- **No undo**: Merge is permanent (confirmation dialog warns user)

### Delete Functionality

- Only available when ingredient has 0 recipe associations
- Confirmation dialog before deletion
- Deletes ingredient and all its disambiguations

### Search Behavior

- Searching for any disambiguation finds the parent ingredient
- Example: Searching "Carotte" finds "Carrot" (if Carotte is a disambiguation of Carrot)

---

## Technical Design

### Database Changes

New table for disambiguations:

```sql
CREATE TABLE ingredient_disambiguation (
    id UUID PRIMARY KEY DEFAULT gen_uuid_v7(),
    ingredient_id UUID NOT NULL REFERENCES ingredient(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(name) -- disambiguation names must be globally unique
);

CREATE INDEX idx_disambiguation_ingredient ON ingredient_disambiguation(ingredient_id);
CREATE INDEX idx_disambiguation_name ON ingredient_disambiguation(name);
```

### Backend API Changes

#### New/Modified Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/ingredients` | List ingredients with search/filter (MODIFY) |
| `GET` | `/api/ingredients/:id` | Get ingredient detail (MODIFY) |
| `PUT` | `/api/ingredients/:id` | Update ingredient name and disambiguations (NEW) |
| `DELETE` | `/api/ingredients/:id` | Delete ingredient (NEW) |
| `POST` | `/api/ingredients/merge` | Merge multiple ingredients (NEW) |

#### Modified: GET /api/ingredients

**Query params**:
- `q` (string): Search query - searches name AND disambiguations
- `minRecipeCount` (int): Filter by minimum recipe count
- `hasDisambiguations` (boolean): Filter by presence of disambiguations
- `cursor` (UUID): Pagination cursor
- `limit` (int): Page size (default 20, max 100)

**Response**:
```json
{
  "ingredients": [
    {
      "id": "uuid",
      "name": "carrot",
      "disambiguations": ["carotte", "carrots"],
      "recipeCount": 15
    }
  ],
  "nextCursor": "uuid",
  "hasMore": true
}
```

#### Modified: GET /api/ingredients/:id

**Response**:
```json
{
  "id": "uuid",
  "name": "carrot",
  "disambiguations": ["carotte", "carrots"],
  "recipeCount": 15,
  "recipes": [
    {
      "id": "uuid",
      "name": "Carrot Cake",
      "cookbookTitle": "Best Desserts",
      "pageNumber": 42
    }
  ],
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

#### New: PUT /api/ingredients/:id

**Request**:
```json
{
  "name": "carrot",
  "disambiguations": ["carotte", "carrots", "karrotte"]
}
```

**Response**: Updated ingredient (same as GET)

**Validation**:
- Name cannot be empty
- Name must be unique (not exist as another ingredient's name or disambiguation)
- Disambiguations must be unique globally

#### New: DELETE /api/ingredients/:id

**Response**: 204 No Content

**Validation**:
- Ingredient must have 0 recipe associations
- Returns 409 Conflict if recipes exist

#### New: POST /api/ingredients/merge

**Request**:
```json
{
  "targetId": "uuid",
  "sourceIds": ["uuid1", "uuid2"]
}
```

**Response**: Merged ingredient (same as GET /api/ingredients/:id)

**Operation**:
1. Validate all IDs exist
2. Add source ingredient names as disambiguations on target
3. Add all source disambiguations to target
4. Update all recipe_ingredient rows: change source ingredient_ids to target_id
5. Delete source ingredients (CASCADE deletes their disambiguations)
6. Return updated target ingredient

---

## Implementation Phases

### Phase 1: Database & Entity Layer ✅ COMPLETED

1. ✅ Create Flyway migration for `ingredient_disambiguation` table
2. ✅ Create `IngredientDisambiguation` Panache entity
3. ✅ Update `Ingredient` entity with relationship to disambiguations
4. ✅ Update `Ingredient.findByPrefix` to also search disambiguations
5. ✅ Add query methods for filters (minRecipeCount, hasDisambiguations)

### Phase 2: Backend API ✅ COMPLETED

1. ✅ Create DTOs:
   - `IngredientDetailResponse`
   - `UpdateIngredientRequest`
   - `MergeIngredientsRequest`
   - `IngredientListResponse` (with pagination)
2. ✅ Update `IngredientResource.listIngredients()` with new filters and response
3. ✅ Update `IngredientResource.getIngredient()` with full detail response
4. ✅ Add `IngredientResource.updateIngredient()` endpoint
5. ✅ Add `IngredientResource.deleteIngredient()` endpoint
6. ✅ Add `IngredientResource.mergeIngredients()` endpoint
7. ✅ Write integration tests for all endpoints (37 tests)

### Phase 3: Frontend - Ingredient List Page ✅ COMPLETED

1. ✅ Create `IngredientsPage.vue` view component
2. ✅ Create `IngredientCard.vue` component
3. ✅ Create `useIngredients` composable
4. ✅ Add route `/ingredients` to router
5. ✅ Add "Ingredients" link to SideNav (already existed)
6. ✅ Implement search bar and filters
7. ✅ Implement infinite scroll pagination
8. ✅ Implement checkbox selection for merge
9. ✅ Add "Merge" button (disabled until 2+ selected)

### Phase 4: Frontend - Ingredient Detail Page ✅ COMPLETED

1. ✅ Create `IngredientDetailPage.vue` view component
2. ✅ Add route `/ingredients/:id` to router (already existed from Phase 3)
3. ✅ Implement view mode with recipe preview
4. ✅ Implement edit mode with name and disambiguations editing
5. ✅ Implement delete functionality with confirmation
6. ✅ Create `useIngredient` composable for single ingredient operations

### Phase 5: Frontend - Merge Flow ✅ COMPLETED

1. ✅ Create `MergeIngredientsDialog.vue` component
2. ✅ Implement target selection UI
3. ✅ Show merge preview
4. ✅ Implement merge confirmation and API call
5. ✅ Handle success/error states

### Phase 6: Polish & Testing ✅ COMPLETED

1. ✅ Add loading skeletons for ingredient cards
2. ✅ Add error handling and error states
3. ✅ Ensure mobile responsiveness
4. ✅ Add keyboard navigation support
5. ✅ Manual testing of all flows
6. ✅ Update API client (`npm run generate-api`)

---

## UI Mockups (ASCII)

### Ingredient List Page

```
┌─────────────────────────────────────────────────────────────┐
│ Ingredients                                    [Merge (3)]  │
├─────────────────────────────────────────────────────────────┤
│ ┌─────────────────────────────────────────────────────────┐ │
│ │ [Search ingredients...]                    [Filters ▼]  │ │
│ └─────────────────────────────────────────────────────────┘ │
│                                                             │
│ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐         │
│ │ [✓] Carrot   │ │ [ ] Onion    │ │ [✓] Tomato   │         │
│ │ Also: Carotte│ │              │ │ Also: Tomate │         │
│ │ 15 recipes   │ │ 42 recipes   │ │ 8 recipes    │         │
│ └──────────────┘ └──────────────┘ └──────────────┘         │
│ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐         │
│ │ [✓] Garlic   │ │ [ ] Pepper   │ │ [ ] Salt     │         │
│ │              │ │ Also: Poivre │ │              │         │
│ │ 38 recipes   │ │ 22 recipes   │ │ 156 recipes  │         │
│ └──────────────┘ └──────────────┘ └──────────────┘         │
│                                                             │
│                    [Loading more...]                        │
└─────────────────────────────────────────────────────────────┘
```

### Ingredient Detail Page (View Mode)

```
┌─────────────────────────────────────────────────────────────┐
│ ← Back                                                      │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│ Carrot                                          [Edit]      │
│                                                             │
│ Also known as: Carotte, Carrots, Karrotte                  │
│                                                             │
│ Used in 15 recipes                                          │
│                                                             │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │ Recipes using this ingredient                           │ │
│ ├─────────────────────────────────────────────────────────┤ │
│ │ • Carrot Cake (Best Desserts, p. 42)                   │ │
│ │ • Beef Stew (Home Cooking, p. 78)                      │ │
│ │ • Glazed Carrots (Vegetable Bible, p. 23)              │ │
│ │ • Carrot Soup (Soups & Stews, p. 15)                   │ │
│ │ • Garden Salad (Fresh & Light, p. 8)                   │ │
│ │                                                         │ │
│ │                   [View all 15 recipes →]               │ │
│ └─────────────────────────────────────────────────────────┘ │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Ingredient Detail Page (Edit Mode)

```
┌─────────────────────────────────────────────────────────────┐
│ ← Back                                                      │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│ Edit Ingredient                                             │
│                                                             │
│ Name                                                        │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │ carrot                                                  │ │
│ └─────────────────────────────────────────────────────────┘ │
│                                                             │
│ Disambiguations (aliases)                                   │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │ [carotte ×] [carrots ×] [karrotte ×]                   │ │
│ │ ┌──────────────────────────────┐                       │ │
│ │ │ Add new alias...             │  [Add]                │ │
│ │ └──────────────────────────────┘                       │ │
│ └─────────────────────────────────────────────────────────┘ │
│                                                             │
│                              [Cancel]  [Save Changes]       │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Merge Dialog

```
┌─────────────────────────────────────────────────────────────┐
│ Merge Ingredients                                      [×]  │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│ Select the primary ingredient (others will merge into it):  │
│                                                             │
│ ○ Carrot (15 recipes)                                       │
│   Existing aliases: Carotte                                 │
│                                                             │
│ ● Tomato (8 recipes)  ← selected as target                  │
│   Existing aliases: Tomate                                  │
│                                                             │
│ ○ Garlic (38 recipes)                                       │
│   No existing aliases                                       │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│ Preview:                                                    │
│ • "Tomato" will become the primary name                    │
│ • "Carrot" and "Garlic" will be added as aliases           │
│ • Existing aliases (Carotte, Tomate) will be kept          │
│ • 61 recipes will be updated                                │
│                                                             │
│ ⚠️ This action cannot be undone.                            │
│                                                             │
│                              [Cancel]  [Merge Ingredients]  │
└─────────────────────────────────────────────────────────────┘
```

---

## Acceptance Criteria

### List Page
- [ ] Ingredients display in alphabetical order by default
- [ ] Infinite scroll loads more ingredients
- [ ] Search finds ingredients by name OR disambiguation
- [ ] Filters work correctly (min recipe count, has disambiguations)
- [ ] Checkboxes allow multi-select
- [ ] Merge button appears when 2+ ingredients selected
- [ ] Cards show recipe count and disambiguations inline

### Detail Page
- [ ] Shows ingredient name, disambiguations, recipe count
- [ ] Shows preview of 5 recipes with "View all" link
- [ ] Edit button opens edit mode
- [ ] Edit mode allows changing name
- [ ] Edit mode allows adding/removing disambiguations
- [ ] Delete button only shows when recipe count = 0
- [ ] Delete requires confirmation

### Merge
- [ ] Merge dialog shows all selected ingredients
- [ ] User can select target ingredient
- [ ] Preview shows what will happen
- [ ] Merge combines disambiguations from all sources
- [ ] Merge updates all recipe associations
- [ ] Merge deletes source ingredients
- [ ] Success message shown after merge

### Search Integration
- [ ] Searching "Carotte" in recipe search finds recipes with "Carrot"
- [ ] Autocomplete suggests ingredients by name and disambiguation

---

## Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| Merge is destructive | High | Confirmation dialog with preview, no undo by design |
| Disambiguation name conflicts | Medium | Validate uniqueness globally before save |
| Performance with many ingredients | Medium | Cursor pagination, indexed queries |
| User merges wrong ingredients | Medium | Clear preview in merge dialog, require explicit target selection |

---

## Open Questions

1. ~~Should disambiguations be case-sensitive?~~ **Decision: No, normalize like ingredient names**
2. ~~Maximum number of disambiguations per ingredient?~~ **Decision: No limit**
3. ~~Should there be bulk delete for unused ingredients?~~ **Future enhancement**
