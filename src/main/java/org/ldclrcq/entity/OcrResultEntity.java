package org.ldclrcq.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ocr_result")
public class OcrResultEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    public UUID id;

    @Column(name = "cookbook_id", nullable = false)
    public UUID cookbookId;

    @Column(nullable = false)
    public String ingredient;

    @Column(name = "recipe_name", nullable = false)
    public String recipeName;

    @Column(name = "page_number", nullable = false)
    public int pageNumber;

    @Column(nullable = false)
    public double confidence;

    @Column(name = "needs_review", nullable = false)
    public boolean needsReview;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    public OcrResultEntity() {
    }

    public static OcrResultEntity create(UUID cookbookId, String ingredient, String recipeName, int pageNumber, double confidence, boolean needsReview) {
        OcrResultEntity entity = new OcrResultEntity();
        entity.cookbookId = cookbookId;
        entity.ingredient = ingredient;
        entity.recipeName = recipeName;
        entity.pageNumber = pageNumber;
        entity.confidence = confidence;
        entity.needsReview = needsReview;
        return entity;
    }

    public static List<OcrResultEntity> findByCookbookId(UUID cookbookId) {
        return list("cookbookId", cookbookId);
    }

    public static long deleteByCookbookId(UUID cookbookId) {
        return delete("cookbookId", cookbookId);
    }
}
