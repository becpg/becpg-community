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

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.packaging.VariantPackagingData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.formulation.PackagingHelper;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

/**
 * Reproduces #31701: the weight of a SECONDARY packaging (e.g. a "caisse") declared on a
 * sub-component must be counted at every composition level, not only when the packaging is
 * on the finished product itself. Only the PRIMARY packaging of components used to propagate.
 */
public class SecondaryPackagingWeightIT extends AbstractFinishedProductTest {

	private static final Log logger = LogFactory.getLog(SecondaryPackagingWeightIT.class);

	@Override
	public void setUp() throws Exception {
		super.setUp();
		initParts();
	}

	private NodeRef createSemiFinished(String name, boolean withSecondaryCaisse) {
		return inWriteTx(() -> {
			SemiFinishedProductData sf = new SemiFinishedProductData();
			sf.setName(name);
			sf.setUnit(ProductUnit.kg);
			sf.setQty(1d);
			sf.setDensity(1d);

			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial5NodeRef));
			sf.getCompoListView().setCompoList(compoList);

			if (withSecondaryCaisse) {
				List<PackagingListDataItem> packList = new ArrayList<>();
				// packagingMaterial4 = 50 g, declared as SECONDARY packaging (the "caisse")
				packList.add(PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.P).withPkgLevel(PackagingLevel.Secondary)
						.withIsMaster(true).withProduct(packagingMaterial4NodeRef));
				sf.getPackagingListView().setPackagingList(packList);
			}

			return alfrescoRepository.create(getTestFolderNodeRef(), sf).getNodeRef();
		});
	}

	private NodeRef createFinishedProductUsing(String name, NodeRef sfNodeRef) {
		return inWriteTx(() -> {
			FinishedProductData fp = new FinishedProductData();
			fp.setName(name);
			fp.setUnit(ProductUnit.kg);
			fp.setQty(1d);
			fp.setDensity(1d);

			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.P).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(sfNodeRef));
			fp.getCompoListView().setCompoList(compoList);

			List<PackagingListDataItem> packList = new ArrayList<>();
			// Primary packaging of the finished product (packagingMaterial1 = 15 g)
			packList.add(PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.P).withPkgLevel(PackagingLevel.Primary).withIsMaster(true)
					.withProduct(packagingMaterial1NodeRef));
			fp.getPackagingListView().setPackagingList(packList);

			return alfrescoRepository.create(getTestFolderNodeRef(), fp).getNodeRef();
		});
	}

	private NodeRef formulate(NodeRef nodeRef) {
		inWriteTx(() -> {
			productService.formulate(nodeRef);
			return null;
		});
		return nodeRef;
	}

	@Test
	public void testSecondaryPackagingOfComponentCountedAtAllLevels() {

		NodeRef sfWithCaisse = createSemiFinished("SF with caisse", true);
		NodeRef sfWithoutCaisse = createSemiFinished("SF without caisse", false);

		NodeRef fpWith = formulate(createFinishedProductUsing("FP with SF caisse", sfWithCaisse));
		NodeRef fpWithout = formulate(createFinishedProductUsing("FP without SF caisse", sfWithoutCaisse));

		inReadTx(() -> {
			ProductData with = (ProductData) alfrescoRepository.findOne(fpWith);
			ProductData without = (ProductData) alfrescoRepository.findOne(fpWithout);

			// Aggregated secondary/tertiary/inner tare of the composition (the #31701 contribution)
			VariantPackagingData compWith = PackagingHelper.getCompositionTareByLevel(with);
			VariantPackagingData compWithout = PackagingHelper.getCompositionTareByLevel(without);

			double secWith = compWith.getTareSecondary().doubleValue();
			double secWithout = compWithout.getTareSecondary().doubleValue();

			logger.info("#31701 composition secondary tare with caisse=" + secWith + " kg ; without=" + secWithout + " kg");
			logger.info("#31701 tare (primary) with=" + with.getTare() + " without=" + without.getTare());

			// The component's 50 g secondary caisse must be counted once (0.05 kg), scaled by qty (=1),
			// without re-multiplication by productPerBoxes.
			assertEquals("Component secondary caisse (50 g) must be aggregated at the secondary level", 0.05d, secWith, 0.0005d);
			assertEquals("No secondary caisse -> no secondary composition tare", 0d, secWithout, 0.0005d);

			// Primary tare must be identical (the caisse is secondary, not primary) -> proves no regression on primary.
			assertEquals("Primary tare must not be affected by a component secondary caisse", without.getTare(), with.getTare(), 0.0001);
			return null;
		});
	}

}
