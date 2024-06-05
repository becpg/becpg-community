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
 * @author matthieu
 * @version $Id: $Id
 */
@Service("lcaDatabaseService")
public class LCADatabaseService {

	private static final String DATABASES_FOLDER = "/app:company_home/cm:System/cm:LCADatabases";

	@Autowired
	private NodeService nodeService;
	
	@Autowired
	private FileFolderService fileFolderService;
	
	@Autowired
	private Repository repositoryHelper;
	
	@Autowired
	private LCADatabasePlugin[] lcaPlugins;
	
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
	 * <p>extractLCAList.</p>
	 *
	 * @param databaseNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param entity a {@link java.lang.String} object
	 * @return a {@link java.util.List} object
	 */
	public List<LCAListDataItem> extractLCAList(NodeRef databaseNodeRef, String entity) {

		List<LCAListDataItem> items = new ArrayList<>();

		LCADatabasePlugin plugin = getPlugin(databaseNodeRef);
		Map<String, LCAData> lcaData = plugin.extractData(databaseNodeRef);

		String method = plugin.getMethod();
		
		LCAData efpData = lcaData.get(entity);
		
		LCAListDataItem item = createLCAListDataItem("ACIDIFICATION", efpData.getAcidification());
		if (item != null) {
			item.setMethod(method);
			items.add(item);
		}
		
		item = createLCAListDataItem("CLIMATE_CHANGE", efpData.getClimateChange());
		if (item != null) {
			item.setMethod(method);
			items.add(item);
		}
		
		item = createLCAListDataItem("PARTICULATE_MATTER", efpData.getParticulateMatter());
		if (item != null) {
			item.setMethod(method);
			items.add(item);
		}
		
		item = createLCAListDataItem("WATER_USE", efpData.getWaterUse());
		if (item != null) {
			item.setMethod(method);
			items.add(item);
		}
		
		item = createLCAListDataItem("LAND_USE", efpData.getLandUse());
		if (item != null) {
			item.setMethod(method);
			items.add(item);
		}
		
		item = createLCAListDataItem("RESOURCE_USE_FOSSILS", efpData.getResourceUseFossils());
		if (item != null) {
			item.setMethod(method);
			items.add(item);
		}
		
		item = createLCAListDataItem("ECOTOXICITY_FRESHWATER", efpData.getFreshwaterEcotoxicity());
		if (item != null) {
			item.setMethod(method);
			items.add(item);
		}
		
		item = createLCAListDataItem("OZONE_DEPLETION", efpData.getOzoneDepletion());
		if (item != null) {
			item.setMethod(method);
			items.add(item);
		}
		
		item = createLCAListDataItem("IONIZING_RADIATION", efpData.getIonizingRadiation());
		if (item != null) {
			item.setMethod(method);
			items.add(item);
		}
		
		item = createLCAListDataItem("PHOTOCHEMICAL_OZONE_FORMATION", efpData.getPhotochemicalOzoneFormation());
		if (item != null) {
			item.setMethod(method);
			items.add(item);
		}
		
		item = createLCAListDataItem("EUTROPHICATION_TERRESTRIAL", efpData.getTerrestrialEutrophication());
		if (item != null) {
			item.setMethod(method);
			items.add(item);
		}
		
		item = createLCAListDataItem("EUTROPHICATION_MARINE", efpData.getMarineEutrophication());
		if (item != null) {
			item.setMethod(method);
			items.add(item);
		}
		
		item = createLCAListDataItem("EUTROPHICATION_FRESHWATER", efpData.getFreshwaterEutrophication());
		if (item != null) {
			item.setMethod(method);
			items.add(item);
		}
		
		item = createLCAListDataItem("HUMAN_TOXICITY_CANCER", efpData.getHumanToxicityCancer());
		if (item != null) {
			item.setMethod(method);
			items.add(item);
		}
		
		item = createLCAListDataItem("HUMAN_TOXICITY_NON_CANCER", efpData.getHumanToxicityNonCancer());
		if (item != null) {
			item.setMethod(method);
			items.add(item);
		}
		
		item = createLCAListDataItem("RESOURCE_USE_MINERALS_METALS", efpData.getResourceUseMineralsMetal());
		if (item != null) {
			item.setMethod(method);
			items.add(item);
		}
		
		return items;
	}
	
	private LCAListDataItem createLCAListDataItem(String lcaCode, Double lcaValue) {
		if (lcaValue != null) {
			List<NodeRef> lca = BeCPGQueryBuilder.createQuery().inDB().ofType(PLMModel.TYPE_LCA).andPropEquals(PLMModel.PROP_LCA_CODE, lcaCode).list();
			if (lca != null && !lca.isEmpty()) {
				LCAListDataItem item = new LCAListDataItem();
				item.setLca(lca.get(0));
				item.setValue(lcaValue);
				return item;
			}
		}
		return null;
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
