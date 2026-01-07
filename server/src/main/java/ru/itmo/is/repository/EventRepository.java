package ru.itmo.is.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.itmo.is.entity.Event;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends CrudRepository<Event, Long> {
    @Query(value = "SELECT e FROM Event e " +
                    "WHERE e.usr.login = :resident AND (e.type = 'IN' OR e.type = 'OUT')" +
                    "ORDER BY e.timestamp DESC LIMIT 1")
    Optional<Event> getLastInOutEvent(@Param("resident") String resident);

    List<Event> getByTypeInAndUsrLoginOrderByTimestampDesc(Collection<Event.Type> type, String resident);

    @Query("SELECT e.usr.login, MAX(e.timestamp) " +
           "FROM Event e " +
           "WHERE e.type IN :types " +
           "GROUP BY e.usr.login")
    List<Object[]> getLastEventTimesForAllResidents(@Param("types") Collection<Event.Type> types);
}
