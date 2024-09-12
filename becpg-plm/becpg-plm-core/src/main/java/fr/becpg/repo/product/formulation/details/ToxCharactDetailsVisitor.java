package fr.becpg.repo.product.formulation.details;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.stereotype.Service;

import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.data.CharactDetails;
import fr.becpg.repo.product.data.CharactDetailsValue;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.ToxListDataItem;
import fr.becpg.repo.toxicology.ToxicologyService;

@Service
public class ToxCharactDetailsVisitor extends SimpleCharactDetailsVisitor {

	private ToxicologyService toxicologyService;
	
	public void setToxicologyService(ToxicologyService toxicologyService) {
		this.toxicologyService = toxicologyService;
	}
	
	@Override
	public CharactDetails visit(ProductData productData, List<NodeRef> dataListItems, Integer maxLevel) throws FormulateException {
		
		CharactDetails ret = createCharactDetails(dataListItems);
		ret.setTotalOperation("MIN");
		for (ToxListDataItem toxListDataItem : productData.getToxList()) {
			if (ret.hasElement(toxListDataItem.getTox())) {
				for (IngListDataItem ingListDataItem : productData.getIngList()) {
					if (ingListDataItem.getQtyPerc() != null && ingListDataItem.getQtyPerc() != 0d) {
						Double maxValue = toxicologyService.computeMaxValue(ingListDataItem.getIng(), toxListDataItem.getTox());
						if (maxValue != null) {
							Double ingMaxValue = maxValue * 100 / ingListDataItem.getQtyPerc();
							CharactDetailsValue charactDetailValue = new CharactDetailsValue(productData.getNodeRef(), ingListDataItem.getIng(), ingListDataItem.getNodeRef(), ingMaxValue, 0, "%");
							ret.addKeyValue(toxListDataItem.getTox(), charactDetailValue);
						}
					}
				}
			}
		}
		
		return ret;
	}

}
