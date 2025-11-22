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
    @Query(value = "SELECT NOT is_room_filled(:roomId)", nativeQuery = true)
    boolean isRoomFree(@Param("roomId") Integer roomId);
    Optional<Room> getByDormitoryIdAndNumber(int dormitoryId, int number);

    List<Room> findAllByOrderById();

    @Query(value = "SELECT * FROM room r WHERE r.dormitory_id = :dormId AND NOT is_room_filled(r.id)", nativeQuery = true)
    List<Room> getAvailableInDormitory(@Param("dormId") int dormId);
}
