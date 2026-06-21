package com.example.movieticket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.movieticket.domain.SeatHold;
import com.example.movieticket.domain.ShowSeat;
import com.example.movieticket.domain.enums.HoldStatus;
import com.example.movieticket.repository.SeatHoldRepository;
import com.example.movieticket.repository.ShowSeatRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HoldExpiryServiceTest {

    private static final Instant NOW = Instant.parse("2026-07-01T10:10:00Z");

    @Mock
    private SeatHoldRepository seatHoldRepository;
    @Mock
    private ShowSeatRepository showSeatRepository;
    @Mock
    private SeatLockManager seatLockManager;

    @InjectMocks
    private HoldExpiryService holdExpiryService;

    @Test
    void expiresDueHoldsReleasesSeatsAndMarksExpired() {
        SeatHold hold = new SeatHold(null, null, NOW.minusSeconds(60));
        hold.setId(55L);
        ShowSeat seat = new ShowSeat(null, null, new BigDecimal("200.00"));
        seat.setId(101L);
        when(seatHoldRepository.findByStatusAndExpiresAtBefore(HoldStatus.ACTIVE, NOW))
                .thenReturn(List.of(hold));
        when(showSeatRepository.findByCurrentHoldId(55L)).thenReturn(List.of(seat));
        when(seatLockManager.lockSeats(List.of(101L))).thenReturn(List.of(seat));

        int expired = holdExpiryService.expireDueHolds(NOW);

        assertThat(expired).isEqualTo(1);
        assertThat(hold.getStatus()).isEqualTo(HoldStatus.EXPIRED);
        verify(seatLockManager).release(List.of(seat));
    }

    @Test
    void returnsZeroWhenNoHoldsDue() {
        when(seatHoldRepository.findByStatusAndExpiresAtBefore(HoldStatus.ACTIVE, NOW))
                .thenReturn(List.of());

        assertThat(holdExpiryService.expireDueHolds(NOW)).isZero();
        verifyNoInteractions(seatLockManager);
    }
}
