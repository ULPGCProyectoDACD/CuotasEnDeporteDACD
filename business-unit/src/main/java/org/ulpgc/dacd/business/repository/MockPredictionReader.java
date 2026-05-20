package org.ulpgc.dacd.business.repository;

import org.ulpgc.dacd.business.model.FilterOptionsDTO;
import org.ulpgc.dacd.business.model.PredictionDTO;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class MockPredictionReader implements PredictionReader {

    private final List<PredictionDTO> mockPredictions;
    private final FilterOptionsDTO mockFilters;
    private final Random random = new Random();

    private final List<String> TEAMS = Arrays.asList(
            "Barcelona", "Real Madrid", "Villarreal", "Atletico Madrid", "Real Betis", "Celta Vigo", "Getafe",
            "Real Sociedad", "Athletic Club", "Rayo Vallecano", "Osasuna", "Valencia", "Sevilla", "Elche", "Mallorca",
            "Espanyol", "Girona", "Alaves", "Levante", "Oviedo"
    );

    private final List<String> BOOKMAKERS = Arrays.asList(
            "Bet365", "Bwin", "William Hill", "Betfair", "888sport", "Pinnacle", "Marathonbet", "Interwetten", "Unibet", "LeoVegas"
    );

    public MockPredictionReader() {
        this.mockPredictions = generateMockData();
        this.mockFilters = new FilterOptionsDTO(TEAMS, BOOKMAKERS);
        System.out.println("🧪 [MOCK MODE] Generadas " + mockPredictions.size() + " predicciones sintéticas realistas.");
    }

    @Override
    public List<PredictionDTO> getPredictions(String team, String bookmaker) {
        return mockPredictions.stream()
                .filter(p -> (team == null || team.isBlank() || p.homeTeam().equals(team) || p.awayTeam().equals(team)))
                .filter(p -> (bookmaker == null || bookmaker.isBlank() || p.bookmaker().equals(bookmaker)))
                .collect(Collectors.toList());
    }

    @Override
    public FilterOptionsDTO getFilterOptions() {
        return mockFilters;
    }

    private List<PredictionDTO> generateMockData() {
        List<PredictionDTO> data = new ArrayList<>();
        int idCounter = 1;
        Instant now = Instant.now();

        // Generar 50 partidos únicos (cruces aleatorios)
        for (int i = 0; i < 50; i++) {
            String home = TEAMS.get(random.nextInt(TEAMS.size()));
            String away;
            do { away = TEAMS.get(random.nextInt(TEAMS.size())); } while (away.equals(home));

            Instant matchDate = now.plus(random.nextInt(7), ChronoUnit.DAYS)
                                  .plus(random.nextInt(24), ChronoUnit.HOURS);

            // Probabilidades del modelo con mínimos razonables (suman 1.0)
            double rawHome = 0.25 + random.nextDouble() * 0.40;  // [0.25, 0.65]
            double rawDraw = 0.15 + random.nextDouble() * 0.15;  // [0.15, 0.30]
            double rawAway = Math.max(0.12, 1.0 - rawHome - rawDraw);
            double total = rawHome + rawDraw + rawAway;
            double rHome = rawHome / total;
            double rDraw = rawDraw / total;
            double rAway = rawAway / total;

            for (String bookie : BOOKMAKERS) {
                double margin = 1.03 + random.nextDouble() * 0.05;

                double priceHome, priceDraw, priceAway;

                // ~35% de casas ofrecerán cuotas con ventaja real (value bets)
                if (random.nextDouble() < 0.35) {
                    // Generar cuota que garantice bri positivo: price = multiplier / prob
                    // multiplier > 1.0 → bri = (prob * price) - 1 = multiplier - 1 > 0
                    double mult = 1.25 + random.nextDouble() * 0.35; // bri entre +0.25 y +0.60
                    priceHome = Math.min(5.0, Math.max(1.10, mult / rHome));
                    priceDraw = Math.min(5.0, Math.max(1.10, mult / rDraw));
                    priceAway = Math.min(5.0, Math.max(1.10, mult / rAway));
                } else {
                    // Cuotas normales con margen de la casa (bri negativo o neutro)
                    priceHome = Math.min(5.0, Math.max(1.10, 1.0 / (rHome * margin)));
                    priceDraw = Math.min(5.0, Math.max(1.10, 1.0 / (rDraw * margin)));
                    priceAway = Math.min(5.0, Math.max(1.10, 1.0 / (rAway * margin)));
                }

                data.add(createPrediction(idCounter++, matchDate, home, away, bookie, home, priceHome, rHome, rDraw, rAway));
                data.add(createPrediction(idCounter++, matchDate, home, away, bookie, "Empate", priceDraw, rHome, rDraw, rAway));
                data.add(createPrediction(idCounter++, matchDate, home, away, bookie, away, priceAway, rHome, rDraw, rAway));
            }
        }
        return data;
    }

    private PredictionDTO createPrediction(int id, Instant date, String home, String away, String bookie, String outcome, double price, double p1, double pX, double p2) {
        // El indice de valor es (ProbModelo * Cuota) - 1
        double modelProb = outcome.equals(home) ? p1 : (outcome.equals("Empate") ? pX : p2);
        double index = (modelProb * price) - 1;

        return new PredictionDTO(
            id, date.toString(), home, away, bookie, "h2h", outcome,
            Math.round(price * 100.0) / 100.0,
            p1, pX, p2,
            Math.round(index * 1000.0) / 1000.0,
            Instant.now().toString()
        );
    }
}
