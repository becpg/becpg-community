package fr.becpg.repo.repository.model;

/**
 * <p>FormulatedCharactDataItem interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface FormulatedCharactDataItem extends SimpleCharactDataItem, ManualDataItem {

	/**
	 * <p>getFormulatedValue.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	Double getFormulatedValue();
	
	/**
	 * <p>setFormulatedValue.</p>
	 *
	 * @param formulatedValue a {@link java.lang.Double} object.
	 */
	void setFormulatedValue(Double formulatedValue);
	
	/**
	 * <p>getIsFormulated.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	Boolean getIsFormulated();
	
	/**
	 * <p>setIsFormulated.</p>
	 *
	 * @param isFormulated a {@link java.lang.Boolean} object.
	 */
	void setIsFormulated(Boolean isFormulated);

}
