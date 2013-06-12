package fr.becpg.repo.security.listvalue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.listvalue.ListValueEntry;
import fr.becpg.repo.listvalue.ListValueExtractor;
import fr.becpg.repo.listvalue.ListValuePage;
import fr.becpg.repo.listvalue.impl.AbstractBaseListValuePlugin;

/**
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 * 
 */
public class SecurityListValuePlugin extends AbstractBaseListValuePlugin {

	private static String TYPE_ACL_TYPE = "aclType";

	private static String SEPARATOR = "|";

	/** The service registry. */
	private ServiceRegistry serviceRegistry;

	/**
	 * @param serviceRegistry
	 *            the serviceRegistry to set
	 */
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { TYPE_ACL_TYPE };
	}

	@Override
	public ListValuePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {

		return getAvailableEntityTypeNames(query, pageNum, pageSize);

	}

	public class StringValueExtractor implements ListValueExtractor<String> {

		private String type;

		public StringValueExtractor(String type) {
			this.type = type;
		}

		@Override
		public List<ListValueEntry> extract(List<String> values) {

			List<ListValueEntry> suggestions = new ArrayList<ListValueEntry>();
			if (values != null) {
				for (String value : values) {
					String[] splitted = value.split("\\|");
					suggestions.add(new ListValueEntry(splitted[0], splitted[1], this.type));
				}
			}
			return suggestions;

		}

	}

	private boolean filter(String suggestion, String query) {
		return query.contains("*") || suggestion.toLowerCase().contains(query.toLowerCase());
	}

	private ListValuePage getAvailableEntityTypeNames(String query, Integer pageNum, Integer pageSize) {

		List<String> suggestions = new ArrayList<String>();
		Collection<QName> types = serviceRegistry.getDictionaryService().getSubTypes(BeCPGModel.TYPE_ENTITY_V2, true);
		types.addAll(serviceRegistry.getDictionaryService().getSubTypes(BeCPGModel.TYPE_ENTITYLIST_ITEM, true));

		if (types != null) {
			for (QName type : types) {
				TypeDefinition typeDef = serviceRegistry.getDictionaryService().getType(type);
				String suggestion = type.toPrefixString(serviceRegistry.getNamespaceService())
						+ SEPARATOR
						+ (typeDef != null && typeDef.getTitle() != null && typeDef.getTitle().length() > 0 ? typeDef.getTitle() : type.toPrefixString(serviceRegistry
								.getNamespaceService()));
				if (filter(suggestion, query)) {
					suggestions.add(suggestion);
				}
			}
		}

		return new ListValuePage(suggestions, pageNum, pageSize, new StringValueExtractor("modelType"));
	}

}
