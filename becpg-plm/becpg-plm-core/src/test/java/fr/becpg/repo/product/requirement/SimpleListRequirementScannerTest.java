package fr.becpg.repo.product.requirement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.regulatory.RequirementListDataItem;
import fr.becpg.repo.regulatory.RequirementType;

/**
 * Unit tests of the range checks shared by the simple list requirement scanners, run through the
 * physico-chemical scanner.
 */
public class SimpleListRequirementScannerTest {

	private static final String SUPPORTED_LOCALES = "fr,en";

	private static final NodeRef PHYSICO_CHEM = new NodeRef("workspace://SpacesStore/physico-chem");

	private final PhysicoRequirementScanner scanner = new PhysicoRequirementScanner();

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
	public void valueAboveMaximumGivesMaximumUsableQuantity() {
		RequirementListDataItem requirement = checkSingleRequirement(100d, 60d, 80d);

		assertEquals(RequirementType.Forbidden, requirement.getReqType());
		assertEquals(80d, requirement.getReqMaxQty(), 0.0001d);
	}

	@Test
	public void valueBelowMinimumGivesNoMaximumUsableQuantity() {
		RequirementListDataItem requirement = checkSingleRequirement(50d, 60d, 80d);

		assertEquals(RequirementType.Forbidden, requirement.getReqType());
		assertNull(requirement.getReqMaxQty());
	}

	private RequirementListDataItem checkSingleRequirement(Double value, Double mini, Double maxi) {
		List<RequirementListDataItem> requirements = scanner.checkRequirements(productWith(value), List.of(specificationWith(mini, maxi)));
		assertEquals(1, requirements.size());
		return requirements.get(0);
	}

	private FinishedProductData productWith(Double value) {
		FinishedProductData product = new FinishedProductData();
		product.setPhysicoChemList(listOf(new PhysicoChemListDataItem(null, value, null, null, null, PHYSICO_CHEM)));
		return product;
	}

	private ProductSpecificationData specificationWith(Double mini, Double maxi) {
		ProductSpecificationData specification = new ProductSpecificationData();
		specification.setName("Specification");
		specification.setPhysicoChemList(listOf(new PhysicoChemListDataItem(null, null, null, mini, maxi, PHYSICO_CHEM)));
		return specification;
	}

	private static <T> List<T> listOf(T item) {
		List<T> list = new ArrayList<>();
		list.add(item);
		return list;
	}

}
