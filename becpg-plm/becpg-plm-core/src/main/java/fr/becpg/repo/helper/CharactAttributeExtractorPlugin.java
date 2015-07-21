/*
Copyright (C) 2010-2015 beCPG. 
 
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

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityDictionaryService;

/**
 * @author matthieu
 * 
 */
@Service
public class CharactAttributeExtractorPlugin extends AbstractExprNameExtractor {

	@Value("${beCPG.charact.name.format}")
	private String charactNameFormat;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	@Autowired
	private NamespaceService namespaceService;

	@Override
	public Collection<QName> getMatchingTypes() {
		return entityDictionaryService.getSubTypes(PLMModel.TYPE_CHARACT);
	}

	@Override
	public String extractPropName(QName type, NodeRef nodeRef) {
		return extractExpr(nodeRef, charactNameFormat);
	}

	@Override
	public String extractMetadata(QName type, NodeRef nodeRef) {
		return type.toPrefixString(namespaceService).split(":")[1];
	}

}
