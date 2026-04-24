package org.ulpgc.dacd.control;

import org.junit.jupiter.api.Test;
import org.ulpgc.dacd.control.feeder.OddsFeeder;
import org.ulpgc.dacd.control.persistence.OddsStore;
import org.ulpgc.dacd.model.BookmakerContext;
import org.ulpgc.dacd.model.MatchContext;
import org.ulpgc.dacd.model.Odd;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

class OddsControllerTest {

    @Test
    void should_get_odds_from_feeder_and_save_them_in_store() {

        OddsFeeder mockFeeder = mock(OddsFeeder.class);
        OddsStore mockStore = mock(OddsStore.class);

        OddsController controller = new OddsController(mockFeeder, mockStore);

        List<Odd> fakeOdds = new ArrayList<>();

        MatchContext match = new MatchContext("id1", "soccer", "Madrid", "Barça", "2024-01-01T20:00:00Z");
        BookmakerContext bookmaker = new BookmakerContext("b1", "Bet365", "2024-01-01T10:00:00Z");

        fakeOdds.add(new Odd(match, bookmaker, "h2h", "Madrid", 1.85, null));

        when(mockFeeder.getOdds()).thenReturn(fakeOdds);

        controller.execute();

        verify(mockFeeder, times(1)).getOdds();

        verify(mockStore, times(1)).save(fakeOdds);
    }

    @Test
    void should_handle_empty_list_when_no_odds_available() {

        OddsFeeder mockFeeder = mock(OddsFeeder.class);
        OddsStore mockStore = mock(OddsStore.class);

        OddsController controller = new OddsController(mockFeeder, mockStore);

        List<Odd> emptyOdds = new ArrayList<>();
        when(mockFeeder.getOdds()).thenReturn(emptyOdds);

        controller.execute();

        verify(mockFeeder, times(1)).getOdds();

        verify(mockStore, never()).save(any());
    }
}