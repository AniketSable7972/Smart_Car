package com.smartcar.monitoring.config;

import com.smartcar.monitoring.model.TripCost;
import com.smartcar.monitoring.repository.TripCostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;

@Configuration
public class DataSeedConfig {

    private static final Logger logger = LoggerFactory.getLogger(DataSeedConfig.class);

    @Autowired
    private TripCostRepository tripCostRepository;

    private static final String[] PUNE_POINTS = new String[]{
            "Shivajinagar, Pune", "Kothrud, Pune", "Hinjewadi, Pune", "Viman Nagar, Pune", "Kalyani Nagar, Pune"
    };

    @PostConstruct
    public void seedTripCosts() {
        try {
            for (int i = 0; i < PUNE_POINTS.length; i++) {
                for (int j = 0; j < PUNE_POINTS.length; j++) {
                    if (i == j) continue;
                    String start = PUNE_POINTS[i];
                    String end = PUNE_POINTS[j];
                    if (tripCostRepository.findByStartPointAndEndPointAndIsActiveTrue(start, end).isEmpty()) {
                        // Simple heuristic cost based on indices distance
                        int distance = Math.abs(i - j) + 1;
                        BigDecimal cost = BigDecimal.valueOf(50L * distance); // Rs.50 per hop
                        tripCostRepository.save(new TripCost(start, end, cost));
                    }
                }
            }
            logger.info("TripCost seed completed");
        } catch (Exception e) {
            logger.error("Error seeding TripCost", e);
        }
    }
}