package com.gogame.domain.model;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    @Test
    void testPlayerCreation() {
        UUID id = UUID.randomUUID();
        String nickname = "TestPlayer";
        
        Player player = new Player(id, nickname);
        
        assertEquals(id, player.getId());
        assertEquals(nickname, player.getNickname());
        assertEquals(0, player.getCapturedStones());
    }

    @Test
    void testAddCaptured() {
        Player player = new Player(UUID.randomUUID(), "Player1");
        
        assertEquals(0, player.getCapturedStones());
        
        player.addCaptured(3);
        assertEquals(3, player.getCapturedStones());
        
        player.addCaptured(2);
        assertEquals(5, player.getCapturedStones());
    }

    @Test
    void testAddCapturedMultipleTimes() {
        Player player = new Player(UUID.randomUUID(), "Player1");
        
        player.addCaptured(1);
        player.addCaptured(1);
        player.addCaptured(1);
        
        assertEquals(3, player.getCapturedStones());
    }

    @Test
    void testGetters() {
        UUID id = UUID.randomUUID();
        String nickname = "TestPlayer";
        Player player = new Player(id, nickname);
        
        assertNotNull(player.getId());
        assertNotNull(player.getNickname());
        assertEquals(id, player.getId());
        assertEquals(nickname, player.getNickname());
    }
}
