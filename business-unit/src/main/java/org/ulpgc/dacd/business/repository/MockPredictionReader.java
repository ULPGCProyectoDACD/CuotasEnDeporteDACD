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
            
            // Probabilidades reales del "modelo" (suman 1.0)
            double rHome = 0.2 + random.nextDouble() * 0.5;
            double rDraw = 0.1 + random.nextDouble() * 0.2;
            double rAway = 1.0 - rHome - rDraw;

            // Para cada partido, generamos cuotas en todas las casas de apuestas
            for (String bookie : BOOKMAKERS) {
                // Cada casa tiene su propio margen (overround) entre 3% y 8%
                double margin = 1.03 + random.nextDouble() * 0.05;
                
                // Las cuotas de la casa (el inverso de la prob con margen)
                double priceHome = 1.0 / (rHome * margin + (random.nextDouble() - 0.5) * 0.1);
                double priceDraw = 1.0 / (rDraw * margin + (random.nextDouble() - 0.5) * 0.05);
                double priceAway = 1.0 / (rAway * margin + (random.nextDouble() - 0.5) * 0.1);

                // Asegurar que las cuotas no sean absurdas (< 1.0)
                priceHome = Math.max(1.1, priceHome);
                priceDraw = Math.max(1.1, priceDraw);
                priceAway = Math.max(1.1, priceAway);

                // Generar entradas para los 3 resultados posibles (como haria el sistema real)
                // Pero para simplificar el Dashboard, solemos mostrar la mejor oportunidad del partido
                // Aqui generamos una entrada por cada posible resultado
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
