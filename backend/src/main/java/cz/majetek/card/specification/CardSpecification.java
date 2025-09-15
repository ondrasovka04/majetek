package cz.majetek.card.specification;

import cz.majetek.card.dto.CardFilterQuery;
import cz.majetek.entity.Card;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class CardSpecification {

    /**
     * Creates a specification for filtering cards based on CardFilterQuery.
     */
    public static Specification<Card> filter(CardFilterQuery query) {
        return (Root<Card> root, CriteriaQuery<?> cq, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query.getId() != null) {
                predicates.add(cb.equal(root.get("id"), query.getId()));
            }

            if (query.getMinId() != null) {
                predicates.add(cb.ge(root.get("id"), query.getMinId()));
            }

            if (query.getMaxId() != null) {
                predicates.add(cb.le(root.get("id"), query.getMaxId()));
            }

            if (query.getName() != null) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + query.getName().toLowerCase() + "%"));
            }

            if (query.getMinPrice() != null) {
                predicates.add(cb.ge(root.get("price"), query.getMinPrice()));
            }

            if (query.getMaxPrice() != null) {
                predicates.add(cb.le(root.get("price"), query.getMaxPrice()));
            }

            if (query.getMinQuantity() != null) {
                predicates.add(cb.ge(root.get("quantity"), query.getMinQuantity()));
            }

            if (query.getMaxQuantity() != null) {
                predicates.add(cb.le(root.get("quantity"), query.getMaxQuantity()));
            }

            if (query.getLocationId() != null) {
                predicates.add(cb.equal(root.get("location").get("id"), query.getLocationId()));
            }

            if (query.getClassId() != null) {
                predicates.add(cb.equal(root.get("assetClass").get("id"), query.getClassId()));
            }

            if (query.getGroupId() != null) {
                predicates.add(cb.equal(root.get("group").get("id"), query.getGroupId()));
            }

            if (query.getPurchasedAfter() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("purchaseDate"), query.getPurchasedAfter()));
            }

            if (query.getPurchasedBefore() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("purchaseDate"), query.getPurchasedBefore()));
            }

            if (query.getPurchaseMethodId() != null) {
                predicates.add(cb.equal(root.get("purchaseMethod").get("id"), query.getPurchaseMethodId()));
            }

            if (query.getPurchaseInvoice() != null) {
                predicates.add(cb.equal(root.get("purchaseInvoice"), query.getPurchaseInvoice()));
            }

            if (query.getWriteOffAfter() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("writeOffDate"), query.getWriteOffAfter()));
            }

            if (query.getWriteOffBefore() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("writeOffDate"), query.getWriteOffBefore()));
            }

            if (query.getCreatedBy() != null) {
                predicates.add(cb.equal(root.get("createdBy").get("id"), query.getCreatedBy()));
            }

            if (query.getCreatedAfter() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdDate"), query.getCreatedAfter()));
            }

            if (query.getCreatedBefore() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdDate"), query.getCreatedBefore()));
            }

            if (query.getResponsePerson() != null) {
                predicates.add(cb.equal(root.get("responsePerson").get("id"), query.getResponsePerson()));
            }

            if (query.getNote() != null) {
                predicates.add(cb.like(cb.lower(root.get("note")), "%" + query.getNote().toLowerCase() + "%"));
            }

            if (query.getFulltextSearch() != null && !query.getFulltextSearch().isBlank()) {
                String normalizedSearch = query.getFulltextSearch().toLowerCase();

                Expression<String> searchTerm = cb.function("unaccent", String.class, cb.literal(normalizedSearch));

                Expression<String> paddedId = cb.function("lpad", String.class, root.get("id").as(String.class),
                        cb.literal(4), cb.literal("0"));

                Expression<String> unaccentedName = cb.function("unaccent", String.class, cb.lower(root.get("name")));

                predicates.add(cb.or(
                        cb.like(unaccentedName, cb.concat("%", cb.concat(searchTerm, "%"))),
                        cb.like(paddedId, cb.concat("%", cb.concat(searchTerm, "%")))
                ));
            }

            if (query.getIncludeWriteOff() != null && !query.getIncludeWriteOff()) {
                predicates.add(root.get("writeOffDate").isNull());
            }

            if (query.getAsOfDate() != null) {
                LocalDate asOf = query.getAsOfDate();
                predicates.add(cb.lessThanOrEqualTo(root.get("purchaseDate"), asOf));

                predicates.add(cb.or(
                        root.get("writeOffDate").isNull(),
                        cb.greaterThan(root.get("writeOffDate"), asOf)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
