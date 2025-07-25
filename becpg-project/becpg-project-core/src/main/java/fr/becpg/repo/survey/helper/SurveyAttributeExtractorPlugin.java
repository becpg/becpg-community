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
package fr.becpg.repo.survey.helper;

import java.util.Arrays;
import java.util.Collection;

import javax.annotation.Nonnull;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.helper.AttributeExtractorService.AttributeExtractorPlugin;
import fr.becpg.repo.survey.SurveyModel;

/**
 * <p>SurveyAttributeExtractorPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class SurveyAttributeExtractorPlugin implements AttributeExtractorPlugin {

	@Autowired
	private NodeService nodeService;

	/** {@inheritDoc} */
	@Override
	@Nonnull
	public Collection<QName> getMatchingTypes() {
		return Arrays.asList(SurveyModel.TYPE_SURVEY_QUESTION);
	}

	/** {@inheritDoc} */
	@Override
	@Nonnull
	public String extractPropName(@Nonnull QName type, @Nonnull NodeRef nodeRef) {
		String label = (String) nodeService.getProperty(nodeRef, SurveyModel.PROP_SURVEY_QUESTION_LABEL);
		return label != null ? label : "";
	}

	/** {@inheritDoc} */
	@Override
	@Nonnull
	public String extractMetadata(@Nonnull QName type, @Nonnull NodeRef nodeRef) {
		String localName = SurveyModel.PROP_SURVEY_QUESTION_LABEL.getLocalName();
		return localName != null ? localName : "";
	}

	/** {@inheritDoc} */
	@Override
	public Integer getPriority() {
		return HIGH_PRIORITY;
	}

}
