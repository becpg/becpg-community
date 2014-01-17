package fr.becpg.repo.product.data.ing;

import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;

@AlfType
@AlfQname(qname = "bcpg:ingTypeItem")
public class IngTypeItem extends AbstractLabelingComponent{

	public static final IngTypeItem DEFAULT_GROUP = new IngTypeItem();

}
