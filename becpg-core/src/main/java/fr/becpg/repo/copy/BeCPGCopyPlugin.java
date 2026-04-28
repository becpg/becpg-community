package fr.becpg.repo.copy;

import org.alfresco.repo.copy.CopyDetails;

/**
 * <p>BeCPGCopyPlugin interface.</p>
 *
 * @author matthieu
 */
public interface BeCPGCopyPlugin {

	public boolean shouldCopy(String typeToReset, CopyDetails copyDetails);
}
