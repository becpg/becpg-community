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

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import javax.annotation.Nonnull;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.helper.impl.AbstractExprNameExtractor;

/**
 * <p>ListValueHeritedNameExtractorPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class ListValueHeritedNameExtractorPlugin extends AbstractExprNameExtractor {

	@Autowired
	@Qualifier("mlAwareNodeService")
	private NodeService mlNodeService;

	

	/** {@inheritDoc} */
	@Override
	public Collection<QName> getMatchingTypes() {
		return Arrays.asList(PLMModel.TYPE_ING_TYPE_ITEM,PackModel.TYPE_PACKAGING_MATERIAL);
	}

	/** {@inheritDoc} */
	@Override
	@Nonnull
	public String extractPropName(@Nonnull QName type, @Nonnull NodeRef nodeRef) {
		MLText tmp = (MLText) mlNodeService.getProperty(nodeRef, BeCPGModel.PROP_LV_VALUE);
		Locale locale = I18NUtil.getContentLocale();
		String value = MLTextHelper.getClosestValue(tmp, locale);
		return value != null ? value : "";
	}


	/** {@inheritDoc} */
	@Override
	@Nonnull
	public String extractMetadata(@Nonnull QName type, @Nonnull NodeRef nodeRef) {
		String[] parts = type.toPrefixString(namespaceService).split(":");
		return parts.length > 1 ? parts[1] : "";
	}


}
