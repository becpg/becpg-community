package fr.becpg.repo.product.formulation;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.ToxListDataItem;
import fr.becpg.repo.toxicology.ToxicologyService;

/**
 * <p>ToxFormulationHandler class.</p>
 *
 * @author matthieu
 */
public class ToxFormulationHandler extends FormulationBaseHandler<ProductData> {

	private ToxicologyService toxicologyService;
	
	/**
	 * <p>Setter for the field <code>toxicologyService</code>.</p>
	 *
	 * @param toxicologyService a {@link fr.becpg.repo.toxicology.ToxicologyService} object
	 */
	public void setToxicologyService(ToxicologyService toxicologyService) {
		this.toxicologyService = toxicologyService;
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean process(ProductData formulatedProduct) {
		if (formulatedProduct.getToxList() != null && formulatedProduct.getIngList() != null) {
			for (ToxListDataItem toxListDataItem : formulatedProduct.getToxList()) {
				Double toxMaxQuantity = null;
				for (IngListDataItem ingListDataItem : formulatedProduct.getIngList()) {
					Double qtyPerc = ingListDataItem.getQtyPerc();
					if (qtyPerc != null && qtyPerc != 0d) {
						Double maxQuantity = toxicologyService.computeMaxValue(ingListDataItem.getIng(), toxListDataItem.getTox());
						if (maxQuantity != null) {
							double currentMax = maxQuantity * 100 / qtyPerc;
							if (toxMaxQuantity == null || currentMax < toxMaxQuantity) {
								toxMaxQuantity = currentMax;
							}
						}
					}
				}
				toxListDataItem.setValue(toxMaxQuantity);
				if (!toxListDataItem.getAspects().contains(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM)) {
					toxListDataItem.getAspects().add(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM);
				}
			}
		}
		
		return true;
	}
	
}
