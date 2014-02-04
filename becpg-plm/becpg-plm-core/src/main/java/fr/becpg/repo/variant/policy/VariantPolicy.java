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
package fr.becpg.repo.variant.policy;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.search.BeCPGSearchService;

public class VariantPolicy extends AbstractBeCPGPolicy implements CopyServicePolicies.OnCopyCompletePolicy, CheckOutCheckInServicePolicies.OnCheckOut,
		CheckOutCheckInServicePolicies.BeforeCheckIn {

	private static Log logger = LogFactory.getLog(VariantPolicy.class);

	private BeCPGSearchService beCPGSearchService;

	private CopyService copyService;

	private EntityListDAO entityListDAO;

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	public void setCopyService(CopyService copyService) {
		this.copyService = copyService;
	}

	public void doInit() {

		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyCompletePolicy.QNAME, PLMModel.TYPE_VARIANT, new JavaBehaviour(this, "onCopyComplete"));

		policyComponent.bindClassBehaviour(CheckOutCheckInServicePolicies.OnCheckOut.QNAME, PLMModel.ASPECT_ENTITY_VARIANT, new JavaBehaviour(this, "onCheckOut"));
		policyComponent.bindClassBehaviour(CheckOutCheckInServicePolicies.BeforeCheckIn.QNAME, PLMModel.ASPECT_ENTITY_VARIANT, new JavaBehaviour(this, "beforeCheckIn"));		
	}

	@Override
	public void onCopyComplete(QName classRef, final NodeRef sourceNodeRef, final NodeRef destinationRef, boolean copyToNewNode, Map<NodeRef, NodeRef> copyMap) {
		logger.debug("On copy complete Variant");

		NodeRef entityNodeRef = nodeService.getPrimaryParent(destinationRef).getParentRef();

		String query = LuceneHelper.mandatory(LuceneHelper.getCondAspect(PLMModel.ASPECT_ENTITYLIST_VARIANT));
		query += LuceneHelper.getCondEqualValue(PLMModel.PROP_VARIANTIDS, sourceNodeRef.toString(), LuceneHelper.Operator.AND);

		if (logger.isDebugEnabled()) {
			logger.debug("Entity of destination " + nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME) + " " + entityNodeRef + 
					" variant: " + nodeService.getProperty(sourceNodeRef, ContentModel.PROP_NAME));
			logger.debug("Search for" + query);
		}

		List<NodeRef> result = beCPGSearchService.luceneSearch(query, RepoConsts.MAX_RESULTS_UNLIMITED);

		if (!result.isEmpty()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Found " + result.size() + " entityDataList to check for update variant");
			}
			for (NodeRef entityDataListNodeRef : result) {
				NodeRef tmpNodeRef = entityListDAO.getEntity(entityDataListNodeRef);
				logger.debug("Check is " + tmpNodeRef + "is entity");
				if (entityNodeRef.equals(tmpNodeRef)) {
					logger.debug("Ok for replacement");
					@SuppressWarnings("unchecked")
					List<NodeRef> variantIds = (List<NodeRef>) nodeService.getProperty(entityDataListNodeRef, PLMModel.PROP_VARIANTIDS);
					variantIds.remove(sourceNodeRef);
					variantIds.add(destinationRef);
					
					if (logger.isDebugEnabled()) {
						logger.debug("entityDataListNodeRef " + entityDataListNodeRef);
						logger.debug("VariantIds remove " + sourceNodeRef + " " + nodeService.getProperty(sourceNodeRef, ContentModel.PROP_NAME));
						logger.debug("VariantIds add " + destinationRef + " " + nodeService.getProperty(destinationRef, ContentModel.PROP_NAME));
					}

					nodeService.setProperty(entityDataListNodeRef, PLMModel.PROP_VARIANTIDS, (Serializable) variantIds);
				}
			}
		}
	}

	@Override
	public void onCheckOut(final NodeRef workingCopyNodeRef) {
		logger.debug("On check out Variant");

		AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {
			@Override
			public Void doWork() throws Exception {

				NodeRef origNodeRef = getCheckedOut(workingCopyNodeRef);

				// Copy variants

				List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(origNodeRef, PLMModel.ASSOC_VARIANTS, RegexQNamePattern.MATCH_ALL);
				for (ChildAssociationRef childAssoc : childAssocs) {
					if (logger.isDebugEnabled()) {
						logger.debug("Copy variant " + nodeService.getProperty(childAssoc.getChildRef(), ContentModel.PROP_NAME) + " to workingCopy ");
					}

					copyService.copyAndRename(
							childAssoc.getChildRef(),
							workingCopyNodeRef,
							PLMModel.ASSOC_VARIANTS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
									QName.createValidLocalName((String) nodeService.getProperty(childAssoc.getChildRef(), ContentModel.PROP_NAME))), false);
				}

				return null;

			}
		}, AuthenticationUtil.getSystemUserName());

	}

	@Override
	public void beforeCheckIn(final NodeRef workingCopyNodeRef, Map<String, Serializable> versionProperties, String contentUrl, boolean keepCheckedOut) {
		logger.debug("On check in Variant");
		
		AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {
			@Override
			public Void doWork() throws Exception {
				NodeRef origNodeRef = getCheckedOut(workingCopyNodeRef);

				if (origNodeRef != null) {
					List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(origNodeRef, PLMModel.ASSOC_VARIANTS, RegexQNamePattern.MATCH_ALL);
					for (ChildAssociationRef childAssoc : childAssocs) {
						if (logger.isDebugEnabled()) {
							logger.debug("Remove variant on OrigNode" + childAssoc.getChildRef() + " " + nodeService.getProperty(childAssoc.getChildRef(), ContentModel.PROP_NAME) + " to origNode ");
						}
						nodeService.removeChildAssociation(childAssoc);
					}
					
					// move variants of working copy
					childAssocs = nodeService.getChildAssocs(workingCopyNodeRef, PLMModel.ASSOC_VARIANTS, RegexQNamePattern.MATCH_ALL);
					for (ChildAssociationRef childAssoc : childAssocs) {
						if (logger.isDebugEnabled()) {
							logger.debug("move variant of workfingCopy" + childAssoc.getChildRef() + " " + nodeService.getProperty(childAssoc.getChildRef(), ContentModel.PROP_NAME) + " to origNode ");
						}
						nodeService.moveNode(childAssoc.getChildRef(), origNodeRef, PLMModel.ASSOC_VARIANTS, PLMModel.ASSOC_VARIANTS);
					}
				}

				return null;

			}
		}, AuthenticationUtil.getSystemUserName());
	}
	
	//TODO on checkout update variant
	

	private NodeRef getCheckedOut(NodeRef nodeRef) {
		NodeRef original = null;
		if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)) {
			List<AssociationRef> assocs = nodeService.getSourceAssocs(nodeRef, ContentModel.ASSOC_WORKING_COPY_LINK);
			// It is a 1:1 relationship
			if (!assocs.isEmpty()) {
				if (logger.isWarnEnabled()) {
					if (assocs.size() > 1) {
						logger.warn("Found multiple " + ContentModel.ASSOC_WORKING_COPY_LINK + " associations to node: " + nodeRef);
					}
				}
				original = assocs.get(0).getSourceRef();
			} else {
				logger.warn("No working copy link found");
			}
		} else {
			logger.warn("Node is not a working copy");
		}

		return original;
	}
}
