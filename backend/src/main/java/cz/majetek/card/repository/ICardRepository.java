package cz.majetek.card.repository;


import cz.majetek.entity.Card;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ICardRepository extends JpaRepository<Card, Integer>, JpaSpecificationExecutor<Card> {
    @Query("SELECT COALESCE(MAX(c.id), 0) FROM Card c")
    int findMaxId();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Card c WHERE c.id = :id")
    Optional<Card> findByIdForUpdate(@Param("id") Integer id);
}
