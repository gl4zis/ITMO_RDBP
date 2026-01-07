package ru.itmo.is.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.is.dto.*;
import ru.itmo.is.entity.Event;
import ru.itmo.is.entity.bid.*;
import ru.itmo.is.entity.dorm.Dormitory;
import ru.itmo.is.entity.dorm.Room;
import ru.itmo.is.entity.dorm.University;
import ru.itmo.is.entity.notification.Notification;
import ru.itmo.is.entity.user.Resident;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.exception.BadRequestException;
import ru.itmo.is.exception.ForbiddenException;
import ru.itmo.is.exception.NotFoundException;
import ru.itmo.is.mapper.BidMapper;
import ru.itmo.is.mapper.RoomMapper;
import ru.itmo.is.repository.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BidServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private UniversityRepository universityRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private BidRepository bidRepository;
    @Mock
    private BidFileRepository bidFileRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private ResidentRepository residentRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BidComparator bidComparator;
    @Mock
    private BidMapper bidMapper;
    @Mock
    private RoomMapper roomMapper;
    @Mock
    private RoomService roomService;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private BidService bidService;

    private User currentUser;
    private User manager;
    private Resident resident;
    private Bid bid;
    private OccupationBid occupationBid;
    private DepartureBid departureBid;
    private RoomChangeBid roomChangeBid;
    private University university;
    private Dormitory dormitory;
    private Room room;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setLogin("user1");
        currentUser.setRole(User.Role.RESIDENT);

        manager = new User();
        manager.setLogin("manager1");
        manager.setRole(User.Role.MANAGER);

        resident = new Resident();
        resident.setLogin("user1");
        resident.setRole(User.Role.RESIDENT);

        university = new University();
        university.setId(1);
        university.setDormitories(new ArrayList<>());

        dormitory = new Dormitory();
        dormitory.setId(1);
        university.getDormitories().add(dormitory);

        room = new Room();
        room.setId(1);
        room.setType(Room.Type.BLOCK);
        room.setCapacity(2);
        room.setResidents(new ArrayList<>());
        room.setDormitory(dormitory);

        bid = new Bid();
        bid.setId(1L);
        bid.setType(Bid.Type.EVICTION);
        bid.setStatus(Bid.Status.IN_PROCESS);
        bid.setSender(currentUser);
        bid.setFiles(new ArrayList<>());

        occupationBid = new OccupationBid();
        occupationBid.setId(2L);
        occupationBid.setStatus(Bid.Status.IN_PROCESS);
        occupationBid.setSender(currentUser);
        occupationBid.setUniversity(university);
        occupationBid.setDormitory(dormitory);
        occupationBid.setFiles(new ArrayList<>());

        departureBid = new DepartureBid();
        departureBid.setId(3L);
        departureBid.setStatus(Bid.Status.IN_PROCESS);
        departureBid.setSender(currentUser);
        departureBid.setDayFrom(LocalDate.now().plusDays(1));
        departureBid.setDayTo(LocalDate.now().plusDays(10));
        departureBid.setFiles(new ArrayList<>());

        roomChangeBid = new RoomChangeBid();
        roomChangeBid.setId(4L);
        roomChangeBid.setStatus(Bid.Status.IN_PROCESS);
        roomChangeBid.setSender(currentUser);
        roomChangeBid.setRoomPreferType(Room.Type.BLOCK);
        roomChangeBid.setFiles(new ArrayList<>());
    }

    @Test
    void testGetSelfOpenedBidTypes_ShouldReturnBidTypes() {
        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(bidRepository.getOpenedBidTypes("user1")).thenReturn(List.of(Bid.Type.OCCUPATION));
        when(bidMapper.mapBidTypeToDto(Bid.Type.OCCUPATION)).thenReturn(BidType.OCCUPATION);

        List<BidType> result = bidService.getSelfOpenedBidTypes();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetInProcessBids_ShouldReturnSortedBids() {
        Bid bid2 = new Bid();
        bid2.setId(2L);
        when(bidRepository.getByStatusIn(List.of(Bid.Status.IN_PROCESS))).thenReturn(List.of(bid, bid2));
        when(bidComparator.compare(any(), any())).thenReturn(0);
        when(bidMapper.mapBidToDto(any(Bid.class))).thenReturn(new EvictionResponse());

        List<BidResponse> result = bidService.getInProcessBids();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(bidComparator, atLeastOnce()).compare(any(), any());
    }

    @Test
    void testGetPendingBids_ShouldReturnBids() {
        when(bidRepository.getByStatusIn(List.of(Bid.Status.PENDING_REVISION))).thenReturn(List.of(bid));
        when(bidMapper.mapBidToDto(bid)).thenReturn(new EvictionResponse());

        List<BidResponse> result = bidService.getPendingBids();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetArchivedBids_ShouldReturnBids() {
        bid.setStatus(Bid.Status.ACCEPTED);
        when(bidRepository.getByStatusIn(List.of(Bid.Status.ACCEPTED, Bid.Status.DENIED)))
                .thenReturn(List.of(bid));
        when(bidMapper.mapBidToDto(bid)).thenReturn(new EvictionResponse());

        List<BidResponse> result = bidService.getArchivedBids();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetSelfBids_ShouldReturnBids() {
        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(bidRepository.getBySenderLoginOrderByIdDesc("user1")).thenReturn(List.of(bid));
        when(bidMapper.mapBidToDto(bid)).thenReturn(new EvictionResponse());

        List<BidResponse> result = bidService.getSelfBids();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetBid_WhenManager_ShouldReturnBid() {
        when(bidRepository.findById(1L)).thenReturn(Optional.of(bid));
        when(userService.getCurrentUserOrThrow()).thenReturn(manager);
        when(bidMapper.mapBidToDto(bid)).thenReturn(new EvictionResponse());

        BidResponse result = bidService.getBid(1L);

        assertNotNull(result);
    }

    @Test
    void testGetBid_WhenSender_ShouldReturnBid() {
        when(bidRepository.findById(1L)).thenReturn(Optional.of(bid));
        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(bidMapper.mapBidToDto(bid)).thenReturn(new EvictionResponse());

        BidResponse result = bidService.getBid(1L);

        assertNotNull(result);
    }

    @Test
    void testGetBid_WhenNotAuthorized_ShouldThrowForbiddenException() {
        User otherUser = new User();
        otherUser.setLogin("other");
        otherUser.setRole(User.Role.RESIDENT);
        when(bidRepository.findById(1L)).thenReturn(Optional.of(bid));
        when(userService.getCurrentUserOrThrow()).thenReturn(otherUser);

        assertThrows(ForbiddenException.class, () -> {
            bidService.getBid(1L);
        });
    }

    @Test
    void testGetBid_WhenNotFound_ShouldThrowNotFoundException() {
        when(bidRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            bidService.getBid(1L);
        });
    }

    @Test
    void testDenyBid_ShouldUpdateBidStatus() {
        when(bidRepository.findById(1L)).thenReturn(Optional.of(bid));
        when(userService.getCurrentUserOrThrow()).thenReturn(manager);

        bidService.denyBid(1L, "Denied");

        assertEquals(Bid.Status.DENIED, bid.getStatus());
        assertEquals("Denied", bid.getComment());
        verify(bidRepository).save(bid);
    }

    @Test
    void testDenyBid_WhenNotInProcess_ShouldThrowNotFoundException() {
        bid.setStatus(Bid.Status.ACCEPTED);
        when(bidRepository.findById(1L)).thenReturn(Optional.of(bid));

        assertThrows(NotFoundException.class, () -> {
            bidService.denyBid(1L, "Denied");
        });
    }

    @Test
    void testPendBid_ShouldUpdateStatusAndCreateNotification() {
        when(bidRepository.findById(1L)).thenReturn(Optional.of(bid));
        when(userService.getCurrentUserOrThrow()).thenReturn(manager);

        bidService.pendBid(1L, "Need revision");

        assertEquals(Bid.Status.PENDING_REVISION, bid.getStatus());
        assertEquals("Need revision", bid.getComment());
        verify(bidRepository).save(bid);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void testCreateOccupationBid_ShouldSaveBid() {
        OccupationRequest req = new OccupationRequest();
        req.setUniversityId(1);
        req.setDormitoryId(1);
        req.setText("Test");
        req.setAttachmentKeys(new ArrayList<>());

        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(bidRepository.existsBySenderLoginAndTypeAndStatusIn(
                eq("user1"), eq(Bid.Type.OCCUPATION), anyList())).thenReturn(false);
        when(universityRepository.findById(1)).thenReturn(Optional.of(university));
        when(bidFileRepository.getByKeyIn(anyList())).thenReturn(new ArrayList<>());

        bidService.createOccupationBid(req);

        verify(bidRepository).save(any(OccupationBid.class));
    }

    @Test
    void testCreateOccupationBid_WhenBidExists_ShouldThrowBadRequestException() {
        OccupationRequest req = new OccupationRequest();
        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(bidRepository.existsBySenderLoginAndTypeAndStatusIn(
                eq("user1"), eq(Bid.Type.OCCUPATION), anyList())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> {
            bidService.createOccupationBid(req);
        });
    }

    @Test
    void testCreateOccupationBid_WhenUniversityNotFound_ShouldThrowBadRequestException() {
        OccupationRequest req = new OccupationRequest();
        req.setUniversityId(999);
        req.setDormitoryId(1);
        req.setText("Test");
        req.setAttachmentKeys(new ArrayList<>());

        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(bidRepository.existsBySenderLoginAndTypeAndStatusIn(
                eq("user1"), eq(Bid.Type.OCCUPATION), anyList())).thenReturn(false);
        when(universityRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> {
            bidService.createOccupationBid(req);
        });
    }

    @Test
    void testUpdateOccupationBid_ShouldUpdateBid() {
        OccupationRequest req = new OccupationRequest();
        req.setUniversityId(1);
        req.setDormitoryId(1);
        req.setText("Updated");
        req.setAttachmentKeys(new ArrayList<>());

        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(bidRepository.findById(2L)).thenReturn(Optional.of(occupationBid));
        when(universityRepository.findById(1)).thenReturn(Optional.of(university));
        when(bidFileRepository.getByKeyIn(anyList())).thenReturn(new ArrayList<>());

        bidService.updateOccupationBid(2L, req);

        verify(bidRepository).save(occupationBid);
    }

    @Test
    void testCreateEvictionBid_ShouldSaveBid() {
        EvictionRequest req = new EvictionRequest();
        req.setText("Test");
        req.setAttachmentKeys(new ArrayList<>());

        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(bidRepository.existsBySenderLoginAndTypeAndStatusIn(
                eq("user1"), eq(Bid.Type.EVICTION), anyList())).thenReturn(false);
        when(bidFileRepository.getByKeyIn(anyList())).thenReturn(new ArrayList<>());

        bidService.createEvictionBid(req);

        verify(bidRepository).save(any(Bid.class));
    }

    @Test
    void testCreateDepartureBid_ShouldSaveBid() {
        DepartureRequest req = new DepartureRequest();
        req.setText("Test");
        req.setDayFrom(LocalDate.now().plusDays(1));
        req.setDayTo(LocalDate.now().plusDays(10));
        req.setAttachmentKeys(new ArrayList<>());

        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(bidRepository.existsBySenderLoginAndTypeAndStatusIn(
                eq("user1"), eq(Bid.Type.DEPARTURE), anyList())).thenReturn(false);
        when(bidFileRepository.getByKeyIn(anyList())).thenReturn(new ArrayList<>());

        bidService.createDepartureBid(req);

        verify(bidRepository).save(any(DepartureBid.class));
    }

    @Test
    void testCreateRoomChangeBid_ShouldSaveBid() {
        RoomChangeRequest req = new RoomChangeRequest();
        req.setText("Test");
        req.setRoomPreferType(RoomType.BLOCK);
        req.setAttachmentKeys(new ArrayList<>());

        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(bidRepository.existsBySenderLoginAndTypeAndStatusIn(
                eq("user1"), eq(Bid.Type.ROOM_CHANGE), anyList())).thenReturn(false);
        when(roomMapper.mapRoomTypeToModel(RoomType.BLOCK)).thenReturn(Room.Type.BLOCK);
        when(bidFileRepository.getByKeyIn(anyList())).thenReturn(new ArrayList<>());

        bidService.createRoomChangeBid(req);

        verify(bidRepository).save(any(RoomChangeBid.class));
    }

    @Test
    void testCreateRoomChangeBid_WithRoomToId_ShouldSaveBid() {
        RoomChangeRequest req = new RoomChangeRequest();
        req.setText("Test");
        req.setRoomToId(1);
        req.setRoomPreferType(null);
        req.setAttachmentKeys(new ArrayList<>());

        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(bidRepository.existsBySenderLoginAndTypeAndStatusIn(
                eq("user1"), eq(Bid.Type.ROOM_CHANGE), anyList())).thenReturn(false);
        when(roomRepository.findById(1)).thenReturn(Optional.of(room));
        when(bidFileRepository.getByKeyIn(anyList())).thenReturn(new ArrayList<>());

        bidService.createRoomChangeBid(req);

        verify(bidRepository).save(any(RoomChangeBid.class));
    }

    @Test
    void testAcceptBid_WithOccupationBid_ShouldAcceptAndAssignRoom() {
        when(bidRepository.findById(2L)).thenReturn(Optional.of(occupationBid));
        when(userService.getCurrentUserOrThrow()).thenReturn(manager);
        when(roomRepository.getByTypeAndDormitoryId(Room.Type.BLOCK, 1))
                .thenReturn(List.of(room));
        when(roomService.isRoomFree(any())).thenReturn(true);
        doNothing().when(residentRepository).userIsResidentNow(anyString(), anyInt(), anyInt());
        when(userRepository.save(any(User.class))).thenReturn(currentUser);
        when(eventRepository.save(any(Event.class))).thenReturn(new Event());

        bidService.acceptBid(2L);

        assertEquals(Bid.Status.ACCEPTED, occupationBid.getStatus());
        verify(residentRepository).userIsResidentNow(anyString(), anyInt(), anyInt());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void testAcceptBid_WithEvictionBid_ShouldEvictResident() {
        when(bidRepository.findById(1L)).thenReturn(Optional.of(bid));
        when(userService.getCurrentUserOrThrow()).thenReturn(manager);
        when(userService.getResidentByLogin("user1")).thenReturn(resident);
        when(bidRepository.getBySenderLoginAndStatusIn(anyString(), anyList()))
                .thenReturn(new ArrayList<>());
        doNothing().when(residentRepository).userIsNotResidentAnyMore(anyString());
        when(userRepository.save(any(User.class))).thenReturn(resident);
        when(eventRepository.save(any(Event.class))).thenReturn(new Event());

        bidService.acceptBid(1L);

        assertEquals(Bid.Status.ACCEPTED, bid.getStatus());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void testAcceptBid_WithDepartureBid_ShouldAccept() {
        when(bidRepository.findById(3L)).thenReturn(Optional.of(departureBid));
        when(userService.getCurrentUserOrThrow()).thenReturn(manager);

        bidService.acceptBid(3L);

        assertEquals(Bid.Status.ACCEPTED, departureBid.getStatus());
        verify(bidRepository).save(departureBid);
    }

    @Test
    void testAcceptBid_WithRoomChangeBid_ShouldChangeRoom() {
        resident.setRoom(room);
        Room newRoom = new Room();
        newRoom.setId(2);
        newRoom.setCapacity(2);
        newRoom.setResidents(new ArrayList<>());
        newRoom.setDormitory(dormitory);

        roomChangeBid.setRoomTo(newRoom);

        when(bidRepository.findById(4L)).thenReturn(Optional.of(roomChangeBid));
        when(userService.getCurrentUserOrThrow()).thenReturn(manager);
        when(userService.getResidentByLogin("user1")).thenReturn(resident);
        when(residentRepository.save(any(Resident.class))).thenReturn(resident);
        when(eventRepository.save(any(Event.class))).thenReturn(new Event());
        when(roomService.isRoomFree(any())).thenReturn(true);

        bidService.acceptBid(4L);

        assertEquals(Bid.Status.ACCEPTED, roomChangeBid.getStatus());
        verify(residentRepository).save(resident);
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void testAcceptBid_WithRoomChangeBid_WhenRoomFull_ShouldThrowBadRequestException() {
        resident.setRoom(room);
        Room fullRoom = new Room();
        fullRoom.setId(2);
        fullRoom.setCapacity(2);
        fullRoom.setResidents(List.of(new Resident(), new Resident()));
        fullRoom.setDormitory(dormitory);

        roomChangeBid.setRoomTo(fullRoom);

        when(bidRepository.findById(4L)).thenReturn(Optional.of(roomChangeBid));
        when(userService.getCurrentUserOrThrow()).thenReturn(manager);
        when(userService.getResidentByLogin("user1")).thenReturn(resident);

        assertThrows(BadRequestException.class, () -> {
            bidService.acceptBid(4L);
        });
    }

    @Test
    void testAcceptBid_WithOccupationBid_WhenNoFreeRoom_ShouldThrowBadRequestException() {
        when(bidRepository.findById(2L)).thenReturn(Optional.of(occupationBid));
        when(userService.getCurrentUserOrThrow()).thenReturn(manager);
        when(roomRepository.getByTypeAndDormitoryId(Room.Type.BLOCK, 1))
                .thenReturn(List.of(room));
        when(roomService.isRoomFree(any())).thenReturn(false);
        when(roomRepository.getByTypeAndDormitoryId(Room.Type.AISLE, 1))
                .thenReturn(new ArrayList<>());

        assertThrows(BadRequestException.class, () -> {
            bidService.acceptBid(2L);
        });
    }

    @Test
    void testEvictResident_ShouldEvictAndDenyBids() {
        Bid openBid = new Bid();
        openBid.setId(5L);
        openBid.setStatus(Bid.Status.IN_PROCESS);

        when(userService.getResidentByLogin("user1")).thenReturn(resident);
        when(bidRepository.getBySenderLoginAndStatusIn(eq("user1"), anyList()))
                .thenReturn(List.of(openBid));
        when(bidRepository.findById(5L)).thenReturn(Optional.of(openBid));
        when(userService.getCurrentUserOrThrow()).thenReturn(manager);
        doNothing().when(residentRepository).userIsNotResidentAnyMore("user1");
        when(userRepository.save(any(User.class))).thenReturn(resident);
        when(eventRepository.save(any(Event.class))).thenReturn(new Event());

        bidService.evictResident("user1");

        assertEquals(User.Role.NON_RESIDENT, resident.getRole());
        verify(userRepository).save(resident);
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void testAcceptBid_WhenNotFound_ShouldThrowNotFoundException() {
        when(bidRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            bidService.acceptBid(1L);
        });
    }

    @Test
    void testAcceptBid_WhenNotInProcess_ShouldThrowNotFoundException() {
        bid.setStatus(Bid.Status.ACCEPTED);
        when(bidRepository.findById(1L)).thenReturn(Optional.of(bid));

        assertThrows(NotFoundException.class, () -> {
            bidService.acceptBid(1L);
        });
    }

    @Test
    void testPendBid_WhenNotFound_ShouldThrowNotFoundException() {
        when(bidRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            bidService.pendBid(1L, "Comment");
        });
    }

    @Test
    void testPendBid_WhenNotInProcess_ShouldThrowNotFoundException() {
        bid.setStatus(Bid.Status.ACCEPTED);
        when(bidRepository.findById(1L)).thenReturn(Optional.of(bid));

        assertThrows(NotFoundException.class, () -> {
            bidService.pendBid(1L, "Comment");
        });
    }

    @Test
    void testUpdateOccupationBid_WhenBidNotFound_ShouldThrowNotFoundException() {
        OccupationRequest req = new OccupationRequest();
        when(bidRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            bidService.updateOccupationBid(2L, req);
        });
    }

    @Test
    void testUpdateOccupationBid_WhenWrongSender_ShouldThrowForbiddenException() {
        User otherUser = new User();
        otherUser.setLogin("other");
        OccupationRequest req = new OccupationRequest();
        when(bidRepository.findById(2L)).thenReturn(Optional.of(occupationBid));
        when(userService.getCurrentUserOrThrow()).thenReturn(otherUser);

        assertThrows(ForbiddenException.class, () -> {
            bidService.updateOccupationBid(2L, req);
        });
    }

    @Test
    void testUpdateOccupationBid_WhenWrongType_ShouldThrowBadRequestException() {
        OccupationRequest req = new OccupationRequest();
        bid.setType(Bid.Type.EVICTION);
        when(bidRepository.findById(1L)).thenReturn(Optional.of(bid));
        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);

        assertThrows(BadRequestException.class, () -> {
            bidService.updateOccupationBid(1L, req);
        });
    }

    @Test
    void testUpdateOccupationBid_WhenNotEditable_ShouldThrowBadRequestException() {
        OccupationRequest req = new OccupationRequest();
        occupationBid.setStatus(Bid.Status.ACCEPTED);
        when(bidRepository.findById(2L)).thenReturn(Optional.of(occupationBid));
        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);

        assertThrows(BadRequestException.class, () -> {
            bidService.updateOccupationBid(2L, req);
        });
    }

    @Test
    void testUpdateOccupationBid_WhenDormitoryNotFound_ShouldThrowBadRequestException() {
        OccupationRequest req = new OccupationRequest();
        req.setUniversityId(1);
        req.setDormitoryId(999);
        req.setText("Test");
        req.setAttachmentKeys(new ArrayList<>());

        University otherUniversity = new University();
        otherUniversity.setId(1);
        otherUniversity.setDormitories(new ArrayList<>());

        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(bidRepository.findById(2L)).thenReturn(Optional.of(occupationBid));
        when(universityRepository.findById(1)).thenReturn(Optional.of(otherUniversity));

        assertThrows(BadRequestException.class, () -> {
            bidService.updateOccupationBid(2L, req);
        });
    }

    @Test
    void testUpdateEvictionBid_WhenBidNotFound_ShouldThrowNotFoundException() {
        EvictionRequest req = new EvictionRequest();
        when(bidRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            bidService.updateEvictionBid(1L, req);
        });
    }

    @Test
    void testUpdateEvictionBid_WhenWrongType_ShouldThrowBadRequestException() {
        EvictionRequest req = new EvictionRequest();
        occupationBid.setType(Bid.Type.OCCUPATION);
        when(bidRepository.findById(2L)).thenReturn(Optional.of(occupationBid));
        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);

        assertThrows(BadRequestException.class, () -> {
            bidService.updateEvictionBid(2L, req);
        });
    }

    @Test
    void testUpdateDepartureBid_WhenBidNotFound_ShouldThrowNotFoundException() {
        DepartureRequest req = new DepartureRequest();
        when(bidRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            bidService.updateDepartureBid(3L, req);
        });
    }

    @Test
    void testUpdateDepartureBid_WhenWrongType_ShouldThrowBadRequestException() {
        DepartureRequest req = new DepartureRequest();
        bid.setType(Bid.Type.EVICTION);
        when(bidRepository.findById(1L)).thenReturn(Optional.of(bid));
        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);

        assertThrows(BadRequestException.class, () -> {
            bidService.updateDepartureBid(1L, req);
        });
    }

    @Test
    void testUpdateRoomChangeBid_WhenBidNotFound_ShouldThrowNotFoundException() {
        RoomChangeRequest req = new RoomChangeRequest();
        when(bidRepository.findById(4L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            bidService.updateRoomChangeBid(4L, req);
        });
    }

    @Test
    void testUpdateRoomChangeBid_WhenWrongType_ShouldThrowBadRequestException() {
        RoomChangeRequest req = new RoomChangeRequest();
        bid.setType(Bid.Type.EVICTION);
        when(bidRepository.findById(1L)).thenReturn(Optional.of(bid));
        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);

        assertThrows(BadRequestException.class, () -> {
            bidService.updateRoomChangeBid(1L, req);
        });
    }

    @Test
    void testUpdateRoomChangeBid_WhenRoomNotFound_ShouldThrowBadRequestException() {
        RoomChangeRequest req = new RoomChangeRequest();
        req.setText("Test");
        req.setRoomToId(999);
        req.setAttachmentKeys(new ArrayList<>());

        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(bidRepository.findById(4L)).thenReturn(Optional.of(roomChangeBid));
        when(roomRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> {
            bidService.updateRoomChangeBid(4L, req);
        });
    }

    @Test
    void testCreateRoomChangeBid_WhenRoomNotFound_ShouldThrowBadRequestException() {
        RoomChangeRequest req = new RoomChangeRequest();
        req.setText("Test");
        req.setRoomToId(999);
        req.setAttachmentKeys(new ArrayList<>());

        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(bidRepository.existsBySenderLoginAndTypeAndStatusIn(
                eq("user1"), eq(Bid.Type.ROOM_CHANGE), anyList())).thenReturn(false);
        when(roomRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> {
            bidService.createRoomChangeBid(req);
        });
    }

    @Test
    void testAcceptBid_WithOccupationBid_WhenNoBlockRooms_ShouldUseAisleRooms() {
        Room aisleRoom = new Room();
        aisleRoom.setId(2);
        aisleRoom.setType(Room.Type.AISLE);
        aisleRoom.setCapacity(2);
        aisleRoom.setResidents(new ArrayList<>());
        aisleRoom.setDormitory(dormitory);

        when(bidRepository.findById(2L)).thenReturn(Optional.of(occupationBid));
        when(userService.getCurrentUserOrThrow()).thenReturn(manager);
        when(roomRepository.getByTypeAndDormitoryId(Room.Type.BLOCK, 1))
                .thenReturn(new ArrayList<>());
        when(roomRepository.getByTypeAndDormitoryId(Room.Type.AISLE, 1))
                .thenReturn(List.of(aisleRoom));
        when(roomService.isRoomFree(any())).thenReturn(true);
        doNothing().when(residentRepository).userIsResidentNow(anyString(), anyInt(), anyInt());
        when(userRepository.save(any(User.class))).thenReturn(currentUser);
        when(eventRepository.save(any(Event.class))).thenReturn(new Event());

        bidService.acceptBid(2L);

        assertEquals(Bid.Status.ACCEPTED, occupationBid.getStatus());
        verify(residentRepository).userIsResidentNow(anyString(), anyInt(), eq(2));
    }

    @Test
    void testAcceptBid_WithRoomChangeBid_WhenRoomToIsNull_ShouldUsePreferType() {
        resident.setRoom(room);
        Room newRoom = new Room();
        newRoom.setId(2);
        newRoom.setType(Room.Type.BLOCK);
        newRoom.setCapacity(2);
        newRoom.setResidents(new ArrayList<>());
        newRoom.setDormitory(dormitory);

        roomChangeBid.setRoomTo(null);
        roomChangeBid.setRoomPreferType(Room.Type.BLOCK);

        when(bidRepository.findById(4L)).thenReturn(Optional.of(roomChangeBid));
        when(userService.getCurrentUserOrThrow()).thenReturn(manager);
        when(userService.getResidentByLogin("user1")).thenReturn(resident);
        when(roomRepository.getByTypeAndDormitoryId(Room.Type.BLOCK, 1))
                .thenReturn(List.of(newRoom));
        when(roomService.isRoomFree(any())).thenReturn(true);
        when(residentRepository.save(any(Resident.class))).thenReturn(resident);
        when(eventRepository.save(any(Event.class))).thenReturn(new Event());

        bidService.acceptBid(4L);

        assertEquals(Bid.Status.ACCEPTED, roomChangeBid.getStatus());
        verify(residentRepository).save(resident);
    }

    @Test
    void testAcceptBid_WithRoomChangeBid_WhenRoomToIsNullAndNoFreeRoom_ShouldThrowBadRequestException() {
        resident.setRoom(room);
        roomChangeBid.setRoomTo(null);
        roomChangeBid.setRoomPreferType(Room.Type.BLOCK);

        when(bidRepository.findById(4L)).thenReturn(Optional.of(roomChangeBid));
        when(userService.getCurrentUserOrThrow()).thenReturn(manager);
        when(userService.getResidentByLogin("user1")).thenReturn(resident);
        when(roomRepository.getByTypeAndDormitoryId(Room.Type.BLOCK, 1))
                .thenReturn(new ArrayList<>());

        assertThrows(BadRequestException.class, () -> {
            bidService.acceptBid(4L);
        });
    }

    @Test
    void testUpdateBidFiles_WhenBidFilesIsNull_ShouldNotThrowException() {
        bid.setFiles(null);
        EvictionRequest req = new EvictionRequest();
        req.setText("Test");
        req.setAttachmentKeys(List.of("key1"));

        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(bidRepository.existsBySenderLoginAndTypeAndStatusIn(
                eq("user1"), eq(Bid.Type.EVICTION), anyList())).thenReturn(false);
        when(bidFileRepository.getByKeyIn(List.of("key1"))).thenReturn(List.of(new BidFile()));

        assertDoesNotThrow(() -> {
            bidService.createEvictionBid(req);
        });
    }

    @Test
    void testCreateEvictionBid_WhenBidExists_ShouldThrowBadRequestException() {
        EvictionRequest req = new EvictionRequest();
        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(bidRepository.existsBySenderLoginAndTypeAndStatusIn(
                eq("user1"), eq(Bid.Type.EVICTION), anyList())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> {
            bidService.createEvictionBid(req);
        });
    }

    @Test
    void testCreateDepartureBid_WhenBidExists_ShouldThrowBadRequestException() {
        DepartureRequest req = new DepartureRequest();
        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(bidRepository.existsBySenderLoginAndTypeAndStatusIn(
                eq("user1"), eq(Bid.Type.DEPARTURE), anyList())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> {
            bidService.createDepartureBid(req);
        });
    }

    @Test
    void testCreateRoomChangeBid_WhenBidExists_ShouldThrowBadRequestException() {
        RoomChangeRequest req = new RoomChangeRequest();
        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(bidRepository.existsBySenderLoginAndTypeAndStatusIn(
                eq("user1"), eq(Bid.Type.ROOM_CHANGE), anyList())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> {
            bidService.createRoomChangeBid(req);
        });
    }

    @Test
    void testUpdateEvictionBid_ShouldUpdateBid() {
        EvictionRequest req = new EvictionRequest();
        req.setText("Updated");
        req.setAttachmentKeys(new ArrayList<>());

        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(bidRepository.findById(1L)).thenReturn(Optional.of(bid));
        when(bidFileRepository.getByKeyIn(anyList())).thenReturn(new ArrayList<>());

        bidService.updateEvictionBid(1L, req);

        verify(bidRepository).save(bid);
        assertEquals("Updated", bid.getText());
    }

    @Test
    void testUpdateDepartureBid_ShouldUpdateBid() {
        DepartureRequest req = new DepartureRequest();
        req.setText("Updated");
        req.setDayFrom(LocalDate.now().plusDays(1));
        req.setDayTo(LocalDate.now().plusDays(10));
        req.setAttachmentKeys(new ArrayList<>());

        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(bidRepository.findById(3L)).thenReturn(Optional.of(departureBid));
        when(bidFileRepository.getByKeyIn(anyList())).thenReturn(new ArrayList<>());

        bidService.updateDepartureBid(3L, req);

        verify(bidRepository).save(departureBid);
        assertEquals("Updated", departureBid.getText());
    }

    @Test
    void testUpdateRoomChangeBid_ShouldUpdateBid() {
        RoomChangeRequest req = new RoomChangeRequest();
        req.setText("Updated");
        req.setRoomPreferType(RoomType.AISLE);
        req.setAttachmentKeys(new ArrayList<>());

        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(bidRepository.findById(4L)).thenReturn(Optional.of(roomChangeBid));
        when(roomMapper.mapRoomTypeToModel(RoomType.AISLE)).thenReturn(Room.Type.AISLE);
        when(bidFileRepository.getByKeyIn(anyList())).thenReturn(new ArrayList<>());

        bidService.updateRoomChangeBid(4L, req);

        verify(bidRepository).save(roomChangeBid);
        assertEquals("Updated", roomChangeBid.getText());
    }

    @Test
    void testAcceptBid_WithOccupationBid_WhenBlockRoomsNotFree_ShouldTryAisleRooms() {
        Room occupiedBlockRoom = new Room();
        occupiedBlockRoom.setId(1);
        occupiedBlockRoom.setType(Room.Type.BLOCK);
        occupiedBlockRoom.setResidents(List.of(new Resident()));

        Room freeAisleRoom = new Room();
        freeAisleRoom.setId(2);
        freeAisleRoom.setType(Room.Type.AISLE);
        freeAisleRoom.setCapacity(2);
        freeAisleRoom.setResidents(new ArrayList<>());
        freeAisleRoom.setDormitory(dormitory);

        when(bidRepository.findById(2L)).thenReturn(Optional.of(occupationBid));
        when(userService.getCurrentUserOrThrow()).thenReturn(manager);
        when(roomRepository.getByTypeAndDormitoryId(Room.Type.BLOCK, 1))
                .thenReturn(List.of(occupiedBlockRoom));
        when(roomService.isRoomFree(occupiedBlockRoom)).thenReturn(false);
        when(roomRepository.getByTypeAndDormitoryId(Room.Type.AISLE, 1))
                .thenReturn(List.of(freeAisleRoom));
        when(roomService.isRoomFree(freeAisleRoom)).thenReturn(true);
        doNothing().when(residentRepository).userIsResidentNow(anyString(), anyInt(), anyInt());
        when(userRepository.save(any(User.class))).thenReturn(currentUser);
        when(eventRepository.save(any(Event.class))).thenReturn(new Event());

        bidService.acceptBid(2L);

        assertEquals(Bid.Status.ACCEPTED, occupationBid.getStatus());
        verify(residentRepository).userIsResidentNow(anyString(), anyInt(), eq(2));
    }

    @Test
    void testUpdateBidFiles_WhenBidHasExistingFiles_ShouldUpdateFiles() {
        BidFile existingFile1 = new BidFile();
        existingFile1.setKey("oldKey1");
        existingFile1.setBid(bid);
        BidFile existingFile2 = new BidFile();
        existingFile2.setKey("oldKey2");
        existingFile2.setBid(bid);
        bid.setFiles(List.of(existingFile1, existingFile2));

        BidFile newFile = new BidFile();
        newFile.setKey("newKey");
        newFile.setBid(null);

        EvictionRequest req = new EvictionRequest();
        req.setText("Updated");
        req.setAttachmentKeys(List.of("newKey"));

        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(bidRepository.findById(1L)).thenReturn(Optional.of(bid));
        when(bidFileRepository.getByKeyIn(List.of("newKey"))).thenReturn(List.of(newFile));

        bidService.updateEvictionBid(1L, req);

        // Note: saveEvictionBid replaces bid.getFiles() before calling updateBidFiles,
        // so old files are not detached, but new files are attached
        // Verify new file is attached (detached and re-attached by updateBidFiles)
        verify(bidFileRepository, atLeastOnce()).save(any(BidFile.class));
        // Verify new file is attached to the bid
        assertEquals(bid, newFile.getBid());
    }

    @Test
    void testSaveRoomChangeBid_WhenRoomToIdIsNull_ShouldSetRoomToNull() {
        RoomChangeRequest req = new RoomChangeRequest();
        req.setText("Test");
        req.setRoomToId(null);
        req.setRoomPreferType(RoomType.BLOCK);
        req.setAttachmentKeys(new ArrayList<>());

        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(bidRepository.existsBySenderLoginAndTypeAndStatusIn(
                eq("user1"), eq(Bid.Type.ROOM_CHANGE), anyList())).thenReturn(false);
        when(roomMapper.mapRoomTypeToModel(RoomType.BLOCK)).thenReturn(Room.Type.BLOCK);
        when(bidFileRepository.getByKeyIn(anyList())).thenReturn(new ArrayList<>());

        bidService.createRoomChangeBid(req);

        verify(bidRepository).save(argThat(bid -> {
            if (bid instanceof RoomChangeBid rcb) {
                return rcb.getRoomTo() == null && rcb.getRoomPreferType() == Room.Type.BLOCK;
            }
            return false;
        }));
    }

    @Test
    void testUpdateRoomChangeBid_WhenRoomToIdIsNull_ShouldSetRoomToNull() {
        RoomChangeRequest req = new RoomChangeRequest();
        req.setText("Updated");
        req.setRoomToId(null);
        req.setRoomPreferType(RoomType.AISLE);
        req.setAttachmentKeys(new ArrayList<>());

        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(bidRepository.findById(4L)).thenReturn(Optional.of(roomChangeBid));
        when(roomMapper.mapRoomTypeToModel(RoomType.AISLE)).thenReturn(Room.Type.AISLE);
        when(bidFileRepository.getByKeyIn(anyList())).thenReturn(new ArrayList<>());

        bidService.updateRoomChangeBid(4L, req);

        assertNull(roomChangeBid.getRoomTo());
        assertEquals(Room.Type.AISLE, roomChangeBid.getRoomPreferType());
        verify(bidRepository).save(roomChangeBid);
    }

    @Test
    void testEvictResident_WhenNoOpenBids_ShouldNotCallDenyBid() {
        when(userService.getResidentByLogin("user1")).thenReturn(resident);
        when(bidRepository.getBySenderLoginAndStatusIn(eq("user1"), anyList()))
                .thenReturn(new ArrayList<>()); // No open bids
        doNothing().when(residentRepository).userIsNotResidentAnyMore("user1");
        when(userRepository.save(any(User.class))).thenReturn(resident);
        when(eventRepository.save(any(Event.class))).thenReturn(new Event());

        bidService.evictResident("user1");

        assertEquals(User.Role.NON_RESIDENT, resident.getRole());
        verify(userRepository).save(resident);
        verify(eventRepository).save(any(Event.class));
        verify(residentRepository).userIsNotResidentAnyMore("user1");
        // Verify denyBid is never called (no open bids to deny)
        verify(bidRepository, never()).findById(anyLong());
    }

    @Test
    void testUpdateBidFiles_WhenNoNewAttachments_ShouldClearFiles() {
        BidFile existingFile = new BidFile();
        existingFile.setKey("oldKey");
        existingFile.setBid(bid);
        bid.setFiles(List.of(existingFile));

        EvictionRequest req = new EvictionRequest();
        req.setText("Updated");
        req.setAttachmentKeys(new ArrayList<>()); // No new attachments

        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(bidRepository.findById(1L)).thenReturn(Optional.of(bid));
        when(bidFileRepository.getByKeyIn(anyList())).thenReturn(new ArrayList<>());

        bidService.updateEvictionBid(1L, req);

        // Note: saveEvictionBid replaces bid.getFiles() with empty list before updateBidFiles
        // updateBidFiles won't save anything because bid.getFiles() is empty (forEach doesn't execute)
        // Verify bid files are cleared
        assertTrue(bid.getFiles().isEmpty());
        verify(bidRepository).save(bid);
    }

    @Test
    void testUpdateBidFiles_WhenNoOldFilesAndNewAttachments_ShouldOnlyAttachNewFiles() {
        bid.setFiles(new ArrayList<>()); // Empty list, not null

        BidFile newFile1 = new BidFile();
        newFile1.setKey("newKey1");
        newFile1.setBid(null);
        BidFile newFile2 = new BidFile();
        newFile2.setKey("newKey2");
        newFile2.setBid(null);

        EvictionRequest req = new EvictionRequest();
        req.setText("Updated");
        req.setAttachmentKeys(List.of("newKey1", "newKey2"));

        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(bidRepository.findById(1L)).thenReturn(Optional.of(bid));
        when(bidFileRepository.getByKeyIn(List.of("newKey1", "newKey2")))
                .thenReturn(List.of(newFile1, newFile2));

        bidService.updateEvictionBid(1L, req);

        // Note: saveEvictionBid sets bid.getFiles() to new files, then updateBidFiles detaches and re-attaches them
        // So each file is saved twice (detached + attached) = 4 saves total for 2 files
        verify(bidFileRepository, times(4)).save(any(BidFile.class));
        // Verify new files have bid set
        assertEquals(bid, newFile1.getBid());
        assertEquals(bid, newFile2.getBid());
    }

    @Test
    void testUpdateBidFiles_WhenRemovingAllFiles_ShouldClearFiles() {
        BidFile existingFile1 = new BidFile();
        existingFile1.setKey("oldKey1");
        existingFile1.setBid(bid);
        BidFile existingFile2 = new BidFile();
        existingFile2.setKey("oldKey2");
        existingFile2.setBid(bid);
        bid.setFiles(List.of(existingFile1, existingFile2));

        EvictionRequest req = new EvictionRequest();
        req.setText("Updated");
        req.setAttachmentKeys(new ArrayList<>()); // Remove all files

        when(userService.getCurrentUserOrThrow()).thenReturn(currentUser);
        when(bidRepository.findById(1L)).thenReturn(Optional.of(bid));
        when(bidFileRepository.getByKeyIn(anyList())).thenReturn(new ArrayList<>());

        bidService.updateEvictionBid(1L, req);

        // Note: saveEvictionBid replaces bid.getFiles() with empty list before updateBidFiles
        // updateBidFiles won't save anything because bid.getFiles() is empty (forEach doesn't execute)
        // Verify bid files are cleared
        assertTrue(bid.getFiles().isEmpty());
        verify(bidRepository).save(bid);
    }
}

