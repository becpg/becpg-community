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
	protected boolean shouldForceWeight(CharactDetailsVisitorContext context, ProductData partProduct, SimpleCharactDataItem simpleCharact) {
		return true;
	}
	

	/** {@inheritDoc} */
	@Override
	protected void provideAdditionalValues(ProductData rootProduct, ProductData formulatedProduct, 
	                                       SimpleCharactDataItem simpleCharact, String unit, 
	                                       Double qtyUsed, Double netQty, 
	                                       CharactDetailsValue currentCharactDetailsValue) {
	    IngListDataItem ingListDataItem = (IngListDataItem) simpleCharact;

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
