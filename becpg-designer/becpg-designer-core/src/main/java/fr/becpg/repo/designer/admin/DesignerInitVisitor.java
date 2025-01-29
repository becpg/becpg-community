/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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
package fr.becpg.repo.designer.admin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.CompareMimeTypeEvaluator;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.evaluator.compare.ComparePropertyValueOperation;
import org.alfresco.repo.action.executer.AddFeaturesActionExecuter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.admin.impl.AbstractInitVisitorImpl;
import fr.becpg.repo.designer.DesignerModel;
import fr.becpg.repo.helper.ContentHelper;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * Init designer files and folders
 *
 * @author "Matthieu Laborie"
 * @version $Id: $Id
 */
@Service
public class DesignerInitVisitor extends AbstractInitVisitorImpl {

	private static final String PATH_CONFIGS = "configs";

	private static final String XPATH_DICTIONARY = "./app:dictionary";

	private static final String PATH_MODELS = "../app:models";

	@Autowired
	private ContentHelper contentHelper;

	/** {@inheritDoc} */
	@Override
	public List<SiteInfo> visitContainer(NodeRef companyHome) {

		logger.info("Run DesignerInitVisitor");

		// System
		NodeRef dictionaryNodeRef = BeCPGQueryBuilder.createQuery().selectNodeByPath(companyHome, XPATH_DICTIONARY);

		// Security
		visitFolder(dictionaryNodeRef, PATH_CONFIGS);

		return new ArrayList<>();

	}

	/** {@inheritDoc} */
	@Override
	protected void visitRules(NodeRef nodeRef, String folderName) {
		if (Objects.equals(folderName, PATH_CONFIGS)) {
			addAspectRule(nodeRef, "Add config aspect", "Add config aspect to xml file", DesignerModel.ASPECT_CONFIG, MimetypeMap.MIMETYPE_XML,
					".xml");
			addAspectRule(nodeRef, "Add config aspect", "Add config aspect to properties file", DesignerModel.ASPECT_CONFIG,
					MimetypeMap.MIMETYPE_TEXT_PLAIN, ".properties");
			addAspectRule(getModelNodeRef(nodeRef), "Add model aspect", "Add model aspect to xml file", DesignerModel.ASPECT_MODEL,
					MimetypeMap.MIMETYPE_XML, ".xml");
		}
	}

	private NodeRef getModelNodeRef(NodeRef configNodeRef) {
		return BeCPGQueryBuilder.createQuery().selectNodeByPath(configNodeRef, PATH_MODELS);
	}

	/** {@inheritDoc} */
	@Override
	protected void visitFiles(NodeRef folderNodeRef, String folderName, boolean folderExists) {
		if (Objects.equals(folderName, PATH_CONFIGS)) {
			contentHelper.addFilesResources(folderNodeRef, "classpath:beCPG/designer/extCustomForm.xml");
			contentHelper.addFilesResources(getModelNodeRef(folderNodeRef), "classpath:beCPG/designer/extCustomModel.xml");
		}
	}

	/**
	 * 
	 * @param nodeRef
	 * @param ruleName
	 * @param ruleDescription
	 * @param aspectModel
	 * @param mimeType
	 * @param nameExtension
	 */
	private void addAspectRule(NodeRef nodeRef, String ruleName, String ruleDescription, QName aspectModel, String mimeType, String nameExtension) {

		// action
		CompositeAction compositeAction = actionService.createCompositeAction();
		Map<String, Serializable> params = new HashMap<>();
		params.put(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, aspectModel);
		Action action = actionService.createAction(AddFeaturesActionExecuter.NAME, params);
		compositeAction.addAction(action);

		// compare-mime-type == text/xml
		ActionCondition conditionOnMimeType = actionService.createActionCondition(CompareMimeTypeEvaluator.NAME);
		conditionOnMimeType.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, mimeType);
		conditionOnMimeType.setParameterValue(ComparePropertyValueEvaluator.PARAM_PROPERTY, ContentModel.PROP_CONTENT);
		conditionOnMimeType.setInvertCondition(false);
		compositeAction.addActionCondition(conditionOnMimeType);

		// compare-name == *.xml
		ActionCondition conditionOnName = actionService.createActionCondition(ComparePropertyValueEvaluator.NAME);
		conditionOnName.setParameterValue(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.ENDS.toString());
		conditionOnName.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, nameExtension);
		conditionOnName.setParameterValue(ComparePropertyValueEvaluator.PARAM_PROPERTY, ContentModel.PROP_NAME);
		conditionOnName.setInvertCondition(false);
		compositeAction.addActionCondition(conditionOnName);

		createRule(nodeRef, ruleName, ruleDescription, true, compositeAction);

	}

	/** {@inheritDoc} */
	@Override
	public Integer initOrder() {
		return 1;
	}

}
