package fr.becpg.test.repo.product.formulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.PackagingMaterialData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.TareUnit;
import fr.becpg.repo.product.data.productList.PackMaterialListDataItem;
import fr.becpg.repo.product.data.productList.PackagingComponentListDataItem;
import fr.becpg.repo.product.data.productList.SupplierPackagingListDataItem;
import fr.becpg.test.PLMBaseTestCase;

/**
 * Checks that the components of a packaging drive its tare and its materials, and that the
 * packaging its supplier delivers it in does not.
 * <p>
 * Component names are left unset on purpose: they are a constrained value list, empty until
 * the reference data is imported, and the formulation never reads them.
 *
 * @author matthieu
 */
public class PackagingComponentFormulationIT extends PLMBaseTestCase {

	private static final double DELTA = 0.01d;

	@Autowired
	private ProductService productService;

	private NodeRef glassNodeRef;

	private NodeRef paperNodeRef;

	/** {@inheritDoc} */
	@Override
	public void setUp() throws Exception {
		super.setUp();
		inWriteTx(() -> {
			glassNodeRef = createMaterial("Glass");
			paperNodeRef = createMaterial("Paper");
			return null;
		});
	}

	@Test
	public void testComponentsDriveTareAndMaterials() {

		NodeRef bottleNodeRef = inWriteTx(() -> {
			PackagingMaterialData bottle = PackagingMaterialData.build().withName("Bottle 750ml");
			bottle.setPackagingComponentList(new ArrayList<>(List.of(
					PackagingComponentListDataItem.build().withTare(290d, TareUnit.g).withMaterial(glassNodeRef),
					PackagingComponentListDataItem.build().withTare(0.75d, TareUnit.g).withMaterial(paperNodeRef)
							.withRecycledPerc(50d),
					PackagingComponentListDataItem.build().withTare(0.6d, TareUnit.g).withMaterial(paperNodeRef))));
			return alfrescoRepository.create(getTestFolderNodeRef(), bottle).getNodeRef();
		});

		inWriteTx(() -> {
			productService.formulate(bottleNodeRef);
			return null;
		});

		inReadTx(() -> {
			PackagingMaterialData bottle = (PackagingMaterialData) alfrescoRepository.findOne(bottleNodeRef);

			Assert.assertEquals(TareUnit.g, bottle.getTareUnit());
			Assert.assertEquals(291.35d, bottle.getTare(), DELTA);

			Map<NodeRef, PackMaterialListDataItem> materials = byMaterial(bottle.getPackMaterialList());
			Assert.assertEquals(2, materials.size());

			PackMaterialListDataItem glass = materials.get(glassNodeRef);
			Assert.assertEquals(290d, glass.getPmlWeight(), DELTA);
			Assert.assertEquals(99.53d, glass.getPmlPerc(), DELTA);
			Assert.assertEquals(0d, glass.getPmlRecycledPercentage(), DELTA);

			// Both labels are merged: 1.35 g of paper, half of the 0.75 g one being recycled
			PackMaterialListDataItem paper = materials.get(paperNodeRef);
			Assert.assertEquals(1.35d, paper.getPmlWeight(), DELTA);
			Assert.assertEquals(0.46d, paper.getPmlPerc(), DELTA);
			Assert.assertEquals(27.77d, paper.getPmlRecycledPercentage(), DELTA);

			return null;
		});
	}

	@Test
	public void testOmittedComponentIsExcluded() {

		NodeRef crateNodeRef = inWriteTx(() -> {
			PackagingMaterialData crate = PackagingMaterialData.build().withName("Returnable crate");
			PackagingComponentListDataItem returnable = PackagingComponentListDataItem.build()
					.withTare(500d, TareUnit.g).withMaterial(glassNodeRef);
			returnable.setDeclType(DeclarationType.Omit);
			crate.setPackagingComponentList(new ArrayList<>(List.of(
					PackagingComponentListDataItem.build().withTare(2d, TareUnit.g).withMaterial(paperNodeRef),
					returnable)));
			return alfrescoRepository.create(getTestFolderNodeRef(), crate).getNodeRef();
		});

		inWriteTx(() -> {
			productService.formulate(crateNodeRef);
			return null;
		});

		inReadTx(() -> {
			PackagingMaterialData crate = (PackagingMaterialData) alfrescoRepository.findOne(crateNodeRef);

			Assert.assertEquals(2d, crate.getTare(), DELTA);

			Map<NodeRef, PackMaterialListDataItem> materials = byMaterial(crate.getPackMaterialList());
			Assert.assertEquals(1, materials.size());
			Assert.assertNotNull(materials.get(paperNodeRef));
			Assert.assertNull(materials.get(glassNodeRef));

			return null;
		});
	}

	@Test
	public void testChildComponentCarriesItsOwnWeight() {

		NodeRef bottleNodeRef = inWriteTx(() -> {
			PackagingMaterialData bottle = PackagingMaterialData.build().withName("Bottle with cap");
			PackagingComponentListDataItem body = PackagingComponentListDataItem.build()
					.withTare(100d, TareUnit.g).withMaterial(glassNodeRef);
			PackagingComponentListDataItem cap = PackagingComponentListDataItem.build().withTare(5d, TareUnit.g)
					.withMaterial(paperNodeRef).withParent(body);
			bottle.setPackagingComponentList(new ArrayList<>(List.of(body, cap)));
			return alfrescoRepository.create(getTestFolderNodeRef(), bottle).getNodeRef();
		});

		inWriteTx(() -> {
			productService.formulate(bottleNodeRef);
			return null;
		});

		inReadTx(() -> {
			PackagingMaterialData bottle = (PackagingMaterialData) alfrescoRepository.findOne(bottleNodeRef);
			// The hierarchy describes the assembly: a parent does not total its children
			Assert.assertEquals(105d, bottle.getTare(), DELTA);
			return null;
		});
	}

	@Test
	public void testSupplierPackagingLeavesTareUntouched() {

		NodeRef flourNodeRef = inWriteTx(() -> {
			RawMaterialData flour = RawMaterialData.build().withName("Flour");
			flour.setSupplierPackagingList(new ArrayList<>(List.of(
					SupplierPackagingListDataItem.build().withQty(1d).withTare(80d, TareUnit.g).withMaterial(paperNodeRef),
					SupplierPackagingListDataItem.build().withQty(1d).withTare(25000d, TareUnit.g)
							.withMaterial(glassNodeRef))));
			return alfrescoRepository.create(getTestFolderNodeRef(), flour).getNodeRef();
		});

		inWriteTx(() -> {
			productService.formulate(flourNodeRef);
			return null;
		});

		inReadTx(() -> {
			RawMaterialData flour = (RawMaterialData) alfrescoRepository.findOne(flourNodeRef);

			Assert.assertNull("Upstream packaging must not set the tare of the raw material", flour.getTare());
			Assert.assertTrue("Upstream packaging must not feed the packaging materials",
					(flour.getPackMaterialList() == null) || flour.getPackMaterialList().isEmpty());

			return null;
		});
	}

	private NodeRef createMaterial(String name) {
		return nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS,
				PackModel.TYPE_PACKAGING_MATERIAL, Map.of(BeCPGModel.PROP_LV_VALUE, name)).getChildRef();
	}

	private Map<NodeRef, PackMaterialListDataItem> byMaterial(List<PackMaterialListDataItem> packMaterialList) {
		Map<NodeRef, PackMaterialListDataItem> materials = new HashMap<>();
		if (packMaterialList != null) {
			for (PackMaterialListDataItem material : packMaterialList) {
				materials.put(material.getPmlMaterial(), material);
			}
		}
		return materials;
	}

}
