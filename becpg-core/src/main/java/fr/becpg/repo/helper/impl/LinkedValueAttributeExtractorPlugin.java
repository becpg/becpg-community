/*
Copyright (C) 2010-2021 beCPG. 
 
This file is part of beCPG 
 
beCPG is free software: you can redistribute it and/or modify 
it under the terms of the GNU Lesser General Public License as published by 
the Free Software Foundation, either version 3 of the License, or 
(at your option) any later version. 
 
beCPG is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 

MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
GNU Lesser General Public License for more details. 
 
You should have received a copy of the GNU Lesser General Public License 
along with beCPG. If not, see <http://www.gnu.org/licenses/>.
*/
package fr.becpg.repo.helper.impl;

import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.AttributeExtractorService.AttributeExtractorPlugin;

/**
 * <p>LinkedValueAttributeExtractorPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class LinkedValueAttributeExtractorPlugin implements AttributeExtractorPlugin {

	
	@Autowired
	private NodeService nodeService;
	
	
	/** {@inheritDoc} */
	@Override
	public Collection<QName> getMatchingTypes() {
		return Collections.singletonList(BeCPGModel.TYPE_LINKED_VALUE);
	}
	

	/** {@inheritDoc} */
	@Override
	@Nonnull
	public String extractPropName(@Nonnull QName type, @Nonnull NodeRef nodeRef) {
		String value = (String) nodeService.getProperty(nodeRef, BeCPGModel.PROP_LKV_VALUE);
		return value != null ? value : "";
	}

	
	/** {@inheritDoc} */
	@Override
	@Nonnull
	public String extractMetadata(@Nonnull QName type, @Nonnull NodeRef nodeRef) {
		return BeCPGModel.PROP_LKV_VALUE.getLocalName() != null ? 
		       BeCPGModel.PROP_LKV_VALUE.getLocalName() : "";
	}


	/** {@inheritDoc} */
	@Override
	public Integer getPriority() {
		return HIGH_PRIORITY;
	}

	

}
