package ru.itmo.is.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.is.api.BidApi;
import ru.itmo.is.dto.*;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.security.RolesAllowed;
import ru.itmo.is.service.BidService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BidController implements BidApi {
    private final BidService bidService;

    @Override
    @RolesAllowed({User.Role.NON_RESIDENT, User.Role.RESIDENT})
    public ResponseEntity<List<BidType>> getSelfOpenedBidTypes() {
        return ResponseEntity.ok(bidService.getSelfOpenedBidTypes());
    }

    @Override
    @RolesAllowed({User.Role.NON_RESIDENT, User.Role.RESIDENT})
    public ResponseEntity<List<BidResponse>> getSelfBids() {
        return ResponseEntity.ok(bidService.getSelfBids());
    }

    @Override
    @RolesAllowed({User.Role.NON_RESIDENT, User.Role.RESIDENT, User.Role.MANAGER})
    public ResponseEntity<BidResponse> getBid(Long id) {
        return ResponseEntity.ok(bidService.getBid(id));
    }

    @Override
    @RolesAllowed(User.Role.MANAGER)
    public ResponseEntity<List<BidResponse>> getInProcessBids() {
        return ResponseEntity.ok(bidService.getInProcessBids());
    }

    @Override
    @RolesAllowed(User.Role.MANAGER)
    public ResponseEntity<List<BidResponse>> getPendingBids() {
        return ResponseEntity.ok(bidService.getPendingBids());
    }

    @Override
    @RolesAllowed(User.Role.MANAGER)
    public ResponseEntity<List<BidResponse>> getArchivedBids() {
        return ResponseEntity.ok(bidService.getArchivedBids());
    }

    @Override
    @RolesAllowed(User.Role.NON_RESIDENT)
    public ResponseEntity<Void> createOccupationBid(OccupationRequest req) {
        bidService.createOccupationBid(req);
        return ResponseEntity.ok().build();
    }

    @Override
    @RolesAllowed(User.Role.NON_RESIDENT)
    public ResponseEntity<Void> updateOccupationBid(Long id, OccupationRequest req) {
        bidService.updateOccupationBid(id, req);
        return ResponseEntity.ok().build();
    }

    @Override
    @RolesAllowed(User.Role.RESIDENT)
    public ResponseEntity<Void> createEvictionBid(BidRequest req) {
        bidService.createEvictionBid(req);
        return ResponseEntity.ok().build();
    }

    @Override
    @RolesAllowed(User.Role.RESIDENT)
    public ResponseEntity<Void> updateEvictionBid(Long id, BidRequest req) {
        bidService.updateEvictionBid(id, req);
        return ResponseEntity.ok().build();
    }

    @Override
    @RolesAllowed(User.Role.RESIDENT)
    public ResponseEntity<Void> createDepartureBid(DepartureRequest req) {
        bidService.createDepartureBid(req);
        return ResponseEntity.ok().build();
    }

    @Override
    @RolesAllowed(User.Role.RESIDENT)
    public ResponseEntity<Void> updateDepartureBid(Long id, DepartureRequest req) {
        bidService.updateDepartureBid(id, req);
        return ResponseEntity.ok().build();
    }

    @Override
    @RolesAllowed(User.Role.RESIDENT)
    public ResponseEntity<Void> createRoomChangeBid(RoomChangeRequest req) {
        bidService.createRoomChangeBid(req);
        return ResponseEntity.ok().build();
    }

    @Override
    @RolesAllowed(User.Role.RESIDENT)
    public ResponseEntity<Void> updateRoomChangeBid(Long id, RoomChangeRequest req) {
        bidService.updateRoomChangeBid(id, req);
        return ResponseEntity.ok().build();
    }

    @Override
    @RolesAllowed(User.Role.MANAGER)
    public ResponseEntity<Void> acceptBid(Long id) {
        bidService.acceptBid(id);
        return ResponseEntity.ok().build();
    }

    @Override
    @RolesAllowed(User.Role.MANAGER)
    public ResponseEntity<Void> pendBid(Long id, OneFieldString body) {
        bidService.pendBid(id, body.getData());
        return ResponseEntity.ok().build();
    }

    @Override
    @RolesAllowed(User.Role.MANAGER)
    public ResponseEntity<Void> denyBid(Long id, OneFieldString body) {
        bidService.denyBid(id, body.getData());
        return ResponseEntity.ok().build();
    }
}
