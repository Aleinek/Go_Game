package student.pwr.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BoardResponseDTOTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldMapJsonToDtoCorrectly() throws Exception {
        String json = "{"
            + "\"size\": 19,"
            + "\"stones\": ["
            + "  {\"x\": 3, \"y\": 3, \"color\": \"BLACK\"},"
            + "  {\"x\": 10, \"y\": 10, \"color\": \"WHITE\"}"
            + "],"
            + "\"blackTerritory\": 5,"
            + "\"whiteTerritory\": 2"
            + "}";

        BoardResponseDTO dto = mapper.readValue(json, BoardResponseDTO.class);

        assertEquals(19, dto.size());
        assertEquals(2, dto.stones().size());
        assertEquals("BLACK", dto.stones().get(0).color());
        assertEquals(5, dto.blackTerritory());
        assertEquals(2, dto.whiteTerritory());
    }
}
