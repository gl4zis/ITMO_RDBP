package ru.itmo.is.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import ru.itmo.is.entity.dorm.Dormitory;
import ru.itmo.is.entity.dorm.University;

import java.util.List;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UniversityResponse {
    int id;
    String name;
    String address;
    List<Integer> dormitoryIds;

    public UniversityResponse(University university) {
        this.id = university.getId();
        this.name = university.getName();
        this.address = university.getAddress();
        this.dormitoryIds = university.getDormitories().stream().map(Dormitory::getId).toList();
    }
}
