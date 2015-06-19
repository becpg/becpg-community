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
package fr.becpg.repo.product.policy.productListUnits;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.formulation.AbstractSimpleListFormulationHandler;
import fr.becpg.repo.product.formulation.CostsCalculatingFormulationHandler;
import fr.becpg.repo.product.formulation.NutsCalculatingFormulationHandler;

public class ProductListPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnCreateAssociationPolicy,
		NodeServicePolicies.OnUpdatePropertiesPolicy {

	private static final String KEY_PRODUCT_LISTITEMS = "ProductListPolicy.productListItems";
	private static final String KEY_PRODUCTS = "ProductListPolicy.products";

	private static final Log logger = LogFactory.getLog(ProductListPolicy.class);

	private TransactionListener transactionListener;

	private EntityListDAO entityListDAO;

	private AssociationService associationService;

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	@Override
	public void doInit() {
		logger.debug("Init productListUnits.ProductListPolicy...");
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, PLMModel.TYPE_COSTLIST,
				PLMModel.ASSOC_COSTLIST_COST, new JavaBehaviour(this, "onCreateAssociation"));

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, PLMModel.TYPE_NUTLIST,
				PLMModel.ASSOC_NUTLIST_NUT, new JavaBehaviour(this, "onCreateAssociation"));

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, PLMModel.TYPE_PHYSICOCHEMLIST,
				PLMModel.ASSOC_PHYSICOCHEMLIST_PHYSICOCHEM, new JavaBehaviour(this, "onCreateAssociation"));

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, PLMModel.TYPE_LABELCLAIMLIST,
				PLMModel.ASSOC_LCL_LABELCLAIM, new JavaBehaviour(this, "onCreateAssociation"));

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, PackModel.TYPE_LABELING_LIST,
				PackModel.ASSOC_LL_LABEL, new JavaBehaviour(this, "onCreateAssociation"));

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, PLMModel.TYPE_PRODUCT, new JavaBehaviour(this,
				"onUpdateProperties"));

		super.disableOnCopyBehaviour(PLMModel.TYPE_COSTLIST);
		super.disableOnCopyBehaviour(PLMModel.TYPE_NUTLIST);
		super.disableOnCopyBehaviour(PLMModel.TYPE_PHYSICOCHEMLIST);
		super.disableOnCopyBehaviour(PLMModel.TYPE_LABELCLAIMLIST);
		super.disableOnCopyBehaviour(PackModel.TYPE_LABELING_LIST);
		super.disableOnCopyBehaviour(PLMModel.TYPE_PRODUCT);

		// transaction listeners
		this.transactionListener = new ProductListPolicyTransactionListener();
	}

	@Override
	public void onCreateAssociation(AssociationRef assocRef) {

		// Bind the listener to the transaction
		AlfrescoTransactionSupport.bindListener(transactionListener);
		// Get the set of nodes read
		Set<AssociationRef> assocRefs = AlfrescoTransactionSupport.getResource(KEY_PRODUCT_LISTITEMS);
		if (assocRefs == null) {
			assocRefs = new HashSet<>(5);
			AlfrescoTransactionSupport.bindResource(KEY_PRODUCT_LISTITEMS, assocRefs);
		}
		assocRefs.add(assocRef);
	}

	@Override
	public void onUpdateProperties(NodeRef productNodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

		String beforeProductUnit = (String) before.get(PLMModel.PROP_PRODUCT_UNIT);
		String afterProductUnit = (String) after.get(PLMModel.PROP_PRODUCT_UNIT);

		if (afterProductUnit != null && !afterProductUnit.equals(beforeProductUnit)) {

			// Bind the listener to the transaction
			AlfrescoTransactionSupport.bindListener(transactionListener);
			// Get the set of nodes read
			Set<NodeRef> nodeRefs = AlfrescoTransactionSupport.getResource(KEY_PRODUCTS);
			if (nodeRefs == null) {
				nodeRefs = new HashSet<>(3);
				AlfrescoTransactionSupport.bindResource(KEY_PRODUCTS, nodeRefs);
			}
			nodeRefs.add(productNodeRef);
		}
	}

	private class ProductListPolicyTransactionListener extends TransactionListenerAdapter {

		final Map<NodeRef, ProductUnit> productsUnit = new HashMap<>(3);
		final Map<NodeRef, NodeRef> productNodeRefs = new HashMap<>(3);

		@Override
		public void beforeCommit(boolean readOnly) {

			final Set<NodeRef> products = AlfrescoTransactionSupport.getResource(KEY_PRODUCTS);

			final Set<AssociationRef> assocRefs = AlfrescoTransactionSupport.getResource(KEY_PRODUCT_LISTITEMS);

			updateProducts(products);
			updateProductListItems(assocRefs);
		}

		/*
		 * Update the productListItem unit when the product unit is modified
		 */
		private void updateProducts(Set<NodeRef> productNodeRefs) {

			if (productNodeRefs != null) {

				for (NodeRef productNodeRef : productNodeRefs) {
					if (nodeService.exists(productNodeRef)) {
						ProductUnit productUnit = ProductUnit.getUnit((String) nodeService.getProperty(productNodeRef, PLMModel.PROP_PRODUCT_UNIT));

						// look for product lists
						NodeRef listContainerNodeRef = entityListDAO.getListContainer(productNodeRef);
						if (listContainerNodeRef != null) {

							// costList
							NodeRef costListNodeRef = entityListDAO.getList(listContainerNodeRef, PLMModel.TYPE_COSTLIST);							
							if (costListNodeRef != null && !isTemplate(productNodeRef)) {

								productsUnit.put(costListNodeRef, productUnit);
								
								for (NodeRef productListItemNodeRef : entityListDAO.getListItems(costListNodeRef, PLMModel.TYPE_COSTLIST)) {

									NodeRef costNodeRef = associationService.getTargetAssoc(productListItemNodeRef, PLMModel.ASSOC_COSTLIST_COST);
									if (costNodeRef != null) {
										Boolean costFixed = (Boolean) nodeService.getProperty(costNodeRef, PLMModel.PROP_COSTFIXED);

										if (costFixed == null || !costFixed) {

											String costCurrency = (String) nodeService.getProperty(costNodeRef, PLMModel.PROP_COSTCURRENCY);
											String costListUnit = (String) nodeService.getProperty(productListItemNodeRef,
													PLMModel.PROP_COSTLIST_UNIT);

											if (!(costListUnit != null && !costListUnit.isEmpty() && costListUnit
													.endsWith(CostsCalculatingFormulationHandler.calculateSuffixUnit(productUnit)))) {
												nodeService.setProperty(productListItemNodeRef, PLMModel.PROP_COSTLIST_UNIT,
														CostsCalculatingFormulationHandler.calculateUnit(productUnit, costCurrency, costFixed));
											}
										}
									}
								}
							}

							// nutList
							NodeRef nutListNodeRef = entityListDAO.getList(listContainerNodeRef, PLMModel.TYPE_NUTLIST);
							if (nutListNodeRef != null) {

								productsUnit.put(nutListNodeRef, productUnit);

								for (NodeRef productListItemNodeRef : entityListDAO.getListItems(nutListNodeRef,  PLMModel.TYPE_NUTLIST)) {

									String nutListUnit = (String) nodeService.getProperty(productListItemNodeRef, PLMModel.PROP_NUTLIST_UNIT);

									NodeRef nutNodeRef = associationService.getTargetAssoc(productListItemNodeRef, PLMModel.ASSOC_NUTLIST_NUT);
									if (nutNodeRef != null) {
										String nutUnit = (String) nodeService.getProperty(nutNodeRef, PLMModel.PROP_NUTUNIT);

										if (!(nutListUnit != null && !nutListUnit.isEmpty() && nutListUnit.endsWith(NutsCalculatingFormulationHandler
												.calculateSuffixUnit(productUnit)))) {

											nodeService.setProperty(productListItemNodeRef, PLMModel.PROP_NUTLIST_UNIT,
													NutsCalculatingFormulationHandler.calculateUnit(productUnit, nutUnit));
										}
									}
								}
							}
						}
					}
				}
			}
		}

		/*
		 * Update the productListItem unit when the target assoc is modified
		 */
		private void updateProductListItems(final Set<AssociationRef> assocRefs) {

			if (assocRefs != null) {

				for (AssociationRef assocRef : assocRefs) {

					NodeRef targetNodeRef = assocRef.getTargetRef();
					NodeRef productListItemNodeRef = assocRef.getSourceRef();

					if (nodeService.exists(targetNodeRef) && nodeService.exists(productListItemNodeRef)) {

						QName type = nodeService.getType(productListItemNodeRef);

						if (type.equals(PLMModel.TYPE_COSTLIST)) {

							Boolean costFixed = (Boolean) nodeService.getProperty(targetNodeRef, PLMModel.PROP_COSTFIXED);
							String costCurrency = (String) nodeService.getProperty(targetNodeRef, PLMModel.PROP_COSTCURRENCY);
							String costListUnit = (String) nodeService.getProperty(productListItemNodeRef, PLMModel.PROP_COSTLIST_UNIT);
						
							if (costFixed != null && costFixed) {

								if (!(costListUnit != null && costListUnit.equals(costCurrency))) {
									nodeService.setProperty(productListItemNodeRef, PLMModel.PROP_COSTLIST_UNIT, costCurrency);
								}
							} else {

								if (!(costListUnit != null && !costListUnit.isEmpty() && costListUnit.startsWith(costCurrency
										+ AbstractSimpleListFormulationHandler.UNIT_SEPARATOR))) {

									NodeRef listNodeRef = nodeService.getPrimaryParent(productListItemNodeRef).getParentRef();

									if (listNodeRef != null) {

										nodeService.setProperty(productListItemNodeRef, PLMModel.PROP_COSTLIST_UNIT,
												CostsCalculatingFormulationHandler.calculateUnit(getProductUnit(listNodeRef), 
														costCurrency, 
														costFixed));
									}
								}
							}
						} else if (type.equals(PLMModel.TYPE_NUTLIST)) {
							String nutUnit = (String) nodeService.getProperty(targetNodeRef, PLMModel.PROP_NUTUNIT);
							String nutListUnit = (String) nodeService.getProperty(productListItemNodeRef, PLMModel.PROP_NUTLIST_UNIT);

							// nutListUnit
							if (!(nutListUnit != null && !nutListUnit.isEmpty() && nutListUnit.startsWith(nutUnit
									+ AbstractSimpleListFormulationHandler.UNIT_SEPARATOR))) {

								NodeRef listNodeRef = nodeService.getPrimaryParent(productListItemNodeRef).getParentRef();

								if (listNodeRef != null) {

									nodeService.setProperty(productListItemNodeRef, PLMModel.PROP_NUTLIST_UNIT,
											NutsCalculatingFormulationHandler.calculateUnit(getProductUnit(listNodeRef), nutUnit));
								}
							}

							// nutListGroup
							String nutGroup = (String) nodeService.getProperty(targetNodeRef, PLMModel.PROP_NUTGROUP);
							nodeService.setProperty(productListItemNodeRef, PLMModel.PROP_NUTLIST_GROUP, nutGroup);
						} else if (type.equals(PLMModel.TYPE_PHYSICOCHEMLIST)) {
							String physicoChemUnit = (String) nodeService.getProperty(targetNodeRef, PLMModel.PROP_PHYSICO_CHEM_UNIT);
							nodeService.setProperty(productListItemNodeRef, PLMModel.PROP_PHYSICOCHEMLIST_UNIT, physicoChemUnit);
						} else if (type.equals(PLMModel.TYPE_LABELCLAIMLIST)) {
							// labelClaimType
							String labelClaimType = (String) nodeService.getProperty(targetNodeRef, PLMModel.PROP_LABEL_CLAIM_TYPE);
							nodeService.setProperty(productListItemNodeRef, PLMModel.PROP_LCL_TYPE, labelClaimType);
						} else if (type.equals(PackModel.TYPE_LABELING_LIST)) {
							// labelingList
							String labelType = (String) nodeService.getProperty(targetNodeRef, PackModel.PROP_LABEL_TYPE);
							nodeService.setProperty(productListItemNodeRef, PackModel.PROP_LL_TYPE, labelType);
						}
					}
				}
			}
		}

		private ProductUnit getProductUnit(NodeRef listNodeRef) {

			ProductUnit productUnit = productsUnit.get(listNodeRef);

			if (productUnit == null) {

				NodeRef productNodeRef = getProduct(listNodeRef);
				if (productNodeRef != null) {

					productUnit = ProductUnit.getUnit((String) nodeService.getProperty(productNodeRef, PLMModel.PROP_PRODUCT_UNIT));
					productsUnit.put(listNodeRef, productUnit);
				}
			}

			return productUnit;
		}
		
		private NodeRef getProduct(NodeRef listNodeRef){
			NodeRef productNodeRef = productNodeRefs.get(listNodeRef);
			if(productNodeRef == null){
				NodeRef listContainerNodeRef = nodeService.getPrimaryParent(listNodeRef).getParentRef();
				if (listContainerNodeRef != null) {

					productNodeRef = nodeService.getPrimaryParent(listContainerNodeRef).getParentRef();
					productNodeRefs.put(listNodeRef, productNodeRef);
				}
			}
			return productNodeRef;
		}
		
		private boolean isTemplate(NodeRef productNodeRef){
			return nodeService.hasAspect(productNodeRef, BeCPGModel.ASPECT_ENTITY_TPL);
		}
	}
}
