# Go Game API - Przykłady

## Game API (Pełna gra)

### Utwórz nową grę
```bash
curl -X POST "http://localhost:8080/api/game/create" \
  -H "Content-Type: application/json" \
  -d '{
    "blackPlayerName": "Alice",
    "whitePlayerName": "Bob",
    "boardSize": 9
  }'
```

**Przykładowa odpowiedź:**
```json
{
  "gameId": "550e8400-e29b-41d4-a716-446655440000",
  "blackPlayer": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "nickname": "Alice",
    "capturedStones": 0
  },
  "whitePlayer": {
    "id": "123e4567-e89b-12d3-a456-426614174001",
    "nickname": "Bob",
    "capturedStones": 0
  },
  "currentTurn": "BLACK",
  "status": "IN_PROGRESS",
  "boardSize": 9,
  "moveCount": 0,
  "consecutivePasses": 0,
  "message": "Game created successfully"
}
```

### Pobierz stan gry
```bash
curl -X GET "http://localhost:8080/api/game/{gameId}"
```

### Wykonaj ruch (czarny gracz)
```bash
curl -X POST "http://localhost:8080/api/game/{gameId}/move" \
  -H "Content-Type: application/json" \
  -d '{
    "row": 2,
    "col": 2
  }'
```

**Przykładowa odpowiedź:**
```json
{
  "gameId": "550e8400-e29b-41d4-a716-446655440000",
  "success": true,
  "currentTurn": "WHITE",
  "status": "IN_PROGRESS",
  "moveCount": 1,
  "capturedStones": 0,
  "gameOver": false,
  "winner": null,
  "message": "Move made successfully"
}
```

### Wykonaj ruch (biały gracz)
```bash
curl -X POST "http://localhost:8080/api/game/{gameId}/move" \
  -H "Content-Type: application/json" \
  -d '{
    "row": 2,
    "col": 3
  }'
```

### Spasuj
```bash
curl -X POST "http://localhost:8080/api/game/{gameId}/pass"
```

### Poddaj się
```bash
curl -X POST "http://localhost:8080/api/game/{gameId}/resign" \
  -H "Content-Type: application/json" \
  -d '{
    "playerId": "123e4567-e89b-12d3-a456-426614174000"
  }'
```

### Pobierz wszystkie ruchy
```bash
curl -X GET "http://localhost:8080/api/game/{gameId}/moves"
```

**Przykładowa odpowiedź:**
```json
{
  "gameId": "550e8400-e29b-41d4-a716-446655440000",
  "moves": [
    {
      "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
      "playerName": "Alice",
      "row": 2,
      "col": 2,
      "moveNumber": 1,
      "isPass": false,
      "capturedStones": 0,
      "timestamp": "2025-12-08"
    },
    {
      "id": "7c9e6679-7425-40de-944b-e07fc1f90ae8",
      "playerName": "Bob",
      "row": 2,
      "col": 3,
      "moveNumber": 2,
      "isPass": false,
      "capturedStones": 0,
      "timestamp": "2025-12-08"
    }
  ],
  "message": "Moves retrieved successfully"
}
```

### Lista wszystkich gier
```bash
curl -X GET "http://localhost:8080/api/game/list"
```

**Przykładowa odpowiedź:**
```json
[
  {
    "gameId": "550e8400-e29b-41d4-a716-446655440000",
    "blackPlayer": "Alice",
    "whitePlayer": "Bob",
    "status": "IN_PROGRESS",
    "moveCount": 5
  }
]
```

### Usuń grę
```bash
curl -X DELETE "http://localhost:8080/api/game/{gameId}"
```

---

## Board API (Prosta plansza)

### Utwórz planszę
```bash
curl -X POST "http://localhost:8080/api/board/create?size=9"
```

### Pobierz stan planszy
```bash
curl -X GET "http://localhost:8080/api/board/{boardId}"
```

### Umieść kamień
```bash
curl -X POST "http://localhost:8080/api/board/{boardId}/place" \
  -H "Content-Type: application/json" \
  -d '{"row": 3, "col": 4, "color": "BLACK"}'
```

### Lista plansz
```bash
curl -X GET "http://localhost:8080/api/board/list"
```

### Usuń planszę
```bash
curl -X DELETE "http://localhost:8080/api/board/{boardId}"
```
