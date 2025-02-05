package fr.becpg.test.repo.ecoscore;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.alfresco.util.Pair;
import org.junit.Assert;
import org.junit.Test;

import fr.becpg.repo.product.formulation.ecoscore.EcoScoreService;

public class EcoScoreServiceTest {

	EcoScoreService ecoScoreService = new EcoScoreService();
	  
    @Test
    public void testEnvironmentalFootprintsLoading() {
        Map<String, EcoScoreService.EnvironmentalFootprintValue> footprints = ecoScoreService.getEnvironmentalFootprints();
        assertNotNull(footprints);
    }
    
    @Test
    public void testCountryScoresLoading() {
        Map<String, Pair<Double, Double>> scores = ecoScoreService.getCountryScores();
        assertNotNull(scores);
        Assert.assertEquals(93d,(double)ecoScoreService.countrySPI("FR"),0.1);
        Assert.assertEquals(100d,(double)ecoScoreService.countryEPI("FR"),0.1);
    }
    
    @Test
    public void testCountryLocationsLoading() {
        Map<String, Pair<Double, Double>> locations = ecoScoreService.getCountryLocations();
        assertNotNull(locations);
    }
    
    @Test
    public void testDistanceCalculation() {
        
        Long distance = ecoScoreService.distance("FR", "US");
        assertNotNull(distance);
        assertTrue(distance > 0);
        Assert.assertEquals(0,(long)ecoScoreService.distance("FR", "FR"));
        Assert.assertEquals(504,(long)ecoScoreService.distance("FR", "BE"));
        Assert.assertEquals(802,(long)ecoScoreService.distance("FR", "ES"));
    }
    
    @Test
    public void testInvalidCountryCodeDistance() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ecoScoreService.distance("XX", "YY");
        });
        Assert.assertEquals("Invalid country codes provided: XX", exception.getMessage());
    }
	
}
