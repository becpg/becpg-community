package fr.becpg.repo.product.formulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.model.GS1Model;
import fr.becpg.model.PackModel;
import fr.becpg.repo.product.data.LogisticUnitData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.packaging.VariantPackagingData;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;

/**
 * Unit tests of the packaging measures and pallet information written by the tare formulation (#37189).
 */
public class TareFormulationHandlerTest {

	private static final NodeRef PACKAGING_KIT = new NodeRef("workspace://SpacesStore/packaging-kit");

	private static final int PALLET_LAYERS = 13;

	private static final int BOXES_PER_PALLET = 52;

	private static final double SECONDARY_WIDTH = 400d;

	private TareFormulationHandler handler;

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() {
		AlfrescoRepository<ProductData> alfrescoRepository = mock(AlfrescoRepository.class);
		new PackagingHelper(mock(NodeService.class), alfrescoRepository).afterPropertiesSet();
		handler = new TareFormulationHandler(alfrescoRepository);
	}

	@Test
	public void logisticUnitGetsThePalletInformationOfItsPackagingKit() {
		LogisticUnitData logisticUnit = packagedWithPalletKit(LogisticUnitData.build());

		handler.process(logisticUnit);

		assertEquals(PALLET_LAYERS, logisticUnit.getExtraProperties().get(PackModel.PROP_PALLET_LAYERS));
		assertEquals(BOXES_PER_PALLET, logisticUnit.getExtraProperties().get(PackModel.PROP_PALLET_BOXES_PER_PALLET));
	}

	@Test
	public void logisticUnitGetsTheSecondaryMeasuresOfItsPackagingKit() {
		LogisticUnitData logisticUnit = packagedWithPalletKit(LogisticUnitData.build());

		handler.process(logisticUnit);

		assertEquals(SECONDARY_WIDTH, logisticUnit.getExtraProperties().get(GS1Model.PROP_SECONDARY_WIDTH));
	}

	@Test
	public void semiFinishedProductWithoutPalletAspectGetsNoPalletInformation() {
		SemiFinishedProductData semiFinishedProduct = packagedWithPalletKit(new SemiFinishedProductData());

		handler.process(semiFinishedProduct);

		assertFalse(semiFinishedProduct.getExtraProperties().containsKey(PackModel.PROP_PALLET_LAYERS));
	}

	private static <T extends ProductData> T packagedWithPalletKit(T product) {
		product.setName("Case of 24");
		product.setNetWeight(3d);
		product.getCompoListView().setCompoList(new ArrayList<>());
		product.getPackagingListView().setPackagingList(List.of(PackagingListDataItem.build().withPkgLevel(PackagingLevel.Secondary)
				.withUnit(ProductUnit.PP).withQty(1d).withProduct(PACKAGING_KIT)));
		product.setDefaultVariantPackagingData(palletKitPackaging());
		return product;
	}

	private static VariantPackagingData palletKitPackaging() {
		VariantPackagingData packagingData = new VariantPackagingData();
		packagingData.setProductPerBoxes(1);
		packagingData.setBoxesPerPallet(BOXES_PER_PALLET);
		packagingData.setPalletLayers(PALLET_LAYERS);
		packagingData.setManualPalletInformations(false);
		packagingData.setManualSecondary(false);
		packagingData.setSecondaryWidth(SECONDARY_WIDTH);
		return packagingData;
	}
}
