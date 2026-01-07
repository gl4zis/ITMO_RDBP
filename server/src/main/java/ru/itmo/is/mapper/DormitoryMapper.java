package ru.itmo.is.mapper;

import org.springframework.stereotype.Component;
import ru.itmo.is.dto.DormitoryResponse;
import ru.itmo.is.entity.dorm.Dormitory;
import ru.itmo.is.entity.dorm.Room;
import ru.itmo.is.entity.dorm.University;

import java.util.List;

@Component
public class DormitoryMapper {

    public DormitoryResponse toResponse(Dormitory dormitory) {
        var response = new DormitoryResponse();
        response.setId(dormitory.getId());
        response.setAddress(dormitory.getAddress());
        response.setUniversityIds(dormitory.getUniversities().stream().map(University::getId).toList());
        response.setResidentNumber(
                dormitory.getRooms().stream()
                .map(Room::getResidents)
                .map(List::size)
                .reduce(0, Integer::sum)
        );
        return response;
    }
}
