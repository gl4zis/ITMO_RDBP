package ru.itmo.is.mapper;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.itmo.is.dto.*;
import ru.itmo.is.entity.bid.*;
import ru.itmo.is.entity.dorm.Room;

@Component
@RequiredArgsConstructor
public class BidMapper {
    private final UserMapper userMapper;
    private final RoomMapper roomMapper;

    public BidResponse mapBidToDto(Bid bid) {
        return switch (bid.getType()) {
            case DEPARTURE -> mapDepartureBidToDto((DepartureBid) bid);
            case OCCUPATION -> mapOccupationBidToDto((OccupationBid) bid);
            case ROOM_CHANGE -> mapRoomChangeBidToDto((RoomChangeBid) bid);
            case EVICTION -> mapEvictionBidToDto(bid);
        };
    }

    public BidType mapBidTypeToDto(Bid.Type type) {
        return switch (type) {
            case DEPARTURE -> BidType.DEPARTURE;
            case OCCUPATION -> BidType.OCCUPATION;
            case ROOM_CHANGE -> BidType.ROOM_CHANGE;
            case EVICTION -> BidType.EVICTION;
        };
    }

    private DepartureResponse mapDepartureBidToDto(DepartureBid bid) {
        var response = new DepartureResponse();
        doDefaultMappings(response, bid);

        response.setDayFrom(bid.getDayFrom());
        response.setDayTo(bid.getDayTo());
        return response;
    }

    private OccupationResponse mapOccupationBidToDto(OccupationBid bid) {
        var response = new OccupationResponse();
        doDefaultMappings(response, bid);

        response.setUniversityId(bid.getUniversity().getId());
        response.setDormitoryId(bid.getDormitory().getId());
        return response;
    }

    private RoomChangeResponse mapRoomChangeBidToDto(RoomChangeBid bid) {
        var response = new RoomChangeResponse();
        doDefaultMappings(response, bid);

        @Nullable Room roomTo = bid.getRoomTo();
        response.setRoomToId(roomTo == null ? null : roomTo.getId());
        response.setRoomPreferType(roomMapper.mapRoomTypeToDto(bid.getRoomPreferType()));
        return response;
    }

    private EvictionResponse mapEvictionBidToDto(Bid bid) {
        var response = new EvictionResponse();
        doDefaultMappings(response, bid);
        return response;
    }

    private void doDefaultMappings(BidResponse response, Bid bid) {
        response.setNumber(bid.getId());
        response.setSender(userMapper.mapUserResponse(bid.getSender()));
        response.setText(bid.getText());
        response.setType(mapBidTypeToDto(bid.getType()));
        response.setAttachments(bid.getFiles().stream().map(this::mapAttachmentToDto).toList());
        response.setStatus(mapBidStatusToDto(bid.getStatus()));
        response.setComment(bid.getComment());
        if (bid.getManager() != null) {
            response.setManager(userMapper.mapUserResponse(bid.getManager()));
        }
    }

    private BidResponseAttachmentsInner mapAttachmentToDto(BidFile file) {
        return new BidResponseAttachmentsInner(file.getName(), file.getKey());
    }

    private BidStatus mapBidStatusToDto(Bid.Status status) {
        return switch (status) {
            case IN_PROCESS -> BidStatus.IN_PROCESS;
            case PENDING_REVISION -> BidStatus.PENDING_REVISION;
            case ACCEPTED -> BidStatus.ACCEPTED;
            case DENIED -> BidStatus.DENIED;
        };
    }
}
