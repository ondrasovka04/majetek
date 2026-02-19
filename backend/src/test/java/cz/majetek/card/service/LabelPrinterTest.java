package cz.majetek.card.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.majetek.card.service.implementation.LabelPrinter;
import cz.majetek.entity.Card;
import cz.majetek.entity.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LabelPrinterTest {

    private LabelPrinter labelPrinter;

    @BeforeEach
    void setUp() {
        labelPrinter = new LabelPrinter(new RestTemplate(), new ObjectMapper());
        ReflectionTestUtils.setField(labelPrinter, "apiKey", "test-key");
        ReflectionTestUtils.setField(labelPrinter, "tenant", "test-tenant");
        ReflectionTestUtils.setField(labelPrinter, "sn", "test-sn");
    }

    @Test
    void getLines_ShortText_ShouldFitInFirstLine() {
        String[] lines = labelPrinter.getLines("Short");
        assertEquals(2, lines.length);
        assertEquals("Short", lines[0]);
        assertEquals("", lines[1]);
    }

    @Test
    void getLines_LongText_ShouldSplitIntoTwoLines() {
        String[] lines = labelPrinter.getLines("This is a very long text that should split across multiple lines");
        assertEquals(2, lines.length);
        assertFalse(lines[0].isEmpty());
        assertFalse(lines[1].isEmpty());
    }

    @Test
    void getLines_SingleWord_ShouldBeOnFirstLine() {
        String[] lines = labelPrinter.getLines("Abcde");
        assertEquals("Abcde", lines[0]);
        assertEquals("", lines[1]);
    }

    @Test
    void getLines_EmptyText_ShouldThrow() {
        // TextLayout does not accept empty strings; empty names are handled before getLines is called
        assertThrows(IllegalArgumentException.class, () -> labelPrinter.getLines(""));
    }

    @Test
    void generateZplFromCards_ShouldReturnZplForAllCards() {
        Card card1 = new Card();
        card1.setId(1);
        card1.setName("Card One");

        Card card2 = new Card();
        card2.setId(2);
        card2.setName("Card Two");

        String zpl = labelPrinter.generateZplFromCards(List.of(card1, card2));
        assertNotNull(zpl);
        assertFalse(zpl.isEmpty());
        assertTrue(zpl.contains("0001"));
        assertTrue(zpl.contains("0002"));
    }

    @Test
    void generateZplFromCards_SingleCard_ShouldContainId() {
        Card card = new Card();
        card.setId(42);
        card.setName("Test Card");

        String zpl = labelPrinter.generateZplFromCards(List.of(card));
        assertTrue(zpl.contains("0042"));
    }

    @Test
    void generateZplFromLocation_WithName_ShouldContainId() {
        Location loc = new Location();
        loc.setId("LOC1");
        loc.setName("Test Location");

        String zpl = labelPrinter.generateZplFromLocation(loc);
        assertNotNull(zpl);
        assertTrue(zpl.contains("LOC1"));
    }

    @Test
    void generateZplFromLocation_NullName_ShouldHandleGracefully() {
        Location loc = new Location();
        loc.setId("LOC2");
        loc.setName(null);

        String zpl = labelPrinter.generateZplFromLocation(loc);
        assertNotNull(zpl);
        assertTrue(zpl.contains("LOC2"));
    }
}
