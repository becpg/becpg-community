/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.repo.dictionary.M2Constraint;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.dictionary.M2Property;
import org.alfresco.repo.dictionary.M2PropertyOverride;
import org.alfresco.repo.dictionary.M2Type;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gdata.data.dublincore.Date;

import fr.becpg.repo.designer.DesignerModel;

/**
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
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
									QName assocTypeQname = getNodeTypeQName(m2Obj,model);
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

	private QName getNodeTypeQName(Object m2Object, Object model) {

		String name = "string";
		
		if(m2Object instanceof M2Constraint && (model instanceof M2Property || model instanceof M2PropertyOverride)){
			name = "constraintRef";
		} else {
		
			Class<?> fieldArgClass = m2Object.getClass();
			name = fieldArgClass.getSimpleName().replace("M2", "");
	
			name = new StringBuffer(name.length()).append(Character.toLowerCase(name.charAt(0))).append(name.substring(1))
					.toString();
		}
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
						
						setterMethod = retrieveMethod( m2Model.getClass(),"addMandatoryAspect");
						if (setterMethod != null) {
							for(String aspect : aspects){
								logger.debug("Invoke :"+setterMethod.getName());
								setterMethod.invoke(m2Model, new Object[]{aspect});
							}
						}
					}
				} else {
					setterMethod = retieveSetter(entry.getKey(), m2Model.getClass());
					if (setterMethod != null && entry.getValue()!=null && !(entry.getValue() instanceof String && ((String)entry.getValue()).isEmpty()) ) {
						logger.debug("Invoke :"+setterMethod.getName()+" "+entry.getValue().getClass().getSimpleName());
						setterMethod.invoke(m2Model, getPropValue(entry));
					} 
				}
			}
		}
		
		 List<ChildAssociationRef> assocs = 	nodeService.getChildAssocs(modelNodeRef);
		 
		 for(ChildAssociationRef assoc : assocs){
			 NodeRef childRef = assoc.getChildRef();
			 Method createAssocMethod = null;
			 if(assoc.getQName().getNamespaceURI().equals(DesignerModel.M2_URI)){
				 if(assoc.getQName().equals(DesignerModel.ASSOC_M2_CONSTRAINTS)
						 && (nodeService.getType(modelNodeRef).equals(DesignerModel.TYPE_M2_PROPERTY)
							|| nodeService.getType(modelNodeRef).equals(DesignerModel.TYPE_M2_PROPERTY_OVERRIDE)) ){
					createAssocMethod = retrieveMethod( m2Model.getClass(),"addConstraintRef");
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
			 } else {
				 logger.debug("Skip assoc :"+assoc.getQName().toString());
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
	
	return retrieveMethod( clazz,createName);
	}

	private Method retieveSetter(QName prop, Class<?> clazz) {
		String setterName;
		if(prop.getLocalName().startsWith("is")){
			setterName = "set" + StringUtils.capitalize(prop.getLocalName().replaceFirst("is", ""));
		} else if(prop.getLocalName().equals("propertyType")){
			setterName = "setType";
		} else if(prop.getLocalName().equals("published")){
			setterName = "setPublishedDate";
		} 
		
		else {
			setterName = "set" + StringUtils.capitalize(prop.getLocalName());
		}
		
	   return retrieveMethod(clazz, setterName);
	}
	
	

	private Method retieveGetter(Class<?> m2Class, Field field) {
		
		String getterName = "get" + StringUtils.capitalize(field.getName());
	
			if (field.getName().equals("propertyType")) {
				getterName = "getType";
			}  else if ( field.getName().startsWith("is")) {
				getterName = field.getName();
			} else if (field.getName().equals("propagateTimestamps") ) {
				getterName = "is" + StringUtils.capitalize(field.getName());
			} else if (field.getName().equals("allowDuplicateChildName") ) {
				getterName = field.getName();
			}
			else if (field.getType().equals(Date.class)) {
				getterName = "get" + StringUtils.capitalize(field.getName() + "Date");
			} else if(field.getName().equals("dataTypes")){
				getterName = "getPropertyTypes";
			} else if(field.getName().equals("published")){
				getterName = "getPublishedDate";
			}
			return retrieveMethod(m2Class, getterName);
		
	}
	
	private Method retrieveMethod( Class<?> clazz, String... methodNames){
		
		 for(Method method : clazz.getMethods()){
			 for(String methodName : methodNames){
				if(method.getName().equals(methodName)){
					return method;
				}
			 }
			}
		logger.error("Cannot find method for name:"+methodNames[0]+" under "+clazz.getSimpleName());
		return null;
	}

	public void visitModelTemplate(NodeRef ret, QName nodeTypeQname, String modelName, InputStream xml) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException, ClassNotFoundException {
		logger.debug("Visiting template model for:"+modelName);
		
		M2Model templateModel = M2Model.createModel(xml);

		//TODO better 
		if(nodeTypeQname.equals(DesignerModel.TYPE_M2_CONSTRAINT)){
			
			for(M2Constraint constraint : templateModel.getConstraints()) {
				if(constraint.getName().equals(modelName)){
					visitModelNodeRef(ret, constraint);
					return;
				}
			}
		} else if(nodeTypeQname.equals(DesignerModel.TYPE_M2_TYPE)){
			for(M2Type type : templateModel.getTypes()) {
				if(type.getName().equals(modelName)){
					visitModelNodeRef(ret, type);
					return;
				}
			}
		} else {
			logger.error("Unable to read model");
		}

	}

	/**
	 * For future use
	 * @param nodeTypeQname
	 * @return
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unused")
	private Class<?> getClassFromQname(QName nodeTypeQname) throws ClassNotFoundException {
		String className = "org.alfresco.repo.dictionary.M2" + StringUtils.capitalize(nodeTypeQname.getLocalName());
		logger.debug("Try to instanciate from xml :" + className);
		Class<?> ret = getClass().getClassLoader().loadClass(className);

		return ret;
	}

}
