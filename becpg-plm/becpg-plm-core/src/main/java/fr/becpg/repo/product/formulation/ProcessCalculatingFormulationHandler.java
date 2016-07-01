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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ResourceProductData;
import fr.becpg.repo.product.data.constraints.ProcessListUnit;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.data.productList.ResourceParamListItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.variant.filters.VariantFilters;

public class ProcessCalculatingFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(ProcessCalculatingFormulationHandler.class);

	private AlfrescoRepository<ResourceProductData> alfrescoRepository;
	
	private PackagingHelper packagingHelper;

	public void setAlfrescoRepository(AlfrescoRepository<ResourceProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setPackagingHelper(PackagingHelper packagingHelper) {
		this.packagingHelper = packagingHelper;
	}

	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {

		logger.debug("process calculating visitor");

		// no compo => no formulation
		if (!formulatedProduct.hasProcessListEl(new VariantFilters<>())) {
			logger.debug("no process => no formulation");
			return true;
		}
		
		if(formulatedProduct.getDefaultVariantPackagingData() == null){
			formulatedProduct.setDefaultVariantPackagingData(packagingHelper.getDefaultVariantPackagingData(formulatedProduct));
		}
		
		if (formulatedProduct instanceof ResourceProductData || formulatedProduct.getResourceParamList()!=null) {

			int sort = 0;

			List<ResourceParamListItem> toAdd = new LinkedList<>();
			// Keep local param
			for (ResourceParamListItem param : formulatedProduct.getResourceParamList()) {
				if (param.getResource() == null) {
					param.setSort(sort++);
					toAdd.add(param);
				}
			}

			for (ProcessListDataItem p : formulatedProduct.getProcessList(new VariantFilters<>())) {
				if (p.getResource() != null) {
					ResourceProductData resource = alfrescoRepository.findOne(p.getResource());
					for (ResourceParamListItem param : resource.getResourceParamList()) {
						boolean isFound = false;
						for (ResourceParamListItem param2 : formulatedProduct.getResourceParamList()) {
							if (Objects.equals(param2.getParam(), param.getParam()) && Objects.equals(param2.getResource(), resource.getNodeRef())
									&& Objects.equals(param2.getStep(), p.getStep()) && Objects.equals(param2.getParamType(), param.getParamType())) {
								param2.setSort(sort++);

								toAdd.add(param2);
								isFound = true;
							}
						}
						if (!isFound) {
							param.setSort(sort++);
							param.setName(null);
							param.setNodeRef(null);
							param.setParentNodeRef(null);
							param.setStep(p.getStep());
							param.setResource(resource.getNodeRef());
							toAdd.add(param);
						}
					}
				}

			}

			if (formulatedProduct.getEntityTpl() != null && !formulatedProduct.getEntityTpl().equals(formulatedProduct)) {
				// Set default params
				List<ResourceParamListItem> templatePl = formulatedProduct.getEntityTpl().getResourceParamList();
				if (templatePl != null) {
					for (ResourceParamListItem paramTpl : templatePl) {
						for (ResourceParamListItem param : formulatedProduct.getResourceParamList()) {
							if (Objects.equals(param.getParam(), paramTpl.getParam()) && Objects.equals(param.getResource(), paramTpl.getResource())
									&& Objects.equals(param.getStep(), paramTpl.getStep())
									&& Objects.equals(param.getParamType(), paramTpl.getParamType()) && paramTpl.getParamValue() != null
									&& !paramTpl.getParamValue().isEmpty()) {
								param.setParamValue(paramTpl.getParamValue());
							}
						}
					}

				}
			}

			formulatedProduct.getResourceParamList().clear();
			formulatedProduct.getResourceParamList().addAll(toAdd);
			if(formulatedProduct instanceof ResourceProductData){
				formulatedProduct.setUnit(ProductUnit.h);
			}

		}

		// visit resources and steps from the end to the beginning
		for (ProcessListDataItem p : formulatedProduct.getProcessList(new VariantFilters<>())) {

			if (p.getRateResource() != null) {
				if (ProcessListUnit.P.equals(p.getUnit())) {
					p.setRateProduct(p.getRateResource());
				}
				else if(ProcessListUnit.Box.equals(p.getUnit())){
					if(formulatedProduct.getDefaultVariantPackagingData() != null && formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes() != null){
						p.setRateProduct(p.getRateResource() * formulatedProduct.getDefaultVariantPackagingData().getProductPerBoxes());
					}
					else{
						String message = I18NUtil.getMessage(FormulationHelper.MISSING_NUMBER_OF_PRODUCT_PER_BOX);
						formulatedProduct.getProcessListView().getReqCtrlList().add(new ReqCtrlListDataItem(null, 
								RequirementType.Forbidden, 
								message, 
								null, new ArrayList<NodeRef>(), RequirementDataType.Packaging));
					}					 
				}
				else {
					Double productQtyToTransform = p.getQty() != null ? p.getQty() : FormulationHelper.getNetWeight(formulatedProduct, null);										
					if (productQtyToTransform != null) {
						p.setRateProduct(p.getRateResource() / productQtyToTransform);
					} else {
						p.setRateProduct(null);
					}
				}
			} else {
				p.setRateProduct(null);
			}
		}
		return true;

	}

	
}
