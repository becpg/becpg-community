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
	public static int NONE_ACCESS = 0;
	public static int READ_ACCESS = 1;
	public static int WRITE_ACCESS = 2;
	
	/**
	 * Compute access mode for the given field name on a specific type
	 * @param nodeType
	 * @param name
	 * @return Access Mode status
	 */
	public int computeAccessMode(QName nodeType, String name);
	
	/**
	 * Compute ACLS in memory
	 */
	public void computeAcls();

	/**
	 * Extract props list based on existing ACL_GROUPS
	 * @param item
	 * @return
	 */
	public List<String> getAvailablePropNames();

}
