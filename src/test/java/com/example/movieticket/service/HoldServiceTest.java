package com.example.movieticket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.movieticket.config.HoldProperties;
import com.example.movieticket.domain.SeatHold;
import com.example.movieticket.domain.Show;
import com.example.movieticket.domain.ShowSeat;
import com.example.movieticket.domain.User;
import com.example.movieticket.domain.enums.HoldStatus;
import com.example.movieticket.domain.enums.SeatCategory;
import com.example.movieticket.domain.enums.ShowType;
import com.example.movieticket.exception.SeatUnavailableException;
import com.example.movieticket.exception.UnauthorizedActionException;
import com.example.movieticket.mapper.HoldMapperImpl;
import com.example.movieticket.repository.SeatHoldRepository;
import com.example.movieticket.repository.ShowRepository;
import com.example.movieticket.repository.ShowSeatRepository;
import com.example.movieticket.repository.UserRepository;
import com.example.movieticket.support.factory.SeatFactory;
import com.example.movieticket.support.factory.ShowFactory;
import com.example.movieticket.support.factory.UserFactory;
import com.example.movieticket.web.dto.HoldResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HoldServiceTest {

    @Mock
    private ShowRepository showRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SeatHoldRepository seatHoldRepository;
    @Mock
    private ShowSeatRepository showSeatRepository;
    @Mock
    private SeatLockManager seatLockManager;
    @Mock
    private HoldProperties holdProperties;

    private HoldService holdService;

    private final User user = UserFactory.withId(1L, UserFactory.customer("alice@example.com"));
    private final Show show = ShowFactory.withId(9L,
            ShowFactory.show(null, null, Instant.parse("2026-07-01T10:00:00Z"),
                    Instant.parse("2026-07-01T12:00:00Z"), ShowType.REGULAR, new BigDecimal("200.00")));

    @BeforeEach
    void setUp() {
        holdService = new HoldService(showRepository, userRepository, seatHoldRepository,
                showSeatRepository, seatLockManager, new HoldMapperImpl(), holdProperties);
    }

    private ShowSeat showSeat(long id, Show owningShow) {
        ShowSeat seat = new ShowSeat(owningShow, SeatFactory.seat(null, "A", (int) id, SeatCategory.REGULAR),
                new BigDecimal("200.00"));
        seat.setId(id);
        return seat;
    }

    @Test
    void holdSeatsCreatesHoldAndReturnsTotal() {
        when(showRepository.findById(9L)).thenReturn(Optional.of(show));
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(holdProperties.ttlMinutes()).thenReturn(5L);
        List<ShowSeat> seats = List.of(showSeat(101L, show), showSeat(102L, show));
        when(seatLockManager.lockSeats(List.of(101L, 102L))).thenReturn(seats);
        when(seatHoldRepository.save(any(SeatHold.class))).thenAnswer(inv -> {
            SeatHold hold = inv.getArgument(0);
            hold.setId(55L);
            return hold;
        });

        HoldResponse response = holdService.holdSeats("alice@example.com", 9L, List.of(101L, 102L));

        assertThat(response.getHoldId()).isEqualTo(55L);
        assertThat(response.getShowId()).isEqualTo(9L);
        assertThat(response.getSeats()).hasSize(2);
        assertThat(response.getTotalAmount()).isEqualByComparingTo("400.00");
    }

    @Test
    void holdSeatsRejectsSeatFromDifferentShow() {
        Show otherShow = ShowFactory.withId(99L,
                ShowFactory.show(null, null, show.getStartTime(), show.getEndTime(),
                        ShowType.REGULAR, new BigDecimal("200.00")));
        when(showRepository.findById(9L)).thenReturn(Optional.of(show));
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(seatLockManager.lockSeats(List.of(500L))).thenReturn(List.of(showSeat(500L, otherShow)));

        assertThatThrownBy(() -> holdService.holdSeats("alice@example.com", 9L, List.of(500L)))
                .isInstanceOf(SeatUnavailableException.class);
    }

    @Test
    void releaseHoldRejectsAnotherUsersHold() {
        SeatHold hold = new SeatHold(user, show, Instant.parse("2026-07-01T10:05:00Z"));
        hold.setId(55L);
        when(seatHoldRepository.findById(55L)).thenReturn(Optional.of(hold));

        assertThatThrownBy(() -> holdService.releaseHold("mallory@example.com", 55L))
                .isInstanceOf(UnauthorizedActionException.class);
    }

    @Test
    void releaseHoldReleasesSeatsForOwner() {
        SeatHold hold = new SeatHold(user, show, Instant.parse("2026-07-01T10:05:00Z"));
        hold.setId(55L);
        when(seatHoldRepository.findById(55L)).thenReturn(Optional.of(hold));
        when(showSeatRepository.findByCurrentHoldId(55L)).thenReturn(List.of(showSeat(101L, show)));
        when(seatLockManager.lockSeats(List.of(101L))).thenReturn(List.of(showSeat(101L, show)));

        holdService.releaseHold("alice@example.com", 55L);

        assertThat(hold.getStatus()).isEqualTo(HoldStatus.RELEASED);
    }
}
