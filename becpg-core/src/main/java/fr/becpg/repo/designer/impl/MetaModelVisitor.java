package fr.becpg.repo.designer.impl;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gdata.data.dublincore.Date;

import fr.becpg.model.DesignerModel;
import fr.becpg.repo.designer.data.ModelTree;

public class MetaModelVisitor {

	private NodeService nodeService;
	
	private NamespaceService namespaceService;

	private static Log logger = LogFactory.getLog(MetaModelVisitor.class);

	
	
	/**
	 * @return the nodeService
	 */
	public NodeService getNodeService() {
		return nodeService;
	}


	/**
	 * @param nodeService the nodeService to set
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}


	/**
	 * @return the namespaceService
	 */
	public NamespaceService getNamespaceService() {
		return namespaceService;
	}


	/**
	 * @param namespaceService the namespaceService to set
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}


	// Here we create nodeRef
	@SuppressWarnings("unchecked")
	public void visitModelNodeRef(NodeRef modelNodeRef, Object model) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {

		List<Class<?>> classes = new ArrayList<Class<?>>();
		classes.add(model.getClass());
		classes.add(model.getClass().getSuperclass());

		for (Class<?> m2Class : classes) {
		
			logger.debug("Filling model from :" + m2Class.getSimpleName());

			for (Field field : m2Class.getDeclaredFields()) {

				if (!shouldIgnoreField(field.getName())) {
					logger.debug("Reading field :" + field.getName());

					Method getterMethod = retieveGetter(m2Class, field);

					if (getterMethod != null) {
						if (field.getType().equals(List.class) && ! getNodeTypeQName(field).getLocalName().equals("string")) {
							List<Object> m2List = (List<Object>) getterMethod.invoke(model, new Object[] {});
							if (m2List != null) {
								for (Object m2Obj : m2List) {
									
									if (logger.isDebugEnabled()) {
										logger.debug("Add child : " + getAssocQname(field) + " "
												+ getNodeTypeQName(field));
									}
									QName assocQname = getAssocQname(field);
									
									ChildAssociationRef childAssociationRef = nodeService.createNode(modelNodeRef,
											assocQname, assocQname, getNodeTypeQName(field));
									visitModelNodeRef(childAssociationRef.getChildRef(), m2Obj);
									
								}
							}
						} else {
							QName propName = getPropQname(field);
							if (logger.isDebugEnabled()) {
								logger.debug("Insert prop : " + propName);
							}
							nodeService.setProperty(modelNodeRef, propName, getPropValue(getterMethod, model));
						}
					}
				}
			}
		}

	}

	
	public ModelTree visitModelTreeNodeRef(NodeRef modelNodeRef){
		ModelTree ret  = extractModelTreeNode(modelNodeRef);
		
		List<ChildAssociationRef> assocs =  nodeService.getChildAssocs(modelNodeRef);
		Map<String,ModelTree> assocRoots = new HashMap<String, ModelTree>();
		//TODO recur
		for(ChildAssociationRef assoc : assocs){
			String assocName = assoc.getQName().getLocalName();
			ModelTree tmp = null;
			if(assocRoots.containsKey(assocName)){
				tmp = assocRoots.get(assocName);
			} else {
				tmp  = new ModelTree();
				tmp.setName(assocName);
				tmp.setTitle(assocName);
				tmp.setType(assoc.getTypeQName().toPrefixString(namespaceService));
				
				assocRoots.put(assocName, tmp);
				ret.getChildrens().add(tmp);
			}
			tmp.getChildrens().add(visitModelTreeNodeRef(assoc.getChildRef()));
			
		}
		
		
		return ret;
	}
	
	
	private ModelTree extractModelTreeNode(NodeRef modelNodeRef) {
		ModelTree tmp = new ModelTree(modelNodeRef.toString());
		String name = (String) nodeService.getProperty(modelNodeRef,DesignerModel.PROP_M2_NAME );
		String title = (String) nodeService.getProperty(modelNodeRef,DesignerModel.PROP_M2_TITLE );
		String description =  (String) nodeService.getProperty(modelNodeRef,DesignerModel.PROP_M2_DESCRIPTION );
	   
		if(name==null){
			name = (String) nodeService.getProperty(modelNodeRef,DesignerModel.PROP_M2_URI );
		}
		
		if(name==null){
			name = (String) nodeService.getProperty(modelNodeRef,DesignerModel.PROP_M2_REF );
		}
		
		tmp.setName(name);
		tmp.setTitle(title);
		tmp.setDescription(description);
		tmp.setType(nodeService.getType(modelNodeRef).toPrefixString(namespaceService));
		return tmp;
	}


	private Method retieveGetter(Class<?> m2Class, Field field) {
		try {
			if (field.getType().equals(Date.class)) {
				return m2Class.getMethod("get" + StringUtils.capitalize(field.getName() + "Date"), new Class<?>[] {});
			}

			return m2Class.getMethod("get" + StringUtils.capitalize(field.getName()), new Class<?>[] {});
		} catch (Exception e) {
			logger.debug("Cannot find getter");
		}
		return null;
	}

	private Serializable getPropValue(Method getterMethod, Object o) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		Object ret = getterMethod.invoke(o, new Object[] {});
		if (ret instanceof QName) {
			return ret.toString();
		}
		if (ret instanceof IndexTokenisationMode) {
			return ret.toString().toLowerCase();
		}

		return (Serializable) ret;
	}

	private QName getNodeTypeQName(Field field) {
		
		String name = "string"; 
		
		Type genericFieldType = field.getGenericType();
	    
		if(genericFieldType instanceof ParameterizedType){
		    ParameterizedType aType = (ParameterizedType) genericFieldType;
		    Type[] fieldArgTypes = aType.getActualTypeArguments();
		    for(Type fieldArgType : fieldArgTypes){
		        Class<?> fieldArgClass = (Class<?>) fieldArgType;
		        name = fieldArgClass.getSimpleName().replace("M2", "");
		       
		        name =  new StringBuffer(name.length())
		            .append(Character.toLowerCase(name.charAt(0)))
		            .append(name.substring(1))
		            .toString();
		        break;
		    }
		}
		return QName.createQName(DesignerModel.M2_URI, name);
	}

	private QName getPropQname(Field field) {
		return QName.createQName(DesignerModel.M2_URI, field.getName());
	}

	private QName getAssocQname(Field field) {
		return QName.createQName(DesignerModel.M2_URI, field.getName());
	}

	private boolean shouldIgnoreField(String name) {
		return name.equals("analyserResourceBundleName") || name.equals("JiBX_bindingList");
	}


	public InputStream visitModelXml(NodeRef modelNodeRef) {
		// TODO Auto-generated method stub
		return null;
	}

}
