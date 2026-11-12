package fr.becpg.repo.product.formulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.ResourceProductData;
import fr.becpg.repo.product.data.allergen.AllergenItem;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.requirement.AllergenRequirementScanner;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;

/**
 * Unit tests of the involuntary regulatory threshold when a process resource declares a
 * cross contamination (#38377).
 */
public class AllergensCalculatingFormulationHandlerTest {

	private static final NodeRef PRODUCT = new NodeRef("workspace://SpacesStore/product");
	private static final NodeRef RAW_MATERIAL = new NodeRef("workspace://SpacesStore/rawMaterial");
	private static final NodeRef RESOURCE = new NodeRef("workspace://SpacesStore/resource");
	private static final NodeRef ALLERGEN = new NodeRef("workspace://SpacesStore/allergen");

	private static final double INVOLUNTARY_THRESHOLD = 0.01d;
	private static final double VOLUNTARY_THRESHOLD = 1d;
	private static final double TRACE_UNDER_THRESHOLD = 0.005d;
	private static final double TRACE_OVER_THRESHOLD = 0.02d;
	private static final double DELTA = 0.000001d;

	private final AllergensCalculatingFormulationHandler handler = new AllergensCalculatingFormulationHandler();

	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	private AllergenItem allergen;

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() {
		alfrescoRepository = mock(AlfrescoRepository.class);
		AllergenRequirementScanner allergenRequirementScanner = mock(AllergenRequirementScanner.class);
		NodeService nodeService = mock(NodeService.class);

		when(alfrescoRepository.hasDataList(any(RepositoryEntity.class), eq(PLMModel.TYPE_ALLERGENLIST))).thenReturn(true);
		when(allergenRequirementScanner.extractRequirements(any())).thenReturn(Collections.emptyMap());

		handler.setAlfrescoRepository(alfrescoRepository);
		handler.setAllergenRequirementScanner(allergenRequirementScanner);
		handler.setNodeService(nodeService);
		handler.setMlNodeService(nodeService);

		allergen = new AllergenItem();
		allergen.setNodeRef(ALLERGEN);
		allergen.setCharactName("Allergen A");
		allergen.setAllergenInVoluntaryRegulatoryThreshold(INVOLUNTARY_THRESHOLD);
		when(alfrescoRepository.findOne(ALLERGEN)).thenReturn(allergen);
	}

	@Test
	public void rawMaterialTraceUnderThresholdIsNotDeclared() {
		givenRawMaterialWithTrace(TRACE_UNDER_THRESHOLD);
		ProductData product = productWith(compoList(), new ArrayList<>());

		handler.process(product);

		assertFalse(formulatedAllergen(product).getInVoluntary());
	}

	@Test
	public void resourceContaminationIsKeptUnderThreshold() {
		givenRawMaterialWithTrace(TRACE_UNDER_THRESHOLD);
		givenResourceWithContamination();
		ProductData product = productWith(compoList(), processList());

		handler.process(product);

		AllergenListDataItem formulated = formulatedAllergen(product);
		assertTrue(formulated.getInVoluntary());
		assertTrue(formulated.getInVoluntarySources().contains(RESOURCE));
		assertEquals(TRACE_UNDER_THRESHOLD, formulated.getQtyPerc(), DELTA);
	}

	@Test
	public void resourceContaminationOfCleanedLineFollowsThreshold() {
		givenRawMaterialWithTrace(TRACE_UNDER_THRESHOLD);
		givenResourceWithContamination();
		ProductData product = productWith(compoList(), processList());
		product.getAllergenList().add(AllergenListDataItem.build().withAllergen(ALLERGEN).withIsCleaned(true));

		handler.process(product);

		assertFalse(formulatedAllergen(product).getInVoluntary());
	}

	@Test
	public void rawMaterialTraceOverThresholdIsDeclaredWithResource() {
		givenRawMaterialWithTrace(TRACE_OVER_THRESHOLD);
		givenResourceWithContamination();
		ProductData product = productWith(compoList(), processList());

		handler.process(product);

		assertTrue(formulatedAllergen(product).getInVoluntary());
	}

	@Test
	public void resourceWithoutQuantityRaisesNoRequirement() {
		allergen.setAllergenRegulatoryThreshold(VOLUNTARY_THRESHOLD);
		givenResourceWithContamination();
		ProductData product = productWith(new ArrayList<>(), processList());

		handler.process(product);

		assertTrue(product.getReqCtrlList().isEmpty());
		assertTrue(formulatedAllergen(product).getInVoluntary());
	}

	private void givenRawMaterialWithTrace(double tracePerc) {
		RawMaterialData rawMaterial = new RawMaterialData();
		rawMaterial.setNodeRef(RAW_MATERIAL);
		rawMaterial.setState(SystemState.Valid);
		rawMaterial.setAllergenList(allergenList(tracePerc));
		when(alfrescoRepository.findOne(RAW_MATERIAL)).thenReturn(rawMaterial);
	}

	private void givenResourceWithContamination() {
		ResourceProductData resource = new ResourceProductData();
		resource.setNodeRef(RESOURCE);
		resource.setState(SystemState.Valid);
		resource.setAllergenList(allergenList(null));
		when(alfrescoRepository.findOne(RESOURCE)).thenReturn(resource);
	}

	private static List<AllergenListDataItem> allergenList(Double qtyPerc) {
		List<AllergenListDataItem> allergenList = new ArrayList<>();
		allergenList.add(AllergenListDataItem.build().withAllergen(ALLERGEN).withVoluntary(false).withInVoluntary(true).withQtyPerc(qtyPerc));
		return allergenList;
	}

	private static List<CompoListDataItem> compoList() {
		List<CompoListDataItem> compoList = new ArrayList<>();
		compoList.add(CompoListDataItem.build().withProduct(RAW_MATERIAL).withQty(1d).withQtyUsed(1d).withUnit(ProductUnit.kg));
		return compoList;
	}

	private static List<ProcessListDataItem> processList() {
		List<ProcessListDataItem> processList = new ArrayList<>();
		processList.add(ProcessListDataItem.build().withResource(RESOURCE));
		return processList;
	}

	private static ProductData productWith(List<CompoListDataItem> compoList, List<ProcessListDataItem> processList) {
		FinishedProductData product = new FinishedProductData().withQty(1d).withUnit(ProductUnit.kg).withCompoList(compoList)
				.withProcessList(processList);
		product.setNodeRef(PRODUCT);
		product.setAllergenList(new ArrayList<>());
		product.setReqCtrlList(new ArrayList<>());
		return product;
	}

	private static AllergenListDataItem formulatedAllergen(ProductData product) {
		assertEquals(1, product.getAllergenList().size());
		return product.getAllergenList().get(0);
	}
}
