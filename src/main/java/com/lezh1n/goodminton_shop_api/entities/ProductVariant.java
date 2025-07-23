package com.lezh1n.goodminton_shop_api.entities;

import java.util.ArrayList;
import java.util.List;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_variant")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variant_id")
    private Integer variantId;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "version_id")
    private Version version;

    @ManyToOne
    @JoinColumn(name = "color_id")
    private Color color;

    @OneToMany(mappedBy = "productVariant", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<VariantSize> sizes = new ArrayList<>();

    @OneToMany(mappedBy = "variant", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<VariantImage> images = new ArrayList<>();
}
