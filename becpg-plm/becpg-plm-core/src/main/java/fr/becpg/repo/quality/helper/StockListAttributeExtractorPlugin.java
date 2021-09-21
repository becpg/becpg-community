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
package fr.becpg.repo.quality.helper;

import java.util.Collection;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.model.QualityModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.helper.impl.AbstractExprNameExtractor;

/**
 * <p>ProductAttributeExtractorPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class StockListAttributeExtractorPlugin extends AbstractExprNameExtractor {

	
	@Autowired
	private EntityDictionaryService entityDictionaryService;


	/** {@inheritDoc} */
	@Override
	public Collection<QName> getMatchingTypes() {
		return entityDictionaryService.getSubTypes(QualityModel.TYPE_STOCK_LIST);
	}

	/** {@inheritDoc} */
	@Override
	public String extractPropName(QName type, NodeRef nodeRef) {
		return  (String) nodeService.getProperty(nodeRef, QualityModel.PROP_BATCH_ID);

	}

	/** {@inheritDoc} */
	@Override
	public String extractMetadata(QName type, NodeRef nodeRef) {
		return type.toPrefixString(namespaceService).split(":")[1];
	}

}
