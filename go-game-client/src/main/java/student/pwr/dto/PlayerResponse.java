package student.pwr.dto;

import java.util.UUID;

public record PlayerResponse(UUID id, String nickname, String token, String createdAt) {}