/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.test.repo.product.formulation;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.PackagingMaterialData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.TareUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.SvhcListDataItem;
import fr.becpg.test.PLMBaseTestCase;

/**
 * Reproduces #32363: hazardous substances (SVHC) defined on a "Boite neutre"
 * (neutral packaging material) must still propagate to the finished product
 * when the neutral box is nested inside a "Boite imprimee" (printed packaging
 * material used in the finished product packaging list), exactly as they do
 * when the neutral box is used directly.
 */
public class SvhcNestedPackagingIT extends PLMBaseTestCase {

	private static final Log logger = LogFactory.getLog(SvhcNestedPackagingIT.class);

	@Autowired
	protected ProductService productService;

	private NodeRef createNode(ProductData productData) {
		return alfrescoRepository.create(getTestFolderNodeRef(), productData).getNodeRef();
	}

	/** Control: neutral box used directly -> SVHC must propagate (already works). */
	@Test
	public void testDirectNeutralBox() {
		final NodeRef fpNodeRef = inWriteTx(() -> {
			NodeRef neutralBox = createNeutralBox();
			FinishedProductData fp = FinishedProductData.build().withName("Quiche direct neutral box").withUnit(ProductUnit.kg)
					.withCompoList(List.of(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withProduct(createRawMaterial())))
					.withPackagingList(List.of(PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.P).withIsMaster(true)
							.withPkgLevel(PackagingLevel.Primary).withProduct(neutralBox)));
			return alfrescoRepository.create(getTestFolderNodeRef(), fp).getNodeRef();
		});

		inWriteTx(() -> {
			productService.formulate(fpNodeRef);
			return null;
		});

		inReadTx(() -> {
			ProductData fp = (ProductData) alfrescoRepository.findOne(fpNodeRef);
			dumpSvhc("direct neutral box", fp);
			assertTrue("SVHC should propagate from the directly used neutral box", hasSvhc(fp, ings.get(0)));
			return null;
		});
	}

	/** Bug: neutral box nested inside a printed box -> SVHC must still propagate. */
	@Test
	public void testNestedPrintedBox() {
		final NodeRef fpNodeRef = inWriteTx(() -> {
			NodeRef neutralBox = createNeutralBox();

			// Printed box: a packaging material whose own packaging list contains the neutral box.
			PackagingMaterialData printedBox = PackagingMaterialData.build().withName("Boite imprimee").withTare(1d, TareUnit.kg);
			printedBox.getPackagingListView().setPackagingList(List.of(PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.P)
					.withIsMaster(true).withPkgLevel(PackagingLevel.Primary).withProduct(neutralBox)));
			NodeRef printedBoxNodeRef = createNode(printedBox);

			FinishedProductData fp = FinishedProductData.build().withName("Quiche printed box").withUnit(ProductUnit.kg)
					.withCompoList(List.of(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withProduct(createRawMaterial())))
					.withPackagingList(List.of(PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.P).withIsMaster(true)
							.withPkgLevel(PackagingLevel.Primary).withProduct(printedBoxNodeRef)));
			return alfrescoRepository.create(getTestFolderNodeRef(), fp).getNodeRef();
		});

		inWriteTx(() -> {
			productService.formulate(fpNodeRef);
			return null;
		});

		inReadTx(() -> {
			ProductData fp = (ProductData) alfrescoRepository.findOne(fpNodeRef);
			dumpSvhc("nested printed box", fp);
			assertTrue("SVHC must propagate from the neutral box nested inside the printed box (#32363)",
					hasSvhc(fp, ings.get(0)));
			return null;
		});
	}

	/**
	 * Closest to the customer data of #32363: the neutral box is nested in the printed box without an
	 * explicit packaging level, which is what users usually leave in the sub packaging list. Checks both
	 * the printed box own aggregation and the propagation up to the finished product.
	 */
	@Test
	public void testNestedPrintedBoxWithoutExplicitLevel() {
		final NodeRef[] refs = inWriteTx(() -> {
			NodeRef neutralBox = createNeutralBox();

			PackagingMaterialData printedBox = PackagingMaterialData.build().withName("Boite imprimee sans niveau").withTare(1d, TareUnit.kg);
			printedBox.getPackagingListView().setPackagingList(List.of(
					PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.P).withIsMaster(true).withProduct(neutralBox)));
			NodeRef printedBoxNodeRef = createNode(printedBox);

			FinishedProductData fp = FinishedProductData.build().withName("Quiche printed box no level").withUnit(ProductUnit.kg)
					.withCompoList(List.of(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withProduct(createRawMaterial())))
					.withPackagingList(List.of(PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.P).withIsMaster(true)
							.withPkgLevel(PackagingLevel.Primary).withProduct(printedBoxNodeRef)));
			return new NodeRef[] { alfrescoRepository.create(getTestFolderNodeRef(), fp).getNodeRef(), printedBoxNodeRef };
		});

		inWriteTx(() -> {
			productService.formulate(refs[1]);
			productService.formulate(refs[0]);
			return null;
		});

		inReadTx(() -> {
			ProductData printedBox = (ProductData) alfrescoRepository.findOne(refs[1]);
			dumpSvhc("printed box own list (no level)", printedBox);
			ProductData fp = (ProductData) alfrescoRepository.findOne(refs[0]);
			dumpSvhc("finished product (no level)", fp);
			assertTrue("SVHC must propagate to the finished product through a nested packaging without explicit level (#32363)",
					hasSvhc(fp, ings.get(0)));
			return null;
		});
	}

	/**
	 * Retest of #32363 on the customer data: no packaging level was filled in, neither on the printed box
	 * used by the finished product nor on the neutral box nested in it. An unset level must be read as
	 * primary so that the substances still reach the finished product, and the printed box own list must
	 * carry them exactly once.
	 */
	@Test
	public void testNestedPrintedBoxWithoutAnyLevel() {
		final NodeRef[] refs = inWriteTx(() -> {
			NodeRef neutralBox = createNeutralBox();

			PackagingMaterialData printedBox = PackagingMaterialData.build().withName("Boite imprimee sans aucun niveau").withTare(1d, TareUnit.kg);
			printedBox.getPackagingListView().setPackagingList(
					List.of(PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.P).withIsMaster(true).withProduct(neutralBox)));
			NodeRef printedBoxNodeRef = createNode(printedBox);

			FinishedProductData fp = FinishedProductData.build().withName("Quiche printed box no level at all").withUnit(ProductUnit.kg)
					.withCompoList(List.of(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withProduct(createRawMaterial())))
					.withPackagingList(List.of(PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.P).withIsMaster(true)
							.withProduct(printedBoxNodeRef)));
			return new NodeRef[] { alfrescoRepository.create(getTestFolderNodeRef(), fp).getNodeRef(), printedBoxNodeRef };
		});

		inWriteTx(() -> {
			productService.formulate(refs[1]);
			productService.formulate(refs[0]);
			return null;
		});

		inReadTx(() -> {
			ProductData printedBox = (ProductData) alfrescoRepository.findOne(refs[1]);
			dumpSvhc("printed box own list (no level at all)", printedBox);
			assertTrue("The printed box own list must carry the substances of the nested neutral box (#32363)",
					hasSvhc(printedBox, ings.get(0)));

			ProductData fp = (ProductData) alfrescoRepository.findOne(refs[0]);
			dumpSvhc("finished product (no level at all)", fp);
			assertTrue("SVHC must propagate when no packaging level is filled in (#32363)", hasSvhc(fp, ings.get(0)));
			return null;
		});
	}

	/**
	 * The printed box aggregates the substances of the nested neutral box in its own list, and the
	 * finished product also walks the nested packaging: the substance must be counted once, so the
	 * finished product must hold the same amount whether the printed box carries an aggregate or not.
	 */
	@Test
	public void testNestedPrintedBoxIsNotCountedTwice() {
		final NodeRef[] refs = inWriteTx(() -> {
			NodeRef neutralBox = createNeutralBox();

			PackagingMaterialData printedBox = PackagingMaterialData.build().withName("Boite imprimee double comptage").withTare(1d, TareUnit.kg);
			printedBox.getPackagingListView().setPackagingList(List.of(PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.P)
					.withIsMaster(true).withPkgLevel(PackagingLevel.Primary).withProduct(neutralBox)));
			NodeRef printedBoxNodeRef = createNode(printedBox);
			NodeRef rawMaterial = createRawMaterial();

			FinishedProductData directFp = FinishedProductData.build().withName("Quiche direct reference").withUnit(ProductUnit.kg)
					.withCompoList(List.of(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withProduct(rawMaterial)))
					.withPackagingList(List.of(PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.P).withIsMaster(true)
							.withPkgLevel(PackagingLevel.Primary).withProduct(neutralBox)));

			FinishedProductData nestedFp = FinishedProductData.build().withName("Quiche nested comparison").withUnit(ProductUnit.kg)
					.withCompoList(List.of(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withProduct(rawMaterial)))
					.withPackagingList(List.of(PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.P).withIsMaster(true)
							.withPkgLevel(PackagingLevel.Primary).withProduct(printedBoxNodeRef)));

			return new NodeRef[] { alfrescoRepository.create(getTestFolderNodeRef(), directFp).getNodeRef(),
					alfrescoRepository.create(getTestFolderNodeRef(), nestedFp).getNodeRef(), printedBoxNodeRef };
		});

		inWriteTx(() -> {
			productService.formulate(refs[2]);
			productService.formulate(refs[0]);
			productService.formulate(refs[1]);
			return null;
		});

		inReadTx(() -> {
			ProductData directFp = (ProductData) alfrescoRepository.findOne(refs[0]);
			ProductData nestedFp = (ProductData) alfrescoRepository.findOne(refs[1]);
			dumpSvhc("direct reference", directFp);
			dumpSvhc("nested comparison", nestedFp);
			assertEquals("The nested neutral box must not be counted twice (#32363)", maxiOf(directFp, ings.get(0)),
					maxiOf(nestedFp, ings.get(0)), 0.0001d);
			return null;
		});
	}

	private Double maxiOf(ProductData product, NodeRef ing) {
		return product.getSvhcList().stream().filter(s -> ing.equals(s.getIng())).map(SvhcListDataItem::getMaxi).filter(m -> m != null).findFirst()
				.orElse(0d);
	}

	private NodeRef createRawMaterial() {
		RawMaterialData rm = RawMaterialData.build().withName("Quiche filling").withUnit(ProductUnit.kg);
		return createNode(rm);
	}

	private NodeRef createNeutralBox() {
		PackagingMaterialData neutralBox = PackagingMaterialData.build().withName("Boite neutre").withTare(1d, TareUnit.kg);
		neutralBox.setSvhcList(List.of(SvhcListDataItem.build().withIngredient(ings.get(0)).withQtyPerc(50d).withMigrationPerc(100d)));
		return createNode(neutralBox);
	}

	private boolean hasSvhc(ProductData fp, NodeRef ing) {
		if (fp.getSvhcList() == null) {
			return false;
		}
		return fp.getSvhcList().stream().anyMatch(s -> ing.equals(s.getIng()));
	}

	private void dumpSvhc(String label, ProductData fp) {
		logger.info("---- svhcList " + label + " ----");
		if (fp.getSvhcList() != null) {
			for (SvhcListDataItem s : fp.getSvhcList()) {
				logger.info("  ing=" + s.getIng() + " qtyPerc=" + s.getQtyPerc() + " value=" + s.getValue());
			}
		}
	}

}
