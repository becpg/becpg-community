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

	/** {@inheritDoc} */
	@Override
	protected void provideAdditionalValues(ProductData rootProduct, ProductData formulatedProduct, SimpleCharactDataItem simpleCharact, String unit,
			Double qtyUsed, Double netQty, CharactDetailsValue currentCharactDetailsValue) {
		IngListDataItem ingListDataItem = (IngListDataItem) simpleCharact;

		Double qtyPerc = ingListDataItem.getQtyPerc();

		// Calculate qty with yield based on root product's yield applied to the base qtyPerc
		Double rootYield = rootProduct.getYield();
		if ((rootYield != null) && (rootYield != 0d) && (qtyPerc != null)) {
			Double qtyPercWithYield = qtyPerc / (rootYield / 100d);

			CharactDetailAdditionalValue qtyWithYieldValue = new CharactDetailAdditionalValue("bcpg:ingListQtyPercWithYield",
					I18NUtil.getMessage("bcpg_bcpgmodel.property.bcpg_ingListQtyPercWithYield.title"),
					FormulationHelper.calculateValue(0d, qtyUsed, qtyPercWithYield, netQty), unit);
			currentCharactDetailsValue.getAdditionalValues().add(qtyWithYieldValue);
		}

		// Calculate qty with secondary yield based on root product's secondary yield
		Double rootSecondaryYield = rootProduct.getSecondaryYield();
		if ((rootSecondaryYield != null) && (rootSecondaryYield != 0d) && (qtyPerc != null)) {
			// Apply both yields if primary yield exists, otherwise just secondary yield
			Double baseValue = qtyPerc;
			if ((rootYield != null) && (rootYield != 0d)) {
				baseValue = qtyPerc / (rootYield / 100d);
			}
			Double qtyPercWithSecondaryYield = baseValue / (rootSecondaryYield / 100d);
			CharactDetailAdditionalValue qtyWithSecondaryYieldValue = new CharactDetailAdditionalValue("bcpg:ingListQtyPercWithSecondaryYield",
					I18NUtil.getMessage("bcpg_bcpgmodel.property.bcpg_ingListQtyPercWithSecondaryYield.title"),
					FormulationHelper.calculateValue(0d, qtyUsed, qtyPercWithSecondaryYield, netQty), unit);
			currentCharactDetailsValue.getAdditionalValues().add(qtyWithSecondaryYieldValue);
		}

		// Array of percentage values
		Double[] qtyPercValues = { ingListDataItem.getQtyPerc1(), ingListDataItem.getQtyPerc2(), ingListDataItem.getQtyPerc3(),
				ingListDataItem.getQtyPerc4(), ingListDataItem.getQtyPerc5() };

		// Loop through percentage values
		for (int i = 0; i < qtyPercValues.length; i++) {
			Double qtyPercPerValue = qtyPercValues[i];
			if (qtyPercPerValue != null) {
				String titleKey = String.format("bcpg_bcpgmodel.property.bcpg_ingListQtyPerc%d.title", i + 1);
				CharactDetailAdditionalValue additionalValue = new CharactDetailAdditionalValue("bcpg:ingListQtyPerc" + (i + 1), I18NUtil.getMessage(titleKey),
						FormulationHelper.calculateValue(0d, qtyUsed, qtyPercPerValue, netQty), unit);
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
