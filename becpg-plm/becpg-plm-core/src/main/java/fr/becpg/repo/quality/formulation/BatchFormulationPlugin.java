package fr.becpg.repo.quality.formulation;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.QualityModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.formulation.FormulationPlugin;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.repository.L2CacheSupport;

/**
 * <p>BatchFormulationPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class BatchFormulationPlugin implements FormulationPlugin {

	@Autowired
	private FormulationService<ProductData> formulationService;

	@Autowired
	private BehaviourFilter policyBehaviourFilter;

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	
	
	/** {@inheritDoc} */
	@Override
	public FormulationPluginPriority getMatchPriority(QName type) {
		return entityDictionaryService.isSubClass(type, QualityModel.TYPE_BATCH) ? FormulationPluginPriority.NORMAL : FormulationPluginPriority.NONE;

	}

	/** {@inheritDoc} */
	@Override
	public void runFormulation(NodeRef entityNodeRef, String chainId) {
		try {
			policyBehaviourFilter.disableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
			policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
			policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);

			L2CacheSupport.doInCacheContext(() -> 
				AuthenticationUtil.runAsSystem(() -> {
					
						formulationService.formulate(entityNodeRef, chainId);
				
					return true;
				}), false, true);

		} finally {
			policyBehaviourFilter.enableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
			policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
			policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
		}
		
	}

}
