/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score;

import java.util.LinkedHashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.model.GS1Model;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;

/**
 * Reads the documented characteristics of a product, indexed by their code.
 *
 * <p>Every front of pack scheme starts by asking the same question, so it is asked once
 * here rather than in each of them. Rounding follows what the product declares, because
 * several regulations compute on the value printed on the pack.</p>
 *
 * @author matthieu
 */
@Service("nutrientValueProvider")
public class NutrientValueProvider {

	/** Constant <code>EU_ROUNDING="EU"</code> */
	private static final String EU_ROUNDING = "EU";

	private final NodeService nodeService;

	/**
	 * <p>Constructor for NutrientValueProvider.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	@Autowired
	public NutrientValueProvider(@Qualifier("nodeService") NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Returns the nutrients and the physico chemical characteristics of a product,
	 * indexed by their code.</p>
	 *
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object
	 * @return a {@link java.util.Map} object, never null
	 */
	public Map<String, Double> extractNutrients(ProductData product) {
		return extractNutrients(product, ScoreBasis.Per100g);
	}

	/**
	 * <p>Returns the characteristics of a product, converted to the quantity a score refers
	 * to.</p>
	 *
	 * <p>Only the nutrients are converted: a percentage such as the fruit and vegetable
	 * content of the physico chemical list does not depend on the quantity.</p>
	 *
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param basis the quantity the score refers to
	 * @return a {@link java.util.Map} object, never null
	 */
	public Map<String, Double> extractNutrients(ProductData product, ScoreBasis basis) {
		Map<String, Double> values = new LinkedHashMap<>();

		fillNutrients(product, values);
		convert(values, basis.conversionFactor(product));
		fillPhysicoChems(product, values);

		return values;
	}

	/**
	 * <p>convert.</p>
	 *
	 * @param values the nutrients held per 100 g
	 * @param factor the conversion factor of the basis
	 */
	private void convert(Map<String, Double> values, double factor) {
		if (factor == 1d) {
			return;
		}
		values.replaceAll((code, value) -> value * factor);
	}

	/**
	 * <p>fillNutrients.</p>
	 *
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param values a {@link java.util.Map} object
	 */
	private void fillNutrients(ProductData product, Map<String, Double> values) {
		if (product.getNutList() == null) {
			return;
		}

		for (NutListDataItem nutList : product.getNutList()) {
			if (nutList.getNut() == null) {
				continue;
			}

			String code = (String) nodeService.getProperty(nutList.getNut(), GS1Model.PROP_NUTRIENT_TYPE_CODE);
			Double value = extractValue(product, nutList);

			if ((code != null) && (value != null)) {
				values.put(code, value);
			}
		}
	}

	/**
	 * <p>extractValue.</p>
	 *
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param nutList a {@link fr.becpg.repo.product.data.productList.NutListDataItem} object
	 * @return a {@link java.lang.Double} object
	 */
	private Double extractValue(ProductData product, NutListDataItem nutList) {
		if (product.useUnroundedNutrients()) {
			return product.isPrepared() && (nutList.getPreparedValue() != null) ? nutList.getPreparedValue() : nutList.getValue();
		}
		return product.isPrepared() && (nutList.preparedValue(EU_ROUNDING) != null) ? nutList.preparedValue(EU_ROUNDING)
				: nutList.value(EU_ROUNDING);
	}

	/**
	 * The schemes reading a fruit and vegetable content find it in the physico chemical
	 * list, which is where beCPG stores it.
	 *
	 * @param product a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param values a {@link java.util.Map} object
	 */
	private void fillPhysicoChems(ProductData product, Map<String, Double> values) {
		if (product.getPhysicoChemList() == null) {
			return;
		}

		for (PhysicoChemListDataItem physicoChem : product.getPhysicoChemList()) {
			if (physicoChem.getPhysicoChem() == null) {
				continue;
			}

			String code = (String) nodeService.getProperty(physicoChem.getPhysicoChem(), PLMModel.PROP_PHYSICO_CHEM_CODE);

			if ((code != null) && (physicoChem.getValue() != null)) {
				values.put(code, physicoChem.getValue());
			}
		}
	}

}
