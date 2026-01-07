package ru.itmo.is.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.is.dto.*;
import ru.itmo.is.entity.bid.*;
import ru.itmo.is.entity.dorm.Dormitory;
import ru.itmo.is.entity.dorm.Room;
import ru.itmo.is.entity.dorm.University;
import ru.itmo.is.entity.user.User;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BidMapperTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private RoomMapper roomMapper;
    @InjectMocks
    private BidMapper bidMapper;

    private Bid bid;
    private User sender;
    private User manager;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        sender = new User();
        sender.setLogin("sender1");

        manager = new User();
        manager.setLogin("manager1");

        BidFile bidFile = new BidFile();
        bidFile.setKey("file1");
        bidFile.setName("test.pdf");

        bid = new Bid();
        bid.setId(1L);
        bid.setType(Bid.Type.EVICTION);
        bid.setStatus(Bid.Status.IN_PROCESS);
        bid.setText("Test text");
        bid.setSender(sender);
        bid.setManager(manager);
        bid.setComment("Test comment");
        bid.setFiles(new ArrayList<>());
        bid.getFiles().add(bidFile);

        userResponse = new UserResponse();
        userResponse.setLogin("sender1");
    }

    @Test
    void testBidTypes_ShouldHaveEqualSize() {
        assertEquals(BidType.values().length, Bid.Type.values().length);
    }

    @Test
    void testBidStatuses_ShouldHaveEqualSize() {
        assertEquals(BidStatus.values().length, Bid.Status.values().length);
    }

    @Test
    void testMapBidTypeToDto_ShouldMapAllTypes() {
        for (var type : Bid.Type.values()) {
            var dtoType = BidType.fromValue(type.name());
            assertEquals(dtoType, bidMapper.mapBidTypeToDto(type));
        }
    }

    @Test
    void testMapBidToDto_WithEvictionBid_ShouldMapCorrectly() {
        when(userMapper.mapUserResponse(sender)).thenReturn(userResponse);
        when(userMapper.mapUserResponse(manager)).thenReturn(userResponse);

        BidResponse result = bidMapper.mapBidToDto(bid);

        assertNotNull(result);
        assertEquals(1L, result.getNumber());
        assertEquals("Test text", result.getText());
        assertEquals(BidType.EVICTION, result.getType());
        assertEquals(BidStatus.IN_PROCESS, result.getStatus());
        assertEquals("Test comment", result.getComment());
        assertEquals(1, result.getAttachments().size());
        verify(userMapper, times(2)).mapUserResponse(any(User.class));
    }

    @Test
    void testMapBidToDto_WithDepartureBid_ShouldMapCorrectly() {
        DepartureBid departureBid = buildDepartureBid();

        when(userMapper.mapUserResponse(sender)).thenReturn(userResponse);

        BidResponse result = bidMapper.mapBidToDto(departureBid);

        assertNotNull(result);
        assertInstanceOf(DepartureResponse.class, result);
        DepartureResponse departureResponse = (DepartureResponse) result;
        assertEquals(departureBid.getDayFrom(), departureResponse.getDayFrom());
        assertEquals(departureBid.getDayTo(), departureResponse.getDayTo());
    }

    @Test
    void testMapBidToDto_WithOccupationBid_ShouldMapCorrectly() {
        OccupationBid occupationBid = buildOccupationBid();

        when(userMapper.mapUserResponse(sender)).thenReturn(userResponse);

        BidResponse result = bidMapper.mapBidToDto(occupationBid);

        assertNotNull(result);
        assertInstanceOf(OccupationResponse.class, result);
        OccupationResponse occupationResponse = (OccupationResponse) result;
        assertEquals(1, occupationResponse.getUniversityId());
        assertEquals(1, occupationResponse.getDormitoryId());
    }

    @Test
    void testMapBidToDto_WithRoomChangeBid_ShouldMapCorrectly() {
        RoomChangeBid roomChangeBid = buildRoomChangeBid();

        when(userMapper.mapUserResponse(sender)).thenReturn(userResponse);
        when(roomMapper.mapRoomTypeToDto(Room.Type.BLOCK)).thenReturn(RoomType.BLOCK);

        BidResponse result = bidMapper.mapBidToDto(roomChangeBid);

        assertNotNull(result);
        assertInstanceOf(RoomChangeResponse.class, result);
        RoomChangeResponse roomChangeResponse = (RoomChangeResponse) result;
        assertEquals(1, roomChangeResponse.getRoomToId());
        assertEquals(RoomType.BLOCK, roomChangeResponse.getRoomPreferType());
    }

    @Test
    void testMapBidToDto_WithNullManager_ShouldNotMapManager() {
        bid.setManager(null);
        when(userMapper.mapUserResponse(sender)).thenReturn(userResponse);

        BidResponse result = bidMapper.mapBidToDto(bid);

        assertNotNull(result);
        assertNull(result.getManager());
    }

    private DepartureBid buildDepartureBid() {
        DepartureBid departureBid = new DepartureBid();
        departureBid.setId(1L);
        departureBid.setType(Bid.Type.DEPARTURE);
        departureBid.setStatus(Bid.Status.IN_PROCESS);
        departureBid.setText("Departure text");
        departureBid.setSender(sender);
        departureBid.setDayFrom(LocalDate.now().plusDays(1));
        departureBid.setDayTo(LocalDate.now().plusDays(10));
        departureBid.setFiles(new ArrayList<>());
        return departureBid;
    }

    private OccupationBid buildOccupationBid() {
        University university = new University();
        university.setId(1);
        Dormitory dormitory = new Dormitory();
        dormitory.setId(1);

        OccupationBid occupationBid = new OccupationBid();
        occupationBid.setId(1L);
        occupationBid.setType(Bid.Type.OCCUPATION);
        occupationBid.setStatus(Bid.Status.IN_PROCESS);
        occupationBid.setText("Occupation text");
        occupationBid.setSender(sender);
        occupationBid.setUniversity(university);
        occupationBid.setDormitory(dormitory);
        occupationBid.setFiles(new ArrayList<>());
        return occupationBid;
    }

    private RoomChangeBid buildRoomChangeBid() {
        Room room = new Room();
        room.setId(1);

        RoomChangeBid roomChangeBid = new RoomChangeBid();
        roomChangeBid.setId(1L);
        roomChangeBid.setType(Bid.Type.ROOM_CHANGE);
        roomChangeBid.setStatus(Bid.Status.IN_PROCESS);
        roomChangeBid.setText("Room change text");
        roomChangeBid.setSender(sender);
        roomChangeBid.setRoomTo(room);
        roomChangeBid.setRoomPreferType(Room.Type.BLOCK);
        roomChangeBid.setFiles(new ArrayList<>());
        return roomChangeBid;
    }
}
