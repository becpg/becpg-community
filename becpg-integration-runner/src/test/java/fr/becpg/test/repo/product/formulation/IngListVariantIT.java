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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.variant.model.VariantData;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

/**
 * Reproduces #35430: the aggregated ingredient list (ingList) shows wrong
 * percentages when the composition contains an inactive (non-default) variant
 * line. Only the default/active variant should be taken into account, exactly
 * like the labeling and the ingredient detail popup do.
 * <p>
 * The regression (#33082, 25.3) made the ingList use a shared, stateful
 * {@code FormulationFilters.EFFECTIVE_VARIANT_COMPO} instance. Its default
 * variant node set is cached on first use and frozen for the whole JVM, so a
 * product formulated after another variant product is filtered against a stale
 * variant node set: its own default variant line is dropped and the ingredient
 * it carries is diluted down to the contribution of the common lines only.
 */
public class IngListVariantIT extends AbstractFinishedProductTest {

	private static final Log logger = LogFactory.getLog(IngListVariantIT.class);

	@Autowired
	protected ProductService productService;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		initParts();
	}

	private NodeRef createRawMaterial(String name, List<IngListDataItem> ingList) {
		return inWriteTx(() -> {
			RawMaterialData rm = new RawMaterialData();
			rm.setName(name);
			rm.setUnit(ProductUnit.kg);
			rm = (RawMaterialData) alfrescoRepository.create(getTestFolderNodeRef(), rm);
			rm.setIngList(ingList);
			alfrescoRepository.save(rm);
			return rm.getNodeRef();
		});
	}

	/**
	 * Formulates a first, unrelated product that has its own default variant so
	 * that the shared variant filter caches this product's variant node set.
	 */
	private void formulateVariantPrimer() {
		final NodeRef rm = createRawMaterial("Primer MP (ing2)",
				List.of(IngListDataItem.build().withIngredient(ing2).withQtyPerc(100d)));

		final NodeRef primerNodeRef = inWriteTx(() -> {
			FinishedProductData fp = new FinishedProductData();
			fp.setName("FP primer (other variant)");
			fp.setUnit(ProductUnit.kg);
			fp.setQty(1d);
			fp.setDensity(1d);
			fp = (FinishedProductData) alfrescoRepository.create(getTestFolderNodeRef(), fp);
			Map<QName, Serializable> props = new HashMap<>();
			props.put(ContentModel.PROP_NAME, "primer default variant");
			props.put(BeCPGModel.PROP_IS_DEFAULT_VARIANT, true);
			nodeService.createNode(fp.getNodeRef(), BeCPGModel.ASSOC_VARIANTS, BeCPGModel.ASSOC_VARIANTS, BeCPGModel.TYPE_VARIANT, props);
			return fp.getNodeRef();
		});

		inWriteTx(() -> {
			FinishedProductData fp = (FinishedProductData) alfrescoRepository.findOne(primerNodeRef);
			VariantData v = fp.getVariants().get(0);
			CompoListDataItem line = CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg)
					.withDeclarationType(DeclarationType.Declare).withProduct(rm);
			line.setVariants(Collections.singletonList(v.getNodeRef()));
			fp.getCompoListView().setCompoList(new ArrayList<>(List.of(line)));
			alfrescoRepository.save(fp);
			return null;
		});

		inWriteTx(() -> {
			productService.formulate(primerNodeRef);
			return null;
		});
	}

	@Test
	public void testIngListWithInactiveVariant() {

		// Pollute the shared variant filter with an unrelated product's variant node set.
		formulateVariantPrimer();

		// base MP carries both ing1 (only common source) and ing3 (also carried by the sugar lines)
		final NodeRef rmBase = createRawMaterial("Base MP (ing1+ing3)",
				List.of(IngListDataItem.build().withIngredient(ing1).withQtyPerc(70d),
						IngListDataItem.build().withIngredient(ing3).withQtyPerc(30d)));
		// two alternative "sugar" raw materials -> ing3
		final NodeRef rmSucre1 = createRawMaterial("Sucre blanc (ing3)",
				List.of(IngListDataItem.build().withIngredient(ing3).withQtyPerc(100d)));
		final NodeRef rmSucre2 = createRawMaterial("Sucre cd2 (ing3)",
				List.of(IngListDataItem.build().withIngredient(ing3).withQtyPerc(100d)));

		// ---- Baseline: no variant, base + sucre1 only ----
		final NodeRef baselineNodeRef = inWriteTx(() -> {
			FinishedProductData fp = new FinishedProductData();
			fp.setName("FP baseline (no variant)");
			fp.setUnit(ProductUnit.kg);
			fp.setQty(4d);
			fp.setDensity(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withQtyUsed(3d).withUnit(ProductUnit.kg)
					.withDeclarationType(DeclarationType.Declare).withProduct(rmBase));
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg)
					.withDeclarationType(DeclarationType.Declare).withProduct(rmSucre1));
			fp.getCompoListView().setCompoList(compoList);
			return alfrescoRepository.create(getTestFolderNodeRef(), fp).getNodeRef();
		});

		inWriteTx(() -> {
			productService.formulate(baselineNodeRef);
			return null;
		});

		final Double baselineSucrePerc = inReadTx(() -> {
			ProductData fp = (ProductData) alfrescoRepository.findOne(baselineNodeRef);
			dumpIngList("BASELINE (no variant)", fp);
			return ingPerc(fp, ing3);
		});

		// ---- Variant product: base + sucre1 (default variant) + sucre2 (inactive variant) ----
		final NodeRef variantNodeRef = inWriteTx(() -> {
			FinishedProductData fp = new FinishedProductData();
			fp.setName("FP with variant");
			fp.setUnit(ProductUnit.kg);
			fp.setQty(4d);
			fp.setDensity(1d);
			fp = (FinishedProductData) alfrescoRepository.create(getTestFolderNodeRef(), fp);

			Map<QName, Serializable> defProps = new HashMap<>();
			defProps.put(ContentModel.PROP_NAME, "default variant");
			defProps.put(BeCPGModel.PROP_IS_DEFAULT_VARIANT, true);
			nodeService.createNode(fp.getNodeRef(), BeCPGModel.ASSOC_VARIANTS, BeCPGModel.ASSOC_VARIANTS, BeCPGModel.TYPE_VARIANT, defProps);

			Map<QName, Serializable> altProps = new HashMap<>();
			altProps.put(ContentModel.PROP_NAME, "alt variant");
			altProps.put(BeCPGModel.PROP_IS_DEFAULT_VARIANT, false);
			nodeService.createNode(fp.getNodeRef(), BeCPGModel.ASSOC_VARIANTS, BeCPGModel.ASSOC_VARIANTS, BeCPGModel.TYPE_VARIANT, altProps);
			return fp.getNodeRef();
		});

		inWriteTx(() -> {
			FinishedProductData fp = (FinishedProductData) alfrescoRepository.findOne(variantNodeRef);
			List<VariantData> variants = fp.getVariants();
			VariantData defaultVariant = variants.stream().filter(v -> Boolean.TRUE.equals(v.getIsDefaultVariant())).findFirst().orElse(null);
			VariantData altVariant = variants.stream().filter(v -> !Boolean.TRUE.equals(v.getIsDefaultVariant())).findFirst().orElse(null);

			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withQtyUsed(3d).withUnit(ProductUnit.kg)
					.withDeclarationType(DeclarationType.Declare).withProduct(rmBase));

			CompoListDataItem sucre1Line = CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg)
					.withDeclarationType(DeclarationType.Declare).withProduct(rmSucre1);
			sucre1Line.setVariants(Collections.singletonList(defaultVariant.getNodeRef()));
			compoList.add(sucre1Line);

			CompoListDataItem sucre2Line = CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg)
					.withDeclarationType(DeclarationType.Declare).withProduct(rmSucre2);
			sucre2Line.setVariants(Collections.singletonList(altVariant.getNodeRef()));
			compoList.add(sucre2Line);

			fp.getCompoListView().setCompoList(compoList);
			alfrescoRepository.save(fp);
			return null;
		});

		inWriteTx(() -> {
			productService.formulate(variantNodeRef);
			return null;
		});

		inReadTx(() -> {
			ProductData fp = (ProductData) alfrescoRepository.findOne(variantNodeRef);
			dumpIngList("VARIANT product", fp);
			Double variantSucrePerc = ingPerc(fp, ing3);
			logger.info("#35430 baseline ing3 %=" + baselineSucrePerc + " ; variant ing3 %=" + variantSucrePerc);
			assertNotNull("ing3 should be present in the ingList", variantSucrePerc);
			assertNotNull(baselineSucrePerc);
			assertEquals("ing3 % must match the no-variant baseline (only the default variant counts, sugar not diluted)",
					baselineSucrePerc, variantSucrePerc, 0.01);
			return null;
		});
	}

	/**
	 * Reproduces the second point of #35430: a local semi-finished whose sub-lines are
	 * variants of each other. The sub-composition summed every variant, so a 100 g
	 * semi-finished made of two alternative 100 g lines was credited 200 g of components
	 * and got a 50 % yield out of nowhere, which then skewed the labeling percentages.
	 */
	@Test
	public void testLocalSemiFinishedYieldWithInactiveVariant() {

		final NodeRef rmSucre1 = createRawMaterial("LSF sucre blanc (ing3)",
				List.of(IngListDataItem.build().withIngredient(ing3).withQtyPerc(100d)));
		final NodeRef rmSucre2 = createRawMaterial("LSF sucre cd2 (ing3)",
				List.of(IngListDataItem.build().withIngredient(ing3).withQtyPerc(100d)));

		final NodeRef lsfNodeRef = inWriteTx(() -> {
			LocalSemiFinishedProductData lsf = new LocalSemiFinishedProductData();
			lsf.setName("LSF post-cuisson");
			lsf.setUnit(ProductUnit.kg);
			lsf.setQty(1d);
			lsf.setDensity(1d);
			return alfrescoRepository.create(getTestFolderNodeRef(), lsf).getNodeRef();
		});

		final NodeRef fpNodeRef = inWriteTx(() -> {
			FinishedProductData fp = new FinishedProductData();
			fp.setName("FP with LSF and variants");
			fp.setUnit(ProductUnit.kg);
			fp.setQty(1d);
			fp.setDensity(1d);
			fp = (FinishedProductData) alfrescoRepository.create(getTestFolderNodeRef(), fp);

			Map<QName, Serializable> defProps = new HashMap<>();
			defProps.put(ContentModel.PROP_NAME, "LSF default variant");
			defProps.put(BeCPGModel.PROP_IS_DEFAULT_VARIANT, true);
			nodeService.createNode(fp.getNodeRef(), BeCPGModel.ASSOC_VARIANTS, BeCPGModel.ASSOC_VARIANTS, BeCPGModel.TYPE_VARIANT, defProps);

			Map<QName, Serializable> altProps = new HashMap<>();
			altProps.put(ContentModel.PROP_NAME, "LSF alt variant");
			altProps.put(BeCPGModel.PROP_IS_DEFAULT_VARIANT, false);
			nodeService.createNode(fp.getNodeRef(), BeCPGModel.ASSOC_VARIANTS, BeCPGModel.ASSOC_VARIANTS, BeCPGModel.TYPE_VARIANT, altProps);
			return fp.getNodeRef();
		});

		inWriteTx(() -> {
			FinishedProductData fp = (FinishedProductData) alfrescoRepository.findOne(fpNodeRef);
			List<VariantData> variants = fp.getVariants();
			VariantData defaultVariant = variants.stream().filter(v -> Boolean.TRUE.equals(v.getIsDefaultVariant())).findFirst().orElse(null);
			VariantData altVariant = variants.stream().filter(v -> !Boolean.TRUE.equals(v.getIsDefaultVariant())).findFirst().orElse(null);

			List<CompoListDataItem> compoList = new ArrayList<>();

			// the semi-finished weighs exactly one of its two alternative sub-lines
			CompoListDataItem lsfLine = CompoListDataItem.build().withParent(null).withQtyUsed(0.1d).withUnit(ProductUnit.kg)
					.withDeclarationType(DeclarationType.Declare).withProduct(lsfNodeRef);
			compoList.add(lsfLine);

			CompoListDataItem sucre1Line = CompoListDataItem.build().withParent(lsfLine).withQtyUsed(0.1d).withUnit(ProductUnit.kg)
					.withDeclarationType(DeclarationType.Declare).withProduct(rmSucre1);
			sucre1Line.setVariants(Collections.singletonList(defaultVariant.getNodeRef()));
			compoList.add(sucre1Line);

			CompoListDataItem sucre2Line = CompoListDataItem.build().withParent(lsfLine).withQtyUsed(0.1d).withUnit(ProductUnit.kg)
					.withDeclarationType(DeclarationType.Declare).withProduct(rmSucre2);
			sucre2Line.setVariants(Collections.singletonList(altVariant.getNodeRef()));
			compoList.add(sucre2Line);

			fp.getCompoListView().setCompoList(compoList);
			alfrescoRepository.save(fp);
			return null;
		});

		inWriteTx(() -> {
			productService.formulate(fpNodeRef);
			return null;
		});

		inReadTx(() -> {
			ProductData fp = (ProductData) alfrescoRepository.findOne(fpNodeRef);
			Double lsfYield = null;
			for (CompoListDataItem item : fp.getCompoList()) {
				logger.info("  compo product=" + item.getProduct() + " qty=" + item.getQty() + " yield=" + item.getYieldPerc());
				if (lsfNodeRef.equals(item.getProduct())) {
					lsfYield = item.getYieldPerc();
				}
			}
			assertNotNull("the semi-finished line should be present", lsfYield);
			assertEquals("only the default variant feeds the semi-finished, so no yield should appear", 100d, lsfYield, 0.01);
			return null;
		});
	}

	private Double ingPerc(ProductData fp, NodeRef ing) {
		for (IngListDataItem item : fp.getIngList()) {
			if (ing.equals(item.getIng())) {
				return item.getQtyPerc();
			}
		}
		return null;
	}

	private void dumpIngList(String label, ProductData fp) {
		logger.info("---- ingList " + label + " ----");
		for (IngListDataItem item : fp.getIngList()) {
			logger.info("  ing=" + item.getIng() + " qtyPerc=" + item.getQtyPerc() + " qtyPercWithYield=" + item.getQtyPercWithYield());
		}
	}

}
