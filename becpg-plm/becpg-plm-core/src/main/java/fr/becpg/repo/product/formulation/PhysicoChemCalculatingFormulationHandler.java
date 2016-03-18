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
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.repository.model.SimpleListDataItem;

/**
 * @author querephi
 */
public class PhysicoChemCalculatingFormulationHandler extends AbstractSimpleListFormulationHandler<PhysicoChemListDataItem> {

	private static final Log logger = LogFactory.getLog(PhysicoChemCalculatingFormulationHandler.class);

	public static final String MESSAGE_PHYSICO_NOT_IN_RANGE = "message.formulate.physicoChemList.notInRangeValue";
	
	@Override
	protected Class<PhysicoChemListDataItem> getInstanceClass() {

		return PhysicoChemListDataItem.class;
	}

	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {

		if (accept(formulatedProduct)) {
			logger.debug("Physico chemical calculating visitor");

			if (formulatedProduct.getPhysicoChemList() == null) {
				formulatedProduct.setPhysicoChemList(new LinkedList<PhysicoChemListDataItem>());
			}

			// has compo
			if (formulatedProduct.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
				formulateSimpleList(formulatedProduct, formulatedProduct.getPhysicoChemList());
			}

			computeFormulatedList(formulatedProduct, formulatedProduct.getPhysicoChemList(), PLMModel.PROP_PHYSICO_CHEM_FORMULA,
					"message.formulate.physicoChemList.error");

			formulatedProduct.getPhysicoChemList().forEach(n -> {
				n.setUnit((String) nodeService.getProperty(n.getPhysicoChem(), PLMModel.PROP_PHYSICO_CHEM_UNIT));
			});

			checkRequirementsOfFormulatedProduct(formulatedProduct);
			
		}
		return true;
	}

	@Override
	protected boolean accept(ProductData formulatedProduct) {
		if (formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL) || ((formulatedProduct.getPhysicoChemList() == null)
				&& !alfrescoRepository.hasDataList(formulatedProduct, PLMModel.TYPE_PHYSICOCHEMLIST))) {
			return false;
		}
		return true;
	}

	@Override
	protected List<PhysicoChemListDataItem> getDataListVisited(ProductData partProduct) {
		return partProduct.getPhysicoChemList();
	}

	@Override
	protected boolean isCharactFormulated(SimpleListDataItem sl) {
		if (!super.isCharactFormulated(sl)) {
			return false;
		}
		Boolean isFormulated = (Boolean) nodeService.getProperty(sl.getCharactNodeRef(), PLMModel.PROP_PHYSICO_CHEM_FORMULATED);
		return isFormulated != null ? isFormulated : false;
	}

	@Override
	protected Map<NodeRef, List<NodeRef>> getMandatoryCharacts(ProductData formulatedProduct, QName componentType) {
		return getMandatoryCharactsFromList(formulatedProduct.getPhysicoChemList());
	}

	@Override
	protected RequirementDataType getRequirementDataType() {
		return RequirementDataType.Physicochem;
	}

	@Override
	protected String getSpecErrorMessageKey() {
		return MESSAGE_PHYSICO_NOT_IN_RANGE;
	}

}
