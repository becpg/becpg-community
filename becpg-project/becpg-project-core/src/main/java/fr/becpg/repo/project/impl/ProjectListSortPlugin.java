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
package fr.becpg.repo.project.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.entity.datalist.DataListSortPlugin;
import fr.becpg.repo.hierarchy.HierarchyHelper;

/**
 * <p>ProjectListSortPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class ProjectListSortPlugin implements DataListSortPlugin {

	private static final String PLUGIN_ID = "ProjectList";
	private static final Log logger = LogFactory.getLog(ProjectListSortPlugin.class);

	@Autowired
	private NodeService nodeService;

	/** {@inheritDoc} */
	@Override
	public String getPluginId() {
		return PLUGIN_ID;
	}

	/** {@inheritDoc} */
	@Override
	public List<NodeRef> sort(List<NodeRef> projectList, Map<String, Boolean> sortMap) {

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		boolean dir = false;
		QName prop = ContentModel.PROP_CREATED;

		if (!sortMap.isEmpty()) {
			Map.Entry<String, Boolean> kv = sortMap.entrySet().iterator().next();

			if (!"@bcpg:sort".equals(kv.getKey())) {
				dir = kv.getValue();
				prop = QName.createQName(kv.getKey().replaceFirst("@", ""));
			}
		}

		final boolean sortDir = dir;
		final QName sortProp = prop;

		Collections.sort(projectList, new Comparator<NodeRef>() {

			@Override
			public int compare(NodeRef n1, NodeRef n2) {

				int comp = compare(getHierarchy(n1, ProjectModel.PROP_PROJECT_HIERARCHY1), getHierarchy(n2, ProjectModel.PROP_PROJECT_HIERARCHY1));

				if (EQUAL == comp) {
					comp = compare(getHierarchy(n1, ProjectModel.PROP_PROJECT_HIERARCHY2), getHierarchy(n2, ProjectModel.PROP_PROJECT_HIERARCHY2));

					if (EQUAL == comp) {
						if (sortDir) {
							comp = compare(extract(nodeService.getProperty(n1, sortProp)), extract(nodeService.getProperty(n2, sortProp)));
						} else {
							comp = compare(extract(nodeService.getProperty(n2, sortProp)), extract(nodeService.getProperty(n1, sortProp)));
						}
					}
				}

				return comp;
			}

			private Comparable<?> extract(Serializable property) {
				if (property != null) {
					if (property instanceof Comparable) {
						return (Comparable<?>) property;
					}
					return property.toString();
				}
				return null;
			}

			private String getHierarchy(NodeRef nodeRef, QName hierarchyQName) {

				NodeRef hierarchyNodeRef = (NodeRef) nodeService.getProperty(nodeRef, hierarchyQName);
				return HierarchyHelper.getHierachyName(hierarchyNodeRef, nodeService);
			}

			@SuppressWarnings({ "rawtypes", "unchecked" })
			private int compare(Comparable n1, Comparable n2) {

				if ((n1 != null) && (n2 != null)) {
					if (n1.equals(n2)) {
						return EQUAL;
					}

					return n1.compareTo(n2);

				} else if ((n1 == null) && (n2 != null)) {
					return AFTER;
				} else if (n1 != null) {
					return BEFORE;
				} else {
					return EQUAL;
				}
			}
		});

		if (logger.isDebugEnabled() && (watch != null)) {
			watch.stop();
			logger.debug("Project List sorted in " + watch.getTotalTimeSeconds() + " seconds - size results " + projectList.size());
		}

		return projectList;
	}
}
