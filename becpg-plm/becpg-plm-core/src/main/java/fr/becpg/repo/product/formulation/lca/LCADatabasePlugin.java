package fr.becpg.repo.product.formulation.lca;

import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

public interface LCADatabasePlugin {

	Map<String, LCAData> extractData(NodeRef databaseNodeRef);

	boolean acceptDatabaseFilename(String databaseName);

	String getMethod();

}
