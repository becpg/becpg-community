package fr.becpg.repo.repository.model;

import org.alfresco.service.cmr.repository.MLText;

/**
 * <p>ControlableListDataItem interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface ControlableListDataItem extends SimpleCharactDataItem {

	/**
	 * <p>getTextCriteria.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public MLText getTextCriteria();
	
}
