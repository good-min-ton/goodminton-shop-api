package com.lezh1n.goodminton_shop_api.configurations;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "app.google")
@Getter
@Setter
public class GoogleProperties {

    /**
     * OAuth client id from Google Cloud Console. Blank turns the feature off:
     * without it there is no audience to check the ID token against, and an
     * unchecked audience means a token minted for any other site would be
     * accepted here.
     */
    private String clientId = "";

    public boolean isEnabled() {
        return clientId != null && !clientId.isBlank();
    }
}
