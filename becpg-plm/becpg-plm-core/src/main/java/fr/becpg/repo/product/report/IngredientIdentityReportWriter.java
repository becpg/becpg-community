/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.product.report;

import java.io.Serializable;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.AttributeExtractorService;

/**
 * <p>Writes the identity of an ingredient on its ingredient line of the report data source: the identification codes
 * (CEE, CAS, EINECS and EC numbers) and the function the ingredient holds in the formula, as a product information
 * file lists them for each ingredient.</p>
 *
 * <pre>
 * &lt;ingList ingListIng="CITRIC ACID" ingCEECode="E330" casNumber="77-92-9" ceNumber="201-069-1" ecNumber="201-069-1"
 *          ingListFunctionality="pH adjusters" .../&gt;
 * </pre>
 *
 * <p>A code or a function the ingredient does not hold writes no attribute.</p>
 */
@Service("ingredientIdentityReportWriter")
public class IngredientIdentityReportWriter {

	/** Attribute read by the quali-quanti composition report for the function of the ingredient. */
	private static final String ATTR_FUNCTION = "ingListFunctionality";

	private static final List<QName> IDENTIFICATION_CODES = List.of(PLMModel.PROP_ING_CEECODE, PLMModel.PROP_CAS_NUMBER, PLMModel.PROP_CE_NUMBER,
			PLMModel.PROP_EC_NUMBER);

	private final NodeService nodeService;

	private final AttributeExtractorService attributeExtractorService;

	/**
	 * <p>Constructor for IngredientIdentityReportWriter.</p>
	 *
	 * @param nodeService the node service reading the ingredient
	 * @param attributeExtractorService the service naming the ingredient type as reports display it
	 */
	public IngredientIdentityReportWriter(@Qualifier("nodeService") NodeService nodeService, AttributeExtractorService attributeExtractorService) {
		this.nodeService = nodeService;
		this.attributeExtractorService = attributeExtractorService;
	}

	/**
	 * Writes the identification codes and the function of an ingredient on its ingredient line.
	 *
	 * @param ingNodeRef the ingredient
	 * @param ingListElt the ingredient line element of the report data source
	 */
	public void write(NodeRef ingNodeRef, Element ingListElt) {
		for (QName identificationCode : IDENTIFICATION_CODES) {
			writeAttribute(ingListElt, identificationCode.getLocalName(), nodeService.getProperty(ingNodeRef, identificationCode));
		}
		if (nodeService.getProperty(ingNodeRef, PLMModel.PROP_ING_TYPE_V2) instanceof NodeRef ingTypeNodeRef) {
			writeAttribute(ingListElt, ATTR_FUNCTION, attributeExtractorService.extractPropName(ingTypeNodeRef));
		}
	}

	/**
	 * Writes an attribute holding a value, and nothing for a missing or blank value.
	 *
	 * @param element the element
	 * @param name the attribute name
	 * @param value the value
	 */
	private void writeAttribute(Element element, String name, Serializable value) {
		if ((value != null) && !value.toString().isBlank()) {
			element.addAttribute(name, value.toString());
		}
	}
}
