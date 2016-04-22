/*
Copyright (C) 2010-2016 beCPG. 
 
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
package fr.becpg.repo.helper;

import java.util.Collection;
import java.util.Collections;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.impl.CharactAttributeExtractorPlugin;

/**
 * @author matthieu
 * 
 */
@Service
public class LabelClaimExtractorPlugin extends CharactAttributeExtractorPlugin {


	@Override
	public Collection<QName> getMatchingTypes() {
		return Collections.singletonList(PLMModel.TYPE_LABEL_CLAIM);
	}

	@Override
	public String extractMetadata(QName type, NodeRef nodeRef) {
		 MLText tmp = (MLText) mlNodeService.getProperty(nodeRef, ContentModel.PROP_DESCRIPTION);
	     if(tmp!=null){
	    	return tmp.getClosestValue(I18NUtil.getContentLocale());
	     }
		return null;
		
	}

	@Override
	public Integer getPriority() {
		return 1;
	}

}
