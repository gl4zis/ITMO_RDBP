package ru.itmo.is.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.itmo.is.entity.dorm.Room;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends CrudRepository<Room, Integer> {
    List<Room> getByTypeAndDormitoryId(Room.Type type, Integer dormId);

    Optional<Room> getByDormitoryIdAndNumber(int dormitoryId, int number);

    List<Room> findAllByOrderById();

    @Query("SELECT r FROM Room r WHERE r.dormitory.id = :dormId")
    List<Room> getInDormitory(@Param("dormId") int dormId);

    @Query("SELECT r.room.cost FROM Resident r WHERE r.login = :resident")
    int getResidentRoomCost(@Param("resident") String resident);
}
