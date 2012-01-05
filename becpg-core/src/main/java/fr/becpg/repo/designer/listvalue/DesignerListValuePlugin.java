package fr.becpg.repo.designer.listvalue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.common.RepoConsts;
import fr.becpg.repo.designer.DesignerModel;
import fr.becpg.repo.designer.DesignerService;
import fr.becpg.repo.listvalue.ListValueExtractor;
import fr.becpg.repo.listvalue.ListValuePage;
import fr.becpg.repo.listvalue.ListValueService;
import fr.becpg.repo.listvalue.impl.AbstractBaseListValuePlugin;

public class DesignerListValuePlugin extends AbstractBaseListValuePlugin {

	private static String TYPE_PARENT_NAME = "parentName";
	private static String TYPE_MANDATORY_ASPECTS = "mandatoryAspects";
	private static String TYPE_PROPERTY_TYPE = "propertyType";
	private static String TYPE_TARGET_CLASS_NAME = "targetClassName";
	private static String TYPE_CONSTRAINT_REF = "constraintRef";

	private static String SEPARATOR = "|";

	/** The service registry. */
	private ServiceRegistry serviceRegistry;

	private DesignerService designerService;

	
	
	/**
	 * @param designerService
	 *            the designerService to set
	 */
	public void setDesignerService(DesignerService designerService) {
		this.designerService = designerService;
	}

	/**
	 * @param serviceRegistry
	 *            the serviceRegistry to set
	 */
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { TYPE_PARENT_NAME, TYPE_MANDATORY_ASPECTS, TYPE_PROPERTY_TYPE, TYPE_TARGET_CLASS_NAME,
				TYPE_CONSTRAINT_REF };
	}

	@Override
	public ListValuePage suggest(String sourceType, String query, Integer pageNum, Map<String, Serializable> props) {

		String nodeRef = (String) props.get(ListValueService.PROP_NODEREF);

		NodeRef modelNodeRef = null;
		if(nodeRef!=null){
			modelNodeRef = designerService.findModelNodeRef(new NodeRef(nodeRef));
		}

		if (sourceType.equals(TYPE_PARENT_NAME)) {
			return getAvailableEntityTypeNames(modelNodeRef, query, pageNum);
		} else if (sourceType.equals(TYPE_MANDATORY_ASPECTS)) {
			return getAvailableEntityAspectNames(modelNodeRef, query, pageNum);
		} else if (sourceType.equals(TYPE_PROPERTY_TYPE)) {
			return getAvailableDataTypeNames(modelNodeRef, query, pageNum);
		} else if (sourceType.equals(TYPE_TARGET_CLASS_NAME)) {
			return getAvailableEntityTypeNames(modelNodeRef, query, pageNum);
		} else if (sourceType.equals(TYPE_CONSTRAINT_REF)) {
			return getAvailableConstraints(modelNodeRef, query, pageNum);
		}

		return null;
	}

	private ListValuePage getAvailableConstraints(NodeRef modelNodeRef, String query, Integer pageNum) {
		List<String> suggestions = new ArrayList<String>();
		
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
		
		return new ListValuePage(suggestions, pageNum, RepoConsts.SUGGEST_PAGE_SIZE, new StringValueExtractor(),
				"dataType");
	}

	public class StringValueExtractor implements ListValueExtractor<String> {

		@Override
		public Map<String, String> extract(List<String> values) {

			Map<String, String> suggestions = new HashMap<String, String>();
			if (values != null) {
				for (String value : values) {
					String[] splitted = value.split("\\|");
					suggestions.put(splitted[0], splitted[1]);
				}
			}
			return suggestions;

		}

	}

	private ListValuePage getAvailableDataTypeNames(NodeRef modelNodeRef, String query, Integer pageNum) {
		List<String> uris = getImports(modelNodeRef);
		
		List<String> suggestions = new ArrayList<String>();
		Collection<QName> types = serviceRegistry.getDictionaryService().getAllDataTypes();
		if (types != null) {
			for (QName type : types) {
				if (uris.contains(type.getNamespaceURI())) {
					DataTypeDefinition typeDef = serviceRegistry.getDictionaryService().getDataType(type);
					String suggestion = type.toPrefixString(serviceRegistry.getNamespaceService()) + SEPARATOR +
							(typeDef != null && typeDef.getTitle()!=null && typeDef.getTitle().length()>0
							? typeDef.getTitle():type.toPrefixString(serviceRegistry.getNamespaceService()));
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
		

		return new ListValuePage(suggestions, pageNum, RepoConsts.SUGGEST_PAGE_SIZE, new StringValueExtractor(),
				"dataType");
	}

	private boolean filter(String suggestion, String query) {
		return query.contains("*") || suggestion.toLowerCase().contains(query.toLowerCase());
	}

	private ListValuePage getAvailableEntityTypeNames(NodeRef modelNodeRef, String query, Integer pageNum) {

		List<String> uris = getImports(modelNodeRef);

		List<String> suggestions = new ArrayList<String>();
		Collection<QName> types = serviceRegistry.getDictionaryService().getAllTypes();
		if (types != null) {
			for (QName type : types) {
				if (uris.contains(type.getNamespaceURI())) {
					TypeDefinition typeDef = serviceRegistry.getDictionaryService().getType(type);
					String suggestion = type.toPrefixString(serviceRegistry.getNamespaceService()) + SEPARATOR +
							(typeDef != null && typeDef.getTitle()!=null && typeDef.getTitle().length()>0
							? typeDef.getTitle():type.toPrefixString(serviceRegistry.getNamespaceService()));
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
		

		return new ListValuePage(suggestions, pageNum, RepoConsts.SUGGEST_PAGE_SIZE, new StringValueExtractor(),
				"modelType");
	}

	

	private ListValuePage getAvailableEntityAspectNames(NodeRef modelNodeRef, String query, Integer pageNum) {

		List<String> uris = getImports(modelNodeRef);

		List<String> suggestions = new ArrayList<String>();
		Collection<QName> aspects = serviceRegistry.getDictionaryService().getAllAspects();
		if (aspects != null) {
			for (QName aspect : aspects) {
				if (uris.contains(aspect.getNamespaceURI())) {
					AspectDefinition typeDef = serviceRegistry.getDictionaryService().getAspect(aspect);
					String suggestion = aspect.toPrefixString(serviceRegistry.getNamespaceService()) + SEPARATOR +
							(typeDef != null && typeDef.getTitle()!=null && typeDef.getTitle().length()>0
							? typeDef.getTitle():aspect.toPrefixString(serviceRegistry.getNamespaceService()));
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

		return new ListValuePage(suggestions, pageNum, RepoConsts.SUGGEST_PAGE_SIZE, new StringValueExtractor(),
				"aspectType");
	}
	
	
	private List<String> getImports(NodeRef modelNodeRef) {
		List<String> imports = new ArrayList<String>();
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
		
		logger.warn("Could not find any imports");
		return imports;
	}

}
