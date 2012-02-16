/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.entity.policy;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;

// TODO: Auto-generated Javadoc
/**
 * Store in the name of the product list folder the type of product list.
 *
 * @author querephi
 */
public class InitEntityListPolicy implements NodeServicePolicies.OnUpdatePropertiesPolicy {
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(InitEntityListPolicy.class);
	
	/** The policy component. */
	private PolicyComponent policyComponent;		
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The namespace service. */
	private NamespaceService namespaceService;
	
	/** The dictionary service. */
	private DictionaryService dictionaryService;
			
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
	
	/**
	 * Sets the namespace service.
	 *
	 * @param namespaceService the new namespace service
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}
	
	/**
	 * Sets the dictionary service.
	 *
	 * @param dictionaryService the new dictionary service
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}
	
	/**
	 * Inits the.
	 */
	public void init(){
		logger.debug("Init ProductListPolicies...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, DataListModel.TYPE_DATALIST, new JavaBehaviour(this, "onUpdateProperties"));
	}

	/**
	 * Initialize the name of the datalist with the local name if it is a entityDataList.
	 *
	 * @param dataListNodeRef the data list node ref
	 * @param before the before
	 * @param after the after
	 */
	@Override
	public void onUpdateProperties(NodeRef dataListNodeRef, Map<QName, Serializable> before,
									Map<QName, Serializable> after) {
		
		
		if(after.containsKey(DataListModel.PROP_DATALISTITEMTYPE)){
			
			String beforeDLType = (String)before.get(DataListModel.PROP_DATALISTITEMTYPE);
			String afterDLType = (String)after.get(DataListModel.PROP_DATALISTITEMTYPE);
			
			if(afterDLType != null && !afterDLType.isEmpty() && !afterDLType.equals(beforeDLType)){
								
				QName dataListTypeQName = QName.createQName(afterDLType, namespaceService);
				if(dictionaryService.isSubClass(dataListTypeQName, BeCPGModel.TYPE_ENTITYLIST_ITEM)){
					
					String dataListName = (String)nodeService.getProperty(dataListNodeRef, ContentModel.PROP_NAME);
					if(!dataListTypeQName.getLocalName().equals(dataListName)){
					
						nodeService.setProperty(dataListNodeRef, ContentModel.PROP_NAME, dataListTypeQName.getLocalName());
					}				
				}
			}
		}		
	}	

}
