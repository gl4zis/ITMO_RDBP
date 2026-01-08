package ru.itmo.is.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.is.dto.UniversityRequest;
import ru.itmo.is.dto.UniversityResponse;
import ru.itmo.is.entity.dorm.University;
import ru.itmo.is.exception.NotFoundException;
import ru.itmo.is.mapper.UniversityMapper;
import ru.itmo.is.repository.UniversityRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UniversityService {
    public static final String UNIVERSITY_NOT_FOUND = "University not found";

    private final UniversityRepository universityRepository;
    private final UniversityMapper universityMapper;

    public List<UniversityResponse> getAllUniversities() {
        return universityRepository.findAllByOrderById().stream().map(universityMapper::toResponse).toList();
    }

    public UniversityResponse getUniversity(int id) {
        return universityRepository.findById(id)
                .map(universityMapper::toResponse)
                .orElseThrow(() -> new NotFoundException(UNIVERSITY_NOT_FOUND));
    }

    public void addUniversity(UniversityRequest req) {
        University university = new University();
        university.setName(req.getName());
        university.setAddress(req.getAddress());
        universityRepository.save(university);
    }

    public void updateUniversity(int id, UniversityRequest req) {
        University university = universityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(UNIVERSITY_NOT_FOUND));
        university.setName(req.getName());
        university.setAddress(req.getAddress());
        universityRepository.save(university);
    }

    public void deleteUniversity(int id) {
        University university = universityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(UNIVERSITY_NOT_FOUND));
        universityRepository.delete(university);
    }
}
