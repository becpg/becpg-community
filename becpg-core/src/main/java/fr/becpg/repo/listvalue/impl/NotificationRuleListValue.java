package fr.becpg.repo.listvalue.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.repo.listvalue.ListValueEntry;
import fr.becpg.repo.listvalue.ListValueExtractor;
import fr.becpg.repo.listvalue.ListValuePage;
import fr.becpg.repo.listvalue.ListValuePlugin;

@Service
public class NotificationRuleListValue implements ListValuePlugin {

	private static final Log logger = LogFactory.getLog(NotificationRuleListValue.class);
	
	private static final String ENTITY_TYPE_VALUE = "entityTypeValue";
	private static final String PROP_DATE_VALUE = "propertyDateValue";
	private static final String SEPARATOR = "|";
	
	@Autowired
	@Qualifier("ServiceRegistry")
	private ServiceRegistry serviceRegistry;
	
	
	@Override
	public String[] getHandleSourceTypes() {
		return new String [] { ENTITY_TYPE_VALUE, PROP_DATE_VALUE };
	}

	@Override
	public ListValuePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {
		switch(sourceType){
			case ENTITY_TYPE_VALUE:
				return getAvailableTypeNames(query, pageNum, pageSize);
			case PROP_DATE_VALUE:
				return getAvailablePropNames(query, pageNum, pageSize);
		}
		
		logger.warn("Could not find any values for sourceType " + sourceType);
		
		return null;
	}
	
	
	private boolean filter(String suggestion, String query) {
		return query.contains("*") || suggestion.toLowerCase().contains(query.toLowerCase());
	}
	
	
	public class StringValueExtractor implements ListValueExtractor<String> {

		private final String type;
		
		public StringValueExtractor(String type) {
			this.type = type;
		}

		@Override
		public List<ListValueEntry> extract(List<String> values) {

			List<ListValueEntry> suggestions = new ArrayList<>();
			if (values != null) {
				for (String value : values) {
					String[] splitted = value.split("\\|");
					suggestions.add(new  ListValueEntry( splitted[0], splitted[1], this.type));
				}
			}
			return suggestions;
		}

	}
	
	
	private ListValuePage getAvailableTypeNames(String query, Integer pageNum, Integer pageSize) {
		List<String> suggestions = new ArrayList<>();
		Collection<QName> types = serviceRegistry.getDictionaryService().getAllTypes();
		if (types != null) {
			for (QName type : types) {
				serviceRegistry.getDictionaryService().getProperty(type);
				TypeDefinition typeDef = serviceRegistry.getDictionaryService().getType(type);
				String suggestion = type.toPrefixString(serviceRegistry.getNamespaceService()) + SEPARATOR +
						(typeDef != null && typeDef.getTitle(serviceRegistry.getDictionaryService())!=null && typeDef.getTitle(serviceRegistry.getDictionaryService()).length()>0
						? typeDef.getTitle(serviceRegistry.getDictionaryService()):type.toPrefixString(serviceRegistry.getNamespaceService()));
					if (filter(suggestion,query)) {
						suggestions.add(suggestion);
					}
			}
		}

		return new ListValuePage(suggestions, pageNum, pageSize, new StringValueExtractor("modelType"));
	}
	
	
	private ListValuePage getAvailablePropNames(String query, Integer pageNum, Integer pageSize) {
		List<String> suggestions = new ArrayList<>();
		Collection<QName> props = serviceRegistry.getDictionaryService().getAllProperties(DataTypeDefinition.DATETIME);
		props.addAll(serviceRegistry.getDictionaryService().getAllProperties(DataTypeDefinition.DATE));
		if (props != null) {
			for (QName prop : props) {
				serviceRegistry.getDictionaryService().getProperty(prop);
				PropertyDefinition propDef = serviceRegistry.getDictionaryService().getProperty(prop);
				String suggestion = prop.toPrefixString(serviceRegistry.getNamespaceService()) + SEPARATOR +
						(propDef != null && propDef.getTitle(serviceRegistry.getDictionaryService())!=null && propDef.getTitle(serviceRegistry.getDictionaryService()).length()>0
						? propDef.getTitle(serviceRegistry.getDictionaryService()):prop.toPrefixString(serviceRegistry.getNamespaceService()));
					if (filter(suggestion,query)) {
						suggestions.add(suggestion + " (" + prop.toPrefixString() + ")");
					}
			}
		}

		return new ListValuePage(suggestions, pageNum, pageSize, new StringValueExtractor("modelType"));
	}
	
	

}
	