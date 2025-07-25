package fr.becpg.repo.product.requirement;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.NutListDataItem;

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
				return listDataItem.valuePerServing(countryKey);
			case LabeledAsPrepared:
				return listDataItem.preparedValue(countryKey);
			case LabeledGdaPerc:
				return listDataItem.gdaPerc(countryKey);
			case LabeledValue:
				return listDataItem.value(countryKey);
			case LabeledAsPreparedServing,AsPreparedServing:
				return listDataItem.preparedValuePerServing(countryKey);
			default:
				break;
			}
		}

		return listDataItem.getValue();
	}

	private String extractCountryKey(NutListDataItem regulatoryListItem) {
		String key = "EU";

		if (regulatoryListItem.getRegulatoryCountriesRef() != null) {
			for (NodeRef country : regulatoryListItem.getRegulatoryCountriesRef()) {
				key = (String) mlNodeService.getProperty(country, PLMModel.PROP_REGULATORY_CODE);
				if ((key != null) && !key.isBlank()) {
					break;
				}
			}
		}
		return key;
	}

	/** {@inheritDoc} */
	@Override
	protected boolean shouldMerge(NutListDataItem item, NutListDataItem sl) {
		return item.getCharactNodeRef().equals(sl.getCharactNodeRef())
				&& (((item.getRequirementType() != null) && item.getRequirementType().equals(sl.getRequirementType()))
						|| ((item.getRequirementType() == null) && (sl.getRequirementType() == null)));
	}

}
