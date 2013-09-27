/*
 * 
 */
package fr.becpg.repo.product.data.ing;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;


@AlfType
@AlfQname(qname="bcpg:ing")
public class IngItem extends AbstractLabelingComponent {	

	private IngTypeItem ingType;

	@AlfProp
	@AlfQname(qname="bcpg:ingTypeV2")
	public IngTypeItem getIngType() {
		return ingType;
	}

	public void setIngType(IngTypeItem ingType) {
		this.ingType = ingType;
	}


	
	
}
