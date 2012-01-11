package fr.becpg.repo.listvalue;

import java.io.Serializable;
import java.util.Map;

/**
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
public interface ListValuePlugin {



	public String[] getHandleSourceTypes();

	public ListValuePage suggest(String sourceType, String query, Integer pageNum, Map<String, Serializable> props);

	
}
