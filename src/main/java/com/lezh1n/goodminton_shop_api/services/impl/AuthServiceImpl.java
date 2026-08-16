package com.lezh1n.goodminton_shop_api.services.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lezh1n.goodminton_shop_api.dtos.request.CreateAccountRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.GoogleLoginRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.LoginRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.LogoutRequest;
import com.lezh1n.goodminton_shop_api.dtos.request.RefreshTokenRequest;
import com.lezh1n.goodminton_shop_api.dtos.response.AccountResponse;
import com.lezh1n.goodminton_shop_api.dtos.response.AuthenticationResponse;
import com.lezh1n.goodminton_shop_api.entities.Account;
import com.lezh1n.goodminton_shop_api.enums.AccountStatus;
import com.lezh1n.goodminton_shop_api.enums.AuthProvider;
import com.lezh1n.goodminton_shop_api.enums.UserRole;
import com.lezh1n.goodminton_shop_api.exceptions.AppException;
import com.lezh1n.goodminton_shop_api.exceptions.ErrorCode;
import com.lezh1n.goodminton_shop_api.mappers.AccountMapper;
import com.lezh1n.goodminton_shop_api.repositories.AccountRepository;
import com.lezh1n.goodminton_shop_api.services.AuthService;
import com.lezh1n.goodminton_shop_api.services.TokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final GoogleIdentityVerifier googleIdentityVerifier;

    @Override
    public AccountResponse register(CreateAccountRequest request, UserRole role) {

        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.AUTH_EMAIL_EXISTED);
        }

        if (accountRepository.existsByPhone(request.getPhone())) {
            throw new AppException(ErrorCode.AUTH_PHONE_EXISTED);
        }

        Account account = accountMapper.toAccount(request);
        account.setRole(role);
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        return accountMapper.toAccountResponse(accountRepository.save(account));
    }

    @Override
    public AuthenticationResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getIdentifier(),
                            request.getPassword()));

            Account account = (Account) authentication.getPrincipal();
            if (account.getStatus() == AccountStatus.INACTIVE) {
                throw new AppException(ErrorCode.AUTH_ACCOUNT_INACTIVE);
            }

            String accessToken = jwtService.generateAccessToken(account);
            String refreshToken = jwtService.generateRefreshToken(account);

            return AuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        } catch (BadCredentialsException e) {
            throw new AppException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }
    }

    /**
     * Sign in with a Google ID token, creating the account on first use.
     *
     * Matching is by Google's {@code sub} first and email only as a fallback. A
     * person can change the email on their Google account; {@code sub} never
     * changes, so treating email as the key would hand the old address to
     * whoever registers it next.
     *
     * Falling back to email is what links a Google sign-in to an account that
     * already registered with a password, and it is gated on {@code
     * email_verified}. Without that gate anyone able to mint a token for an
     * unverified address of their choosing could claim an existing account.
     */
    @Override
    @Transactional
    public AuthenticationResponse loginWithGoogle(GoogleLoginRequest request) {
        Jwt token = googleIdentityVerifier.verify(request.getCredential());

        String subject = token.getSubject();
        String email = token.getClaimAsString("email");
        boolean emailVerified = Boolean.TRUE.equals(token.getClaim("email_verified"));

        if (subject == null || subject.isBlank() || email == null || email.isBlank()) {
            throw new AppException(ErrorCode.AUTH_GOOGLE_TOKEN_INVALID);
        }
        if (!emailVerified) {
            throw new AppException(ErrorCode.AUTH_GOOGLE_EMAIL_UNVERIFIED);
        }

        Account account = accountRepository
                .findByProviderAndProviderId(AuthProvider.GOOGLE, subject)
                .or(() -> accountRepository.findByEmail(email))
                .map(existing -> linkIfNeeded(existing, subject, token))
                .orElseGet(() -> createFromGoogle(subject, email, token));

        if (account.getStatus() == AccountStatus.INACTIVE) {
            throw new AppException(ErrorCode.AUTH_ACCOUNT_INACTIVE);
        }

        return AuthenticationResponse.builder()
                .accessToken(jwtService.generateAccessToken(account))
                .refreshToken(jwtService.generateRefreshToken(account))
                .build();
    }

    /**
     * Attach the Google identity to an account that was found by email.
     *
     * Staff are refused: an admin's mailbox being a Google account should not be
     * enough to reach the admin panel, which has its own password login. The
     * check is on the account being signed into, not on what the token claims.
     */
    private Account linkIfNeeded(Account account, String subject, Jwt token) {
        if (account.getRole() != UserRole.CUSTOMER) {
            throw new AppException(ErrorCode.AUTH_GOOGLE_NOT_FOR_STAFF);
        }
        if (account.getProviderId() == null) {
            account.setProvider(AuthProvider.GOOGLE);
            account.setProviderId(subject);
        }
        // Refreshed on every sign-in so a changed Google picture follows along.
        // Name is left alone: the customer may have edited it here on purpose.
        String picture = token.getClaimAsString("picture");
        if (picture != null && !picture.isBlank()) {
            account.setAvatarUrl(picture);
        }
        return accountRepository.save(account);
    }

    private Account createFromGoogle(String subject, String email, Jwt token) {
        String name = token.getClaimAsString("name");
        Account account = Account.builder()
                .fullName(name == null || name.isBlank() ? email : trim(name, 100))
                .email(trim(email, 255))
                // phone stays null: Google does not supply one, and the column is
                // nullable for exactly this. Orders carry their own recipient
                // phone, so checkout is unaffected.
                .phone(null)
                .password(null)
                .provider(AuthProvider.GOOGLE)
                .providerId(subject)
                .avatarUrl(trim(token.getClaimAsString("picture"), 500))
                .role(UserRole.CUSTOMER)
                .status(AccountStatus.ACTIVE)
                .build();
        return accountRepository.save(account);
    }

    /** Google's fields have no length limit; the columns do. */
    private String trim(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }

    @Override
    public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new AppException(ErrorCode.JWT_INVALID_TOKEN);
        }

        if (jwtService.isTokenExpired(refreshToken)) {
            throw new AppException(ErrorCode.JWT_EXPIRED_TOKEN);
        }

        if (!jwtService.validateToken(refreshToken)) {
            throw new AppException(ErrorCode.JWT_INVALID_TOKEN);
        }

        if (tokenService.isBlacklisted(refreshToken)) {
            throw new AppException(ErrorCode.JWT_TOKEN_BLACKLISTED);
        }

        String email = jwtService.extractEmail(refreshToken);

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        String newAccessToken = jwtService.generateAccessToken(account);

        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public void logout(LogoutRequest request) {
        String refreshToken = request.getRefreshToken();

        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new AppException(ErrorCode.JWT_INVALID_TOKEN);
        }

        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new AppException(ErrorCode.JWT_INVALID_TOKEN);
        }

        if (!jwtService.validateToken(refreshToken)) {
            throw new AppException(ErrorCode.JWT_INVALID_TOKEN);
        }

        if (tokenService.isBlacklisted(refreshToken)) {
            log.info("Token already blacklisted");
            return;
        }

        jwtService.blacklistToken(refreshToken);
    }

}
