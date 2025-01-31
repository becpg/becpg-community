package fr.becpg.test.repo.ecoscore;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import fr.becpg.repo.project.formulation.ScoreRangeConverter;

public class ScoreRangeConverterTest {
	  private ScoreRangeConverter converter;

	    @Before
	    void setUp() {
	        String input = "A: [8), B: [6;8), C: [4;6), D: [2;4), E: [0;2)";
	        converter = new ScoreRangeConverter(input);
	    }

	    @Test
	    void testScoreLetter() {
	        assertEquals("A", converter.getScoreLetter(8));
	        assertEquals("B", converter.getScoreLetter(7));
	        assertEquals("C", converter.getScoreLetter(5));
	        assertEquals("D", converter.getScoreLetter(3));
	        assertEquals("E", converter.getScoreLetter(1));
	        assertEquals("N/A", converter.getScoreLetter(-1));
	    }
}
