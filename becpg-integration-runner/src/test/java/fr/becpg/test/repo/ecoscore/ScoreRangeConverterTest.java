package fr.becpg.test.repo.ecoscore;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import fr.becpg.repo.formulation.spel.BeCPGSpelFunctions;
import fr.becpg.repo.formulation.spel.BeCPGSpelFunctions.BeCPGSpelFunctionsWrapper;
import fr.becpg.repo.project.formulation.ScoreRangeConverter;

public class ScoreRangeConverterTest {

	@Test
	public void testScoreLetter() {
		String input = "A: [8;10), B: [6;8), FFF1A: [4;6), D: [2;4), X: [0;2)";
		ScoreRangeConverter converter = new ScoreRangeConverter(input);
		assertEquals("A", converter.getScoreLetter(8));
		assertEquals("B", converter.getScoreLetter(7));
		assertEquals("FFF1A", converter.getScoreLetter(5));
		assertEquals("D", converter.getScoreLetter(3));
		assertEquals("X", converter.getScoreLetter(1));
		assertEquals("N/A", converter.getScoreLetter(-1));
	}
	
	@Test
	public void testInterpolate() {
		BeCPGSpelFunctions beCPGSpelFunctions = new BeCPGSpelFunctions();
		
		BeCPGSpelFunctionsWrapper wrapper = (BeCPGSpelFunctionsWrapper) beCPGSpelFunctions.create(null);
		
		// Test case with descending thresholds
        List<Double> valuesDesc = List.of(100d, 80d, 60d, 40d, 20d, 0d);
        List<Double> thresholdsDesc = List.of(100d, 74.66d, 56d, 37.33d, 18.66d, 0d);
        
        // 1) For input 75d:
        //    It lies between thresholds 100 and 74.66 (first segment).
        //    Calculation: 100 + (75 - 100) * (80 - 100) / (74.66 - 100)
        //    Expected ≈ 80.27
        double result1 = wrapper.interpolate(75d, valuesDesc, thresholdsDesc);
        assertEquals("Interpolated value for 75d (descending) should be ~80.27", 
                     80.27, result1, 0.01);
        
        // 2) For input 73d:
        //    73 is in the segment between 74.66 and 56.
        //    Calculation: 80 + (73 - 74.66) * (60 - 80) / (56 - 74.66)
        //    Expected ≈ 78.22
        double result2 = wrapper.interpolate(73d, valuesDesc, thresholdsDesc);
        assertEquals("Interpolated value for 73d (descending) should be ~78.22", 
                     78.22, result2, 0.01);
        
        // 3) For input 18.66d:
        //    18.66 equals a threshold value.
        //    According to the logic, this should yield the corresponding value.
        double result3 = wrapper.interpolate(18.66d, valuesDesc, thresholdsDesc);
        assertEquals("Interpolated value for 18.66d (descending) should be 20.0", 
                     20d, result3, 0.001);
        
        // Test case with ascending thresholds
        List<Double> thresholdsAsc = List.of(0.0, 0.01, 0.1, 1.0, 5.0, 10.0);
        
        // 4) For input 3d:
        //    3 falls between 1.0 and 5.0 (indices 3 and 4).
        //    Calculation: 40 + (3 - 1) * (20 - 40) / (5 - 1)
        //    Expected = 40 + 2 * (-20) / 4 = 40 - 10 = 30.0
        double result4 = wrapper.interpolate(3d, valuesDesc, thresholdsAsc);
        assertEquals("Interpolated value for 3d (ascending) should be 30.0", 
                     30d, result4, 0.01);
        
        
	}
	
}
