package fr.becpg.repo.security;

import java.util.List;

import org.alfresco.service.namespace.QName;

/**
 * 
 * @author "Matthieu Laborie <laborima@gmail.com>"
 *
 */
public interface SecurityService {

	/**
	 * Access status
	 */
	static int NONE_ACCESS = 0;
	static int READ_ACCESS = 1;
	static int WRITE_ACCESS = 2;
	
	/**
	 * Compute access mode for the given field name on a specific type
	 * @param nodeType
	 * @param name
	 * @return Access Mode status
	 */
	int computeAccessMode(QName nodeType, String name);
	
	/**
	 * Refresh ACLS cache per tenant
	 */
	void refreshAcls();

	/**
	 * Extract props list based on existing ACL_GROUPS
	 * @param item
	 * @return
	 */
	List<String> getAvailablePropNames();

}
