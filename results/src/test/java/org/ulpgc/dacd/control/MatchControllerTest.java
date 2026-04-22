package org.ulpgc.dacd.control;

import org.junit.jupiter.api.Test;
import org.ulpgc.dacd.control.feeder.MatchFeeder;
import org.ulpgc.dacd.control.persistence.MatchStore;
import org.ulpgc.dacd.model.Match;
import org.ulpgc.dacd.model.Team;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

class MatchControllerTest {

    @Test
    void should_get_matches_from_feeder_and_save_them_in_store() {

        MatchFeeder mockFeeder = mock(MatchFeeder.class);
        MatchStore mockStore = mock(MatchStore.class);

        MatchController controller = new MatchController(mockFeeder, mockStore);

        List<Match> fakeMatches = new ArrayList<>();
        fakeMatches.add(new Match(1,
                new Team(1, "Madrid", "MAD"),
                new Team(2, "Barça", "BAR"),
                2, 1, Instant.now(), "FINISHED", null, Instant.now()));

        when(mockFeeder.getMatches()).thenReturn(fakeMatches);

        controller.execute();

        verify(mockFeeder, times(1)).getMatches();

        verify(mockStore, times(1)).save(fakeMatches);
    }


    @Test
    void should_handle_empty_list_when_no_matches_available() {

        MatchFeeder mockFeeder = mock(MatchFeeder.class);
        MatchStore mockStore = mock(MatchStore.class);

        MatchController controller = new MatchController(mockFeeder, mockStore);

        List<Match> emptyMatches = new ArrayList<>();
        when(mockFeeder.getMatches()).thenReturn(emptyMatches);

        controller.execute();

        verify(mockFeeder, times(1)).getMatches();

        verify(mockStore, never()).save(any());
    }

}