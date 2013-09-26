package fr.becpg.repo.product.data.spel;

import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;

/**
 * 
 * @author matthieu
 *
 */
public class DeclarationFilterContext {

	CompoListDataItem compoListDataItem;
	IngListDataItem ingListDataItem;

	public DeclarationFilterContext(CompoListDataItem compoListDataItem, IngListDataItem ingListDataItem) {
		super();
		this.compoListDataItem = compoListDataItem;
		this.ingListDataItem = ingListDataItem;
	}

	public DeclarationFilterContext() {
		super();
	}

	public CompoListDataItem getCompoListDataItem() {
		return compoListDataItem;
	}

	public IngListDataItem getIngListDataItem() {
		return ingListDataItem;
	}

}
