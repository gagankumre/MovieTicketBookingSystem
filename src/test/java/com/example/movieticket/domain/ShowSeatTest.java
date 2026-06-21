package com.example.movieticket.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.movieticket.domain.enums.SeatStatus;
import com.example.movieticket.support.factory.ShowSeatFactory;
import org.junit.jupiter.api.Test;

class ShowSeatTest {

    @Test
    void newSeatStartsAvailable() {
        assertThat(ShowSeatFactory.availableSeat().getStatus()).isEqualTo(SeatStatus.AVAILABLE);
    }

    @Test
    void blockMovesAvailableToHeldAndRecordsHold() {
        ShowSeat seat = ShowSeatFactory.availableSeat();

        assertThat(seat.block(7L)).isTrue();

        assertThat(seat.getStatus()).isEqualTo(SeatStatus.HELD);
        assertThat(seat.getCurrentHoldId()).isEqualTo(7L);
    }

    @Test
    void blockFailsWhenNotAvailable() {
        ShowSeat seat = ShowSeatFactory.availableSeat();
        seat.block(7L);

        assertThat(seat.block(8L)).isFalse();
        assertThat(seat.getCurrentHoldId()).isEqualTo(7L);
    }

    @Test
    void confirmMovesHeldToBookedClearingHoldAndSettingBooking() {
        ShowSeat seat = ShowSeatFactory.availableSeat();
        seat.block(7L);

        assertThat(seat.confirm(99L)).isTrue();

        assertThat(seat.getStatus()).isEqualTo(SeatStatus.BOOKED);
        assertThat(seat.getCurrentHoldId()).isNull();
        assertThat(seat.getCurrentBookingId()).isEqualTo(99L);
    }

    @Test
    void confirmFailsWhenNotHeld() {
        ShowSeat seat = ShowSeatFactory.availableSeat();

        assertThat(seat.confirm(99L)).isFalse();
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
    }

    @Test
    void releaseFromBookedReturnsToAvailableAndClearsPointers() {
        ShowSeat seat = ShowSeatFactory.availableSeat();
        seat.block(7L);
        seat.confirm(99L);

        assertThat(seat.release()).isTrue();

        assertThat(seat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
        assertThat(seat.getCurrentHoldId()).isNull();
        assertThat(seat.getCurrentBookingId()).isNull();
    }

    @Test
    void releaseFailsWhenAlreadyAvailable() {
        assertThat(ShowSeatFactory.availableSeat().release()).isFalse();
    }

    @Test
    void seatIsRebookableAfterRelease() {
        ShowSeat seat = ShowSeatFactory.availableSeat();
        seat.block(7L);
        seat.confirm(99L);
        seat.release();

        assertThat(seat.block(8L)).isTrue();
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.HELD);
    }

    @Test
    void casStatusOnlySucceedsFromExpectedState() {
        ShowSeat seat = ShowSeatFactory.availableSeat();

        assertThat(seat.casStatus(SeatStatus.HELD, SeatStatus.BOOKED)).isFalse();
        assertThat(seat.casStatus(SeatStatus.AVAILABLE, SeatStatus.HELD)).isTrue();
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.HELD);
    }
}
