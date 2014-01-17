/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.designer.impl;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.CompareMimeTypeEvaluator;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.evaluator.compare.ComparePropertyValueOperation;
import org.alfresco.repo.action.executer.AddFeaturesActionExecuter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.repo.designer.DesignerInitService;
import fr.becpg.repo.designer.DesignerModel;

/**
 * Init designer diles and folders
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 * 
 */
public class DesignerInitServiceImpl implements DesignerInitService {

	public static final StoreRef SPACES_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");

	/**
	 * Message key path prefix
	 */
	private static final String PATH_MSG_PFX = "path.";

	/** The Constant XPATH. */
	private static final String XPATH = "./%s:%s";

	private static final String MODEL_PREFIX_SEPARATOR = ":";

	private static final String DATA_DICTIONNARY_PATH = "./app:dictionary";

	private static final String PATH_MODELS = "./app:dictionary/app:models";

	private static final String PATH_WORKFLOWS = "./app:dictionary/app:workflow_defs";

	/** The node service. */
	private NodeService nodeService;

	/** The rule service. */
	private RuleService ruleService;

	/** The action service. */
	private ActionService actionService;

	private ContentService contentService;

	private MimetypeService mimetypeService;
	
	private SearchService searchService;

	private NamespaceService namespaceService;

	private Repository repository;

	/** The logger. */
	private static final Log logger = LogFactory.getLog(DesignerInitServiceImpl.class);

	/**
	 * @param nodeService
	 *            the nodeService to set
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * @param ruleService
	 *            the ruleService to set
	 */
	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}

	/**
	 * @param actionService
	 *            the actionService to set
	 */
	public void setActionService(ActionService actionService) {
		this.actionService = actionService;
	}

	/**
	 * @param contentService
	 *            the contentService to set
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	/**
	 * @param mimetypeService
	 *            the mimetypeService to set
	 */
	public void setMimetypeService(MimetypeService mimetypeService) {
		this.mimetypeService = mimetypeService;
	}

	/**
	 * @param searchService
	 */
	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	/**
	 * @param namespaceService
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * Sets the repository.
	 * 
	 * @param repository
	 *            the new repository
	 */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	private NodeRef getFolderByPath(NodeRef parentNodeRef, String path) {

		String xPath = path.contains(MODEL_PREFIX_SEPARATOR) ? path : String.format(XPATH, NamespaceService.CONTENT_MODEL_PREFIX, ISO9075.encode(path));

		logger.debug("get folder by path: " + xPath);

		List<NodeRef> nodes = searchService.selectNodes(parentNodeRef, xPath, null, namespaceService, false);

		if (!nodes.isEmpty()) {
			return nodes.get(0);
		}

		return null;
	}

	public void visitRules(NodeRef nodeRef, String folderName) {
		if (folderName == PATH_CONFIGS) {
			addAspectRule(nodeRef, "Add config aspect", "Add model config to xml file", DesignerModel.ASPECT_CONFIG);
		}

	}

	private void addAspectRule(NodeRef nodeRef, String ruleName, String ruleDescription, QName aspectModel) {

		// action
		CompositeAction compositeAction = actionService.createCompositeAction();
		Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, aspectModel);
		Action action = actionService.createAction(AddFeaturesActionExecuter.NAME, params);
		compositeAction.addAction(action);

		// compare-mime-type == text/xml
		ActionCondition conditionOnMimeType = actionService.createActionCondition(CompareMimeTypeEvaluator.NAME);
		conditionOnMimeType.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, MimetypeMap.MIMETYPE_XML);
		conditionOnMimeType.setParameterValue(ComparePropertyValueEvaluator.PARAM_PROPERTY, ContentModel.PROP_CONTENT);
		conditionOnMimeType.setInvertCondition(false);
		compositeAction.addActionCondition(conditionOnMimeType);

		// compare-name == *.xml
		ActionCondition conditionOnName = actionService.createActionCondition(ComparePropertyValueEvaluator.NAME);
		conditionOnName.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.ENDS.toString());
		conditionOnName.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, ".xml");
		conditionOnName.setParameterValue(ComparePropertyValueEvaluator.PARAM_PROPERTY, ContentModel.PROP_NAME);
		conditionOnName.setInvertCondition(false);
		compositeAction.addActionCondition(conditionOnName);

		// rule
		Rule rule = new Rule();
		rule.setRuleType(RuleType.INBOUND);
		rule.setAction(compositeAction);
		rule.applyToChildren(true);
		rule.setTitle(ruleName);
		rule.setExecuteAsynchronously(true);
		rule.setDescription(ruleDescription);
		ruleService.saveRule(nodeRef, rule);

	}

	protected NodeRef visitFolder(NodeRef parentNodeRef, String folderPath) {

		// get translated message
		String folderName = getTranslatedPath(folderPath);
		if (folderName == null) {
			folderName = folderPath;
		}

		NodeRef folderNodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, folderName);
		if (folderNodeRef == null) {

			logger.debug("Create folder, path: " + folderPath + " - translatedName: " + folderName);

			Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
			properties.put(ContentModel.PROP_NAME, folderName);

			folderNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, folderPath),
					ContentModel.TYPE_FOLDER, properties).getChildRef();

		}

		visitRules(folderNodeRef, folderPath);
		visitFiles(folderNodeRef, folderPath);

		return folderNodeRef;
	}

	/**
	 * Add resources to folder
	 */
	protected void visitFiles(NodeRef folderNodeRef, String folderName) {

		if (folderName == PATH_CONFIGS) {
			addFilesResources(folderNodeRef, "classpath:beCPG/designer/extCustomForm.xml");
		}

	}

	private void addFilesResources(NodeRef folderNodeRef, String pattern) {
		try {

			PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

			for (Resource res : resolver.getResources(pattern)) {

				addFileResource(folderNodeRef, res, new HashMap<QName, Serializable>());
			}
		} catch (Exception e) {
			logger.error(e, e);
		}

	}

	private void addFileResource(NodeRef folderNodeRef, Resource res, Map<QName, Serializable> properties) {
		try {

			String fileName = res.getFilename();
			logger.debug("add file " + fileName);

			properties.put(ContentModel.PROP_NAME, fileName);

			NodeRef nodeRef = nodeService.getChildByName(folderNodeRef, ContentModel.ASSOC_CONTAINS, (String) properties.get(ContentModel.PROP_NAME));
			if (nodeRef == null) {
				nodeRef = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName((String) properties.get(ContentModel.PROP_NAME))),
						ContentModel.TYPE_CONTENT, properties).getChildRef();

				ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);

				InputStream in = res.getInputStream();
				writer.setMimetype(mimetypeService.guessMimetype(fileName));
				writer.putContent(in);
				in.close();

			}
		} catch (Exception e) {
			logger.error(e, e);
		}

	}

	@Override
	public void addReadOnlyDesignerFiles(String pattern) {

		try {

			PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

			for (Resource res : resolver.getResources(pattern)) {
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
				properties.put(DesignerModel.PROP_DSG_READ_ONLY_FILE, true);
				addFileResource(getModelsNodeRef(), res, properties);
			}
		} catch (Exception e) {
			logger.error(e, e);
		}

	}

	public String getTranslatedPath(String name) {

		String translation = I18NUtil.getMessage(PATH_MSG_PFX + name.toLowerCase(), Locale.getDefault());
		if (translation == null) {
			logger.error("Failed to translate path. path: " + name);
		}

		return translation;
	}

	public NodeRef getDictionnaryNodeRef() {
		return getFolderByPath(repository.getCompanyHome(), DATA_DICTIONNARY_PATH);
	}

	@Override
	public NodeRef getWorkflowsNodeRef() {

		return getFolderByPath(repository.getCompanyHome(), PATH_WORKFLOWS);
	}

	@Override
	public NodeRef getModelsNodeRef() {

		return getFolderByPath(repository.getCompanyHome(), PATH_MODELS);
	}

	@Override
	public NodeRef getConfigsNodeRef() {

		// Data dictionnary

		String folderName = getTranslatedPath(PATH_CONFIGS);
		if (folderName == null) {
			folderName = PATH_CONFIGS;
		}

		NodeRef configsNodeRef = nodeService.getChildByName(getDictionnaryNodeRef(), ContentModel.ASSOC_CONTAINS, folderName);

		if (configsNodeRef == null) {
			logger.debug("Starting designer initialization");
			configsNodeRef = visitFolder(getDictionnaryNodeRef(), PATH_CONFIGS);
			addAspectRule(getModelsNodeRef(), "Add model aspect", "Add model aspect to xml file", DesignerModel.ASPECT_MODEL);
			addFilesResources(getModelsNodeRef(), "classpath:beCPG/designer/extCustomModel.xml");
		}

		return configsNodeRef;
	}

}
