package org.ulpgc.dacd.frontend.controller;

import com.google.gson.Gson;
import io.javalin.http.Context;
import org.ulpgc.dacd.frontend.model.FilterOptionsDTO;
import org.ulpgc.dacd.frontend.model.PredictionDTO;
import org.ulpgc.dacd.frontend.repository.PredictionReader;

import java.util.List;

public class PredictionController {
    private final PredictionReader reader;
    private final Gson gson;

    public PredictionController(PredictionReader reader) {
        this.reader = reader;
        this.gson = new Gson();
    }

    public void getPredictions(Context ctx) {
        String team = ctx.queryParam("team");
        String bookmaker = ctx.queryParam("bookmaker");
        List<PredictionDTO> predictions = reader.getPredictions(team, bookmaker);
        ctx.contentType("application/json");
        ctx.result(gson.toJson(predictions));
    }

    public void getFilters(Context ctx) {
        FilterOptionsDTO filters = reader.getFilterOptions();
        ctx.contentType("application/json");
        ctx.result(gson.toJson(filters));
    }
}
