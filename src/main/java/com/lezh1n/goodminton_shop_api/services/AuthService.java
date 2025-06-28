package com.lezh1n.goodminton_shop_api.services;

import com.lezh1n.goodminton_shop_api.dto.request.CreateAccountRequest;
import com.lezh1n.goodminton_shop_api.dto.response.AccountResponse;

public interface AuthService {
    AccountResponse register(CreateAccountRequest request);

    
}
