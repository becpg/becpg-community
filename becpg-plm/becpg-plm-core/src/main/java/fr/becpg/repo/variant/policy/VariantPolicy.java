/*******************************************************************************
 * Copyright (C) 2010-2018 beCPG. 
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.transaction.TransactionSupportUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.version.EntityVersionPlugin;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.search.BeCPGQueryBuilder;

public class VariantPolicy extends AbstractBeCPGPolicy implements CopyServicePolicies.OnCopyCompletePolicy, EntityVersionPlugin {

	private static final Log logger = LogFactory.getLog(VariantPolicy.class);

	private CopyService copyService;

	private EntityListDAO entityListDAO;
	
	public static final String KEY_QUEUE_VARIANT = "Variant_Item_";

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setCopyService(CopyService copyService) {
		this.copyService = copyService;
	}

	public void doInit() {
		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyCompletePolicy.QNAME, PLMModel.ASPECT_ENTITYLIST_VARIANT, new JavaBehaviour(this,
				"onCopyComplete"));
	}

	@Override
	public void onCopyComplete(QName classRef, final NodeRef sourceNodeRef, final NodeRef destinationRef, boolean copyToNewNode,
			Map<NodeRef, NodeRef> copyMap) {
		logger.debug("On copy complete Variant ");
		
		NodeRef targetEntityRef = entityListDAO.getEntity(destinationRef);
		
		queueNode(KEY_QUEUE_VARIANT + targetEntityRef.toString(), destinationRef);
	}

	@Override
	public void doAfterCheckout(NodeRef origNodeRef, final NodeRef workingCopyNodeRef) {

		if (nodeService.hasAspect(origNodeRef, PLMModel.ASPECT_ENTITY_VARIANT)) {
			logger.debug("On check out Variant");
			AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
				@Override
				public Void doWork() throws Exception {

					NodeRef origNodeRef = getCheckedOut(workingCopyNodeRef);
					
					// Copy variants

					List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(origNodeRef, PLMModel.ASSOC_VARIANTS,
							RegexQNamePattern.MATCH_ALL);
					for (ChildAssociationRef childAssoc : childAssocs) {
						if (logger.isDebugEnabled()) {
							logger.debug("Copy variant " + nodeService.getProperty(childAssoc.getChildRef(), ContentModel.PROP_NAME)
									+ " to workingCopy ");
						}

						copyService.copyAndRename(childAssoc.getChildRef(), workingCopyNodeRef, PLMModel.ASSOC_VARIANTS, QName.createQName(
								NamespaceService.CONTENT_MODEL_1_0_URI,
								QName.createValidLocalName((String) nodeService.getProperty(childAssoc.getChildRef(), ContentModel.PROP_NAME))),
								false);
					}

					return null;

				}
			});
		}
	}

	
	@Override
	public void doBeforeCheckin(NodeRef origNodeRef,final  NodeRef workingCopyNodeRef) {
		
		if (nodeService.hasAspect(origNodeRef, PLMModel.ASPECT_ENTITY_VARIANT) || nodeService.hasAspect(workingCopyNodeRef, PLMModel.ASPECT_ENTITY_VARIANT)) {
			
			logger.debug("On check in Variant");

			AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Void>() {
				@Override
				public Void doWork() throws Exception {
					NodeRef origNodeRef = getCheckedOut(workingCopyNodeRef);

					if (origNodeRef != null) {
						List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(origNodeRef, PLMModel.ASSOC_VARIANTS,
								RegexQNamePattern.MATCH_ALL);
						
						//On initial version while branch merging  
						if(childAssocs != null && !childAssocs.isEmpty()) {
							for(String key :  getKeyRegistry(KEY_REGISTRY)) {
								if (key.startsWith(KEY_QUEUE_VARIANT)){
									Set<NodeRef> pendingNodes = new HashSet<>();
									Set<NodeRef> tempPendingNodes = TransactionSupportUtil.getResource(key);
									if(tempPendingNodes != null) {
										pendingNodes.addAll(tempPendingNodes);
										
										if(pendingNodes != null && ! pendingNodes.isEmpty()) {
											updateVariantIds(key, pendingNodes, true);
										}
									}
									
								}
							}
						}
						
						for (ChildAssociationRef childAssoc : childAssocs) {
							if (logger.isDebugEnabled()) {
								logger.debug("Remove variant on OrigNode " + childAssoc.getChildRef() + " "
										+ nodeService.getProperty(childAssoc.getChildRef(), ContentModel.PROP_NAME) + " to origNode ");
							}
							nodeService.removeChildAssociation(childAssoc);
						}

						// move variants of working copy
						childAssocs = nodeService.getChildAssocs(workingCopyNodeRef, PLMModel.ASSOC_VARIANTS, RegexQNamePattern.MATCH_ALL);
						for (ChildAssociationRef childAssoc : childAssocs) {
							if (logger.isDebugEnabled()) {
								logger.debug("move variant of workfingCopy " + childAssoc.getChildRef() + " "
										+ nodeService.getProperty(childAssoc.getChildRef(), ContentModel.PROP_NAME) + " to origNode ");
							}
							nodeService.moveNode(childAssoc.getChildRef(), origNodeRef, PLMModel.ASSOC_VARIANTS, PLMModel.ASSOC_VARIANTS);
						}
					}

					return null;

				}
			});
		}
		
	}

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

	@Override
	protected boolean doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		logger.debug("On before commit");
		
		if(pendingNodes != null && !pendingNodes.isEmpty()) {
			updateVariantIds(key, pendingNodes, false);
		}
		return false;
	}
	
	
	private void updateVariantIds(String key, Set<NodeRef> pendingNodes, boolean unQueueNode ){
		
		logger.debug("Pending nodes of "+ key + " : "+pendingNodes);
		
		NodeRef targetEntityRef = entityListDAO.getEntity(pendingNodes.iterator().next());
		if(!pendingNodes.isEmpty()) {
			
			List<NodeRef> result = BeCPGQueryBuilder.createQuery().ofType(PLMModel.TYPE_VARIANT).parent(targetEntityRef).inDB().list();
			
			Map<NodeRef, String> targetEntityVariants = new HashMap<>();
			
			result.forEach((variantRef) -> {
				targetEntityVariants.put(variantRef, (String)nodeService.getProperty(variantRef, ContentModel.PROP_NAME));
			});
			
			if (logger.isDebugEnabled()) {
				logger.debug("Search variant of : " + targetEntityRef);
			}
			
			for(NodeRef itemTargetRef : pendingNodes) {
				if(unQueueNode) {
					logger.info("unQueue Node : "+itemTargetRef);
					unQueueNode(key, itemTargetRef);
				}
				
				@SuppressWarnings("unchecked")
				List<NodeRef> itemVariantIds = (List<NodeRef>) nodeService.getProperty(itemTargetRef, PLMModel.PROP_VARIANTIDS);
				
				
				if(itemVariantIds != null) {
					Map<NodeRef, String> originVariantIds =  new HashMap<>();
					itemVariantIds.forEach((variantRef) -> {
						originVariantIds.put(variantRef, (String)nodeService.getProperty(variantRef, ContentModel.PROP_NAME));
					});
					List<NodeRef> newVariantIds = new ArrayList<>();
					
					for(NodeRef variantId  : itemVariantIds) {
						String variantName = originVariantIds.get(variantId);
						NodeRef newVariantRef = null; 

						if(targetEntityVariants.containsValue(variantName)) {
							newVariantRef = getKeyByValue(targetEntityVariants, variantName);
							if (logger.isDebugEnabled()) {
								logger.debug("Replace variant : " + variantId +" by : " + newVariantRef);
							}
						
						} else {
							if (logger.isDebugEnabled()) {
								logger.debug("Create variant : "+ variantName );
							}
							
							Map<QName, Serializable> props = new HashMap<>();
							props.put(ContentModel.PROP_NAME, variantName);
							props.put(PLMModel.PROP_IS_DEFAULT_VARIANT, nodeService.getProperty(variantId, PLMModel.PROP_IS_DEFAULT_VARIANT));
							newVariantRef =  nodeService.createNode(targetEntityRef, PLMModel.ASSOC_VARIANTS, PLMModel.ASSOC_VARIANTS, PLMModel.TYPE_VARIANT, props).getChildRef();
							targetEntityVariants.put(newVariantRef, variantName);
						}
						if(newVariantRef != null) {
							newVariantIds.add(newVariantRef);
						}
					}
					nodeService.setProperty(itemTargetRef, PLMModel.PROP_VARIANTIDS, (Serializable) newVariantIds);
				}
			}
			
		}
	}
	
	
	private <K, V> K getKeyByValue(Map<K, V> map, V value) {
	    for (Entry<K, V> entry : map.entrySet()) {
	        if (entry.getValue().equals(value)) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}
	
	
	@Override
	public void cancelCheckout(NodeRef origNodeRef, NodeRef workingCopyNodeRef) {
		// Do nothing
	}

	@Override
	public void impactWUsed(NodeRef entityNodeRef, VersionType versionType, String description) {
		// Do nothing
	}

	

}
