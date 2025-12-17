# Dokumentacja Techniczna - System Gry w Go

## Spis Treści
1. [Wprowadzenie](#1-wprowadzenie)
2. [Architektura Systemu](#2-architektura-systemu)
3. [Projekt API (Kontrakt JSON)](#3-projekt-api-kontrakt-json)
4. [Model Danych](#4-model-danych)

---

## 1. Wprowadzenie

### 1.1 Cel Dokumentu
Niniejsza dokumentacja opisuje architekturę i specyfikację techniczną systemu do przeprowadzania rozgrywek w grę w Go. System składa się z serwera Spring Boot oraz klienta konsolowego w Javie.

### 1.2 Zakres Funkcjonalności
System obejmuje:
- Architektura klient-serwer
- Rejestracja graczy i dołączanie do gry (matchmaking)
- Wysyłanie ruchów między graczami (interfejs konsolowy)
- Implementacja zasad gry:
  - Gra dla dwóch graczy na planszy 9x9, 13x13 lub 19x19
  - Naprzemienne kładzenie kamieni (czarne zaczynają)
  - Mechanizm zbijania kamieni (oddechy/liberties)
  - Możliwość pasu i poddania gry
- Komunikacja real-time poprzez WebSocket

### 1.3 Wymagania Niefunkcjonalne
- System zaprojektowany z myślą o rozszerzalności
- Kod czysty, testowalny i dobrze udokumentowany
- Wykorzystanie wzorców projektowych i dobrych praktyk

---

## 2. Architektura Systemu

### 2.1 Stos Technologiczny

| Komponent | Technologia | Wersja |
|-----------|-------------|--------|
| **Język Backend** | Java | 17 |
| **Framework** | Spring Boot | 3.2.0 |
| **Język Klient** | Java | 17 |
| **Komunikacja Real-time** | WebSocket (STOMP) | - |
| **Build Tool** | Maven | - |
| **Serializacja JSON** | Jackson | 2.17.0 (klient) |
| **Testy** | JUnit 5 | 5.10.2 |

### 2.2 Wzorce Projektowe

#### 2.2.1 Architektura Warstwowa (Layered Architecture)
```
┌─────────────────────────────────────────────┐
│           Presentation Layer                │
│     (REST Controllers, WebSocket Handlers)  │
├─────────────────────────────────────────────┤
│            Service Layer                    │
│   (GameService, PlayerService, BoardService)│
├─────────────────────────────────────────────┤
│            Domain Layer                     │
│     (Game, Board, Stone, Chain, Player)     │
└─────────────────────────────────────────────┘
```

#### 2.2.2 Zastosowane Wzorce

| Wzorzec | Zastosowanie | Uzasadnienie |
|---------|--------------|--------------|
| **Service Layer** | Logika biznesowa | Separacja logiki od kontrolerów |
| **DTO Pattern** | Transfer danych | Separacja modelu domenowego od API |
| **Strategy Pattern** | Walidacja ruchów | Różne strategie walidacji |
| **Observer Pattern** | WebSocket notifications | Powiadamianie graczy o zmianach |
| **In-Memory Storage** | Przechowywanie danych | ConcurrentHashMap dla gier i graczy |

### 2.3 Struktura Projektu

```
Go_Game/
├── go-game-server/                  # Backend (Spring Boot)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/gogame/
│   │   │   │   ├── Main.java
│   │   │   │   ├── config/
│   │   │   │   │   └── WebSocketConfig.java
│   │   │   │   ├── controller/
│   │   │   │   │   ├── GameController.java
│   │   │   │   │   └── PlayerController.java
│   │   │   │   ├── service/
│   │   │   │   │   ├── GameService.java
│   │   │   │   │   ├── PlayerService.java
│   │   │   │   │   ├── BoardService.java
│   │   │   │   │   └── GameNotificationService.java
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── Game.java
│   │   │   │   │   │   ├── Board.java
│   │   │   │   │   │   ├── Stone.java
│   │   │   │   │   │   ├── Chain.java
│   │   │   │   │   │   ├── Position.java
│   │   │   │   │   │   ├── Player.java
│   │   │   │   │   │   ├── Move.java
│   │   │   │   │   │   ├── MoveResult.java
│   │   │   │   │   │   ├── Territory.java
│   │   │   │   │   │   ├── BoardPrinter.java
│   │   │   │   │   │   ├── BoardCharacters.java
│   │   │   │   │   │   └── CLIController.java
│   │   │   │   │   ├── enums/
│   │   │   │   │   │   ├── StoneColor.java
│   │   │   │   │   │   ├── GameStatus.java
│   │   │   │   │   │   └── MoveType.java
│   │   │   │   │   └── exception/
│   │   │   │   │       ├── InvalidMoveException.java
│   │   │   │   │       ├── GameNotFoundException.java
│   │   │   │   │       └── PlayerNotFoundException.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── request/
│   │   │   │   │   │   ├── CreatePlayerRequest.java
│   │   │   │   │   │   ├── JoinGameRequest.java
│   │   │   │   │   │   └── MakeMoveRequest.java
│   │   │   │   │   └── response/
│   │   │   │   │       ├── PlayerResponse.java
│   │   │   │   │       ├── GameResponse.java
│   │   │   │   │       ├── MoveResponse.java
│   │   │   │   │       ├── MovesListResponse.java
│   │   │   │   │       ├── BoardResponse.java
│   │   │   │   │       └── ErrorResponse.java
│   │   │   │   └── websocket/
│   │   │   │       ├── GameEvent.java
│   │   │   │       └── GameEventPayloads.java
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   └── test/
│   │       └── java/com/gogame/
│   │           ├── service/
│   │           ├── domain/
│   │           └── controller/
│   ├── pom.xml
│   ├── Dockerfile
│   └── README.md
├── go-game-client/                  # Klient konsolowy (Java)
│   ├── src/
│   │   └── main/java/com/gogame/
│   │       ├── Main.java
│   │       ├── controller/
│   │       │   ├── APIController.java
│   │       │   └── CLIController.java
│   │       ├── dto/
│   │       │   ├── PlayerRequest.java
│   │       │   ├── PlayerResponse.java
│   │       │   ├── GameResponse.java
│   │       │   ├── GamePlayer.java
│   │       │   ├── GameMove.java
│   │       │   ├── MoveResponse.java
│   │       │   ├── BoardResponseDTO.java
│   │       │   ├── StoneDTO.java
│   │       │   ├── JoinGameRequest.java
│   │       │   ├── MakeMoveRequest.java
│   │       │   └── WaitingStatus.java
│   │       ├── model/
│   │       │   ├── Board.java
│   │       │   ├── Stone.java
│   │       │   ├── StoneColor.java
│   │       │   ├── MoveType.java
│   │       │   └── Territory.java
│   │       └── printer/
│   │           ├── BoardPrinter.java
│   │           └── BoardCharacters.java
│   └── pom.xml
├── go-game-demo/                    # Demo aplikacja
│   └── src/main/java/com/gogame/demo/
├── Dockerfile
├── test-game.sh
└── docs/
    ├── diagrams.puml
    ├── documentation.md
    └── api-examples.md
```

### 2.4 Konfiguracja Serwera (application.yml)

```yaml
server:
  port: 8080

spring:
  application:
    name: go-game-server
```

### 2.5 Zależności Maven (Server)

```xml
<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Boot WebSocket -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-websocket</artifactId>
    </dependency>

    <!-- Spring Boot Test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 2.6 Zależności Maven (Client)

```xml
<dependencies>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.17.0</version>
    </dependency>

    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-api</artifactId>
        <version>5.10.2</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## 3. Projekt API (Kontrakt JSON)

### 3.1 Przegląd Endpointów

| Metoda | Endpoint | Opis | Autoryzacja |
|--------|----------|------|-------------|
| `POST` | `/api/players` | Rejestracja nowego gracza | Nie |
| `GET` | `/api/players/{id}` | Pobranie danych gracza | Nie |
| `POST` | `/api/games/join` | Dołączenie do kolejki/gry | Tak (X-Player-Id) |
| `GET` | `/api/games/waiting/{waitingGameId}` | Sprawdzenie statusu oczekiwania | Nie |
| `GET` | `/api/games/{id}` | Pobranie stanu gry | Nie |
| `POST` | `/api/games/{id}/move` | Wykonanie ruchu | Tak (X-Player-Id) |
| `POST` | `/api/games/{id}/pass` | Pas (rezygnacja z ruchu) | Tak (X-Player-Id) |
| `POST` | `/api/games/{id}/resign` | Poddanie gry | Tak (X-Player-Id) |
| `GET` | `/api/games/{id}/board` | Pobranie stanu planszy | Nie |
| `GET` | `/api/games/{id}/moves` | Pobranie listy ruchów | Nie |

### 3.2 WebSocket Endpoints

| Endpoint | Kierunek | Opis |
|----------|----------|------|
| `/ws/game` | Connect | Połączenie WebSocket |
| `/topic/game/{gameId}` | Subscribe | Subskrypcja aktualizacji gry |

### 3.3 Szczegółowa Specyfikacja Endpointów

---

#### 3.3.1 POST `/api/players` - Rejestracja Gracza

**Request:**
```json
{
  "nickname": "GoMaster2025"
}
```

**Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "nickname": "GoMaster2025",
  "token": "token_550e8400-e29b-41d4-a716-446655440000_a1b2c3d4",
  "createdAt": "2025-12-17T10:30:00Z"
}
```

**Response (400 Bad Request):**
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Nickname must be between 3 and 20 characters"
}
```

---

#### 3.3.2 POST `/api/games/join` - Dołączenie do Gry

**Headers:**
```
X-Player-Id: 550e8400-e29b-41d4-a716-446655440000
```

**Request:**
```json
{
  "boardSize": 19
}
```

**Response (202 Accepted) - Oczekiwanie na przeciwnika:**
```json
{
  "id": "waiting-game-uuid",
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

**Response (201 Created) - Gra utworzona:**
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
  "createdAt": "2025-12-17T10:35:00Z",
  "updatedAt": "2025-12-17T10:35:00Z",
  "message": "Game created successfully"
}
```

---

#### 3.3.3 GET `/api/games/waiting/{waitingGameId}` - Sprawdzenie Statusu Oczekiwania

**Response (200 OK) - Wciąż czeka:**
```json
{
  "status": "WAITING",
  "message": "Waiting for opponent..."
}
```

**Response (200 OK) - Gra znaleziona:**
```json
{
  "status": "MATCHED",
  "gameId": "660e8400-e29b-41d4-a716-446655440001"
}
```

---

#### 3.3.4 GET `/api/games/{id}` - Stan Gry

**Response (200 OK):**
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
    "timestamp": "2025-12-17T11:45:30Z"
  },
  "createdAt": "2025-12-17T10:35:00Z",
  "updatedAt": "2025-12-17T11:45:30Z",
  "message": "Game state retrieved"
}
```

---

#### 3.3.5 POST `/api/games/{id}/move` - Wykonanie Ruchu

**Headers:**
```
X-Player-Id: 550e8400-e29b-41d4-a716-446655440000
```

**Request:**
```json
{
  "x": 3,
  "y": 3
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "move": {
    "moveNumber": 43,
    "x": 3,
    "y": 3,
    "color": "BLACK",
    "capturedStones": 2,
    "timestamp": "2025-12-17T11:46:15Z"
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
      {"x": 4, "y": 4, "color": "WHITE"}
    ]
  },
  "message": "Move made successfully"
}
```

**Response (400 Bad Request) - Nieprawidłowy ruch:**
```json
{
  "error": "POSITION_OCCUPIED",
  "message": "Position is already occupied"
}
```

**Możliwe kody błędów ruchu:**
| Kod | Opis |
|-----|------|
| `POSITION_OCCUPIED` | Pozycja jest już zajęta |
| `OUT_OF_BOUNDS` | Pozycja poza planszą |
| `NOT_YOUR_TURN` | Nie Twoja tura |
| `SUICIDE_MOVE` | Ruch samobójczy (kamień bez oddechu) |
| `GAME_NOT_FOUND` | Gra nie istnieje |
| `GAME_NOT_IN_PROGRESS` | Gra nie jest w trakcie |
---

#### 3.3.6 POST `/api/games/{id}/pass` - Pas

**Headers:**
```
X-Player-Id: 550e8400-e29b-41d4-a716-446655440000
```

**Response (200 OK):**
```json
{
  "success": true,
  "move": {
    "moveNumber": 44,
    "x": -1,
    "y": -1,
    "color": "BLACK",
    "capturedStones": 0,
    "timestamp": "2025-12-17T11:47:00Z"
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

**Response (200 OK) - Dwa pasy z rzędu (koniec gry):**
```json
{
  "success": true,
  "move": {
    "moveNumber": 45,
    "x": -1,
    "y": -1,
    "color": "WHITE",
    "capturedStones": 0,
    "timestamp": "2025-12-17T11:47:30Z"
  },
  "capturedPositions": [],
  "currentTurn": null,
  "board": {
    "size": 19,
    "stones": []
  },
  "message": "Game ended. Both players passed."
}
```

---

#### 3.3.7 POST `/api/games/{id}/resign` - Poddanie Gry

**Headers:**
```
X-Player-Id: 550e8400-e29b-41d4-a716-446655440000
```

**Response (200 OK):**
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
    "timestamp": "2025-12-17T11:45:30Z"
  },
  "createdAt": "2025-12-17T10:35:00Z",
  "updatedAt": "2025-12-17T11:48:00Z",
  "message": "Player1 resigned. Player2 wins!"
}
```

---

#### 3.3.8 GET `/api/games/{id}/board` - Stan Planszy

**Response (200 OK):**
```json
{
  "gameId": "660e8400-e29b-41d4-a716-446655440001",
  "size": 19,
  "moveNumber": 43,
  "stones": [
    {"x": 3, "y": 3, "color": "BLACK"},
    {"x": 4, "y": 4, "color": "WHITE"},
    {"x": 16, "y": 3, "color": "BLACK"}
  ],
  "blackCaptured": 3,
  "whiteCaptured": 1
}
```

---

#### 3.3.9 GET `/api/games/{id}/moves` - Lista Ruchów

**Response (200 OK):**
```json
{
  "gameId": "660e8400-e29b-41d4-a716-446655440001",
  "moves": [
    {
      "moveNumber": 1,
      "x": 3,
      "y": 3,
      "color": "BLACK",
      "timestamp": "2025-12-17T10:36:00Z"
    },
    {
      "moveNumber": 2,
      "x": 4,
      "y": 4,
      "color": "WHITE",
      "timestamp": "2025-12-17T10:36:30Z"
    }
  ],
  "message": "Moves retrieved successfully"
}
```

---

### 3.4 WebSocket Events

#### 3.4.1 Połączenie
**Endpoint:** `ws://localhost:8080/ws/game`

#### 3.4.2 Subskrypcja gry
**Topic:** `/topic/game/{gameId}`

#### 3.4.3 Event: `MOVE_MADE`
```json
{
  "type": "MOVE_MADE",
  "gameId": "660e8400-e29b-41d4-a716-446655440001",
  "move": {
    "moveNumber": 44,
    "x": 10,
    "y": 10,
    "color": "WHITE"
  },
  "currentTurn": "BLACK"
}
```

#### 3.4.4 Event: `GAME_ENDED`
```json
{
  "type": "GAME_ENDED",
  "gameId": "660e8400-e29b-41d4-a716-446655440001",
  "reason": "RESIGNATION",
  "winner": "WHITE"
}
```

---

### 3.5 Kody Statusów HTTP

| Kod | Znaczenie | Kiedy używany |
|-----|-----------|---------------|
| 200 | OK | Sukces operacji (GET, POST move/pass/resign) |
| 201 | Created | Zasób utworzony (POST player, game matched) |
| 202 | Accepted | Żądanie przyjęte, przetwarzanie (join queue) |
| 400 | Bad Request | Błąd walidacji, nieprawidłowy ruch |
| 404 | Not Found | Zasób nie istnieje (gracz, gra) |
| 500 | Internal Server Error | Błąd serwera |

---

## 4. Model Danych

### 4.1 Przechowywanie Danych

System wykorzystuje **in-memory storage** z wykorzystaniem `ConcurrentHashMap`:

```java
// W GameService
private final Map<UUID, Game> games = new ConcurrentHashMap<>();
private final Map<UUID, Instant> gameCreatedAt = new ConcurrentHashMap<>();
private final Map<UUID, Instant> gameUpdatedAt = new ConcurrentHashMap<>();

// W PlayerService
private final Map<UUID, Player> players = new ConcurrentHashMap<>();
private final Map<String, UUID> nicknameToId = new ConcurrentHashMap<>();
private final Map<UUID, String> tokens = new ConcurrentHashMap<>();
private final Map<UUID, Instant> createdAt = new ConcurrentHashMap<>();

// W GameController (matchmaking)
private final Map<Integer, UUID> waitingQueue = new HashMap<>();
private final Map<UUID, WaitingGame> waitingGames = new HashMap<>();
```

### 4.2 Główne Klasy Domenowe

#### Player
```java
public class Player {
    private UUID id;
    private String nickname;
    private StoneColor stoneColor;
    private int capturedStones;
    // metody: getNickname(), getId(), getStoneColor(), etc.
}
```

#### Game
```java
public class Game {
    private UUID id;
    private Player blackPlayer;
    private Player whitePlayer;
    private Board board;
    private GameStatus status;
    private StoneColor currentTurn;
    private List<Move> moves;
    private int consecutivePasses;
    // metody: makeMove(), pass(), resign(), etc.
}
```

#### Board
```java
public class Board {
    private int size;
    private Map<Position, Stone> stones;
    // metody: placeStone(), removeStone(), getStone(), etc.
}
```

#### Stone
```java
public class Stone {
    private Position position;
    private StoneColor color;
}
```

#### Position
```java
public class Position {
    private int x;
    private int y;
    // metody: getNeighbors(), equals(), hashCode()
}
```

#### Chain
```java
public class Chain {
    private Set<Stone> stones;
    private StoneColor color;
    // metody: getLiberties(), addStone(), contains()
}
```

#### Move
```java
public class Move {
    private int moveNumber;
    private Position position;
    private StoneColor color;
    private MoveType type; // PLACE lub PASS
    private int capturedStones;
}
```

### 4.3 Enumeracje

#### StoneColor
```java
public enum StoneColor {
    BLACK, WHITE, EMPTY
}
```

#### GameStatus
```java
public enum GameStatus {
    WAITING,
    IN_PROGRESS,
    FINISHED,
    RESIGNED
}
```

#### MoveType
```java
public enum MoveType {
    PLACE,
    PASS
}
```

### 4.4 Wyjątki

```java
public class GameNotFoundException extends RuntimeException
public class PlayerNotFoundException extends RuntimeException
public class InvalidMoveException extends RuntimeException
```

---