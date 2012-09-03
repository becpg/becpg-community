package fr.becpg.repo.entity.policy;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

public class EffectivityAspectPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnAddAspectPolicy {

	private static Log logger = LogFactory.getLog(EffectivityAspectPolicy.class);
	
	private EffectivityAspectCopyBehaviourCallback effectivityAspectCopyBehaviourCallback;
	private DictionaryService dictionaryService;
	
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
		effectivityAspectCopyBehaviourCallback = new EffectivityAspectCopyBehaviourCallback(dictionaryService);
	}


	@Override
	public void doInit() {
		
		policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnAddAspectPolicy.QNAME,
                BeCPGModel.ASPECT_EFFECTIVITY,
                new JavaBehaviour(this, "onAddAspect"));

	}
	

	@Override
	public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName) {
		if(!dictionaryService.isSubClass(nodeService.getType(nodeRef), BeCPGModel.TYPE_ENTITYLIST_ITEM)){
			nodeService.setProperty(nodeRef, BeCPGModel.PROP_START_EFFECTIVITY, new Date());
		}
	}
	
    public CopyBehaviourCallback onCopyNode(QName classRef, CopyDetails copyDetails)
    {
        return effectivityAspectCopyBehaviourCallback;   
    }

    
    private static class EffectivityAspectCopyBehaviourCallback extends DefaultCopyBehaviourCallback
    {
    	
        
    	private DictionaryService dictionaryService;
    	
        public EffectivityAspectCopyBehaviourCallback(DictionaryService dictionaryService) {
			this.dictionaryService = dictionaryService;
		}

		/**
         * Don't copy certain auditable p
         */
        @Override
        public Map<QName, Serializable> getCopyProperties(
                QName classQName, CopyDetails copyDetails, Map<QName, Serializable> properties)
        {
        	
        	
            if(classQName.equals(BeCPGModel.ASPECT_EFFECTIVITY))
            {
            	if(!dictionaryService.isSubClass(copyDetails.getSourceNodeTypeQName(), BeCPGModel.TYPE_ENTITYLIST_ITEM)){
            		// Have the key properties reset by the aspect
            		properties.remove(BeCPGModel.PROP_START_EFFECTIVITY);
            	} else {
            		logger.error("Subtype of :"+copyDetails.getSourceNodeTypeQName());
            	}
          
            }
            
            return properties;
        }
        
        /**
         * Do copy the aspects
         * 
         * @return          Returns <tt>true</tt> always
         */
        @Override
        public boolean getMustCopy(QName classQName, CopyDetails copyDetails)
        {
            return true;
        }
    }


	
	
}
