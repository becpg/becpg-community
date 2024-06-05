/*
 *
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.ing.IngItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.data.productList.SvhcListDataItem;
import fr.becpg.repo.repository.model.SimpleListDataItem;
import fr.becpg.repo.variant.model.VariantData;

/**
 * <p>PhysicoChemCalculatingFormulationHandler class.</p>
 *
 * @author querephi
 * @version $Id: $Id
 */
public class SvhcCalculatingFormulationHandler extends AbstractSimpleListFormulationHandler<SvhcListDataItem> {

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
				formulatedProduct.setSvhcList(new LinkedList<>());
			}

			boolean hasCompoEl = formulatedProduct.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))
					|| formulatedProduct.hasPackagingListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))
					|| formulatedProduct.hasProcessListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE));

			formulateSimpleList(formulatedProduct, formulatedProduct.getSvhcList(),

					new DefaultSimpleListQtyProvider(formulatedProduct) {

						@Override
						public Double getQty(CompoListDataItem compoListDataItem, Double parentLossRatio, ProductData componentProduct) {
							if(formulatedProduct.isGeneric()) {
								return 100d;
							}
							return super.getQty(compoListDataItem, parentLossRatio, componentProduct);
						}
				
						@Override
						public Double getQty(PackagingListDataItem packagingListDataItem, ProductData componentProduct) {
							if (PackagingLevel.Primary.equals(packagingListDataItem.getPkgLevel())) {
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
	 * @param formulatedProduct
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
	protected Double extractValue(ProductData formulatedProduct, ProductData partProduct, SimpleListDataItem slDataItem) {
		if (partProduct.isPackaging() && slDataItem instanceof SvhcListDataItem svhcListDataItem) {

			Double migrationPerc = svhcListDataItem.getMigrationPerc();
			if (migrationPerc == null || migrationPerc == 0d) {
				return null;
			}

			if (svhcListDataItem.getQtyPerc() != null) {
				return migrationPerc * svhcListDataItem.getQtyPerc() / 100d;
			}

			return null;
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
