package ru.itmo.is.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.is.dto.request.UniversityRequest;
import ru.itmo.is.dto.response.UniversityResponse;
import ru.itmo.is.entity.dorm.University;
import ru.itmo.is.exception.NotFoundException;
import ru.itmo.is.repository.UniversityRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UniversityService {
    private final UniversityRepository universityRepository;

    public List<UniversityResponse> getAllUniversities() {
        return universityRepository.findAllByOrderById().stream().map(UniversityResponse::new).toList();
    }

    public UniversityResponse getUniversity(int id) {
        return universityRepository.findById(id)
                .map(UniversityResponse::new)
                .orElseThrow(() -> new NotFoundException("University not found"));
    }

    public void addUniversity(UniversityRequest req) {
        University university = new University();
        university.setName(req.getName());
        university.setAddress(req.getAddress());
        universityRepository.save(university);
    }

    public void updateUniversity(int id, UniversityRequest req) {
        University university = universityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("University not found"));
        university.setName(req.getName());
        university.setAddress(req.getAddress());
        universityRepository.save(university);
    }

    public void deleteUniversity(int id) {
        University university = universityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("University not found"));
        universityRepository.delete(university);
    }
}
