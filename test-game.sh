#!/bin/bash

# Go Game - Skrypt testowy API
# Tworzy dwóch graczy, rozpoczyna grę i wykonuje losowe ruchy

set -e

# Konfiguracja
BASE_URL="http://localhost:8080"
BOARD_SIZE=19
MAX_MOVES=10

# Kolory dla czytelności
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Go Game API Test ===${NC}\n"

# Generowanie unikalnych nazw graczy z timestampem
TIMESTAMP=$(date +%s)
PLAYER1_NAME="Alice_${TIMESTAMP}"
PLAYER2_NAME="Bob_${TIMESTAMP}"

# 1. Rejestracja Gracza 1
echo -e "${YELLOW}[1/7] Rejestracja gracza 1: ${PLAYER1_NAME}${NC}"
PLAYER1_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/players" \
  -H "Content-Type: application/json" \
  -d "{\"nickname\": \"${PLAYER1_NAME}\"}")

PLAYER1_ID=$(echo $PLAYER1_RESPONSE | grep -o '"id":"[^"]*' | cut -d'"' -f4)
PLAYER1_TOKEN=$(echo $PLAYER1_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)

echo -e "${GREEN}✓ Gracz 1 zarejestrowany${NC}"
echo -e "  ID: ${PLAYER1_ID}"
echo -e "  Nickname: ${PLAYER1_NAME}\n"

# 2. Rejestracja Gracza 2
echo -e "${YELLOW}[2/7] Rejestracja gracza 2: ${PLAYER2_NAME}${NC}"
PLAYER2_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/players" \
  -H "Content-Type: application/json" \
  -d "{\"nickname\": \"${PLAYER2_NAME}\"}")

PLAYER2_ID=$(echo $PLAYER2_RESPONSE | grep -o '"id":"[^"]*' | cut -d'"' -f4)
PLAYER2_TOKEN=$(echo $PLAYER2_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)

echo -e "${GREEN}✓ Gracz 2 zarejestrowany${NC}"
echo -e "  ID: ${PLAYER2_ID}"
echo -e "  Nickname: ${PLAYER2_NAME}\n"

# 3. Gracz 1 dołącza do gry (WAITING)
echo -e "${YELLOW}[3/7] Gracz 1 dołącza do kolejki (rozmiar planszy: ${BOARD_SIZE})${NC}"
JOIN1_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/games/join" \
  -H "Content-Type: application/json" \
  -H "X-Player-Id: ${PLAYER1_ID}" \
  -d "{\"boardSize\": ${BOARD_SIZE}}")

echo -e "${GREEN}✓ Gracz 1 czeka na przeciwnika${NC}\n"

# 4. Gracz 2 dołącza do gry (GAME CREATED)
echo -e "${YELLOW}[4/7] Gracz 2 dołącza do gry${NC}"
JOIN2_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/games/join" \
  -H "Content-Type: application/json" \
  -H "X-Player-Id: ${PLAYER2_ID}" \
  -d "{\"boardSize\": ${BOARD_SIZE}}")

GAME_ID=$(echo $JOIN2_RESPONSE | grep -o '"id":"[^"]*' | grep -v '"blackPlayer"' | grep -v '"whitePlayer"' | head -1 | cut -d'"' -f4)

echo -e "${GREEN}✓ Gra utworzona!${NC}"
echo -e "  Game ID: ${GAME_ID}"
echo -e "  Plansza: ${BOARD_SIZE}x${BOARD_SIZE}\n"

# 5. Sprawdzenie stanu gry
echo -e "${YELLOW}[5/7] Sprawdzanie stanu gry${NC}"
GAME_STATE=$(curl -s -X GET "${BASE_URL}/api/games/${GAME_ID}")

BLACK_PLAYER=$(echo $GAME_STATE | grep -o '"blackPlayer":{[^}]*"nickname":"[^"]*' | grep -o '"nickname":"[^"]*' | cut -d'"' -f4)
WHITE_PLAYER=$(echo $GAME_STATE | grep -o '"whitePlayer":{[^}]*"nickname":"[^"]*' | grep -o '"nickname":"[^"]*' | cut -d'"' -f4)
CURRENT_TURN=$(echo $GAME_STATE | grep -o '"currentTurn":"[^"]*' | cut -d'"' -f4)

echo -e "${GREEN}✓ Stan gry pobrany${NC}"
echo -e "  BLACK: ${BLACK_PLAYER}"
echo -e "  WHITE: ${WHITE_PLAYER}"
echo -e "  Rozpoczyna: ${CURRENT_TURN}\n"

# Określenie ID graczy na podstawie koloru
if [ "$BLACK_PLAYER" == "$PLAYER1_NAME" ]; then
    BLACK_ID=$PLAYER1_ID
    WHITE_ID=$PLAYER2_ID
else
    BLACK_ID=$PLAYER2_ID
    WHITE_ID=$PLAYER1_ID
fi

# 6. Wykonywanie losowych ruchów
echo -e "${YELLOW}[6/7] Wykonywanie ${MAX_MOVES} losowych ruchów${NC}\n"

# Tablica na sprawdzanie zajętych pozycji (prosta implementacja)
declare -A occupied_positions

for ((i=1; i<=MAX_MOVES; i++)); do
    # Ustalenie gracza na podstawie parzystości ruchu
    if [ $((i % 2)) -eq 1 ]; then
        CURRENT_PLAYER_ID=$BLACK_ID
        CURRENT_COLOR="BLACK"
        CURRENT_NAME=$BLACK_PLAYER
    else
        CURRENT_PLAYER_ID=$WHITE_ID
        CURRENT_COLOR="WHITE"
        CURRENT_NAME=$WHITE_PLAYER
    fi
    
    # Generowanie losowej pozycji (z kilkoma próbami jeśli zajęta)
    MAX_ATTEMPTS=20
    attempt=0
    move_made=false
    
    while [ $attempt -lt $MAX_ATTEMPTS ] && [ "$move_made" = false ]; do
        X=$((RANDOM % BOARD_SIZE))
        Y=$((RANDOM % BOARD_SIZE))
        POS_KEY="${X}_${Y}"
        
        if [ -z "${occupied_positions[$POS_KEY]}" ]; then
            # Wykonanie ruchu
            echo -e "${BLUE}Ruch #${i}: ${CURRENT_COLOR} (${CURRENT_NAME}) -> (${X}, ${Y})${NC}"
            
            MOVE_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/games/${GAME_ID}/move" \
              -H "Content-Type: application/json" \
              -H "X-Player-Id: ${CURRENT_PLAYER_ID}" \
              -d "{\"x\": ${X}, \"y\": ${Y}}")
            
            # Sprawdzenie czy ruch się powiódł
            if echo $MOVE_RESPONSE | grep -q '"success":true'; then
                occupied_positions[$POS_KEY]=1
                CAPTURED=$(echo $MOVE_RESPONSE | grep -o '"capturedStones":[0-9]*' | cut -d':' -f2)
                
                if [ "$CAPTURED" -gt 0 ]; then
                    echo -e "${GREEN}  ✓ Ruch wykonany! Przejęto ${CAPTURED} kamieni${NC}"
                else
                    echo -e "${GREEN}  ✓ Ruch wykonany${NC}"
                fi
                move_made=true
            else
                # Wyciągnięcie kodu błędu jeśli istnieje
                ERROR=$(echo $MOVE_RESPONSE | grep -o '"error":"[^"]*' | cut -d'"' -f4)
                if [ ! -z "$ERROR" ]; then
                    echo -e "${RED}  ✗ Błąd: ${ERROR}, próba ponowna...${NC}"
                fi
                attempt=$((attempt + 1))
            fi
        else
            attempt=$((attempt + 1))
        fi
    done
    
    if [ "$move_made" = false ]; then
        echo -e "${YELLOW}  → Nie udało się wykonać ruchu po ${MAX_ATTEMPTS} próbach, wykonuję PAS${NC}"
        PASS_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/games/${GAME_ID}/pass" \
          -H "X-Player-Id: ${CURRENT_PLAYER_ID}")
        echo -e "${GREEN}  ✓ PAS wykonany${NC}"
    fi
    
    echo ""
    sleep 0.5  # Krótka przerwa dla czytelności
done

# 7. Pobranie końcowego stanu planszy
echo -e "${YELLOW}[7/7] Pobieranie końcowego stanu planszy${NC}"
BOARD_RESPONSE=$(curl -s -X GET "${BASE_URL}/api/games/${GAME_ID}/board")

MOVE_NUMBER=$(echo $BOARD_RESPONSE | grep -o '"moveNumber":[0-9]*' | cut -d':' -f2)
BLACK_CAPTURED=$(echo $BOARD_RESPONSE | grep -o '"blackCaptured":[0-9]*' | cut -d':' -f2)
WHITE_CAPTURED=$(echo $BOARD_RESPONSE | grep -o '"whiteCaptured":[0-9]*' | cut -d':' -f2)
STONES_COUNT=$(echo $BOARD_RESPONSE | grep -o '"x":' | wc -l)

echo -e "${GREEN}✓ Plansza pobrana${NC}"
echo -e "  Numer ruchu: ${MOVE_NUMBER}"
echo -e "  Kamienie na planszy: ${STONES_COUNT}"
echo -e "  BLACK przejął: ${BLACK_CAPTURED}"
echo -e "  WHITE przejął: ${WHITE_CAPTURED}\n"

# Pobranie listy wszystkich ruchów
echo -e "${YELLOW}Pobieranie historii ruchów...${NC}"
MOVES_RESPONSE=$(curl -s -X GET "${BASE_URL}/api/games/${GAME_ID}/moves")
echo -e "${GREEN}✓ Historia ruchów pobrana${NC}\n"

# Podsumowanie
echo -e "${BLUE}=== Podsumowanie ===${NC}"
echo -e "Game ID: ${GAME_ID}"
echo -e "Gracz 1 (${BLACK_PLAYER}): ID = ${PLAYER1_ID}"
echo -e "Gracz 2 (${WHITE_PLAYER}): ID = ${PLAYER2_ID}"
echo -e "${GREEN}✓ Test zakończony pomyślnie!${NC}\n"

echo -e "${YELLOW}Sprawdź stan gry:${NC}"
echo -e "  curl -X GET \"${BASE_URL}/api/games/${GAME_ID}\"\n"
