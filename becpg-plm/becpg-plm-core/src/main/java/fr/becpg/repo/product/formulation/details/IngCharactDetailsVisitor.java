package fr.becpg.repo.product.formulation.details;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;

public class IngCharactDetailsVisitor extends SimpleCharactDetailsVisitor {

	@Override
	protected boolean shouldFormulateInVolume(CharactDetailsVisitorContext context, ProductData partProduct, SimpleCharactDataItem simpleCharact) {
		return false;
	}
	
	@Override
	protected boolean shouldForceWeight(CharactDetailsVisitorContext context, ProductData partProduct, SimpleCharactDataItem simpleCharact) {
		return true;
	}
	
}
