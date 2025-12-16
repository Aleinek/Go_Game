Wzorce projektowe i architektoniczne w projekcie Go Game Client

1. DTO (Data Transfer Object)
Gdzie: Pakiet com.gogame.dto (np. BoardResponseDTO, PlayerResponse, GameMove).
Opis: Uzycie rekordow (Java Records) do przesylania danych miedzy serwerem a klientem. Oddziela to strukture danych sieciowych od wewnetrznej logiki aplikacji.

2. Fasada (Facade)
Gdzie: Klasa APIController.
Opis: Klasa ta ukrywa skomplikowana logike komunikacji sieciowej (HttpClient, ObjectMapper, JSON) za prostym interfejsem metod takich jak registerPlayer() czy joinGame().

3. MVC (Model-View-Controller)
Podzial rol w aplikacji:
- Model: Pakiet com.gogame.model (Board, Stone) – stan gry i logika.
- View (Widok): Klasa BoardPrinter oraz metody wypisujace w CLIController – prezentacja planszy i komunikatow.
- Controller: Klasa Main (przeplyw), APIController (dane) oraz CLIController (wejscie uzytkownika).

4. Static Utility Class (Klasa Narzedziowa)
Gdzie: CLIController, BoardPrinter.
Opis: Klasy skladajace sie z metod statycznych, sluzace jako zbior funkcji pomocniczych do obslugi wejscia/wyjscia.

5. Adapter 
Gdzie: Konstruktor w klasie Board (public Board(BoardResponseDTO ...)).
Opis: Konstruktor dziala jak adapter, przeksztalcajac obiekt DTO z API na wewnetrzny model domeny (Board), mapujac liste StoneDTO na tablice Stone[][].

6. Builder (Budowniczy)
Gdzie: Wewnatrz APIController (HttpRequest.newBuilder()).
Opis: Wykorzystanie wzorca z biblioteki standardowej Javy do czytelnego tworzenia zapytan HTTP.
