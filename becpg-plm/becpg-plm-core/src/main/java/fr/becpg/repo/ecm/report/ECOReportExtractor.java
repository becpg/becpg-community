/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG. 
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
package fr.becpg.repo.ecm.report;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ECMModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.ecm.data.ChangeOrderData;
import fr.becpg.repo.ecm.data.dataList.SimulationListDataItem;
import fr.becpg.repo.hierarchy.HierarchyHelper;
import fr.becpg.repo.report.entity.EntityReportData;
import fr.becpg.repo.report.entity.EntityReportExtractorPlugin;
import fr.becpg.repo.repository.AlfrescoRepository;

@Service
public class ECOReportExtractor implements EntityReportExtractorPlugin {

	private static final String TAG_ECO = "eco";
	private static final String TAG_CALCULATED_CHARACTS = "calculatedCharacts";
	private static final String TAG_CALCULATED_CHARACT = "calculatedCharact";
	private static final String ATTR_SOURCEITEM_HIERARCHY1 = "sourceItemHierarchy1";
	private static final String ATTR_SOURCEITEM_HIERARCHY2 = "sourceItemHierarchy2";
	private static final String ATTR_SOURCEITEM_NAME = "sourceItemName";
	private static final String ATTR_SOURCEITEM_CODE = "sourceItemCode";
	private static final String ATTR_CHARACT_NAME = "charactName";
	private static final String ATTR_SOURCE_VALUE = "sourceValue";
	private static final String ATTR_TARGET_VALUE = "targetValue";
	private static final String ATTR_IS_COST = "isCost";

	private static final Integer DEFAULT_PROJECTED_QTY = 1;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private AlfrescoRepository<ChangeOrderData> alfrescoRepository;

	
	@Override
	public EntityReportData extract(NodeRef entityNodeRef, Map<String,String> preferences) {

		EntityReportData ret = new EntityReportData();

		ChangeOrderData ecoData = alfrescoRepository.findOne(entityNodeRef);

		// Prepare data source
		Element xmlDataSource = extractXml(ecoData);

		ret.setXmlDataSource(xmlDataSource);

		return ret;

	}

	private Element extractXml(ChangeOrderData ecoData) {

		Document document = DocumentHelper.createDocument();
		Element ecoElt = document.addElement(TAG_ECO);
		ecoElt.addAttribute(ContentModel.PROP_NAME.getLocalName(), ecoData.getName());
		ecoElt.addAttribute(BeCPGModel.PROP_CODE.getLocalName(), ecoData.getCode());
		Element calculatedCharactsElt = ecoElt.addElement(TAG_CALCULATED_CHARACTS);

		for (SimulationListDataItem sl : ecoData.getSimulationList()) {

			Element calculatedCharactElt = calculatedCharactsElt.addElement(TAG_CALCULATED_CHARACT);

			Boolean isCost = Boolean.FALSE;
			QName charactQName = nodeService.getType(sl.getCharact());
			if (charactQName != null && charactQName.isMatch(PLMModel.TYPE_COST)) {
				isCost = Boolean.TRUE;
			}

			Serializable projectedQty = nodeService.getProperty(sl.getSourceItem(), PLMModel.PROP_PROJECTED_QTY);
			if (projectedQty == null) {
				projectedQty = DEFAULT_PROJECTED_QTY;
			}

			calculatedCharactElt.addAttribute(ATTR_SOURCEITEM_HIERARCHY1,
					HierarchyHelper.getHierachyName((NodeRef) nodeService.getProperty(sl.getSourceItem(), PLMModel.PROP_PRODUCT_HIERARCHY1), nodeService));
			calculatedCharactElt.addAttribute(ATTR_SOURCEITEM_HIERARCHY2,
					HierarchyHelper.getHierachyName((NodeRef) nodeService.getProperty(sl.getSourceItem(), PLMModel.PROP_PRODUCT_HIERARCHY2), nodeService));
			calculatedCharactElt.addAttribute(ATTR_SOURCEITEM_CODE, (String) nodeService.getProperty(sl.getSourceItem(), BeCPGModel.PROP_CODE));
			calculatedCharactElt.addAttribute(ATTR_SOURCEITEM_NAME, (String) nodeService.getProperty(sl.getSourceItem(), ContentModel.PROP_NAME));
			calculatedCharactElt.addAttribute(ATTR_CHARACT_NAME, (String) nodeService.getProperty(sl.getCharact(), ContentModel.PROP_NAME));
			calculatedCharactElt.addAttribute(ATTR_SOURCE_VALUE, sl.getSourceValue() == null ? "" : sl.getSourceValue().toString());
			calculatedCharactElt.addAttribute(ATTR_TARGET_VALUE, sl.getTargetValue() == null ? "" : sl.getTargetValue().toString());
			calculatedCharactElt.addAttribute(PLMModel.PROP_PROJECTED_QTY.getLocalName(), projectedQty.toString());
			calculatedCharactElt.addAttribute(ATTR_IS_COST, isCost.toString());

		}

		return ecoElt;
	}

	@Override
	public boolean shouldGenerateReport(NodeRef entityNodeRef, Date generatedReportDate) {
		return false;
	}


	@Override
	public EntityReportExtractorPriority getMatchPriority(QName type) {
		return ECMModel.TYPE_ECO.equals(type) ? EntityReportExtractorPriority.NORMAL: EntityReportExtractorPriority.NONE;
	}
}
