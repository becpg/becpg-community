/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package fr.becpg.repo.rule;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.alfresco.model.DataListModel;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.rule.ruletrigger.RuleTriggerAbstractBase;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;

/**
 * <p>SingleAssocRefPolicyRuleTrigger class.</p>
 *
 * @author matthieu
 */
public class SingleAssocRefPolicyRuleTrigger extends RuleTriggerAbstractBase {
	private static Log logger = LogFactory.getLog(SingleAssocRefPolicyRuleTrigger.class);

	private String policyNamespace = NamespaceService.ALFRESCO_URI;
	private String policyName;
	private Set<QName> excludedAssocTypes = Collections.emptySet();

	private boolean ignoreEntityDataList = true;

	/**
	 * <p>Setter for the field <code>policyNamespace</code>.</p>
	 *
	 * @param policyNamespace a {@link java.lang.String} object
	 */
	public void setPolicyNamespace(String policyNamespace) {
		this.policyNamespace = policyNamespace;
	}

	/**
	 * <p>Setter for the field <code>policyName</code>.</p>
	 *
	 * @param policyName a {@link java.lang.String} object
	 */
	public void setPolicyName(String policyName) {
		this.policyName = policyName;
	}

	/**
	 * <p>setExcludedAssociationTypes.</p>
	 *
	 * @param assocTypes a {@link java.util.Set} object
	 */
	public void setExcludedAssociationTypes(Set<QName> assocTypes) {
		this.excludedAssocTypes = assocTypes;
	}

	/**
	 * <p>Setter for the field <code>ignoreEntityDataList</code>.</p>
	 *
	 * @param ignoreEntityDataList a boolean
	 */
	public void setIgnoreEntityDataList(boolean ignoreEntityDataList) {
		this.ignoreEntityDataList = ignoreEntityDataList;
	}

	/**
	 * <p>registerRuleTrigger.</p>
	 *
	 * @see org.alfresco.repo.rule.ruletrigger.RuleTrigger#registerRuleTrigger()
	 */
	public void registerRuleTrigger() {
		PropertyCheck.mandatory(this, "policyNamespace", policyNamespace);
		PropertyCheck.mandatory(this, "policyName", policyName);

		this.policyComponent.bindAssociationBehaviour(QName.createQName(this.policyNamespace, this.policyName), this,
				new JavaBehaviour(this, "policyBehaviour"));
	}

	/**
	 * <p>policyBehaviour.</p>
	 *
	 * @param assocRef a {@link org.alfresco.service.cmr.repository.AssociationRef} object
	 */
	public void policyBehaviour(AssociationRef assocRef) {
		//beCPG Break out early if rules are not enabled
		if (!areRulesEnabled()) {
			return;
		}
		final QName assocTypeQName = assocRef.getTypeQName();
		if (!excludedAssocTypes.contains(assocTypeQName)) {
			NodeRef nodeRef = assocRef.getSourceRef();

			if (nodeService.exists(nodeRef)) {

				if (ignoreEntityDataList && (dictionaryService.isSubClass(nodeService.getType(nodeRef), BeCPGModel.TYPE_ENTITYLIST_ITEM)
						|| dictionaryService.isSubClass(nodeService.getType(nodeRef), DataListModel.TYPE_DATALIST)

				)) {
					return;
				}

				List<ChildAssociationRef> parentsAssocRefs = this.nodeService.getParentAssocs(nodeRef);
				for (ChildAssociationRef parentAssocRef : parentsAssocRefs) {
					triggerRules(parentAssocRef.getParentRef(), nodeRef);
					if (logger.isDebugEnabled()) {
						logger.debug("OnUpdateAssoc " + assocTypeQName + " rule triggered (parent); " + "nodeRef=" + parentAssocRef.getParentRef());
					}
				}
			}
		}
	}
}
