package fr.becpg.repo.product.formulation.lca;

import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>LCADatabasePlugin interface.</p>
 *
 * @author valentin
 * @version $Id: $Id
 */
public interface LCADatabasePlugin {

	/**
	 * <p>extractData.</p>
	 *
	 * @param databaseNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link java.util.Map} object
	 */
	Map<String, LCAData> extractData(NodeRef databaseNodeRef);

	/**
	 * <p>acceptDatabaseFilename.</p>
	 *
	 * @param databaseName a {@link java.lang.String} object
	 * @return a boolean
	 */
	boolean acceptDatabaseFilename(String databaseName);

	/**
	 * <p>getMethod.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	String getMethod();

}
