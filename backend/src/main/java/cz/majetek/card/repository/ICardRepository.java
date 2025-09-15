package cz.majetek.card.repository;


import cz.majetek.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface ICardRepository extends JpaRepository<Card, Integer>, JpaSpecificationExecutor<Card> {
    @Query("SELECT COALESCE(MAX(c.id), 0) FROM Card c")
    Integer findMaxId();
}
