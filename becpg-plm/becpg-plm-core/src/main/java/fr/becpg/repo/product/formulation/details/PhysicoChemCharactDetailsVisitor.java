package fr.becpg.repo.product.formulation.details;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;

/**
 * <p>PhysicoChemCharactDetailsVisitor class.</p>
 *
 * @author matthieu
 */
public class PhysicoChemCharactDetailsVisitor extends SimpleCharactDetailsVisitor {

	/** {@inheritDoc} */
	@Override
	protected boolean shouldFormulateInVolume(CharactDetailsVisitorContext context, ProductData partProduct, SimpleCharactDataItem simpleCharact) {
		if (FormulationHelper.isCharactFormulatedFromVol(nodeService, simpleCharact)) {
			return true;
		}
		return false;
	}
	
	/** {@inheritDoc} */
	@Override
	protected boolean shouldForceWeight(CharactDetailsVisitorContext context, ProductData partProduct, SimpleCharactDataItem simpleCharact) {
		if (FormulationHelper.isCharactFormulatedFromVol(nodeService, simpleCharact)) {
			return true;
		}
		return super.shouldForceWeight(context, partProduct, simpleCharact);
	}
	
}
