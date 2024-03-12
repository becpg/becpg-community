/*
 *
 */
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
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

	/** {@inheritDoc} */
	protected Class<SvhcListDataItem> getInstanceClass() {
		return SvhcListDataItem.class;
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(ProductData formulatedProduct) {

		boolean accept = accept(formulatedProduct);

		if (accept) {
			logger.debug("Substances of Very High Concerns calculating visitor");

			if (formulatedProduct.getSvhcList() == null) {
				formulatedProduct.setSvhcList(new LinkedList<>());
			}

			formulateSimpleList(formulatedProduct, formulatedProduct.getSvhcList(), new SimpleListQtyProvider() {

				@Override
				public Double getQty(CompoListDataItem compoListDataItem, Double parentLossRatio, ProductData componentProduct) {
					return FormulationHelper.getQtyInKg(compoListDataItem);
				}

				@Override
				public Double getVolume(CompoListDataItem compoListDataItem, Double parentLossRatio, ProductData componentProduct) {
					return FormulationHelper.getNetVolume(compoListDataItem, componentProduct);
				}

				@Override
				public Double getNetWeight(VariantData variant) {
					return FormulationHelper.getNetWeight(formulatedProduct, variant, FormulationHelper.DEFAULT_NET_WEIGHT);
				}

				@Override
				public Double getNetQty(VariantData variant) {
					return FormulationHelper.getNetQtyInLorKg(formulatedProduct, variant, FormulationHelper.DEFAULT_NET_WEIGHT);
				}

				@Override
				public Boolean omitElement(CompoListDataItem compoListDataItem) {
					return DeclarationType.Omit.equals(compoListDataItem.getDeclType());
				}

				@Override
				public Double getQty(PackagingListDataItem packagingListDataItem, ProductData componentProduct) {
					return FormulationHelper.getQtyForCostByPackagingLevel(formulatedProduct, packagingListDataItem, componentProduct);
				}

				@Override
				public Double getQty(ProcessListDataItem processListDataItem, VariantData variant) {
					return FormulationHelper.getQty(formulatedProduct, variant, processListDataItem);
				}

			}, formulatedProduct.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE)));

			addMPIngredientsToSvhcList(formulatedProduct);

		}

		if (accept || formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL)
				|| (formulatedProduct instanceof ProductSpecificationData)) {

			formulatedProduct.getSvhcList().forEach(n -> {
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

		QName nodeType = nodeService.getType(formulatedProduct.getNodeRef());

		if (PLMModel.TYPE_RAWMATERIAL.equals(nodeType)) {

			for (IngListDataItem ing : formulatedProduct.getIngList()) {

				IngItem ingItem = (IngItem) alfrescoRepository.findOne(ing.getIng());

				if (Boolean.TRUE.equals(ingItem.getIsSubstanceOfVeryHighConcern())) {

					// if ing exists in the svhc list
					if (svhcList.stream().map(SvhcListDataItem::getIng).anyMatch(svhcIngNodeRef -> svhcIngNodeRef.equals(ing.getIng()))) {
						SvhcListDataItem substance = svhcList.stream().filter(sub -> sub.getIng().equals(ing.getIng())).findFirst().get();
						substance.setQtyPerc(ing.getQtyPerc());

					} else {
						SvhcListDataItem svhcItem = SvhcListDataItem.build();
						svhcItem.withIngredient(ing.getIng());
						svhcItem.withQtyPerc(ing.getQtyPerc());

						formulatedProduct.getSvhcList().add(svhcItem);
					}
				}
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	protected boolean accept(ProductData formulatedProduct) {
		return !(formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL) || (formulatedProduct instanceof ProductSpecificationData)
				|| ((formulatedProduct.getPhysicoChemList() == null)
						&& !alfrescoRepository.hasDataList(formulatedProduct, PLMModel.TYPE_PHYSICOCHEMLIST)));
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

	@Override
	protected boolean propagateModeEnable(ProductData formulatedProduct) {
		return true;
	}

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
