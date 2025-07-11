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
package fr.becpg.repo.product.policy.productListUnits;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.formulation.AbstractCostCalculatingFormulationHandler;
import fr.becpg.repo.product.formulation.AbstractSimpleListFormulationHandler;
import fr.becpg.repo.product.formulation.NutsCalculatingFormulationHandler;

/**
 * <p>ProductListPolicy class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ProductListPolicy extends AbstractBeCPGPolicy
		implements NodeServicePolicies.OnCreateAssociationPolicy, NodeServicePolicies.OnUpdatePropertiesPolicy {

	private static final String KEY_PRODUCT_LISTITEMS = "ProductListPolicy.productListItems";
	private static final String KEY_PRODUCTS = "ProductListPolicy.products";

	private static final Log logger = LogFactory.getLog(ProductListPolicy.class);

	private EntityListDAO entityListDAO;

	private AssociationService associationService;
	
	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object.
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	/**
	 * <p>Setter for the field <code>entityListDAO</code>.</p>
	 *
	 * @param entityListDAO a {@link fr.becpg.repo.entity.EntityListDAO} object.
	 */
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	/** {@inheritDoc} */
	@Override
	public void doInit() {
		logger.debug("Init productListUnits.ProductListPolicy...");
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, PLMModel.TYPE_COSTLIST,
				PLMModel.ASSOC_COSTLIST_COST, new JavaBehaviour(this, "onCreateAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, PLMModel.TYPE_LCALIST,
				PLMModel.ASSOC_LCALIST_LCA, new JavaBehaviour(this, "onCreateAssociation"));

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, PLMModel.TYPE_NUTLIST,
				PLMModel.ASSOC_NUTLIST_NUT, new JavaBehaviour(this, "onCreateAssociation"));

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, PLMModel.TYPE_PHYSICOCHEMLIST,
				PLMModel.ASSOC_PHYSICOCHEMLIST_PHYSICOCHEM, new JavaBehaviour(this, "onCreateAssociation"));
		
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, PLMModel.TYPE_MICROBIOLIST,
				PLMModel.ASSOC_MICROBIOLIST_MICROBIO, new JavaBehaviour(this, "onCreateAssociation"));


		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, PLMModel.TYPE_LABELCLAIMLIST,
				PLMModel.ASSOC_LCL_LABELCLAIM, new JavaBehaviour(this, "onCreateAssociation"));

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, PackModel.TYPE_LABELING_LIST,
				PackModel.ASSOC_LL_LABEL, new JavaBehaviour(this, "onCreateAssociation"));

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, PLMModel.TYPE_PRODUCT,
				new JavaBehaviour(this, "onUpdateProperties"));

		super.disableOnCopyBehaviour(PLMModel.TYPE_COSTLIST);
		super.disableOnCopyBehaviour(PLMModel.TYPE_LCALIST);
		super.disableOnCopyBehaviour(PLMModel.TYPE_NUTLIST);
		super.disableOnCopyBehaviour(PLMModel.TYPE_PHYSICOCHEMLIST);
		super.disableOnCopyBehaviour(PLMModel.TYPE_MICROBIOLIST);
		super.disableOnCopyBehaviour(PLMModel.TYPE_LABELCLAIMLIST);
		super.disableOnCopyBehaviour(PackModel.TYPE_LABELING_LIST);
		super.disableOnCopyBehaviour(PLMModel.TYPE_PRODUCT);

	}

	/** {@inheritDoc} */
	@Override
	public void onCreateAssociation(AssociationRef assocRef) {

		queueAssoc(KEY_PRODUCT_LISTITEMS, assocRef);

	}

	/** {@inheritDoc} */
	@Override
	public void onUpdateProperties(NodeRef productNodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

		if (isPropChanged(before, after, PLMModel.PROP_PRODUCT_UNIT) || isPropChanged(before, after, PLMModel.PROP_PRODUCT_SERVING_SIZE_UNIT)
				|| isPropChanged(before, after, PLMModel.PROP_NUTRIENT_PREPARED_UNIT)) {
			queueNode(KEY_PRODUCTS, productNodeRef);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected boolean doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		boolean isEnabledBehaviour = policyBehaviourFilter.isEnabled(ContentModel.ASPECT_AUDITABLE);
		try {
			policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);

			if (KEY_PRODUCTS.equals(key)) {
				updateProducts(pendingNodes);
			}
		} finally {
			if(isEnabledBehaviour) {
				policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
			}
		}

		return true;

	}

	/** {@inheritDoc} */
	@Override
	protected boolean doBeforeAssocsCommit(String key, Set<AssociationRef> pendingAssocs) {
		try {
			policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);

			if (KEY_PRODUCT_LISTITEMS.equals(key)) {
				updateProductListItems(pendingAssocs);
			}
		} finally {
			policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
		}
		return true;
	}

	/*
	 *
	 *
	 *
	 * Update the productListItem unit when the product unit is modified
	 */
	private void updateProducts(Set<NodeRef> productNodeRefs) {

		if (productNodeRefs != null) {
			StopWatch watch = null;

			if (logger.isDebugEnabled()) {
				watch = new StopWatch();
				watch.start();
			}

			for (NodeRef productNodeRef : productNodeRefs) {
				if (nodeService.exists(productNodeRef)) {
					ProductUnit productUnit = ProductUnit.getUnit((String) nodeService.getProperty(productNodeRef, PLMModel.PROP_PRODUCT_UNIT));

					// look for product lists
					NodeRef listContainerNodeRef = entityListDAO.getListContainer(productNodeRef);
					if (listContainerNodeRef != null) {

						// costList
						NodeRef costListNodeRef = entityListDAO.getList(listContainerNodeRef, PLMModel.TYPE_COSTLIST);
						if ((costListNodeRef != null) && !nodeService.hasAspect(productNodeRef, BeCPGModel.ASPECT_ENTITY_TPL)) {

							for (NodeRef productListItemNodeRef : entityListDAO.getListItems(costListNodeRef, PLMModel.TYPE_COSTLIST)) {

								NodeRef costNodeRef = associationService.getTargetAssoc(productListItemNodeRef, PLMModel.ASSOC_COSTLIST_COST);
								if (costNodeRef != null) {
									Boolean costFixed = (Boolean) nodeService.getProperty(costNodeRef, PLMModel.PROP_COSTFIXED);

									if (!Boolean.TRUE.equals(costFixed)) {

										String costCurrency = (String) nodeService.getProperty(costNodeRef, PLMModel.PROP_COSTCURRENCY);
										String costListUnit = (String) nodeService.getProperty(productListItemNodeRef, PLMModel.PROP_COSTLIST_UNIT);

										if (!((costListUnit != null) && !costListUnit.isEmpty()
												&& costListUnit.endsWith(AbstractCostCalculatingFormulationHandler.calculateSuffixUnit(productUnit)))) {
											nodeService.setProperty(productListItemNodeRef, PLMModel.PROP_COSTLIST_UNIT,
													AbstractCostCalculatingFormulationHandler.calculateUnit(productUnit, costCurrency, costFixed));
										}
									}
								}
							}
						}

						// nutList
						NodeRef nutListNodeRef = entityListDAO.getList(listContainerNodeRef, PLMModel.TYPE_NUTLIST);
						if (nutListNodeRef != null) {
							
							ProductUnit servingSizeUnit = ProductUnit
									.getUnit((String) nodeService.getProperty(productNodeRef, PLMModel.PROP_PRODUCT_SERVING_SIZE_UNIT));
							ProductUnit nutrientPreparedUnit = ProductUnit
									.getUnit((String) nodeService.getProperty(productNodeRef, PLMModel.PROP_NUTRIENT_PREPARED_UNIT));
							
							
							for (NodeRef productListItemNodeRef : entityListDAO.getListItems(nutListNodeRef, PLMModel.TYPE_NUTLIST)) {

								String nutListUnit = (String) nodeService.getProperty(productListItemNodeRef, PLMModel.PROP_NUTLIST_UNIT);
								String nutListUnitPrepared = (String) nodeService.getProperty(productListItemNodeRef, PLMModel.PROP_NUTLIST_UNIT_PREPARED);
								NodeRef nutNodeRef = associationService.getTargetAssoc(productListItemNodeRef, PLMModel.ASSOC_NUTLIST_NUT);
								if (nutNodeRef != null) {
									String nutUnit = (String) nodeService.getProperty(nutNodeRef, PLMModel.PROP_NUTUNIT);

									if (!((nutListUnit != null) && !nutListUnit.isEmpty() && nutListUnit
											.endsWith(NutsCalculatingFormulationHandler.calculateSuffixUnit(productUnit, servingSizeUnit)))) {

						
										nodeService.setProperty(productListItemNodeRef, PLMModel.PROP_NUTLIST_UNIT,
												NutsCalculatingFormulationHandler.calculateUnit(productUnit, servingSizeUnit, nutUnit));
									}
									
									
									if ( !((nutListUnitPrepared != null) && !nutListUnitPrepared.isEmpty() && nutListUnitPrepared
											.endsWith(NutsCalculatingFormulationHandler.calculateSuffixUnit(productUnit, nutrientPreparedUnit))) 
											&& nodeService.hasAspect(productListItemNodeRef,PLMModel.ASPECT_NUTLIST_PREPARED)) {
						
										nodeService.setProperty(productListItemNodeRef, PLMModel.PROP_NUTLIST_UNIT_PREPARED,
												NutsCalculatingFormulationHandler.calculateUnit(productUnit, nutrientPreparedUnit, nutUnit));
									}
								}
							}
						}
					}
				}
			}

			if (logger.isDebugEnabled() && watch!=null) {
				logger.debug("BeforeCommit run in  " + watch.getTotalTimeSeconds() + " seconds for key " + KEY_PRODUCTS + "  - pendingNodesSize : "
						+ productNodeRefs.size());
			}
		}
	}

	/*
	 * Update the productListItem unit when the target assoc is modified
	 */
	private void updateProductListItems(final Set<AssociationRef> assocRefs) {

		if (assocRefs != null) {

			StopWatch watch = null;

			if (logger.isDebugEnabled()) {
				watch = new StopWatch();
				watch.start();
			}

			for (AssociationRef assocRef : assocRefs) {

				NodeRef targetNodeRef = assocRef.getTargetRef();
				NodeRef productListItemNodeRef = assocRef.getSourceRef();

				if (nodeService.exists(targetNodeRef) && nodeService.exists(productListItemNodeRef)) {

					QName type = nodeService.getType(productListItemNodeRef);

					if (type.equals(PLMModel.TYPE_COSTLIST)) {

						Boolean costFixed = (Boolean) nodeService.getProperty(targetNodeRef, PLMModel.PROP_COSTFIXED);
						String costCurrency = (String) nodeService.getProperty(targetNodeRef, PLMModel.PROP_COSTCURRENCY);
						String costListUnit = (String) nodeService.getProperty(productListItemNodeRef, PLMModel.PROP_COSTLIST_UNIT);

						if ((costFixed != null) && costFixed) {

							if (!((costListUnit != null) && costListUnit.equals(costCurrency))) {
								nodeService.setProperty(productListItemNodeRef, PLMModel.PROP_COSTLIST_UNIT, costCurrency);
							}
						} else {

							if (!((costListUnit != null) && !costListUnit.isEmpty()
									&& costListUnit.startsWith(costCurrency + AbstractSimpleListFormulationHandler.UNIT_SEPARATOR))) {

								ProductUnit unit = getProductUnit(productListItemNodeRef);

								if (unit != null) {
									nodeService.setProperty(productListItemNodeRef, PLMModel.PROP_COSTLIST_UNIT,
											AbstractCostCalculatingFormulationHandler.calculateUnit(unit, costCurrency, costFixed));
								}

							}
						}
					} else if (type.equals(PLMModel.TYPE_NUTLIST)) {
						String nutUnit = (String) nodeService.getProperty(targetNodeRef, PLMModel.PROP_NUTUNIT);
						String nutListUnit = (String) nodeService.getProperty(productListItemNodeRef, PLMModel.PROP_NUTLIST_UNIT);
						String nutListUnitPrepared = (String) nodeService.getProperty(productListItemNodeRef, PLMModel.PROP_NUTLIST_UNIT_PREPARED);

						// nutListUnit
						if (!((nutListUnit != null) && !nutListUnit.isEmpty()
								&& nutListUnit.startsWith(nutUnit + AbstractSimpleListFormulationHandler.UNIT_SEPARATOR))) {

							ProductUnit unit = getProductUnit(productListItemNodeRef);

							if (unit != null) {
								nodeService.setProperty(productListItemNodeRef, PLMModel.PROP_NUTLIST_UNIT,
										NutsCalculatingFormulationHandler.calculateUnit(unit, getServingSizeUnit(productListItemNodeRef), nutUnit));
							}

						}
						
						if (!((nutListUnitPrepared != null) && !nutListUnitPrepared.isEmpty()
								&& nutListUnitPrepared.startsWith(nutUnit + AbstractSimpleListFormulationHandler.UNIT_SEPARATOR)) && 	
								nodeService.hasAspect(productListItemNodeRef,PLMModel.ASPECT_NUTLIST_PREPARED) ) {

							ProductUnit unit = getProductUnit(productListItemNodeRef);

							if (unit != null) {
								
							
								nodeService.setProperty(productListItemNodeRef, PLMModel.PROP_NUTLIST_UNIT,
										NutsCalculatingFormulationHandler.calculateUnit(unit, getNutrientPreparationUnit(productListItemNodeRef), nutUnit));
							}

						}
						
						// nutListGroup
						String nutGroup = (String) nodeService.getProperty(targetNodeRef, PLMModel.PROP_NUTGROUP);
						nodeService.setProperty(productListItemNodeRef, PLMModel.PROP_NUTLIST_GROUP, nutGroup);
					} else if (type.equals(PLMModel.TYPE_PHYSICOCHEMLIST)) {
						String physicoChemUnit = (String) nodeService.getProperty(targetNodeRef, PLMModel.PROP_PHYSICO_CHEM_UNIT);
						nodeService.setProperty(productListItemNodeRef, PLMModel.PROP_PHYSICOCHEMLIST_UNIT, physicoChemUnit);
						
						String physicoChemType = (String) nodeService.getProperty(targetNodeRef, PLMModel.PROP_PHYSICO_CHEM_TYPE);
						nodeService.setProperty(productListItemNodeRef, PLMModel.PROP_PHYSICOCHEMLIST_TYPE, physicoChemType);
						
						
					} else if (type.equals(PLMModel.TYPE_MICROBIOLIST)) {
					    
						String microbioType = (String) nodeService.getProperty(targetNodeRef, PLMModel.PROP_MICROBIO_TYPE);
						nodeService.setProperty(productListItemNodeRef, PLMModel.PROP_MICROBIOLIST_TYPE, microbioType);

					}
					
					else if (type.equals(PLMModel.TYPE_LABELCLAIMLIST)) {
						// labelClaimType
						String labelClaimType = (String) nodeService.getProperty(targetNodeRef, PLMModel.PROP_LABEL_CLAIM_TYPE);
						nodeService.setProperty(productListItemNodeRef, PLMModel.PROP_LCL_TYPE, labelClaimType);
					} else if (type.equals(PackModel.TYPE_LABELING_LIST)) {
						// labelingList
						String labelType = (String) nodeService.getProperty(targetNodeRef, PackModel.PROP_LABEL_TYPE);
						nodeService.setProperty(productListItemNodeRef, PackModel.PROP_LL_TYPE, labelType);
					} else if (type.equals(PLMModel.TYPE_LCALIST)) {

						Boolean lcaFixed = (Boolean) nodeService.getProperty(targetNodeRef, PLMModel.PROP_LCAFIXED);
						String lcaUnit = (String) nodeService.getProperty(targetNodeRef, PLMModel.PROP_LCAUNIT);
						String lcaListUnit = (String) nodeService.getProperty(productListItemNodeRef, PLMModel.PROP_LCALIST_UNIT);

						if ((lcaFixed != null) && lcaFixed) {

							if (!((lcaListUnit != null) && lcaListUnit.equals(lcaUnit))) {
								nodeService.setProperty(productListItemNodeRef, PLMModel.PROP_LCALIST_UNIT, lcaUnit);
							}
						} else {

							if (!((lcaListUnit != null) && !lcaListUnit.isEmpty()
									&& lcaListUnit.startsWith(lcaUnit + AbstractSimpleListFormulationHandler.UNIT_SEPARATOR))) {

								ProductUnit unit = getProductUnit(productListItemNodeRef);

								if (unit != null) {
									nodeService.setProperty(productListItemNodeRef, PLMModel.PROP_LCALIST_UNIT,
											AbstractCostCalculatingFormulationHandler.calculateUnit(unit, lcaUnit, lcaFixed));
								}

							}
						}
					}
				}
			}

			if (logger.isDebugEnabled() && watch!=null) {
				logger.debug("BeforeCommit run in  " + watch.getTotalTimeSeconds() + " seconds for key " + KEY_PRODUCT_LISTITEMS
						+ "  - pendingNodesSize : " + assocRefs.size());
			}
		}
	}

	private ProductUnit getProductUnit(NodeRef listNodeRef) {

		NodeRef productNodeRef = entityListDAO.getEntity(listNodeRef);
		if ((productNodeRef != null) && nodeService.exists(productNodeRef)) {
			return ProductUnit.getUnit((String) nodeService.getProperty(productNodeRef, PLMModel.PROP_PRODUCT_UNIT));
		}

		return null;
	}

	private ProductUnit getServingSizeUnit(NodeRef listNodeRef) {

		NodeRef productNodeRef = entityListDAO.getEntity(listNodeRef);
		if ((productNodeRef != null) && nodeService.exists(productNodeRef)) {
			return ProductUnit.getUnit((String) nodeService.getProperty(productNodeRef, PLMModel.PROP_PRODUCT_SERVING_SIZE_UNIT));
		}

		return null;
	}
	
	private ProductUnit getNutrientPreparationUnit(NodeRef listNodeRef) {

		NodeRef productNodeRef = entityListDAO.getEntity(listNodeRef);
		if ((productNodeRef != null) && nodeService.exists(productNodeRef)) {
			return ProductUnit.getUnit((String) nodeService.getProperty(productNodeRef, PLMModel.PROP_NUTRIENT_PREPARED_UNIT));
		}

		return null;
	}

}
