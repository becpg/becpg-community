package fr.becpg.repo.product.requirement;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.alfresco.service.cmr.repository.MLText;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.ToxListDataItem;

/**
 * <p>ToxRequirementScanner class.</p>
 */
public class ToxRequirementScanner extends SimpleListRequirementScanner<ToxListDataItem> {

	public static final String MESSAGE_TOX_NOT_IN_RANGE = "message.formulate.tox.notInRangeValue";

	public static final String MESSAGE_TOX_INFO = "message.formulate.info.tox.notInRangeValue";

	@Override
	protected List<ToxListDataItem> getDataListVisited(ProductData productData) {
		return productData.getToxList() != null ? productData.getToxList() : new ArrayList<>();
	}

	@Override
	protected Double getValue(ToxListDataItem specDataItem, ToxListDataItem listDataItem) {
		return listDataItem.getValue();
	}

	@Override
	protected String getSpecErrorMessageKey(ToxListDataItem specDataItem) {
		return MESSAGE_TOX_NOT_IN_RANGE;
	}

	@Override
	protected String getSpecInfoMessageKey(ToxListDataItem specDataItem) {
		return MESSAGE_TOX_INFO;
	}

	@Override
	protected Double getMaxi(ToxListDataItem specDataItem, ToxListDataItem listDataItem) {
		Double maxi = super.getMaxi(specDataItem, listDataItem);
		return maxi != null ? maxi : 100.0;
	}

	@Override
	protected boolean isAuthorizedModeSupported() {
		return true;
	}

	@Override
	protected boolean isNullValueAllowed() {
		return true;
	}

	@Override
	protected MLText getNotAuthorizedMessage(ToxListDataItem listDataItem) {
		return MLTextHelper.getI18NMessage(
				"message.formulate.tox.notAuthorized",
				mlNodeService.getProperty(listDataItem.getCharactNodeRef(), BeCPGModel.PROP_CHARACT_NAME)
		);
	}

	@Override
	protected Object getDisplayedValue(Double value, Locale locale) {
		return value != null ? NumberFormat.getInstance(locale).format(value) + "%" : "";
	}

}
