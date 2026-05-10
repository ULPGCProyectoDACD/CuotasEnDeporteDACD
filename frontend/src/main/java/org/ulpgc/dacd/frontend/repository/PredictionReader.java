package org.ulpgc.dacd.frontend.repository;

import org.ulpgc.dacd.frontend.model.FilterOptionsDTO;
import org.ulpgc.dacd.frontend.model.PredictionDTO;

import java.util.List;

public interface PredictionReader {
    List<PredictionDTO> getPredictions(String team, String bookmaker);
    FilterOptionsDTO getFilterOptions();
}
