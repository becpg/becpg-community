package fr.becpg.repo.product.requirement;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.formulation.nutrient.RegulationFormulationHelper;

/**
 * <p>NutsRequirementScanner class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class NutsRequirementScanner extends SimpleListRequirementScanner<NutListDataItem> {

	/** Constant <code>MESSAGE_NUT_NOT_IN_RANGE="message.formulate.nut.notInRangeValue"</code> */
	public static final String MESSAGE_NUT_NOT_IN_RANGE = "message.formulate.nut.notInRangeValue";

	/** Constant <code>MESSAGE_NUT_NOT_IN_RANGE_INFO="message.formulate.info.nut.notInRangeVa"{trunked}</code> */
	public static final String MESSAGE_NUT_NOT_IN_RANGE_INFO = "message.formulate.info.nut.notInRangeValue";

	private static final Log logger = LogFactory.getLog(NutsRequirementScanner.class);

	/** {@inheritDoc} */
	@Override
	protected String getSpecErrorMessageKey(NutListDataItem specDataItem) {
		if (specDataItem.getRequirementType() != null) {
			return MESSAGE_NUT_NOT_IN_RANGE + "." + specDataItem.getRequirementType().toString();
		}
		return MESSAGE_NUT_NOT_IN_RANGE;
	}

	/** {@inheritDoc} */
	@Override
	protected List<NutListDataItem> getDataListVisited(ProductData partProduct) {
		return partProduct.getNutList() != null ? partProduct.getNutList() : new ArrayList<>();
	}

	/** {@inheritDoc} */
	@Override
	protected String getSpecInfoMessageKey(NutListDataItem specDataItem) {
		return MESSAGE_NUT_NOT_IN_RANGE_INFO;
	}

	/** {@inheritDoc} */
	@Override
	protected Double getValue(NutListDataItem specListDataItem, NutListDataItem listDataItem) {

		String countryKey = extractCountryKey(specListDataItem);

		if (specListDataItem.getRequirementType() != null) {
			switch (specListDataItem.getRequirementType()) {
			case Serving:
				return listDataItem.getValuePerServing();
			case GdaPerc:
				return listDataItem.getGdaPerc();
			case AsPrepared:
				return listDataItem.getPreparedValue();
			case LabeledServing:
				return roundedOrRawValue(listDataItem.valuePerServing(countryKey), listDataItem.getValuePerServing(), countryKey);
			case LabeledAsPrepared:
				return roundedOrRawValue(listDataItem.preparedValue(countryKey), listDataItem.getPreparedValue(), countryKey);
			case LabeledGdaPerc:
				return roundedOrRawValue(listDataItem.gdaPerc(countryKey), listDataItem.getGdaPerc(), countryKey);
			case LabeledValue:
				return roundedOrRawValue(listDataItem.value(countryKey), listDataItem.getValue(), countryKey);
			case LabeledAsPreparedServing,AsPreparedServing:
				return roundedOrRawValue(listDataItem.preparedValuePerServing(countryKey), listDataItem.getValuePerServing(), countryKey);
			default:
				break;
			}
		}

		return listDataItem.getValue();
	}

	/**
	 * Returns the rounded value when available, otherwise falls back to the raw value
	 * so that the requirement is still checked when the roundedValue JSON is missing
	 * or does not contain the requested regulation key.
	 *
	 * @param roundedValue the value extracted from the roundedValue JSON, can be null
	 * @param rawValue the raw value of the product nut list item, can be null
	 * @param countryKey the regulation key used for the extraction
	 * @return the rounded value if available, the raw value otherwise
	 */
	private Double roundedOrRawValue(Double roundedValue, Double rawValue, String countryKey) {
		if (roundedValue != null) {
			return roundedValue;
		}
		if ((rawValue != null) && logger.isDebugEnabled()) {
			logger.debug("No rounded value found for regulation key '" + countryKey + "', falling back to raw value: " + rawValue);
		}
		return rawValue;
	}

	/**
	 * <p>extractCountryKey.</p>
	 *
	 * @param regulatoryListItem a {@link fr.becpg.repo.product.data.productList.NutListDataItem} object
	 * @return a {@link java.lang.String} object
	 */
	private String extractCountryKey(NutListDataItem regulatoryListItem) {
		String key = null;

		if (regulatoryListItem.getRegulatoryCountriesRef() != null) {
			for (NodeRef country : regulatoryListItem.getRegulatoryCountriesRef()) {
				key = (String) mlNodeService.getProperty(country, PLMModel.PROP_REGULATORY_CODE);
				if ((key != null) && !key.isBlank()) {
					break;
				}
			}
		}
		return RegulationFormulationHelper.toRegulationKey(key);
	}



	/** {@inheritDoc} */
	@Override
	protected boolean shouldMerge(NutListDataItem item, NutListDataItem sl) {
		return item.getCharactNodeRef().equals(sl.getCharactNodeRef())
				&& (((item.getRequirementType() != null) && item.getRequirementType().equals(sl.getRequirementType()))
						|| ((item.getRequirementType() == null) && (sl.getRequirementType() == null)));
	}

}
