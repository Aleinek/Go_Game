package com.gogame.dto.response;

import java.time.Instant;
import java.util.UUID;

public record PlayerResponse(
    UUID id,
    String nickname,
    String token,
    Instant createdAt
) {
}
