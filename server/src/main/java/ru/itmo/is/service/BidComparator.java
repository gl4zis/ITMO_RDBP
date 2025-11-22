package ru.itmo.is.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.itmo.is.entity.bid.Bid;
import ru.itmo.is.entity.user.User;

@Component
@RequiredArgsConstructor
public class BidComparator {
    private final UserService userService;

    public int compare(Bid bid1, Bid bid2) {
        User manager = userService.getCurrentUserOrThrow();
        if (manager.equals(bid1.getManager())) return -1;
        if (manager.equals(bid2.getManager())) return 1;

        boolean isEmpty1 = bid1.getManager() == null;
        boolean isEmpty2 = bid2.getManager() == null;

        if (isEmpty1 && isEmpty2) return 0;
        if (isEmpty1) return -1;
        if (isEmpty2) return 1;

        return bid1.getManager().getLogin().compareTo(bid2.getManager().getLogin());
    }
}
