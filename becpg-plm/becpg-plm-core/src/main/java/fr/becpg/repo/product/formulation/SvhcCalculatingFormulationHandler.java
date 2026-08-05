/*
 *
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.ing.IngItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.data.productList.SvhcListDataItem;
import fr.becpg.repo.regulatory.RequirementDataType;
import fr.becpg.repo.repository.model.SimpleListDataItem;
import fr.becpg.repo.variant.model.VariantData;

/**
 * <p>PhysicoChemCalculatingFormulationHandler class.</p>
 *
 * @author querephi
 * @version $Id: $Id
 */
public class SvhcCalculatingFormulationHandler extends AbstractSimpleListFormulationHandler<SvhcListDataItem> {

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(SvhcCalculatingFormulationHandler.class);

	/**
	 * {@inheritDoc}
	 *
	 * @return a {@link java.lang.Class} object
	 */
	protected Class<SvhcListDataItem> getInstanceClass() {
		return SvhcListDataItem.class;
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(ProductData formulatedProduct) {

		if (accept(formulatedProduct)) {
			logger.debug("Substances of Very High Concerns calculating visitor");

			if (formulatedProduct.getSvhcList() == null) {
				formulatedProduct.setSvhcList(new ArrayList<>());
			}

			boolean hasCompoEl = formulatedProduct.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))
					|| formulatedProduct.hasPackagingListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))
					|| formulatedProduct.hasProcessListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE));

			formulateSimpleList(formulatedProduct, formulatedProduct.getSvhcList(),

					new DefaultSimpleListQtyProvider(formulatedProduct) {

				
						@Override
						public Double getQty(PackagingListDataItem packagingListDataItem, ProductData componentProduct) {
							if (migratesToProduct(packagingListDataItem)) {
								return FormulationHelper.getQtyForCostByPackagingLevel(formulatedProduct, packagingListDataItem, componentProduct);
							}
							return null;
						}

						@Override
						public Double getQty(ProcessListDataItem processListDataItem, VariantData variant) {
							return 0d;
						}

					}, hasCompoEl);

			if (formulatedProduct.isRawMaterial() && !hasCompoEl) {
				addMPIngredientsToSvhcList(formulatedProduct);
			} else if(formulatedProduct.isGeneric()) {
				formulatedProduct.getSvhcList().forEach(n -> {
					n.setValue(n.getMaxi());
				});
			}

		}

		if (formulatedProduct.getSvhcList() != null) {

			formulatedProduct.getSvhcList().forEach(n -> {
				@SuppressWarnings("unchecked")
				List<String> reasonsForInclusion = (List<String>) nodeService.getProperty(n.getIng(), PLMModel.PROP_SVHC_REASONS_FOR_INCLUSION);
				if (reasonsForInclusion != null) {
					n.setReasonsForInclusion(new ArrayList<>(reasonsForInclusion));
				} else {
					n.setReasonsForInclusion(new ArrayList<>());
				}
			});
		}

		return true;
	}

	/**
	 * Hazardous substances only migrate from the packaging in contact with the product, hence the
	 * secondary and tertiary levels are left out. A level that was never filled in is read as primary,
	 * as it already is when the quantity is computed
	 * ({@link fr.becpg.repo.product.formulation.FormulationHelper#getQtyForCostByPackagingLevel}), so
	 * that a packaging line stays accounted for instead of being silently dropped (see #32363).
	 *
	 * @param packagingListDataItem the packaging line being visited
	 * @return {@code true} when the substances of that packaging must be aggregated
	 */
	private boolean migratesToProduct(PackagingListDataItem packagingListDataItem) {
		PackagingLevel pkgLevel = packagingListDataItem.getPkgLevel();
		return (pkgLevel == null) || PackagingLevel.Primary.equals(pkgLevel);
	}

	/**
	 * A packaging aggregates the substances of the packaging it embeds without keeping their migration
	 * rate, so its own formulated lines must not be aggregated again by the product using it: the nested
	 * definitions, which do carry the migration rate, are walked instead (see
	 * {@link fr.becpg.repo.product.formulation.AbstractSimpleListFormulationHandler#visitNestedPackaging}).
	 * Aggregating both would count the substance twice (see #32363).
	 *
	 * @param partProduct the component being visited
	 * @param visitedListItem the component substance line
	 * @return {@code true} when the line is an aggregate already covered by the nested walk
	 */
	private boolean isAggregatedPackagingItem(ProductData partProduct, SimpleListDataItem visitedListItem) {
		return partProduct.isPackaging() && partProduct.hasPackagingListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))
				&& (visitedListItem instanceof SvhcListDataItem svhcListDataItem) && (svhcListDataItem.getMigrationPerc() == null);
	}

	/**
	 * <p>addMPIngredientsToSvhcList.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 */
	private void addMPIngredientsToSvhcList(ProductData formulatedProduct) {
		List<SvhcListDataItem> svhcList = formulatedProduct.getSvhcList();

		for (IngListDataItem ing : formulatedProduct.getIngList()) {

			IngItem ingItem = (IngItem) alfrescoRepository.findOne(ing.getIng());

			if (Boolean.TRUE.equals(ingItem.getIsSubstanceOfVeryHighConcern())) {

				// if ing exists in the svhc list
				Optional<SvhcListDataItem> substance = svhcList.stream().filter(sub -> sub.getIng().equals(ing.getIng())).findFirst();
				if (substance.isPresent()) {
					substance.get().setQtyPerc(ing.getQtyPerc());
				} else {
					SvhcListDataItem svhcItem = SvhcListDataItem.build().withIngredient(ing.getIng()).withQtyPerc(ing.getQtyPerc());

					formulatedProduct.getSvhcList().add(svhcItem);
				}
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void calculate(ProductData formulatedProduct, ProductData partProduct, SimpleListDataItem calculatedListItem,
			SimpleListDataItem visitedListItem, Double qtyUsed, Double netQty, VariantData variant) {

		if (isAggregatedPackagingItem(partProduct, visitedListItem)) {
			return;
		}

		super.calculate(formulatedProduct, partProduct, calculatedListItem, visitedListItem, qtyUsed, netQty, variant);
	}

	/** {@inheritDoc} */
	@Override
	protected Double extractValue(ProductData formulatedProduct, ProductData partProduct, SimpleListDataItem slDataItem) {
		if (partProduct.isPackaging() && slDataItem instanceof SvhcListDataItem svhcListDataItem) {
			return SvhcCalculatingHelper.extractPackagingValue(svhcListDataItem);
		}
		return super.extractValue(formulatedProduct, partProduct, slDataItem);
	}

	/** {@inheritDoc} */
	@Override
	protected boolean accept(ProductData formulatedProduct) {
		return !(formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL) || (formulatedProduct instanceof ProductSpecificationData)
				|| ((formulatedProduct.getSvhcList() == null) && !alfrescoRepository.hasDataList(formulatedProduct, PLMModel.TYPE_SVHCLIST)));
	}

	/** {@inheritDoc} */
	@Override
	protected List<SvhcListDataItem> getDataListVisited(ProductData partProduct) {
		return partProduct.getSvhcList();
	}

	/** {@inheritDoc} */
	@Override
	protected boolean propagateNestedPackaging() {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	protected RequirementDataType getRequirementDataType() {
		return RequirementDataType.Formulation;
	}

	/** {@inheritDoc} */
	@Override
	protected boolean propagateModeEnable(ProductData formulatedProduct) {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	protected SvhcListDataItem newSimpleListDataItem(NodeRef charactNodeRef) {
		SvhcListDataItem ret = new SvhcListDataItem();
		ret.setCharactNodeRef(charactNodeRef);
		return ret;
	}

	/** {@inheritDoc} */
	@Override
	protected Map<NodeRef, List<NodeRef>> getMandatoryCharacts(ProductData formulatedProduct, QName componentType) {
		return new HashMap<>();
	}
}
