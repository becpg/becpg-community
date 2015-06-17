/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package fr.becpg.repo.admin.patch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.repo.admin.patch.AbstractBeCPGPatch;

/**
 * Update claim mail templates
 */
public class ProjectEmailTemplatesPatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(ProjectEmailTemplatesPatch.class);

	private static final String MSG_SUCCESS = "patch.bcpg.project.projectEmailTemplatesPatch";

	@Override
	protected String applyInternal() throws Exception {

		logger.info("Updating project mail templates");
		
		updateResource("app:company_home/app:dictionary/app:email_templates/cm:project/.",
				"classpath:beCPG/mails/project/*.ftl");
		

		return I18NUtil.getMessage(MSG_SUCCESS);
	}

}
