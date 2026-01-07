package ru.itmo.is.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.is.dto.DormitoryRequest;
import ru.itmo.is.dto.DormitoryResponse;
import ru.itmo.is.entity.dorm.Dormitory;
import ru.itmo.is.entity.dorm.Room;
import ru.itmo.is.entity.dorm.University;
import ru.itmo.is.exception.BadRequestException;
import ru.itmo.is.exception.NotFoundException;
import ru.itmo.is.mapper.DormitoryMapper;
import ru.itmo.is.repository.DormitoryRepository;
import ru.itmo.is.repository.UniversityRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DormitoryServiceTest {

    @Mock
    private DormitoryRepository dormitoryRepository;
    @Mock
    private UniversityRepository universityRepository;
    @Mock
    private DormitoryMapper dormitoryMapper;
    @InjectMocks
    private DormitoryService dormitoryService;

    private Dormitory dormitory;
    private DormitoryRequest dormitoryRequest;
    private DormitoryResponse dormitoryResponse;
    private University university;

    @BeforeEach
    void setUp() {
        university = new University();
        university.setId(1);
        university.setDormitories(new ArrayList<>());

        dormitory = new Dormitory();
        dormitory.setId(1);
        dormitory.setAddress("Test Address");
        dormitory.setUniversities(new ArrayList<>());
        dormitory.setRooms(new ArrayList<>());

        dormitoryRequest = new DormitoryRequest();
        dormitoryRequest.setAddress("Test Address");
        dormitoryRequest.setUniversityIds(List.of(1));

        dormitoryResponse = new DormitoryResponse();
        dormitoryResponse.setId(1);
        dormitoryResponse.setAddress("Test Address");
    }

    @Test
    void testGetAllDormitories_ShouldReturnList() {
        when(dormitoryRepository.findAllByOrderById()).thenReturn(List.of(dormitory));
        when(dormitoryMapper.toResponse(dormitory)).thenReturn(dormitoryResponse);

        List<DormitoryResponse> result = dormitoryService.getAllDormitories();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetDormitory_WhenExists_ShouldReturnDormitory() {
        when(dormitoryRepository.findById(1)).thenReturn(Optional.of(dormitory));
        when(dormitoryMapper.toResponse(dormitory)).thenReturn(dormitoryResponse);

        DormitoryResponse result = dormitoryService.getDormitory(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void testGetDormitory_WhenNotExists_ShouldThrowNotFoundException() {
        when(dormitoryRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            dormitoryService.getDormitory(1);
        });
    }

    @Test
    void testAddDormitory_WithValidUniversities_ShouldSaveDormitory() {
        when(universityRepository.getByIdIn(List.of(1))).thenReturn(List.of(university));

        dormitoryService.addDormitory(dormitoryRequest);

        verify(dormitoryRepository).save(any(Dormitory.class));
        verify(universityRepository).getByIdIn(List.of(1));
    }

    @Test
    void testAddDormitory_WithInvalidUniversityIds_ShouldThrowNotFoundException() {
        when(universityRepository.getByIdIn(List.of(1))).thenReturn(List.of());

        assertThrows(NotFoundException.class, () -> {
            dormitoryService.addDormitory(dormitoryRequest);
        });
    }

    @Test
    void testUpdateDormitory_WhenExists_ShouldUpdateDormitory() {
        when(dormitoryRepository.findById(1)).thenReturn(Optional.of(dormitory));
        when(universityRepository.getByIdIn(List.of(1))).thenReturn(List.of(university));

        dormitoryService.updateDormitory(1, dormitoryRequest);

        verify(dormitoryRepository).save(dormitory);
    }

    @Test
    void testUpdateDormitory_WithResidentsAndAddressChange_ShouldThrowBadRequestException() {
        Room room = new Room();
        ru.itmo.is.entity.user.Resident resident = new ru.itmo.is.entity.user.Resident();
        resident.setLogin("resident1");
        room.setResidents(List.of(resident)); // Add a resident
        dormitory.setRooms(List.of(room));
        when(dormitoryRepository.findById(1)).thenReturn(Optional.of(dormitory));
        when(universityRepository.getByIdIn(List.of(1))).thenReturn(List.of(university));
        dormitoryRequest.setAddress("New Address");

        assertThrows(BadRequestException.class, () -> {
            dormitoryService.updateDormitory(1, dormitoryRequest);
        });
    }

    @Test
    void testDeleteDormitory_WhenExists_ShouldDeleteDormitory() {
        when(dormitoryRepository.findById(1)).thenReturn(Optional.of(dormitory));

        dormitoryService.deleteDormitory(1);

        verify(dormitoryRepository).delete(dormitory);
    }
}

