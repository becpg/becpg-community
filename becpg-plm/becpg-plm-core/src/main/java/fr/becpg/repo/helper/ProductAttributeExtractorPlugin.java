/*
Copyright (C) 2010-2014 beCPG. 
 
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.helper.AttributeExtractorService.AttributeExtractorPlugin;

/**
 * @author matthieu
 * 
 */
@Service
public class ProductAttributeExtractorPlugin implements AttributeExtractorPlugin {

	@Value("${beCPG.product.name.format}")
	private String productNameFormat;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	@Autowired
	private NamespaceService namespaceService;

	@Override
	public Collection<QName> getMatchingTypes() {
		return entityDictionaryService.getSubTypes(PLMModel.TYPE_PRODUCT);
	}

	@Override
	public String extractPropName(QName type, NodeRef nodeRef) {

		Matcher patternMatcher = Pattern.compile("\\{([^}]+)\\}").matcher(productNameFormat);
		StringBuffer sb = new StringBuffer();
		while (patternMatcher.find()) {

			String propQname = patternMatcher.group(1);
			String replacement = "";
			if(propQname.contains("|")){
			 for(String propQnameAlt : propQname.split("\\|")){
				 replacement = (String) nodeService.getProperty(nodeRef,
							QName.createQName(propQnameAlt, namespaceService));
				 if(replacement!=null && !replacement.isEmpty()){
					 break;
				 }
			 }
				
			} else {
				replacement = (String) nodeService.getProperty(nodeRef,
						QName.createQName(propQname, namespaceService));
			}

			patternMatcher.appendReplacement(sb, replacement != null ? replacement : "");

		}
		patternMatcher.appendTail(sb);
		return sb.toString();

	}

	@Override
	public String extractMetadata(QName type, NodeRef nodeRef) {
		// TODO task state
		return type.toPrefixString(namespaceService).split(":")[1] + "-"
				+ nodeService.getProperty(nodeRef, PLMModel.PROP_PRODUCT_STATE);
	}

}
