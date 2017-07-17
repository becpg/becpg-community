/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG. 
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
package fr.becpg.repo.quality.report;

import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.config.format.PropertyFormats;
import fr.becpg.model.QualityModel;
import fr.becpg.repo.quality.data.NonConformityData;
import fr.becpg.repo.quality.data.dataList.WorkLogDataItem;
import fr.becpg.repo.report.entity.impl.DefaultEntityReportExtractor;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;

@Service
public class NonConformityReportExtractorPlugin extends DefaultEntityReportExtractor {

	protected static final String TAG_WORKLOG = "workLog";
	protected static final String TAG_WORK = "work";

	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	/**
	 * load the datalists of the product data.
	 * 
	 * @param productData
	 *            the product data
	 * @param dataListsElt
	 *            the data lists elt
	 * @return the element
	 */
	@Override
	protected void loadDataLists(NodeRef entityNodeRef, Element dataListsElt, Map<String, byte[]> images) {

		PropertyFormats propertyFormats = new PropertyFormats(true);
		NonConformityData ncData = (NonConformityData) alfrescoRepository.findOne(entityNodeRef);

		// workLog
		if (ncData.getWorkLog() != null) {
			Element workLogElt = dataListsElt.addElement(TAG_WORKLOG);

			for (WorkLogDataItem dataItem : ncData.getWorkLog()) {

				Element workElt = workLogElt.addElement(TAG_WORK);
				workElt.addAttribute(QualityModel.PROP_WL_STATE.getLocalName(), dataItem.getState());
				workElt.addAttribute(QualityModel.PROP_WL_COMMENT.getLocalName(), dataItem.getComment());
				workElt.addAttribute(ContentModel.PROP_CREATOR.getLocalName(), dataItem.getCreator());
				workElt.addAttribute(ContentModel.PROP_CREATED.getLocalName(), attributeExtractorService.getStringValue(
						dictionaryService.getProperty(ContentModel.PROP_CREATED), dataItem.getCreated(), propertyFormats));
			}
		}
	}

	@Override
	protected boolean isMultiLinesAttribute(QName attribute) {
		return false;
	}
	
	@Override
	public EntityReportExtractorPriority getMatchPriority(QName type) {
		return QualityModel.TYPE_NC.equals(type) ? EntityReportExtractorPriority.NORMAL: EntityReportExtractorPriority.NONE;
	}

}
