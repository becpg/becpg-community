package fr.becpg.repo.product.formulation.ecobalyse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.LCAListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * Unit tests of the non-LCA complements of the Ecobalyse environmental cost.
 */
public class EcobalyseComplementsCalculatorTest {

	private static final double PRECISION = 0.0001d;

	private static final String ECOBALYSE_CODE = "barley-whole-fr";

	private static final NodeRef COMPLEMENTS_LCA = new NodeRef("workspace://SpacesStore/complements");

	private static final NodeRef COMPONENT = new NodeRef("workspace://SpacesStore/component");

	private final NodeService nodeService = mock(NodeService.class);

	private final EcobalyseIngredientService ecobalyseIngredientService = mock(EcobalyseIngredientService.class);

	@SuppressWarnings("unchecked")
	private final AlfrescoRepository<RepositoryEntity> alfrescoRepository = mock(AlfrescoRepository.class);

	private final EcobalyseComplementsCalculator calculator = new EcobalyseComplementsCalculator();

	private final RawMaterialData component = new RawMaterialData();

	private MockedStatic<BeCPGQueryBuilder> queryBuilderStatic;

	@Before
	public void setUp() {
		calculator.setNodeService(nodeService);
		calculator.setEcobalyseIngredientService(ecobalyseIngredientService);
		calculator.setAlfrescoRepository(alfrescoRepository);

		BeCPGQueryBuilder queryBuilder = mock(BeCPGQueryBuilder.class, RETURNS_SELF);
		when(queryBuilder.list()).thenReturn(List.of(COMPLEMENTS_LCA));
		queryBuilderStatic = mockStatic(BeCPGQueryBuilder.class);
		queryBuilderStatic.when(BeCPGQueryBuilder::createQuery).thenReturn(queryBuilder);

		component.setLcaList(new ArrayList<>());
		when(alfrescoRepository.findOne(COMPONENT)).thenReturn(component);
		when(ecobalyseIngredientService.findByCode(any())).thenReturn(Optional.empty());
	}

	@After
	public void tearDown() {
		queryBuilderStatic.close();
	}

	@Test
	public void practicesOfTheLcaListAreEnoughToComputeTheComplements() {
		givenPractice("AP_HEDGES", 2.5d);
		givenPractice("AP_PLOT_SIZE", 4d);

		FinishedProductData product = formulate(0.5d);

		assertEquals(-(6.5d * 0.5d), complements(product), PRECISION);
	}

	@Test
	public void ecobalyseCodeTakesTheServicesOfTheReference() {
		when(nodeService.getProperty(COMPONENT, PLMModel.PROP_ECOBALYSE_CODE)).thenReturn(ECOBALYSE_CODE);
		when(ecobalyseIngredientService.findByCode(ECOBALYSE_CODE)).thenReturn(Optional.of(
				EcobalyseIngredient.builder().withIdentity("id", ECOBALYSE_CODE, "Orge").withEcosystemicServices(0d, 2.5d, null, 4.09d).build()));

		FinishedProductData product = formulate(1d);

		assertEquals(-6.59d, complements(product), PRECISION);
	}

	@Test
	public void complementsOfASemiFinishedProductPrevailOverItsPractices() {
		givenPractice("AP_HEDGES", 1d);
		givenPractice("BIODIVERSITY_COMPLEMENTS", -5d);

		FinishedProductData product = formulate(0.5d);

		assertEquals(-2.5d, complements(product), PRECISION);
	}

	@Test
	public void componentWithoutCodeNorPracticeBringsNoComplement() {
		FinishedProductData product = formulate(1d);

		assertTrue(product.getLcaList().isEmpty());
	}

	@Test
	public void otherIndicatorsAreNotTakenForPractices() {
		givenPractice("CLIMATE_CHANGE", 2.9d);

		FinishedProductData product = formulate(1d);

		assertTrue(product.getLcaList().isEmpty());
	}

	private FinishedProductData formulate(double quantity) {
		FinishedProductData product = new FinishedProductData();
		product.setNetWeight(1d);
		product.setLcaList(new ArrayList<>());
		product.withCompoList(new ArrayList<>(List.of(CompoListDataItem.build().withProduct(COMPONENT).withQty(quantity))));

		calculator.addComplements(product);

		return product;
	}

	private void givenPractice(String code, double value) {
		NodeRef lca = new NodeRef("workspace://SpacesStore/" + code);
		when(nodeService.getProperty(lca, PLMModel.PROP_LCA_CODE)).thenReturn(code);

		LCAListDataItem line = new LCAListDataItem();
		line.setLca(lca);
		line.setValue(value);
		component.getLcaList().add(line);
	}

	private double complements(FinishedProductData product) {
		for (LCAListDataItem line : product.getLcaList()) {
			if (COMPLEMENTS_LCA.equals(line.getLca())) {
				return line.getValue();
			}
		}
		throw new AssertionError("No complements line in the LCA list");
	}

}
