package ru.practicum.dal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.dal.model.Interaction;
import ru.practicum.dal.model.RecommendedEventQueryRecord;

import java.util.List;
import java.util.Optional;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {
    Optional<Interaction> findByEventIdAndUserId(Long eventId, Long userId);

    @Query("""
            SELECT new ru.practicum.dal.model.RecommendedEventQueryRecord(i.eventId, sum(i.rating))
              FROM Interaction i
             WHERE i.eventId IN (:eventIds)
             GROUP BY i.eventId
            """)
    List<RecommendedEventQueryRecord> getInteractionsSumByEventIds(@Param("eventIds") List<Long> eventIds);
}
