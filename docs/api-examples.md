# Go Game API - Przykłady Użycia

## Spis Treści
1. [Player API - Zarządzanie graczami](#player-api)
2. [Game API - Rozgrywki](#game-api)
3. [WebSocket - Real-time komunikacja](#websocket)
4. [Kody błędów](#error-codes)

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

**Sukces (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "nickname": "GoMaster2025",
  "token": "token_550e8400-e29b-41d4-a716-446655440000_a1b2c3d4",
  "createdAt": "2025-12-11T10:30:00Z"
}
```

**Błąd - nickname zajęty (400):**
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Nickname 'GoMaster2025' is already taken",
  "timestamp": "2025-12-11T10:30:00Z"
}
```

**Błąd - za krótki nickname (400):**
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

**Odpowiedź (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "nickname": "GoMaster2025",
  "token": "token_550e8400-e29b-41d4-a716-446655440000_a1b2c3d4",
  "createdAt": "2025-12-11T10:30:00Z"
}
```

**Błąd - gracz nie istnieje (404):**
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

**Pierwszy gracz - oczekiwanie (202 Accepted):**
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

**Drugi gracz - gra utworzona (201 Created):**
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

**Błąd - nieprawidłowy rozmiar planszy (400):**
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

**Odpowiedź (200 OK):**
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

**Sukces (200 OK):**
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

**Błąd - pozycja zajęta (400):**
```json
{
  "error": "POSITION_OCCUPIED",
  "message": "Position is already occupied at position (3, 3)",
  "timestamp": "2025-12-11T11:46:15Z"
}
```

**Błąd - poza planszą (400):**
```json
{
  "error": "OUT_OF_BOUNDS",
  "message": "Position is outside the board boundaries at position (25, 3)",
  "timestamp": "2025-12-11T11:46:15Z"
}
```

**Błąd - ruch samobójczy (400):**
```json
{
  "error": "SUICIDE_MOVE",
  "message": "Move would result in immediate capture (suicide)",
  "timestamp": "2025-12-11T11:46:15Z"
}
```

**Błąd - nie twoja tura (400):**
```json
{
  "error": "NOT_YOUR_TURN",
  "message": "It's not your turn",
  "timestamp": "2025-12-11T11:46:15Z"
}
```

### 4. Pas
```bash
curl -X POST "http://localhost:8080/api/games/660e8400-e29b-41d4-a716-446655440001/pass" \
  -H "X-Player-Id: 550e8400-e29b-41d4-a716-446655440000"
```

**Odpowiedź - pierwszy pas (200 OK):**
```json
{
  "success": true,
  "move": {
    "moveNumber": 44,
    "x": -1,
    "y": -1,
    "color": "PASS",
    "capturedStones": 0,
    "timestamp": "2025-12-11T11:47:00Z"
  },
  "capturedPositions": [],
  "currentTurn": "WHITE",
  "board": {
    "size": 19,
    "stones": [...]
  },
  "message": "Pass recorded. Opponent's turn."
}
```

**Odpowiedź - drugi pas z rzędu, koniec gry (200 OK):**
```json
{
  "success": true,
  "move": {
    "moveNumber": 45,
    "x": -1,
    "y": -1,
    "color": "PASS",
    "capturedStones": 0,
    "timestamp": "2025-12-11T11:47:30Z"
  },
  "capturedPositions": [],
  "currentTurn": "BLACK",
  "board": {
    "size": 19,
    "stones": [...]
  },
  "message": "Game ended. Both players passed."
}
```

### 5. Poddanie gry
```bash
curl -X POST "http://localhost:8080/api/games/660e8400-e29b-41d4-a716-446655440001/resign" \
  -H "X-Player-Id: 550e8400-e29b-41d4-a716-446655440000"
```

**Odpowiedź (200 OK):**
```json
{
  "id": "660e8400-e29b-41d4-a716-446655440001",
  "status": "RESIGNED",
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
  "lastMove": null,
  "createdAt": "2025-12-11T10:35:00Z",
  "updatedAt": "2025-12-11T11:48:00Z",
  "message": "Player1 resigned. Player2 wins!"
}
```

### 6. Pobranie stanu planszy
```bash
curl -X GET "http://localhost:8080/api/games/660e8400-e29b-41d4-a716-446655440001/board"
```

**Odpowiedź (200 OK):**
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

## WebSocket

### Połączenie
```javascript
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);
    
    // Subskrypcja do eventów gry dla konkretnego gracza
    stompClient.subscribe('/user/' + playerId + '/queue/game', function(message) {
        const event = JSON.parse(message.body);
        handleGameEvent(event);
    });
});
```

### Event: GAME_STARTED
Wysyłany do obu graczy po utworzeniu gry.

```json
{
  "type": "GAME_STARTED",
  "payload": {
    "gameId": "660e8400-e29b-41d4-a716-446655440001",
    "yourColor": "BLACK",
    "opponent": {
      "nickname": "Player2"
    },
    "boardSize": 19
  },
  "timestamp": "2025-12-11T10:35:00Z"
}
```

### Event: OPPONENT_MOVED
Wysyłany po ruchu przeciwnika.

```json
{
  "type": "OPPONENT_MOVED",
  "payload": {
    "move": {
      "moveNumber": 2,
      "x": 4,
      "y": 4,
      "color": "WHITE"
    },
    "capturedPositions": [],
    "currentTurn": "BLACK"
  },
  "timestamp": "2025-12-11T10:36:00Z"
}
```

### Event: OPPONENT_PASSED
Wysyłany po pasie przeciwnika.

```json
{
  "type": "OPPONENT_PASSED",
  "payload": {
    "moveNumber": 5,
    "consecutivePasses": 1,
    "currentTurn": "BLACK"
  },
  "timestamp": "2025-12-11T10:37:00Z"
}
```

### Event: GAME_ENDED
Wysyłany po zakończeniu gry.

```json
{
  "type": "GAME_ENDED",
  "payload": {
    "reason": "RESIGNATION",
    "winner": "Player2",
    "resignedBy": "Player1"
  },
  "timestamp": "2025-12-11T11:48:00Z"
}
```

**Powody zakończenia:**
- `RESIGNATION` - poddanie się
- `TWO_PASSES` - dwa pasy z rzędu

---

## Error Codes

### Kody błędów API

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
# Zapisz ID i token z odpowiedzi: player1Id, player1Token

# Gracz 2
curl -X POST "http://localhost:8080/api/players" \
  -H "Content-Type: application/json" \
  -d '{"nickname": "Bob"}'
# Zapisz ID i token z odpowiedzi: player2Id, player2Token
```

### 2. Dołączenie do gry
```bash
# Gracz 1 dołącza pierwszy
curl -X POST "http://localhost:8080/api/games/join" \
  -H "Content-Type: application/json" \
  -H "X-Player-Id: {player1Id}" \
  -d '{"boardSize": 9}'
# Odpowiedź: WAITING

# Gracz 2 dołącza - gra się rozpoczyna
curl -X POST "http://localhost:8080/api/games/join" \
  -H "Content-Type: application/json" \
  -H "X-Player-Id: {player2Id}" \
  -d '{"boardSize": 9}'
# Odpowiedź: Gra utworzona, zapisz gameId
```

### 3. Rozgrywka
```bash
# Gracz 1 (BLACK) wykonuje ruch
curl -X POST "http://localhost:8080/api/games/{gameId}/moves" \
  -H "Content-Type: application/json" \
  -H "X-Player-Id: {player1Id}" \
  -d '{"x": 2, "y": 2}'

# Gracz 2 (WHITE) wykonuje ruch
curl -X POST "http://localhost:8080/api/games/{gameId}/moves" \
  -H "Content-Type: application/json" \
  -H "X-Player-Id: {player2Id}" \
  -d '{"x": 2, "y": 3}'

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

---

## Testowanie w Postman

### Kolekcja zmiennych środowiskowych
```json
{
  "baseUrl": "http://localhost:8080",
  "player1Id": "",
  "player1Token": "",
  "player2Id": "",
  "player2Token": "",
  "gameId": ""
}
```

### Pre-request Scripts
Dla żądań wymagających X-Player-Id:
```javascript
pm.request.headers.add({
    key: 'X-Player-Id',
    value: pm.environment.get('player1Id')
});
```

### Tests (dla zapisywania ID)
Po utworzeniu gracza:
```javascript
const response = pm.response.json();
pm.environment.set('player1Id', response.id);
pm.environment.set('player1Token', response.token);
```

Po utworzeniu gry:
```javascript
const response = pm.response.json();
if (response.id) {
    pm.environment.set('gameId', response.id);
}
```
