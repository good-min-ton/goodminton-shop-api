package com.lezh1n.goodminton_shop_api.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "variant_size")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariantSize {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variant_size_id")
    private Integer variantSizeId;

    @ManyToOne
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant productVariant;

    @ManyToOne
    @JoinColumn(name = "size_id", nullable = false)
    private Size size;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
