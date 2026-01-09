# Feature: Import a Cookbook

## What

Allow users to import a physical cookbook into the database by:
1. Entering book metadata (title, author)
2. Uploading photos of the index pages
3. Running OCR to extract recipes (name, page number, associated ingredient)
4. Reviewing and confirming extracted data (with ability to edit or skip low-confidence results)

## Why

The core purpose of the-recipe-db is to index physical cookbooks so users can search for recipes by ingredient across all their books. This feature is the foundational entry point - without importing cookbooks, there's nothing to search.

### User Problem
"I have many physical cookbooks but no way to quickly find which book has a recipe using a specific ingredient, or remember where a recipe is located."

### Value Delivered
- Digitizes cookbook indexes without manual data entry
- Captures recipe-to-ingredient relationships for future search
- Preserves page numbers so users can find recipes in their physical books

## Scope

### In Scope
- Create a new cookbook with title and author
- Upload multiple index page images (processed in order)
- Store original images
- OCR via OpenAI vision model (GPT-4o) through LangChain4j
- Extract structured data: recipe name, page number, associated ingredient
- Interactive confirmation flow during import for low-confidence extractions
- User can edit extracted text or skip entries
- Success screen with options: "Import another cookbook" or "Go to home"

### Out of Scope (for this feature)
- Recipe search functionality
- Editing/deleting cookbooks after import
- Editing recipes after import confirmation
- User authentication
- Cover photo for cookbooks
- Index sections/categories

## Data Model

### Cookbook
| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Primary key |
| title | String | Book title |
| author | String | Book author |
| created_at | Timestamp | When imported |

### CookbookIndexPage
| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Primary key |
| cookbook_id | UUID | FK to Cookbook |
| page_order | Integer | Order of the index page (1, 2, 3...) |
| image_data | byte[] | Image binary data (BLOB) |
| content_type | String | MIME type (image/jpeg, image/png) |

### Ingredient
| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Primary key |
| name | String | Ingredient name (normalized: lowercase + trimmed, unique) |

### Recipe
| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Primary key |
| cookbook_id | UUID | FK to Cookbook |
| name | String | Recipe name |
| page_number | Integer | Page in the physical book |

### RecipeIngredient (join table)
| Field | Type | Description |
|-------|------|-------------|
| recipe_id | UUID | FK to Recipe |
| ingredient_id | UUID | FK to Ingredient |
| (composite PK) | | recipe_id + ingredient_id |

## User Flow

```
[Home]
    → Click "Import Cookbook"
    → [Import Form]
        - Enter title
        - Enter author
        - Upload index page image(s)
        - Click "Import"
    → [Processing]
        - OCR runs on each page (in order)
        - Results displayed for confirmation
    → [Confirmation Screen]
        - Show extracted recipes grouped by ingredient
        - Flag low-confidence items
        - User can: Edit / Skip each item
        - Click "Confirm"
    → [Success Screen]
        - "Cookbook imported successfully"
        - Options: "Import another" / "Go to home"
```

## Technical Approach

### OCR Strategy
- Use OpenAI GPT-4o vision model via quarkus-langchain4j
- Send each index page image to the model
- Prompt the model to return structured JSON with:
  - Ingredient groupings
  - Recipe names and page numbers
  - Confidence scores per extraction

### Confidence Scoring
- The LLM will self-report confidence (or we derive it from response patterns)
- Low confidence triggers user review
- Threshold: 80% (items below this are flagged for review)

### Image Storage
- Store original images as BLOB in PostgreSQL
- Binary data stored in `CookbookIndexPage.image_data`
- MIME type tracked in `content_type` field

## Decisions

### Functional Decisions
| Question | Decision |
|----------|----------|
| Confidence threshold | 80% - items below this are flagged for user review |
| Ingredient normalization | Lowercase + trim (e.g., "  Chicken " → "chicken") |
| Duplicate recipes | Single recipe linked to multiple ingredients via join table |
| Image formats | JPEG and PNG only |
| Max pages per cookbook | No limit |
| Existing ingredients | Reuse existing (enables cross-cookbook search) |
| Confirmation UI | Show all extracted recipes at once in scrollable list |
| Upload flow | Upload all images first, then process together |

### Technical Decisions
| Question | Decision |
|----------|----------|
| Frontend framework | Vue 3 with TypeScript |
| CSS approach | Tailwind CSS |
| Component library | Headless UI |
| OCR processing | Async with Server-Sent Events (SSE) for progress |
| API style | REST with explicit request/response DTOs |
| Backend architecture | Hexagonal (Ports & Adapters) |
| Package structure | By layer: domain/, application/ (commands + queries), infrastructure/ |
| Image storage | PostgreSQL BLOB |
| Testing | Integration tests only (@QuarkusTest) |
| API client generation | Orval (generates TypeScript client from OpenAPI spec) |
| OpenAPI spec location | `src/main/webui/src/api/` (generated on build) |

## Implementation Tasks

### Phase 1: Project Setup & Infrastructure

#### 1.1 Backend Package Structure
- [x] Create hexagonal architecture packages:
  - `org.ldclrcq.domain` - Entities, value objects, domain services
  - `org.ldclrcq.application.command` - Command handlers (write operations)
  - `org.ldclrcq.application.query` - Query handlers (read operations)
  - `org.ldclrcq.infrastructure` - Repository implementations, external services
  - `org.ldclrcq.adapter.in.rest` - REST controllers, DTOs
  - `org.ldclrcq.adapter.out.persistence` - JPA entities, Panache repositories
  - `org.ldclrcq.adapter.out.ai` - LangChain4j integration
- [x] Remove example/boilerplate code (ExampleResource, MyEntity, etc.)

#### 1.2 Frontend Setup
- [x] Initialize Vue 3 + TypeScript project in `src/main/webui/`
- [x] Configure Tailwind CSS
- [x] Install and configure Headless UI
- [x] Configure Quinoa integration in `application.properties`
- [x] Set up basic routing (Vue Router)
- [x] Install and configure Orval for API client generation
- [x] Configure build to copy OpenAPI spec to `src/main/webui/src/api/`

#### 1.3 Database Setup
- [x] Create Flyway migration for initial schema:
  - `cookbook` table
  - `cookbook_index_page` table (with BLOB column)
  - `ingredient` table (with unique constraint on normalized name)
  - `recipe` table
  - `recipe_ingredient` join table

### Phase 2: Domain Layer

#### 2.1 Domain Entities
- [x] Create `Cookbook` domain entity (id, title, author, createdAt)
- [x] Create `CookbookIndexPage` domain entity (id, cookbookId, pageOrder, imageData, contentType)
- [x] Create `Ingredient` domain entity (id, name) with normalization logic
- [x] Create `Recipe` domain entity (id, cookbookId, name, pageNumber, ingredients)

#### 2.2 Domain Services
- [x] Create `IngredientNormalizer` service (lowercase + trim)

### Phase 3: Application Layer

#### 3.1 Commands
- [x] Create `CreateCookbookCommand` + handler
- [x] Create `UploadIndexPagesCommand` + handler
- [x] Create `StartOcrProcessingCommand` + handler
- [x] Create `ConfirmImportCommand` + handler (saves recipes + ingredients)

#### 3.2 Queries
- [x] Create `GetCookbookQuery` + handler
- [x] Create `GetOcrProcessingStatusQuery` + handler

#### 3.3 OCR Processing
- [x] Define `OcrResult` DTO (ingredient, recipeName, pageNumber, confidence)
- [x] Define `OcrPort` interface for AI extraction
- [x] Create async processing logic with status tracking

### Phase 4: Infrastructure Layer

#### 4.1 Persistence Adapters
- [x] Create `CookbookJpaEntity` + Panache repository
- [x] Create `CookbookIndexPageJpaEntity` + Panache repository
- [x] Create `IngredientJpaEntity` + Panache repository (with findByNormalizedName)
- [x] Create `RecipeJpaEntity` + Panache repository
- [x] Create mappers between domain entities and JPA entities

#### 4.2 AI Adapter (LangChain4j)
- [x] Configure OpenAI GPT-4o vision model in `application.properties`
- [x] Create `@RegisterAiService` for OCR extraction
- [x] Design prompt for structured JSON extraction (ingredient, recipes, page numbers, confidence)
- [x] Implement `OcrPort` using LangChain4j

### Phase 5: REST API Layer

#### 5.1 DTOs
- [x] Create `CreateCookbookRequest` (title, author)
- [x] Create `CookbookResponse` (id, title, author, createdAt)
- [x] Create `UploadIndexPagesResponse` (cookbookId, pageCount)
- [x] Create `OcrProgressEvent` (status, currentPage, totalPages, results)
- [x] Create `OcrResultDto` (ingredient, recipeName, pageNumber, confidence, needsReview)
- [x] Create `ConfirmImportRequest` (list of confirmed/edited recipes)

#### 5.2 REST Endpoints
- [x] `POST /api/cookbooks` - Create cookbook with metadata
- [x] `POST /api/cookbooks/{id}/index-pages` - Upload index page images (multipart)
- [x] `GET /api/cookbooks/{id}/ocr` - SSE endpoint for OCR progress
- [x] `POST /api/cookbooks/{id}/ocr/start` - Trigger OCR processing
- [x] `POST /api/cookbooks/{id}/confirm` - Confirm and save import

### Phase 6: Frontend Implementation

#### 6.1 Core Components
- [x] Create `HomePage.vue` with "Import Cookbook" button
- [x] Create `ImportCookbookPage.vue` (main container)
- [x] Create `CookbookForm.vue` (title, author inputs)
- [x] Create `ImageUploader.vue` (drag & drop, multiple files, preview)
- [x] Create `OcrProgress.vue` (progress bar, SSE listener)
- [x] Create `RecipeReviewList.vue` (scrollable list of extracted recipes)
- [x] Create `RecipeReviewItem.vue` (edit/skip controls, highlight low confidence)
- [x] Create `ImportSuccessPage.vue` (success message, navigation options)

#### 6.2 State Management
- [x] Create composable `useImportWizard()` for multi-step flow state
- [x] Create composable `useOcrStream()` for SSE connection

#### 6.3 API Integration
- [x] Generate API client with Orval from OpenAPI spec
- [x] Create custom SSE hook for OCR progress (not generated by Orval)

### Phase 7: End-to-End Integration

- [ ] Test full import flow manually
- [ ] Handle edge cases:
  - [ ] Empty OCR results
  - [ ] All items skipped
  - [ ] Network errors during SSE
  - [ ] Large images
- [ ] Add loading states and error handling in UI

## Success Criteria

- [ ] User can create a cookbook with title and author
- [ ] User can upload multiple index page images
- [ ] Images are stored and associated with the cookbook
- [ ] OCR extracts recipe name, page number, and ingredient from index structure
- [ ] User can review, edit, and skip extracted entries
- [ ] Recipes are saved with correct cookbook and ingredient associations
- [ ] User sees success screen with navigation options
