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
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.PLMModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.PackagingKitData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ResourceProductData;
import fr.becpg.repo.product.data.constraints.PackagingListUnit;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompositionDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.variant.filters.VariantFilters;

public class ProductFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final String MESSAGE_MISSING_NET_WEIGHT = "message.formulate.missing.netWeight";
	private static final String MESSAGE_MISSING_QTY = "message.formulate.missing.qty";
	private static final String MESSAGE_MISSING_UNIT = "message.formulate.missing.unit";
	private static final String MESSAGE_MISSING_DENSITY = "message.formulate.missing.density";
	private static final String MESSAGE_WRONG_UNIT = "message.formulate.wrong.unit";
	private static final String MESSAGE_MISSING_TARE = "message.formulate.missing.tare";

	protected static Log logger = LogFactory.getLog(ProductFormulationHandler.class);

	private NodeService nodeService;

	private ProductService productService;

	private AlfrescoRepository<ProductData> alfrescoRepository;

	private boolean formulateChildren = false;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
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

	@SuppressWarnings("unchecked")
	@Override
	public boolean process(ProductData productData) throws FormulateException {

		if ((productData.hasCompoListEl(EffectiveFilters.ALL, VariantFilters.DEFAULT_VARIANT))
				|| (productData.hasPackagingListEl(EffectiveFilters.ALL, VariantFilters.DEFAULT_VARIANT))
				|| (productData.hasProcessListEl(EffectiveFilters.ALL, VariantFilters.DEFAULT_VARIANT))) {

			if (productData.hasCompoListEl(EffectiveFilters.ALL, VariantFilters.DEFAULT_VARIANT)) {
				if (productData.getCompoListView().getReqCtrlList() != null) {
					clearReqCltrlList(productData.getCompoListView().getReqCtrlList());
				} else {
					productData.getCompoListView().setReqCtrlList(new LinkedList<ReqCtrlListDataItem>());
				}
			}
			if (productData.hasPackagingListEl(EffectiveFilters.ALL, VariantFilters.DEFAULT_VARIANT)) {
				if (productData.getPackagingListView().getReqCtrlList() != null) {
					clearReqCltrlList(productData.getPackagingListView().getReqCtrlList());
				} else {
					productData.getPackagingListView().setReqCtrlList(new LinkedList<ReqCtrlListDataItem>());
				}
			}
			if (productData.hasProcessListEl(EffectiveFilters.ALL, VariantFilters.DEFAULT_VARIANT)) {
				if (productData.getProcessListView().getReqCtrlList() != null) {
					clearReqCltrlList(productData.getProcessListView().getReqCtrlList());
				} else {
					productData.getProcessListView().setReqCtrlList(new LinkedList<ReqCtrlListDataItem>());
				}
			}

			if (formulateChildren) {
				checkShouldFormulateComponents(true, productData, new HashSet<NodeRef>());
			}

			checkMissingProperties(productData);

			// Continue
			return true;
		}

		// Reset
		if (productData.getCompoListView() != null && productData.getCompoListView().getReqCtrlList() != null) {
			productData.getCompoListView().getReqCtrlList().clear();
		}
		if (productData.getPackagingListView() != null && productData.getPackagingListView().getReqCtrlList() != null) {
			productData.getPackagingListView().getReqCtrlList().clear();
		}
		if (productData.getProcessListView() != null && productData.getProcessListView().getReqCtrlList() != null) {
			productData.getProcessListView().getReqCtrlList().clear();
		}

		// Skip formulation
		return true;
	}

	private void clearReqCltrlList(List<ReqCtrlListDataItem> reqCtrlList) {
		if (reqCtrlList != null) {
			for (Iterator<ReqCtrlListDataItem> iterator = reqCtrlList.iterator(); iterator.hasNext();) {
				ReqCtrlListDataItem reqCtrlListDataItem = (ReqCtrlListDataItem) iterator.next();
				if (reqCtrlListDataItem.getNodeRef() == null) {
					iterator.remove();
				}
			}
		}

	}

	@SuppressWarnings("unchecked")
	private boolean checkShouldFormulateComponents(boolean isRoot, ProductData productData, Set<NodeRef> checkedProducts) throws FormulateException {
		boolean isFormulated = false;

		if (logger.isDebugEnabled()) {
			logger.debug("checkShouldFormulateComponents: " + productData.getName());
		}

		if (!checkedProducts.contains(productData.getNodeRef())) {
			checkedProducts.add(productData.getNodeRef());

			Set<CompositionDataItem> compositionDataItems = new HashSet<>();
			compositionDataItems.addAll(productData.getCompoList());
			compositionDataItems.addAll(productData.getPackagingList());

			if (!compositionDataItems.isEmpty()) {

				boolean shouldFormulate = false;
				for (CompositionDataItem c : compositionDataItems) {
					ProductData p = alfrescoRepository.findOne(c.getProduct());
					// recursive
					if (checkShouldFormulateComponents(false, p, checkedProducts)) {
						shouldFormulate = true;
					}

					// check modified date on component
					Date modified = (Date) nodeService.getProperty(c.getProduct(), ContentModel.PROP_MODIFIED);
					if (modified == null || productData.getFormulatedDate() == null || modified.getTime() > productData.getFormulatedDate().getTime()) {
						shouldFormulate = true;
					}
				}

				if (!isRoot && (shouldFormulate || productService.shouldFormulate(productData.getNodeRef()))) {

					if (logger.isDebugEnabled()) {
						logger.debug("auto-formulate: " + productData.getName());
					}
					productService.formulate(productData);
					alfrescoRepository.save(productData);
					isFormulated = true;
				}
			}

		}

		return isFormulated;
	}

	@SuppressWarnings("unchecked")
	private void checkMissingProperties(ProductData formulatedProduct) {

		checkFormulatedProduct(formulatedProduct);

		if (formulatedProduct.hasCompoListEl(EffectiveFilters.ALL, VariantFilters.DEFAULT_VARIANT)) {
			for (CompoListDataItem c : formulatedProduct.getCompoList()) {
				if (c.getCompoListUnit() == null) {
					addMessingReq(formulatedProduct.getCompoListView().getReqCtrlList(), null, MESSAGE_WRONG_UNIT);
				} else {
					checkCompositionItem(formulatedProduct.getCompoListView().getReqCtrlList(), c.getProduct(), c);
				}
			}
		}
		if (formulatedProduct.hasPackagingListEl(EffectiveFilters.ALL, VariantFilters.DEFAULT_VARIANT)) {
			for (PackagingListDataItem p : formulatedProduct.getPackagingList()) {
				if (p.getPackagingListUnit() == null) {
					addMessingReq(formulatedProduct.getCompoListView().getReqCtrlList(), null, MESSAGE_WRONG_UNIT);
				} else {
					checkPackagingItem(formulatedProduct.getPackagingListView().getReqCtrlList(), p);
				}

			}
		}
	}

	private void checkFormulatedProduct(ProductData formulatedProduct) {

		NodeRef productNodeRef = formulatedProduct.getNodeRef();
		List<ReqCtrlListDataItem> reqCtrlList = null;
		if (formulatedProduct instanceof PackagingKitData) {
			reqCtrlList = formulatedProduct.getPackagingListView().getReqCtrlList();

		} else {
			if (!(formulatedProduct instanceof ResourceProductData)) {
				Double qty = formulatedProduct.getQty();
				if (qty == null || qty.equals(0d)) {
					addMessingReq(formulatedProduct.getCompoListView().getReqCtrlList(), productNodeRef, MESSAGE_MISSING_QTY);
				}
				Double netWeight = FormulationHelper.getNetWeight(formulatedProduct, null);
				if (netWeight == null || netWeight.equals(0d)) {
					addMessingReq(formulatedProduct.getCompoListView().getReqCtrlList(), productNodeRef, MESSAGE_MISSING_NET_WEIGHT);
				}
			}

			reqCtrlList = formulatedProduct.getCompoListView().getReqCtrlList();

		}
		if (!(formulatedProduct instanceof ResourceProductData)) {
			ProductUnit productUnit = formulatedProduct.getUnit();
			if (productUnit == null) {
				addMessingReq(reqCtrlList, productNodeRef, MESSAGE_MISSING_UNIT);
			}
		}
	}

	private void checkCompositionItem(List<ReqCtrlListDataItem> reqCtrlListDataItem, NodeRef productNodeRef, CompoListDataItem c) {

		if (!PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT.isMatch(nodeService.getType(productNodeRef))) {

			ProductUnit productUnit = FormulationHelper.getProductUnit(productNodeRef, nodeService);
			if (c != null) {
				if (FormulationHelper.isCompoUnitP(c.getCompoListUnit())) {
					checkNetWeight(reqCtrlListDataItem, productNodeRef);
				} else {
					boolean shouldUseLiter = FormulationHelper.isProductUnitLiter(productUnit);
					boolean useLiter = FormulationHelper.isCompoUnitLiter(c.getCompoListUnit());

					if (shouldUseLiter && !useLiter || !shouldUseLiter && useLiter) {
						addMessingReq(reqCtrlListDataItem, productNodeRef, MESSAGE_WRONG_UNIT);
					}
				}
				Double overrunPerc = c.getOverrunPerc();
				if (FormulationHelper.isProductUnitLiter(productUnit) || overrunPerc != null) {
					Double density = FormulationHelper.getDensity(productNodeRef, nodeService);
					if (density == null || density.equals(0d)) {
						addMessingReq(reqCtrlListDataItem, productNodeRef, MESSAGE_MISSING_DENSITY);
					}
				}
			}
		}
	}

	private void checkNetWeight(List<ReqCtrlListDataItem> reqCtrlListDataItem, NodeRef productNodeRef) {
		Double netWeight = FormulationHelper.getNetWeight(productNodeRef, nodeService, null);
		if (netWeight == null || netWeight.equals(0d)) {
			addMessingReq(reqCtrlListDataItem, productNodeRef, MESSAGE_MISSING_NET_WEIGHT);
		}
	}

	private void checkPackagingItem(List<ReqCtrlListDataItem> reqCtrlListDataItem, PackagingListDataItem p) {
		NodeRef productNodeRef = p.getProduct();
		ProductUnit productUnit = FormulationHelper.getProductUnit(productNodeRef, nodeService);
		if (productUnit == null) {
			addMessingReq(reqCtrlListDataItem, productNodeRef, MESSAGE_MISSING_UNIT);
		} else {
			if ((p.getPackagingListUnit().equals(PackagingListUnit.kg) || p.getPackagingListUnit().equals(PackagingListUnit.g))
					&& !FormulationHelper.isProductUnitKg(productUnit)) {
				addMessingReq(reqCtrlListDataItem, productNodeRef, MESSAGE_WRONG_UNIT);
			}

			if ((p.getPackagingListUnit().equals(PackagingListUnit.P) || p.getPackagingListUnit().equals(PackagingListUnit.PP))
					&& !productUnit.equals(ProductUnit.P)) {
				addMessingReq(reqCtrlListDataItem, productNodeRef, MESSAGE_WRONG_UNIT);
			}

			if ((p.getPackagingListUnit().equals(PackagingListUnit.m) && !productUnit.equals(ProductUnit.m))
					|| (p.getPackagingListUnit().equals(PackagingListUnit.m2) && !productUnit.equals(ProductUnit.m2))) {
				addMessingReq(reqCtrlListDataItem, productNodeRef, MESSAGE_WRONG_UNIT);
			}
		}

		if (!nodeService.hasAspect(p.getProduct(), PackModel.ASPECT_PALLET)) {
			Double tare = FormulationHelper.getTareInKg(productNodeRef, nodeService);
			if (tare == null) {
				addMessingReq(reqCtrlListDataItem, productNodeRef, MESSAGE_MISSING_TARE);
			}
		}
	}

	private void addMessingReq(List<ReqCtrlListDataItem> reqCtrlListDataItem, NodeRef sourceNodeRef, String reqMsg) {
		String message = I18NUtil.getMessage(reqMsg);
		ArrayList<NodeRef> sources = new ArrayList<NodeRef>(1);
		if (sourceNodeRef != null) {
			sources.add(sourceNodeRef);
		}
		reqCtrlListDataItem.add(new ReqCtrlListDataItem(null, RequirementType.Forbidden, message, sources));
	}
}
