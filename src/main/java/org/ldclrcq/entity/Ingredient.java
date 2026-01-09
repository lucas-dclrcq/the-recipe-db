package org.ldclrcq.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "ingredient", indexes = {
        @Index(name = "uk_ingredient_name", columnList = "name", unique = true)
})
public class Ingredient extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    public UUID id;

    @Column(name = "name", nullable = false, unique = true)
    public String name; 
    
    @OneToMany(mappedBy = "ingredient", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    public List<IngredientDisambiguation> disambiguations = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "ingredient_available_month", joinColumns = @JoinColumn(name = "ingredient_id"))
    @Column(name = "month", nullable = false)
    public Set<Integer> availableMonths = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    public Ingredient() {
    }

    public static Ingredient create(String rawName) {
        Ingredient ingredient = new Ingredient();
        ingredient.name = normalize(rawName);
        return ingredient;
    }

    public static String normalize(String rawName) {
        if (rawName == null) {
            throw new IllegalArgumentException("Ingredient name cannot be null");
        }
        String normalized = rawName.toLowerCase().trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Ingredient name cannot be empty");
        }
        return normalized;
    }

    public static Optional<Ingredient> findByIdOptional(UUID id) {
        return find("id", id).firstResultOptional();
    }

    public static Optional<Ingredient> findByName(String name) {
        return find("name", normalize(name)).firstResultOptional();
    }

    public static List<Ingredient> findWithFilters(String query, Integer minRecipeCount, Boolean hasDisambiguations,
                                                   Boolean availableNow, UUID cursor, int limit) {
        StringBuilder jpql = new StringBuilder();
        jpql.append("SELECT DISTINCT i FROM Ingredient i ");
        jpql.append("LEFT JOIN i.disambiguations d ");

        StringBuilder where = new StringBuilder();
        boolean hasCondition = false;

        // Search query (searches both name and disambiguations)
        if (query != null && !query.isBlank()) {
            where.append("(i.name LIKE :query OR d.name LIKE :query)");
            hasCondition = true;
        }

        // Filter by availability month (current month)
        if (availableNow != null && availableNow) {
            if (hasCondition) {
                where.append(" AND ");
            }
            where.append(":currentMonth MEMBER OF i.availableMonths");
            hasCondition = true;
        }

        // Cursor-based pagination
        if (cursor != null) {
            if (hasCondition) {
                where.append(" AND ");
            }
            where.append("i.name > (SELECT i2.name FROM Ingredient i2 WHERE i2.id = :cursor)");
            hasCondition = true;
        }

        // Has disambiguations filter
        if (hasDisambiguations != null) {
            if (hasCondition) {
                where.append(" AND ");
            }
            if (hasDisambiguations) {
                where.append("SIZE(i.disambiguations) > 0");
            } else {
                where.append("SIZE(i.disambiguations) = 0");
            }
            hasCondition = true;
        }

        if (hasCondition) {
            jpql.append("WHERE ").append(where);
        }

        jpql.append(" ORDER BY i.name");

        var em = getEntityManager();
        var typedQuery = em.createQuery(jpql.toString(), Ingredient.class);

        if (query != null && !query.isBlank()) {
            typedQuery.setParameter("query", query.toLowerCase().trim() + "%");
        }
        if (availableNow != null && availableNow) {
            int currentMonth = java.time.LocalDate.now().getMonthValue();
            typedQuery.setParameter("currentMonth", currentMonth);
        }
        if (cursor != null) {
            typedQuery.setParameter("cursor", cursor);
        }

        List<Ingredient> results = typedQuery.setMaxResults(limit + 1).getResultList();

        // Post-filter by minRecipeCount if specified (done in memory due to complexity)
        if (minRecipeCount != null && minRecipeCount > 0) {
            results = results.stream()
                    .filter(i -> i.countRecipes() >= minRecipeCount)
                    .limit(limit + 1)
                    .toList();
        }

        return results.size() > limit ? results.subList(0, limit) : results;
    }

    public static boolean hasMore(String query, Integer minRecipeCount, Boolean hasDisambiguations,
                                   Boolean availableNow, UUID cursor, int limit) {
        List<Ingredient> results = findWithFiltersInternal(query, minRecipeCount, hasDisambiguations, availableNow, cursor, limit + 1);
        if (minRecipeCount != null && minRecipeCount > 0) {
            return results.stream().filter(i -> i.countRecipes() >= minRecipeCount).count() > limit;
        }
        return results.size() > limit;
    }

    private static List<Ingredient> findWithFiltersInternal(String query, Integer minRecipeCount, Boolean hasDisambiguations,
                                                            Boolean availableNow, UUID cursor, int limit) {
        StringBuilder jpql = new StringBuilder();
        jpql.append("SELECT DISTINCT i FROM Ingredient i ");
        jpql.append("LEFT JOIN i.disambiguations d ");

        StringBuilder where = new StringBuilder();
        boolean hasCondition = false;

        if (query != null && !query.isBlank()) {
            where.append("(i.name LIKE :query OR d.name LIKE :query)");
            hasCondition = true;
        }

        if (availableNow != null && availableNow) {
            if (hasCondition) {
                where.append(" AND ");
            }
            where.append(":currentMonth MEMBER OF i.availableMonths");
            hasCondition = true;
        }

        if (cursor != null) {
            if (hasCondition) {
                where.append(" AND ");
            }
            where.append("i.name > (SELECT i2.name FROM Ingredient i2 WHERE i2.id = :cursor)");
            hasCondition = true;
        }

        if (hasDisambiguations != null) {
            if (hasCondition) {
                where.append(" AND ");
            }
            if (hasDisambiguations) {
                where.append("SIZE(i.disambiguations) > 0");
            } else {
                where.append("SIZE(i.disambiguations) = 0");
            }
            hasCondition = true;
        }

        if (hasCondition) {
            jpql.append("WHERE ").append(where);
        }

        jpql.append(" ORDER BY i.name");

        var em = getEntityManager();
        var typedQuery = em.createQuery(jpql.toString(), Ingredient.class);

        if (query != null && !query.isBlank()) {
            typedQuery.setParameter("query", query.toLowerCase().trim() + "%");
        }
        if (availableNow != null && availableNow) {
            int currentMonth = java.time.LocalDate.now().getMonthValue();
            typedQuery.setParameter("currentMonth", currentMonth);
        }
        if (cursor != null) {
            typedQuery.setParameter("cursor", cursor);
        }

        return typedQuery.setMaxResults(limit).getResultList();
    }

    public void addDisambiguation(String rawName) {
        IngredientDisambiguation disambiguation = IngredientDisambiguation.create(this, rawName);
        this.disambiguations.add(disambiguation);
    }

    public void removeDisambiguation(String rawName) {
        String normalized = normalize(rawName);
        this.disambiguations.removeIf(d -> d.name.equals(normalized));
    }

    public List<String> getDisambiguationNames() {
        return this.disambiguations.stream().map(d -> d.name).toList();
    }

    public long countRecipes() {
        var em = getEntityManager();
        Long cnt = em.createQuery(
                        "SELECT COUNT(DISTINCT r.id) FROM Recipe r JOIN r.ingredients i WHERE i.id = :iid",
                        Long.class)
                .setParameter("iid", this.id)
                .getSingleResult();
        return cnt != null ? cnt : 0L;
    }

    public static boolean isNameAvailable(String name, UUID excludeId) {
        String normalized = normalize(name);
        // Check if name exists as another ingredient's name
        long ingredientCount = count("name = ?1 AND id != ?2", normalized, excludeId);
        if (ingredientCount > 0) {
            return false;
        }
        // Check if name exists as any disambiguation
        long disambiguationCount = IngredientDisambiguation.count("name = ?1", normalized);
        return disambiguationCount == 0;
    }

    public static boolean isDisambiguationAvailable(String name, UUID ingredientId) {
        String normalized = normalize(name);
        // Check if name exists as any ingredient's name
        long ingredientCount = count("name = ?1", normalized);
        if (ingredientCount > 0) {
            return false;
        }
        // Check if name exists as another ingredient's disambiguation
        long disambiguationCount = IngredientDisambiguation.count(
                "name = ?1 AND ingredient.id != ?2", normalized, ingredientId);
        return disambiguationCount == 0;
    }
}
