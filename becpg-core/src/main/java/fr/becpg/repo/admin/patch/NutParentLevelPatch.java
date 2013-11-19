/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package fr.becpg.repo.admin.patch;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * Add NutParentLevelPatch
 */
public class NutParentLevelPatch extends AbstractBeCPGPatch {

	private static Log logger = LogFactory.getLog(NutParentLevelPatch.class);

	private static final String MSG_SUCCESS = "patch.bcpg.nutParentLevelPatch.result";

	private BeCPGSearchService beCPGSearchService;

	private AssociationService associationService;

	private BehaviourFilter policyBehaviourFilter;

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	@Override
	protected String applyInternal() throws Exception {

		logger.info("Apply NutParentLevelPatch");

		try {
			policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);

			List<NodeRef> dataListNodeRefs = beCPGSearchService.luceneSearch("+TYPE:\"bcpg:nutList\" NOT ASPECT:\"bcpg:depthLevelAspect\" ");
			logger.info("EntitySortableListPatch add sort in bcpg:entityListItem, size: " + dataListNodeRefs.size());

			for (NodeRef dataListNodeRef : dataListNodeRefs) {
				if (nodeService.exists(dataListNodeRef)) {
					Map<QName, Serializable> properties = new HashMap<>();
					properties.put(BeCPGModel.PROP_DEPTH_LEVEL, 1);
					nodeService.addAspect(dataListNodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL, properties);
				} else {
					logger.warn("dataListNodeRef doesn't exist : " + dataListNodeRef);
				}
			}

			updateParents();

		} finally {
			policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);
		}

		return I18NUtil.getMessage(MSG_SUCCESS);
	}

	// Glucides
	// Sucres
	// Amidon
	// Polyols totaux

	// Lipides
	// AG saturés
	// AG monoinsaturés
	// AG polyinsaturés

	private void updateParents() {
		NodeRef parent = firstOrNull(beCPGSearchService.luceneSearch("+TYPE:\"bcpg:nut\" AND +@cm\\:name:\"Glucides\" "));
		List<NodeRef> childs = beCPGSearchService.luceneSearch("+TYPE:\"bcpg:nut\" AND( @cm\\:name:\"Sucre\" OR @cm\\:name:\"Amidon\" OR @cm\\:name:\"Polyols totaux\") ");

		updateParent(parent, childs);

		parent = firstOrNull(beCPGSearchService.luceneSearch("+TYPE:\"bcpg:nut\" AND +@cm\\:name:\"Lipides\" "));
		childs = beCPGSearchService.luceneSearch("+TYPE:\"bcpg:nut\" AND( @cm\\:name:\"AG saturés\" OR @cm\\:name:\"AG monoinsaturés\" OR @cm\\:name:\"AG polyinsaturés\") ");

		updateParent(parent, childs);

	}

	private NodeRef firstOrNull(List<NodeRef> nodeRefs) {
		return nodeRefs!=null && !nodeRefs.isEmpty() ? nodeRefs.get(0): null;
	}

	private void updateParent(NodeRef parent, List<NodeRef> childs) {
		if(parent!=null && childs!=null){
		List<NodeRef> parents = associationService.getSourcesAssocs(parent, BeCPGModel.ASSOC_NUTLIST_NUT);
		logger.info("Found " + parents.size() + " to check");

		for (NodeRef child : childs) {
			String nutName = (String) nodeService.getProperty(child, ContentModel.PROP_NAME);
			List<NodeRef> items = associationService.getSourcesAssocs(child, BeCPGModel.ASSOC_NUTLIST_NUT);
			logger.info("Look for nutList: " + nutName + " (" + items.size() + ")");
			for (NodeRef check : parents) {
				for (NodeRef item : items) {
					if (nodeService.getPrimaryParent(item).getParentRef().equals((nodeService.getPrimaryParent(check)).getParentRef())) {
						logger.info("Updating parent for nut" + nutName + " " + item + " with " + check);
						nodeService.setProperty(item, BeCPGModel.PROP_PARENT_LEVEL, check);
						nodeService.setProperty(item, BeCPGModel.PROP_DEPTH_LEVEL, 2);
					}
				}
			}

		}
		}
	}
}
