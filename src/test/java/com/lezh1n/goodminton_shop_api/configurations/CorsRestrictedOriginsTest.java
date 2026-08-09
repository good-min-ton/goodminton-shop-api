package com.lezh1n.goodminton_shop_api.configurations;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * The frontend is on Vercel and this API is reached through a public tunnel, so
 * every browser call is cross-origin. These pin how app.cors-allowed-origins
 * turns into matching, using Spring's own checkOrigin rather than a
 * reimplementation of it.
 *
 * <p>Deliberately a top-level class: surefire scans for *Test.class, so tests
 * tucked into a static nested class are silently skipped by a plain `mvn test`
 * and only run when named explicitly.
 */
@SpringBootTest
@TestPropertySource(properties = "app.cors-allowed-origins=https://shop.vercel.app,https://*.vercel.app")
class CorsRestrictedOriginsTest {

    @Autowired
    // Spring also registers mvcHandlerMappingIntrospector as a CorsConfigurationSource.
    @Qualifier("corsConfigurationSource")
    CorsConfigurationSource source;

    private CorsConfiguration config() {
        return source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/products"));
    }

    @Test
    @DisplayName("the configured origins are parsed and trimmed into patterns")
    void patternsAreParsed() {
        assertThat(config().getAllowedOriginPatterns())
                .containsExactly("https://shop.vercel.app", "https://*.vercel.app");
    }

    @Test
    @DisplayName("the production origin is allowed and an unrelated one is not")
    void exactOriginIsAllowed() {
        assertThat(config().checkOrigin("https://shop.vercel.app"))
                .isEqualTo("https://shop.vercel.app");
        assertThat(config().checkOrigin("https://evil.com")).isNull();
    }

    @Test
    @DisplayName("a wildcard covers Vercel preview subdomains")
    void wildcardCoversPreviews() {
        assertThat(config().checkOrigin("https://goodminton-git-feat-lezh1n.vercel.app"))
                .isNotNull();
    }

    @Test
    @DisplayName("a wildcard does not reach past the domain it is anchored to")
    void wildcardIsAnchored() {
        // The security-relevant case: the pattern must not hand access to a host
        // an attacker controls under a different domain.
        assertThat(config().checkOrigin("https://vercel.app.attacker.com")).isNull();
        assertThat(config().checkOrigin("https://vercel.app")).isNull(); // needs a subdomain
        assertThat(config().checkOrigin("http://shop.vercel.app")).isNull(); // scheme matters
    }

    @Test
    @DisplayName("a wildcard spans any number of labels, unlike the RAG service")
    void wildcardSpansMultipleLabels() {
        // Documented, not desired: Spring expands "*" to ".*", so one wildcard
        // covers a.b.vercel.app too. rag-service compiles the same config value
        // into "[^.]*" and matches a single label only, so the two are not
        // interchangeable - list exact origins when that matters. Nothing rides
        // on it today: *.vercel.app is broad regardless, since anyone can deploy
        // a project there.
        assertThat(config().checkOrigin("https://a.b.vercel.app")).isNotNull();
    }

    @Test
    @DisplayName("credentials stay off, so the bearer token flow is unaffected")
    void credentialsStayOff() {
        // CORS credentials means cookies; the frontend attaches a bearer token
        // itself, which allowedHeaders "*" already permits.
        assertThat(config().getAllowCredentials()).isNotEqualTo(Boolean.TRUE);
        assertThat(config().getAllowedHeaders()).containsExactly("*");
    }
}
