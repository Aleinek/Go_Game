# Dokumentacja Techniczna - System Gry w Go

## Spis Treści
1. [Wprowadzenie](#1-wprowadzenie)
2. [Architektura Systemu](#2-architektura-systemu)
3. [Projekt API (Kontrakt JSON)](#3-projekt-api-kontrakt-json)
4. [Model Danych](#4-model-danych)
5. [Plan Developmentu (Roadmapa)](#5-plan-developmentu-roadmapa)

---

## 1. Wprowadzenie

### 1.1 Cel Dokumentu
Niniejsza dokumentacja opisuje architekturę i specyfikację techniczną systemu do przeprowadzania rozgrywek w grę w Go. Dokument jest przeznaczony dla zespołu deweloperskiego jako przewodnik implementacyjny dla **Iteracji 1**.

### 1.2 Zakres Iteracji 1
Zgodnie z wymaganiami, Iteracja 1 obejmuje:
- Architektura klient-serwer
- Połączenie gracza z serwerem i dołączenie do gry
- Wysyłanie ruchów między graczami (interfejs konsolowy)
- Implementacja zasad gry 1-3:
  - Gra dla dwóch graczy na planszy 9x9, 13x13 lub 19x19
  - Naprzemienne kładzenie kamieni (czarne zaczynają)
  - Mechanizm zbijania kamieni (oddechy/liberties)

### 1.3 Wymagania Niefunkcjonalne
- System musi być zaprojektowany z myślą o rozszerzalności (kolejne iteracje)
- Kod musi być czysty, testowalny i dobrze udokumentowany
- Wykorzystanie wzorców projektowych i dobrych praktyk

---

## 2. Architektura Systemu

### 2.1 Zalecany Stos Technologiczny

| Komponent | Technologia | Uzasadnienie |
|-----------|-------------|--------------|
| **Język Backend** | Java 17+ | Wymagany na zajęciach, silne typowanie, bogaty ekosystem |
| **Framework** | Spring Boot 3.x | Szybki development, wbudowane wsparcie WebSocket, REST, DI |
| **Baza Danych** | PostgreSQL 15+ | Relacyjna, ACID, wsparcie JSON, dojrzałość |
| **ORM** | Spring Data JPA / Hibernate | Mapowanie obiektowo-relacyjne, migracje |
| **Komunikacja Real-time** | WebSocket (STOMP) | Dwukierunkowa komunikacja dla aktualizacji gry |
| **Build Tool** | Maven / Gradle | Zarządzanie zależnościami |
| **Konteneryzacja** | Docker + Docker Compose | Spójne środowisko dev/prod |
| **Testy** | JUnit 5 + Mockito | Standard testowania w Javie |
| **API Docs** | OpenAPI 3.0 (Springdoc) | Automatyczna dokumentacja API |

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
├─────────────────────────────────────────────┤
│           Repository Layer                  │
│  (GameRepository, PlayerRepository, etc.)   │
├─────────────────────────────────────────────┤
│           Infrastructure Layer              │
│      (Database, WebSocket, External APIs)   │
└─────────────────────────────────────────────┘
```

#### 2.2.2 Zastosowane Wzorce

| Wzorzec | Zastosowanie | Uzasadnienie |
|---------|--------------|--------------|
| **Repository Pattern** | Warstwa dostępu do danych | Abstrakcja nad bazą danych, testowalność |
| **Service Layer** | Logika biznesowa | Separacja logiki od kontrolerów |
| **Strategy Pattern** | Walidacja ruchów | Różne strategie walidacji (suicide, ko) |
| **Observer Pattern** | WebSocket notifications | Powiadamianie graczy o zmianach |
| **Factory Pattern** | Tworzenie gier | Enkapsulacja logiki tworzenia obiektów |
| **DTO Pattern** | Transfer danych | Separacja modelu domenowego od API |
| **Builder Pattern** | Budowanie Board state | Czytelne tworzenie złożonych obiektów |

### 2.3 Struktura Projektu

```
go-game/
├── server/                          # Backend (Spring Boot)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/gogame/
│   │   │   │   ├── GoGameApplication.java
│   │   │   │   ├── config/
│   │   │   │   │   ├── WebSocketConfig.java
│   │   │   │   │   ├── SecurityConfig.java
│   │   │   │   │   └── CorsConfig.java
│   │   │   │   ├── controller/
│   │   │   │   │   ├── GameController.java
│   │   │   │   │   ├── PlayerController.java
│   │   │   │   │   └── WebSocketController.java
│   │   │   │   ├── service/
│   │   │   │   │   ├── GameService.java
│   │   │   │   │   ├── PlayerService.java
│   │   │   │   │   ├── BoardService.java
│   │   │   │   │   └── MatchmakingService.java
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── Game.java
│   │   │   │   │   │   ├── Board.java
│   │   │   │   │   │   ├── Stone.java
│   │   │   │   │   │   ├── Chain.java
│   │   │   │   │   │   ├── Position.java
│   │   │   │   │   │   ├── Player.java
│   │   │   │   │   │   └── Move.java
│   │   │   │   │   ├── enums/
│   │   │   │   │   │   ├── StoneColor.java
│   │   │   │   │   │   └── GameStatus.java
│   │   │   │   │   └── exception/
│   │   │   │   │       ├── InvalidMoveException.java
│   │   │   │   │       ├── GameNotFoundException.java
│   │   │   │   │       └── PlayerNotFoundException.java
│   │   │   │   ├── repository/
│   │   │   │   │   ├── GameRepository.java
│   │   │   │   │   ├── PlayerRepository.java
│   │   │   │   │   └── MoveRepository.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── request/
│   │   │   │   │   │   ├── CreatePlayerRequest.java
│   │   │   │   │   │   ├── JoinGameRequest.java
│   │   │   │   │   │   └── MakeMoveRequest.java
│   │   │   │   │   └── response/
│   │   │   │   │       ├── PlayerResponse.java
│   │   │   │   │       ├── GameResponse.java
│   │   │   │   │       ├── MoveResponse.java
│   │   │   │   │       └── BoardResponse.java
│   │   │   │   └── validation/
│   │   │   │       ├── MoveValidator.java
│   │   │   │       ├── LibertyCalculator.java
│   │   │   │       └── CaptureCalculator.java
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       ├── application-dev.yml
│   │   │       └── db/migration/
│   │   │           └── V1__initial_schema.sql
│   │   └── test/
│   │       └── java/com/gogame/
│   │           ├── service/
│   │           ├── domain/
│   │           └── controller/
│   ├── pom.xml
│   └── Dockerfile
├── client/                          # Klient konsolowy (Java)
│   ├── src/
│   │   └── main/java/com/gogame/client/
│   │       ├── GoGameClient.java
│   │       ├── ConsoleUI.java
│   │       ├── ApiClient.java
│   │       ├── WebSocketClient.java
│   │       └── BoardRenderer.java
│   ├── pom.xml
│   └── Dockerfile
├── docker-compose.yml
├── README.md
└── docs/
    ├── diagrams.puml
    └── documentation.md
```

### 2.4 Infrastruktura (Docker)

#### docker-compose.yml
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: go-game-db
    environment:
      POSTGRES_DB: gogame
      POSTGRES_USER: gogame
      POSTGRES_PASSWORD: gogame_secret
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U gogame"]
      interval: 5s
      timeout: 5s
      retries: 5

  server:
    build:
      context: ./server
      dockerfile: Dockerfile
    container_name: go-game-server
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/gogame
      SPRING_DATASOURCE_USERNAME: gogame
      SPRING_DATASOURCE_PASSWORD: gogame_secret
    depends_on:
      postgres:
        condition: service_healthy

volumes:
  postgres_data:
```

#### Dockerfile (Server)
```dockerfile
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 3. Projekt API (Kontrakt JSON)

### 3.1 Przegląd Endpointów

| Metoda | Endpoint | Opis | Autoryzacja |
|--------|----------|------|-------------|
| `POST` | `/api/players` | Rejestracja nowego gracza | Nie |
| `GET` | `/api/players/{id}` | Pobranie danych gracza | Tak |
| `POST` | `/api/games/join` | Dołączenie do kolejki/gry | Tak |
| `GET` | `/api/games/{id}` | Pobranie stanu gry | Tak |
| `POST` | `/api/games/{id}/moves` | Wykonanie ruchu | Tak |
| `POST` | `/api/games/{id}/pass` | Pas (rezygnacja z ruchu) | Tak |
| `POST` | `/api/games/{id}/resign` | Poddanie gry | Tak |
| `GET` | `/api/games/{id}/board` | Pobranie stanu planszy | Tak |

### 3.2 WebSocket Endpoints

| Endpoint | Kierunek | Opis |
|----------|----------|------|
| `/ws/game/{gameId}` | Subscribe | Subskrypcja aktualizacji gry |
| `/app/game/{gameId}/move` | Send | Wysłanie ruchu (alternatywa dla REST) |

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
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "createdAt": "2025-01-15T10:30:00Z"
}
```

**Response (400 Bad Request):**
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Nickname must be between 3 and 20 characters",
  "timestamp": "2025-01-15T10:30:00Z"
}
```

**Response (409 Conflict):**
```json
{
  "error": "NICKNAME_TAKEN",
  "message": "Nickname 'GoMaster2025' is already in use",
  "timestamp": "2025-01-15T10:30:00Z"
}
```

---

#### 3.3.2 POST `/api/games/join` - Dołączenie do Gry

**Request:**
```json
{
  "boardSize": 19
}
```

**Response (202 Accepted) - Oczekiwanie na przeciwnika:**
```json
{
  "status": "WAITING",
  "message": "Waiting for opponent...",
  "queuePosition": 1,
  "boardSize": 19
}
```

**Response (201 Created) - Gra utworzona:**
```json
{
  "status": "STARTED",
  "gameId": "660e8400-e29b-41d4-a716-446655440001",
  "color": "BLACK",
  "opponent": {
    "id": "770e8400-e29b-41d4-a716-446655440002",
    "nickname": "Opponent123"
  },
  "boardSize": 19,
  "message": "Game started! You play as BLACK. Your turn."
}
```

**Walidacja:**
- `boardSize` musi być jednym z: 9, 13, 19

---

#### 3.3.3 GET `/api/games/{id}` - Stan Gry

**Response (200 OK):**
```json
{
  "id": "660e8400-e29b-41d4-a716-446655440001",
  "status": "IN_PROGRESS",
  "boardSize": 19,
  "currentTurn": "BLACK",
  "blackPlayer": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "nickname": "GoMaster2025",
    "capturedStones": 3
  },
  "whitePlayer": {
    "id": "770e8400-e29b-41d4-a716-446655440002",
    "nickname": "Opponent123",
    "capturedStones": 1
  },
  "moveCount": 42,
  "lastMove": {
    "moveNumber": 42,
    "x": 15,
    "y": 4,
    "color": "WHITE",
    "capturedStones": 0,
    "timestamp": "2025-01-15T11:45:30Z"
  },
  "createdAt": "2025-01-15T10:30:00Z",
  "updatedAt": "2025-01-15T11:45:30Z"
}
```

---

#### 3.3.4 POST `/api/games/{id}/moves` - Wykonanie Ruchu

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
    "timestamp": "2025-01-15T11:46:15Z"
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
  }
}
```

**Response (400 Bad Request) - Nieprawidłowy ruch:**
```json
{
  "success": false,
  "error": "INVALID_MOVE",
  "message": "Position (3, 3) is already occupied",
  "code": "POSITION_OCCUPIED"
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
| `GAME_ENDED` | Gra już się zakończyła |

---

#### 3.3.5 POST `/api/games/{id}/pass` - Pas

**Response (200 OK):**
```json
{
  "success": true,
  "moveNumber": 44,
  "type": "PASS",
  "currentTurn": "WHITE",
  "consecutivePasses": 1,
  "message": "Pass recorded. Opponent's turn."
}
```

**Response (200 OK) - Dwa pasy z rzędu (koniec gry):**
```json
{
  "success": true,
  "gameEnded": true,
  "status": "FINISHED",
  "result": {
    "winner": "WHITE",
    "blackScore": {
      "territory": 42,
      "captures": 5,
      "total": 47
    },
    "whiteScore": {
      "territory": 45,
      "captures": 8,
      "total": 53
    }
  },
  "message": "Game ended. WHITE wins by 6 points."
}
```

---

#### 3.3.6 POST `/api/games/{id}/resign` - Poddanie Gry

**Response (200 OK):**
```json
{
  "success": true,
  "status": "RESIGNED",
  "winner": {
    "id": "770e8400-e29b-41d4-a716-446655440002",
    "nickname": "Opponent123"
  },
  "resignedBy": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "nickname": "GoMaster2025"
  },
  "message": "GoMaster2025 resigned. Opponent123 wins!"
}
```

---

#### 3.3.7 GET `/api/games/{id}/board` - Stan Planszy

**Response (200 OK):**
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
  "grid": [
    [".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", "."],
    [".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", "."],
    [".", ".", ".", "B", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", "B", ".", "."],
    [".", ".", ".", ".", "W", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", ".", "."]
  ],
  "blackCaptured": 3,
  "whiteCaptured": 1
}
```

> **Uwaga:** Pole `grid` jest reprezentacją 2D planszy gdzie:
> - `.` = puste pole
> - `B` = czarny kamień
> - `W` = biały kamień

---

### 3.4 WebSocket Events

#### 3.4.1 Subskrypcja gry
**Endpoint:** `/ws/game/{gameId}`

#### 3.4.2 Event: `OPPONENT_MOVED`
```json
{
  "type": "OPPONENT_MOVED",
  "payload": {
    "move": {
      "moveNumber": 44,
      "x": 10,
      "y": 10,
      "color": "WHITE"
    },
    "capturedPositions": [],
    "currentTurn": "BLACK"
  }
}
```

#### 3.4.3 Event: `OPPONENT_PASSED`
```json
{
  "type": "OPPONENT_PASSED",
  "payload": {
    "moveNumber": 45,
    "consecutivePasses": 1,
    "currentTurn": "BLACK"
  }
}
```

#### 3.4.4 Event: `GAME_ENDED`
```json
{
  "type": "GAME_ENDED",
  "payload": {
    "reason": "RESIGNATION",
    "winner": "WHITE",
    "resignedBy": "BLACK"
  }
}
```

#### 3.4.5 Event: `GAME_STARTED`
```json
{
  "type": "GAME_STARTED",
  "payload": {
    "gameId": "660e8400-e29b-41d4-a716-446655440001",
    "yourColor": "BLACK",
    "opponent": {
      "nickname": "Opponent123"
    },
    "boardSize": 19
  }
}
```

---

### 3.5 Kody Statusów HTTP

| Kod | Znaczenie | Kiedy używany |
|-----|-----------|---------------|
| 200 | OK | Sukces operacji (GET, PUT) |
| 201 | Created | Zasób utworzony (POST player, game) |
| 202 | Accepted | Żądanie przyjęte, przetwarzanie (join queue) |
| 400 | Bad Request | Błąd walidacji, nieprawidłowy ruch |
| 401 | Unauthorized | Brak tokenu autoryzacji |
| 403 | Forbidden | Brak uprawnień (np. ruch w cudzej grze) |
| 404 | Not Found | Zasób nie istnieje |
| 409 | Conflict | Konflikt (np. nickname zajęty) |
| 500 | Internal Server Error | Błąd serwera |

---

## 4. Model Danych

### 4.1 Schemat Bazy Danych (PostgreSQL)

```sql
-- V1__initial_schema.sql

-- Tabela graczy
CREATE TABLE players (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nickname VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    is_connected BOOLEAN DEFAULT false,
    last_activity TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_players_nickname ON players(nickname);

-- Typ wyliczeniowy dla statusu gry
CREATE TYPE game_status AS ENUM ('WAITING', 'IN_PROGRESS', 'FINISHED', 'RESIGNED', 'CANCELLED');

-- Typ wyliczeniowy dla koloru kamienia
CREATE TYPE stone_color AS ENUM ('BLACK', 'WHITE');

-- Tabela gier
CREATE TABLE games (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    black_player_id UUID REFERENCES players(id),
    white_player_id UUID REFERENCES players(id),
    winner_id UUID REFERENCES players(id),
    board_size INTEGER NOT NULL CHECK (board_size IN (9, 13, 19)),
    status game_status NOT NULL DEFAULT 'WAITING',
    current_turn stone_color NOT NULL DEFAULT 'BLACK',
    consecutive_passes INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_games_status ON games(status);
CREATE INDEX idx_games_players ON games(black_player_id, white_player_id);

-- Tabela ruchów
CREATE TABLE moves (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    game_id UUID NOT NULL REFERENCES games(id) ON DELETE CASCADE,
    player_id UUID NOT NULL REFERENCES players(id),
    move_number INTEGER NOT NULL,
    x_position INTEGER,
    y_position INTEGER,
    is_pass BOOLEAN DEFAULT false,
    captured_stones INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT valid_position CHECK (
        (is_pass = true AND x_position IS NULL AND y_position IS NULL) OR
        (is_pass = false AND x_position IS NOT NULL AND y_position IS NOT NULL)
    )
);

CREATE INDEX idx_moves_game ON moves(game_id, move_number);

-- Tabela stanów planszy (dla odtwarzania gry)
CREATE TABLE board_states (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    game_id UUID NOT NULL REFERENCES games(id) ON DELETE CASCADE,
    move_number INTEGER NOT NULL,
    board_data JSONB NOT NULL,
    black_captured INTEGER DEFAULT 0,
    white_captured INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(game_id, move_number)
);

CREATE INDEX idx_board_states_game ON board_states(game_id, move_number DESC);

-- Kolejka oczekujących na grę
CREATE TABLE waiting_queue (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    player_id UUID NOT NULL UNIQUE REFERENCES players(id) ON DELETE CASCADE,
    preferred_board_size INTEGER NOT NULL CHECK (preferred_board_size IN (9, 13, 19)),
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_waiting_queue_board_size ON waiting_queue(preferred_board_size, joined_at);

-- Trigger do aktualizacji updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_games_updated_at 
    BEFORE UPDATE ON games 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

### 4.2 Format board_data (JSONB)

```json
{
  "size": 19,
  "stones": {
    "3,3": "BLACK",
    "4,4": "WHITE",
    "16,3": "BLACK",
    "16,16": "WHITE"
  }
}
```

---

## 5. Plan Developmentu (Roadmapa)

### Legenda statusów
- ⬜ Do zrobienia
- 🔄 W trakcie
- ✅ Ukończone

---

### Milestone 1: Setup Projektu (Dzień 1-2)

| # | Zadanie | Opis | Priorytet |
|---|---------|------|-----------|
| 1.1 | ⬜ Inicjalizacja repozytorium | Utworzenie struktury katalogów, `.gitignore`, `README.md` | 🔴 Wysoki |
| 1.2 | ⬜ Setup Spring Boot (Server) | Utworzenie projektu Maven/Gradle z zależnościami: Spring Web, Spring Data JPA, Spring WebSocket, PostgreSQL Driver, Lombok | 🔴 Wysoki |
| 1.3 | ⬜ Setup Klienta Java | Utworzenie projektu Maven dla klienta konsolowego | 🔴 Wysoki |
| 1.4 | ⬜ Docker Compose | Konfiguracja `docker-compose.yml` z PostgreSQL i aplikacją | 🔴 Wysoki |
| 1.5 | ⬜ Konfiguracja aplikacji | `application.yml` z profilem dev/prod, połączenie z bazą | 🔴 Wysoki |
| 1.6 | ⬜ Flyway/Liquibase setup | Konfiguracja migracji bazy danych | 🟡 Średni |

**Deliverable:** Działający "Hello World" endpoint, aplikacja łączy się z bazą danych.

---

### Milestone 2: Warstwa Domenowa - Model Planszy (Dzień 3-5)

| # | Zadanie | Opis | Priorytet |
|---|---------|------|-----------|
| 2.1 | ⬜ Enum `StoneColor` | BLACK, WHITE, EMPTY | 🔴 Wysoki |
| 2.2 | ⬜ Enum `GameStatus` | WAITING, IN_PROGRESS, FINISHED, RESIGNED | 🔴 Wysoki |
| 2.3 | ⬜ Klasa `Position` | Reprezentacja pozycji (x, y), metoda `getNeighbors()` | 🔴 Wysoki |
| 2.4 | ⬜ Klasa `Stone` | Kamień na planszy (position, color) | 🔴 Wysoki |
| 2.5 | ⬜ Klasa `Chain` | Łańcuch kamieni, metody: `getLiberties()`, `isCaptured()` | 🔴 Wysoki |
| 2.6 | ⬜ Klasa `Board` | Plansza NxN, metody: `placeStone()`, `isValidMove()`, `isEmpty()` | 🔴 Wysoki |
| 2.7 | ⬜ Testy jednostkowe Board | Testowanie `placeStone()`, `isEmpty()`, walidacja granic | 🔴 Wysoki |

**Deliverable:** Klasa `Board` z pełną funkcjonalnością stawiania kamieni.

---

### Milestone 3: Logika Zbijania Kamieni (Dzień 6-8)

| # | Zadanie | Opis | Priorytet |
|---|---------|------|-----------|
| 3.1 | ⬜ `LibertyCalculator` | Obliczanie oddechów kamienia/łańcucha | 🔴 Wysoki |
| 3.2 | ⬜ `CaptureCalculator` | Wykrywanie i usuwanie zbitych kamieni | 🔴 Wysoki |
| 3.3 | ⬜ Walidacja ruchu samobójczego | Sprawdzanie czy ruch nie pozbawia kamienia oddechu (zasada 5) | 🔴 Wysoki |
| 3.4 | ⬜ Integracja z `Board` | Metoda `Board.placeStone()` zwraca listę zbitych kamieni | 🔴 Wysoki |
| 3.5 | ⬜ Testy zbijania | Scenariusze: pojedynczy kamień, łańcuch, rogi, krawędzie | 🔴 Wysoki |
| 3.6 | ⬜ Test ruchu samobójczego | Scenariusz: ruch w punkt bez oddechu | 🔴 Wysoki |

**Deliverable:** Pełna implementacja zasady 3 (zbijanie kamieni przez oddechy).

---

### Milestone 4: Warstwa Persystencji (Dzień 9-10)

| # | Zadanie | Opis | Priorytet |
|---|---------|------|-----------|
| 4.1 | ⬜ Encja `PlayerEntity` | Mapowanie JPA, walidacje | 🔴 Wysoki |
| 4.2 | ⬜ Encja `GameEntity` | Mapowanie JPA, relacje do graczy | 🔴 Wysoki |
| 4.3 | ⬜ Encja `MoveEntity` | Mapowanie JPA, relacja do gry | 🔴 Wysoki |
| 4.4 | ⬜ Encja `BoardStateEntity` | Przechowywanie stanu planszy (JSONB) | 🟡 Średni |
| 4.5 | ⬜ `PlayerRepository` | CRUD + findByNickname() | 🔴 Wysoki |
| 4.6 | ⬜ `GameRepository` | CRUD + findByStatus(), findByPlayerId() | 🔴 Wysoki |
| 4.7 | ⬜ `MoveRepository` | CRUD + findByGameIdOrderByMoveNumber() | 🔴 Wysoki |
| 4.8 | ⬜ Migracja bazy danych | Skrypt V1__initial_schema.sql | 🔴 Wysoki |

**Deliverable:** Działająca warstwa persystencji z testami integracyjnymi.

---

### Milestone 5: Warstwa Serwisowa (Dzień 11-13)

| # | Zadanie | Opis | Priorytet |
|---|---------|------|-----------|
| 5.1 | ⬜ `PlayerService` | createPlayer(), getPlayer(), updateConnectionStatus() | 🔴 Wysoki |
| 5.2 | ⬜ `MatchmakingService` | joinQueue(), leaveQueue(), matchPlayers() | 🔴 Wysoki |
| 5.3 | ⬜ `GameService` | createGame(), getGame(), makeMove(), pass(), resign() | 🔴 Wysoki |
| 5.4 | ⬜ `BoardService` | getCurrentBoardState(), validateMove(), applyMove() | 🔴 Wysoki |
| 5.5 | ⬜ DTO Request/Response | CreatePlayerRequest, JoinGameRequest, MakeMoveRequest, etc. | 🔴 Wysoki |
| 5.6 | ⬜ Mapowanie DTO <-> Entity | Konwertery lub MapStruct | 🟡 Średni |
| 5.7 | ⬜ Testy serwisów | Mockowanie repozytoriów, testy logiki | 🔴 Wysoki |

**Deliverable:** Pełna logika biznesowa z testami.

---

### Milestone 6: REST API (Dzień 14-15)

| # | Zadanie | Opis | Priorytet |
|---|---------|------|-----------|
| 6.1 | ⬜ `PlayerController` | POST /api/players, GET /api/players/{id} | 🔴 Wysoki |
| 6.2 | ⬜ `GameController` | POST /api/games/join, GET /api/games/{id} | 🔴 Wysoki |
| 6.3 | ⬜ `GameController` - Ruchy | POST /api/games/{id}/moves, /pass, /resign | 🔴 Wysoki |
| 6.4 | ⬜ `GameController` - Plansza | GET /api/games/{id}/board | 🔴 Wysoki |
| 6.5 | ⬜ Global Exception Handler | @ControllerAdvice, obsługa wyjątków | 🔴 Wysoki |
| 6.6 | ⬜ Walidacja wejścia | @Valid, Bean Validation | 🔴 Wysoki |
| 6.7 | ⬜ Testy kontrolerów | MockMvc, testy integracyjne | 🔴 Wysoki |

**Deliverable:** Działające REST API zgodne ze specyfikacją.

---

### Milestone 7: WebSocket (Dzień 16-17)

| # | Zadanie | Opis | Priorytet |
|---|---------|------|-----------|
| 7.1 | ⬜ `WebSocketConfig` | Konfiguracja STOMP, SockJS fallback | 🔴 Wysoki |
| 7.2 | ⬜ `WebSocketController` | Obsługa subskrypcji gry | 🔴 Wysoki |
| 7.3 | ⬜ `GameNotificationService` | Wysyłanie powiadomień: OPPONENT_MOVED, GAME_ENDED | 🔴 Wysoki |
| 7.4 | ⬜ Integracja z GameService | Trigger powiadomień po ruchu | 🔴 Wysoki |
| 7.5 | ⬜ Obsługa rozłączenia | Wykrywanie i obsługa disconnectów | 🟡 Średni |

**Deliverable:** Real-time komunikacja między graczami.

---

### Milestone 8: Klient Konsolowy (Dzień 18-20)

| # | Zadanie | Opis | Priorytet |
|---|---------|------|-----------|
| 8.1 | ⬜ `ApiClient` | HTTP client do komunikacji z REST API | 🔴 Wysoki |
| 8.2 | ⬜ `WebSocketClient` | STOMP client do subskrypcji | 🔴 Wysoki |
| 8.3 | ⬜ `BoardRenderer` | Renderowanie planszy w konsoli (ASCII art) | 🔴 Wysoki |
| 8.4 | ⬜ `ConsoleUI` | Menu, input handling, walidacja wejścia | 🔴 Wysoki |
| 8.5 | ⬜ `GoGameClient` | Główna klasa klienta, flow aplikacji | 🔴 Wysoki |
| 8.6 | ⬜ Parsowanie ruchów | Format: "A1", "C15", "pass", "resign" | 🔴 Wysoki |

**Deliverable:** Działający klient konsolowy.

---

### Milestone 9: Integracja i Testy E2E (Dzień 21-22)

| # | Zadanie | Opis | Priorytet |
|---|---------|------|-----------|
| 9.1 | ⬜ Testy E2E | Scenariusz pełnej gry: join -> moves -> end | 🔴 Wysoki |
| 9.2 | ⬜ Test dwóch klientów | Uruchomienie dwóch klientów, rozgrywka | 🔴 Wysoki |
| 9.3 | ⬜ Test zbijania w grze | Scenariusz z faktycznym zbiciem kamieni | 🔴 Wysoki |
| 9.4 | ⬜ Bug fixing | Naprawa błędów znalezionych w testach | 🔴 Wysoki |

**Deliverable:** Stabilna, przetestowana aplikacja.

---

### Milestone 10: Dokumentacja i Deploy (Dzień 23-24)

| # | Zadanie | Opis | Priorytet |
|---|---------|------|-----------|
| 10.1 | ⬜ README.md | Instrukcja uruchomienia, wymagania | 🔴 Wysoki |
| 10.2 | ⬜ Diagramy UML | Zaktualizowane diagramy w PlantUML | 🔴 Wysoki |
| 10.3 | ⬜ OpenAPI/Swagger | Dokumentacja API | 🟡 Średni |
| 10.4 | ⬜ Docker build & test | Weryfikacja buildów Docker | 🔴 Wysoki |
| 10.5 | ⬜ Przegląd kodu | Code review, refaktoring | 🟡 Średni |
| 10.6 | ⬜ Upload na ePortal | Link do GitHub | 🔴 Wysoki |

**Deliverable:** Gotowy projekt do oddania.

---

## Podsumowanie Czasowe

| Faza | Milestone | Czas |
|------|-----------|------|
| Setup | M1 | 2 dni |
| Core Domain | M2, M3 | 6 dni |
| Persistence | M4 | 2 dni |
| Services | M5 | 3 dni |
| API | M6, M7 | 4 dni |
| Client | M8 | 3 dni |
| Testing | M9 | 2 dni |
| Docs | M10 | 2 dni |
| **SUMA** | | **~24 dni robocze** |

---

## Załączniki

### A. Przykładowy Rendering Planszy (ASCII)

```
     A B C D E F G H J K L M N O P Q R S T
  19 . . . . . . . . . . . . . . . . . . . 19
  18 . . . . . . . . . . . . . . . . . . . 18
  17 . . . . . . . . . . . . . . . . . . . 17
  16 . . . + . . . . . + . . . . . + . . . 16
  15 . . . . . . . . . . . . . . . . . . . 15
  14 . . . . . . . . . . . . . . . . . . . 14
  13 . . . . . . . . . . . . . . . . . . . 13
  12 . . . . . . . . . . . . . . . . . . . 12
  11 . . . . . . . . . . . . . . . . . . . 11
  10 . . . + . . . . . + . . . . . + . . . 10
   9 . . . . . . . . . . . . . . . . . . .  9
   8 . . . . . . . . . . . . . . . . . . .  8
   7 . . . . . . . . . . . . . . . . . . .  7
   6 . . . . . . . . . . . . . . . . . . .  6
   5 . . . . . . . . . . . . . . . . . . .  5
   4 . . . ● . . . . . + . . . . . ○ . . .  4
   3 . . . . . . . . . . . . . . . . . . .  3
   2 . . . . . . . . . . . . . . . . . . .  2
   1 . . . . . . . . . . . . . . . . . . .  1
     A B C D E F G H J K L M N O P Q R S T

Black (●) captured: 0    White (○) captured: 0
Current turn: BLACK

Enter move (e.g., D4) or 'pass'/'resign': _
```

### B. Komendy Docker

```bash
# Uruchomienie całego stacka
docker-compose up -d

# Tylko baza danych (development)
docker-compose up -d postgres

# Rebuild po zmianach
docker-compose up -d --build

# Logi serwera
docker-compose logs -f server

# Zatrzymanie
docker-compose down

# Czyszczenie danych
docker-compose down -v
```

### C. Przydatne zapytania SQL (Debug)

```sql
-- Aktywne gry
SELECT g.id, p1.nickname as black, p2.nickname as white, g.status, g.current_turn
FROM games g
JOIN players p1 ON g.black_player_id = p1.id
JOIN players p2 ON g.white_player_id = p2.id
WHERE g.status = 'IN_PROGRESS';

-- Historia ruchów gry
SELECT m.move_number, p.nickname, m.x_position, m.y_position, m.is_pass, m.captured_stones
FROM moves m
JOIN players p ON m.player_id = p.id
WHERE m.game_id = 'game-uuid-here'
ORDER BY m.move_number;

-- Gracze w kolejce
SELECT p.nickname, w.preferred_board_size, w.joined_at
FROM waiting_queue w
JOIN players p ON w.player_id = p.id
ORDER BY w.joined_at;
```

---

*Dokumentacja wygenerowana: Grudzień 2025*  
*Wersja: 1.0 (Iteracja 1)*
