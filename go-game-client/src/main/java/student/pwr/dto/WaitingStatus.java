package student.pwr.dto;

import java.util.UUID;

public record WaitingStatus(String status, UUID gameId, String message) {}