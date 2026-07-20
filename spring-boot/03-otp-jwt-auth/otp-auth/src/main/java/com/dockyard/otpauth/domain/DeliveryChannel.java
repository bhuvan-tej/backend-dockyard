package com.dockyard.otpauth.domain;

/**
 * DeliveryChannel — how the OTP would be delivered in a real system.
 *
 * This demo does not actually send SMS/email (that needs a paid gateway and
 * would break the "runs anywhere with zero setup" promise). Instead the code is
 * logged and — in dev mode only — returned in the response. The channel is still
 * modelled because it changes validation (e.g. an EMAIL identifier vs a phone).
 */
public enum DeliveryChannel {
    EMAIL,
    SMS
}