package fr.becpg.repo.copy;

import org.alfresco.repo.copy.CopyDetails;

/**
 * <p>BeCPGCopyPlugin interface.</p>
 *
 * @author matthieu
 */
public interface BeCPGCopyPlugin {

	/**
	 * <p>shouldCopy.</p>
	 *
	 * @param typeToReset a {@link java.lang.String} object
	 * @param copyDetails a {@link org.alfresco.repo.copy.CopyDetails} object
	 * @return a boolean
	 */
	public boolean shouldCopy(String typeToReset, CopyDetails copyDetails);
}
