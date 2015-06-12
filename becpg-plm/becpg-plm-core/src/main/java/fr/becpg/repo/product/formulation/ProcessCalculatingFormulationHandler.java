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
package fr.becpg.repo.product.formulation;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ResourceProductData;
import fr.becpg.repo.product.data.constraints.ProcessListUnit;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.data.productList.ResourceParamListItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.variant.filters.VariantFilters;

public class ProcessCalculatingFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static Log logger = LogFactory.getLog(ProcessCalculatingFormulationHandler.class);

	private AlfrescoRepository<ResourceProductData> alfrescoRepository;

	public void setAlfrescoRepository(AlfrescoRepository<ResourceProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {

		logger.debug("process calculating visitor");

		// no compo => no formulation
		if (!formulatedProduct.hasProcessListEl(EffectiveFilters.ALL, VariantFilters.DEFAULT_VARIANT)) {
			logger.debug("no process => no formulation");
			return true;
		}

		if (formulatedProduct instanceof ResourceProductData) {

			int sort = 0;

			List<ResourceParamListItem> toAdd = new LinkedList<>();
			// Keep local param
			for (ResourceParamListItem param : ((ResourceProductData) formulatedProduct).getResourceParamList()) {
				if (param.getResource() == null) {
					param.setSort(sort++);
					toAdd.add(param);
				}
			}

			for (ProcessListDataItem p : formulatedProduct.getProcessList(EffectiveFilters.ALL, VariantFilters.DEFAULT_VARIANT)) {
				if (p.getResource() != null) {
					ResourceProductData resource = alfrescoRepository.findOne(p.getResource());
					for (ResourceParamListItem param : resource.getResourceParamList()) {
						boolean isFound = false;
						for (ResourceParamListItem param2 : ((ResourceProductData) formulatedProduct).getResourceParamList()) {
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

			if (formulatedProduct.getEntityTpl() != null) {
				// Set default params
				List<ResourceParamListItem> templatePl = ((ResourceProductData) formulatedProduct.getEntityTpl()).getResourceParamList();
				if (templatePl != null) {
					for (ResourceParamListItem paramTpl : templatePl) {
						for (ResourceParamListItem param : ((ResourceProductData) formulatedProduct).getResourceParamList()) {
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

			((ResourceProductData) formulatedProduct).getResourceParamList().clear();
			((ResourceProductData) formulatedProduct).getResourceParamList().addAll(toAdd);
			formulatedProduct.setUnit(ProductUnit.h);

		}

		// visit resources and steps from the end to the beginning

		for (ProcessListDataItem p : formulatedProduct.getProcessList(EffectiveFilters.ALL, VariantFilters.DEFAULT_VARIANT)) {

			if (p.getRateResource() != null && p.getQtyResource() != null) {
				if (ProcessListUnit.P.equals(p.getUnit())) {
					p.setRateProduct(p.getRateResource());
				} else {
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
