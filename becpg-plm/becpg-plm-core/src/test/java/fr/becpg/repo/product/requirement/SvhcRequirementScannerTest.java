package fr.becpg.repo.product.requirement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.productList.SvhcListDataItem;
import fr.becpg.repo.regulatory.RequirementListDataItem;
import fr.becpg.repo.regulatory.RequirementType;

/**
 * Unit tests of the substances of very high concern requirements of a product specification.
 */
public class SvhcRequirementScannerTest {

	private static final String SUPPORTED_LOCALES = "fr,en";

	private static final NodeRef SUBSTANCE = new NodeRef("workspace://SpacesStore/substance");

	private final SvhcRequirementScanner scanner = new SvhcRequirementScanner();

	@Before
	public void setUp() {
		MLTextHelper.flushCache();
		MLTextHelper.setSupportedLocales(SUPPORTED_LOCALES);
		scanner.setMlNodeService(mock(NodeService.class));
	}

	@After
	public void tearDown() {
		MLTextHelper.flushCache();
	}

	@Test
	public void minimumIsIgnored() {
		SvhcListDataItem requirement = SvhcListDataItem.build().withIngredient(SUBSTANCE);
		requirement.setMini(60d);
		requirement.setMaxi(80d);

		assertTrue(scanner.checkRequirements(productWith(50d), List.of(specificationWith(requirement))).isEmpty());
	}

	@Test
	public void valueAboveMaximumIsForbidden() {
		SvhcListDataItem requirement = SvhcListDataItem.build().withIngredient(SUBSTANCE);
		requirement.setMaxi(30d);

		List<RequirementListDataItem> requirements = scanner.checkRequirements(productWith(40d), List.of(specificationWith(requirement)));

		assertEquals(1, requirements.size());
		assertEquals(RequirementType.Forbidden, requirements.get(0).getReqType());
		assertEquals(75d, requirements.get(0).getReqMaxQty(), 0.0001d);
	}

	@Test
	public void legacyQuantityIsAMaximumNotAnExactValue() {
		SvhcListDataItem requirement = SvhcListDataItem.build().withIngredient(SUBSTANCE).withQtyPerc(30d);

		assertTrue(scanner.checkRequirements(productWith(20d), List.of(specificationWith(requirement))).isEmpty());
	}

	private FinishedProductData productWith(Double qtyPerc) {
		FinishedProductData product = new FinishedProductData();
		product.setSvhcList(listOf(SvhcListDataItem.build().withIngredient(SUBSTANCE).withQtyPerc(qtyPerc)));
		return product;
	}

	private ProductSpecificationData specificationWith(SvhcListDataItem requirement) {
		ProductSpecificationData specification = new ProductSpecificationData();
		specification.setName("Specification");
		specification.setSvhcList(listOf(requirement));
		return specification;
	}

	private static <T> List<T> listOf(T item) {
		List<T> list = new ArrayList<>();
		list.add(item);
		return list;
	}

}
