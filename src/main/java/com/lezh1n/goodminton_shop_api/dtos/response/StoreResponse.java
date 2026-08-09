package com.lezh1n.goodminton_shop_api.dtos.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StoreResponse {
    private Integer id;
    private String name;
    private String address;
    private String contact;
    private BigDecimal longitude;
    private BigDecimal latitude;

    // Explicit: the field is a primitive, so Lombok generates isCentral() and
    // Jackson would strip the prefix and send "central". The Create/Update
    // requests declare a Boolean wrapper, whose getIsCentral() keeps it, so the
    // API accepted "isCentral" and answered with "central" - and a client reading
    // back the field it had just sent got undefined.
    @JsonProperty("isCentral")
    private boolean isCentral;
    private LocalDateTime createdAt;
    private AccountResponse admin;
}
