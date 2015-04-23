/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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
package fr.becpg.repo.project.extractor;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.DataListPagination;
import fr.becpg.repo.entity.datalist.impl.SimpleExtractor;
import fr.becpg.repo.helper.AttributeExtractorService.AttributeExtractorMode;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;
import fr.becpg.repo.project.ProjectActivityService;
import fr.becpg.repo.project.data.projectList.ActivityType;

/**
 * 
 * @author matthieu Extract activity Fields
 */
public class ActivityListExtractor extends SimpleExtractor {

	private static final String ACTIVITY_LIST = "activityList";

	private ProjectActivityService projectActivityService;
	
	public void setProjectActivityService(ProjectActivityService projectActivityService) {
		this.projectActivityService = projectActivityService;
	}

	@Override
	protected List<NodeRef> getListNodeRef(DataListFilter dataListFilter, DataListPagination pagination) {

		Map<String, Boolean> sortMap = new LinkedHashMap<String, Boolean>();

		sortMap.put("@cm:created", false);

		dataListFilter.setSortMap(sortMap);

		return super.getListNodeRef(dataListFilter, pagination);
	}
	
	
	@Override
	public PaginatedExtractedItems extract(DataListFilter dataListFilter, List<String> metadataFields, DataListPagination pagination,
			boolean hasWriteAccess) {
		return super.extract(dataListFilter, metadataFields, pagination, false);
	}

	@Override
	protected Map<String, Object> doExtract(NodeRef nodeRef, QName itemType, List<AttributeExtractorStructure> metadataFields,
			AttributeExtractorMode mode, Map<QName, Serializable> properties, Map<String, Object> props, Map<NodeRef, Map<String, Object>> cache) {
		Map<String, Object> ret = super.doExtract(nodeRef, itemType, metadataFields, mode, properties, props, cache);
		if(ProjectModel.TYPE_ACTIVITY_LIST.equals(itemType)){
			postLookupActivity(ret,properties);
		}

		return ret;

	}
	
	protected void postLookupActivity(Map<String, Object> ret, Map<QName, Serializable> properties) {
		ret.put("prop_pjt_alUserId", extractPerson((String) properties.get(ProjectModel.PROP_ACTIVITYLIST_USERID)));
		ret.put("prop_pjt_alData", projectActivityService.postActivityLookUp(
				ActivityType.valueOf((String) properties.get(ProjectModel.PROP_ACTIVITYLIST_TYPE)),
				(String)properties.get(ProjectModel.PROP_ACTIVITYLIST_DATA)));
		
	}

	@Override
	public boolean applyTo(DataListFilter dataListFilter) {
		return dataListFilter.getDataListName() != null && dataListFilter.getDataListName().equals(ACTIVITY_LIST);
	}

	@Override
	public boolean hasWriteAccess() {
		return false;
	}

}
