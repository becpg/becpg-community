/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.designer.policy;

import java.io.InputStream;
import java.io.OutputStream;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.DesignerModel;
import fr.becpg.repo.designer.DesignerService;

// TODO: Auto-generated Javadoc
/**
 * Generate xml on model change 
 * create model on aspect add
 *
 * @author matthieu
 */
public class ModelAspectPolicy extends TransactionListenerAdapter implements
		NodeServicePolicies.OnAddAspectPolicy,
		NodeServicePolicies.OnUpdateNodePolicy{

	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ModelAspectPolicy.class);

	/** The policy component. */
	private PolicyComponent policyComponent;
	

	/** The designer service. */
	private DesignerService designerService;	
	
	
	/** The content service **/
	private ContentService contentService;
	
	/** The node service. */
	private NodeService nodeService;
	
	/**
	 * Sets the policy component.
	 *
	 * @param policyComponent the new policy component
	 */
	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	
	
	
	public void setDesignerService(DesignerService designerService) {
		this.designerService = designerService;
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
	 * Inits the.
	 */
	public void init() {				
		
		logger.debug("Init ModelAspectPolicy...");
		policyComponent.bindClassBehaviour(
				NodeServicePolicies.OnUpdateNodePolicy.QNAME,
				ContentModel.TYPE_DICTIONARY_MODEL, new JavaBehaviour(this,
						"onUpdateNode", NotificationFrequency.TRANSACTION_COMMIT));
		
		policyComponent.bindAssociationBehaviour(
				NodeServicePolicies.OnAddAspectPolicy.QNAME,
				DesignerModel.ASPECT_MODEL, new JavaBehaviour(this,
						"onAddAspect", NotificationFrequency.TRANSACTION_COMMIT));
		
		
	}

	
	@Override
	public void onUpdateNode(NodeRef dictionnaryModelNode) {		
		if(nodeService.hasAspect(dictionnaryModelNode, DesignerModel.ASPECT_MODEL)){
			//TODO test if is a model change or a content change
			ContentWriter writer = contentService.getWriter(dictionnaryModelNode, ContentModel.PROP_CONTENT,true);
			OutputStream out = null ;
			InputStream in = null;
			try {
				out = writer.getContentOutputStream();
				in  = designerService.getXmlFromModelAspectNode( dictionnaryModelNode);
				IOUtils.copy(in, out);
			} catch (Exception e){
				logger.error(e,e);
			}
			finally {
				if(out!=null){
					try {
						out.close();
						in.close();
					} catch (Exception e) {
						//Cannot do nothing here
					}
				}
			}
		}
		
	}




	@Override
	public void onAddAspect(NodeRef dictionnaryModelNode, QName aspect) {
		if(ContentModel.TYPE_DICTIONARY_MODEL.equals(nodeService.getType(dictionnaryModelNode))){			
			ContentReader reader = contentService.getReader(dictionnaryModelNode, ContentModel.PROP_CONTENT);
			InputStream in = null ;
			try {
				in = reader.getContentInputStream();
				designerService.createModelAspectNode( dictionnaryModelNode, in );
			} finally {
				if(in!=null){
					try {
						in.close();
					} catch (Exception e) {
						//Cannot do nothing here
					}
				}
			}
		
		} else {
			logger.warn("Type doesn't accept  aspect:"+aspect);
		}
		
		
	}
	

}
