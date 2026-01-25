package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.dto.request.RequestStatus;
import ru.practicum.model.Request;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {

    @Query("""
            select r
              from Request r
             where r.requesterId = :userId
            """)
    List<Request> findUserRequests(@Param("userId") long userId);

    @Query("""
            select r
              from Request r
             where r.requesterId = :userId
               and r.eventId = :eventId
            """)
    Optional<Request> findByUserIdAndEvent(@Param("userId") long userId, @Param("eventId") long eventId);

    @Query("""
            select r
              from Request r
             where r.eventId = :eventId
            """)
    List<Request> findByEventId(@Param("eventId") long eventId);

    @Query("""
            select count(r)
              from Request r
             where r.eventId = :eventId
               and r.status = :status
            """)
    long countByEventAndStatus(@Param("eventId") long eventId, @Param("status") RequestStatus status);

    @Query("""
            SELECT r.eventId, COUNT(r)
            FROM Request r
            WHERE r.eventId IN :eventIds AND r.status = 'CONFIRMED'
            GROUP BY r.eventId
            """)
    List<Object[]> countConfirmedRequestsByEventIds(@Param("eventIds") List<Long> eventIds);
}
