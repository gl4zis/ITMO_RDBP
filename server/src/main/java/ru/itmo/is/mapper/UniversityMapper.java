package ru.itmo.is.mapper;

import org.springframework.stereotype.Component;
import ru.itmo.is.dto.UniversityResponse;
import ru.itmo.is.entity.dorm.Dormitory;
import ru.itmo.is.entity.dorm.University;

@Component
public class UniversityMapper {

    public UniversityResponse toResponse(University university) {
        var response = new UniversityResponse();
        response.setId(university.getId());
        response.setName(university.getName());
        response.setAddress(university.getAddress());
        response.setDormitoryIds(university.getDormitories().stream().map(Dormitory::getId).toList());
        return response;
    }
}
