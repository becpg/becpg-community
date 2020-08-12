package fr.becpg.repo.form.column.decorator;

import org.alfresco.repo.forms.Item;
import org.alfresco.service.cmr.repository.NodeRef;

public interface ColumnDecorator {
	
	public boolean match(Item item);
	
	public DataGridFormFieldTitleProvider createTitleResolver(NodeRef entityNodeRef, Item item);
}
