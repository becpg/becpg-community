package fr.becpg.repo.product.formulation.details;

import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.repo.product.data.CharactDetailAdditionalValue;
import fr.becpg.repo.product.data.CharactDetailsValue;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;

/**
 * <p>LabelClaimCharactDetailsVisitor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class LabelClaimCharactDetailsVisitor extends SimpleCharactDetailsVisitor {

	/** {@inheritDoc} */
	@Override
	protected String provideUnit(CharactDetailsVisitorContext context, SimpleCharactDataItem simpleCharact) {
		return "%";
	}
	
	/** {@inheritDoc} */
	@Override
	protected Double extractValue(ProductData formulatedProduct, ProductData partProduct, SimpleCharactDataItem simpleCharact) {
		
		LabelClaimListDataItem labelClaimItem = (LabelClaimListDataItem) simpleCharact;
		
		Double percentClaim = labelClaimItem.getPercentClaim();
		
		switch (labelClaimItem.getLabelClaimValue()) {
		case LabelClaimListDataItem.VALUE_TRUE, LabelClaimListDataItem.VALUE_CERTIFIED:

			if (percentClaim == null) {
				percentClaim = 100d;
			}

			break;
		case LabelClaimListDataItem.VALUE_NA:
			percentClaim = 0d;

			break;
		case LabelClaimListDataItem.VALUE_SUITABLE:

			if (percentClaim == null) {
				percentClaim = 100d;
			}

			break;
		case LabelClaimListDataItem.VALUE_FALSE:
			percentClaim = 0d;

			break;
		case LabelClaimListDataItem.VALUE_EMPTY:
		default:
			percentClaim = 0d;
			break;
		}
	
		return percentClaim;
		
	}
	
	/** {@inheritDoc} */
	@Override
	protected void provideAdditionalValues(ProductData rootProduct, ProductData formulatedProduct, SimpleCharactDataItem simpleCharact, String unit, Double qtyUsed, Double netQty, CharactDetailsValue currentCharactDetailsValue) {
		
		LabelClaimListDataItem labelClaimItem = (LabelClaimListDataItem) simpleCharact;
		
		Double percentApplicable = labelClaimItem.getPercentApplicable();
		
		if (percentApplicable == null) {
			if (LabelClaimListDataItem.VALUE_NA.equals(labelClaimItem.getLabelClaimValue())) {
				percentApplicable = 0d;
			} else {
				percentApplicable = 100d;
			}
		}
		
		CharactDetailAdditionalValue additionalValue = new CharactDetailAdditionalValue(I18NUtil.getMessage("bcpg_bcpgmodel.property.bcpg_lclPercentApplicable.title"),
				FormulationHelper.calculateValue(0d, qtyUsed, percentApplicable, netQty), unit);
		currentCharactDetailsValue.getAdditionalValues().add(additionalValue);
	}
	
	/** {@inheritDoc} */
	@Override
	protected boolean applyYield() {
		return false;
	}
}
