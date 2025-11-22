package ru.itmo.is.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.itmo.is.dto.OneFieldDto;
import ru.itmo.is.dto.request.bid.BidRequest;
import ru.itmo.is.dto.request.bid.DepartureRequest;
import ru.itmo.is.dto.request.bid.OccupationRequest;
import ru.itmo.is.dto.request.bid.RoomChangeRequest;
import ru.itmo.is.dto.response.bid.BidResponse;
import ru.itmo.is.entity.bid.Bid;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.security.RolesAllowed;
import ru.itmo.is.service.BidService;

import java.util.List;

@RestController
@RequestMapping("/bid")
@RequiredArgsConstructor
public class BidController {
    private final BidService bidService;

    @RolesAllowed({User.Role.NON_RESIDENT, User.Role.RESIDENT})
    @GetMapping("/my/opened-types")
    public List<Bid.Type> getSelfOpenedBidTypes() {
        return bidService.getSelfOpenedBidTypes();
    }

    @RolesAllowed({User.Role.NON_RESIDENT, User.Role.RESIDENT})
    @GetMapping("/my")
    public List<BidResponse> getSelfBids() {
        return bidService.getSelfBids();
    }

    @RolesAllowed({User.Role.NON_RESIDENT, User.Role.RESIDENT, User.Role.MANAGER})
    @GetMapping("{id}")
    public BidResponse getBid(@PathVariable("id") long id) {
        return bidService.getBid(id);
    }

    @RolesAllowed(User.Role.MANAGER)
    @GetMapping("/in-process")
    public List<BidResponse> getInProcessBids() {
        return bidService.getInProcessBids();
    }

    @RolesAllowed(User.Role.MANAGER)
    @GetMapping("/pending")
    public List<BidResponse> getPendingBids() {
        return bidService.getPendingBids();
    }

    @RolesAllowed(User.Role.MANAGER)
    @GetMapping("/archived")
    public List<BidResponse> getArchivedBids() {
        return bidService.getArchivedBids();
    }

    @RolesAllowed(User.Role.NON_RESIDENT)
    @PostMapping("/occupation")
    public void createOccupationBid(@RequestBody @Valid OccupationRequest req) {
        bidService.saveOccupationBid(null, req);
    }

    @RolesAllowed(User.Role.NON_RESIDENT)
    @PutMapping("/occupation/{id}")
    public void editOccupationBid(@PathVariable("id") long id, @RequestBody @Valid OccupationRequest req) {
        bidService.saveOccupationBid(id, req);
    }

    @RolesAllowed(User.Role.RESIDENT)
    @PostMapping("/eviction")
    public void createEvictionBid(@RequestBody @Valid BidRequest req) {
        bidService.saveEvictionBid(null, req);
    }

    @RolesAllowed(User.Role.RESIDENT)
    @PutMapping("/eviction/{id}")
    public void editEvictionBid(@PathVariable("id") long id, @RequestBody @Valid BidRequest req) {
        bidService.saveEvictionBid(id, req);
    }

    @RolesAllowed(User.Role.RESIDENT)
    @PostMapping("/departure")
    public void createDepartureBid(@RequestBody @Valid DepartureRequest req) {
        bidService.saveDepartureBid(null, req);
    }

    @RolesAllowed(User.Role.RESIDENT)
    @PutMapping("/departure/{id}")
    public void editDepartureBid(@PathVariable("id") long id, @RequestBody @Valid DepartureRequest req) {
        bidService.saveDepartureBid(id, req);
    }

    @RolesAllowed(User.Role.RESIDENT)
    @PostMapping("/room-change")
    public void createRoomChangeBid(@RequestBody @Valid RoomChangeRequest req) {
        bidService.saveRoomChangeBid(null, req);
    }

    @RolesAllowed(User.Role.RESIDENT)
    @PutMapping("/room-change/{id}")
    public void editRoomChangeBid(@PathVariable("id") long id, @RequestBody @Valid RoomChangeRequest req) {
        bidService.saveRoomChangeBid(id, req);
    }

    @RolesAllowed(User.Role.MANAGER)
    @PostMapping("/{id}/accept")
    public void acceptBid(@PathVariable("id") long id) {
        bidService.acceptBid(id);
    }

    @RolesAllowed(User.Role.MANAGER)
    @PostMapping("/{id}/pend")
    public void pendBid(@PathVariable("id") long id, @RequestBody @Valid OneFieldDto<String> body) {
        bidService.pendBid(id, body.getData());
    }

    @RolesAllowed(User.Role.MANAGER)
    @PostMapping("/{id}/deny")
    public void denyBid(@PathVariable("id") long id, @RequestBody @Valid OneFieldDto<String> body) {
        bidService.denyBid(id, body.getData());
    }
}
