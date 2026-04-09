package org.ulpgc.dacd;

import java.util.List;

public interface MatchStore {
    void save(List<Match> matches);
}