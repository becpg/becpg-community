/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG.
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
package fr.becpg.repo.jscript;

import java.util.Locale;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.quickshare.QuickShareService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.chemistry.opencmis.commons.impl.jaxb.PolicyService;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.repo.entity.AutoNumService;
import fr.becpg.repo.olap.OlapService;

/**
 * Utility script methods
 * 
 * @author matthieu
 *
 */
public final class BeCPGScriptHelper extends BaseScopableProcessorExtension {

	private AutoNumService autoNumService;

	private OlapService olapService;

	private QuickShareService quickShareService;

	private NodeService mlNodeService;

	private NamespaceService namespaceService;
	
	private BehaviourFilter policyBehaviourFilter;

	public void setOlapService(OlapService olapService) {
		this.olapService = olapService;
	}

	public void setAutoNumService(AutoNumService autoNumService) {
		this.autoNumService = autoNumService;
	}

	public void setQuickShareService(QuickShareService quickShareService) {
		this.quickShareService = quickShareService;
	}

	public String getOrCreateBeCPGCode(ScriptNode sourceNode) {
		return autoNumService.getOrCreateBeCPGCode(sourceNode.getNodeRef());
	}

	public void shareContent(ScriptNode sourceNode) {
		quickShareService.shareContent(sourceNode.getNodeRef());
	}
	
	public void setMlNodeService(NodeService mlNodeService) {
		this.mlNodeService = mlNodeService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}
	
	

	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	public String getMLProperty(ScriptNode sourceNode, String propQName, String locale) {
		MLText mlText = (MLText) mlNodeService.getProperty(sourceNode.getNodeRef(), QName.createQName(propQName, namespaceService));
		if (mlText != null) {
			return mlText.getClosestValue(new Locale(locale));
		}
		return null;
	}

	public void setMLProperty(ScriptNode sourceNode, String propQName, String locale, String value) {
		
		try {
			policyBehaviourFilter.disableBehaviour(sourceNode.getNodeRef());
		
			MLText mlText = (MLText) mlNodeService.getProperty(sourceNode.getNodeRef(), QName.createQName(propQName, namespaceService));
			if (mlText == null) {
				mlText = new MLText();
			}
			if ((value != null) && !value.isEmpty()) {
				mlText.addValue(new Locale(locale), value);
			} else {
				mlText.removeValue(new Locale(locale));
			}
			mlNodeService.setProperty(sourceNode.getNodeRef(), QName.createQName(propQName, namespaceService), mlText);
		
		} finally {
			policyBehaviourFilter.enableBehaviour(sourceNode.getNodeRef());
		}

	}

	public String getMessage(String messageKey) {
		return I18NUtil.getMessage(messageKey, Locale.getDefault());
	}

	public String getMessage(String messageKey, Object param) {
		return I18NUtil.getMessage(messageKey, param, Locale.getDefault());
	}

	public String getOlapSSOUrl() {
		return olapService.getSSOUrl();
	}

}
