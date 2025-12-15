package com.gogame.dto;

import java.util.UUID;

public record PlayerResponse(UUID id, String nickname, String token, String createdAt) {}