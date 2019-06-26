package fr.becpg.test.repo.helper;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import fr.becpg.repo.product.helper.Nutrient5CHelper;

public class Nutrient5CTest {

	@Test
	public void test5C() {
		//#5500 
		Assert.assertEquals(12,Nutrient5CHelper.compute5CScore(2084d, 2.8d, 22.9d, 4.73d, 672d, 0d, 0d, 4.13d, 5.81d, "Others"));
		
		Assert.assertEquals("D",Nutrient5CHelper.buildNutrientClass(12d,
				Arrays.asList(new Double[]{18d,10d,2d,-1d}),Arrays.asList(new String[]{"E","D","C","B","A"})));
		
		
		Assert.assertEquals(8,Nutrient5CHelper.compute5CScore(1809d, 1.1d, 11d, 5.11d, 593d, 0d, 0d, 10d, 19.3d, "Others"));
		
		Assert.assertEquals("C",Nutrient5CHelper.buildNutrientClass(8d,
				Arrays.asList(new Double[]{18d,10d,2d,-1d}),Arrays.asList(new String[]{"E","D","C","B","A"})));
		
		
		Assert.assertEquals(4,Nutrient5CHelper.compute5CScore(2084d, 2.8d, 22.9d, 4.73d, 672d, 90d, 0d, 4.13d, 5.81d, "Others"));
		
		
	}
	
	
}
