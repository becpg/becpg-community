/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.product.report;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.AttributeExtractorService;

/**
 * Unit tests of {@link IngredientIdentityReportWriter}: the identification codes and the function written on an ingredient
 * line of the report data source.
 */
public class IngredientIdentityReportWriterTest {

	private static final NodeRef ING = new NodeRef("workspace://SpacesStore/ing");
	private static final NodeRef ING_TYPE = new NodeRef("workspace://SpacesStore/ing-type");
	private static final String CEE = "E422";
	private static final String CAS = "56-81-5";
	private static final String EINECS = "200-289-5";
	private static final String EC = "200-289-5,616-940-5";
	private static final String HUMECTANT = "skin conditioning - humectant";
	private static final String ATTR_FUNCTION = "ingListFunctionality";

	private NodeService nodeService;

	private AttributeExtractorService attributeExtractorService;

	private IngredientIdentityReportWriter writer;

	private Element ingListElt;

	@Before
	public void setUp() {
		nodeService = mock(NodeService.class);
		attributeExtractorService = mock(AttributeExtractorService.class);
		writer = new IngredientIdentityReportWriter(nodeService, attributeExtractorService);
		ingListElt = DocumentHelper.createElement(PLMModel.TYPE_INGLIST.getLocalName());
	}

	@Test
	public void testIdentificationCodesAreWritten() {
		when(nodeService.getProperty(ING, PLMModel.PROP_ING_CEECODE)).thenReturn(CEE);
		when(nodeService.getProperty(ING, PLMModel.PROP_CAS_NUMBER)).thenReturn(CAS);
		when(nodeService.getProperty(ING, PLMModel.PROP_CE_NUMBER)).thenReturn(EINECS);
		when(nodeService.getProperty(ING, PLMModel.PROP_EC_NUMBER)).thenReturn(EC);

		writer.write(ING, ingListElt);

		assertEquals(CEE, ingListElt.attributeValue(PLMModel.PROP_ING_CEECODE.getLocalName()));
		assertEquals(CAS, ingListElt.attributeValue(PLMModel.PROP_CAS_NUMBER.getLocalName()));
		assertEquals(EINECS, ingListElt.attributeValue(PLMModel.PROP_CE_NUMBER.getLocalName()));
		assertEquals(EC, ingListElt.attributeValue(PLMModel.PROP_EC_NUMBER.getLocalName()));
	}

	@Test
	public void testMissingOrBlankCodesWriteNoAttribute() {
		when(nodeService.getProperty(ING, PLMModel.PROP_ING_CEECODE)).thenReturn("");
		when(nodeService.getProperty(ING, PLMModel.PROP_CAS_NUMBER)).thenReturn(" ");

		writer.write(ING, ingListElt);

		assertEquals(0, ingListElt.attributeCount());
	}

	@Test
	public void testFunctionIsTheDisplayNameOfTheIngredientType() {
		when(nodeService.getProperty(ING, PLMModel.PROP_ING_TYPE_V2)).thenReturn(ING_TYPE);
		when(attributeExtractorService.extractPropName(ING_TYPE)).thenReturn(HUMECTANT);

		writer.write(ING, ingListElt);

		assertEquals(HUMECTANT, ingListElt.attributeValue(ATTR_FUNCTION));
	}

	@Test
	public void testNoIngredientTypeWritesNoFunction() {
		writer.write(ING, ingListElt);

		assertNull(ingListElt.attributeValue(ATTR_FUNCTION));
	}
}
