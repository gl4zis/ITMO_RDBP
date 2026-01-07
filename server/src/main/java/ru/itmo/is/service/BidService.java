package ru.itmo.is.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.is.dto.*;
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
import ru.itmo.is.mapper.BidMapper;
import ru.itmo.is.mapper.RoomMapper;
import ru.itmo.is.repository.*;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BidService {
    private final UserService userService;
    private final NotificationService notificationService;
    private final UniversityRepository universityRepository;
    private final RoomRepository roomRepository;
    private final BidRepository bidRepository;
    private final BidFileRepository bidFileRepository;
    private final EventRepository eventRepository;
    private final ResidentRepository residentRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final BidComparator bidComparator;
    private final BidMapper bidMapper;
    private final RoomMapper roomMapper;
    private final RoomService roomService;

    public List<BidType> getSelfOpenedBidTypes() {
        return bidRepository.getOpenedBidTypes(userService.getCurrentUserOrThrow().getLogin())
                .stream()
                .map(bidMapper::mapBidTypeToDto)
                .toList();
    }

    public List<BidResponse> getInProcessBids() {
        List<Bid> bids = bidRepository.getByStatusIn(List.of(Bid.Status.IN_PROCESS));
        return bids.stream()
                .sorted(bidComparator::compare)
                .map(bidMapper::mapBidToDto)
                .toList();
    }

    public List<BidResponse> getPendingBids() {
        List<Bid> bids = bidRepository.getByStatusIn(List.of(Bid.Status.PENDING_REVISION));
        return bids.stream().map(bidMapper::mapBidToDto).toList();
    }

    public List<BidResponse> getArchivedBids() {
        List<Bid> bids = bidRepository.getByStatusIn(List.of(Bid.Status.ACCEPTED, Bid.Status.DENIED));
        return bids.stream().map(bidMapper::mapBidToDto).toList();
    }

    public List<BidResponse> getSelfBids() {
        User sender = userService.getCurrentUserOrThrow();
        List<Bid> bids = bidRepository.getBySenderLoginOrderByIdDesc(sender.getLogin());
        return bids.stream().map(bidMapper::mapBidToDto).toList();
    }

    public BidResponse getBid(long id) {
        Optional<Bid> bidO = bidRepository.findById(id);
        if (bidO.isEmpty()) {
            throw new NotFoundException("No bid with such id");
        }

        User user = userService.getCurrentUserOrThrow();
        if (user.getRole() == User.Role.MANAGER || bidO.get().getSender().equals(user)) {
            return bidMapper.mapBidToDto(bidO.get());
        }
        throw new ForbiddenException("You are not allowed to get bid by this user");
    }

    @Transactional
    public void denyBid(Long id, String comment) {
        Bid bid = bidRepository.findById(id)
                .filter(b -> b.getStatus() == Bid.Status.IN_PROCESS)
                .orElseThrow(() -> new NotFoundException("No such bid"));
        bid.setStatus(Bid.Status.DENIED);
        bid.setManager(userService.getCurrentUserOrThrow());
        bid.setComment(comment);
        bidRepository.save(bid);
        notificationService.notifySenderAboutBidStatus(bid);
    }

    public void acceptBid(Long id) {
        Bid bid = bidRepository.findById(id)
                .filter(b -> b.getStatus() == Bid.Status.IN_PROCESS)
                .orElseThrow(() -> new NotFoundException("No such bid"));

        bid.setStatus(Bid.Status.ACCEPTED);
        bid.setManager(userService.getCurrentUserOrThrow());
        switch (bid.getType()) {
            case OCCUPATION -> acceptOccupationBid((OccupationBid) bid);
            case EVICTION -> acceptEvictionBid(bid);
            case DEPARTURE -> acceptDepartureBid((DepartureBid) bid);
            case ROOM_CHANGE -> acceptRoomChangeBid((RoomChangeBid) bid);
        }
        bidRepository.save(bid);
        notificationService.notifySenderAboutBidStatus(bid);
    }

    @Transactional
    public void pendBid(long id, String comment) {
        Bid bid = bidRepository.findById(id)
                .filter(b -> b.getStatus() == Bid.Status.IN_PROCESS)
                .orElseThrow(() -> new NotFoundException("No such bid"));
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

    public void createOccupationBid(OccupationRequest req) {
        checkUserBidIsNotExists(Bid.Type.OCCUPATION);
        saveOccupationBid(new OccupationBid(), req);
    }

    public void updateOccupationBid(long bidId, OccupationRequest req) {
        checkEditableBid(bidId, Bid.Type.OCCUPATION);
        var bid = bidRepository.findById(bidId)
                .filter(occBid -> occBid.getType() == Bid.Type.OCCUPATION)
                .map(occBid -> (OccupationBid) occBid)
                .orElseThrow(() -> new BadRequestException("Occupation bid with such id doesn't exist"));
        saveOccupationBid(bid, req);
    }

    private void saveOccupationBid(OccupationBid bid, OccupationRequest req) {
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
        
        notificationService.notifyManagersAboutNewBid(bid);
    }

    public void createEvictionBid(EvictionRequest req) {
        checkUserBidIsNotExists(Bid.Type.EVICTION);
        saveEvictionBid(new Bid(), req);
    }

    public void updateEvictionBid(long bidId, EvictionRequest req) {
        checkEditableBid(bidId, Bid.Type.EVICTION);
        var bid = bidRepository.findById(bidId)
                .filter(eBid -> eBid.getType() == Bid.Type.EVICTION)
                .orElseThrow(() -> new BadRequestException("Eviction bid with such id doesn't exist"));
        saveEvictionBid(bid, req);
    }

    private void saveEvictionBid(Bid bid, EvictionRequest req) {
        bid.setStatus(Bid.Status.IN_PROCESS);
        bid.setFiles(bidFileRepository.getByKeyIn(req.getAttachmentKeys()));
        bid.setSender(userService.getCurrentUserOrThrow());
        bid.setText(req.getText());
        bid.setType(Bid.Type.EVICTION);
        bidRepository.save(bid);

        updateBidFiles(req.getAttachmentKeys(), bid);
        
        notificationService.notifyManagersAboutNewBid(bid);
    }

    public void createDepartureBid(DepartureRequest req) {
        checkUserBidIsNotExists(Bid.Type.DEPARTURE);
        saveDepartureBid(new DepartureBid(), req);
    }

    public void updateDepartureBid(long bidId, DepartureRequest req) {
        checkEditableBid(bidId, Bid.Type.DEPARTURE);
        var bid = bidRepository.findById(bidId)
                .filter(occBid -> occBid.getType() == Bid.Type.DEPARTURE)
                .map(dBid -> (DepartureBid) dBid)
                .orElseThrow(() -> new BadRequestException("Departure bid with such id doesn't exist"));
        saveDepartureBid(bid, req);
    }

    private void saveDepartureBid(DepartureBid bid, DepartureRequest req) {
        bid.setStatus(Bid.Status.IN_PROCESS);
        bid.setFiles(bidFileRepository.getByKeyIn(req.getAttachmentKeys()));
        bid.setSender(userService.getCurrentUserOrThrow());
        bid.setText(req.getText());
        bid.setDayFrom(req.getDayFrom());
        bid.setDayTo(req.getDayTo());
        bidRepository.save(bid);

        updateBidFiles(req.getAttachmentKeys(), bid);
        
        notificationService.notifyManagersAboutNewBid(bid);
    }

    public void createRoomChangeBid(RoomChangeRequest req) {
        checkUserBidIsNotExists(Bid.Type.ROOM_CHANGE);
        saveRoomChangeBid(new RoomChangeBid(), req);
    }

    public void updateRoomChangeBid(long bidId, RoomChangeRequest req) {
        checkEditableBid(bidId, Bid.Type.ROOM_CHANGE);
        var bid = bidRepository.findById(bidId)
                .filter(occBid -> occBid.getType() == Bid.Type.ROOM_CHANGE)
                .map(rcBid -> (RoomChangeBid) rcBid)
                .orElseThrow(() -> new BadRequestException("Room change bid with such id doesn't exist"));
        saveRoomChangeBid(bid, req);
    }

    private void saveRoomChangeBid(RoomChangeBid bid, RoomChangeRequest req) {
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
        bid.setRoomPreferType(roomMapper.mapRoomTypeToModel(req.getRoomPreferType()));
        bidRepository.save(bid);

        updateBidFiles(req.getAttachmentKeys(), bid);

        if (roomO.isPresent() && !roomService.isRoomFree(roomO.get())) {
            bid.setStatus(Bid.Status.DENIED);
            bid.setComment("Auto-denied: target room is full");
            bidRepository.save(bid);
            notificationService.notifySenderAboutBidStatus(bid);
        } else {
            notificationService.notifyManagersAboutNewBid(bid);
        }
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
                .filter(roomService::isRoomFree)
                .findFirst();
        if (roomO.isEmpty()) {
            List<Room> aisleRoomIds = roomRepository
                    .getByTypeAndDormitoryId(Room.Type.AISLE, bid.getDormitory().getId());
            roomO = aisleRoomIds.stream()
                    .filter(roomService::isRoomFree)
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
            if (!roomService.isRoomFree(room)) {
                throw new BadRequestException("Room is not free");
            }
        } else {
            List<Room> rooms = roomRepository
                    .getByTypeAndDormitoryId(bid.getRoomPreferType(), resident.getRoom().getDormitory().getId());
            Optional<Room> roomO = rooms.stream()
                    .filter(roomService::isRoomFree)
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
}
