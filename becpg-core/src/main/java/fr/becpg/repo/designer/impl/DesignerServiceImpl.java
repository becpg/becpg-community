package fr.becpg.repo.designer.impl;

import java.io.InputStream;
import java.io.OutputStream;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
			InputStream in = null;
			try {
				out = writer.getContentOutputStream();
				 NodeRef modelNodeRef = findModelNodeRef(dictionaryModelNodeRef);	
				
				if(modelNodeRef != null){
					in  = metaModelVisitor.visitModelXml( modelNodeRef);
					IOUtils.copy(in, out);
				}
				logger.error("No assoc model found ");
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

	
	
	private NodeRef findModelNodeRef(NodeRef dictionaryModelNodeRef) {
		 NodeRef modelNodeRef = null;
		for(ChildAssociationRef assoc :  nodeService.getChildAssocs(dictionaryModelNodeRef)){
			if(assoc.getQName().equals(DesignerModel.ASSOC_MODEL)){
				modelNodeRef = assoc.getChildRef();
				break;
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

}
