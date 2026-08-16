package com.lezh1n.goodminton_shop_api.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne
    @JoinColumn(name = "related_product_id")
    @JsonBackReference
    private Product relatedProduct;

    @OneToMany(mappedBy = "relatedProduct")
    @JsonManagedReference
    private List<Product> relatedProducts;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "slug", length = 200, nullable = false, unique = true)
    private String slug;

    @Column(name = "is_visible", nullable = false)
    private Boolean isVisible;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // @OrderBy is not cosmetic here. Without it Hibernate selects the children
    // with no ORDER BY, and PostgreSQL is then free to return them in any order.
    // In practice an UPDATE writes a new tuple at the end of the heap, so
    // editing a variant moves that row to the END of the result set. The
    // storefront defaults to variants[0], so changing one variant's price
    // silently switched the page to a DIFFERENT variant and showed its price -
    // which reads as "the price I just set did not save".
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("id ASC")
    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();

    // Same reasoning: specs are rendered as an ordered table, so they must not
    // shuffle every time one of them is edited.
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("id ASC")
    @Builder.Default
    private List<ProductSpecification> specifications = new ArrayList<>();
}
