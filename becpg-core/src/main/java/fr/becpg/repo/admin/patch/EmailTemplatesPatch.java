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

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Override alfresco email template
 */
public class EmailTemplatesPatch extends AbstractBeCPGPatch {

	private static Log logger = LogFactory.getLog(EmailTemplatesPatch.class);

	private static final String MSG_SUCCESS = "patch.bcpg.emailTemplatesPatch.result";

	@Override
	protected String applyInternal() throws Exception {

		NodeRef oldWFNodeRef = searchFolder("app:company_home/app:dictionary/app:email_templates/app:workflow/.");

		if (oldWFNodeRef != null) {
			logger.info("Delete old wf email template");
			nodeService.deleteNode(oldWFNodeRef);
		}

		updateResource("app:company_home/app:dictionary/app:email_templates/cm:invite/.",
				"classpath:beCPG/mails/invite/*.ftl");

		updateResource("app:company_home/app:dictionary/app:email_templates/cm:activities/.",
				"classpath:beCPG/mails/activities/*.ftl");

		updateResource("app:company_home/app:dictionary/app:email_templates/cm:workflownotification/.",
				"classpath:beCPG/mails/workflow/*.ftl");

		return I18NUtil.getMessage(MSG_SUCCESS);
	}

}
