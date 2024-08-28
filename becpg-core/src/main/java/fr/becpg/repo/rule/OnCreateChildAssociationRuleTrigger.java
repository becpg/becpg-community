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

import java.util.Set;

import org.alfresco.model.DataListModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.rule.ruletrigger.RuleTriggerAbstractBase;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;

/**
 * A rule trigger for the creation of <b>secondary child associations</b>.
 * <p>
 * Policy names supported are:
 * <ul>
 *   <li>{@linkplain org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy}</li>
 * </ul>
 * 
 * @author Roy Wetherall
 */
public class OnCreateChildAssociationRuleTrigger
        extends RuleTriggerAbstractBase
        implements NodeServicePolicies.OnCreateChildAssociationPolicy
{
    /**
     * The logger
     */
    private static Log logger = LogFactory.getLog(OnCreateChildAssociationRuleTrigger.class);
	
	private static final String POLICY_NAME = "onCreateChildAssociation";
    
    private boolean isClassBehaviour = false;
    

	private boolean ignoreEntityDataList = true;


	
	
	public void setIgnoreEntityDataList(boolean ignoreEntityDataList) {
		this.ignoreEntityDataList = ignoreEntityDataList;
	}
		
	public void setIsClassBehaviour(boolean isClassBehaviour)
	{
		this.isClassBehaviour = isClassBehaviour;
	}
	
	/**
	 * @see org.alfresco.repo.rule.ruletrigger.RuleTrigger#registerRuleTrigger()
	 */
	public void registerRuleTrigger()
	{
		if (isClassBehaviour)
		{
			this.policyComponent.bindClassBehaviour(
					QName.createQName(NamespaceService.ALFRESCO_URI, POLICY_NAME), 
					this, 
					new JavaBehaviour(this, POLICY_NAME));
		}
		else
		{		
			this.policyComponent.bindAssociationBehaviour(
					QName.createQName(NamespaceService.ALFRESCO_URI, POLICY_NAME), 
					this, 
					new JavaBehaviour(this, POLICY_NAME));
		}
	}

    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNewNode)
    {
        // Break out early if rules are not enabled
        if (!areRulesEnabled())
        {
            return;
        }

        // Avoid new nodes
        if (isNewNode)
        {
            return;
        }

        NodeRef childNodeRef = childAssocRef.getChildRef();

        // Avoid renamed nodes
        Set<NodeRef> renamedNodeRefSet = TransactionalResourceHelper.getSet(RULE_TRIGGER_RENAMED_NODES);
        if (renamedNodeRefSet.contains(childNodeRef))
        {
            return;
        }
        
        if (ignoreEntityDataList && (dictionaryService.isSubClass(nodeService.getType(childNodeRef), BeCPGModel.TYPE_ENTITYLIST_ITEM) || 
				dictionaryService.isSubClass(nodeService.getType(childNodeRef), DataListModel.TYPE_DATALIST )	
				
				)) {
			return;
		}
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Single child assoc trigger (policy = " + POLICY_NAME + ") fired for parent node " + childAssocRef.getParentRef() + " and child node " + childAssocRef.getChildRef());
        }
        
    	triggerRules(childAssocRef.getParentRef(), childNodeRef);
    }
}
