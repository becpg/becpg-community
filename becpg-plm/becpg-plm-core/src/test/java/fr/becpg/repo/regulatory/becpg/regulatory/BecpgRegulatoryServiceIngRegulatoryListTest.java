package fr.becpg.repo.regulatory.becpg.regulatory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.IngRegulatoryListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;

/**
 * Unit tests of the ingredient compliance list filled by a becpg-regulatory check: only a product that
 * has the list gets it filled, so a product showing the real-time regulatory view does not get it back.
 */
public class BecpgRegulatoryServiceIngRegulatoryListTest {

	private static final NodeRef PRODUCT = new NodeRef("workspace://SpacesStore/product");
	private static final NodeRef ING = new NodeRef("workspace://SpacesStore/ing");
	private static final NodeRef COUNTRY = new NodeRef("workspace://SpacesStore/country");

	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	private BecpgRegulatoryService service;
	private ProductData product;

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() {
		alfrescoRepository = mock(AlfrescoRepository.class);
		service = new BecpgRegulatoryService(mock(NodeService.class), alfrescoRepository, null, null, null, null, null, null, null, null, null);
		product = new FinishedProductData();
		product.setNodeRef(PRODUCT);
		product.setIngRegulatoryList(new ArrayList<>());
	}

	private static List<IngRegulatoryListDataItem> checkResult() {
		IngRegulatoryListDataItem item = new IngRegulatoryListDataItem();
		item.setIng(ING);
		item.setRegulatoryCountries(List.of(COUNTRY));
		return List.of(item);
	}

	@Test
	public void productWithoutTheListDoesNotGetIt() {
		when(alfrescoRepository.hasDataList(product, PLMModel.TYPE_ING_REGULATORY_LIST)).thenReturn(false);

		assertFalse(service.fillIngRegulatoryList(product, checkResult()));
		assertTrue(product.getIngRegulatoryList().isEmpty());
	}

	@Test
	public void productWithTheListGetsItFilled() {
		when(alfrescoRepository.hasDataList(product, PLMModel.TYPE_ING_REGULATORY_LIST)).thenReturn(true);

		assertTrue(service.fillIngRegulatoryList(product, checkResult()));
		assertEquals(1, product.getIngRegulatoryList().size());
		assertEquals(ING, product.getIngRegulatoryList().get(0).getIng());
	}

	@Test
	public void productWithoutLoadedListIsLeftAlone() {
		product.setIngRegulatoryList(null);

		assertFalse(service.fillIngRegulatoryList(product, checkResult()));
	}
}
