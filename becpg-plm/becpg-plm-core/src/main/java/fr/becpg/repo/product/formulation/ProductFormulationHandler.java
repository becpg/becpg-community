/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG.
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
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.util.StopWatch;

import fr.becpg.model.PLMModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.PackagingKitData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ResourceProductData;
import fr.becpg.repo.product.data.constraints.PackagingListUnit;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompositionDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.variant.filters.VariantFilters;

public class ProductFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final String MESSAGE_MISSING_UNIT = "message.formulate.missing.unit";
	private static final String MESSAGE_MISSING_DENSITY = "message.formulate.missing.density";
	private static final String MESSAGE_WRONG_UNIT = "message.formulate.wrong.unit";
	private static final String MESSAGE_MISSING_TARE = "message.formulate.missing.tare";

	protected static final Log logger = LogFactory.getLog(ProductFormulationHandler.class);

	private NodeService nodeService;

	private ProductService productService;

	private AlfrescoRepository<ProductData> alfrescoRepository;

	private LockService lockService;

	private boolean formulateChildren = false;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setLockService(LockService lockService) {
		this.lockService = lockService;
	}

	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setFormulateChildren(boolean formulateChildren) {
		this.formulateChildren = formulateChildren;
	}

	@Override
	public boolean process(ProductData productData) throws FormulateException {

		if ((productData.hasCompoListEl(new VariantFilters<>())) || (productData.hasPackagingListEl(new VariantFilters<>()))
				|| (productData.hasProcessListEl(new VariantFilters<>()))) {

			if (productData.hasCompoListEl(new VariantFilters<>())) {
				if (productData.getCompoListView().getReqCtrlList() != null) {
					clearReqCltrlList(productData.getCompoListView().getReqCtrlList());
				} else {
					productData.getCompoListView().setReqCtrlList(new LinkedList<ReqCtrlListDataItem>());
				}
			}
			if (productData.hasPackagingListEl(new VariantFilters<>())) {
				if (productData.getPackagingListView().getReqCtrlList() != null) {
					clearReqCltrlList(productData.getPackagingListView().getReqCtrlList());
				} else {
					productData.getPackagingListView().setReqCtrlList(new LinkedList<ReqCtrlListDataItem>());
				}
			}
			if (productData.hasProcessListEl(new VariantFilters<>())) {
				if (productData.getProcessListView().getReqCtrlList() != null) {
					clearReqCltrlList(productData.getProcessListView().getReqCtrlList());
				} else {
					productData.getProcessListView().setReqCtrlList(new LinkedList<ReqCtrlListDataItem>());
				}
			}

			if (formulateChildren) {
				checkShouldFormulateComponents(true, productData);
			}

			checkMissingProperties(productData);

			// Continue
			return true;
		}

		// Reset
		if ((productData.getCompoListView() != null) && (productData.getCompoListView().getReqCtrlList() != null)) {
			productData.getCompoListView().getReqCtrlList().clear();
		}
		if ((productData.getPackagingListView() != null) && (productData.getPackagingListView().getReqCtrlList() != null)) {
			productData.getPackagingListView().getReqCtrlList().clear();
		}
		if ((productData.getProcessListView() != null) && (productData.getProcessListView().getReqCtrlList() != null)) {
			productData.getProcessListView().getReqCtrlList().clear();
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

			if ((productData.getNodeRef() == null) || (lockService.getLockStatus(productData.getNodeRef()) == LockStatus.NO_LOCK)) {

				boolean shouldFormulate = false;

				
				if (productData.getCompoList() != null) {
					for (CompositionDataItem c : productData.getCompoList()) {
						if ((c.getComponent() != null)) {
							ProductData subComponent = alfrescoRepository.findOne(c.getComponent());
							if (checkShouldFormulateComponents(false, subComponent) || ((productData.getFormulatedDate() == null)
									|| (productData.getFormulatedDate().getTime() < subComponent.getFormulatedDate().getTime()))) {
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
									|| (productData.getFormulatedDate().getTime() < subComponent.getFormulatedDate().getTime()))) {
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
									|| (productData.getFormulatedDate().getTime() < subComponent.getFormulatedDate().getTime()))) {
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

		if (formulatedProduct.hasCompoListEl(new VariantFilters<>())) {
			for (CompoListDataItem c : formulatedProduct.getCompoList()) {
				if (c.getCompoListUnit() == null) {
					addMessingReq(formulatedProduct.getCompoListView().getReqCtrlList(), null, MESSAGE_WRONG_UNIT, RequirementDataType.Composition);
				} else {
					checkCompositionItem(formulatedProduct.getCompoListView().getReqCtrlList(), c.getProduct(), c);
				}
			}
		}
		if (formulatedProduct.hasPackagingListEl(new VariantFilters<>())) {
			for (PackagingListDataItem p : formulatedProduct.getPackagingList()) {
				if (p.getPackagingListUnit() == null) {
					addMessingReq(formulatedProduct.getCompoListView().getReqCtrlList(), null, MESSAGE_WRONG_UNIT, RequirementDataType.Packaging);
				} else {
					checkPackagingItem(formulatedProduct.getPackagingListView().getReqCtrlList(), p);
				}

			}
		}
	}

	private void checkFormulatedProduct(ProductData formulatedProduct) {

		NodeRef productNodeRef = formulatedProduct.getNodeRef();
		List<ReqCtrlListDataItem> reqCtrlList;
		if (formulatedProduct instanceof PackagingKitData) {
			reqCtrlList = formulatedProduct.getPackagingListView().getReqCtrlList();

		} else {
			reqCtrlList = formulatedProduct.getCompoListView().getReqCtrlList();
		}
		if (!(formulatedProduct instanceof ResourceProductData)) {
			ProductUnit productUnit = formulatedProduct.getUnit();
			if (productUnit == null) {
				addMessingReq(reqCtrlList, productNodeRef, MESSAGE_MISSING_UNIT, RequirementDataType.Nutrient);
			}
		}
	}

	private void checkCompositionItem(List<ReqCtrlListDataItem> reqCtrlListDataItem, NodeRef productNodeRef, CompoListDataItem c) {

		if (!PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT.isMatch(nodeService.getType(productNodeRef))) {

			ProductUnit productUnit = FormulationHelper.getProductUnit(productNodeRef, nodeService);
			if (c != null) {
				boolean shouldUseLiter = FormulationHelper.isProductUnitLiter(productUnit);
				boolean useLiter = FormulationHelper.isCompoUnitLiter(c.getCompoListUnit());
				Double density = FormulationHelper.getDensity(productNodeRef, nodeService);

				if ((density == null) && ((shouldUseLiter && !useLiter) || (!shouldUseLiter && useLiter))) {
					addMessingReq(reqCtrlListDataItem, productNodeRef, MESSAGE_WRONG_UNIT, RequirementDataType.Composition);
					addMessingReq(reqCtrlListDataItem, productNodeRef, MESSAGE_MISSING_DENSITY, RequirementDataType.Composition);
				}
				Double overrunPerc = c.getOverrunPerc();
				if (FormulationHelper.isProductUnitLiter(productUnit) || (overrunPerc != null)) {
					if ((density == null) || density.equals(0d)) {
						addMessingReq(reqCtrlListDataItem, productNodeRef, MESSAGE_MISSING_DENSITY, RequirementDataType.Composition);
					}
				}
			}
		}
	}

	private void checkPackagingItem(List<ReqCtrlListDataItem> reqCtrlListDataItem, PackagingListDataItem p) {
		NodeRef productNodeRef = p.getProduct();
		ProductUnit productUnit = FormulationHelper.getProductUnit(productNodeRef, nodeService);
		if (productUnit == null) {
			addMessingReq(reqCtrlListDataItem, productNodeRef, MESSAGE_MISSING_UNIT, RequirementDataType.Packaging);
		} else {
			if ((p.getPackagingListUnit().equals(PackagingListUnit.kg) || p.getPackagingListUnit().equals(PackagingListUnit.g))
					&& !FormulationHelper.isProductUnitKg(productUnit)) {
				addMessingReq(reqCtrlListDataItem, productNodeRef, MESSAGE_WRONG_UNIT, RequirementDataType.Packaging);
			}

			if ((p.getPackagingListUnit().equals(PackagingListUnit.P) || p.getPackagingListUnit().equals(PackagingListUnit.PP))
					&& !productUnit.equals(ProductUnit.P)) {
				addMessingReq(reqCtrlListDataItem, productNodeRef, MESSAGE_WRONG_UNIT, RequirementDataType.Packaging);
			}

			if ((p.getPackagingListUnit().equals(PackagingListUnit.m) && !productUnit.equals(ProductUnit.m))
					|| (p.getPackagingListUnit().equals(PackagingListUnit.m2) && !productUnit.equals(ProductUnit.m2))) {
				addMessingReq(reqCtrlListDataItem, productNodeRef, MESSAGE_WRONG_UNIT, RequirementDataType.Packaging);
			}
		}

		if (!nodeService.hasAspect(p.getProduct(), PackModel.ASPECT_PALLET)) {
			BigDecimal tare = FormulationHelper.getTareInKg(productNodeRef, nodeService);
			if (tare == null) {
				addMessingReq(reqCtrlListDataItem, productNodeRef, MESSAGE_MISSING_TARE, RequirementDataType.Packaging);
			}
		}
	}

	private void addMessingReq(List<ReqCtrlListDataItem> reqCtrlListDataItem, NodeRef sourceNodeRef, String reqMsg, RequirementDataType reqDataType) {
		String message = I18NUtil.getMessage(reqMsg);
		ArrayList<NodeRef> sources = new ArrayList<>(1);
		if (sourceNodeRef != null) {
			sources.add(sourceNodeRef);
		}
		reqCtrlListDataItem.add(new ReqCtrlListDataItem(null, RequirementType.Forbidden, message, null, sources, reqDataType));
	}
}
