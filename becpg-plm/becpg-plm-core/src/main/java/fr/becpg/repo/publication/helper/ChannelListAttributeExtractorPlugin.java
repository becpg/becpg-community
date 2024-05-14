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
package fr.becpg.repo.publication.helper;

import java.util.Collection;
import java.util.Collections;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.PublicationModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService.AttributeExtractorPlugin;

/**
 * <p>ProductAttributeExtractorPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class ChannelListAttributeExtractorPlugin implements AttributeExtractorPlugin {

	
	@Autowired
	private  NamespaceService namespaceService;	
	
	
	@Autowired
	private AssociationService associationService;
	

	@Autowired
	private NodeService nodeService;
	
	
	/** {@inheritDoc} */
	@Override
	public String extractPropName(QName type, NodeRef nodeRef) {
		
		NodeRef channel = associationService.getTargetAssoc(nodeRef, PublicationModel.ASSOC_PUBCHANNELLIST_CHANNEL);
		
		if(channel!=null){
			return (String) nodeService.getProperty(channel, ContentModel.PROP_NAME);
		}
		
		return type.toPrefixString();
	}



	/** {@inheritDoc} */
	@Override
	public Collection<QName> getMatchingTypes() {
		return Collections.singletonList(PublicationModel.TYPE_PUBLICATION_CHANNEL_LIST);
	}

	/** {@inheritDoc} */
	@Override
	public String extractMetadata(QName type, NodeRef nodeRef) {
		return type.toPrefixString(namespaceService).split(":")[1];
	}



	@Override
	public Integer getPriority() {
		return 0;
	}

}
