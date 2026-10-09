package fr.becpg.repo.product.formulation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LogisticUnitData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;

/**
 * Unit tests of the products the hazardous substances formulation applies to (#37189).
 */
public class SvhcCalculatingFormulationHandlerTest {

	private static final NodeRef PRODUCT = new NodeRef("workspace://SpacesStore/product");

	private final SvhcCalculatingFormulationHandler handler = new SvhcCalculatingFormulationHandler();

	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() {
		alfrescoRepository = mock(AlfrescoRepository.class);
		handler.setAlfrescoRepository(alfrescoRepository);
	}

	@Test
	public void logisticUnitWithoutSvhcListIsNotFormulated() {
		assertFalse(handler.accept(saved(LogisticUnitData.build())));
	}

	@Test
	public void logisticUnitWithSvhcListIsFormulated() {
		givenSvhcListExists();

		assertTrue(handler.accept(saved(LogisticUnitData.build())));
	}

	@Test
	public void finishedProductWithoutSvhcListKeepsBeingFormulated() {
		assertTrue(handler.accept(saved(new FinishedProductData())));
	}

	private void givenSvhcListExists() {
		when(alfrescoRepository.hasDataList(any(RepositoryEntity.class), eq(PLMModel.TYPE_SVHCLIST))).thenReturn(true);
	}

	private static ProductData saved(ProductData product) {
		product.setNodeRef(PRODUCT);
		product.setSvhcList(new ArrayList<>());
		return product;
	}
}
