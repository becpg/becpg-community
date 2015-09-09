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
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.variant.filters.VariantFilters;

/**
 * The Class NutsCalculatingVisitor.
 *
 * @author querephi
 */
public class NutsCalculatingFormulationHandler extends AbstractSimpleListFormulationHandler<NutListDataItem> {

	public static final String UNIT_PER100G = "/100g";

	public static final String UNIT_PER100ML = "/100mL";

	public static final String NUT_FORMULATED = I18NUtil.getMessage("message.formulate.nut.formulated");

	public static final String MESSAGE_MAXIMAL_DAILY_VALUE = "message.formulate.nut.maximalDailyValue";

	private static final Log logger = LogFactory.getLog(NutsCalculatingFormulationHandler.class);

	@Override
	protected Class<NutListDataItem> getInstanceClass() {
		return NutListDataItem.class;
	}

	@Override
	public boolean process(ProductData formulatedProduct) throws FormulateException {
		logger.debug("Nuts calculating visitor");

		// no compo => no formulation
		if ((formulatedProduct.getNutList() == null) && !alfrescoRepository.hasDataList(formulatedProduct, PLMModel.TYPE_NUTLIST)) {
			logger.debug("no compo => no formulation");
			return true;
		}

		boolean hasCompo = formulatedProduct.hasCompoListEl(new VariantFilters<>());

		if (formulatedProduct.getNutList() == null) {
			formulatedProduct.setNutList(new LinkedList<NutListDataItem>());
		}

		if (hasCompo) {
			formulateSimpleList(formulatedProduct, formulatedProduct.getNutList());
		}

		if (formulatedProduct.getNutList() != null) {

			computeFormulatedList(formulatedProduct, formulatedProduct.getNutList(), PLMModel.PROP_NUT_FORMULA, "message.formulate.nutList.error");

			for (NutListDataItem n : formulatedProduct.getNutList()) {

				n.setGroup((String) nodeService.getProperty(n.getNut(), PLMModel.PROP_NUTGROUP));
				n.setUnit(calculateUnit(formulatedProduct.getUnit(), (String) nodeService.getProperty(n.getNut(), PLMModel.PROP_NUTUNIT)));

				if (n.getLossPerc() != null) {
					if (n.getValue() != null) {
						n.setValue(n.getValue() * (100 - n.getLossPerc()) / 100);
					}
					if (n.getMini() != null) {
						n.setMini(n.getMini() * (100 - n.getLossPerc()) / 100);
					}
					if (n.getMaxi() != null) {
						n.setMaxi(n.getMaxi() * (100 - n.getLossPerc()) / 100);
					}
				}

				if (formulatedProduct.getServingSize() != null && n.getValue() != null) {
					double valuePerserving = n.getValue() * formulatedProduct.getServingSize() / 100;
					n.setValuePerServing(valuePerserving);
					Double gda = (Double) nodeService.getProperty(n.getNut(), PLMModel.PROP_NUTGDA);
					if (gda != null && gda != 0d) {
						n.setGdaPerc(100 * n.getValuePerServing() / gda);
					}
					Double ul = (Double) nodeService.getProperty(n.getNut(), PLMModel.PROP_NUTUL);
					if (ul != null) {
						if (valuePerserving > ul) {
							String message = I18NUtil.getMessage(MESSAGE_MAXIMAL_DAILY_VALUE,
									nodeService.getProperty(n.getNut(), BeCPGModel.PROP_CHARACT_NAME));
							formulatedProduct.getCompoListView().getReqCtrlList()
									.add(new ReqCtrlListDataItem(null, RequirementType.Forbidden, message, n.getNut(), new ArrayList<NodeRef>()));
						}
					}
				} else {
					n.setValuePerServing(null);
					n.setGdaPerc(null);
				}

				if (isCharactFormulated(n) && hasCompo) {
					if (n.getManualValue() == null) {
						n.setMethod(NUT_FORMULATED);
					}
				}

				if (transientFormulation) {
					n.setTransient(true);
				}
			}
		}

		return true;
	}

	/**
	 * Calculate the nutListUnit
	 *
	 * @param productUnit
	 * @param nutUnit
	 * @return
	 */
	public static String calculateUnit(ProductUnit productUnit, String nutUnit) {

		return nutUnit += calculateSuffixUnit(productUnit);
	}

	/**
	 * Calculate the suffix of nutListUnit
	 *
	 * @param productUnit
	 * @return
	 */
	public static String calculateSuffixUnit(ProductUnit productUnit) {
		if (ProductUnit.L.equals(productUnit) || ProductUnit.mL.equals(productUnit))
			return UNIT_PER100ML;
		else
			return UNIT_PER100G;
	}

	@Override
	protected List<NutListDataItem> getDataListVisited(ProductData partProduct) {
		return partProduct.getNutList();
	}

	@Override
	protected Map<NodeRef, List<NodeRef>> getMandatoryCharacts(ProductData formulatedProduct, QName componentType) {
		Map<NodeRef, List<NodeRef>> mandatoryCharacts = new HashMap<>();
		for (Map.Entry<NodeRef, List<NodeRef>> kv : getMandatoryCharactsFromList(formulatedProduct.getNutList()).entrySet()) {
			String formula = (String) nodeService.getProperty(kv.getKey(), PLMModel.PROP_NUT_FORMULA);
			if (formula == null || formula.isEmpty()) {
				mandatoryCharacts.put(kv.getKey(), kv.getValue());
			}
		}
		return mandatoryCharacts;
	}
}
