package com.lezh1n.goodminton_shop_api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lezh1n.goodminton_shop_api.entities.Color;

@Repository
public interface ColorRepository extends JpaRepository<Color, Integer> {
    
}
