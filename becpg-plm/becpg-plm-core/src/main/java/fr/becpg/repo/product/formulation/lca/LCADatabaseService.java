package fr.becpg.repo.product.formulation.lca;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.autocomplete.AutoCompleteEntry;
import fr.becpg.repo.autocomplete.AutoCompletePage;
import fr.becpg.repo.helper.BeCPGQueryHelper;
import fr.becpg.repo.product.data.productList.LCAListDataItem;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>LCADatabaseService class.</p>
 *
 * @author valentin
 * @version $Id: $Id
 */
@Service("lcaDatabaseService")
public class LCADatabaseService {

	/** Constant <code>DATABASES_FOLDER="/app:company_home/cm:System/cm:LCADatab"{trunked}</code> */
	private static final String DATABASES_FOLDER = "/app:company_home/cm:System/cm:LCADatabases";

	private final NodeService nodeService;
	
	private final FileFolderService fileFolderService;
	
	private final Repository repositoryHelper;
	
	private final LCADatabasePlugin[] lcaPlugins;
	
	@Autowired
	/**
	 * <p>Constructor for LCADatabaseService.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 * @param fileFolderService a {@link org.alfresco.service.cmr.model.FileFolderService} object
	 * @param repositoryHelper a {@link org.alfresco.repo.model.Repository} object
	 * @param lcaPlugins an array of {@link fr.becpg.repo.product.formulation.lca.LCADatabasePlugin} objects
	 */
	public LCADatabaseService(@Qualifier("nodeService") NodeService nodeService,
			@Qualifier("fileFolderService") FileFolderService fileFolderService,
			@Qualifier("repositoryHelper") Repository repositoryHelper,
			LCADatabasePlugin[] lcaPlugins) {
		this.nodeService = nodeService;
		this.fileFolderService = fileFolderService;
		this.repositoryHelper = repositoryHelper;
		this.lcaPlugins = lcaPlugins;
	}
	
	/**
	 * <p>getPlugin.</p>
	 *
	 * @param databaseNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link fr.becpg.repo.product.formulation.lca.LCADatabasePlugin} object
	 */
	private LCADatabasePlugin getPlugin(NodeRef databaseNodeRef) {
		String databaseFilename = (String) nodeService.getProperty(databaseNodeRef, ContentModel.PROP_NAME);
		for (LCADatabasePlugin lcaDatabasePlugin : lcaPlugins) {
			if (lcaDatabasePlugin.acceptDatabaseFilename(databaseFilename)) {
				return lcaDatabasePlugin;
			}
		}
		throw new IllegalStateException("unknown LCA database: " + databaseFilename);
	}
	
	/**
	 * <p>getLCADatabases.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	public List<FileInfo> getLCADatabases() {
		NodeRef dbFolderNR = BeCPGQueryBuilder.createQuery().inDB().selectNodeByPath(repositoryHelper.getCompanyHome(), DATABASES_FOLDER);
		if (dbFolderNR != null) {
			return fileFolderService.listFiles(dbFolderNR);
		} else {
			return new ArrayList<>();
		}
	}
	
	/**
	 * <p>suggest.</p>
	 *
	 * @param databaseNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param query a {@link java.lang.String} object
	 * @param pageNum a {@link java.lang.Integer} object
	 * @param pageSize a {@link java.lang.Integer} object
	 * @return a {@link fr.becpg.repo.autocomplete.AutoCompletePage} object
	 */
	public AutoCompletePage suggest(NodeRef databaseNodeRef, String query, Integer pageNum, Integer pageSize) {

		List<LCAData> matches = new ArrayList<>();
		LCADatabasePlugin plugin = getPlugin(databaseNodeRef);
		Map<String, LCAData> lcaData = plugin.extractData(databaseNodeRef);
		
		String preparedQuery = BeCPGQueryHelper.prepareQuery(query).replace("*", "");

		matches.addAll(lcaData.values().stream().filter(res -> BeCPGQueryHelper.isQueryMatch(query, res.getValue()))
						.limit(100).collect(Collectors.toList()));

		matches.sort((o1, o2) -> {

			if (BeCPGQueryHelper.isAllQuery(query)) {
				return o1.getValue().compareTo(o2.getValue());
			}

			String value = BeCPGQueryHelper.prepareQueryForSorting(o1.getValue()).replace("*", "").replace(preparedQuery, "A");
			String value2 = BeCPGQueryHelper.prepareQueryForSorting(o2.getValue()).replace("*", "").replace(preparedQuery, "A");

			return value.compareTo(value2);

		});

		return new AutoCompletePage(matches, pageNum, pageSize, values -> {
			List<AutoCompleteEntry> suggestions = new ArrayList<>();
			if (values != null) {
				for (LCAData value : values) {
					suggestions.add(new AutoCompleteEntry(value.getId(), value.toString(), "category"));
				}
			}
			return suggestions;
		});
	}

	/**
	 * <p>Turns one entry of an LCA database into the lines of an LCA list.</p>
	 *
	 * @param databaseNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param entity the identifier of the entry in the database
	 * @return a {@link java.util.List} object, never null
	 */
	public List<LCAListDataItem> extractLCAList(NodeRef databaseNodeRef, String entity) {

		LCADatabasePlugin plugin = getPlugin(databaseNodeRef);
		LCAData efpData = plugin.extractData(databaseNodeRef).get(entity);

		if (efpData == null) {
			return new ArrayList<>();
		}

		List<LCAListDataItem> items = new ArrayList<>();

		for (Map.Entry<String, Double> impact : efpData.getImpacts().entrySet()) {
			LCAListDataItem item = createLCAListDataItem(impact.getKey(), impact.getValue());
			if (item != null) {
				item.setMethod(plugin.getMethod());
				items.add(item);
			}
		}

		return items;
	}

	/**
	 * <p>createLCAListDataItem.</p>
	 *
	 * @param lcaCode the indicator code, as held by {@code bcpg:lcaCode}
	 * @param lcaValue the impact value
	 * @return the line, or null when the repository holds no indicator for that code
	 */
	private LCAListDataItem createLCAListDataItem(String lcaCode, Double lcaValue) {
		if (lcaValue == null) {
			return null;
		}

		List<NodeRef> lca = BeCPGQueryBuilder.createQuery().inDB().ofType(PLMModel.TYPE_LCA).andPropEquals(PLMModel.PROP_LCA_CODE, lcaCode).list();

		if ((lca == null) || lca.isEmpty()) {
			return null;
		}

		LCAListDataItem item = new LCAListDataItem();
		item.setLca(lca.get(0));
		item.setValue(lcaValue);

		return item;
	}

	/**
	 * <p>extractScore.</p>
	 *
	 * @param databaseNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param entity a {@link java.lang.String} object
	 * @return a {@link java.lang.Double} object
	 */
	public Double extractScore(NodeRef databaseNodeRef, String entity) {
		LCADatabasePlugin plugin = getPlugin(databaseNodeRef);
		Map<String, LCAData> lcaData = plugin.extractData(databaseNodeRef);
		LCAData data = lcaData.get(entity);
		return data.getScore();
	}

	/**
	 * <p>getMethod.</p>
	 *
	 * @param databaseNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link java.lang.String} object
	 */
	public String getMethod(NodeRef databaseNodeRef) {
		LCADatabasePlugin plugin = getPlugin(databaseNodeRef);
		return plugin.getMethod();
	}

}
