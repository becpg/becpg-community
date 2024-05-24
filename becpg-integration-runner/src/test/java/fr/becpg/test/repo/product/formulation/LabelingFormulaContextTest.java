package fr.becpg.test.repo.product.formulation;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

import fr.becpg.repo.product.formulation.labeling.LabelingFormulaContext;

public class LabelingFormulaContextTest {

	
	@Test
	public void roundBigDecimalTest() {
		

        BigDecimal value1 = new BigDecimal("1.2000000000001", LabelingFormulaContext.PRECISION);
        BigDecimal value2 = new BigDecimal("3.3333333333334", LabelingFormulaContext.PRECISION);

        double roundedValue1 = LabelingFormulaContext.roundedDouble(value1);
        double roundedValue2 = LabelingFormulaContext.roundedDouble(value2);

        assertEquals(1.2000000000001, value1.doubleValue(), 0);
        assertEquals(1.2, roundedValue1, 0);
        assertEquals(3.333333333, roundedValue2, 0);

        // Additional tests for division
        BigDecimal divisionResult = BigDecimal.valueOf(1d).divide(BigDecimal.valueOf(3d), LabelingFormulaContext.PRECISION);
        double divisionDoubleValue = divisionResult.doubleValue();
        double roundedDivisionValue = LabelingFormulaContext.roundedDouble(divisionResult);

        // Asserts to verify the expected division results
        assertEquals(0.3333333333333333, divisionDoubleValue, 0);
        assertEquals(0.3333333333, roundedDivisionValue, 0);	}
	

}
