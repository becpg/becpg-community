package fr.becpg.repo.formulation;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.formulation.FormulationPlugin.FormulationPluginPriority;

/**
 * <p>FormulationExecutor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class FormulationExecutor {

	@Autowired
	private FormulationPlugin[] plugins;
	
	@Autowired
	private NodeService nodeService;
	
	public enum FormulationExecutorState {
		SUCCESS, INPROGRESS, ERROR
	}
	
	
		
	/**
	 * <p>execute.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param async a boolean.
	 * @return a {@link fr.becpg.repo.formulation.FormulationExecutor.FormulationExecutorState} object.
	 * @throws fr.becpg.repo.formulation.FormulateException if any.
	 */
	public FormulationExecutorState execute(NodeRef  entityNodeRef, String chainId , boolean async)  {
		FormulationPlugin plugin = retrievePlugin(entityNodeRef);
		if(plugin !=null ) {
			plugin.runFormulation(entityNodeRef, chainId);
		}
		
		return FormulationExecutorState.SUCCESS;
	}
	
	
	FormulationExecutorState getState(NodeRef  entityNodeRef) {
		
		return FormulationExecutorState.INPROGRESS;
	}
	
	private FormulationPlugin retrievePlugin(NodeRef entityNodeRef ){

		FormulationPlugin ret = null;

		QName type = nodeService.getType(entityNodeRef);

		for (FormulationPlugin plugin : plugins) {
			FormulationPluginPriority priority = plugin.getMatchPriority(type);
			if (!FormulationPluginPriority.NONE.equals(priority)) {
				if ((ret == null) || priority.isHigherPriority(ret.getMatchPriority(type))) {
					ret = plugin;
				}
			}
		}

		return ret;
	}
	
	
	
}
