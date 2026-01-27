package com.gogame.repository;

import com.gogame.repository.entity.GameDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * MongoDB repository for game persistence.
 * <p>
 * Provides CRUD operations for saved games and custom queries
 * for game history retrieval.
 * </p>
 * 
 * @author Go Game Team
 * @version 1.0
 */
@Repository
public interface GameRepository extends MongoRepository<GameDocument, UUID> {
    
    /**
     * Find all finished games ordered by finish time descending.
     */
    List<GameDocument> findByStatusInOrderByFinishedAtDesc(List<String> statuses);
    
    /**
     * Find games where a specific player participated.
     */
    @Query("{ $or: [ { 'blackPlayer.playerId': ?0 }, { 'whitePlayer.playerId': ?0 } ] }")
    List<GameDocument> findByPlayerId(UUID playerId);
    
    /**
     * Find recent games (limit by status).
     */
    List<GameDocument> findTop10ByStatusOrderByUpdatedAtDesc(String status);
}
