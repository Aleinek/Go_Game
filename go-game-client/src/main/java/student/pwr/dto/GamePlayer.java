package student.pwr.dto;

import java.util.UUID;

/**
 * DTO representing a player in a game.
 * 
 * @param id the player's unique identifier
 * @param nickname the player's display name
 * @param capturedStones number of opponent stones captured
 * @author Go Game Team - PWR
 * @version 1.0
 */
public record GamePlayer(UUID id, String nickname, int capturedStones) {}