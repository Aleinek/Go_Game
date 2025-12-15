package com.gogame;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gogame.dto.*;

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
            throw new RuntimeException("Blad sieci podczas pobierania gry", e);
        }
    }

    public MoveResponse makeMove(UUID gameId, UUID playerId, int x, int y) throws IOException, InterruptedException {
        MakeMoveRequest requestBody = new MakeMoveRequest(x, y);
        String jsonToSend = mapper.writeValueAsString(requestBody);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverURL + "/api/games/" + gameId + "/move")) 
                .header("Content-Type", "application/json")
                .header("X-Player-Id", playerId.toString()) 
                .POST(HttpRequest.BodyPublishers.ofString(jsonToSend))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return mapper.readValue(response.body(), MoveResponse.class);
        } else {
            throw new RuntimeException("Blad wykonywania ruchu! Kod: " + response.statusCode() + ", Treść: " + response.body());
        }
    }

    public MoveResponse pass(UUID gameId, UUID playerId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverURL + "/api/games/" + gameId + "/pass"))
                .header("Content-Type", "application/json")
                .header("X-Player-Id", playerId.toString())
                .POST(HttpRequest.BodyPublishers.noBody()) // Pusty POST
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return mapper.readValue(response.body(), MoveResponse.class);
        } else {
            throw new RuntimeException("Blad podczas pasowania! Kod: " + response.statusCode() + ", Treść: " + response.body());
        }
    }

    public GameResponse resign(UUID gameId, UUID playerId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverURL + "/api/games/" + gameId + "/resign"))
                    .header("Content-Type", "application/json")
                    .header("X-Player-Id", playerId.toString())
                    .POST(HttpRequest.BodyPublishers.noBody()) 
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return mapper.readValue(response.body(), GameResponse.class);
            } else {
                throw new RuntimeException("Blad podczas poddawania gry! Kod: " + response.statusCode() + ", Treść: " + response.body());
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Blad sieci podczas poddawania gry", e);
        }
    }

    public BoardResponseDTO fetchBoard(UUID gameId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverURL + "/api/games/" + gameId + "/board"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return mapper.readValue(response.body(), BoardResponseDTO.class);
            } else {
                throw new RuntimeException("Błąd pobierania planszy! Kod: " + response.statusCode() + ", Treść: " + response.body());
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Błąd sieci podczas pobierania planszy", e);
        }
    }
}