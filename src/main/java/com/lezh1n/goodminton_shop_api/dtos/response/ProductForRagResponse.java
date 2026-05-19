package com.lezh1n.goodminton_shop_api.dtos.response;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductForRagResponse {
    private Integer id;
    private String name;
    private String description;
    private String brand;
    private String category;
    private List<Map<String, String>> specifications;
}
