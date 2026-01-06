package ru.itmo.is.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.is.entity.bid.Bid;
import ru.itmo.is.entity.user.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BidComparatorTest {

    @Mock
    private UserService userService;
    @InjectMocks
    private BidComparator bidComparator;

    private User currentManager;
    private User otherManager;
    private Bid bid1;
    private Bid bid2;

    @BeforeEach
    void setUp() {
        currentManager = new User();
        currentManager.setLogin("currentManager");

        otherManager = new User();
        otherManager.setLogin("otherManager");

        bid1 = new Bid();
        bid2 = new Bid();
    }

    @Test
    void testCompare_WhenBid1HasCurrentManager_ShouldReturnNegative() {
        when(userService.getCurrentUserOrThrow()).thenReturn(currentManager);
        bid1.setManager(currentManager);
        bid2.setManager(otherManager);

        int result = bidComparator.compare(bid1, bid2);

        assertTrue(result < 0);
    }

    @Test
    void testCompare_WhenBid2HasCurrentManager_ShouldReturnPositive() {
        when(userService.getCurrentUserOrThrow()).thenReturn(currentManager);
        bid1.setManager(otherManager);
        bid2.setManager(currentManager);

        int result = bidComparator.compare(bid1, bid2);

        assertTrue(result > 0);
    }

    @Test
    void testCompare_WhenBothHaveNoManager_ShouldReturnZero() {
        when(userService.getCurrentUserOrThrow()).thenReturn(currentManager);
        bid1.setManager(null);
        bid2.setManager(null);

        int result = bidComparator.compare(bid1, bid2);

        assertEquals(0, result);
    }

    @Test
    void testCompare_WhenBid1HasNoManager_ShouldReturnNegative() {
        when(userService.getCurrentUserOrThrow()).thenReturn(currentManager);
        bid1.setManager(null);
        bid2.setManager(otherManager);

        int result = bidComparator.compare(bid1, bid2);

        assertTrue(result < 0);
    }

    @Test
    void testCompare_WhenBid2HasNoManager_ShouldReturnPositive() {
        when(userService.getCurrentUserOrThrow()).thenReturn(currentManager);
        bid1.setManager(otherManager);
        bid2.setManager(null);

        int result = bidComparator.compare(bid1, bid2);

        assertTrue(result > 0);
    }

    @Test
    void testCompare_WhenBothHaveOtherManagers_ShouldCompareByLogin() {
        when(userService.getCurrentUserOrThrow()).thenReturn(currentManager);
        User managerA = new User();
        managerA.setLogin("managerA");
        User managerB = new User();
        managerB.setLogin("managerB");

        bid1.setManager(managerA);
        bid2.setManager(managerB);

        int result = bidComparator.compare(bid1, bid2);

        assertTrue(result < 0);
    }
}

