# Go Game Server - Dokumentacja

## ✅ Co zostało zaimplementowane

### Architektura bez bazy danych
Projekt został zrefaktoryzowany zgodnie z dokumentacją, ale **bez warstwy persystencji**:
- ✅ Warstwa serwisowa (Service Layer)
- ✅ DTOs (Request/Response)
- ✅ Kontrolery zgodne z API
- ✅ WebSocket support
- ✅ Global Exception Handler
- ❌ Baza danych PostgreSQL (zastąpiona przez `ConcurrentHashMap`)
- ❌ Matchmaking Service (uproszczony do kolejki w pamięci)

---

## 📁 Struktura Pakietów

```
com.gogame/
├── Main.java                           # Punkt wejścia aplikacji
├── config/
│   └── WebSocketConfig.java           # Konfiguracja WebSocket/STOMP
├── controller/
│   ├── GameController.java            # REST API dla gier
│   ├── PlayerController.java          # REST API dla graczy
│   └── GlobalExceptionHandler.java    # Centralna obsługa wyjątków
├── service/
│   ├── PlayerService.java             # Logika zarządzania graczami
│   ├── GameService.java               # Logika gry i ruchów
│   ├── BoardService.java              # Logika planszy
│   └── GameNotificationService.java   # Powiadomienia WebSocket
├── dto/
│   ├── request/
│   │   ├── CreatePlayerRequest.java
│   │   ├── JoinGameRequest.java
│   │   └── MakeMoveRequest.java
│   └── response/
│       ├── PlayerResponse.java
│       ├── GameResponse.java
│       ├── MoveResponse.java
│       ├── BoardResponse.java
│       └── ErrorResponse.java
├── websocket/
│   ├── GameEvent.java                 # Event WebSocket
│   └── GameEventPayloads.java         # Payloady eventów
└── domain/
    ├── model/                          # Model domenowy (bez zmian)
    ├── enums/
    └── exception/
```

---

## 🔌 API Endpoints

### Gracze

#### `POST /api/players`
Rejestracja nowego gracza.

**Request:**
```json
{
  "nickname": "GoMaster2025"
}
```

**Response (201):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "nickname": "GoMaster2025",
  "token": "token_550e8400-...",
  "createdAt": "2025-12-11T10:30:00Z"
}
```

**Walidacje:**
- Nickname: 3-20 znaków
- Tylko litery, cyfry i podkreślenia
- Unikalny

#### `GET /api/players/{id}`
Pobranie informacji o graczu.

---

### Gry

#### `POST /api/games/join`
Dołączenie do gry (uproszczony matchmaking).

**Headers:**
- `X-Player-Id: {UUID gracza}`

**Request:**
```json
{
  "boardSize": 19
}
```

**Response (202 - Oczekiwanie):**
```json
{
  "status": "WAITING",
  "message": "Waiting for opponent...",
  "boardSize": 19
}
```

**Response (201 - Gra utworzona):**
```json
{
  "id": "660e8400-...",
  "status": "IN_PROGRESS",
  "boardSize": 19,
  "currentTurn": "BLACK",
  "blackPlayer": {
    "id": "...",
    "nickname": "Player1",
    "capturedStones": 0
  },
  "whitePlayer": {
    "id": "...",
    "nickname": "Player2",
    "capturedStones": 0
  },
  "message": "Game created successfully"
}
```

#### `GET /api/games/{id}`
Pobranie stanu gry.

#### `POST /api/games/{id}/moves`
Wykonanie ruchu.

**Headers:**
- `X-Player-Id: {UUID gracza}`

**Request:**
```json
{
  "x": 3,
  "y": 3
}
```

**Response:**
```json
{
  "success": true,
  "move": {
    "moveNumber": 1,
    "x": 3,
    "y": 3,
    "color": "BLACK",
    "capturedStones": 0,
    "timestamp": "..."
  },
  "capturedPositions": [],
  "currentTurn": "WHITE",
  "board": {
    "size": 19,
    "stones": [
      {"x": 3, "y": 3, "color": "BLACK"}
    ]
  }
}
```

#### `POST /api/games/{id}/pass`
Pas.

**Headers:**
- `X-Player-Id: {UUID gracza}`

#### `POST /api/games/{id}/resign`
Poddanie gry.

**Headers:**
- `X-Player-Id: {UUID gracza}`

#### `GET /api/games/{id}/board`
Pobranie stanu planszy.

---

## 🔄 WebSocket Events

### Endpoint
```
ws://localhost:8080/ws
```

### Subskrypcja
```javascript
// Klient subskrybuje:
/user/{playerId}/queue/game
```

### Typy Eventów

#### `GAME_STARTED`
```json
{
  "type": "GAME_STARTED",
  "payload": {
    "gameId": "...",
    "yourColor": "BLACK",
    "opponent": {
      "nickname": "Player2"
    },
    "boardSize": 19
  },
  "timestamp": "..."
}
```

#### `OPPONENT_MOVED`
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
  }
}
```

#### `OPPONENT_PASSED`
```json
{
  "type": "OPPONENT_PASSED",
  "payload": {
    "moveNumber": 5,
    "consecutivePasses": 1,
    "currentTurn": "BLACK"
  }
}
```

#### `GAME_ENDED`
```json
{
  "type": "GAME_ENDED",
  "payload": {
    "reason": "RESIGNATION",
    "winner": "Player1",
    "resignedBy": "Player2"
  }
}
```

---

## 🚀 Uruchomienie

### Wymagania
- Java 17+
- Maven 3.6+

### Kompilacja
```bash
mvn clean compile
```

### Uruchomienie
```bash
mvn spring-boot:run
```

Serwer uruchomi się na **http://localhost:8080**

### Testy
```bash
mvn test
```

---

## 🧪 Testowanie API

### Przykład curl - Rejestracja gracza
```bash
curl -X POST http://localhost:8080/api/players \
  -H "Content-Type: application/json" \
  -d '{"nickname": "Player1"}'
```

### Przykład curl - Dołączenie do gry
```bash
# Gracz 1 dołącza
curl -X POST http://localhost:8080/api/games/join \
  -H "Content-Type: application/json" \
  -H "X-Player-Id: {player1-uuid}" \
  -d '{"boardSize": 19}'

# Gracz 2 dołącza (gra się rozpoczyna)
curl -X POST http://localhost:8080/api/games/join \
  -H "Content-Type: application/json" \
  -H "X-Player-Id: {player2-uuid}" \
  -d '{"boardSize": 19}'
```

### Przykład curl - Wykonanie ruchu
```bash
curl -X POST http://localhost:8080/api/games/{game-id}/moves \
  -H "Content-Type: application/json" \
  -H "X-Player-Id: {player-uuid}" \
  -d '{"x": 3, "y": 3}'
```

---

## 📊 Przepływ Danych

### Scenariusz: Dwóch graczy rozgrywa partię

1. **Gracz 1** rejestruje się: `POST /api/players`
2. **Gracz 2** rejestruje się: `POST /api/players`
3. **Gracz 1** dołącza do kolejki: `POST /api/games/join` → Status `WAITING`
4. **Gracz 2** dołącza do kolejki: `POST /api/games/join` → Gra utworzona ✅
5. **Oba strony** otrzymują WebSocket event `GAME_STARTED`
6. **Gracz 1** (BLACK) wykonuje ruch: `POST /api/games/{id}/moves`
7. **Gracz 2** otrzymuje WebSocket event `OPPONENT_MOVED`
8. **Gracz 2** (WHITE) wykonuje ruch
9. **Gracz 1** otrzymuje WebSocket event `OPPONENT_MOVED`
10. ...proces się powtarza

---

## ⚠️ Ograniczenia (bez bazy danych)

1. **Dane w pamięci** - Po restarcie serwera wszystkie gry są tracone
2. **Brak persystencji** - Historia gier nie jest zachowywana
3. **Pojedyncza instancja** - Nie można skalować poziomo
4. **Uproszczony matchmaking** - Brak zaawansowanej kolejki
5. **Brak autoryzacji** - Token jest prosty (nie JWT)

---

## 🔜 Kolejne Kroki (Iteracja 2)

### Dodanie bazy danych
1. Dodać PostgreSQL do `docker-compose.yml`
2. Dodać zależności: Spring Data JPA, PostgreSQL Driver, Flyway
3. Utworzyć migrację `V1__initial_schema.sql`
4. Przekształcić model domenowy w encje JPA
5. Utworzyć repozytoria
6. Refaktoryzacja serwisów aby używały repozytoriów

### Zaawansowany matchmaking
1. Utworzyć `MatchmakingService`
2. Kolejka w bazie danych
3. ELO rating (opcjonalnie)

### Autoryzacja
1. Spring Security
2. JWT tokens
3. Role użytkowników

---

## 📝 Różnice względem dokumentacji

| Funkcjonalność | Dokumentacja | Implementacja | Status |
|----------------|--------------|---------------|--------|
| REST API | ✅ | ✅ | Zgodne |
| WebSocket | ✅ | ✅ | Zgodne |
| Service Layer | ✅ | ✅ | Zgodne |
| DTOs | ✅ | ✅ | Zgodne |
| PostgreSQL | ✅ | ❌ | Brak (pamięć) |
| JPA/Hibernate | ✅ | ❌ | Brak |
| Flyway | ✅ | ❌ | Brak |
| MatchmakingService | ✅ | ⚠️ | Uproszczony |
| JWT Auth | ✅ | ⚠️ | Prosty token |

---

## 👨‍💻 Autorzy

Projekt powstał zgodnie z dokumentacją techniczną Iteracji 1.
