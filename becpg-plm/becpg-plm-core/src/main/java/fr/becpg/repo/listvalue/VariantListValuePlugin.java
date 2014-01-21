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
package fr.becpg.repo.listvalue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.listvalue.impl.EntityListValuePlugin;
import fr.becpg.repo.listvalue.impl.NodeRefListValueExtractor;

@Deprecated //TODO use assocService instead of nodeService.getChildAssocs
public class VariantListValuePlugin extends EntityListValuePlugin {

	private static Log logger = LogFactory.getLog(VariantListValuePlugin.class);

	private static final String SOURCE_TYPE_VARIANT_LIST = "variantList";

	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_VARIANT_LIST };
	}

	@Override
	public ListValuePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {

		NodeRef entityNodeRef = new NodeRef((String) props.get(ListValueService.PROP_NODEREF));
		logger.debug("VariantListValuePlugin sourceType: " + sourceType + " - entityNodeRef: " + entityNodeRef);

		List<NodeRef> ret = new ArrayList<NodeRef>();

		List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(entityNodeRef, BeCPGModel.ASSOC_VARIANTS, RegexQNamePattern.MATCH_ALL);
		for (ChildAssociationRef childAssoc : childAssocs) {
			ret.add(childAssoc.getChildRef());
		}
		return new ListValuePage(ret, pageNum, pageSize, new NodeRefListValueExtractor(ContentModel.PROP_NAME, nodeService));

	}

}
