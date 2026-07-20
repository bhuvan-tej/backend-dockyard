package com.dockyard.otpauth.domain;

/**
 * OtpPurpose — why an OTP was requested.
 *
 * Kept as an enum (not a free string) so history/analytics stay clean and a
 * caller cannot invent arbitrary purposes. The purpose is part of what makes a
 * challenge unique — a LOGIN OTP cannot be replayed to complete a PASSWORD_RESET.
 */
public enum OtpPurpose {
    LOGIN,
    SIGNUP,
    PASSWORD_RESET,
    TRANSACTION
}