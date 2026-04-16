package org.ulpgc.dacd.control.persistence;

import org.ulpgc.dacd.model.Match;
import java.util.List;

public interface MatchStore {
    void save(List<Match> matches);
}