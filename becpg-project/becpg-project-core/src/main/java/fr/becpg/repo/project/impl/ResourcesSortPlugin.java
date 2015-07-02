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
package fr.becpg.repo.project.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.entity.datalist.DataListSortPlugin;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;

@Service
public class ResourcesSortPlugin implements DataListSortPlugin {

	private static final String PLUGIN_ID = "TaskResources";
	private static final Log logger = LogFactory.getLog(ResourcesSortPlugin.class); 
	
	@Autowired
	private AttributeExtractorService attributeExtractorService;
	@Autowired
	private AssociationService associationService;
	@Autowired
	private NodeService nodeService;

	@Override
	public String getPluginId() {
		return PLUGIN_ID;
	}

	public List<NodeRef> sort(List<NodeRef> taskList) {

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		Collections.sort(taskList, new Comparator<NodeRef>() {

			@Override
			public int compare(NodeRef n1, NodeRef n2) {

				int ret = compare(getResource(n1), getResource(n2));

				if (ret == EQUAL) {

					ret = compare((Date) nodeService.getProperty(n1, ProjectModel.PROP_TL_START),
							(Date) nodeService.getProperty(n2, ProjectModel.PROP_TL_START));
				}

				return ret;
			}

			private String getResource(NodeRef nodeRef) {

				NodeRef resourceNodeRef = associationService.getTargetAssoc(nodeRef, ProjectModel.ASSOC_TL_RESOURCES);

				return resourceNodeRef != null ? attributeExtractorService.extractPropName(resourceNodeRef) : null;

			}

			private int compare(String n1, String n2) {

				if (n1 != null && n2 != null) {
					if (n1.equals(n2)) {
						return EQUAL;
					}

					return n1.compareToIgnoreCase(n2);

				} else if (n1 == null && n2 != null) {
					return AFTER;
				} else if (n2 == null && n1 != null) {
					return BEFORE;
				} else {
					return EQUAL;
				}
			}

			private int compare(Date n1, Date n2) {

				if (n1 != null && n2 != null) {
					if (n1.equals(n2)) {
						return EQUAL;
					}

					return n1.compareTo(n2);

				} else if (n1 == null && n2 != null) {
					return AFTER;
				} else if (n2 == null && n1 != null) {
					return BEFORE;
				} else {
					return EQUAL;
				}
			}
		});

		if (logger.isDebugEnabled()) {
			watch.stop();
			logger.debug("Task List sorted in " + watch.getTotalTimeSeconds() + " seconds - size results " + taskList.size());
		}

		return taskList;
	}
}
