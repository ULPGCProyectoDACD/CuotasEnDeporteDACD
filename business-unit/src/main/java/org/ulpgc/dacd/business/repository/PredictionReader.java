package org.ulpgc.dacd.business.repository;

import org.ulpgc.dacd.business.model.FilterOptionsDTO;
import org.ulpgc.dacd.business.model.PredictionDTO;

import java.util.List;

public interface PredictionReader {
    List<PredictionDTO> getPredictions(String team, String bookmaker);
    FilterOptionsDTO getFilterOptions();
}
