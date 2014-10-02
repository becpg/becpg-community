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
			if (formulatedProduct.getEntityTpl() != null) {

				List<ProcessListDataItem> templatePl = formulatedProduct.getEntityTpl().getProcessListView().getProcessList();
				if (templatePl != null) {
					for (ProcessListDataItem plLItem : templatePl) {
						if (plLItem.getResource() != null) {
							boolean isFound = false;
							for (ProcessListDataItem sl : formulatedProduct.getProcessListView().getProcessList()) {
								if (Objects.equals(sl.getProduct(), plLItem.getProduct()) && Objects.equals(sl.getResource(), plLItem.getResource())
										&& Objects.equals(sl.getStep(), plLItem.getStep())) {
									isFound = true;
									break;
								}
							}
							if (!isFound) {
								plLItem.setNodeRef(null);
								plLItem.setParentNodeRef(null);
								formulatedProduct.getProcessListView().getProcessList().add(plLItem);
							}
						}
					}
				}
			}

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

			((ResourceProductData) formulatedProduct).getResourceParamList().clear();
			((ResourceProductData) formulatedProduct).getResourceParamList().addAll(toAdd);
			formulatedProduct.setUnit(ProductUnit.h);

		}

		// visit resources and steps from the end to the beginning

		for (ProcessListDataItem p : formulatedProduct.getProcessList(EffectiveFilters.ALL, VariantFilters.DEFAULT_VARIANT)) {

			if (p.getRateResource() != null && p.getQtyResource() != null) {
				if (ProcessListUnit.P.equals(p.getUnit())) {
					p.setRateProduct(p.getQtyResource() * p.getRateResource());
				} else {
					Double productQtyToTransform = p.getQty() != null ? p.getQty() : FormulationHelper.getNetWeight(formulatedProduct, null);
					if (productQtyToTransform != null) {

						p.setRateProduct(p.getQtyResource() * p.getRateResource() / productQtyToTransform);
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
