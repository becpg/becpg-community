/*
Copyright (C) 2010-2014 beCPG. 
 
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
package fr.becpg.repo.action;

import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author matthieu
 *
 */
public class VersionCleanerActionExecuter extends ActionExecuterAbstractBase
{
    public static final String NAME = "version-cleaner";
    public static final String PARAM_VERSION_TYPE = "versionType";
    public static final String PARAM_NUMBER_OF_VERSION = "numberOfVersion";
    public static final String PARAM_NUMBER_OF_DAY = "numberOfDay";
    public static final String PARAM_NUMBER_BY_DAY = "numberByDay";
    
    private Log logger = LogFactory.getLog(VersionCleanerActionExecuter.class);
    
//    - Nombre de versions mineures à conserver
//    - Nombre de versions majeures à conserver
//    - Nombre de jours à conserver pour les versions mineures
//    - Nombre de jours à conserver pour les versions majeures
 

    
//     B) Nombre de versions à conserver, indépendamment de notions temporelles. 
//
//     C) Nombre de jours à conserver : 
//
//    o Nombre de versions par jour : 
//
//     Toutes. 
//
//     N (les N dernières) 
//
//     Une seule (la dernière saisie de la journée).
//
//     Si on choisit l’option C, les deux informations sont obligatoires (nombre de jours et nombre de versions). 
//
//     D) Conservation de toutes les versions. Cette option est prévue pour éventuellement annuler les effets d’un 
//
//    héritage des règles des niveaux supérieurs.
    
  
    private NodeService nodeService;
    private RuleService ruleService;
    private  VersionService versionService;
    

	public void setVersionService(VersionService versionService) {
		this.versionService = versionService;
	}


	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}


	public void setNodeService(NodeService nodeService) 
	{
		this.nodeService = nodeService;
	}
	
  

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuter#execute(org.alfresco.repo.ref.NodeRef, org.alfresco.repo.ref.NodeRef)
     */
    @Override
	public void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef)
    {
           boolean isLastAction = false;
        	for(Rule rule : ruleService.getRules(actionedUponNodeRef)) {
        		if(!rule.getRuleDisabled()) {
	        		if(rule.getAction().getActionDefinitionName().equals(ruleAction.getActionDefinitionName())) {
	        		   
	        			if(ruleAction.equals(rule.getAction())) {
	        				isLastAction = true;
	        		   } else {
	        			   isLastAction = false;
	        		   }
	        			
	        		}
        		}
        	}
        	logger.info("Action "+ruleAction.toString()+ " isLastAction "+ isLastAction);
//         VersionHistory versionHistory = versionService.getVersionHistory(actionedUponNodeRef);
//         for(Version version : versionHistory.getAllVersions()) {
//        	 logger.info("Version :"+version.toString());
//         }
         
    }

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
	{
		paramList.add(new ParameterDefinitionImpl(PARAM_VERSION_TYPE, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_VERSION_TYPE)));
		paramList.add(new ParameterDefinitionImpl(PARAM_NUMBER_BY_DAY, DataTypeDefinition.INT, false, getParamDisplayLabel(PARAM_NUMBER_BY_DAY)));
		paramList.add(new ParameterDefinitionImpl(PARAM_NUMBER_OF_DAY, DataTypeDefinition.INT, false, getParamDisplayLabel(PARAM_NUMBER_OF_DAY)));
		paramList.add(new ParameterDefinitionImpl(PARAM_NUMBER_OF_VERSION, DataTypeDefinition.INT, false, getParamDisplayLabel(PARAM_NUMBER_OF_VERSION)));
	}

}
