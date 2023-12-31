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
package fr.becpg.repo.security.autocomplete;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.autocomplete.AutoCompleteEntry;
import fr.becpg.repo.autocomplete.AutoCompleteExtractor;
import fr.becpg.repo.autocomplete.AutoCompletePage;
import fr.becpg.repo.autocomplete.AutoCompletePlugin;

/**
 * <p>SecurityListValuePlugin class.</p>
 *
 * @author "Matthieu Laborie"
 * @version $Id: $Id
 */
@Service
public class SecurityAutoCompletePlugin implements AutoCompletePlugin {

	private static final String TYPE_ACL_TYPE = "aclType";

	private static final String SEPARATOR = "|";

	@Autowired
	@Qualifier("ServiceRegistry")
	private ServiceRegistry serviceRegistry;

	/** {@inheritDoc} */
	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { TYPE_ACL_TYPE };
	}

	/** {@inheritDoc} */
	@Override
	public AutoCompletePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {

		return getAvailableEntityTypeNames(query, pageNum, pageSize);

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
					suggestions.add(new AutoCompleteEntry(splitted[0], splitted[1], this.type));
				}
			}
			return suggestions;

		}

	}

	private boolean filter(String suggestion, String query) {
		return query.contains("*") || suggestion.toLowerCase().contains(query.toLowerCase());
	}

	private AutoCompletePage getAvailableEntityTypeNames(String query, Integer pageNum, Integer pageSize) {

		List<String> suggestions = new ArrayList<>();

		addSuggestions(serviceRegistry.getDictionaryService().getSubTypes(BeCPGModel.TYPE_ENTITY_V2, true), suggestions, query);
		addSuggestions(serviceRegistry.getDictionaryService().getSubTypes(BeCPGModel.TYPE_ENTITYLIST_ITEM, true), suggestions, query);

		return new AutoCompletePage(suggestions, pageNum, pageSize, new StringValueExtractor("modelType"));
	}

	private void addSuggestions(Collection<QName> types, List<String> suggestions, String query) {
		if (types != null) {
			for (QName type : types) {
				TypeDefinition typeDef = serviceRegistry.getDictionaryService().getType(type);
				String suggestion = type.toPrefixString(serviceRegistry.getNamespaceService())
						+ SEPARATOR
						+ (typeDef != null && typeDef.getTitle(serviceRegistry.getDictionaryService()) != null
								&& typeDef.getTitle(serviceRegistry.getDictionaryService()).length() > 0 ? typeDef.getTitle(serviceRegistry
								.getDictionaryService()) : type.toPrefixString(serviceRegistry.getNamespaceService()));
				if (filter(suggestion, query)) {
					suggestions.add(suggestion);
				}
			}
		}

	}

}
