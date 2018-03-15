/*******************************************************************************
 * Copyright (C) 2010-2018 beCPG.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.AttributeExtractorService;

/**
 *
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
@Service("charactValueExtractor")
public class CharactValueExtractor implements ListValueExtractor<NodeRef> {

	@Autowired
	private NodeService nodeService;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private AttributeExtractorService attributeExtractorService;

	@Override
	public List<ListValueEntry> extract(List<NodeRef> nodeRefs) {
		
		List<ListValueEntry> suggestions = new ArrayList<>();
		if (nodeRefs != null) {
			for (NodeRef nodeRef : nodeRefs) {

				QName type = nodeService.getType(nodeRef);
				String name = attributeExtractorService.extractPropName(type, nodeRef);
				String cssClass = attributeExtractorService.extractMetadata(type, nodeRef);
				Map<String, String> props = new HashMap<>(2);
				props.put("type", type.toPrefixString(namespaceService));

				String unit = "";

				if (PLMModel.TYPE_NUT.equals(type)) {
					unit = (String) nodeService.getProperty(nodeRef, PLMModel.PROP_NUTUNIT);
				} else if (PLMModel.TYPE_PHYSICO_CHEM.equals(type)) {
					unit = (String) nodeService.getProperty(nodeRef, PLMModel.PROP_PHYSICO_CHEM_UNIT);
				}

				if ((unit != null) && !unit.isEmpty()) {
					name += " (" + unit + ")";
				}

				if (nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_COLOR)) {
					props.put("color", (String) nodeService.getProperty(nodeRef, BeCPGModel.PROP_COLOR));
				}

				ListValueEntry entry = new ListValueEntry(nodeRef.toString(), name, cssClass, props);

				suggestions.add(entry);

			}
		}
		return suggestions;
	}
	
	
	

}
