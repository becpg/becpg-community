package fr.becpg.repo.autocomplete;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.autocomplete.impl.extractors.FileInfoAutoCompleteExtractor;
import fr.becpg.repo.autocomplete.impl.plugins.TargetAssocAutoCompletePlugin;
import fr.becpg.repo.product.formulation.lca.LCADatabaseService;

/**
 * <p>LCADatabaseAutoCompletePlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class LCADatabaseAutoCompletePlugin extends TargetAssocAutoCompletePlugin {

	private static final String SOURCE_TYPE_LCA_DB = "lcaDataBase";
	private static final String SOURCE_TYPE_LCA_SUPPLIERS = "lcaDatabaseSuppliers";

	@Autowired
	private LCADatabaseService lcaDatabaseService;

	/** {@inheritDoc} */
	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_LCA_DB, SOURCE_TYPE_LCA_SUPPLIERS };
	}

	/** {@inheritDoc} */
	@Override
	public AutoCompletePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {
		if (SOURCE_TYPE_LCA_SUPPLIERS.equals(sourceType)) {
			return new AutoCompletePage(lcaDatabaseService.getLCADatabases(), pageNum, pageSize, new FileInfoAutoCompleteExtractor());
		} else if (SOURCE_TYPE_LCA_DB.equals(sourceType)) {
			return lcaDatabaseService.suggest(new NodeRef(props.get("parent").toString()), query, pageNum, pageSize);
		}
		return null;
	}
}
