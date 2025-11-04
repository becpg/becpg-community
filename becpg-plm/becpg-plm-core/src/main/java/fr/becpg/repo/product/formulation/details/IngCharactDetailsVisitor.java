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
	protected void provideAdditionalValues(ProductData rootProduct, ProductData formulatedProduct, 
	                                       SimpleCharactDataItem simpleCharact, String unit, 
	                                       Double qtyUsed, Double netQty, 
	                                       CharactDetailsValue currentCharactDetailsValue) {
	    IngListDataItem ingListDataItem = (IngListDataItem) simpleCharact;

	    // Add qty with yield at final product level (Qty with yield %)
	    if (ingListDataItem.getQtyPercWithYield() != null) {
	        CharactDetailAdditionalValue qtyWithYieldValue = new CharactDetailAdditionalValue(
	            I18NUtil.getMessage("bcpg_bcpgmodel.property.bcpg_ingListQtyPercWithYield.title"),
	            FormulationHelper.calculateValue(0d, qtyUsed, ingListDataItem.getQtyPercWithYield(), netQty),
	            unit
	        );
	        currentCharactDetailsValue.getAdditionalValues().add(qtyWithYieldValue);
	    }
	    
	    if (ingListDataItem.getQtyPercWithSecondaryYield() != null) {
	        CharactDetailAdditionalValue qtyWithSecondaryYieldValue = new CharactDetailAdditionalValue(
	            I18NUtil.getMessage("bcpg_bcpgmodel.property.bcpg_ingListQtyPercWithSecondaryYield.title"),
	            FormulationHelper.calculateValue(0d, qtyUsed, ingListDataItem.getQtyPercWithSecondaryYield(), netQty),
	            unit
	        );
	        currentCharactDetailsValue.getAdditionalValues().add(qtyWithSecondaryYieldValue);
	    }

	    // Array of percentage values
	    Double[] qtyPercValues = {
	        ingListDataItem.getQtyPerc1(),
	        ingListDataItem.getQtyPerc2(),
	        ingListDataItem.getQtyPerc3(),
	        ingListDataItem.getQtyPerc4(),
	        ingListDataItem.getQtyPerc5()
	    };

	    // Loop through percentage values
	    for (int i = 0; i < qtyPercValues.length; i++) {
	        Double qtyPerc = qtyPercValues[i];
	        if (qtyPerc != null) {
	            String titleKey = String.format("bcpg_bcpgmodel.property.bcpg_ingListQtyPerc%d.title", i + 1);
	            CharactDetailAdditionalValue additionalValue = new CharactDetailAdditionalValue(
	                I18NUtil.getMessage(titleKey),
	                FormulationHelper.calculateValue(0d, qtyUsed, qtyPerc, netQty),
	                unit
	            );
	            currentCharactDetailsValue.getAdditionalValues().add(additionalValue);
	        }
	    }
	}
	
}
