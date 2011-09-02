package fr.becpg.repo.security;

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
	public static int WRITE_ACCESS = 2;
	public static int READ_ACCESS = 1;
	
	/**
	 * Compute access mode for the given field name on a specific type
	 * @param nodeType
	 * @param name
	 * @return Access Mode status
	 */
	public int computeAccessMode(QName nodeType, String name);

}
