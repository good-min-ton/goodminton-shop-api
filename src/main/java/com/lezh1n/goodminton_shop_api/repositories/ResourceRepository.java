package com.lezh1n.goodminton_shop_api.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lezh1n.goodminton_shop_api.entities.Resources;
import com.lezh1n.goodminton_shop_api.enums.ResourceOwner;

@Repository
public interface ResourceRepository extends JpaRepository<Resources, Integer> {

    List<Resources> findByOwnerTypeAndOwnerIdOrderBySortOrderAsc(ResourceOwner ownerType, Integer ownerId);

    Optional<Resources> findFirstByOwnerTypeAndOwnerIdOrderBySortOrderAsc(ResourceOwner ownerType, Integer ownerId);

    Optional<Resources> findTopByOwnerTypeAndOwnerIdOrderBySortOrderDesc(ResourceOwner ownerType, Integer ownerId);

    long deleteByOwnerTypeAndOwnerId(ResourceOwner ownerType, Integer ownerId);
}
