/*
 * 
 */
package fr.becpg.repo.eco.policy;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.common.RepoConsts;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ECOModel;
import fr.becpg.repo.eco.ECOService;
import fr.becpg.repo.eco.ECOState;
import fr.becpg.repo.entity.AutoNumService;

/**
 * The Class ECOPolicy.
 *
 * @author querephi
 */
public class ECOPolicy implements NodeServicePolicies.OnUpdatePropertiesPolicy{			
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ECOPolicy.class);
	
	/** The policy component. */
	private PolicyComponent policyComponent;	
	
	/** The node service. */
	private NodeService nodeService;
	
	private ECOService ecoService;
	
	/**
	 * Sets the policy component.
	 *
	 * @param policyComponent the new policy component
	 */
	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}	
	
	/**
	 * Sets the node service.
	 *
	 * @param nodeService the new node service
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	public void setEcoService(ECOService ecoService) {
		this.ecoService = ecoService;
	}

	/**
	 * Inits the.
	 */
	public void init(){
		logger.debug("Init ECOPolicy...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, ECOModel.TYPE_ECO, new JavaBehaviour(this, "onUpdateProperties"));
	}
	
	

	@Override
	public void onUpdateProperties(NodeRef ecoNodeRef, Map<QName, Serializable> before,
			Map<QName, Serializable> after) {
		
		logger.debug("onUpdateProperties");
		
		String beforeState = (String)before.get(ECOModel.PROP_ECO_STATE);
		String afterState = (String)after.get(ECOModel.PROP_ECO_STATE);
		
		logger.debug("onUpdateProperties, beforeState: " + beforeState + "afterState: " + afterState);
		
		if(beforeState != null && afterState != null && !beforeState.equals(afterState)){						
		
			try{
				ECOState ecoState = ECOState.valueOf(afterState);
				
				if(ecoState.equals(ECOState.ToCalculateWUsed)){
					
					logger.debug("calculate WUsed");
					ecoService.calculateWUsedList(ecoNodeRef);					
				}
				else if(ecoState.equals(ECOState.ToSimulate)){
				
					logger.debug("do simulation");
					ecoService.doSimulation(ecoNodeRef);									
				}
				else if(ecoState.equals(ECOState.ToApply)){
				
					logger.debug("apply");
					ecoService.apply(ecoNodeRef);				
				}
			}	
			catch(Exception e){
				logger.error("Failed to apply ECO policy", e);
				nodeService.setProperty(ecoNodeRef, ECOModel.PROP_ECO_STATE, beforeState);
			}
		}
		
	}	
}
