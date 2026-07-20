package com.dockyard.otpauth.security;

/**
 * TokenType — distinguishes an access token from a refresh token.
 *
 * The value is written into the JWT as a {@code type} claim, and checked on the
 * way back in. This stops a refresh token being used as an access token (or vice
 * versa) — a common, dangerous mix-up if the two are not separated.
 */
public enum TokenType {
    ACCESS,
    REFRESH
}