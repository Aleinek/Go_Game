# Go Game API - Przykłady Użycia

## Spis Treści
1. [Player API - Zarządzanie graczami](#player-api)
2. [Game API - Rozgrywki](#game-api)
3. [Kody błędów](#error-codes)

---

## Player API

### 1. Rejestracja gracza
```bash
curl -X POST "http://localhost:8080/api/players" \
  -H "Content-Type: application/json" \
  -d '{
    "nickname": "GoMaster2025"
  }'
```

**Odpowiedź - Sukces (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "nickname": "GoMaster2025",
  "token": "token_550e8400-e29b-41d4-a716-446655440000_a1b2c3d4",
  "createdAt": "2025-12-11T10:30:00Z"
}
```

**Odpowiedź - Błąd walidacji (400 Bad Request):**
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Nickname must be between 3 and 20 characters",
  "timestamp": "2025-12-11T10:30:00Z"
}
```

### 2. Pobranie informacji o graczu
```bash
curl -X GET "http://localhost:8080/api/players/550e8400-e29b-41d4-a716-446655440000"
```

**Odpowiedź - Sukces (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "nickname": "GoMaster2025",
  "token": "token_550e8400-e29b-41d4-a716-446655440000_a1b2c3d4",
  "createdAt": "2025-12-11T10:30:00Z"
}
```

**Odpowiedź - Błąd (404 Not Found):**
```json
{
  "error": "PLAYER_NOT_FOUND",
  "message": "Player with ID 550e8400-e29b-41d4-a716-446655440000 not found",
  "timestamp": "2025-12-11T10:30:00Z"
}
```

---

## Game API

### 1. Dołączenie do gry (Matchmaking)
```bash
curl -X POST "http://localhost:8080/api/games/join" \
  -H "Content-Type: application/json" \
  -H "X-Player-Id: 550e8400-e29b-41d4-a716-446655440000" \
  -d '{
    "boardSize": 19
  }'
```

**Odpowiedź - Pierwszy gracz czeka (202 Accepted):**
```json
{
  "id": null,
  "status": "WAITING",
  "boardSize": 19,
  "currentTurn": null,
  "blackPlayer": null,
  "whitePlayer": null,
  "moveCount": 0,
  "lastMove": null,
  "createdAt": null,
  "updatedAt": null,
  "message": "Waiting for opponent..."
}
```

**Odpowiedź - Drugi gracz, gra utworzona (201 Created):**
```json
{
  "id": "660e8400-e29b-41d4-a716-446655440001",
  "status": "IN_PROGRESS",
  "boardSize": 19,
  "currentTurn": "BLACK",
  "blackPlayer": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "nickname": "Player1",
    "capturedStones": 0
  },
  "whitePlayer": {
    "id": "770e8400-e29b-41d4-a716-446655440002",
    "nickname": "Player2",
    "capturedStones": 0
  },
  "moveCount": 0,
  "lastMove": null,
  "createdAt": "2025-12-11T10:35:00Z",
  "updatedAt": "2025-12-11T10:35:00Z",
  "message": "Game created successfully"
}
```

**Odpowiedź - Błąd walidacji (400 Bad Request):**
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Board size must be 9, 13, or 19",
  "timestamp": "2025-12-11T10:35:00Z"
}
```

### 2. Pobranie stanu gry
```bash
curl -X GET "http://localhost:8080/api/games/660e8400-e29b-41d4-a716-446655440001"
```

**Odpowiedź - Sukces (200 OK):**
```json
{
  "id": "660e8400-e29b-41d4-a716-446655440001",
  "status": "IN_PROGRESS",
  "boardSize": 19,
  "currentTurn": "WHITE",
  "blackPlayer": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "nickname": "Player1",
    "capturedStones": 3
  },
  "whitePlayer": {
    "id": "770e8400-e29b-41d4-a716-446655440002",
    "nickname": "Player2",
    "capturedStones": 1
  },
  "moveCount": 42,
  "lastMove": {
    "moveNumber": 42,
    "x": 15,
    "y": 4,
    "color": "BLACK",
    "capturedStones": 0,
    "timestamp": "2025-12-11T11:45:30Z"
  },
  "createdAt": "2025-12-11T10:35:00Z",
  "updatedAt": "2025-12-11T11:45:30Z",
  "message": "Game state retrieved"
}
```

**Odpowiedź - Błąd (404 Not Found):**
```json
{
  "error": "GAME_NOT_FOUND",
  "message": "Game with ID 660e8400-e29b-41d4-a716-446655440001 not found",
  "timestamp": "2025-12-11T11:45:30Z"
}
```

### 3. Wykonanie ruchu
```bash
curl -X POST "http://localhost:8080/api/games/660e8400-e29b-41d4-a716-446655440001/move" \
  -H "Content-Type: application/json" \
  -H "X-Player-Id: 550e8400-e29b-41d4-a716-446655440000" \
  -d '{
    "x": 3,
    "y": 3
  }'
```

**Odpowiedź - Sukces (200 OK):**
```json
{
  "success": true,
  "move": {
    "moveNumber": 43,
    "x": 3,
    "y": 3,
    "color": "BLACK",
    "capturedStones": 2,
    "timestamp": "2025-12-11T11:46:15Z"
  },
  "capturedPositions": [
    {"x": 3, "y": 2},
    {"x": 4, "y": 3}
  ],
  "currentTurn": "WHITE",
  "board": {
    "size": 19,
    "stones": [
      {"x": 3, "y": 3, "color": "BLACK"},
      {"x": 4, "y": 4, "color": "WHITE"},
      {"x": 2, "y": 3, "color": "BLACK"}
    ]
  },
  "message": "Move made successfully"
}
```

**Odpowiedź - Błąd pozycja zajęta (400 Bad Request):**
```json
{
  "error": "POSITION_OCCUPIED",
  "message": "Position is already occupied",
  "timestamp": "2025-12-11T11:46:15Z"
}
```

**Odpowiedź - Błąd poza planszą (400 Bad Request):**
```json
{
  "error": "OUT_OF_BOUNDS",
  "message": "Position is outside the board boundaries",
  "timestamp": "2025-12-11T11:46:15Z"
}
```

**Odpowiedź - Błąd ruch samobójczy (400 Bad Request):**
```json
{
  "error": "SUICIDE_MOVE",
  "message": "Move would result in immediate capture (suicide)",
  "timestamp": "2025-12-11T11:46:15Z"
}
```

**Odpowiedź - Błąd nie twoja tura (400 Bad Request):**
```json
{
  "error": "NOT_YOUR_TURN",
  "message": "It's not your turn",
  "timestamp": "2025-12-11T11:46:15Z"
}
```

### 4. Pobranie listy ruchów
```bash
curl -X GET "http://localhost:8080/api/games/660e8400-e29b-41d4-a716-446655440001/moves"
```

**Odpowiedź - Sukces (200 OK):**
```json
{
  "gameId": "660e8400-e29b-41d4-a716-446655440001",
  "moves": [
    {
      "moveNumber": 1,
      "x": 3,
      "y": 3,
      "color": "BLACK",
      "timestamp": "2025-12-11T10:36:00Z"
    },
    {
      "moveNumber": 2,
      "x": 4,
      "y": 4,
      "color": "WHITE",
      "timestamp": "2025-12-11T10:36:30Z"
    },
    {
      "moveNumber": 3,
      "x": null,
      "y": null,
      "color": "BLACK",
      "timestamp": "2025-12-11T10:37:00Z"
    }
  ],
  "message": "Moves retrieved successfully"
}
```

### 5. Pas
```bash
curl -X POST "http://localhost:8080/api/games/660e8400-e29b-41d4-a716-446655440001/pass" \
  -H "X-Player-Id: 550e8400-e29b-41d4-a716-446655440000"
```

**Odpowiedź - Pierwszy pas (200 OK):**
```json
{
  "success": true,
  "move": {
    "moveNumber": 44,
    "x": -1,
    "y": -1,
    "color": "BLACK",
    "capturedStones": 0,
    "timestamp": "2025-12-11T11:47:00Z"
  },
  "capturedPositions": [],
  "currentTurn": "WHITE",
  "board": {
    "size": 19,
    "stones": [
      {"x": 3, "y": 3, "color": "BLACK"},
      {"x": 4, "y": 4, "color": "WHITE"}
    ]
  },
  "message": "Pass recorded. Opponent's turn."
}
```

**Odpowiedź - Drugi pas z rzędu, koniec gry (200 OK):**
```json
{
  "success": true,
  "move": {
    "moveNumber": 45,
    "x": -1,
    "y": -1,
    "color": "WHITE",
    "capturedStones": 0,
    "timestamp": "2025-12-11T11:47:30Z"
  },
  "capturedPositions": [],
  "currentTurn": null,
  "board": {
    "size": 19,
    "stones": [
      {"x": 3, "y": 3, "color": "BLACK"},
      {"x": 4, "y": 4, "color": "WHITE"}
    ]
  },
  "message": "Game ended. Both players passed."
}
```

### 6. Poddanie gry
```bash
curl -X POST "http://localhost:8080/api/games/660e8400-e29b-41d4-a716-446655440001/resign" \
  -H "X-Player-Id: 550e8400-e29b-41d4-a716-446655440000"
```

**Odpowiedź - Sukces (200 OK):**
```json
{
  "id": "660e8400-e29b-41d4-a716-446655440001",
  "status": "RESIGNED",
  "boardSize": 19,
  "currentTurn": null,
  "blackPlayer": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "nickname": "Player1",
    "capturedStones": 3
  },
  "whitePlayer": {
    "id": "770e8400-e29b-41d4-a716-446655440002",
    "nickname": "Player2",
    "capturedStones": 1
  },
  "moveCount": 42,
  "lastMove": {
    "moveNumber": 42,
    "x": 15,
    "y": 4,
    "color": "BLACK",
    "capturedStones": 0,
    "timestamp": "2025-12-11T11:45:30Z"
  },
  "createdAt": "2025-12-11T10:35:00Z",
  "updatedAt": "2025-12-11T11:48:00Z",
  "message": "Player1 resigned. Player2 wins!"
}
```

### 7. Pobranie stanu planszy
```bash
curl -X GET "http://localhost:8080/api/games/660e8400-e29b-41d4-a716-446655440001/board"
```

**Odpowiedź - Sukces (200 OK):**
```json
{
  "gameId": "660e8400-e29b-41d4-a716-446655440001",
  "size": 19,
  "moveNumber": 43,
  "stones": [
    {"x": 3, "y": 3, "color": "BLACK"},
    {"x": 4, "y": 4, "color": "WHITE"},
    {"x": 16, "y": 3, "color": "BLACK"},
    {"x": 16, "y": 16, "color": "WHITE"},
    {"x": 3, "y": 16, "color": "BLACK"}
  ],
  "blackCaptured": 3,
  "whiteCaptured": 1
}
```

---

## Kody błędów

| Kod | HTTP Status | Opis |
|-----|-------------|------|
| `VALIDATION_ERROR` | 400 | Błąd walidacji danych wejściowych |
| `POSITION_OCCUPIED` | 400 | Pozycja jest już zajęta |
| `OUT_OF_BOUNDS` | 400 | Pozycja poza planszą |
| `SUICIDE_MOVE` | 400 | Ruch samobójczy |
| `NOT_YOUR_TURN` | 400 | Nie twoja tura |
| `GAME_NOT_IN_PROGRESS` | 400 | Gra nie jest w trakcie |
| `PLAYER_NOT_FOUND` | 404 | Gracz nie istnieje |
| `GAME_NOT_FOUND` | 404 | Gra nie istnieje |
| `INTERNAL_SERVER_ERROR` | 500 | Błąd serwera |

---

## Pełny przepływ gry

### 1. Rejestracja dwóch graczy
```bash
# Gracz 1
curl -X POST "http://localhost:8080/api/players" \
  -H "Content-Type: application/json" \
  -d '{"nickname": "Alice"}'
# Zapisz ID z odpowiedzi jako player1Id

# Gracz 2
curl -X POST "http://localhost:8080/api/players" \
  -H "Content-Type: application/json" \
  -d '{"nickname": "Bob"}'
# Zapisz ID z odpowiedzi jako player2Id
```

### 2. Dołączenie do gry
```bash
# Gracz 1 dołącza pierwszy
curl -X POST "http://localhost:8080/api/games/join" \
  -H "Content-Type: application/json" \
  -H "X-Player-Id: {player1Id}" \
  -d '{"boardSize": 9}'
# Otrzyma status: WAITING

# Gracz 2 dołącza - gra się rozpoczyna
curl -X POST "http://localhost:8080/api/games/join" \
  -H "Content-Type: application/json" \
  -H "X-Player-Id: {player2Id}" \
  -d '{"boardSize": 9}'
# Zapisz gameId z odpowiedzi
```

### 3. Rozgrywka
```bash
# Gracz 1 (BLACK) wykonuje ruch
curl -X POST "http://localhost:8080/api/games/{gameId}/move" \
  -H "Content-Type: application/json" \
  -H "X-Player-Id: {player1Id}" \
  -d '{"x": 2, "y": 2}'

# Gracz 2 (WHITE) wykonuje ruch
curl -X POST "http://localhost:8080/api/games/{gameId}/move" \
  -H "Content-Type: application/json" \
  -H "X-Player-Id: {player2Id}" \
  -d '{"x": 2, "y": 3}'

# Sprawdzenie stanu gry
curl -X GET "http://localhost:8080/api/games/{gameId}"

# Sprawdzenie planszy
curl -X GET "http://localhost:8080/api/games/{gameId}/board"

# Gracz 1 pasuje
curl -X POST "http://localhost:8080/api/games/{gameId}/pass" \
  -H "X-Player-Id: {player1Id}"

# Gracz 2 pasuje (koniec gry)
curl -X POST "http://localhost:8080/api/games/{gameId}/pass" \
  -H "X-Player-Id: {player2Id}"
```

### 4. Sprawdzenie wyniku
```bash
curl -X GET "http://localhost:8080/api/games/{gameId}"
```
