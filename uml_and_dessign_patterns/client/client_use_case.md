# Przypadek Użycia: Rozegranie Partii (Klient vs Klient)

**Aktor:** Gracz (Użytkownik)
**Cel:** Rozegranie gry w Go z innym żywym przeciwnikiem za pośrednictwem serwera.
**Warunek wstępny:** Serwer jest uruchomiony, aplikacja klienta została włączona.

## Główny Scenariusz

1. **Rejestracja**
   - System prosi użytkownika o podanie pseudonimu (`CLIController.getPlayerName`).
   - System rejestruje gracza na serwerze (`apiController.registerPlayer`).

2. **Inicjalizacja Gry**
   - System prosi o podanie rozmiaru planszy (`CLIController.getBoardSize`).
   - System wysyła żądanie dołączenia do gry o podanym rozmiarze (`apiController.joinGame`).

3. **Oczekiwanie (Matchmaking)**
   - Jeśli status gry to "WAITING", System wchodzi w pętlę oczekiwania.
   - System co sekundę odpytuje serwer o status (`apiController.checkWaitingStatus`), wyświetlając kropki postępu.
   - Gdy znajdzie się przeciwnik, System pobiera początkowy stan gry (`apiController.fetchGameStatus`).

4. **Pętla Rozgrywki**
   - System określa kolor gracza (Czarny/Biały) na podstawie pseudonimu.
   - Pętla trwa, dopóki status gry to "IN_PROGRESS".

   **A. Tura Gracza:**
   - System informuje o ruchu przeciwnika (jeśli był) lub o rozpoczęciu gry.
   - System pobiera i rysuje aktualną planszę (`fetchBoard` + `BoardPrinter.printBoard`).
   - System pyta o typ ruchu (`CLIController.getMoveType`):
     - **Ruch normalny:** Użytkownik podaje współrzędne X i Y. System waliduje dane i wysyła ruch (`makeMove`). Jeśli ruch jest niepoprawny, System prosi o ponowienie.
     - **Pas:** System wysyła sygnał spasowania (`pass`).
     - **Rezygnacja:** System wysyła rezygnację (`resign`) i kończy działanie klienta.
   - Po wykonaniu akcji System ponownie rysuje zaktualizowaną planszę.

   **B. Tura Przeciwnika:**
   - System wyświetla komunikat "Czekam na ruch rywala...".
   - System usypia wątek (sleep 1000ms), a następnie odpytuje serwer o aktualizację stanu gry (`fetchGameStatus`).
   - System sprawdza, czy przeciwnik się poddał (Status "RESIGNED").

5. **Zakończenie**
   - Gra kończy się, gdy pętla zostanie przerwana (np. przez rezygnację lub zmianę statusu gry na serwerze).
   - System wyświetla odpowiedni komunikat końcowy (np. o rezygnacji przeciwnika).
