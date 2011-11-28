package fr.becpg.repo.designer.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryModelType;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import fr.becpg.model.DesignerModel;
import fr.becpg.repo.designer.DesignerService;
import fr.becpg.repo.designer.data.ModelTree;

public class DesignerServiceImpl implements DesignerService {
	
	 
	private NodeService nodeService;
	
	
	/** The content service **/
	private ContentService contentService;
	
	
	private MetaModelVisitor metaModelVisitor;
	

	private static Log logger = LogFactory.getLog(DesignerServiceImpl.class);
	


	/**
	 * @param contentService the contentService to set
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	/**
	 * @param nodeService the nodeService to set
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * @param metaModelVisitor the metaModelVisitor to set
	 */
	public void setMetaModelVisitor(MetaModelVisitor metaModelVisitor) {
		this.metaModelVisitor = metaModelVisitor;
	}

	@Override
	public NodeRef createModelAspectNode(NodeRef parentNode, InputStream modelXml) {
		logger.debug("call createModelAspectNode");
		M2Model m2Model = M2Model.createModel(modelXml);
		
		NodeRef modelNodeRef = nodeService.createNode(parentNode, DesignerModel.ASSOC_MODEL, DesignerModel.ASSOC_MODEL, DesignerModel.TYPE_M2_MODEL).getChildRef();
		
		try {
			metaModelVisitor.visitModelNodeRef(modelNodeRef,
					m2Model);
		} catch (Exception e) {
			logger.error(e,e);
		}
		
		return modelNodeRef;
	}

	@Override
	public void writeXmlFromModelAspectNode(NodeRef dictionaryModelNodeRef) {
		if(nodeService.hasAspect(dictionaryModelNodeRef, DesignerModel.ASPECT_MODEL)){
			ContentWriter writer = contentService.getWriter(dictionaryModelNodeRef, ContentModel.PROP_CONTENT,true);
			OutputStream out = null ;
			try {
				out = writer.getContentOutputStream();
				 NodeRef modelNodeRef = findModelNodeRef(dictionaryModelNodeRef);	
				
				if(modelNodeRef != null){
					metaModelVisitor.visitModelXml( modelNodeRef,out);

				}
			} catch (Exception e){
				logger.error(e,e);
			}
			finally {
				if(out!=null){
					try {
						out.close();
					} catch (Exception e) {
						//Cannot do nothing here
					}
				}
			}
		}
	}
	
	@Override
	public void publish(NodeRef dictionaryModelNodeRef) {
		nodeService.setProperty(dictionaryModelNodeRef, ContentModel.PROP_MODEL_ACTIVE, true);
		
	}

	
	@Override
	public NodeRef findModelNodeRef(NodeRef nodeRef) {
		
		if(logger.isDebugEnabled()){
			logger.debug("Find model for nodeRef:"+nodeRef.toString());
			if(nodeRef!=null){
				logger.debug("nodeRef type:"+nodeService.getType(nodeRef).toString());
			}
		}
		
		NodeRef modelNodeRef = null;
		if(nodeService.hasAspect(nodeRef, DesignerModel.ASPECT_MODEL)){
			for(ChildAssociationRef assoc :  nodeService.getChildAssocs(nodeRef)){
				if(assoc.getQName().equals(DesignerModel.ASSOC_MODEL)){
					return assoc.getChildRef();
				}
			}
		} else {
			if(nodeService.getType(nodeRef).equals(DesignerModel.TYPE_M2_MODEL)){
				return nodeRef;
			}
			for(ChildAssociationRef assoc : nodeService.getParentAssocs(nodeRef)){
				modelNodeRef = findModelNodeRef(assoc.getParentRef());
				if(modelNodeRef!=null){
					return modelNodeRef;
				}
			}
		}
		return modelNodeRef;
	}

	@Override
	public ModelTree getModelTree(NodeRef dictionaryModelNodeRef) {
		 NodeRef modelNodeRef = null;	
		 if(nodeService.hasAspect(dictionaryModelNodeRef, DesignerModel.ASPECT_MODEL)){
			  modelNodeRef = findModelNodeRef(dictionaryModelNodeRef);	
			  if(logger.isWarnEnabled() && modelNodeRef==null){
				  logger.warn("No assoc model found for this nodeRef");
			  }
		 } else if (nodeService.getType(dictionaryModelNodeRef).getNamespaceURI().equals(DesignerModel.M2_URI)
				 || nodeService.getType(dictionaryModelNodeRef).getNamespaceURI().equals(DesignerModel.DESIGNER_URI)){ 
			 modelNodeRef  = dictionaryModelNodeRef;
		 } else {
				logger.info("Node has not mandatory aspect : model aspect. Creating ...");
		}
		if(modelNodeRef== null){
			modelNodeRef  = createModelAspectNode(dictionaryModelNodeRef);
		}
		 
		if(modelNodeRef!= null){
			return metaModelVisitor.visitModelTreeNodeRef(modelNodeRef);
		}
		
		return new ModelTree();
	}
	
	
	@Override
	public NodeRef  createModelAspectNode(NodeRef dictionaryModelNodeRef){
		if(ContentModel.TYPE_DICTIONARY_MODEL.equals(nodeService.getType(dictionaryModelNodeRef))){
			ContentReader reader = contentService.getReader(dictionaryModelNodeRef, ContentModel.PROP_CONTENT);
			InputStream in = null ;
			try {
				in = reader.getContentInputStream();
			 return createModelAspectNode( dictionaryModelNodeRef, in );
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
			logger.warn("Node is not of type : dictionnary model");
		}
		return null;
	}

	@Override
	public NodeRef createModelElement(NodeRef parentNodeRef, QName nodeTypeQname, QName assocQname, Map<QName, Serializable> props,
			String modelTemplate) {
		
		ChildAssociationRef childAssociationRef = nodeService.createNode(parentNodeRef,
				assocQname, assocQname, nodeTypeQname);
		
		NodeRef ret = childAssociationRef.getChildRef();
		
		InputStream in = null ;
		try {
			String[] splitted = modelTemplate.split("_");
			
			try {
				in = getModelTemplate(splitted[0]);
			} catch (IOException e) {
				logger.error(e,e);
			}
			if(in!=null){
				metaModelVisitor.visitModelTemplate(ret, nodeTypeQname,splitted[1], in);
			}
			
		} catch (Exception e){
			logger.error(e,e);
		} 
		finally {
			if(in!=null){
				try {
					in.close();
				} catch (Exception e) {
					//Cannot do nothing here
				}
			}
		}
		if(props!=null){
			if(logger.isDebugEnabled()){
				logger.debug("Set properties on node:"+ret.toString());
			}
			for(Entry<QName,Serializable> entry : props.entrySet()){
				nodeService.setProperty(ret, entry.getKey(),entry.getValue());
			}
			
			
		}
		
		return ret;
	}

	private InputStream getModelTemplate(String modelTemplate) throws IOException {
		if(modelTemplate!=null){
			Resource resource = new ClassPathResource("beCPG/designer/"+modelTemplate+".xml");
			if(resource.exists()){
				return resource.getInputStream();
			}
			logger.warn("No model found for :"+modelTemplate);
		}
		return null;
	}

	@Override
	public String prefixName(NodeRef elementRef, String name) {
		
		NodeRef modelNodeRef = findModelNodeRef(elementRef);
		if(modelNodeRef!=null){
			for(ChildAssociationRef assoc :  nodeService.getChildAssocs(modelNodeRef)){
				if(assoc.getQName().equals(DesignerModel.ASSOC_M2_NAMESPACES)){
					NodeRef namespaceNodeRef = assoc.getChildRef();
				    String prefix = (String) nodeService.getProperty(namespaceNodeRef,DesignerModel.PROP_M2_PREFIX);
				    if(logger.isDebugEnabled()){
				    	logger.debug("Prefix name : "+prefix+":"+name);
				    }
					return prefix+":"+name;
				}
			}
			
		} else {
			logger.warn("Cannot find model nodeRef");
		}
		
		logger.warn("Could not find any namespace");
		
		return name;
	}

}
