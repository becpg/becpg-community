package fr.becpg.repo.product.requirement;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.alfresco.service.cmr.repository.MLText;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.SvhcListDataItem;

/**
 * <p>SvhcRequirementScanner class.</p>
 */
public class SvhcRequirementScanner extends SimpleListRequirementScanner<SvhcListDataItem> {

	public static final String MESSAGE_SVHC_NOT_IN_RANGE = "message.formulate.svhc.notInRangeValue";

	public static final String MESSAGE_SVHC_INFO = "message.formulate.info.svhc.notInRangeValue";

	@Override
	protected List<SvhcListDataItem> getDataListVisited(ProductData productData) {
		return productData.getSvhcList() != null ? productData.getSvhcList() : new ArrayList<>();
	}

	@Override
	protected Double getValue(SvhcListDataItem specDataItem, SvhcListDataItem listDataItem) {
		return listDataItem.getValue();
	}

	@Override
	protected String getSpecErrorMessageKey(SvhcListDataItem specDataItem) {
		return MESSAGE_SVHC_NOT_IN_RANGE;
	}

	@Override
	protected String getSpecInfoMessageKey(SvhcListDataItem specDataItem) {
		return MESSAGE_SVHC_INFO;
	}

	@Override
	protected Double getMaxi(SvhcListDataItem specDataItem, SvhcListDataItem listDataItem) {
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
	protected MLText getNotAuthorizedMessage(SvhcListDataItem listDataItem) {
		return MLTextHelper.getI18NMessage(
				"message.formulate.svhc.notAuthorized",
				mlNodeService.getProperty(listDataItem.getCharactNodeRef(), BeCPGModel.PROP_CHARACT_NAME)
		);
	}

	@Override
	protected Object getDisplayedValue(Double value, Locale locale) {
		return value != null ? NumberFormat.getInstance(locale).format(value) + "%" : "";
	}

}
