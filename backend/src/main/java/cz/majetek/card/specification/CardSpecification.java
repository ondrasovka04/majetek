package cz.majetek.card.specification;

import cz.majetek.card.dto.CardFilterQuery;
import cz.majetek.entity.Card;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class CardSpecification {

    private CardSpecification() {
        // Utility class
    }

    private static final String F_ID = "id";
    private static final String F_NAME = "name";
    private static final String F_PRICE = "price";
    private static final String F_QUANTITY = "quantity";
    private static final String F_LOCATION = "location";
    private static final String F_ASSET_CLASS = "assetClass";
    private static final String F_GROUP = "group";
    private static final String F_PURCHASE_DATE = "purchaseDate";
    private static final String F_PURCHASE_METHOD = "purchaseMethod";
    private static final String F_PURCHASE_INVOICE = "purchaseInvoice";
    private static final String F_WRITE_OFF_DATE = "writeOffDate";
    private static final String F_CREATED_BY = "createdBy";
    private static final String F_CREATED_DATE = "createdDate";
    private static final String F_RESPONSE_PERSON = "responsePerson";
    private static final String F_NOTE = "note";

    /**
     * Creates a specification for filtering cards based on CardFilterQuery.
     */
    public static Specification<Card> filter(CardFilterQuery query) {
        return (Root<Card> root, CriteriaQuery<?> cq, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            add(predicates, query.getId(), v -> cb.equal(root.get(F_ID), v));
            add(predicates, query.getMinId(), v -> cb.ge(root.get(F_ID), v));
            add(predicates, query.getMaxId(), v -> cb.le(root.get(F_ID), v));
            addLikeIgnoreCase(predicates, query.getName(), cb, root.get(F_NAME));
            add(predicates, query.getMinPrice(), v -> cb.ge(root.get(F_PRICE), v));
            add(predicates, query.getMaxPrice(), v -> cb.le(root.get(F_PRICE), v));
            add(predicates, query.getMinQuantity(), v -> cb.ge(root.get(F_QUANTITY), v));
            add(predicates, query.getMaxQuantity(), v -> cb.le(root.get(F_QUANTITY), v));
            add(predicates, query.getLocationId(), v -> cb.equal(root.get(F_LOCATION).get(F_ID), v));
            add(predicates, query.getClassId(), v -> cb.equal(root.get(F_ASSET_CLASS).get(F_ID), v));
            add(predicates, query.getGroupId(), v -> cb.equal(root.get(F_GROUP).get(F_ID), v));
            add(predicates, query.getPurchaseMethodId(), v -> cb.equal(root.get(F_PURCHASE_METHOD).get(F_ID), v));
            add(predicates, query.getPurchaseInvoice(), v -> cb.equal(root.get(F_PURCHASE_INVOICE), v));
            add(predicates, query.getCreatedBy(), v -> cb.equal(root.get(F_CREATED_BY).get(F_ID), v));
            add(predicates, query.getResponsePerson(), v -> cb.equal(root.get(F_RESPONSE_PERSON).get(F_ID), v));
            addLikeIgnoreCase(predicates, query.getNote(), cb, root.get(F_NOTE));

            add(predicates, query.getPurchasedAfter(), v -> cb.greaterThanOrEqualTo(root.get(F_PURCHASE_DATE), v));
            add(predicates, query.getPurchasedBefore(), v -> cb.lessThanOrEqualTo(root.get(F_PURCHASE_DATE), v));
            add(predicates, query.getWriteOffAfter(), v -> cb.greaterThanOrEqualTo(root.get(F_WRITE_OFF_DATE), v));
            add(predicates, query.getWriteOffBefore(), v -> cb.lessThanOrEqualTo(root.get(F_WRITE_OFF_DATE), v));
            add(predicates, query.getCreatedAfter(), v -> cb.greaterThanOrEqualTo(root.get(F_CREATED_DATE), v));
            add(predicates, query.getCreatedBefore(), v -> cb.lessThanOrEqualTo(root.get(F_CREATED_DATE), v));

            handleFulltext(predicates, query.getFulltextSearch(), root, cb);

            if (Boolean.FALSE.equals(query.getIncludeWriteOff())) {
                predicates.add(root.get(F_WRITE_OFF_DATE).isNull());
            }

            handleAsOf(predicates, query.getAsOfDate(), root, cb);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static <T> void add(List<Predicate> predicates, T value, Function<T, Predicate> builder) {
        if (value != null) {
            Predicate p = builder.apply(value);
            if (p != null) {
                predicates.add(p);
            }
        }
    }

    private static void addLikeIgnoreCase(List<Predicate> predicates, String value, CriteriaBuilder cb, Path<String> path) {
        if (value != null && !value.isBlank()) {
            predicates.add(cb.like(cb.lower(path), "%" + value.toLowerCase() + "%"));
        }
    }

    private static void handleFulltext(List<Predicate> predicates, String search, Root<Card> root, CriteriaBuilder cb) {
        if (search == null || search.isBlank()) {
            return;
        }
        String normalizedSearch = search.toLowerCase();
        Expression<String> searchTerm = cb.function("unaccent", String.class, cb.literal(normalizedSearch));
        Expression<String> paddedId = cb.function("lpad", String.class, root.get(F_ID).as(String.class), cb.literal(4), cb.literal("0"));
        Expression<String> unaccentedName = cb.function("unaccent", String.class, cb.lower(root.get(F_NAME)));
        predicates.add(cb.or(
                cb.like(unaccentedName, cb.concat("%", cb.concat(searchTerm, "%"))),
                cb.like(paddedId, cb.concat("%", cb.concat(searchTerm, "%")))
        ));
    }

    private static void handleAsOf(List<Predicate> predicates, LocalDate asOf, Root<Card> root, CriteriaBuilder cb) {
        if (asOf == null) {
            return;
        }
        predicates.add(cb.lessThanOrEqualTo(root.get(F_PURCHASE_DATE), asOf));
        predicates.add(cb.or(root.get(F_WRITE_OFF_DATE).isNull(), cb.greaterThan(root.get(F_WRITE_OFF_DATE), asOf)));
    }
}
