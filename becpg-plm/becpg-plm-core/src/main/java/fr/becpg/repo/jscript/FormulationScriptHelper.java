package fr.becpg.repo.jscript;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.cmr.repository.NodeService;

import fr.becpg.repo.product.formulation.FormulationHelper;

/**
 * <p>FormulationScriptHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class FormulationScriptHelper extends BaseScopableProcessorExtension {


	private NodeService nodeService;
	
	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	/**
	 * <p>getNetWeight.</p>
	 *
	 * @param entity a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @return a {@link java.lang.Double} object
	 */
	public Double getNetWeight(final ScriptNode entity) {
		return FormulationHelper.getNetWeight(entity.getNodeRef(), nodeService,
				FormulationHelper.DEFAULT_NET_WEIGHT);
	}

}
