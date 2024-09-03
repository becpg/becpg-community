package fr.becpg.repo.product.formulation.details;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;

public class PhysicoChemCharactDetailsVisitor extends SimpleCharactDetailsVisitor {

	@Override
	protected boolean shouldFormulateInVolume(CharactDetailsVisitorContext context, ProductData partProduct, SimpleCharactDataItem simpleCharact) {
		if (FormulationHelper.isCharactFormulatedFromVol(nodeService, simpleCharact)) {
			return true;
		}
		return false;
	}
	
	@Override
	protected boolean shouldForceWeight(CharactDetailsVisitorContext context, ProductData partProduct, SimpleCharactDataItem simpleCharact) {
		if (FormulationHelper.isCharactFormulatedFromVol(nodeService, simpleCharact)) {
			return true;
		}
		return super.shouldForceWeight(context, partProduct, simpleCharact);
	}
	
}
