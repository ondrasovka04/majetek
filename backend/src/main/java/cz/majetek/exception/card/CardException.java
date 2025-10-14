package cz.majetek.exception.card;

import lombok.Getter;

@Getter
public final class CardException extends RuntimeException {
    private final Integer cardId;

    private CardException(String message, Integer cardId) {
        super(message);
        this.cardId = cardId;
    }

    public CardException(String message) {
        this(message, null);
    }

    public static CardException notFound(Integer id) {
        return new CardException("Card with id " + id + " not found", id);
    }
}
