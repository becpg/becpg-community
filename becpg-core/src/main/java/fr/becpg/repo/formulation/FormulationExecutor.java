package fr.becpg.repo.formulation;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.formulation.FormulationPlugin.FormulationPluginPriority;

/**
 * 
 * @author matthieu
 *
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
	
	
		
	public FormulationExecutorState execute(NodeRef  entityNodeRef , boolean async) throws FormulateException {
		FormulationPlugin plugin = retrievePlugin(entityNodeRef);
		if(plugin !=null ) {
			plugin.runFormulation(entityNodeRef);
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
