package ru.itmo.is.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.is.dto.UniversityRequest;
import ru.itmo.is.dto.UniversityResponse;
import ru.itmo.is.entity.dorm.University;
import ru.itmo.is.exception.NotFoundException;
import ru.itmo.is.mapper.UniversityMapper;
import ru.itmo.is.repository.UniversityRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UniversityServiceTest {

    @Mock
    private UniversityRepository universityRepository;
    @Mock
    private UniversityMapper universityMapper;
    @InjectMocks
    private UniversityService universityService;

    private University university;
    private UniversityResponse universityResponse;
    private UniversityRequest universityRequest;

    @BeforeEach
    void setUp() {
        university = new University();
        university.setId(1);
        university.setName("Test University");
        university.setAddress("Test Address");

        universityResponse = new UniversityResponse();
        universityResponse.setId(1);
        universityResponse.setName("Test University");
        universityResponse.setAddress("Test Address");

        universityRequest = new UniversityRequest();
        universityRequest.setName("Test University");
        universityRequest.setAddress("Test Address");
    }

    @Test
    void testGetAllUniversities_ShouldReturnList() {
        when(universityRepository.findAllByOrderById()).thenReturn(List.of(university));
        when(universityMapper.toResponse(university)).thenReturn(universityResponse);

        List<UniversityResponse> result = universityService.getAllUniversities();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(universityRepository).findAllByOrderById();
    }

    @Test
    void testGetUniversity_WhenExists_ShouldReturnUniversity() {
        when(universityRepository.findById(1)).thenReturn(Optional.of(university));
        when(universityMapper.toResponse(university)).thenReturn(universityResponse);

        UniversityResponse result = universityService.getUniversity(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(universityRepository).findById(1);
    }

    @Test
    void testGetUniversity_WhenNotExists_ShouldThrowNotFoundException() {
        when(universityRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            universityService.getUniversity(1);
        });
    }

    @Test
    void testAddUniversity_ShouldSaveUniversity() {
        universityService.addUniversity(universityRequest);

        verify(universityRepository).save(any(University.class));
    }

    @Test
    void testUpdateUniversity_WhenExists_ShouldUpdateUniversity() {
        when(universityRepository.findById(1)).thenReturn(Optional.of(university));

        universityService.updateUniversity(1, universityRequest);

        assertEquals("Test University", university.getName());
        assertEquals("Test Address", university.getAddress());
        verify(universityRepository).save(university);
    }

    @Test
    void testUpdateUniversity_WhenNotExists_ShouldThrowNotFoundException() {
        when(universityRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            universityService.updateUniversity(1, universityRequest);
        });
    }

    @Test
    void testDeleteUniversity_WhenExists_ShouldDeleteUniversity() {
        when(universityRepository.findById(1)).thenReturn(Optional.of(university));

        universityService.deleteUniversity(1);

        verify(universityRepository).delete(university);
    }

    @Test
    void testDeleteUniversity_WhenNotExists_ShouldThrowNotFoundException() {
        when(universityRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            universityService.deleteUniversity(1);
        });
    }
}

