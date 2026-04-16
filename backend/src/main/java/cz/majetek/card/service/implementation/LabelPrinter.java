package cz.majetek.card.service.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.majetek.entity.Card;
import cz.majetek.entity.Location;
import cz.majetek.exception.card.CardException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates ZPL label code for cards and locations, optionally sends to Zebra Cloud.
 */
@Component
public class LabelPrinter {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public LabelPrinter(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Value("${zebra.apikey}")
    private String apiKey;

    @Value("${zebra.tenant}")
    private String tenant;

    @Value("${zebra.sn}")
    private String sn;

    private static final Font ZEBRA_FONT;
    private static final String ZPL_TEMPLATE_LABEL;
    private static final String ZPL_TEMPLATE_LOCATION;

    private static final FontRenderContext FONT_RENDER_CONTEXT = new FontRenderContext(new AffineTransform(), true, true);

    /** Maximum printable label width in mm (Zebra ZD621d, 40mm label stock). */
    private static final double MAX_PRINT_WIDTH_MM = 33.6;
    /** Zebra printer resolution in dots per inch. */
    private static final double PRINTER_DPI = 300;
    /** Millimeters per inch, used for DPI to mm conversion. */
    private static final double MM_PER_INCH = 25.4;
    /** Font size in points matching ZPL font E (Swiss 721) at printer resolution. */
    private static final float ZEBRA_FONT_SIZE_PT = 29.17f;

    static {
        try {
            try (InputStream is = CardService.class.getResourceAsStream("/fonts/Zebra_Swiss.ttf")) {
                if (is == null) throw new CardException("Unable to load Zebra Swiss font");
                ZEBRA_FONT = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(ZEBRA_FONT_SIZE_PT);
            }

            try (InputStream is = CardService.class.getResourceAsStream("/templates/label.zpl")) {
                if (is == null) throw new CardException("Unable to load template file");
                ZPL_TEMPLATE_LABEL = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }

            try (InputStream is = CardService.class.getResourceAsStream("/templates/location.zpl")) {
                if (is == null) throw new CardException("Unable to load template file");
                ZPL_TEMPLATE_LOCATION = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (FontFormatException | IOException e) {
            throw new CardException("Failed to initialize ZplGenerator resources");
        }
    }

    /**
     * Send ZPL file to Zebra Cloud Print API.
     */
    public void sendToZebraCloud(File file) {
        HttpHeaders headers = new HttpHeaders();

        headers.set("apikey", apiKey);
        headers.set("tenant", tenant);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("sn", sn);
        body.add("zpl_file", new FileSystemResource(file));

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response;
        try {
            response = restTemplate.postForEntity("https://api.zebra.com/v2/devices/printers/send", request, String.class);

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw new CardException("Failed to send to cloud printer");
        }

        try {
            if ("FAILURE".equals(objectMapper.readTree(response.getBody())
                    .get("responses").get(0).get("status").asText())) {
                throw new CardException("Failed to send to cloud printer");
            }
        } catch (JsonProcessingException e) {
            throw new CardException("Cannot parse ZPL response");
        }
    }

    /**
     * Check if text fits within the label width using font metrics.
     */
    private boolean willFit(String text) {
        float width = new TextLayout(text, ZEBRA_FONT, FONT_RENDER_CONTEXT).getAdvance();
        return MAX_PRINT_WIDTH_MM > (width / PRINTER_DPI) * MM_PER_INCH;
    }

    /**
     * Split text into two lines that fit the label width.
     */
    public String[] getLines(String text) {
        String[] words = text.split(" ");
        java.util.List<String> firstRow = new ArrayList<>();
        java.util.List<String> secondRow = new ArrayList<>(java.util.List.of(words));

        String tmp = "";

        for (String word : words) {
            String testLine = tmp.isEmpty() ? word : tmp + " " + word;

            if (willFit(testLine)) {
                tmp = testLine;
                firstRow.add(word);
                secondRow.removeFirst();
            } else {
                if (firstRow.isEmpty()) {
                    firstRow.add(word);
                    secondRow.removeFirst();
                }
                break;
            }
        }
        return new String[]{String.join(" ", firstRow), String.join(" ", secondRow)};
    }

    public String generateZplFromCards(List<Card> cards) {
        return cards.stream()
                .map(this::generateZplFromCard)
                .collect(Collectors.joining("\n"));
    }


    private String generateZplFromCard(Card card) {
        String[] lines = getLines(card.getName());

        return ZPL_TEMPLATE_LABEL.replace("{{ID}}", String.format("%04d", card.getId()))
                .replace("{{FIRST_ROW}}", lines[0])
                .replace("{{SECOND_ROW}}", lines[1]);
    }

    public String generateZplFromLocation(Location location) {
        String[] lines = location.getName() == null ? new String[]{"", ""} : getLines(location.getName());

        return ZPL_TEMPLATE_LOCATION.replace("{{ID}}", location.getId())
                .replace("{{FIRST_ROW}}", lines[0])
                .replace("{{SECOND_ROW}}", lines[1]);
    }
}
