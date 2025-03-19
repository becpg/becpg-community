package fr.becpg.repo.ecm.autocomplete;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.MPMModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.autocomplete.AutoCompletePage;
import fr.becpg.repo.autocomplete.AutoCompleteService;
import fr.becpg.repo.autocomplete.impl.plugins.TargetAssocAutoCompletePlugin;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.product.data.constraints.LabelingRuleType;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * Plugin for auto-completing calculated characteristics in ECO context.
 * Provides suggestions for dynamic characteristics and labeling rules.
 *
 * @author matthieu
 * @version 1.0
 */
@Service("calculatedCharactsAutoCompletePlugin")
public class CalculatedCharactsAutoCompletePlugin extends TargetAssocAutoCompletePlugin {

	private static final String SOURCE_TYPE_ECO = "eco";
	
	private static final Log logger = LogFactory.getLog(CalculatedCharactsAutoCompletePlugin.class);

	@Autowired
	private CalculatedCharactsAutoCompleteExtractor calculatedCharactsValueExtractor;

	@Autowired
	private EntityListDAO entityListDAO;

	/** 
	 * {@inheritDoc} 
	 */
	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_ECO };
	}

	/** 
	 * {@inheritDoc} 
	 */
	@Override
	public AutoCompletePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {
		if (logger.isDebugEnabled()) {
			logger.debug("Suggesting calculated characteristics for query: '" + query + "'");
		}
		
		List<NodeRef> results = new LinkedList<>();

		// Find all template products
		List<NodeRef> templateProducts = findTemplateProducts();
		if (logger.isDebugEnabled()) {
			logger.debug("Found " + templateProducts.size() + " template products");
		}

		// Process each template product
		for (NodeRef templateProduct : templateProducts) {
			addTemplateCharacteristics(results, templateProduct, query);
		}

		// Add standard characteristics
		addStandardCharacteristics(results, query, props);

		if (logger.isDebugEnabled()) {
			logger.debug("Returning " + results.size() + " suggestions");
		}
		return new AutoCompletePage(results, pageNum, pageSize, calculatedCharactsValueExtractor);
	}

	/**
	 * Finds all product templates in the system.
	 * 
	 * @return List of template product NodeRefs
	 */
	protected List<NodeRef> findTemplateProducts() {
		return BeCPGQueryBuilder.createQuery()
				.ofType(PLMModel.TYPE_PRODUCT)
				.withAspect(BeCPGModel.ASPECT_ENTITY_TPL)
				.inDB()
				.list();
	}

	/**
	 * Adds standard characteristics matching the query.
	 * 
	 * @param results The list to add results to
	 * @param query The search query
	 * @param props Additional properties for the search
	 */
	protected void addStandardCharacteristics(List<NodeRef> results, String query, Map<String, Serializable> props) {
		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery()
				.inType(PLMModel.TYPE_NUT)
				.inType(PLMModel.TYPE_COST)
				.inType(PLMModel.TYPE_PHYSICO_CHEM)
				.inType(PLMModel.TYPE_LABEL_CLAIM)
				.andPropQuery(BeCPGModel.PROP_CHARACT_NAME, prepareQuery(query))
				.ftsLanguage();
		
		// Handle deleted items flag
		boolean includeDeleted = (props != null) && 
				props.containsKey(AutoCompleteService.PROP_INCLUDE_DELETED) &&
				(Boolean) props.get(AutoCompleteService.PROP_INCLUDE_DELETED);
		
		if (!includeDeleted) {
			queryBuilder.excludeProp(BeCPGModel.PROP_IS_DELETED, "true");
		}

		List<NodeRef> standardCharacteristics = queryBuilder.list();
		if (logger.isDebugEnabled()) {
			logger.debug("Found " + standardCharacteristics.size() + " standard characteristics matching query");
		}
		
		results.addAll(standardCharacteristics);
	}

	/**
	 * Adds characteristics from a template product.
	 * 
	 * @param results The list to add results to
	 * @param templateProduct The template product NodeRef
	 * @param query The search query
	 */
	protected void addTemplateCharacteristics(List<NodeRef> results, NodeRef templateProduct, String query) {
		if (logger.isDebugEnabled()) {
			logger.debug("Processing template product: " + templateProduct);
		}
		
		addDynamicCharacteristics(results, templateProduct, query);
		addLabelingRules(results, templateProduct, query);
	}

	/**
	 * Adds labeling rules from a template product that match the query.
	 * 
	 * @param results The list to add results to
	 * @param templateProduct The template product NodeRef
	 * @param query The search query
	 */
	protected void addLabelingRules(List<NodeRef> results, NodeRef templateProduct, String query) {
		NodeRef listsContainer = entityListDAO.getListContainer(templateProduct);
		if (listsContainer == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("No lists container found for template product: " + templateProduct);
			}
			return;
		}

		NodeRef labelingRulesList = entityListDAO.getList(listsContainer, PLMModel.TYPE_INGLABELINGLIST);
		if (labelingRulesList == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("No labeling rules list found for template product: " + templateProduct);
			}
			return;
		}

		List<NodeRef> labelingRules = entityListDAO.getListItems(labelingRulesList, PLMModel.TYPE_LABELINGRULELIST);
		int matchCount = 0;
		
		for (NodeRef labelingRule : labelingRules) {
			String ruleType = (String) nodeService.getProperty(labelingRule, PLMModel.PROP_LABELINGRULELIST_TYPE);
			
			if (LabelingRuleType.Render.toString().equals(ruleType)) {
				String name = (String) nodeService.getProperty(labelingRule, ContentModel.PROP_NAME);

				if (isQueryMatch(query, name)) {
					results.add(labelingRule);
					matchCount++;
				}
			}
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("Found " + matchCount + " matching labeling rules");
		}
	}

	/**
	 * Adds dynamic characteristics from a template product that match the query.
	 * 
	 * @param results The list to add results to
	 * @param templateProduct The template product NodeRef
	 * @param query The search query
	 */
	protected void addDynamicCharacteristics(List<NodeRef> results, NodeRef templateProduct, String query) {
		NodeRef listsContainer = entityListDAO.getListContainer(templateProduct);
		if (listsContainer == null) {
			return;
		}

		// Process characteristics from different list types
		NodeRef compoList = entityListDAO.getList(listsContainer, PLMModel.TYPE_COMPOLIST);
		NodeRef packagingList = entityListDAO.getList(listsContainer, PLMModel.TYPE_PACKAGINGLIST);
		NodeRef processList = entityListDAO.getList(listsContainer, MPMModel.TYPE_PROCESSLIST);

		int totalMatches = 0;
		totalMatches += addDynamicCharacteristicsFromList(results, compoList, query);
		totalMatches += addDynamicCharacteristicsFromList(results, packagingList, query);
		totalMatches += addDynamicCharacteristicsFromList(results, processList, query);
		
		if (logger.isDebugEnabled()) {
			logger.debug("Found " + totalMatches + " matching dynamic characteristics");
		}
	}

	/**
	 * Adds dynamic characteristics from a specific list that match the query.
	 * 
	 * @param results The list to add results to
	 * @param dataList The data list NodeRef
	 * @param query The search query
	 * @return The number of matches found
	 */
	protected int addDynamicCharacteristicsFromList(List<NodeRef> results, NodeRef dataList, String query) {
		if (dataList == null) {
			return 0;
		}
		
		List<NodeRef> listItems = entityListDAO.getListItems(dataList, PLMModel.TYPE_DYNAMICCHARACTLIST);
		int matchCount = 0;
		
		for (NodeRef item : listItems) {
			Object columnValue = nodeService.getProperty(item, PLMModel.PROP_DYNAMICCHARACT_COLUMN);
			
			// Only include items with null or empty column
			if (columnValue == null || columnValue.toString().isEmpty()) {
				String title = (String) nodeService.getProperty(item, PLMModel.PROP_DYNAMICCHARACT_TITLE);

				if (isQueryMatch(query, title)) {
					results.add(item);
					matchCount++;
				}
			}
		}
		
		return matchCount;
	}
}
