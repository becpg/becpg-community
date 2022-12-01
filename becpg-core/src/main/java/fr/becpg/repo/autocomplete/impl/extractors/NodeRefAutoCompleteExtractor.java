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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.autocomplete.AutoCompleteEntry;
import fr.becpg.repo.autocomplete.AutoCompleteExtractor;

/**
 * Used to extract properties from nodeRef
 *
 * @author "Matthieu Laborie"
 * @version $Id: $Id
 */
public class NodeRefAutoCompleteExtractor implements AutoCompleteExtractor<NodeRef> {

	private final Set<QName> propNames;
	
	private final NodeService nodeService;
	
	

	/**
	 * <p>Constructor for NodeRefListValueExtractor.</p>
	 *
	 * @param propName a {@link org.alfresco.service.namespace.QName} object.
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public NodeRefAutoCompleteExtractor(QName propName,NodeService nodeService) {
		super();
		propNames  = new HashSet<>();
		propNames.add(propName);
		this.nodeService = nodeService;
	}
	
	/**
	 * <p>Constructor for NodeRefListValueExtractor.</p>
	 *
	 * @param propNames a {@link java.util.Set} object.
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public NodeRefAutoCompleteExtractor(Set<QName> propNames,NodeService nodeService) {
		super();
		this.propNames = propNames;
		this.nodeService = nodeService;
	}


	/** {@inheritDoc} */
	@Override
	public List<AutoCompleteEntry> extract(List<NodeRef> nodeRefs) {
		List<AutoCompleteEntry> suggestions = new ArrayList<>();
    	if(nodeRefs!=null){
    		for(NodeRef nodeRef : nodeRefs){
    			
    			String name = "";
    			
    			for(QName propName : propNames) {
    				if(!name.isEmpty()) {
    					name = name.trim()+" ";
    				}
    				name += (String)nodeService.getProperty(nodeRef, propName);
    			}
    			suggestions.add(new AutoCompleteEntry(nodeRef.toString(),name.trim(),nodeService.getType(nodeRef).getLocalName()));
    		}
    	}
		return suggestions;
	}
	
}
