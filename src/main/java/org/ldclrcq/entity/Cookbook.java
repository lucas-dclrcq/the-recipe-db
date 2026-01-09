package org.ldclrcq.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "cookbook")
public class Cookbook extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    public UUID id;

    @Column(nullable = false)
    public String title;

    @Column(nullable = false)
    public String author;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "cover_data")
    public byte[] coverData;

    @Column(name = "cover_content_type")
    public String coverContentType;

    @Column(name = "has_cover", nullable = false)
    public boolean hasCover = false;

    public Cookbook() {
    }

    public static Cookbook create(String title, String author) {
        Cookbook cookbook = new Cookbook();
        cookbook.title = title;
        cookbook.author = author;
        return cookbook;
    }

    public static Optional<Cookbook> findByIdOptional(UUID id) {
        return find("id", id).firstResultOptional();
    }

    public static List<Cookbook> listAll() {
        return findAll().list();
    }

    public long countRecipes() {
        return Recipe.countByCookbookId(this.id);
    }
}
