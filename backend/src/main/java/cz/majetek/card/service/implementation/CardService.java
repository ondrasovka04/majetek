package cz.majetek.card.service.implementation;


import cz.majetek.auth.repository.IUserRepository;
import cz.majetek.auth.security.UserPrincipal;
import cz.majetek.card.dto.CardChangeDto;
import cz.majetek.card.dto.CardDto;
import cz.majetek.card.dto.CardExportDto;
import cz.majetek.card.dto.CardFilterQuery;
import cz.majetek.card.export.CalibriStyleStrategy;
import cz.majetek.card.export.ColumnWidthStrategy;
import cz.majetek.card.export.FreezeFirstRowHandler;
import cz.majetek.card.mapper.ICardMapper;
import cz.majetek.card.repository.ICardRepository;
import cz.majetek.card.service.ICardService;
import cz.majetek.card.specification.CardSpecification;
import cz.majetek.dictionary.assetgroup.repository.IAssetGroupRepository;
import cz.majetek.dictionary.classentity.repository.IClassRepository;
import cz.majetek.dictionary.location.repository.ILocationRepository;
import cz.majetek.dictionary.purchasemethod.repository.IPurchaseMethodRepository;
import cz.majetek.dictionary.writeoffmethod.repository.IWriteOffMethodRepository;
import cz.majetek.entity.AssetGroup;
import cz.majetek.entity.Card;
import cz.majetek.entity.Location;
import cz.majetek.entity.user.User;
import cz.majetek.exception.auth.UserException;
import cz.majetek.exception.card.CardException;
import cz.majetek.exception.dictionary.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.alibaba.excel.EasyExcelFactory.write;

/**
 * Service handling card CRUD, Excel/PDF exports and ZPL label generation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CardService implements ICardService {

    private final ICardRepository cardRepository;
    private final ILocationRepository locationRepository;
    private final IClassRepository classRepository;
    private final IAssetGroupRepository assetGroupRepository;
    private final IPurchaseMethodRepository purchaseMethodRepository;
    private final IUserRepository userRepository;
    private final IWriteOffMethodRepository writeOffMethodRepository;

    private final LabelPrinter labelPrinter;
    private final ICardMapper cardMapper;

    @Value("${export.path}")
    private String exportPath;

    @Value("${attachments.folder}")
    private String uploadRoot;

    @Override
    @Transactional(readOnly = true)
    public List<CardDto> getFilteredCards(CardFilterQuery query) {
        return cardMapper.toDto(cardRepository.findAll(CardSpecification.filter(query)));
    }

    @Value("${max.labels.per.print}")
    private long maxLabelPerPrint;

    /**
     * Generate ZPL label, optionally send to Zebra Cloud.
     */
    @Override
    @Transactional(readOnly = true)
    public InputStreamResource exportLabel(CardFilterQuery query, boolean printCloud) {
        List<Card> cards = cardRepository.findAll(CardSpecification.filter(query));
        if (cards.isEmpty()) throw new CardException("No cards found");

        if (printCloud && cards.size() > maxLabelPerPrint) {
            throw new CardException("Printing more than " + maxLabelPerPrint + " cards is forbidden");
        }

        String zplContent = labelPrinter.generateZplFromCards(cards);
        Path path = Path.of(exportPath, "label-" + Instant.now().toEpochMilli() + ".zpl");
        try {
            Files.writeString(path, "\uFEFF" + zplContent, StandardCharsets.UTF_8);

            if (printCloud) labelPrinter.sendToZebraCloud(path.toFile());

            return new InputStreamResource(new FileInputStream(path.toFile()));
        } catch (IOException e) {
            throw new CardException("Cannot write ZPL file");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardDto> getAllCards() {
        return cardMapper.toDto(cardRepository.findAll());
    }

    @Transactional(readOnly = true)
    public CardDto getCardById(Integer id) {
        Card card = cardRepository.findById(id).orElseThrow(() -> CardException.notFound(id));
        return cardMapper.toDto(card);
    }

    @Override
    @Transactional
    public CardDto createCard(CardChangeDto dto) {
        validateReferencesExist(dto.getLocationId(), dto.getClassId(), dto.getGroupId(),
                dto.getPurchaseMethodId(), dto.getWriteOffMethodId(), dto.getCreatedById(), dto.getResponsiblePersonId());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = ((UserPrincipal) authentication.getPrincipal()).user();
        Card card = cardMapper.toEntity(dto);
        card.setId(cardRepository.findMaxId() + 1);
        card.setCreatedDate(LocalDateTime.now());
        card.setCreatedBy(user);
        Card saved = cardRepository.save(card);
        return cardMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CardDto updateCard(Integer id, CardChangeDto dto) {
        Card existing = cardRepository.findById(id)
                .orElseThrow(() -> CardException.notFound(id));

        validateReferencesExist(dto.getLocationId(), dto.getClassId(), dto.getGroupId(),
                dto.getPurchaseMethodId(), dto.getWriteOffMethodId(), dto.getCreatedById(), dto.getResponsiblePersonId());

        Card updated = cardMapper.toEntity(dto);
        updated.setId(id);
        updated.setCreatedDate(existing.getCreatedDate());
        Card saved = cardRepository.save(updated);
        return cardMapper.toDto(saved);
    }

    /**
     * Delete card from DB, then clean up attachment files (best-effort).
     */
    @Override
    public void deleteCard(Integer id) {
        if (!cardRepository.existsById(id)) throw CardException.notFound(id);

        cardRepository.deleteById(id);

        Path cardFolder = Path.of(uploadRoot, String.valueOf(id));
        if (Files.exists(cardFolder)) {
            try (Stream<Path> walk = Files.walk(cardFolder)) {
                walk.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                log.warn("Failed to delete file: {}", path, e);
                            }
                        });
            } catch (IOException e) {
                log.warn("Failed to traverse attachment folder for card ID: {}", id, e);
            }
        }
    }

    /**
     * Export filtered cards to XLSX using EasyExcel.
     */
    @Override
    @Transactional(readOnly = true)
    public InputStreamResource exportExcel(CardFilterQuery query) {
        List<Card> cards = cardRepository.findAll(CardSpecification.filter(query), Sort.by(Sort.Direction.ASC, "id"));
        if (cards.isEmpty()) throw new CardException("No cards found");

        List<CardExportDto> exportList = getExportListFromCards(cards);

        try {
            Path excelPath = Path.of(exportPath, "cards-" + Instant.now().toEpochMilli() + ".xlsx");
            write(excelPath.toFile(), CardExportDto.class)
                    .registerWriteHandler(new CalibriStyleStrategy())
                    .registerWriteHandler(new ColumnWidthStrategy())
                    .registerWriteHandler(new FreezeFirstRowHandler())
                    .sheet("Karty")
                    .doWrite(exportList);

            return new InputStreamResource(new FileInputStream(excelPath.toFile()));
        } catch (IOException e) {
            throw new CardException("Failed to generate Excel export");
        }
    }

    private List<CardExportDto> getExportListFromCards(List<Card> cards) {
        return cards.stream().map(this::toExportDto).toList();
    }

    private CardExportDto toExportDto(Card card) {
        return new CardExportDto(
                String.format("%04d", card.getId()),
                card.getName(),
                card.getPrice() != null ? card.getPrice() : BigDecimal.valueOf(0),
                card.getSerialNumber(),
                card.getLocation() != null ? card.getLocation().getName() : null,
                card.getGroup() != null ? card.getGroup().getName() : null,
                card.getPurchaseInvoice(),
                card.getPurchaseDate(),
                card.getWriteOffDate(),
                card.getPurchaseMethod() != null ? card.getPurchaseMethod().getName() : null,
                card.getWriteOffMethod() != null ? card.getWriteOffMethod().getName() : null,
                card.getResponsiblePerson() != null ? card.getResponsiblePerson().getName() : null,
                card.getNote(),
                card.getCreatedDate() != null ? LocalDate.from(card.getCreatedDate()) : null,
                card.getCreatedBy() != null ? card.getCreatedBy().getName() : null
        );
    }

    @Override
    @Transactional(readOnly = true)
    public InputStreamResource exportGroupReport(String groupId, LocalDate asOf) {
        AssetGroup group = assetGroupRepository.findById(groupId).orElseThrow(() -> AssetGroupException.notFound(groupId));
        CardFilterQuery query = new CardFilterQuery();
        query.setGroupId(group.getId());
        query.setAsOfDate(asOf);
        List<Card> cards = cardRepository.findAll(CardSpecification.filter(query),
                Sort.by(Sort.Direction.ASC, "id")
        );

        byte[] pdfData = PdfGenerator.generateGroupReport(group, cards, asOf);
        Path pdfPath = Path.of(exportPath, "group_report_" + group.getId() + "_" + Instant.now().toEpochMilli() + ".pdf");

        try {
            Files.write(pdfPath, pdfData);
            return new InputStreamResource(Files.newInputStream(pdfPath));
        } catch (IOException e) {
            throw new CardException("Failed to generate PDF export");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public InputStreamResource exportAllGroupsReport(LocalDate asOf) {
        List<AssetGroup> groups = assetGroupRepository.findAll(Sort.by("id"));

        CardFilterQuery allQuery = new CardFilterQuery();
        allQuery.setAsOfDate(asOf);
        allQuery.setIncludeWriteOff(false);
        List<Card> allCards = cardRepository.findAll(CardSpecification.filter(allQuery));

        Map<String, List<Card>> cardsByGroup = allCards.stream()
                .filter(c -> c.getGroup() != null)
                .collect(Collectors.groupingBy(c -> c.getGroup().getId()));

        List<Pair<String, Double>> groupTotals = groups.stream().map(group -> {
            List<Card> cards = cardsByGroup.getOrDefault(group.getId(), List.of());
            double total = cards.stream()
                    .mapToDouble(c -> {
                        double price = c.getPrice() != null ? c.getPrice().doubleValue() : 0;
                        int qty = c.getQuantity() != null ? c.getQuantity().intValue() : 1;
                        return price * qty;
                    })
                    .sum();
            return Pair.of(group.getId() + " - " + group.getName(), total);
        }).toList();

        byte[] pdfData = PdfGenerator.generateAllGroupsReport(groupTotals, asOf);
        Path pdfPath = Path.of(exportPath, "all_groups_report_" + Instant.now().toEpochMilli() + ".pdf");

        try {
            Files.write(pdfPath, pdfData);
            return new InputStreamResource(Files.newInputStream(pdfPath));
        } catch (IOException e) {
            throw new CardException("Failed to generate all groups PDF");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public InputStreamResource exportLocationReport(String locationId, LocalDate asOf) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> LocationException.notFound(locationId));

        CardFilterQuery query = new CardFilterQuery();
        query.setLocationId(locationId);
        query.setAsOfDate(asOf);
        List<Card> cards = cardRepository.findAll(CardSpecification.filter(query),
                Sort.by(Sort.Direction.ASC, "id"));

        byte[] pdfData = PdfGenerator.generateLocationReport(location, cards, asOf);
        Path pdfPath = Path.of(exportPath, "location_report_" + locationId + "_" + Instant.now().toEpochMilli() + ".pdf");

        try {
            Files.write(pdfPath, pdfData);
            return new InputStreamResource(Files.newInputStream(pdfPath));
        } catch (IOException e) {
            throw new CardException("Failed to generate location PDF report");
        }
    }

    @Transactional(readOnly = true)
    public InputStreamResource exportCardReport(Integer cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> CardException.notFound(cardId));

        byte[] pdfData = PdfGenerator.generateCardReport(card);
        Path pdfPath = Path.of(exportPath, "card_report_" + cardId + "_" + Instant.now().toEpochMilli() + ".pdf");

        try {
            Files.write(pdfPath, pdfData);
            return new InputStreamResource(Files.newInputStream(pdfPath));
        } catch (IOException e) {
            throw new CardException("Failed to generate card PDF report");
        }
    }

    /**
     * Verify all referenced dictionary/user entities exist before save.
     */
    private void validateReferencesExist(
            String locationId,
            String classId,
            String groupId,
            String purchaseMethodId,
            String writeOffMethodId,
            String createdById,
            String responsiblePersonId
    ) {
        if (!locationRepository.existsById(locationId))
            throw LocationException.notFound(locationId);

        if (classId != null && !classRepository.existsById(classId))
            throw ClassEntityException.notFound(classId);

        if (groupId != null && !assetGroupRepository.existsById(groupId))
            throw AssetGroupException.notFound(groupId);

        if (purchaseMethodId != null && !purchaseMethodRepository.existsById(purchaseMethodId))
            throw PurchaseMethodException.notFound(purchaseMethodId);

        if (writeOffMethodId != null && !writeOffMethodRepository.existsById(writeOffMethodId))
            throw WriteOffMethodException.notFound(writeOffMethodId);

        if (createdById != null && !userRepository.existsById(createdById))
            throw UserException.notFound(createdById);

        if (responsiblePersonId != null && !userRepository.existsById(responsiblePersonId))
            throw UserException.notFound(responsiblePersonId);
    }


}
