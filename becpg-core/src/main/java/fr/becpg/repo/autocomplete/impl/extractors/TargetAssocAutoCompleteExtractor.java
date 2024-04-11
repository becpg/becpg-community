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
package fr.becpg.repo.autocomplete.impl.extractors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.favourites.FavouritesService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.autocomplete.AutoCompleteEntry;
import fr.becpg.repo.autocomplete.AutoCompleteExtractor;
import fr.becpg.repo.helper.AttributeExtractorService;

/**
 * Used to extract properties from product
 *
 * @author "Matthieu Laborie"
 * @version $Id: $Id
 */
@Service("targetAssocValueExtractor")
public class TargetAssocAutoCompleteExtractor implements AutoCompleteExtractor<NodeRef> {

	@Autowired
	private NodeService nodeService;

	@Autowired
	private NamespaceService namespaceService;
	
	@Autowired
	private FavouritesService favouritesService;
	
	@Autowired
	protected AttributeExtractorService attributeExtractorService;

	/** {@inheritDoc} */
	@Override
	public List<AutoCompleteEntry> extract(List<NodeRef> nodeRefs) {

		List<AutoCompleteEntry> suggestions = new ArrayList<>();
		if (nodeRefs != null) {
			for (NodeRef nodeRef : nodeRefs) {

				QName type = nodeService.getType(nodeRef);
				String name = attributeExtractorService.extractPropName(type,nodeRef);
				String cssClass = attributeExtractorService.extractMetadata(type,nodeRef);
				Map<String, String> props = new HashMap<>(2);
				props.put("type", type.toPrefixString(namespaceService));
				String userName = AuthenticationUtil.getFullyAuthenticatedUser();
				if(!AuthenticationUtil.SYSTEM_USER_NAME.equals(userName) && favouritesService.getType(nodeRef)!=null && favouritesService.isFavourite(userName, nodeRef)){
				   	cssClass += "  favourite";
				}
				
				if (nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_COLOR)) {
					props.put("color", (String) nodeService.getProperty(nodeRef, BeCPGModel.PROP_COLOR));
				}
				if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TITLED)) {
					props.put("title", (String) nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE));
					props.put("description", (String) nodeService.getProperty(nodeRef, ContentModel.PROP_DESCRIPTION));
				}

				AutoCompleteEntry entry = new AutoCompleteEntry(nodeRef.toString(), name, cssClass, props);

				suggestions.add(entry);

			}
		}
		return suggestions;
	}

}
