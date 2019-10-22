/*******************************************************************************
 * Copyright (C) 2010-2018 beCPG. 
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
package fr.becpg.tools.http;

public class ListWorkflowsCommand extends AbstractHttpCommand {

	///alfresco/service/api/workflow-instances?state={state?}&initiator={initiator?}&priority={priority?}&dueBefore={dueBefore?}&dueAfter={dueAfter?}&definitionId={definitionId?}&definitionName={definitionName?}&startedBefore={startedBefore?}&startedAfter={startedAfter?}&completedBefore={completedBefore?}&completedAfter={completedAfter?}&maxItems={maxItems?}&skipCount={skipCount?}&exclude={exclude?}
	

	private static final String COMMAND_URL_TEMPLATE = "/api/workflow-instances?%s";

	
	public ListWorkflowsCommand(String serverUrl) {
		super(serverUrl);
	}

	@Override
	public String getHttpUrl(Object... params) {
		return getServerUrl() + String.format(COMMAND_URL_TEMPLATE, buildQueryString(params) );
	}

	
}
