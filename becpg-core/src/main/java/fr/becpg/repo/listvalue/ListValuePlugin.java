package fr.becpg.repo.listvalue;

import java.io.Serializable;
import java.util.Map;

/**
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
public interface ListValuePlugin {


	/**
	 * 
	 * @return sourceType that the plugin handle
	 */
	public String[] getHandleSourceTypes();

	/**
	 * 
	 * @param sourceType
	 * @param query
	 * @param pageNum
	 * @param pageSize
	 * @param props
	 * @return Suggested values page
	 */
	public ListValuePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props);

	
}
