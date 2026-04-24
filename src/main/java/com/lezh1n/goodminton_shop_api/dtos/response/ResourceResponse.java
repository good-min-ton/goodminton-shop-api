package com.lezh1n.goodminton_shop_api.dtos.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lezh1n.goodminton_shop_api.enums.ResourceType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceResponse {
    private Integer id;
    private String publicId;
    private String url;
    private ResourceType type;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
