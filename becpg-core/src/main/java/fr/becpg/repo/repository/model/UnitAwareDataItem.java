package fr.becpg.repo.repository.model;

/**
 * <p>UnitAwareDataItem interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface UnitAwareDataItem {
	
	/**
	 * <p>setUnit.</p>
	 *
	 * @param unit a {@link java.lang.String} object.
	 */
	void setUnit(String unit);
	
	/**
	 * <p>getUnit.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	String getUnit();

}
