/*
 * 
 */
package fr.becpg.repo.product.data.ing;

import java.util.ArrayList;
import java.util.List;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;


@AlfType
@AlfQname(qname="bcpg:ing")
public class IngItem extends AbstractLabelingComponent {	

	private IngTypeItem ingType;

	private List<IngItem> subIngs  = new ArrayList<>();
	
	
	@AlfProp
	@AlfQname(qname="bcpg:ingTypeV2")
	public IngTypeItem getIngType() {
		return ingType;
	}

	public void setIngType(IngTypeItem ingType) {
		this.ingType = ingType;
	}

	public List<IngItem> getSubIngs() {
		return subIngs;
	}

	public void setSubIngs(List<IngItem> subIngs) {
		this.subIngs = subIngs;
	}


}
