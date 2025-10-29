/*
 * 
 */
package fr.becpg.repo.product.data;

import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.product.data.constraints.ProductUnit;

/**
 * <p>ResourceProductData class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:resourceProduct")
public class ResourceProductData extends ProductData  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 36647573027313786L;

	/**
	 * <p>build.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.ResourceProductData} object
	 */
	public static ResourceProductData build() {
		return new ResourceProductData();
	}

	/**
	 * <p>withName.</p>
	 *
	 * @param name a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.product.data.ResourceProductData} object
	 */
	public ResourceProductData withName(String name) {
		setName(name);
		return this;
	}

	/**
	 * <p>withQty.</p>
	 *
	 * @param qty a {@link java.lang.Double} object
	 * @return a {@link fr.becpg.repo.product.data.ResourceProductData} object
	 */
	public ResourceProductData withQty(Double qty) {
		setQty(qty);
		return this;
	}

	/**
	 * <p>withUnit.</p>
	 *
	 * @param unit a {@link fr.becpg.repo.product.data.constraints.ProductUnit} object
	 * @return a {@link fr.becpg.repo.product.data.ResourceProductData} object
	 */
	public ResourceProductData withUnit(ProductUnit unit) {
		setUnit(unit);
		return this;
	}

	/**
	 * <p>withDensity.</p>
	 *
	 * @param density a {@link java.lang.Double} object
	 * @return a {@link fr.becpg.repo.product.data.ResourceProductData} object
	 */
	public ResourceProductData withDensity(Double density) {
		setDensity(density);
		return this;
	}
}
