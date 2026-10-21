package fr.becpg.repo.web.scripts.report;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;

/**
 * Unit tests of the export search templates {@link ReportTplsWebScript} offers for a type.
 */
public class ReportTplsWebScriptTest {

	private static final String BECPG_URI = "http://www.bcpg.fr/model/becpg/1.0";

	private static final QName TYPE_ENTITY = QName.createQName(BECPG_URI, "entityV2");

	private static final QName TYPE_PRODUCT = QName.createQName(BECPG_URI, "product");

	private static final QName TYPE_PACKAGING_KIT = QName.createQName(BECPG_URI, "packagingKit");

	private static final NodeRef PRODUCT_TPL = new NodeRef("workspace://SpacesStore/product-tpl");

	private static final NodeRef PACKAGING_KIT_TPL = new NodeRef("workspace://SpacesStore/packaging-kit-tpl");

	private final ReportTplService reportTplService = mock(ReportTplService.class);

	private final DictionaryService dictionaryService = mock(DictionaryService.class);

	private final ReportTplsWebScript webScript = new ReportTplsWebScript();

	@Before
	public void setUp() {
		webScript.setReportTplService(reportTplService);
		webScript.setDictionaryService(dictionaryService);
		declareType(TYPE_PACKAGING_KIT, TYPE_PRODUCT);
		declareType(TYPE_PRODUCT, TYPE_ENTITY);
		declareType(TYPE_ENTITY, null);
		declareTemplates(TYPE_PACKAGING_KIT);
		declareTemplates(TYPE_PRODUCT, PRODUCT_TPL);
		declareTemplates(TYPE_ENTITY);
	}

	@Test
	public void subTypeGetsTheTemplatesOfItsParentType() {
		Assert.assertEquals(List.of(PRODUCT_TPL), webScript.getExportSearchTemplates(TYPE_PACKAGING_KIT));
	}

	@Test
	public void templatesOfTheTypeComeBeforeTheInheritedOnes() {
		declareTemplates(TYPE_PACKAGING_KIT, PACKAGING_KIT_TPL, PRODUCT_TPL);

		Assert.assertEquals(List.of(PACKAGING_KIT_TPL, PRODUCT_TPL), webScript.getExportSearchTemplates(TYPE_PACKAGING_KIT));
	}

	@Test
	public void noTypeReturnsTheTemplatesBoundToNoType() {
		declareTemplates(null, PRODUCT_TPL);

		Assert.assertEquals(List.of(PRODUCT_TPL), webScript.getExportSearchTemplates(null));
	}

	@Test
	public void unknownTypeReturnsOnlyItsOwnTemplates() {
		QName unknownType = QName.createQName(BECPG_URI, "unknown");
		declareTemplates(unknownType, PACKAGING_KIT_TPL);

		Assert.assertEquals(List.of(PACKAGING_KIT_TPL), webScript.getExportSearchTemplates(unknownType));
	}

	private void declareType(QName type, QName parentType) {
		ClassDefinition classDefinition = mock(ClassDefinition.class);
		when(classDefinition.getParentName()).thenReturn(parentType);
		when(dictionaryService.getClass(type)).thenReturn(classDefinition);
	}

	private void declareTemplates(QName type, NodeRef... templates) {
		when(reportTplService.getUserReportTemplates(ReportType.ExportSearch, type, null)).thenReturn(List.of(templates));
	}
}
