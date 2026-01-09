package org.ldclrcq.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "ingredient_disambiguation", indexes = {
        @Index(name = "idx_disambiguation_ingredient", columnList = "ingredient_id"),
        @Index(name = "idx_disambiguation_name", columnList = "name")
})
public class IngredientDisambiguation extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    public Ingredient ingredient;

    @Column(name = "name", nullable = false, unique = true)
    public String name;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    public IngredientDisambiguation() {
    }

    public static IngredientDisambiguation create(Ingredient ingredient, String rawName) {
        IngredientDisambiguation disambiguation = new IngredientDisambiguation();
        disambiguation.ingredient = ingredient;
        disambiguation.name = Ingredient.normalize(rawName);
        return disambiguation;
    }

    public static Optional<IngredientDisambiguation> findByName(String name) {
        return find("name", Ingredient.normalize(name)).firstResultOptional();
    }

    public static List<IngredientDisambiguation> findByIngredient(Ingredient ingredient) {
        return find("ingredient", ingredient).list();
    }

    public static List<IngredientDisambiguation> findByPrefix(String prefix, int limit) {
        if (prefix == null || prefix.isBlank()) {
            return find("ORDER BY name").page(0, limit).list();
        }
        String normalizedPrefix = prefix.toLowerCase().trim();
        return find("name LIKE ?1 ORDER BY name", normalizedPrefix + "%")
                .page(0, limit)
                .list();
    }

    public static boolean existsByName(String name) {
        return count("name", Ingredient.normalize(name)) > 0;
    }
}
