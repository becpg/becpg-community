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
package fr.becpg.repo.variant.policy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
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

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.version.EntityVersionPlugin;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>VariantPolicy class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class VariantPolicy extends AbstractBeCPGPolicy implements CopyServicePolicies.OnCopyCompletePolicy, EntityVersionPlugin {

	private static final Log logger = LogFactory.getLog(VariantPolicy.class);

	private CopyService copyService;

	private EntityListDAO entityListDAO;
	/** Constant <code>KEY_QUEUE_VARIANT="Variant_Item_"</code> */
	public static final String KEY_QUEUE_VARIANT = "Variant_Item_";

	/**
	 * <p>Setter for the field <code>entityListDAO</code>.</p>
	 *
	 * @param entityListDAO a {@link fr.becpg.repo.entity.EntityListDAO} object.
	 */
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	/**
	 * <p>Setter for the field <code>copyService</code>.</p>
	 *
	 * @param copyService a {@link org.alfresco.service.cmr.repository.CopyService} object.
	 */
	public void setCopyService(CopyService copyService) {
		this.copyService = copyService;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>doInit.</p>
	 */
	@Override
	public void doInit() {
		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyCompletePolicy.QNAME, BeCPGModel.ASPECT_ENTITYLIST_VARIANT,
				new JavaBehaviour(this, "onCopyComplete"));
	}

	/** {@inheritDoc} */
	@Override
	public void onCopyComplete(QName classRef, final NodeRef sourceNodeRef, final NodeRef destinationRef, boolean copyToNewNode,
			Map<NodeRef, NodeRef> copyMap) {
		logger.debug("On copy complete Variant ");
		NodeRef targetEntityRef = entityListDAO.getEntity(destinationRef);
		queueNode(KEY_QUEUE_VARIANT + targetEntityRef.toString(), destinationRef);
	}

	/** {@inheritDoc} */
	@Override
	public void doAfterCheckout(NodeRef origNodeRef, final NodeRef workingCopyNodeRef) {

		if (nodeService.hasAspect(origNodeRef, BeCPGModel.ASPECT_ENTITY_VARIANT)) {
			logger.info("On check out Variant");
			AuthenticationUtil.runAsSystem(() -> {

				// Copy variants

				List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(origNodeRef, BeCPGModel.ASSOC_VARIANTS,
						RegexQNamePattern.MATCH_ALL);
				for (ChildAssociationRef childAssoc : childAssocs) {
					if (logger.isDebugEnabled()) {
						logger.debug(
								"Copy variant " + nodeService.getProperty(childAssoc.getChildRef(), ContentModel.PROP_NAME) + " to workingCopy ");
					}

					copyService.copyAndRename(childAssoc.getChildRef(), workingCopyNodeRef, BeCPGModel.ASSOC_VARIANTS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
									QName.createValidLocalName((String) nodeService.getProperty(childAssoc.getChildRef(), ContentModel.PROP_NAME))),
							false);
				}

				return null;

			});
		}
	}

	/** {@inheritDoc} */
	@Override
	public void doBeforeCheckin(NodeRef origNodeRef, final NodeRef workingCopyNodeRef) {

		final NodeRef finalOrigNode = origNodeRef;

		if (nodeService.hasAspect(origNodeRef, BeCPGModel.ASPECT_ENTITY_VARIANT)
				|| nodeService.hasAspect(workingCopyNodeRef, BeCPGModel.ASPECT_ENTITY_VARIANT)) {

			logger.debug("On check in Variant");

			AuthenticationUtil.runAsSystem(() -> {

				if (finalOrigNode != null) {
					List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(finalOrigNode, BeCPGModel.ASSOC_VARIANTS,
							RegexQNamePattern.MATCH_ALL);

					//On initial version while branch merging
					if ((childAssocs != null) && !childAssocs.isEmpty()) {
						for (String key : new HashSet<>(getKeyRegistry(KEY_REGISTRY))) {
							if (key.startsWith(KEY_QUEUE_VARIANT)) {
								Set<NodeRef> pendingNodes = new HashSet<>();
								Set<NodeRef> tempPendingNodes = TransactionSupportUtil.getResource(key);
								if (tempPendingNodes != null) {
									pendingNodes.addAll(tempPendingNodes);

									if ((pendingNodes != null) && !pendingNodes.isEmpty()) {
										updateVariantIds(key, pendingNodes, true);
									}
								}

							}
						}
					}

					if (childAssocs != null) {
						for (ChildAssociationRef childAssoc1 : childAssocs) {
							if (logger.isDebugEnabled()) {
								logger.debug("Remove variant on OrigNode " + childAssoc1.getChildRef() + " "
										+ nodeService.getProperty(childAssoc1.getChildRef(), ContentModel.PROP_NAME) + " to origNode ");
							}
							nodeService.removeChildAssociation(childAssoc1);
						}
					}

					// move variants of working copy
					childAssocs = nodeService.getChildAssocs(workingCopyNodeRef, BeCPGModel.ASSOC_VARIANTS, RegexQNamePattern.MATCH_ALL);
					for (ChildAssociationRef childAssoc2 : childAssocs) {
						if (logger.isDebugEnabled()) {
							logger.debug("move variant of workfingCopy " + childAssoc2.getChildRef() + " "
									+ nodeService.getProperty(childAssoc2.getChildRef(), ContentModel.PROP_NAME) + " to origNode ");
						}
						nodeService.moveNode(childAssoc2.getChildRef(), finalOrigNode, BeCPGModel.ASSOC_VARIANTS, BeCPGModel.ASSOC_VARIANTS);
					}
				}

				return null;

			});
		}

	}

	/** {@inheritDoc} */
	@Override
	protected boolean doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		logger.debug("On before commit");

		if ((pendingNodes != null) && !pendingNodes.isEmpty()) {
			updateVariantIds(key, pendingNodes, false);
		}
		return false;
	}

	private void updateVariantIds(String key, Set<NodeRef> pendingNodes, boolean unQueueNode) {

		logger.debug("Pending nodes of " + key + " : " + pendingNodes);

		if (!pendingNodes.isEmpty()) {

			NodeRef nodeRef = pendingNodes.stream().filter(n -> nodeService.exists(n)).findFirst().orElse(null);
			if (nodeRef != null) {
				NodeRef entityNodeRef = entityListDAO.getEntity(nodeRef);
				List<NodeRef> entityVariants = BeCPGQueryBuilder.createQuery().ofType(BeCPGModel.TYPE_VARIANT).parent(entityNodeRef).inDB().list();
				
				Map<String, NodeRef> entityVariantsMap = entityVariants.stream().collect(Collectors.toMap(v -> (String) nodeService.getProperty(v, ContentModel.PROP_NAME), v -> v));
				if (logger.isDebugEnabled()) {
					logger.debug("Search variant of : " + entityNodeRef);
				}
				
				for (NodeRef itemNodeRef : pendingNodes) {
					if (nodeService.exists(itemNodeRef)) {
						if (unQueueNode) {
							logger.info("unQueue Node : " + itemNodeRef);
							unQueueNode(key, itemNodeRef);
						}
						updateItemVariants(entityNodeRef, entityVariantsMap, itemNodeRef);
					}
				}
			}
		}
	}

	private void updateItemVariants(NodeRef entityNodeRef, Map<String, NodeRef> entityVariantsMap, NodeRef itemNodeRef) {

		@SuppressWarnings("unchecked")
		List<NodeRef> itemVariants = (List<NodeRef>) nodeService.getProperty(itemNodeRef, BeCPGModel.PROP_VARIANTIDS);

		if (itemVariants != null) {
			
			List<NodeRef> newVariants = new ArrayList<>();
			
			for (NodeRef itemVariant : itemVariants) {
				NodeRef newVariant = updateVariant(itemVariant, entityVariantsMap, entityNodeRef);
				newVariants.add(newVariant);
			}

			nodeService.setProperty(itemNodeRef, BeCPGModel.PROP_VARIANTIDS, (Serializable) newVariants);
		}
	}

	private NodeRef updateVariant(NodeRef itemVariant, Map<String, NodeRef> entityVariantsMap, NodeRef entityNodeRef) {
		
		if (nodeService.hasAspect(nodeService.getPrimaryParent(itemVariant).getParentRef(), BeCPGModel.ASPECT_ENTITY_TPL)) {
			return itemVariant;
		}
		
		String variantName = (String) nodeService.getProperty(itemVariant, ContentModel.PROP_NAME);
		
		if (entityVariantsMap.containsKey(variantName)) {
			NodeRef entityVariant = entityVariantsMap.get(variantName);
			if (logger.isDebugEnabled()) {
				logger.debug("Replace variant : " + itemVariant + " by : " + entityVariant);
			}
			return entityVariant;
		}
		
		return createNewVariant(itemVariant, entityVariantsMap, entityNodeRef, variantName);
		
	}

	private NodeRef createNewVariant(NodeRef itemVariant, Map<String, NodeRef> entityVariantsMap, NodeRef entityNodeRef, String variantName) {
		NodeRef newVariant;
		if (logger.isDebugEnabled()) {
			logger.debug("Create variant : " + variantName);
		}
		
		Map<QName, Serializable> props = new HashMap<>();
		props.put(ContentModel.PROP_NAME, variantName);
		props.put(BeCPGModel.PROP_IS_DEFAULT_VARIANT, nodeService.getProperty(itemVariant, BeCPGModel.PROP_IS_DEFAULT_VARIANT));
		newVariant = nodeService.createNode(entityNodeRef, BeCPGModel.ASSOC_VARIANTS, BeCPGModel.ASSOC_VARIANTS,
				BeCPGModel.TYPE_VARIANT, props).getChildRef();
		entityVariantsMap.put(variantName, newVariant);
		return newVariant;
	}

	/** {@inheritDoc} */
	@Override
	public void cancelCheckout(NodeRef origNodeRef, NodeRef workingCopyNodeRef) {
		// Do nothing
	}

	/** {@inheritDoc} */
	@Override
	public void impactWUsed(NodeRef entityNodeRef, VersionType versionType, String description, Date effetiveDate) {
		// Do nothing
	}

}
