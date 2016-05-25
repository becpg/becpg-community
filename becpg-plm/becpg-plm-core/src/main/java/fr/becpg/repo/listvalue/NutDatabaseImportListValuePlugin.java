package fr.becpg.repo.listvalue;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.listvalue.impl.EntityListValuePlugin;
import fr.becpg.repo.listvalue.impl.FileInfoListValueExtractor;
import fr.becpg.repo.product.NutDatabaseService;

@Service
public class NutDatabaseImportListValuePlugin extends EntityListValuePlugin {

	private static final String SOURCE_TYPE_NUT_DB = "nutDataBase";
	private static final String SOURCE_TYPE_DATABASE_SUPPLIERS = "databaseSuppliers";

	@Autowired
	private NutDatabaseService nutDatabaseService;

	private final static Log logger = LogFactory.getLog(NutDatabaseImportListValuePlugin.class);

	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_NUT_DB, SOURCE_TYPE_DATABASE_SUPPLIERS };
	}

	@Override
	public ListValuePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {
		logger.debug("suggest src=" + sourceType + ", query=" + query + ", props=" + props);
		logger.debug("nutDatabaseService: " + nutDatabaseService);
		if (SOURCE_TYPE_DATABASE_SUPPLIERS.equals(sourceType)) {
			return new ListValuePage(nutDatabaseService.getNutDatabases(), pageNum, pageSize, new FileInfoListValueExtractor());
		} else if (SOURCE_TYPE_NUT_DB.equals(sourceType)) {
			return nutDatabaseService.suggest(props.get("parent").toString(), query, pageNum, pageSize);
		} else {
			return null;
		}
	}
}
