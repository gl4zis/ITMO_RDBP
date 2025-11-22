package ru.itmo.is.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.itmo.is.dto.request.PaymentRequest;
import ru.itmo.is.dto.response.PaymentResponse;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.security.RolesAllowed;
import ru.itmo.is.service.PaymentService;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @RolesAllowed(User.Role.RESIDENT)
    @GetMapping("/info/self")
    public PaymentResponse getSelfPaymentInfo() {
        return paymentService.getSelfPaymentInfo();
    }

    @RolesAllowed(User.Role.MANAGER)
    @GetMapping("/info")
    public PaymentResponse getPaymentInfo(@RequestParam("login") String login) {
        return paymentService.getPaymentInfo(login);
    }

    @RolesAllowed(User.Role.RESIDENT)
    @PostMapping("/pay")
    public void pay(@RequestBody PaymentRequest req) {
        paymentService.currentUserPay(req);
    }
}
