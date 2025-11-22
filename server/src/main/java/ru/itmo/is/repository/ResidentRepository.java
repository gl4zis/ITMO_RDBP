package ru.itmo.is.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.itmo.is.entity.user.Resident;

@Repository
public interface ResidentRepository extends CrudRepository<Resident, String> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO resident (login, university_id, room_id) VALUES " +
                    "(:login, :universityId, :roomId)", nativeQuery = true)
    void userIsResidentNow(
            @Param("login") String login,
            @Param("universityId") int universityId,
            @Param("roomId") int roomId
    );

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM resident WHERE login = :login", nativeQuery = true)
    void userIsNotResidentAnyMore(@Param("login") String login);
}
