package org.ldclrcq.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "recipe")
public class Recipe extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    public UUID id;

    @Column(name = "cookbook_id", nullable = false)
    public UUID cookbookId;

    @Column(name = "name", nullable = false)
    public String name;

    @Column(name = "page_number", nullable = false)
    public int pageNumber;

    @ManyToMany
    @JoinTable(
            name = "recipe_ingredient",
            joinColumns = @JoinColumn(name = "recipe_id"),
            inverseJoinColumns = @JoinColumn(name = "ingredient_id")
    )
    public Set<Ingredient> ingredients = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    public Recipe() {
    }

    public static Recipe create(UUID cookbookId, String name, int pageNumber) {
        Recipe recipe = new Recipe();
        recipe.cookbookId = cookbookId;
        recipe.name = name;
        recipe.pageNumber = pageNumber;
        recipe.ingredients = new HashSet<>();
        return recipe;
    }

    public void addIngredient(Ingredient ingredient) {
        this.ingredients.add(ingredient);
    }

    public static Optional<Recipe> findByIdOptional(UUID id) {
        return find("id", id).firstResultOptional();
    }

    public static List<Recipe> findByCookbookId(UUID cookbookId) {
        return list("cookbookId", cookbookId);
    }

    public static long countByCookbookId(UUID cookbookId) {
        return count("cookbookId", cookbookId);
    }

    /**
     * Search recipes with optional filters and cursor-based pagination.
     */
    public static SearchResult search(String nameQuery, UUID ingredientId, Boolean availableNow, UUID cookbookId, UUID cursorId, int limit) {
        StringBuilder hql = new StringBuilder("SELECT DISTINCT r FROM Recipe r");
        List<String> conditions = new ArrayList<>();
        Parameters params = new Parameters();

        boolean joinedIngredients = false;
        if (ingredientId != null) {
            hql.append(" JOIN r.ingredients i");
            joinedIngredients = true;
            conditions.add("i.id = :ingredientId");
            params.and("ingredientId", ingredientId);
        }

        if (availableNow != null && availableNow) {
            if (!joinedIngredients) {
                hql.append(" JOIN r.ingredients i");
                joinedIngredients = true;
            }
            conditions.add(":currentMonth MEMBER OF i.availableMonths");
            int currentMonth = java.time.LocalDate.now().getMonthValue();
            params.and("currentMonth", currentMonth);
        }

        if (nameQuery != null && !nameQuery.isBlank()) {
            conditions.add("LOWER(r.name) LIKE :nameQuery");
            params.and("nameQuery", "%" + nameQuery.toLowerCase().trim() + "%");
        }

        if (cookbookId != null) {
            conditions.add("r.cookbookId = :cookbookId");
            params.and("cookbookId", cookbookId);
        }

        if (cursorId != null) {
            conditions.add("r.id > :cursorId");
            params.and("cursorId", cursorId);
        }

        if (!conditions.isEmpty()) {
            hql.append(" WHERE ").append(String.join(" AND ", conditions));
        }

        hql.append(" ORDER BY r.name ASC, r.id ASC");

        List<Recipe> recipes = find(hql.toString(), params).page(0, limit + 1).list();

        boolean hasMore = recipes.size() > limit;
        if (hasMore) {
            recipes = recipes.subList(0, limit);
        }

        String nextCursor = null;
        if (hasMore && !recipes.isEmpty()) {
            nextCursor = recipes.get(recipes.size() - 1).id.toString();
        }

        return new SearchResult(recipes, nextCursor, hasMore);
    }

    public record SearchResult(List<Recipe> recipes, String nextCursor, boolean hasMore) {
    }

    /**
     * Delete all recipes for a cookbook and their ingredient associations.
     */
    public static long deleteByCookbookId(UUID cookbookId) {
        // First delete the recipe_ingredient associations
        getEntityManager().createNativeQuery("""
                DELETE FROM recipe_ingredient
                WHERE recipe_id IN (SELECT id FROM recipe WHERE cookbook_id = :cookbookId)
                """)
                .setParameter("cookbookId", cookbookId)
                .executeUpdate();

        // Then delete the recipes
        return delete("cookbookId", cookbookId);
    }

    /**
     * Find recipes that contain a specific ingredient by ingredient ID.
     */
    public static List<Recipe> findByIngredientId(UUID ingredientId, int limit) {
        return find("SELECT DISTINCT r FROM Recipe r JOIN r.ingredients i WHERE i.id = :ingredientId ORDER BY r.name ASC",
                Parameters.with("ingredientId", ingredientId))
                .page(0, limit)
                .list();
    }

    /**
     * Update all recipe_ingredient references from one ingredient to another.
     * Used during ingredient merge operations.
     */
    public static void updateIngredientReferences(UUID sourceIngredientId, UUID targetIngredientId) {
        // First, delete any duplicates that would occur (recipes that already have the target)
        getEntityManager().createNativeQuery("""
                DELETE FROM recipe_ingredient ri1
                WHERE ri1.ingredient_id = :sourceId
                AND EXISTS (
                    SELECT 1 FROM recipe_ingredient ri2
                    WHERE ri2.recipe_id = ri1.recipe_id
                    AND ri2.ingredient_id = :targetId
                )
                """)
                .setParameter("sourceId", sourceIngredientId)
                .setParameter("targetId", targetIngredientId)
                .executeUpdate();

        // Then update remaining references
        getEntityManager().createNativeQuery("""
                UPDATE recipe_ingredient
                SET ingredient_id = :targetId
                WHERE ingredient_id = :sourceId
                """)
                .setParameter("sourceId", sourceIngredientId)
                .setParameter("targetId", targetIngredientId)
                .executeUpdate();
    }
}
