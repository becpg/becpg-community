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

import fr.becpg.model.PLMModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.repository.model.SimpleListDataItem;

/**
 * The Class PhysicoChemCalculatingVisitor.
 *
 * @author querephi
 */
public class PhysicoChemCalculatingFormulationHandler extends AbstractSimpleListFormulationHandler<PhysicoChemListDataItem> {

	private static final Log logger = LogFactory.getLog(PhysicoChemCalculatingFormulationHandler.class);

	@Override
	protected Class<PhysicoChemListDataItem> getInstanceClass() {

		return PhysicoChemListDataItem.class;
	}

	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {
		logger.debug("Physico chemical calculating visitor");

		// no compo => no formulation
		if (!formulatedProduct.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
			logger.debug("no compo => no formulation");
			return true;
		}

		if (formulatedProduct.getPhysicoChemList() == null) {
			formulatedProduct.setPhysicoChemList(new LinkedList<PhysicoChemListDataItem>());
		}

		formulateSimpleList(formulatedProduct, formulatedProduct.getPhysicoChemList());

		return true;
	}

	@Override
	protected List<PhysicoChemListDataItem> getDataListVisited(ProductData partProduct) {
		return partProduct.getPhysicoChemList();
	}

	@Override
	protected boolean isCharactFormulated(SimpleListDataItem sl) {
		if (!super.isCharactFormulated(sl))
			return false;
		Boolean isFormulated = (Boolean) nodeService.getProperty(sl.getCharactNodeRef(), PLMModel.PROP_PHYSICO_CHEM_FORMULATED);
		return isFormulated != null ? isFormulated : false;
	}

	@Override
	protected boolean isCharactFormulatedFromVol(SimpleListDataItem sl) {
		Boolean isFormulatedFromVol = (Boolean) nodeService.getProperty(sl.getCharactNodeRef(), PLMModel.PROP_PHYSICO_CHEM_FORMULATED_FROM_VOL);
		return isFormulatedFromVol != null ? isFormulatedFromVol : false;
	}

	@Override
	protected Map<NodeRef, List<NodeRef>> getMandatoryCharacts(ProductData formulatedProduct, QName componentType) {
		return getMandatoryCharactsFromList(formulatedProduct.getPhysicoChemList());
	}
}
