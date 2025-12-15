package com.gogame.dto;

import java.util.UUID;

public record GamePlayer(UUID id, String nickname, int capturedStones) {}