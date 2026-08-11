package fr.becpg.repo.form.filter;

import org.alfresco.repo.forms.FormData;
import org.alfresco.service.cmr.dictionary.TypeDefinition;

/**
 * <p>{@link AbstractMultilingualFormFilter} on the <b>creation</b> path
 * ({@code POST api/type/{type}/formprocessor}).</p>
 *
 * <p>There is no node yet, so there is nothing to merge with: the submitted map <i>is</i> the
 * value. Without this half, a multilingual field filled in on a creation form would still be
 * persisted as raw JSON — and a creation is exactly where a supplier enters a legal name for the
 * first time.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class MultilingualTypeFormFilter extends AbstractMultilingualFormFilter<TypeDefinition> {

	/** {@inheritDoc} */
	@Override
	public void beforePersist(TypeDefinition item, FormData data) {
		convertMlTextFields(data, null);
	}

}
