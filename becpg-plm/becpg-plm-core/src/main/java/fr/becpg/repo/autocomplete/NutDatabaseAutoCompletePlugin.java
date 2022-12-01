package fr.becpg.repo.autocomplete;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.autocomplete.impl.extractors.FileInfoAutoCompleteExtractor;
import fr.becpg.repo.autocomplete.impl.plugins.TargetAssocAutoCompletePlugin;
import fr.becpg.repo.product.formulation.nutrient.NutDatabaseService;

/**
 * <p>NutDatabaseImportListValuePlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class NutDatabaseAutoCompletePlugin extends TargetAssocAutoCompletePlugin {

	private static final String SOURCE_TYPE_NUT_DB = "nutDataBase";
	private static final String SOURCE_TYPE_DATABASE_SUPPLIERS = "nutDatabaseSuppliers";

	@Autowired
	private NutDatabaseService nutDatabaseService;

	private final static Log logger = LogFactory.getLog(NutDatabaseAutoCompletePlugin.class);

	/** {@inheritDoc} */
	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_NUT_DB, SOURCE_TYPE_DATABASE_SUPPLIERS };
	}

	/** {@inheritDoc} */
	@Override
	public AutoCompletePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {
		logger.debug("suggest src=" + sourceType + ", query=" + query + ", props=" + props);
		logger.debug("nutDatabaseService: " + nutDatabaseService);
		if (SOURCE_TYPE_DATABASE_SUPPLIERS.equals(sourceType)) {
			return new AutoCompletePage(nutDatabaseService.getNutDatabases(), pageNum, pageSize, new FileInfoAutoCompleteExtractor());
		} else if (SOURCE_TYPE_NUT_DB.equals(sourceType)) {
			return nutDatabaseService.suggest(props.get("parent").toString(), query, pageNum, pageSize);
		} else {
			return null;
		}
	}
}
