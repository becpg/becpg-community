package fr.becpg.repo.designer.impl;

import java.io.InputStream;
import java.io.OutputStream;
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
import java.util.Map.Entry;

import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

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
	 * @param nodeService
	 *            the nodeService to set
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
	 * @param namespaceService
	 *            the namespaceService to set
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
						if (field.getType().equals(List.class)
								&& !getNodeTypeQName(field).getLocalName().equals("string")) {
							List<Object> m2List = (List<Object>) getterMethod.invoke(model, new Object[] {});
							if (m2List != null) {
								for (Object m2Obj : m2List) {
									QName assocQname = getAssocQname(field);
									QName assocTypeQname = getNodeTypeQName(m2Obj);
									if (logger.isDebugEnabled()) {
										logger.debug("Add child : " + assocQname + " " + assocTypeQname);
									}

									ChildAssociationRef childAssociationRef = nodeService.createNode(modelNodeRef,
											assocQname, assocQname, assocTypeQname);
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

	public ModelTree visitModelTreeNodeRef(NodeRef modelNodeRef) {
		ModelTree ret = extractModelTreeNode(modelNodeRef);

		List<ChildAssociationRef> assocs = nodeService.getChildAssocs(modelNodeRef);
		Map<String, ModelTree> assocRoots = new HashMap<String, ModelTree>();
		for (ChildAssociationRef assoc : assocs) {
			String assocName = assoc.getQName().getLocalName();
			ModelTree tmp = null;
			if (assocRoots.containsKey(assocName)) {
				tmp = assocRoots.get(assocName);
			} else {
				tmp = new ModelTree();
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
		String name = (String) nodeService.getProperty(modelNodeRef, DesignerModel.PROP_M2_NAME);
		String title = (String) nodeService.getProperty(modelNodeRef, DesignerModel.PROP_M2_TITLE);
		String description = (String) nodeService.getProperty(modelNodeRef, DesignerModel.PROP_M2_DESCRIPTION);

		if (name == null) {
			name = (String) nodeService.getProperty(modelNodeRef, DesignerModel.PROP_M2_URI);
		}

		if (name == null) {
			name = (String) nodeService.getProperty(modelNodeRef, DesignerModel.PROP_M2_REF);
		}

		tmp.setName(name);
		tmp.setTitle(title);
		tmp.setDescription(description);
		tmp.setType(nodeService.getType(modelNodeRef).toPrefixString(namespaceService));
		return tmp;
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

	private QName getNodeTypeQName(Object m2Object) {

		String name = "string";
		Class<?> fieldArgClass = m2Object.getClass();
		name = fieldArgClass.getSimpleName().replace("M2", "");

		name = new StringBuffer(name.length()).append(Character.toLowerCase(name.charAt(0))).append(name.substring(1))
				.toString();
		return QName.createQName(DesignerModel.M2_URI, name);
	}

	private QName getNodeTypeQName(Field field) {

		String name = "string";

		Type genericFieldType = field.getGenericType();

		if (genericFieldType instanceof ParameterizedType) {
			ParameterizedType aType = (ParameterizedType) genericFieldType;
			Type[] fieldArgTypes = aType.getActualTypeArguments();
			for (Type fieldArgType : fieldArgTypes) {
				Class<?> fieldArgClass = (Class<?>) fieldArgType;
				name = fieldArgClass.getSimpleName().replace("M2", "");

				name = new StringBuffer(name.length()).append(Character.toLowerCase(name.charAt(0)))
						.append(name.substring(1)).toString();
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

	public void visitModelXml(NodeRef modelNodeRef, OutputStream out) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		String name = (String) nodeService.getProperty(modelNodeRef, DesignerModel.PROP_M2_NAME);
		
		M2Model m2Model = M2Model.createModel(name);
		
		visitModel(m2Model,modelNodeRef);
		
		
		m2Model.toXML(out);
	}

	private void visitModel(Object m2Model, NodeRef modelNodeRef) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Map<QName, Serializable> props = nodeService.getProperties(modelNodeRef);
		//Set properties
		for(Entry<QName, Serializable> entry : props.entrySet()){
			if(entry.getKey().getNamespaceURI().equals(DesignerModel.M2_URI)){
				Method setterMethod = null;
				if(entry.getKey().getLocalName().equals("mandatoryAspects")){
					if(entry.getValue() !=null ){
						@SuppressWarnings("unchecked")
						List<String> aspects = (List<String>) entry.getValue();
						
						setterMethod = retriveMethod( m2Model.getClass(),"addMandatoryAspect");
						if (setterMethod != null) {
							for(String aspect : aspects){
								logger.debug("Invoke :"+setterMethod.getName());
								setterMethod.invoke(m2Model, new Object[]{aspect});
							}
						}
					}
				} else {
					setterMethod = retieveSetter(entry.getKey(), m2Model.getClass());
					if (setterMethod != null && entry.getValue()!=null) {
						logger.debug("Invoke :"+setterMethod.getName()+" "+entry.getValue().getClass().getSimpleName());
						setterMethod.invoke(m2Model, getPropValue(entry));
					} 
				}
			}
		}
		
		 List<ChildAssociationRef> assocs = 	nodeService.getChildAssocs(modelNodeRef);
		 
		 for(ChildAssociationRef assoc : assocs){
			 NodeRef childRef = assoc.getChildRef();
			 Method createAssocMethod = retieveCreateMethod(childRef, assoc.getQName(), m2Model.getClass());
			 if(assoc.getQName().equals(DesignerModel.ASSOC_M2_CONSTRAINTS)
					 && (nodeService.getType(modelNodeRef).equals(DesignerModel.TYPE_M2_PROPERTY)
						|| nodeService.getType(modelNodeRef).equals(DesignerModel.TYPE_M2_PROPERTY_OVERRIDE)) ){
				 createAssocMethod = retriveMethod( m2Model.getClass(),"addConstraintRef");
			 } else {
				 createAssocMethod = retieveCreateMethod(childRef, assoc.getQName(), m2Model.getClass());
			 }
			 if(createAssocMethod!=null){
				 logger.debug("Invoke :"+createAssocMethod.getName());
				 Object m2Object = createAssocMethod.invoke(m2Model, new Object[createAssocMethod.getParameterTypes().length]);
				 if(m2Object!=null){
					 visitModel(m2Object, childRef);
				 }
				 
			 }
		 }
		
		
		
	}


	private Object getPropValue(Entry<QName, Serializable> entry) {
		if(entry.getKey().equals(DesignerModel.PROP_M2_INDEX_MODE)){
			return IndexTokenisationMode.valueOf(((String)entry.getValue()).toUpperCase());
		}
		return entry.getValue();
	}

	private Method retieveCreateMethod(NodeRef childRef,QName qName, Class<?> clazz) {
		
		
	String localName = qName.getLocalName();
	if(localName.equals("properties")){
		localName = "propertys";
	}
	String createName = "create" + StringUtils.capitalize(localName).substring(0,localName.length()-1 );
	if(nodeService.getType(childRef).equals(DesignerModel.TYPE_M2_CHILD_ASSOCIATION)){
		createName = "createChildAssociation";
	}
	
	return retriveMethod( clazz,createName);
	}

	private Method retieveSetter(QName prop, Class<?> clazz) {
		String setterName;
		if(prop.getLocalName().startsWith("is")){
			setterName = "set" + StringUtils.capitalize(prop.getLocalName().replaceFirst("is", ""));
		} else if(prop.getLocalName().equals("propertyType")){
			setterName = "setType";
		} else {
			setterName = "set" + StringUtils.capitalize(prop.getLocalName());
		}
		
	   return retriveMethod(clazz, setterName);
	}
	
	

	private Method retieveGetter(Class<?> m2Class, Field field) {
		
		String getterName = "get" + StringUtils.capitalize(field.getName());
	
			if (field.getName().equals("propertyType")) {
				getterName = "getType";
			}  else if ( field.getName().startsWith("is")) {
				getterName = field.getName();
			} else if (field.getType().equals(Date.class)) {
				getterName = "get" + StringUtils.capitalize(field.getName() + "Date");
			}
			return retriveMethod(m2Class, getterName);
		
	}
	
	private Method retriveMethod( Class<?> clazz, String... methodNames){
		
		 for(Method method : clazz.getMethods()){
			 for(String methodName : methodNames){
				if(method.getName().equals(methodName)){
					return method;
				}
			 }
			}
		logger.error("Cannot find method for name:"+methodNames[0]);
		return null;
	}

	public void visitModelTemplate(NodeRef ret, QName nodeTypeQname, InputStream xml) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException, ClassNotFoundException {

		Class<?> clazz = getClassFromQname(nodeTypeQname);
		
		
		Object model = null;
		try {
			IBindingFactory factory = BindingDirectory.getFactory("default",clazz);
			IUnmarshallingContext context = factory.createUnmarshallingContext();
			model = context.unmarshalDocument(xml, null);
		} catch (JiBXException e) {
			throw new DictionaryException("Failed to parse model", e);
		}

		if (model != null) {

			visitModelNodeRef(ret, model);
		} else {
			logger.error("Unable to read model");
		}

	}

	private Class<?> getClassFromQname(QName nodeTypeQname) throws ClassNotFoundException {
		String className = "org.alfresco.repo.dictionary.M2" + StringUtils.capitalize(nodeTypeQname.getLocalName());
		logger.debug("Try to instanciate from xml :" + className);
		Class<?> ret = getClass().getClassLoader().loadClass(className);

		return ret;
	}

}
