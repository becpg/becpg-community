/*
Copyright (C) 2010-2017 beCPG. 
 
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
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.helper.impl.AbstractExprNameExtractor;

/**
 * @author matthieu
 * 
 */
@Service
public class ProductAttributeExtractorPlugin extends AbstractExprNameExtractor {

	@Value("${beCPG.product.name.format}")
	private String productNameFormat;


	public void setProductNameFormat(String productNameFormat) {
		this.productNameFormat = productNameFormat;
	}
	
	public String getProductNameFormat() {
		return productNameFormat;
	}

	@Autowired
	private EntityDictionaryService entityDictionaryService;


	@Override
	public Collection<QName> getMatchingTypes() {
		return entityDictionaryService.getSubTypes(PLMModel.TYPE_PRODUCT);
	}

	@Override
	public String extractPropName(QName type, NodeRef nodeRef) {
		return extractExpr(nodeRef,productNameFormat);
	}

	@Override
	public String extractMetadata(QName type, NodeRef nodeRef) {
		String ret = type.toPrefixString(namespaceService).split(":")[1] + "-" + nodeService.getProperty(nodeRef, PLMModel.PROP_PRODUCT_STATE);
		if (nodeService.hasAspect(nodeRef, PLMModel.ASPECT_NUTRIENT_PROFILING_SCORE)) {
			String nutClass = (String) nodeService.getProperty(nodeRef, PLMModel.PROP_NUTRIENT_PROFILING_CLASS);
			if (nutClass != null && !nutClass.isEmpty() && nutClass.length() < 5) {
				ret += " nutrientClass-" + nutClass;
			}
		}
		return ret;
	}

}
