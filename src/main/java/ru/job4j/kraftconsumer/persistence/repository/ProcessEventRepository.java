package ru.job4j.kraftconsumer.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.job4j.kraftconsumer.persistence.entity.ProcessedEventEntity;

public interface ProcessEventRepository extends JpaRepository<ProcessedEventEntity, Long> {

    @Query("SELECT count(p) > 0 FROM ProcessedEventEntity p WHERE p.messageId = :messageId")
    boolean findByMessId(String messageId);
}
