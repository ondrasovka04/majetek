package cz.majetek.exception.dictionary;

import lombok.Getter;

@Getter
public final class PurchaseMethodException extends RuntimeException {
    private final String purchaseMethodId;

    private PurchaseMethodException(String message, String purchaseMethodId) {
        super(message);
        this.purchaseMethodId = purchaseMethodId;
    }

    public PurchaseMethodException(String message) {
        this(message, null);
    }

    public static PurchaseMethodException notFound(String id) {
        return new PurchaseMethodException("Purchase method with id " + id + " not found", id);
    }
}
