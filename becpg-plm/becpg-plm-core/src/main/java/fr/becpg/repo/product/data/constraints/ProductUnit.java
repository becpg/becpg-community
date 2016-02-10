/*
 *
 */
package fr.becpg.repo.product.data.constraints;

/**
 * The Enum ProductUnit.
 *
 * @author querephi
 */
public enum ProductUnit {

	kg,L,P,m,m2,m3,h,g,cL,mL;

	public static ProductUnit getUnit(String productUnit) {
		return ((productUnit != null) && !productUnit.isEmpty()) ? ProductUnit.valueOf(productUnit) : ProductUnit.kg;
	}
}
