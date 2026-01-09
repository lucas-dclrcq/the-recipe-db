package org.ldclrcq.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cookbook_index_page")
public class CookbookIndexPage extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    public UUID id;

    @Column(name = "cookbook_id", nullable = false)
    public UUID cookbookId;

    @Column(name = "page_order", nullable = false)
    public int pageOrder;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "image_data", nullable = false)
    public byte[] imageData;

    @Column(name = "content_type", nullable = false)
    public String contentType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    public CookbookIndexPage() {
    }

    public static CookbookIndexPage create(UUID cookbookId, int pageOrder, byte[] imageData, String contentType) {
        validateContentType(contentType);
        CookbookIndexPage page = new CookbookIndexPage();
        page.cookbookId = cookbookId;
        page.pageOrder = pageOrder;
        page.imageData = imageData;
        page.contentType = contentType;
        return page;
    }

    private static void validateContentType(String contentType) {
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new IllegalArgumentException("Content type must be image/jpeg or image/png");
        }
    }

    public static List<CookbookIndexPage> findByCookbookIdOrdered(UUID cookbookId) {
        return list("cookbookId = ?1 ORDER BY pageOrder", cookbookId);
    }

    public static long deleteByCookbookId(UUID cookbookId) {
        return delete("cookbookId", cookbookId);
    }
}
