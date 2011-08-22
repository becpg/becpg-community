/*
 * 
 */
package fr.becpg.repo.product.data.ing;

import java.util.Locale;

import org.alfresco.service.cmr.repository.MLText;

// TODO: Auto-generated Javadoc
/**
 * The Interface Ing.
 *
 * @author querephi
 */
public interface Ing {
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName();
	
	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name);
	
	/**
	 * Gets the mL name.
	 *
	 * @return the mL name
	 */
	public MLText getMLName();
	
	/**
	 * Sets the mL name.
	 *
	 * @param mlName the new mL name
	 */
	public void setMLName(MLText mlName);
	
	/**
	 * Gets the name.
	 *
	 * @param locale the locale
	 * @return the name
	 */
	public String getName(Locale locale);
	
	/**
	 * Gets the qty.
	 *
	 * @return the qty
	 */
	public float getQty();
}
