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
package fr.becpg.repo.helper;

import java.util.Collection;

import javax.annotation.Nonnull;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.helper.impl.AbstractExprNameExtractor;


/**
 * <p>VariantAttributeExtractorPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class VariantAttributeExtractorPlugin extends AbstractExprNameExtractor {

	@Autowired
	private EntityDictionaryService entityDictionaryService;


	/** {@inheritDoc} */
	@Override
	public Collection<QName> getMatchingTypes() {
		return entityDictionaryService.getSubTypes(BeCPGModel.TYPE_VARIANT);
	}

	/** {@inheritDoc} */
	@Override
	@Nonnull
	public String extractPropName(@Nonnull QName type, @Nonnull NodeRef nodeRef) {
		String name = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
		return name != null ? name : "";
	}

	/** {@inheritDoc} */
	@Override
	@Nonnull
	public String extractMetadata(@Nonnull QName type, @Nonnull NodeRef nodeRef) {
		Boolean isDefault = (Boolean) nodeService.getProperty(nodeRef, BeCPGModel.PROP_IS_DEFAULT_VARIANT);
		String color = (String) nodeService.getProperty(nodeRef, BeCPGModel.PROP_COLOR);
		StringBuilder result = new StringBuilder();
		result.append(Boolean.TRUE.equals(isDefault) ? "variant-default" : "variant");
		if (color != null && color.startsWith("#")) {
		    result.append(color);
		}
		return result.toString();
	}

}
