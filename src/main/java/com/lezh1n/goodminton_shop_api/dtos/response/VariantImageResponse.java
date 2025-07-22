package com.lezh1n.goodminton_shop_api.dtos.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VariantImageResponse {
    private Integer imageId;
    private String publicId;
    private String imageUrl;
    private Integer sortOrder;
    private LocalDateTime createAt;
}
