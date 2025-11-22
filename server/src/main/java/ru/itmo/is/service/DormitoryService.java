package ru.itmo.is.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.is.dto.request.DormitoryRequest;
import ru.itmo.is.dto.response.DormitoryResponse;
import ru.itmo.is.entity.dorm.Dormitory;
import ru.itmo.is.entity.dorm.Room;
import ru.itmo.is.entity.dorm.University;
import ru.itmo.is.exception.BadRequestException;
import ru.itmo.is.exception.NotFoundException;
import ru.itmo.is.repository.DormitoryRepository;
import ru.itmo.is.repository.UniversityRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DormitoryService {
    private final DormitoryRepository dormitoryRepository;
    private final UniversityRepository universityRepository;

    public List<DormitoryResponse> getAllDormitories() {
        return dormitoryRepository.findAllByOrderById().stream().map(DormitoryResponse::new).toList();
    }

    public DormitoryResponse getDormitory(int id) {
        return dormitoryRepository.findById(id)
                .map(DormitoryResponse::new)
                .orElseThrow(() -> new NotFoundException("Dormitory not found"));
    }

    public void addDormitory(DormitoryRequest req) {
        List<University> universities = universityRepository.getByIdIn(req.getUniversityIds());
        if (universities.size() != req.getUniversityIds().size()) {
            throw new NotFoundException("University not found");
        }

        Dormitory dormitory = new Dormitory();
        dormitory.setAddress(req.getAddress());
        dormitory.setUniversities(universities);
        universities.forEach(university -> university.getDormitories().add(dormitory));
        dormitoryRepository.save(dormitory);
    }

    public void updateDormitory(int id, DormitoryRequest req) {
        Dormitory dormitory = dormitoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Dormitory not found"));

        List<University> universities = universityRepository.getByIdIn(req.getUniversityIds());
        if (universities.size() != req.getUniversityIds().size()) {
            throw new NotFoundException("University not found");
        }

        int residentNumber = dormitory.getRooms().stream()
                .map(Room::getResidents)
                .map(List::size)
                .reduce(0, Integer::sum);
        boolean addressChanged = !req.getAddress().equals(dormitory.getAddress());
        Set<Integer> srcUniversityIds = dormitory.getUniversities().stream()
                .map(University::getId).collect(Collectors.toSet());
        Set<Integer> requestUniversityIds = new HashSet<>(req.getUniversityIds());
        boolean universityRemoved = !requestUniversityIds.containsAll(srcUniversityIds);

        if (residentNumber > 0 && (addressChanged || universityRemoved)) {
            throw new BadRequestException("Cannot change dorm with residents");
        }

        dormitory.setAddress(req.getAddress());
        dormitory.getUniversities().clear();
        dormitory.setUniversities(universities);
        dormitoryRepository.save(dormitory);
    }

    public void deleteDormitory(int id) {
        Dormitory dormitory = dormitoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Dormitory not found"));
        dormitoryRepository.delete(dormitory);
    }
}
