package com.lezh1n.goodminton_shop_api.entities;

import java.time.LocalDateTime;

import com.lezh1n.goodminton_shop_api.enums.ResourceOwner;
import com.lezh1n.goodminton_shop_api.enums.ResourceType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Polymorphic theo (owner_type, owner_id) — không dùng JPA FK, enforce ở service layer.
@Entity
@Table(name = "resources")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resources {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "public_id", length = 255, nullable = false, unique = true)
    private String publicId;

    @Column(name = "url", length = 255, nullable = false)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ResourceType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false)
    private ResourceOwner ownerType;

    @Column(name = "owner_id", nullable = false)
    private Integer ownerId;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
