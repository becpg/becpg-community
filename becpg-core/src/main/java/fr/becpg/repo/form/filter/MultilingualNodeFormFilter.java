package fr.becpg.repo.form.filter;

import org.alfresco.repo.forms.FormData;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>{@link AbstractMultilingualFormFilter} on the <b>update</b> path
 * ({@code POST api/node/{store}/{id}/formprocessor}).</p>
 *
 * <p>The item is the node being written, so the incoming translations are merged into the value it
 * already holds: a language the caller did not send survives the save.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class MultilingualNodeFormFilter extends AbstractMultilingualFormFilter<NodeRef> {

	/** {@inheritDoc} */
	@Override
	public void beforePersist(NodeRef item, FormData data) {
		convertMlTextFields(data, item);
	}

}
