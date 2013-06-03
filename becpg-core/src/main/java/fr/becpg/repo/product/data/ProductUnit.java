/*
 * 
 */
package fr.becpg.repo.product.data;

/**
 * The Enum ProductUnit.
 *
 * @author querephi
 */
public enum ProductUnit {

	/** The kg. */
	kg,
	
	/** The L. */
	L,
	
	/** The P. */
	P,
	
	/** The m. */
	m,
	
	/** The m2. */
	m2,
	
	m3,
	
	h;
		
	public static ProductUnit getUnit(String productUnit){
		return (productUnit != null && !productUnit.isEmpty()) ? ProductUnit.valueOf(productUnit) : ProductUnit.kg;
	}
}
