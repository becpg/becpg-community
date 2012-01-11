package fr.becpg.repo.designer.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import fr.becpg.repo.designer.DesignerModel;
import fr.becpg.repo.designer.DesignerService;
import fr.becpg.repo.designer.data.DesignerTree;
import fr.becpg.repo.designer.data.FormControl;

public class DesignerServiceImpl implements DesignerService {
	
	 
	private NodeService nodeService;
	
	
	/** The content service **/
	private ContentService contentService;
	
	private DictionaryService dictionaryService;
	
	private MetaModelVisitor metaModelVisitor;
	
	private FormModelVisitor formModelVisitor;
	
	private DesignerTreeVisitor designerTreeVisitor;
	
	/**
	 * Path where config files are stored when published
	 */
	private String configPath;
	
	//Controls cache
	private List<FormControl> controls = new ArrayList<FormControl>();
	

	private static Log logger = LogFactory.getLog(DesignerServiceImpl.class);
	
	
	/**
	 * @param dictionaryService the dictionaryService to set
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	/**
	 * @param configPath the configPath to set
	 */
	public void setConfigPath(String configPath) {
		this.configPath = configPath;
	}

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
	
	/**
	 * @param formModelVisitor the formModelVisitor to set
	 */
	public void setFormModelVisitor(FormModelVisitor formModelVisitor) {
		this.formModelVisitor = formModelVisitor;
	}


	/**
	 * @param designerTreeVisitor the designerTreeVisitor to set
	 */
	public void setDesignerTreeVisitor(DesignerTreeVisitor designerTreeVisitor) {
		this.designerTreeVisitor = designerTreeVisitor;
	}

	public void init(){
		logger.debug("Init DesignerServiceImpl");
		InputStream in = null ;
		try {
			
			try {
				in = getControlsTemplate();
			} catch (IOException e) {
				logger.error(e,e);
			}
			if(in!=null){
				controls = formModelVisitor.visitControls(in);
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
	public void writeXml(NodeRef nodeRef) {

		ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
		OutputStream out = null;
		try {
			out = writer.getContentOutputStream();
			if (nodeService.hasAspect(nodeRef, DesignerModel.ASPECT_MODEL)) {
				logger.debug("Write model XML");
				NodeRef modelNodeRef = findModelNodeRef(nodeRef);

				if (modelNodeRef != null) {
					metaModelVisitor.visitModelXml(modelNodeRef, out);

				}
			} else if (nodeService.hasAspect(nodeRef, DesignerModel.ASPECT_CONFIG)){
				logger.debug("Write config XML");
				NodeRef configNodeRef = findConfigNodeRef(nodeRef);
				formModelVisitor.visitConfigXml(configNodeRef,out);
				
			}
		} catch (Exception e) {
			logger.error(e, e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
					// Cannot do nothing here
				}
			}
		}

	}
	
	@Override
	public void publish(NodeRef nodeRef) {
		if (nodeService.hasAspect(nodeRef, DesignerModel.ASPECT_MODEL)) {
			logger.debug("Publish model");
			nodeService.setProperty(nodeRef, ContentModel.PROP_MODEL_ACTIVE, true);
		} else if(nodeService.hasAspect(nodeRef, DesignerModel.ASPECT_CONFIG)){
			String name = (String) nodeService.getProperty(nodeRef,ContentModel.PROP_NAME);
			String path = configPath+System.getProperty("file.separator")+name;
			logger.debug("Publish config under "+path);
			ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
			
			InputStream in = null ;
			OutputStream out = null;
			try {
				File file = new File(path);
				if(!file.exists()){
					file.createNewFile();
				}
				out = new FileOutputStream(file);
				in = reader.getContentInputStream();
				IOUtils.copy(in, out);
			}  catch (Exception e) {
				logger.error(e,e);
			}	finally {
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(out);
			}
		}
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

	public NodeRef findConfigNodeRef(NodeRef nodeRef) {

		for (ChildAssociationRef assoc : nodeService.getChildAssocs(nodeRef)) {
			if (assoc.getTypeQName().equals(DesignerModel.ASSOC_DSG_CONFIG)) {
				return assoc.getChildRef();
			}
		}
		return null;
	}

	@Override
	public DesignerTree getDesignerTree(NodeRef nodeRef) {
		 NodeRef treeNodeRef = null;	
		 if(nodeService.hasAspect(nodeRef, DesignerModel.ASPECT_MODEL)){
			  treeNodeRef = findModelNodeRef(nodeRef);	
			  if(logger.isWarnEnabled() && treeNodeRef==null){
				  logger.warn("No assoc model found for this nodeRef");
			  }
		 }else if(nodeService.hasAspect(nodeRef, DesignerModel.ASPECT_CONFIG)){
			  treeNodeRef = findConfigNodeRef(nodeRef);	
			  if(logger.isWarnEnabled() && treeNodeRef==null){
				  logger.warn("No assoc config found for this nodeRef");
			  }
		 } else if (nodeService.getType(nodeRef).getNamespaceURI().equals(DesignerModel.M2_URI)
				 || nodeService.getType(nodeRef).getNamespaceURI().equals(DesignerModel.DESIGNER_URI)){ 
			 treeNodeRef  = nodeRef;
		 } else {
				logger.info("Node has not mandatory aspect : model aspect. Creating ...");
		}
		if(treeNodeRef== null){
			 if(nodeService.hasAspect(nodeRef, DesignerModel.ASPECT_CONFIG)){
				 treeNodeRef  = createConfigAspectNode(nodeRef);
			 } else {
				 treeNodeRef  = createModelAspectNode(nodeRef);
			 }
		}
		
		if(treeNodeRef!= null){
			return designerTreeVisitor.visitModelTreeNodeRef(treeNodeRef);
		}
		
		return new DesignerTree();
	}
	
	
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
	
	
	public NodeRef  createConfigAspectNode(NodeRef parentNodeRef){
		ContentReader reader = contentService.getReader(parentNodeRef, ContentModel.PROP_CONTENT);
		ChildAssociationRef childAssociationRef = nodeService.createNode(parentNodeRef, DesignerModel.ASSOC_DSG_CONFIG, DesignerModel.ASSOC_DSG_CONFIG, DesignerModel.TYPE_DSG_CONFIG);
		NodeRef configNodeRef = childAssociationRef.getChildRef();
		nodeService.setProperty(configNodeRef, DesignerModel.PROP_DSG_ID, nodeService.getProperty(parentNodeRef, ContentModel.PROP_NAME));
		InputStream in = null ;
			try {
				in = reader.getContentInputStream();
				
				formModelVisitor.visitConfigNodeRef(configNodeRef,
						in);
			} catch (Exception e) {
				logger.error(e,e);
			} finally {
				if(in!=null){
					try {
						in.close();
					} catch (Exception e) {
						//Cannot do nothing here
					}
				}
			}
			return configNodeRef;
	}

	@Override
	public NodeRef createModelElement(NodeRef parentNodeRef, QName nodeTypeQname, QName assocQname, Map<QName, Serializable> props,
			String modelTemplate) {
		 
		AssociationDefinition assocDef = dictionaryService.getAssociation(assocQname);
		if(!assocDef.isTargetMany()){
			logger.debug("Assoc is unique remove existing child");
			List<ChildAssociationRef> assocs =  nodeService.getChildAssocs(parentNodeRef);
			for(ChildAssociationRef assoc : assocs){
				if(assoc.getTypeQName().equals(assocQname)){
					nodeService.deleteNode(assoc.getChildRef());
				}
			}
		}
		
		ChildAssociationRef childAssociationRef = nodeService.createNode(parentNodeRef,
				assocQname, assocQname, nodeTypeQname);
		
		NodeRef ret = childAssociationRef.getChildRef();
		
		if(modelTemplate!=null){
			InputStream in = null ;
			try {
				String[] splitted = modelTemplate.split("_");
				
				try {
					in = getModelTemplate(splitted[0]);
				} catch (IOException e) {
					logger.error(e,e);
				}
				if(in!=null){
					
					if(nodeService.getType(parentNodeRef).getNamespaceURI().equals(DesignerModel.DESIGNER_URI)){
						formModelVisitor.visitModelTemplate(ret, nodeTypeQname,splitted[1], in);
					} else {
						metaModelVisitor.visitModelTemplate(ret, nodeTypeQname,splitted[1], in);
					}
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
	
	private InputStream getControlsTemplate() throws IOException {
		Resource resource = new ClassPathResource("beCPG/designer/formControls.xml");
		if(resource.exists()){
			return resource.getInputStream();
		}
			logger.warn("No controls template for ");
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

	@Override
	public List<FormControl> getFormControls() {
		return controls;
	}

	/**
	 * Handle move of properties 
	 * from type to aspect
	 * from aspect to type
	 * from type or aspect to form or set --> create field
	 * move of field
	 * from set to form
	 * from form to set
	 * move of type
	 * from model to config -->  create form
	 */
	@Override
	public NodeRef moveElement(NodeRef from, NodeRef to) {

		NodeRef ret = null;
		logger.debug("Try to move node from type :"+nodeService.getType(from));
		if(DesignerModel.TYPE_M2_PROPERTY.equals(nodeService.getType(from))){
			logger.debug("Node is a property");
			if(DesignerModel.TYPE_M2_TYPE.equals(nodeService.getType(to))
					|| DesignerModel.TYPE_M2_ASPECT.equals(nodeService.getType(to))){
				logger.debug("Move to type or aspect");
				ChildAssociationRef assocRef = nodeService.moveNode(from, to, DesignerModel.ASSOC_M2_PROPERTIES,  DesignerModel.ASSOC_M2_PROPERTIES);
				ret = assocRef.getChildRef();
			}
			if(DesignerModel.TYPE_DSG_FORM.equals(nodeService.getType(to))
					|| DesignerModel.TYPE_DSG_FORMSET.equals(nodeService.getType(to))){
				logger.debug("Create field");
				
				ChildAssociationRef assocRef = 	nodeService.createNode(to, DesignerModel.ASSOC_DSG_FIELDS,  DesignerModel.ASSOC_DSG_FIELDS,  DesignerModel.TYPE_DSG_FORMFIELD);
				ret = assocRef.getChildRef();
				
				//Copy prop name to field ID
				nodeService.setProperty(ret, DesignerModel.PROP_DSG_ID, nodeService.getProperty(from, DesignerModel.PROP_M2_NAME));
				
			}
		} else if(DesignerModel.TYPE_DSG_FORMFIELD.equals(nodeService.getType(from))){
			if(DesignerModel.TYPE_DSG_FORM.equals(nodeService.getType(to))
					|| DesignerModel.TYPE_DSG_FORMSET.equals(nodeService.getType(to))){
				logger.debug("Move field");
				ChildAssociationRef assocRef = nodeService.moveNode(from, to, DesignerModel.ASSOC_DSG_FIELDS,  DesignerModel.ASSOC_DSG_FIELDS);
				ret = assocRef.getChildRef();
			
			}
		} else if(DesignerModel.TYPE_M2_TYPE.equals(nodeService.getType(from))){
			if(DesignerModel.TYPE_DSG_CONFIG.equals(nodeService.getType(to))){
			  ret = formModelVisitor.visitM2Type(from,to);
			  	
			}
		}
		if(ret==null){
			logger.warn("unknow type");
		}
		return ret;
	}



}
