package fr.becpg.repo.product.requirement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
import fr.becpg.repo.product.data.productList.ToxListDataItem;
import fr.becpg.repo.regulatory.RequirementListDataItem;
import fr.becpg.repo.regulatory.RequirementType;

/**
 * Unit tests of the toxicity requirements of a product specification.
 */
public class ToxRequirementScannerTest {

	private static final String SUPPORTED_LOCALES = "fr,en";

	private static final NodeRef TOX = new NodeRef("workspace://SpacesStore/tox");

	private final ToxRequirementScanner scanner = new ToxRequirementScanner();

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
	public void valueBelowMinimumIsForbiddenWithoutMaximumUsableQuantity() {
		List<RequirementListDataItem> requirements = scanner.checkRequirements(productWith(50d), List.of(specificationWith(requirement(60d, 80d))));

		assertEquals(1, requirements.size());
		assertEquals(RequirementType.Forbidden, requirements.get(0).getReqType());
		assertNull(requirements.get(0).getReqMaxQty());
	}

	@Test
	public void valueAboveMaximumGivesMaximumUsableQuantity() {
		List<RequirementListDataItem> requirements = scanner.checkRequirements(productWith(40d), List.of(specificationWith(requirement(null, 30d))));

		assertEquals(1, requirements.size());
		assertEquals(75d, requirements.get(0).getReqMaxQty(), 0.0001d);
	}

	@Test
	public void specificationValueIsNotAnExactTarget() {
		ToxListDataItem requirement = requirement(10d, 80d);
		requirement.setValue(30d);

		assertTrue(scanner.checkRequirements(productWith(50d), List.of(specificationWith(requirement))).isEmpty());
	}

	private ToxListDataItem requirement(Double mini, Double maxi) {
		ToxListDataItem requirement = new ToxListDataItem();
		requirement.setTox(TOX);
		requirement.setMini(mini);
		requirement.setMaxi(maxi);
		return requirement;
	}

	private FinishedProductData productWith(Double value) {
		ToxListDataItem toxItem = new ToxListDataItem();
		toxItem.setTox(TOX);
		toxItem.setValue(value);
		FinishedProductData product = new FinishedProductData();
		product.setToxList(listOf(toxItem));
		return product;
	}

	private ProductSpecificationData specificationWith(ToxListDataItem requirement) {
		ProductSpecificationData specification = new ProductSpecificationData();
		specification.setName("Specification");
		specification.setToxList(listOf(requirement));
		return specification;
	}

	private static <T> List<T> listOf(T item) {
		List<T> list = new ArrayList<>();
		list.add(item);
		return list;
	}

}
