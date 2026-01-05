package ru.itmo.is.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.is.api.PaymentApi;
import ru.itmo.is.dto.PaymentRequest;
import ru.itmo.is.dto.PaymentResponse;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.security.RolesAllowed;
import ru.itmo.is.service.PaymentService;

@RestController
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {
    private final PaymentService paymentService;

    @Override
    @RolesAllowed(User.Role.RESIDENT)
    public ResponseEntity<PaymentResponse> getSelfPaymentInfo() {
        return ResponseEntity.ok(paymentService.getSelfPaymentInfo());
    }

    @Override
    @RolesAllowed(User.Role.MANAGER)
    public ResponseEntity<PaymentResponse> getPaymentInfo(String login) {
        return ResponseEntity.ok(paymentService.getPaymentInfo(login));
    }

    @Override
    @RolesAllowed(User.Role.RESIDENT)
    public ResponseEntity<Void> pay(PaymentRequest req) {
        paymentService.currentUserPay(req);
        return ResponseEntity.ok().build();
    }
}
