package fr.becpg.repo.formulation;

import java.util.ArrayList;
import java.util.List;

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
	private FormulationChainPlugin[] formulationChainPlugins;
	
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
	 * @param chainId a {@link java.lang.String} object
	 */
	public FormulationExecutorState execute(NodeRef  entityNodeRef, String chainId , boolean async)  {
		FormulationPlugin plugin = retrievePlugin(entityNodeRef);
		if(plugin !=null ) {
			plugin.runFormulation(entityNodeRef, chainId);
		}
		
		return FormulationExecutorState.SUCCESS;
	}
	
	/**
	 * <p>getDisabledFormulationChainIds.</p>
	 *
	 * @param entity a {@link fr.becpg.repo.formulation.ReportableEntity} object
	 * @return a {@link java.util.List} object
	 */
	public List<String> getDisabledFormulationChainIds(ReportableEntity entity) {
		List<String> disabledChainIds = new ArrayList<>();
		if (formulationChainPlugins != null) {
			for (FormulationChainPlugin formulationChainPlugin : formulationChainPlugins) {
				if (entity.getNodeRef() != null && !formulationChainPlugin.isChainActiveOnEntity(entity.getNodeRef())) {
					disabledChainIds.add(formulationChainPlugin.getChainId());
				}
			}
		}
		return disabledChainIds;
	}
	
	/**
	 * <p>getState.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link fr.becpg.repo.formulation.FormulationExecutor.FormulationExecutorState} object
	 */
	public FormulationExecutorState getState(NodeRef entityNodeRef) {
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
