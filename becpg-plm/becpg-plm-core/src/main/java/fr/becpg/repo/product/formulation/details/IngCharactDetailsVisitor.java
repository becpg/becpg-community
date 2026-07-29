package fr.becpg.repo.product.formulation.details;

import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.repo.product.data.CharactDetailAdditionalValue;
import fr.becpg.repo.product.data.CharactDetailsValue;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;

/**
 * <p>IngCharactDetailsVisitor class.</p>
 *
 * @author matthieu
 */
public class IngCharactDetailsVisitor extends SimpleCharactDetailsVisitor {

	/** {@inheritDoc} */
	@Override
	protected boolean shouldFormulateInVolume(CharactDetailsVisitorContext context, ProductData partProduct, SimpleCharactDataItem simpleCharact) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Keeps the main quantity column on the raw recipe basis: the yields of the intermediate
	 * components are cancelled out so that children lines sum up to their parent line.
	 */
	@Override
	protected double provideQtyUsedBasisFactor(CharactDetailsVisitorContext context) {
		return context.getCumulatedYieldFactor();
	}

	/** {@inheritDoc} */
	@Override
	protected void provideAdditionalValues(CharactDetailsVisitorContext context, ProductData formulatedProduct, SimpleCharactDataItem simpleCharact,
			String unit, Double qtyUsed, Double netQty, CharactDetailsValue currentCharactDetailsValue) {
		IngListDataItem ingListDataItem = (IngListDataItem) simpleCharact;
		ProductData rootProduct = context.getRootProductData();

		Double qtyPerc = ingListDataItem.getQtyPerc();

		// Mirror IngsCalculatingFormulationHandler: the stored value carries the component yield chain, fallback on qtyPerc
		Double qtyPercWithYield = ingListDataItem.getQtyPercWithYield();
		if (qtyPercWithYield == null) {
			qtyPercWithYield = qtyPerc;
		}

		Double rootYield = rootProduct.getYield();
		if ((qtyPercWithYield != null) && (rootYield != null) && (rootYield != 0d)) {
			qtyPercWithYield = qtyPercWithYield / (rootYield / 100d);
		}

		if (qtyPercWithYield != null) {
			CharactDetailAdditionalValue qtyWithYieldValue = new CharactDetailAdditionalValue(
					I18NUtil.getMessage("bcpg_bcpgmodel.property.bcpg_ingListQtyPercWithYield.title"),
					FormulationHelper.calculateValue(0d, qtyUsed, qtyPercWithYield, netQty), unit);
			currentCharactDetailsValue.getAdditionalValues().add(qtyWithYieldValue);
		}

		// Secondary yield column only exists when the root product declares a secondary yield
		Double rootSecondaryYield = rootProduct.getSecondaryYield();
		if ((rootSecondaryYield != null) && (rootSecondaryYield != 0d) && (qtyPercWithYield != null)) {
			Double qtyPercWithSecondaryYield = qtyPercWithYield / (rootSecondaryYield / 100d);
			CharactDetailAdditionalValue qtyWithSecondaryYieldValue = new CharactDetailAdditionalValue(
					I18NUtil.getMessage("bcpg_bcpgmodel.property.bcpg_ingListQtyPercWithSecondaryYield.title"),
					FormulationHelper.calculateValue(0d, qtyUsed, qtyPercWithSecondaryYield, netQty), unit);
			currentCharactDetailsValue.getAdditionalValues().add(qtyWithSecondaryYieldValue);
		}

		// Alternate percentage columns follow the raw recipe basis of the main column
		Double rawQtyUsed = qtyUsed * context.getCumulatedYieldFactor();
		Double[] qtyPercValues = { ingListDataItem.getQtyPerc1(), ingListDataItem.getQtyPerc2(), ingListDataItem.getQtyPerc3(),
				ingListDataItem.getQtyPerc4(), ingListDataItem.getQtyPerc5() };

		for (int i = 0; i < qtyPercValues.length; i++) {
			Double qtyPercPerValue = qtyPercValues[i];
			if (qtyPercPerValue != null) {
				String titleKey = String.format("bcpg_bcpgmodel.property.bcpg_ingListQtyPerc%d.title", i + 1);
				CharactDetailAdditionalValue additionalValue = new CharactDetailAdditionalValue(I18NUtil.getMessage(titleKey),
						FormulationHelper.calculateValue(0d, rawQtyUsed, qtyPercPerValue, netQty), unit);
				currentCharactDetailsValue.getAdditionalValues().add(additionalValue);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	protected boolean applyYield() {
		return false;
	}

}
