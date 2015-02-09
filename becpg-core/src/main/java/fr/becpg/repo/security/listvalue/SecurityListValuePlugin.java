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
package fr.becpg.repo.security.listvalue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.listvalue.ListValueEntry;
import fr.becpg.repo.listvalue.ListValueExtractor;
import fr.becpg.repo.listvalue.ListValuePage;
import fr.becpg.repo.listvalue.ListValuePlugin;

/**
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 * 
 */
@Service
public class SecurityListValuePlugin implements ListValuePlugin {

	private static String TYPE_ACL_TYPE = "aclType";

	private static String SEPARATOR = "|";

	@Autowired
	private ServiceRegistry serviceRegistry;

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

		addSuggestions(serviceRegistry.getDictionaryService().getSubTypes(BeCPGModel.TYPE_ENTITY_V2, true), suggestions, query);
		addSuggestions(serviceRegistry.getDictionaryService().getSubTypes(BeCPGModel.TYPE_ENTITYLIST_ITEM, true), suggestions, query);

		return new ListValuePage(suggestions, pageNum, pageSize, new StringValueExtractor("modelType"));
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
