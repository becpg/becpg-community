package fr.becpg.repo.form;

import java.io.IOException;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONException;

import fr.becpg.common.BeCPGException;
import fr.becpg.repo.form.impl.BecpgFormDefinition;
import fr.becpg.repo.form.column.decorator.ColumnDecorator;


/**
 * <p>BecpgFormService interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface BecpgFormService {
	
	
	/**
	 * <p>getForm.</p>
	 *
	 * @param itemKind a {@link java.lang.String} object
	 * @param itemId a {@link java.lang.String} object
	 * @param formId a {@link java.lang.String} object
	 * @param siteId a {@link java.lang.String} object
	 * @param fields a {@link java.util.List} object
	 * @param forcedFields a {@link java.util.List} object
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link fr.becpg.repo.form.impl.BecpgFormDefinition} object
	 * @throws fr.becpg.common.BeCPGException if any.
	 * @throws org.json.JSONException if any.
	 */
	BecpgFormDefinition getForm(String itemKind, String itemId, String formId, String siteId, List<String> fields, List<String> forcedFields,
			NodeRef entityNodeRef) throws BeCPGException, JSONException;

	/**
	 * <p>reloadConfig.</p>
	 *
	 * @throws java.io.IOException if any.
	 */
	void reloadConfig() throws IOException;

	/**
	 * <p>registerDecorator.</p>
	 *
	 * @param columnDecorator a {@link fr.becpg.repo.form.column.decorator.ColumnDecorator} object
	 */
	void registerDecorator(ColumnDecorator columnDecorator);


	

}
