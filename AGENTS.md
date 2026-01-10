# AGENTS.md - AI Agent Instructions

> **Single source of truth for AI agents working on this project.**

## Project Overview

**the-recipe-db** is a recipe database application that allows users to:

- Import cookbook index pages via OCR (using AI/LangChain4j)
- Extract and store recipes with their ingredients
- Search and filter recipes by name, ingredient, or cookbook

### Tech Stack

| Layer              | Technology                              |
|--------------------|-----------------------------------------|
| **Backend**        | Java 21, Quarkus 3.30.5                 |
| **Database**       | PostgreSQL (via JDBC)                   |
| **ORM**            | Hibernate ORM with Panache              |
| **Migrations**     | Flyway                                  |
| **AI/OCR**         | LangChain4j + OpenAI (GPT-4.1-mini)     |
| **API Docs**       | SmallRye OpenAPI + Swagger UI           |
| **Frontend**       | Vue 3, TypeScript, Vite, Tailwind CSS 4 |
| **API Client**     | Orval (auto-generated from OpenAPI)     |
| **Frontend Build** | Quinoa (integrated with Quarkus)        |

---

## Quick Start

### Prerequisites

- Java 21+
- Docker (for PostgreSQL via Dev Services)
- Node.js 20+ (auto-installed by Quinoa)

### Development Mode

```bash
./mvnw quarkus:dev
```

This starts:

- Backend at `http://localhost:8081`
- Frontend dev server at `http://localhost:3001` (proxied through Quarkus)

### Run Tests

```bash
./mvnw test
```

---

## Project Structure

```
src/
├── main/
│   ├── java/org/ldclrcq/
│   │   ├── dto/           # Request/Response DTOs (records)
│   │   ├── entity/        # JPA entities (Panache active record pattern)
│   │   ├── resource/      # JAX-RS REST endpoints
│   │   └── service/       # Business logic & AI services
│   ├── resources/
│   │   ├── application.properties     # Main config
│   │   ├── application-dev.properties # Dev-only overrides
│   │   └── db/migration/              # Flyway SQL migrations
│   └── webui/             # Vue.js frontend (Quinoa)
│       ├── src/
│       │   ├── api/       # Generated API client (Orval)
│       │   ├── components/# Reusable Vue components
│       │   ├── views/     # Page-level Vue components
│       │   ├── router/    # Vue Router configuration
│       │   └── composables/# Vue composables (shared logic)
│       └── orval.config.ts# API client generator config
└── test/
    └── java/org/ldclrcq/  # Integration tests (QuarkusTest)
```

---

## Code Patterns & Conventions

### Backend (Java/Quarkus)

#### Entities (Panache Active Record)

- Extend `PanacheEntityBase` with public fields
- Use `UUID` for primary keys with `@UuidGenerator`
- Include `createdAt`/`updatedAt` timestamps with `@CreationTimestamp`/`@UpdateTimestamp`
- Implement static factory methods (e.g., `Recipe.create(...)`)
- Implement custom queries as static methods on the entity

```java

@Entity
public class Recipe extends PanacheEntityBase {
    @Id
    @GeneratedValue
    @UuidGenerator
    public UUID id;

    public String name;

    public static Optional<Recipe> findByIdOptional(UUID id) {
        return find("id", id).firstResultOptional();
    }
}
```

#### DTOs

- Use Java records for immutable DTOs
- Suffix with `Request` or `Response` based on direction
- Keep them simple - no business logic

```java
public record RecipeResponse(UUID id, String name, int pageNumber, ...) {
}
```

#### Resources (REST Endpoints)

- Path prefix: `/api/`
- Use `@Path`, `@GET`, `@POST`, etc.
- Return `Response` objects for flexibility
- Pagination: cursor-based with `cursor` and `limit` query params

```java

@Path("/api/recipes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RecipeResource { ...
}
```

### Frontend (Vue 3/TypeScript)

#### Components

- Use `<script setup lang="ts">` syntax
- Place in `src/components/` for reusable, `src/views/` for pages
- Use Tailwind CSS for styling (utility-first)
- Import icons from `@heroicons/vue`

#### API Client

- **Auto-generated** by Orval from OpenAPI spec
- Located at `src/api/client.ts`
- Regenerate after backend changes: `npm run generate-api`
- OpenAPI spec auto-generated to `src/api/openapi.json` by Quarkus

#### Routing

- Define routes in `src/router/index.ts`
- Use lazy loading for non-home routes: `() => import('../views/Page.vue')`

---

## Database

### Schema Management

- Flyway migrations in `src/main/resources/db/migration/`
- Naming: `V{version}__{description}.sql` (e.g., `V1.0.0__the-recipe-db.sql`)
- Dev mode: `clean-at-start=true` (resets DB on restart)

### Test Data

- Sample/seed data is in `src/main/resources/import.sql`
- **IMPORTANT**: When modifying entity models (adding/removing columns), update `import.sql` to match the new schema
- The `import.sql` file contains sample cookbooks, recipes, ingredients, and their relationships for development/testing

### Tables

| Table                 | Purpose                                   |
|-----------------------|-------------------------------------------|
| `cookbook`            | Cookbook metadata (title, author)         |
| `cookbook_index_page` | Uploaded index page images (blob storage) |
| `recipe`              | Recipe entries with page number           |
| `ingredient`          | Unique ingredient names                   |
| `recipe_ingredient`   | Many-to-many junction table               |

---

## Development Workflow

1. Write a failing integration test for the API
2. Make the changes to the API to make the test pass
3. Generate the frontend client and modify the frontend as needed

## Testing

### Backend Tests

- Use `@QuarkusTest` for integration tests
- Use REST Assured for API testing
- Tests run against real PostgreSQL (Dev Services)
- Located in `src/test/java/org/ldclrcq/`

```java

@QuarkusTest
class RecipeResourceTest {
    @Test
    void listRecipes_shouldReturnAllRecipes() {
        given()
                .when().get("/api/recipes")
                .then().statusCode(200)
                .body("recipes", not(empty()));
    }
}
```

---

## Common Tasks

### Add a New Entity

1. Create entity class in `src/main/java/org/ldclrcq/entity/`
2. Add Flyway migration in `src/main/resources/db/migration/`
3. Create DTOs in `src/main/java/org/ldclrcq/dto/`
4. Create resource in `src/main/java/org/ldclrcq/resource/`
5. Update `src/main/resources/import.sql` with sample data for the new entity
6. Run `./mvnw quarkus:dev` to apply migration
7. Regenerate frontend client: `cd src/main/webui && npm run generate-api`

### Add a New API Endpoint

1. Add method to appropriate resource class
2. Create request/response DTOs if needed
3. Add tests in corresponding test class
4. Restart dev mode to update OpenAPI spec
5. Regenerate frontend client

### Add a New Frontend Page

1. Create view component in `src/main/webui/src/views/`
2. Add route in `src/router/index.ts`
3. Add navigation link in `SideNav.vue` if needed

### Update OpenAPI Client

```bash
cd src/main/webui
npm run generate-api
```