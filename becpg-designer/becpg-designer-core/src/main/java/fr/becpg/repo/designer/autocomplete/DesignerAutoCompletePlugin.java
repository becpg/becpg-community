/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG. 
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
package fr.becpg.repo.designer.autocomplete;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.repo.autocomplete.AutoCompleteEntry;
import fr.becpg.repo.autocomplete.AutoCompleteExtractor;
import fr.becpg.repo.autocomplete.AutoCompletePage;
import fr.becpg.repo.autocomplete.AutoCompletePlugin;
import fr.becpg.repo.autocomplete.AutoCompleteService;
import fr.becpg.repo.designer.DesignerModel;
import fr.becpg.repo.designer.DesignerService;

/**
 * <p>DesignerListValuePlugin class.</p>
 *
 * @author "Matthieu Laborie"
 * @version $Id: $Id
 *
 * Autocomplete plugin that allows to get current model informations
 *
 * Example:
 *
 * <pre>
 * {@code
 * 	<control template="/org/alfresco/components/form/controls/autocomplete.ftl">
 *  	<control-param name="ds">becpg/autocomplete/mandatoryAspects</control-param>
 *		<control-param name="showTooltip">false</control-param>
 *		<control-param name="saveTitle">false</control-param>
 *   </control>
 * }
 * <pre>
 *
 *  Datasources available:
 *
 *   Return current model available types :
 *   becpg/autocomplete/targetClassName
 *   becpg/autocomplete/parentName (alias)
 *
 *   becpg/autocomplete/mandatoryAspects  return current model available aspects
 *   becpg/autocomplete/propertyType return current model property types
 *   becpg/autocomplete/constraintRef return current model constraint
 */
@Service
public class DesignerAutoCompletePlugin implements AutoCompletePlugin {

	private static final  String TYPE_PARENT_NAME = "parentName";
	private static final  String TYPE_MANDATORY_ASPECTS = "mandatoryAspects";
	private static final  String TYPE_PROPERTY_TYPE = "propertyType";
	private static final  String TYPE_TARGET_CLASS_NAME = "targetClassName";
	private static final  String TYPE_CONSTRAINT_REF = "constraintRef";

	private static final  String SEPARATOR = "|";


	private static final  Log logger = LogFactory.getLog(DesignerAutoCompletePlugin.class);
	
	@Autowired
	@Qualifier("ServiceRegistry")
	private ServiceRegistry serviceRegistry;
	@Autowired
	private DesignerService designerService;


	/** {@inheritDoc} */
	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { TYPE_PARENT_NAME, TYPE_MANDATORY_ASPECTS, TYPE_PROPERTY_TYPE, TYPE_TARGET_CLASS_NAME,
				TYPE_CONSTRAINT_REF };
	}

	/** {@inheritDoc} */
	@Override
	public AutoCompletePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {

		String nodeRef = (String) props.get(AutoCompleteService.PROP_NODEREF);

		if (nodeRef == null || nodeRef.isBlank()) {
			nodeRef = (String) props.get(AutoCompleteService.PROP_ENTITYNODEREF);
		}
		
		NodeRef modelNodeRef = null;
		if(nodeRef!=null){
			modelNodeRef = designerService.findModelNodeRef(new NodeRef(nodeRef));
		}

		switch (sourceType) {
			case TYPE_PARENT_NAME:
				return getAvailableEntityTypeNames(modelNodeRef, query, pageNum, pageSize);
			case TYPE_MANDATORY_ASPECTS:
				return getAvailableEntityAspectNames(modelNodeRef, query, pageNum, pageSize);
			case TYPE_PROPERTY_TYPE:
				return getAvailableDataTypeNames(modelNodeRef, query, pageNum, pageSize);
			case TYPE_TARGET_CLASS_NAME:
				return getAvailableEntityTypeNames(modelNodeRef, query, pageNum, pageSize);
			case TYPE_CONSTRAINT_REF:
				return getAvailableConstraints(modelNodeRef, query, pageNum, pageSize);
		}

		return null;
	}

	private AutoCompletePage getAvailableConstraints(NodeRef modelNodeRef, String query, Integer pageNum, Integer pageSize) {
		List<String> suggestions = new ArrayList<>();
		
		if(modelNodeRef!=null){
			for(ChildAssociationRef assoc :  serviceRegistry.getNodeService().getChildAssocs(modelNodeRef)){
				if(assoc.getQName().equals(DesignerModel.ASSOC_M2_CONSTRAINTS)){
					NodeRef namespaceNodeRef = assoc.getChildRef();
				    String name = (String) serviceRegistry.getNodeService().getProperty(namespaceNodeRef,DesignerModel.PROP_M2_NAME);
				    String title = (String) serviceRegistry.getNodeService().getProperty(namespaceNodeRef,DesignerModel.PROP_M2_TITLE);
				    String suggestion =name + SEPARATOR + ((title !=null && title.length()>0) ? title : name);
					if (filter(suggestion,query)) {
						suggestions.add(suggestion);
					}
				}
			}
			
		} 
		
		return new AutoCompletePage(suggestions, pageNum, pageSize, new StringValueExtractor("dataType"));
	}

	public class StringValueExtractor implements AutoCompleteExtractor<String> {

		private final String type;
		
		public StringValueExtractor(String type) {
			this.type = type;
		}

		@Override
		public List<AutoCompleteEntry> extract(List<String> values) {

			List<AutoCompleteEntry> suggestions = new ArrayList<>();
			if (values != null) {
				for (String value : values) {
					String[] splitted = value.split("\\|");
					suggestions.add(new  AutoCompleteEntry( splitted[0], splitted[1], this.type));
				}
			}
			return suggestions;

		}

	}

	private AutoCompletePage getAvailableDataTypeNames(NodeRef modelNodeRef, String query, Integer pageNum, Integer pageSize) {
		List<String> uris = getImports(modelNodeRef);
		
		List<String> suggestions = new ArrayList<>();
		Collection<QName> types = serviceRegistry.getDictionaryService().getAllDataTypes();
		if (types != null) {
			for (QName type : types) {
				if (uris.contains(type.getNamespaceURI())) {
					DataTypeDefinition typeDef = serviceRegistry.getDictionaryService().getDataType(type);
					String suggestion = type.toPrefixString(serviceRegistry.getNamespaceService()) + SEPARATOR +
							(typeDef != null && typeDef.getTitle(serviceRegistry.getDictionaryService())!=null && typeDef.getTitle(serviceRegistry.getDictionaryService()).length()>0
							? typeDef.getTitle(serviceRegistry.getDictionaryService()):type.toPrefixString(serviceRegistry.getNamespaceService()));
					if (filter(suggestion,query)) {
						suggestions.add(suggestion);
					}
					
				}
			}
		}
		
		
		if(modelNodeRef!=null){
			for(ChildAssociationRef assoc :  serviceRegistry.getNodeService().getChildAssocs(modelNodeRef)){
				if(assoc.getQName().equals(DesignerModel.ASSOC_M2_DATA_TYPE)){
					NodeRef namespaceNodeRef = assoc.getChildRef();
				    String name = (String) serviceRegistry.getNodeService().getProperty(namespaceNodeRef,DesignerModel.PROP_M2_NAME);
				    String title = (String) serviceRegistry.getNodeService().getProperty(namespaceNodeRef,DesignerModel.PROP_M2_TITLE);
				    String suggestion =name + SEPARATOR + ((title !=null && title.length()>0) ? title : name);
					if (filter(suggestion,query)) {
						suggestions.add(suggestion);
					}
				}
			}
			
		} 
		

		return new AutoCompletePage(suggestions, pageNum, pageSize, new StringValueExtractor("dataType"));
	}

	private boolean filter(String suggestion, String query) {
		return query.contains("*") || suggestion.toLowerCase().contains(query.toLowerCase());
	}

	private AutoCompletePage getAvailableEntityTypeNames(NodeRef modelNodeRef, String query, Integer pageNum, Integer pageSize) {

		List<String> uris = getImports(modelNodeRef);

		List<String> suggestions = new ArrayList<>();
		Collection<QName> types = serviceRegistry.getDictionaryService().getAllTypes();
		if (types != null) {
			for (QName type : types) {
				if (uris.contains(type.getNamespaceURI())) {
					TypeDefinition typeDef = serviceRegistry.getDictionaryService().getType(type);
					String suggestion = type.toPrefixString(serviceRegistry.getNamespaceService()) + SEPARATOR +
							(typeDef != null && typeDef.getTitle(serviceRegistry.getDictionaryService())!=null && typeDef.getTitle(serviceRegistry.getDictionaryService()).length()>0
							? typeDef.getTitle(serviceRegistry.getDictionaryService()):type.toPrefixString(serviceRegistry.getNamespaceService()));
						if (filter(suggestion,query)) {
							suggestions.add(suggestion);
						}
				}
			}
		}
		
		
		if(modelNodeRef!=null){
			for(ChildAssociationRef assoc :  serviceRegistry.getNodeService().getChildAssocs(modelNodeRef)){
				if(assoc.getQName().equals(DesignerModel.ASSOC_M2_TYPES)){
					NodeRef namespaceNodeRef = assoc.getChildRef();
				    String name = (String) serviceRegistry.getNodeService().getProperty(namespaceNodeRef,DesignerModel.PROP_M2_NAME);
				    String title = (String) serviceRegistry.getNodeService().getProperty(namespaceNodeRef,DesignerModel.PROP_M2_TITLE);
				    String suggestion =name + SEPARATOR + ((title !=null && title.length()>0) ? title : name);
					if (filter(suggestion,query)) {
						suggestions.add(suggestion);
					}
				}
			}
			
		} 
		

		return new AutoCompletePage(suggestions, pageNum, pageSize, new StringValueExtractor("modelType"));
	}

	

	private AutoCompletePage getAvailableEntityAspectNames(NodeRef modelNodeRef, String query, Integer pageNum, Integer pageSize) {

		List<String> uris = getImports(modelNodeRef);

		List<String> suggestions = new ArrayList<>();
		Collection<QName> aspects = serviceRegistry.getDictionaryService().getAllAspects();
		if (aspects != null) {
			for (QName aspect : aspects) {
				if (uris.contains(aspect.getNamespaceURI())) {
					AspectDefinition typeDef = serviceRegistry.getDictionaryService().getAspect(aspect);
					String suggestion = aspect.toPrefixString(serviceRegistry.getNamespaceService()) + SEPARATOR +
							(typeDef != null && typeDef.getTitle(serviceRegistry.getDictionaryService())!=null && typeDef.getTitle(serviceRegistry.getDictionaryService()).length()>0
							? typeDef.getTitle(serviceRegistry.getDictionaryService()):aspect.toPrefixString(serviceRegistry.getNamespaceService()));
						if (filter(suggestion,query)) {
							suggestions.add(suggestion);
						}
				}
			}
		}
		
		if(modelNodeRef!=null){
			for(ChildAssociationRef assoc :  serviceRegistry.getNodeService().getChildAssocs(modelNodeRef)){
				if(assoc.getQName().equals(DesignerModel.ASSOC_M2_ASPECTS)){
					NodeRef namespaceNodeRef = assoc.getChildRef();
				    String name = (String) serviceRegistry.getNodeService().getProperty(namespaceNodeRef,DesignerModel.PROP_M2_NAME);
				    String title = (String) serviceRegistry.getNodeService().getProperty(namespaceNodeRef,DesignerModel.PROP_M2_TITLE);
				    String suggestion =name + SEPARATOR + ((title !=null && title.length()>0) ? title : name);
					if (filter(suggestion,query)) {
						suggestions.add(suggestion);
					}
				}
			}
			
		} 

		return new AutoCompletePage(suggestions, pageNum, pageSize, new StringValueExtractor("aspectType"));
	}
	
	
	private List<String> getImports(NodeRef modelNodeRef) {
		List<String> imports = new ArrayList<>();
		if(modelNodeRef!=null){
			for(ChildAssociationRef assoc :  serviceRegistry.getNodeService().getChildAssocs(modelNodeRef)){
				if(assoc.getQName().equals(DesignerModel.ASSOC_M2_IMPORTS)){
					NodeRef namespaceNodeRef = assoc.getChildRef();
				    String uri = (String) serviceRegistry.getNodeService().getProperty(namespaceNodeRef,DesignerModel.PROP_M2_URI);
				    imports.add(uri);
				}
			}
			
		} else {
			logger.warn("Cannot find model nodeRef");
		}
		if(imports.size()<1){
			logger.warn("Could not find any imports");
		}
		return imports;
	}

}
