package com.dockyard.springai.dto;

/**
 * The plain-text answer wrapper returned by most endpoints.
 *
 * @param answer the model's reply
 */
public record AskResponse(String answer) {}