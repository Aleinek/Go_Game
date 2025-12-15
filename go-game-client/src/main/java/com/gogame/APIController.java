package com.gogame;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

// rekordy do utworzenia gracza
record PlayerRequest(String nickname) {}
record PlayerResponse(UUID id, String nickname, String token, String createdAt) {}

// rekordy pozwalajace dolaczyc do gry
record JoinGameRequest(int boardSize) {}
record GameResponse(
    UUID id, 
    String status, 
    int boardSize, 
    String currentTurn, 
    Integer moveCount,
    GamePlayer blackPlayer, 
    GamePlayer whitePlayer, 
    String message,
    GameMove lastMove
) {}
record GameMove(int x, int y, String color) {}

// rekord z atrybutami gracza
record GamePlayer(UUID id, String nickname, int capturedStones) {}
// rekord ktory przechowuje informacje dla gracza czekajacego na kolejnego gracza w kolejce
record WaitingStatus(String status, UUID gameId, String message) {}

public class APIController {

    private final String serverURL;
    private final HttpClient client;
    private final ObjectMapper mapper;

    APIController(String serverURL) {
        this.serverURL = serverURL;
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public PlayerResponse registerPlayer(String nickname) {
        try {
            PlayerRequest requestBody = new PlayerRequest(nickname);
            String jsonToSend = mapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverURL + "/api/players"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonToSend))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201) {
                return mapper.readValue(response.body(), PlayerResponse.class);
            } else {
                throw new RuntimeException("Blad rejestracji! Kod: " + response.statusCode() + ", Tresc: " + response.body());
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Blad połączenia z serwerem", e);
        }
    }

    public GameResponse joinGame(UUID playerId, int boardSize) {
        try {
            JoinGameRequest requestBody = new JoinGameRequest(boardSize);
            String jsonToSend = mapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverURL + "/api/games/join"))
                    .header("Content-Type", "application/json")
                    .header("X-Player-Id", playerId.toString())
                    .POST(HttpRequest.BodyPublishers.ofString(jsonToSend))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201 || response.statusCode() == 202) {
                return mapper.readValue(response.body(), GameResponse.class);
            } else {
                throw new RuntimeException("Błąd dołączania do gry! Kod: " + response.statusCode() + ", Treść: " + response.body());
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Błąd połączenia z serwerem przy dołączaniu", e);
        }
    }

    public WaitingStatus checkWaitingStatus(UUID waitingId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverURL + "/api/games/waiting/" + waitingId))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return mapper.readValue(response.body(), WaitingStatus.class);
            } else {
                throw new RuntimeException("Bład sprawdzania statusu kolejki. Kod: " + response.statusCode());
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Bład sieci podczas sprawdzania kolejki", e);
        }
    }

    public GameResponse fetchGameStatus(UUID gameId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverURL + "/api/games/" + gameId))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return mapper.readValue(response.body(), GameResponse.class);
            } else {
                throw new RuntimeException("Bladd pobierania stanu gry! Kod: " + response.statusCode());
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Bladd sieci podczas pobierania gry", e);
        }
    }
    
}