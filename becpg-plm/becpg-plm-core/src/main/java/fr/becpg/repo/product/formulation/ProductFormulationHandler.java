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
package fr.becpg.repo.product.formulation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StopWatch;

import fr.becpg.model.PLMModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ResourceProductData;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.report.engine.impl.ReportServerEngine;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.impl.LazyLoadingDataList;
import fr.becpg.repo.repository.model.CompositionDataItem;
import fr.becpg.repo.variant.filters.VariantFilters;

/**
 * <p>ProductFormulationHandler class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ProductFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final String MESSAGE_MISSING_UNIT = "message.formulate.missing.unit";
	private static final String MESSAGE_MISSING_DENSITY = "message.formulate.missing.density";
	private static final String MESSAGE_WRONG_UNIT = "message.formulate.wrong.unit";
	private static final String MESSAGE_MISSING_TARE = "message.formulate.missing.tare";

	/** Constant <code>logger</code> */
	protected static final Log logger = LogFactory.getLog(ProductFormulationHandler.class);

	private NodeService nodeService;

	private ProductService productService;

	private AlfrescoRepository<ProductData> alfrescoRepository;

	private LockService lockService;

	private boolean formulateChildren = false;

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>lockService</code>.</p>
	 *
	 * @param lockService a {@link org.alfresco.service.cmr.lock.LockService} object.
	 */
	public void setLockService(LockService lockService) {
		this.lockService = lockService;
	}

	/**
	 * <p>Setter for the field <code>productService</code>.</p>
	 *
	 * @param productService a {@link fr.becpg.repo.product.ProductService} object.
	 */
	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/**
	 * <p>Setter for the field <code>formulateChildren</code>.</p>
	 *
	 * @param formulateChildren a boolean.
	 */
	public void setFormulateChildren(boolean formulateChildren) {
		this.formulateChildren = formulateChildren;
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(ProductData productData) throws FormulateException {

		if ((productData.hasCompoListEl(new VariantFilters<>())) || (productData.hasPackagingListEl(new VariantFilters<>()))
				|| (productData.hasProcessListEl(new VariantFilters<>()))) {

			if (productData.getReqCtrlList() != null) {
				clearReqCltrlList(productData.getReqCtrlList());
			} else {
				productData.setReqCtrlList(new LinkedList<ReqCtrlListDataItem>());
			}

			if (formulateChildren) {
				checkShouldFormulateComponents(true, productData);
			}

			checkMissingProperties(productData);

			// Continue
			return true;
		}

		// Reset
		if ((productData.getReqCtrlList() != null)) {

			List<ReqCtrlListDataItem> retainedItems = new ArrayList<>();

			for (ReqCtrlListDataItem item : productData.getReqCtrlList()) {
				if (ReportServerEngine.REPORT_FORMULATION_CHAIN_ID.equals(item.getFormulationChainId())) {
					retainedItems.add(item);
				}
			}

			productData.getReqCtrlList().clear();

			for (ReqCtrlListDataItem item : retainedItems) {
				productData.getReqCtrlList().add(item);
				if (productData.getReqCtrlList() instanceof LazyLoadingDataList) {
					((LazyLoadingDataList<ReqCtrlListDataItem>) productData.getReqCtrlList()).getDeletedNodes().remove(item);
				}
			}
		}

		return true;
	}

	private void clearReqCltrlList(List<ReqCtrlListDataItem> reqCtrlList) {
		if (reqCtrlList != null) {
			reqCtrlList.removeIf(r -> r.getNodeRef() == null);
		}
	}

	private boolean checkShouldFormulateComponents(boolean isRoot, ProductData productData) throws FormulateException {
		boolean isFormulated = false;

		if (!productData.getIsUpToDate()) {

			// Avoid recheck
			productData.setIsUpToDate(true);

			if (logger.isDebugEnabled()) {
				logger.debug("checkShouldFormulateComponents: " + productData.getName());
			}

			if (((productData.getNodeRef() == null) || (lockService.getLockStatus(productData.getNodeRef()) == LockStatus.NO_LOCK))
					&& !(productData instanceof LocalSemiFinishedProductData)) {

				boolean shouldFormulate = false;

				if (productData.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
					for (CompositionDataItem c : productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
						if ((c.getComponent() != null)) {
							ProductData subComponent = alfrescoRepository.findOne(c.getComponent());
							if (checkShouldFormulateComponents(false, subComponent) || ((productData.getFormulatedDate() == null)
									|| (subComponent.getFormulatedDate()!=null && 
										productData.getFormulatedDate().getTime() < subComponent.getFormulatedDate().getTime()))) {
								shouldFormulate = true;
							}
						}
					}
				}
				if (productData.getPackagingList() != null) {
					for (CompositionDataItem c : productData.getPackagingList()) {
						if ((c.getComponent() != null)) {
							ProductData subComponent = alfrescoRepository.findOne(c.getComponent());
							if (checkShouldFormulateComponents(false, subComponent) || ((productData.getFormulatedDate() == null)
									|| (subComponent.getFormulatedDate()!=null &&  productData.getFormulatedDate().getTime() < subComponent.getFormulatedDate().getTime()))) {
								shouldFormulate = true;
							}
						}
					}
				}
				if (productData.getProcessList() != null) {
					for (CompositionDataItem c : productData.getProcessList()) {
						if ((c.getComponent() != null)) {
							ProductData subComponent = alfrescoRepository.findOne(c.getComponent());
							if (checkShouldFormulateComponents(false, subComponent) || ((productData.getFormulatedDate() == null)
									|| (subComponent.getFormulatedDate()!=null &&  productData.getFormulatedDate().getTime() < subComponent.getFormulatedDate().getTime()))) {
								shouldFormulate = true;
							}
						}
					}
				}

				if (!isRoot) {

					// Check modified date on component and template
					Date tplModified = productData.getEntityTpl() != null ? productData.getEntityTpl().getModifiedDate() : null;
					Date modified = productData.getModifiedDate();
					if (tplModified != null) {
						if ((modified == null) || (tplModified.getTime() > modified.getTime())) {
							modified = tplModified;
						}
					}

					Date formulatedDate = productData.getFormulatedDate();
					if (shouldFormulate || ((modified == null) || (formulatedDate == null) || (modified.getTime() > formulatedDate.getTime()))) {

						StopWatch watch = null;
						if (logger.isDebugEnabled()) {
							watch = new StopWatch();
							watch.start();
							logger.debug("auto-formulate: " + productData.getName());
						}

						productService.formulate(productData);
						alfrescoRepository.save(productData);

						if (logger.isDebugEnabled()) {
							assert watch != null;
							watch.stop();
							logger.debug("auto-formulate : " + this.getClass().getName() + " takes " + watch.getTotalTimeSeconds() + " seconds");
						}
						isFormulated = true;
					}
				}

			}
		}
		return isFormulated;
	}

	private void checkMissingProperties(ProductData formulatedProduct) {

		checkFormulatedProduct(formulatedProduct);

		if (formulatedProduct.hasCompoListEl()) {
			for (CompoListDataItem c : formulatedProduct.getCompoList()) {
				if (c.getCompoListUnit() == null) {
					addMessingReq(formulatedProduct.getReqCtrlList(), null, MESSAGE_WRONG_UNIT, RequirementDataType.Composition);
				} else {
					checkCompositionItem(formulatedProduct.getReqCtrlList(), c.getProduct(), c);
				}
			}
		}
		if (formulatedProduct.hasPackagingListEl()) {
			for (PackagingListDataItem p : formulatedProduct.getPackagingList()) {
				if (p.getPackagingListUnit() == null) {
					addMessingReq(formulatedProduct.getReqCtrlList(), null, MESSAGE_WRONG_UNIT, RequirementDataType.Packaging);
				} else {
					checkPackagingItem(formulatedProduct.getReqCtrlList(), p);
				}

			}
		}
	}

	private void checkFormulatedProduct(ProductData formulatedProduct) {
		
		if (!(formulatedProduct instanceof ResourceProductData)) {
			ProductUnit productUnit = formulatedProduct.getUnit();
			if (productUnit == null) {
				addMessingReq(formulatedProduct.getReqCtrlList(), formulatedProduct.getNodeRef(), MESSAGE_MISSING_UNIT, RequirementDataType.Formulation);
			}
		}
	}

	private void checkCompositionItem(List<ReqCtrlListDataItem> reqCtrlListDataItem, NodeRef productNodeRef, CompoListDataItem c) {

		if (productNodeRef!=null && !PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT.isMatch(nodeService.getType(productNodeRef))) {

			ProductData subComponent = alfrescoRepository.findOne(c.getComponent());
			
			ProductUnit productUnit = subComponent.getUnit();
			if (c != null) {
				boolean shouldUseLiter = productUnit!=null && productUnit.isVolume();
				boolean useLiter = c.getCompoListUnit()!=null && c.getCompoListUnit().isVolume();
				Double density = subComponent.getDensity();

				if ((density == null) && ((shouldUseLiter && !useLiter) || (!shouldUseLiter && useLiter))) {
					addMessingReq(reqCtrlListDataItem, productNodeRef, MESSAGE_WRONG_UNIT, RequirementDataType.Composition);
					addMessingReq(reqCtrlListDataItem, productNodeRef, MESSAGE_MISSING_DENSITY, RequirementDataType.Composition);
				}
				Double overrunPerc = c.getOverrunPerc();
				if ((productUnit!=null && productUnit.isVolume()) || (overrunPerc != null)) {
					if ((density == null) || density.equals(0d)) {
						addMessingReq(reqCtrlListDataItem, productNodeRef, MESSAGE_MISSING_DENSITY, RequirementDataType.Composition);
					}
				}
			}
		}
	}

	private void checkPackagingItem(List<ReqCtrlListDataItem> reqCtrlListDataItem, PackagingListDataItem p) {
		NodeRef productNodeRef = p.getProduct();
		ProductData subComponent = alfrescoRepository.findOne(productNodeRef);
		
		ProductUnit productUnit = subComponent.getUnit();
		if (productUnit == null) {
			addMessingReq(reqCtrlListDataItem, productNodeRef, MESSAGE_MISSING_UNIT, RequirementDataType.Packaging);
		} else {
			boolean wrongUnit = false;
			if(productUnit.isWeight()) {
				if(!(p.getPackagingListUnit()!=null && p.getPackagingListUnit().isWeight())) {
					wrongUnit = true;
				}
			} else if(productUnit.isVolume()) {
				if(!(p.getPackagingListUnit()!=null && p.getPackagingListUnit().isWeight())) {
					wrongUnit = true;
				}
			} else {
				if( ProductUnit.PP.equals(p.getPackagingListUnit())) {
					if(!ProductUnit.P.equals(productUnit)) {
						wrongUnit = true;
					}
				} else if(!productUnit.equals(p.getPackagingListUnit())){
					wrongUnit = true;
				}
			}
			
			if(wrongUnit) {
				addMessingReq(reqCtrlListDataItem, productNodeRef, MESSAGE_WRONG_UNIT, RequirementDataType.Packaging);
			}	
		}

		if (!nodeService.hasAspect(p.getProduct(), PackModel.ASPECT_PALLET)) {
			BigDecimal tare = FormulationHelper.getTareInKg(subComponent);
			if (tare == null) {
				addMessingReq(reqCtrlListDataItem, productNodeRef, MESSAGE_MISSING_TARE, RequirementDataType.Packaging);
			}
		}
	}

	private void addMessingReq(List<ReqCtrlListDataItem> reqCtrlListDataItem, NodeRef sourceNodeRef, String reqMsg, RequirementDataType reqDataType) {
		MLText message = MLTextHelper.getI18NMessage(reqMsg);
		ArrayList<NodeRef> sources = new ArrayList<>(1);
		if (sourceNodeRef != null) {
			sources.add(sourceNodeRef);
		}
		reqCtrlListDataItem.add(new ReqCtrlListDataItem(null, RequirementType.Forbidden, message, null, sources, reqDataType));
	}
}
