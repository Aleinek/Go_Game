package com.gogame.controller;

import com.gogame.domain.enums.StoneColor;
import com.gogame.domain.exception.InvalidMoveException;
import com.gogame.domain.model.Board;
import com.gogame.domain.model.Position;
import com.gogame.domain.model.Stone;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/board")
public class BoardController {

    private final Map<String, Board> boards = new HashMap<>();

    @PostMapping("/create")
    public ResponseEntity<BoardResponse> createBoard(@RequestParam(defaultValue = "19") int size) {
        String boardId = UUID.randomUUID().toString();
        Board board = new Board(size);
        boards.put(boardId, board);
        
        return ResponseEntity.ok(new BoardResponse(
            boardId,
            size,
            "Board created successfully",
            Collections.emptyList()
        ));
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<BoardResponse> getBoard(@PathVariable String boardId) {
        Board board = boards.get(boardId);
        if (board == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<StoneDTO> stones = new ArrayList<>();
        for (int row = 0; row < board.getSize(); row++) {
            for (int col = 0; col < board.getSize(); col++) {
                Position pos = new Position(row, col);
                Stone stone = board.getStoneAt(pos);
                if (stone != null && stone.getColor() != StoneColor.EMPTY) {
                    stones.add(new StoneDTO(row, col, stone.getColor().toString()));
                }
            }
        }
        
        return ResponseEntity.ok(new BoardResponse(
            boardId,
            board.getSize(),
            "Board state retrieved",
            stones
        ));
    }

    @PostMapping("/{boardId}/place")
    public ResponseEntity<?> placeStone(
            @PathVariable String boardId,
            @RequestBody PlaceMoveRequest request) {
        
        Board board = boards.get(boardId);
        if (board == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Position position = new Position(request.row(), request.col());
            StoneColor color = StoneColor.valueOf(request.color().toUpperCase(java.util.Locale.ROOT));
            
            board.placeStone(position, color);
            
            List<StoneDTO> stones = new ArrayList<>();
            for (int row = 0; row < board.getSize(); row++) {
                for (int col = 0; col < board.getSize(); col++) {
                    Position pos = new Position(row, col);
                    Stone stone = board.getStoneAt(pos);
                    if (stone != null && stone.getColor() != StoneColor.EMPTY) {
                        stones.add(new StoneDTO(row, col, stone.getColor().toString()));
                    }
                }
            }
            
            return ResponseEntity.ok(new BoardResponse(
                boardId,
                board.getSize(),
                "Stone placed successfully",
                stones
            ));
            
        } catch (InvalidMoveException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                e.getErrorCode().toString(),
                e.getMessage()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                "INVALID_INPUT",
                e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<String> deleteBoard(@PathVariable String boardId) {
        Board removed = boards.remove(boardId);
        if (removed == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("Board deleted successfully");
    }

    @GetMapping("/list")
    public ResponseEntity<List<String>> listBoards() {
        return ResponseEntity.ok(new ArrayList<>(boards.keySet()));
    }
}

record PlaceMoveRequest(int row, int col, String color) {}

record StoneDTO(int row, int col, String color) {}

record BoardResponse(String boardId, int size, String message, List<StoneDTO> stones) {}

record ErrorResponse(String errorCode, String message) {}
