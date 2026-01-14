package com.gogame.dto.websocket;

/**
 * Payload dla zdarzenia NEGOTIATION_ERROR.
 * Wysyłany gdy akcja negocjacji jest nieprawidłowa.
 */
public record NegotiationErrorPayload(
    String errorCode,
    String message,
    Integer chainId         // opcjonalnie - ID łańcucha którego dotyczy błąd
) {}
