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
package fr.becpg.repo.autocomplete;

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

import fr.becpg.config.format.FormatMode;
import fr.becpg.config.format.PropertyFormats;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.helper.AttributeExtractorService;

/**
 * <p>UnitAwareCharactAutoCompleteExtractor class.</p>
 *
 * @author "Matthieu Laborie"
 * @version $Id: $Id
 */
@Service("unitAwareCharactAutoCompleteExtractor")
public class UnitAwareCharactAutoCompleteExtractor implements AutoCompleteExtractor<NodeRef> {

	@Autowired
	private NodeService nodeService;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private AttributeExtractorService attributeExtractorService;
	

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	/** {@inheritDoc} */
	@Override
	public List<AutoCompleteEntry> extract(List<NodeRef> nodeRefs) {
		
		List<AutoCompleteEntry> suggestions = new ArrayList<>();
		if (nodeRefs != null) {
			for (NodeRef nodeRef : nodeRefs) {

				QName type = nodeService.getType(nodeRef);
				String name = attributeExtractorService.extractPropName(type, nodeRef);
				String cssClass = attributeExtractorService.extractMetadata(type, nodeRef);
				Map<String, String> props = new HashMap<>(2);
				props.put("type", type.toPrefixString(namespaceService));

				String unit = "";
				String charactType = "";
				
				PropertyFormats formats = attributeExtractorService.getPropertyFormats(FormatMode.JSON, false);

				if (PLMModel.TYPE_NUT.equals(type)) {
					unit = (String) nodeService.getProperty(nodeRef, PLMModel.PROP_NUTUNIT);
				} else if (PLMModel.TYPE_PHYSICO_CHEM.equals(type)) {
					unit = (String) nodeService.getProperty(nodeRef, PLMModel.PROP_PHYSICO_CHEM_UNIT);
					charactType=  (String) nodeService.getProperty(nodeRef, PLMModel.PROP_PHYSICO_CHEM_TYPE);
					unit = attributeExtractorService.getStringValue(entityDictionaryService.getProperty( PLMModel.PROP_PHYSICO_CHEM_UNIT), unit,formats );
					charactType = attributeExtractorService.getStringValue(entityDictionaryService.getProperty( PLMModel.PROP_PHYSICO_CHEM_TYPE),charactType, formats);
				}else if (PLMModel.TYPE_MICROBIO.equals(type)) {
					charactType=  (String) nodeService.getProperty(nodeRef, PLMModel.PROP_MICROBIO_TYPE);
					charactType = attributeExtractorService.getStringValue(entityDictionaryService.getProperty( PLMModel.PROP_MICROBIO_TYPE), charactType, formats);
					
				}

				if ((unit != null) && !unit.isEmpty()) {
					name += " (" + unit + ")";
				}
				
				if ((charactType != null) && !charactType.isEmpty()) {
					name = "[" + charactType + "] "+name;
				}

				if (nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_COLOR)) {
					props.put("color", (String) nodeService.getProperty(nodeRef, BeCPGModel.PROP_COLOR));
				}

				AutoCompleteEntry entry = new AutoCompleteEntry(nodeRef.toString(), name, cssClass, props);

				suggestions.add(entry);

			}
		}
		return suggestions;
	}
	
	
	

}
