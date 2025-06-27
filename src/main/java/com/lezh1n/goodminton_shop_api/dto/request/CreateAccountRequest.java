package com.lezh1n.goodminton_shop_api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAccountRequest {
    private String fullName;
    private String email;
    private String phone;
    private String password;
}
