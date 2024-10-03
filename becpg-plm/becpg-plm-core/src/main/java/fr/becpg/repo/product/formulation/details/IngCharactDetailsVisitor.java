package fr.becpg.repo.product.formulation.details;

import fr.becpg.repo.product.data.ProductData;
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
	
}
