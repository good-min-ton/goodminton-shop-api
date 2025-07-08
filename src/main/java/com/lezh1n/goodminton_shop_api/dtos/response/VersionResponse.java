package com.lezh1n.goodminton_shop_api.dtos.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VersionResponse {
    private Integer versionId;
    private String name;
}
