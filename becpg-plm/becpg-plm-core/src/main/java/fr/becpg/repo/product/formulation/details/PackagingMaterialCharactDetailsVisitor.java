/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG.
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
package fr.becpg.repo.product.formulation.details;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.data.CharactDetails;
import fr.becpg.repo.product.data.CharactDetailsValue;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.PackagingMaterialData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackMaterialListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;
import fr.becpg.repo.variant.filters.VariantFilters;

/**
 * <p>
 * PackagingMaterialCharactDetailsVisitor class.
 * </p>
 *
 * @author evelyne
 * @version $Id: $Id
 */
@Service
public class PackagingMaterialCharactDetailsVisitor extends SimpleCharactDetailsVisitor {

	private static final Log logger = LogFactory.getLog(PackagingMaterialCharactDetailsVisitor.class);

	/** {@inheritDoc} */
	@Override
	public CharactDetails visit(ProductData formulatedProduct, List<NodeRef> dataListItems, Integer level) throws FormulateException {

		CharactDetails ret = createCharactDetails(dataListItems);

		if (level == null) {
			level = 0;
		}
		/*
		 * CompoList
		 */
		Double netQty = FormulationHelper.getNetQtyInLorKg(formulatedProduct, FormulationHelper.DEFAULT_NET_WEIGHT);
		Double netWeight = FormulationHelper.getNetWeight(formulatedProduct, FormulationHelper.DEFAULT_NET_WEIGHT);
		Double netVol = FormulationHelper.getNetVolume(formulatedProduct, FormulationHelper.DEFAULT_NET_WEIGHT);

		if ((formulatedProduct.getDropPackagingOfComponents() == null) || !formulatedProduct.getDropPackagingOfComponents()) {
			visitRecurPMaterial(formulatedProduct, ret, 0, level, netWeight, netVol, netQty);
		}

		/*
		 * PackagingList
		 */

		if (formulatedProduct.hasPackagingListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {

			for (PackagingListDataItem packagingListDataItem : formulatedProduct
					.getPackagingList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {
				visitMaterial(formulatedProduct.getNodeRef(), packagingListDataItem, ret, 0, 1);
			}
		}

		return ret;
	}

	private void visitMaterial(NodeRef parent, PackagingListDataItem packagingListDataItem, CharactDetails charactDetails, Integer currLevel,
			double subQty) throws FormulateException {

		if (nodeService.getType(packagingListDataItem.getProduct()).equals(PLMModel.TYPE_PACKAGINGKIT)) {
			if ((packagingListDataItem.getQty() != null) && ProductUnit.P.equals(packagingListDataItem.getPackagingListUnit())
					&& PackagingLevel.Primary.equals(packagingListDataItem.getPkgLevel())) {
				subQty *= packagingListDataItem.getQty();
			}
			ProductData packagingListDataItemProduct = (ProductData) alfrescoRepository.findOne(packagingListDataItem.getProduct());
			if (packagingListDataItemProduct.hasPackagingListEl()) {
				for (PackagingListDataItem p : packagingListDataItemProduct.getPackagingList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
					visitMaterial(parent, p, charactDetails, currLevel, subQty);
				}
			}
		} else {
			visitMaterialPackaging(parent, packagingListDataItem, charactDetails, currLevel, subQty);
		}

	}

	private void visitMaterialPackaging(NodeRef parent, PackagingListDataItem packagingListDataItem, CharactDetails charactDetails, Integer currLevel,
			double subQty) throws FormulateException {

		if ((packagingListDataItem.getProduct() != null) && PackagingLevel.Primary.equals(packagingListDataItem.getPkgLevel())) {

			@SuppressWarnings("unchecked")
			List<SimpleCharactDataItem> simpleCharactDataList = (List<SimpleCharactDataItem>) alfrescoRepository.loadDataList(parent, dataListType,
					dataListType);

			PackagingMaterialData packagingProduct = (PackagingMaterialData) alfrescoRepository.findOne(packagingListDataItem.getProduct());

			BigDecimal tare = FormulationHelper.getTareInKg(packagingListDataItem, packagingProduct).multiply(new BigDecimal(subQty*1000d));

			if (alfrescoRepository.hasDataList(packagingProduct, PackModel.PACK_MATERIAL_LIST_TYPE)
					&& (packagingProduct.getPackMaterialList() != null)) {

				for (PackMaterialListDataItem packMateriDataItem : packagingProduct.getPackMaterialList()) {
					if (packMateriDataItem.getPmlWeight() != null) {

						for (SimpleCharactDataItem simpleCharact : simpleCharactDataList) {
							if ((simpleCharact != null) && packMateriDataItem.getPmlMaterial().equals(simpleCharact.getCharactNodeRef())
									&& charactDetails.hasElement(simpleCharact.getCharactNodeRef())) {

								BigDecimal plmWeight = new BigDecimal(packMateriDataItem.getPmlWeight(), MathContext.DECIMAL64).multiply(tare);

								BigDecimal productTare = FormulationHelper.getTareInKg(packagingProduct);
								if(productTare!=null) {
									 plmWeight = plmWeight.divide(productTare.multiply(BigDecimal.valueOf(1000d)));
								}
								
								Double value = plmWeight.doubleValue();

								if ((value != null) && (simpleCharact.shouldDetailIfZero() || (value != 0d))) {
									if (logger.isDebugEnabled()) {
										logger.debug("Add new charact detail. Charact: "
												+ nodeService.getProperty(simpleCharact.getCharactNodeRef(), BeCPGModel.PROP_CHARACT_NAME)
												+ " - entityNodeRef: " + packagingProduct.getName() + " - value: " + value);
									}

									CharactDetailsValue currentCharactDetailsValue = new CharactDetailsValue(parent, packagingProduct.getNodeRef(),
											packagingListDataItem.getNodeRef(), value, currLevel, null);

									charactDetails.addKeyValue(simpleCharact.getCharactNodeRef(), currentCharactDetailsValue);

								}

							}
						}
					}
				}

			} else if ((packagingProduct.getPackagingMaterials() != null) && (packagingProduct.getPackagingMaterials().size() > 0)) {

				for (SimpleCharactDataItem simpleCharact : simpleCharactDataList) {
					if ((simpleCharact != null) && packagingProduct.getPackagingMaterials().contains(simpleCharact.getCharactNodeRef())
							&& charactDetails.hasElement(simpleCharact.getCharactNodeRef())) {

						BigDecimal tareByMaterial = tare
								.divide(new BigDecimal(packagingProduct.getPackagingMaterials().size(), MathContext.DECIMAL64));

						Double value = tareByMaterial.doubleValue();

						if ((value != null) && (simpleCharact.shouldDetailIfZero() || (value != 0d))) {
							if (logger.isDebugEnabled()) {
								logger.debug("Add new charact detail. Charact: "
										+ nodeService.getProperty(simpleCharact.getCharactNodeRef(), BeCPGModel.PROP_CHARACT_NAME)
										+ " - entityNodeRef: " + packagingProduct.getName() + " - value: " + value);
							}

							CharactDetailsValue currentCharactDetailsValue = new CharactDetailsValue(parent, packagingProduct.getNodeRef(),
									packagingListDataItem.getNodeRef(), value, currLevel, null);

							charactDetails.addKeyValue(simpleCharact.getCharactNodeRef(), currentCharactDetailsValue);

						}
					}
				}
			}
		}
	}

	/**
	 * <p>
	 * visitRecurPMaterial.
	 * </p>
	 *
	 * @param subProductData
	 *            a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param ret
	 *            a {@link fr.becpg.repo.product.data.CharactDetails} object.
	 * @param currLevel
	 *            a {@link java.lang.Integer} object.
	 * @param maxLevel
	 *            a {@link java.lang.Integer} object.
	 * @param subWeight
	 *            a {@link java.lang.Double} object.
	 * @param subVol
	 *            a {@link java.lang.Double} object.
	 * @param netQty
	 *            a {@link java.lang.Double} object.
	 * @return a {@link fr.becpg.repo.product.data.CharactDetails} object.
	 * @throws fr.becpg.repo.formulation.FormulateException
	 *             if any.
	 */
	public CharactDetails visitRecurPMaterial(ProductData subProductData, CharactDetails ret, Integer currLevel, Integer maxLevel, Double subWeight,
			Double subVol, Double netQty) throws FormulateException {

		if (subProductData.hasCompoListEl(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {

			for (CompoListDataItem compoListDataItem : subProductData
					.getCompoList(Arrays.asList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE), new VariantFilters<>()))) {

				Double weightUsed = FormulationHelper.getQtyInKg(compoListDataItem);
				Double netWeight = FormulationHelper.getNetWeight(subProductData, FormulationHelper.DEFAULT_NET_WEIGHT);

				if ((netWeight != 0d) && (subWeight != null)) {
					weightUsed = (weightUsed / netWeight) * subWeight;
				}

				Double volUsed = FormulationHelper.getNetVolume(compoListDataItem, subProductData);
				Double netVol = FormulationHelper.getNetVolume(subProductData, FormulationHelper.DEFAULT_NET_WEIGHT);
				if ((volUsed != null) && (netVol != null) && (netVol != 0d) && (subVol != null)) {
					volUsed = (volUsed / netVol) * subVol;
				}

				ProductData compoListProduct = (ProductData) alfrescoRepository.findOne(compoListDataItem.getProduct());

				// get compoProduct qty
				Double compoProductQty = compoListProduct.getQty();
				if (compoProductQty == null) {
					compoProductQty = 1d;
				}

				if (compoListProduct.getUnit().isP()) {
					if ((compoListProduct.getUnit() != null) && !compoListProduct.getUnit().isP()) {
						compoProductQty = 1d;
					}

				} else if (compoListProduct.getUnit().isWeight() || compoListProduct.getUnit().isVolume()) {

					compoProductQty = FormulationHelper.getNetQtyInLorKg(compoListProduct, 1d);
				}

				visitPart(subProductData.getNodeRef(), compoListProduct, compoListDataItem.getNodeRef(), ret, weightUsed, volUsed, compoProductQty,
						compoProductQty, currLevel, null);
				if (((maxLevel < 0) || (currLevel < maxLevel))
						&& !entityDictionaryService.isMultiLevelLeaf(nodeService.getType(compoListDataItem.getProduct()))) {
					visitRecur(compoListProduct, ret, currLevel + 1, maxLevel, weightUsed, volUsed, netQty);
				}

			}
		}

		return ret;
	}

}
