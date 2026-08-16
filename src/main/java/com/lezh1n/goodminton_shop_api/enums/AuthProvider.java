package com.lezh1n.goodminton_shop_api.enums;

/**
 * How an account proves who it is.
 *
 * LOCAL owns a bcrypt password. GOOGLE is vouched for by a Google ID token and
 * has no password of its own, which is why {@code accounts.password} is
 * nullable and guarded by a CHECK constraint instead.
 */
public enum AuthProvider {
    LOCAL,
    GOOGLE
}
