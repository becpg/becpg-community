package fr.becpg.repo.form.column.decorator;

import org.alfresco.repo.forms.Item;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>ColumnDecorator interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface ColumnDecorator {
	
	/**
	 * <p>match.</p>
	 *
	 * @param item a {@link org.alfresco.repo.forms.Item} object.
	 * @return a boolean.
	 */
	public boolean match(Item item);
	
	/**
	 * <p>createTitleResolver.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param item a {@link org.alfresco.repo.forms.Item} object.
	 * @return a {@link fr.becpg.repo.form.column.decorator.DataGridFormFieldTitleProvider} object.
	 */
	public DataGridFormFieldTitleProvider createTitleResolver(NodeRef entityNodeRef, Item item);
}
