/*
 *
 */
package fr.becpg.repo.product.formulation;

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
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.repository.model.SimpleListDataItem;
import fr.becpg.repo.repository.model.VariantAwareDataItem;

/**
 * <p>PhysicoChemCalculatingFormulationHandler class.</p>
 *
 * @author querephi
 * @version $Id: $Id
 */
public class PhysicoChemCalculatingFormulationHandler extends AbstractSimpleListFormulationHandler<PhysicoChemListDataItem> {

	private static final Log logger = LogFactory.getLog(PhysicoChemCalculatingFormulationHandler.class);

	/** {@inheritDoc} */
	@Override
	protected Class<PhysicoChemListDataItem> getInstanceClass() {

		return PhysicoChemListDataItem.class;
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(ProductData formulatedProduct) {

		if (accept(formulatedProduct)) {
			logger.debug("Physico chemical calculating visitor");

			if (formulatedProduct.getPhysicoChemList() == null) {
				formulatedProduct.setPhysicoChemList(new LinkedList<>());
			}

			formulateSimpleList(formulatedProduct, formulatedProduct.getPhysicoChemList(),
					formulatedProduct.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE)));

			computeFormulatedList(formulatedProduct, formulatedProduct.getPhysicoChemList(), PLMModel.PROP_PHYSICO_CHEM_FORMULA,
					"message.formulate.physicoChemList.error");

			formulatedProduct.getPhysicoChemList().forEach(n -> {

				String unit = (String) nodeService.getProperty(n.getPhysicoChem(), PLMModel.PROP_PHYSICO_CHEM_UNIT);

				n.setUnit(unit);
		    /* Don't flat perc #6797
				n.setFormulatedValue(FormulationHelper.flatPercValue(n.getFormulatedValue(), unit));
				n.setMaxi(FormulationHelper.flatPercValue(n.getMaxi(), unit));
				n.setMini(FormulationHelper.flatPercValue(n.getMini(), unit));
				if (n instanceof VariantAwareDataItem) {
					for (int i=1; i<=VariantAwareDataItem.VARIANT_COLUMN_SIZE ; i++) {
						(n).setValue(FormulationHelper.flatPercValue((n).getValue(VariantAwareDataItem.VARIANT_COLUMN_NAME+i), unit), VariantAwareDataItem.VARIANT_COLUMN_NAME+i);
					}
				} 
			  */
				n.setType((String) nodeService.getProperty(n.getPhysicoChem(), PLMModel.PROP_PHYSICO_CHEM_TYPE));

			});

		} else if (formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL) || (formulatedProduct instanceof ProductSpecificationData)) {
			formulatedProduct.getPhysicoChemList().forEach(n -> {

				n.setUnit((String) nodeService.getProperty(n.getPhysicoChem(), PLMModel.PROP_PHYSICO_CHEM_UNIT));
				n.setType((String) nodeService.getProperty(n.getPhysicoChem(), PLMModel.PROP_PHYSICO_CHEM_TYPE));

			});

		}

		return true;
	}

	/** {@inheritDoc} */
	@Override
	protected boolean accept(ProductData formulatedProduct) {
		if (formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL) || (formulatedProduct instanceof ProductSpecificationData)
				|| ((formulatedProduct.getPhysicoChemList() == null)
						&& !alfrescoRepository.hasDataList(formulatedProduct, PLMModel.TYPE_PHYSICOCHEMLIST))) {
			return false;
		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	protected List<PhysicoChemListDataItem> getDataListVisited(ProductData partProduct) {
		return partProduct.getPhysicoChemList();
	}

	/** {@inheritDoc} */
	@Override
	protected boolean isCharactFormulated(SimpleListDataItem sl) {
		if (!super.isCharactFormulated(sl)) {
			return false;
		}
		Boolean isFormulated = (Boolean) nodeService.getProperty(sl.getCharactNodeRef(), PLMModel.PROP_PHYSICO_CHEM_FORMULATED);
		return isFormulated != null ? isFormulated : false;
	}

	/** {@inheritDoc} */
	@Override
	protected Map<NodeRef, List<NodeRef>> getMandatoryCharacts(ProductData formulatedProduct, QName componentType) {
		return getMandatoryCharactsFromList(formulatedProduct.getPhysicoChemList());
	}

	/** {@inheritDoc} */
	@Override
	protected RequirementDataType getRequirementDataType() {
		return RequirementDataType.Physicochem;
	}

}
