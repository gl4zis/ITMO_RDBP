package ru.itmo.is.service;

import jakarta.annotation.Nullable;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.is.dto.request.bid.BidRequest;
import ru.itmo.is.dto.request.bid.DepartureRequest;
import ru.itmo.is.dto.request.bid.OccupationRequest;
import ru.itmo.is.dto.request.bid.RoomChangeRequest;
import ru.itmo.is.dto.response.bid.BidResponse;
import ru.itmo.is.dto.response.bid.DepartureBidResponse;
import ru.itmo.is.dto.response.bid.OccupationBidResponse;
import ru.itmo.is.dto.response.bid.RoomChangeResponse;
import ru.itmo.is.entity.Event;
import ru.itmo.is.entity.bid.Bid;
import ru.itmo.is.entity.bid.DepartureBid;
import ru.itmo.is.entity.bid.OccupationBid;
import ru.itmo.is.entity.bid.RoomChangeBid;
import ru.itmo.is.entity.dorm.Dormitory;
import ru.itmo.is.entity.dorm.Room;
import ru.itmo.is.entity.dorm.University;
import ru.itmo.is.entity.notification.Notification;
import ru.itmo.is.entity.user.Resident;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.exception.BadRequestException;
import ru.itmo.is.exception.ForbiddenException;
import ru.itmo.is.exception.NotFoundException;
import ru.itmo.is.repository.*;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BidService {
    private final UserService userService;
    private final UniversityRepository universityRepository;
    private final RoomRepository roomRepository;
    private final BidRepository bidRepository;
    private final BidFileRepository bidFileRepository;
    private final EventRepository eventRepository;
    private final ResidentRepository residentRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final BidComparator bidComparator;

    public List<Bid.Type> getSelfOpenedBidTypes() {
        return bidRepository.getOpenedBidTypes(userService.getCurrentUserOrThrow().getLogin());
    }

    public List<BidResponse> getInProcessBids() {
        List<Bid> bids = bidRepository.getByStatusIn(List.of(Bid.Status.IN_PROCESS));
        return bids.stream()
                .sorted(bidComparator::compare)
                .map(this::mapBid)
                .toList();
    }

    public List<BidResponse> getPendingBids() {
        List<Bid> bids = bidRepository.getByStatusIn(List.of(Bid.Status.PENDING_REVISION));
        return bids.stream().map(this::mapBid).toList();
    }

    public List<BidResponse> getArchivedBids() {
        List<Bid> bids = bidRepository.getByStatusIn(List.of(Bid.Status.ACCEPTED, Bid.Status.DENIED));
        return bids.stream().map(this::mapBid).toList();
    }

    public List<BidResponse> getSelfBids() {
        User sender = userService.getCurrentUserOrThrow();
        List<Bid> bids = bidRepository.getBySenderLoginOrderByIdDesc(sender.getLogin());
        return bids.stream().map(this::mapBid).toList();
    }

    public BidResponse getBid(long id) {
        Optional<Bid> bidO = bidRepository.findById(id);
        if (bidO.isEmpty()) {
            throw new NotFoundException("No bid with such id");
        }

        User user = userService.getCurrentUserOrThrow();
        if (user.getRole() == User.Role.MANAGER || bidO.get().getSender().equals(user)) {
            return mapBid(bidO.get());
        }
        throw new ForbiddenException("You are not allowed to get bid by this user");
    }

    public void denyBid(Long id, String comment) {
        Bid bid = bidRepository.findById(id)
                .filter(b -> b.getStatus() == Bid.Status.IN_PROCESS)
                .orElseThrow(() -> new BadRequestException("No such bid"));
        bid.setStatus(Bid.Status.DENIED);
        bid.setManager(userService.getCurrentUserOrThrow());
        bid.setComment(comment);
        bidRepository.save(bid);
    }

    @Transactional
    public void acceptBid(Long id) {
        Bid bid = bidRepository.findById(id)
                .filter(b -> b.getStatus() == Bid.Status.IN_PROCESS)
                .orElseThrow(() -> new BadRequestException("No such bid"));

        bid.setStatus(Bid.Status.ACCEPTED);
        bid.setManager(userService.getCurrentUserOrThrow());
        switch (bid.getType()) {
            case OCCUPATION -> acceptOccupationBid((OccupationBid) bid);
            case EVICTION -> acceptEvictionBid(bid);
            case DEPARTURE -> acceptDepartureBid((DepartureBid) bid);
            case ROOM_CHANGE -> acceptRoomChangeBid((RoomChangeBid) bid);
        }
        bidRepository.save(bid);
    }

    public void pendBid(long id, String comment) {
        Bid bid = bidRepository.findById(id)
                .filter(b -> b.getStatus() == Bid.Status.IN_PROCESS)
                .orElseThrow(() -> new BadRequestException("No such bid"));
        bid.setComment(comment);
        bid.setManager(userService.getCurrentUserOrThrow());
        bid.setStatus(Bid.Status.PENDING_REVISION);
        bidRepository.save(bid);

        Notification notification = new Notification();
        notification.setBid(bid);
        notification.setReceiver(bid.getSender());
        notification.setText("Вам нужно поправить/дополнить данные в заявке");
        notificationRepository.save(notification);
    }

    public void saveOccupationBid(@Nullable Long bidId, OccupationRequest req) {
        var bid = new OccupationBid();
        if (bidId == null) {
            checkUserBidIsNotExists(Bid.Type.OCCUPATION);
        } else {
            checkEditableBid(bidId, Bid.Type.OCCUPATION);
            bid = bidRepository.findById(bidId)
                    .filter(occBid -> occBid.getType() == Bid.Type.OCCUPATION)
                    .map(occBid -> (OccupationBid) occBid)
                    .orElseThrow(() -> new BadRequestException("Occupation bid with such id doesn't exist"));
        }

        Optional<University> universityO = universityRepository.findById(req.getUniversityId());
        if (universityO.isEmpty()) {
            throw new BadRequestException("No such university");
        }
        University university = universityO.get();
        Optional<Dormitory> dormitoryO = university.getDormitories().stream()
                .filter(dorm -> dorm.getId().equals(req.getDormitoryId())).findFirst();
        if (dormitoryO.isEmpty()) {
            throw new BadRequestException("No such dormitory or this dormitory is not linked with the university");
        }

        bid.setStatus(Bid.Status.IN_PROCESS);
        bid.setSender(userService.getCurrentUserOrThrow());
        bid.setText(req.getText());
        bid.setUniversity(university);
        bid.setDormitory(dormitoryO.get());
        bidRepository.save(bid);

        updateBidFiles(req.getAttachmentKeys(), bid);
    }

    public void saveEvictionBid(@Nullable Long bidId, BidRequest req) {
        var bid = new Bid();
        if (bidId == null) {
            checkUserBidIsNotExists(Bid.Type.EVICTION);
        } else {
            checkEditableBid(bidId, Bid.Type.EVICTION);
            bid = bidRepository.findById(bidId)
                    .filter(occBid -> occBid.getType() == Bid.Type.EVICTION)
                    .orElseThrow(() -> new BadRequestException("Eviction bid with such id doesn't exist"));
        }

        bid.setStatus(Bid.Status.IN_PROCESS);
        bid.setFiles(bidFileRepository.getByKeyIn(req.getAttachmentKeys()));
        bid.setSender(userService.getCurrentUserOrThrow());
        bid.setText(req.getText());
        bid.setType(Bid.Type.EVICTION);
        bidRepository.save(bid);

        updateBidFiles(req.getAttachmentKeys(), bid);
    }

    public void saveDepartureBid(@Nullable Long bidId, DepartureRequest req) {
        var bid = new DepartureBid();
        if (bidId == null) {
            checkUserBidIsNotExists(Bid.Type.DEPARTURE);
        } else {
            checkEditableBid(bidId, Bid.Type.DEPARTURE);
            bid = bidRepository.findById(bidId)
                    .filter(occBid -> occBid.getType() == Bid.Type.DEPARTURE)
                    .map(occBid -> (DepartureBid) occBid)
                    .orElseThrow(() -> new BadRequestException("Departure bid with such id doesn't exist"));
        }

        bid.setStatus(Bid.Status.IN_PROCESS);
        bid.setFiles(bidFileRepository.getByKeyIn(req.getAttachmentKeys()));
        bid.setSender(userService.getCurrentUserOrThrow());
        bid.setText(req.getText());
        bid.setDayFrom(req.getDayFrom());
        bid.setDayTo(req.getDayTo());
        bidRepository.save(bid);

        updateBidFiles(req.getAttachmentKeys(), bid);
    }

    public void saveRoomChangeBid(@Nullable Long bidId, RoomChangeRequest req) {
        var bid = new RoomChangeBid();
        if (bidId == null) {
            checkUserBidIsNotExists(Bid.Type.ROOM_CHANGE);
        } else {
            checkEditableBid(bidId, Bid.Type.ROOM_CHANGE);
            bid = bidRepository.findById(bidId)
                    .filter(occBid -> occBid.getType() == Bid.Type.ROOM_CHANGE)
                    .map(occBid -> (RoomChangeBid) occBid)
                    .orElseThrow(() -> new BadRequestException("Room change bid with such id doesn't exist"));
        }

        Optional<Room> roomO = Optional.empty();
        if (req.getRoomToId() != null) {
            roomO = roomRepository.findById(req.getRoomToId());
            if (roomO.isEmpty()) {
                throw new BadRequestException("No such room");
            }
        }

        bid.setStatus(Bid.Status.IN_PROCESS);
        bid.setFiles(bidFileRepository.getByKeyIn(req.getAttachmentKeys()));
        bid.setSender(userService.getCurrentUserOrThrow());
        bid.setText(req.getText());
        bid.setRoomTo(roomO.orElse(null));
        bid.setRoomPreferType(req.getRoomPreferType());
        bidRepository.save(bid);

        updateBidFiles(req.getAttachmentKeys(), bid);
    }

    public void evictResident(String login) {
        Resident nonResident = userService.getResidentByLogin(login);
        bidRepository.getBySenderLoginAndStatusIn(login, List.of(Bid.Status.IN_PROCESS, Bid.Status.PENDING_REVISION))
                .forEach(bid -> denyBid(bid.getId(), "Auto-denied by eviction"));

        residentRepository.userIsNotResidentAnyMore(nonResident.getLogin());
        nonResident.setRole(User.Role.NON_RESIDENT);
        userRepository.save(nonResident);

        var event = new Event();
        event.setType(Event.Type.EVICTION);
        event.setUsr(nonResident);
        eventRepository.save(event);
    }

    private void updateBidFiles(List<String> attachments, Bid bid) {
        if (bid.getFiles() != null) {
            bid.getFiles().forEach(bidFile -> {
                bidFile.setBid(null);
                bidFileRepository.save(bidFile);
            });
        }
        bidFileRepository.getByKeyIn(attachments).forEach(bidFile -> {
            bidFile.setBid(bid);
            bidFileRepository.save(bidFile);
        });
    }

    private void checkUserBidIsNotExists(Bid.Type type) {
        if (bidRepository.existsBySenderLoginAndTypeAndStatusIn(
                userService.getCurrentUserOrThrow().getLogin(),
                type,
                List.of(Bid.Status.IN_PROCESS, Bid.Status.PENDING_REVISION)
        )) {
            throw new BadRequestException("Not closed bid with this status already exists");
        }
    }

    private void checkEditableBid(long id, Bid.Type type) {
        Optional<Bid> bidO = bidRepository.findById(id);
        if (bidO.isEmpty()) {
            throw new NotFoundException("No such bid");
        }
        if (!userService.getCurrentUserOrThrow().equals(bidO.get().getSender())) {
            throw new ForbiddenException("You are not allowed to edit this bid");
        }
        if (!bidO.get().getType().equals(type)) {
            throw new BadRequestException("Invalid type of original bid");
        }
        if (!bidO.get().getStatus().isEditable()) {
            throw new BadRequestException("This bid is not editable");
        }
    }

    private void acceptOccupationBid(OccupationBid bid) {
        List<Room> blockRoomIds = roomRepository
                .getByTypeAndDormitoryId(Room.Type.BLOCK, bid.getDormitory().getId());
        Optional<Room> roomO = blockRoomIds.stream()
                .filter(r -> roomRepository.isRoomFree(r.getId()))
                .findFirst();
        if (roomO.isEmpty()) {
            List<Room> aisleRoomIds = roomRepository
                    .getByTypeAndDormitoryId(Room.Type.AISLE, bid.getDormitory().getId());
            roomO = aisleRoomIds.stream()
                    .filter(r -> roomRepository.isRoomFree(r.getId()))
                    .findFirst();
        }
        if (roomO.isEmpty()) {
            throw new BadRequestException("No free room");
        }

        User resident = bid.getSender();
        residentRepository.userIsResidentNow(
                resident.getLogin(),
                bid.getUniversity().getId(),
                roomO.get().getId()
        );
        resident.setRole(User.Role.RESIDENT);
        userRepository.save(resident);

        var event = new Event();
        event.setType(Event.Type.OCCUPATION);
        event.setRoom(roomO.get());
        event.setUsr(resident);
        eventRepository.save(event);
    }

    private void acceptEvictionBid(Bid bid) {
        evictResident(bid.getSender().getLogin());
    }

    private void acceptDepartureBid(DepartureBid bid) {
        // just save bid with ACCEPTED status
    }

    private void acceptRoomChangeBid(RoomChangeBid bid) {
        Resident resident = userService.getResidentByLogin(bid.getSender().getLogin());
        Room room;
        if (bid.getRoomTo() != null) {
            room = bid.getRoomTo();
            if (room.getResidents().size() == room.getCapacity()) {
                throw new BadRequestException("Room is not free");
            }
        } else {
            List<Room> rooms = roomRepository
                    .getByTypeAndDormitoryId(bid.getRoomPreferType(), resident.getRoom().getDormitory().getId());
            Optional<Room> roomO = rooms.stream()
                    .filter(r -> roomRepository.isRoomFree(r.getId()))
                    .findFirst();
            if (roomO.isEmpty()) {
                throw new BadRequestException("No free room");
            }
            room = roomO.get();
        }
        resident.setRoom(room);
        residentRepository.save(resident);

        var event = new Event();
        event.setType(Event.Type.ROOM_CHANGE);
        event.setRoom(room);
        event.setUsr(resident);
        eventRepository.save(event);
    }

    private BidResponse mapBid(Bid bid) {
        return switch (bid.getType()) {
            case DEPARTURE -> new DepartureBidResponse((DepartureBid) bid);
            case OCCUPATION -> new OccupationBidResponse((OccupationBid) bid);
            case ROOM_CHANGE -> new RoomChangeResponse((RoomChangeBid) bid);
            default -> new BidResponse(bid);
        };
    }
}
