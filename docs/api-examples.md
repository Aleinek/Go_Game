# Go Game API - Przykłady

## Utwórz planszę
```bash
curl -X POST "http://localhost:8080/api/board/create?size=9"
```

## Pobierz stan planszy
```bash
curl -X GET "http://localhost:8080/api/board/{boardId}"
```

## Umieść kamień
```bash
curl -X POST "http://localhost:8080/api/board/{boardId}/place" \
  -H "Content-Type: application/json" \
  -d '{"row": 3, "col": 4, "color": "BLACK"}'
```

## Lista plansz
```bash
curl -X GET "http://localhost:8080/api/board/list"
```
