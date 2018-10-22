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

public class ListWorkflowTasksCommand extends AbstractHttpCommand {

	//GET /alfresco/service//{workflow_instance_id}/task-instances?authority={authority?}&state={state?}&priority={priority?}&dueBefore={isoDate?}&dueAfter={isoDate?}&properties={prop1, prop2, prop3...?}&maxItems={maxItems?}&skipCount={skipCount?}&exclude={exclude?}

	private static final String COMMAND_URL_TEMPLATE = "/api/workflow-instances/%s?includeTasks=true";

	public ListWorkflowTasksCommand(String serverUrl) {
		super(serverUrl);
	}

	@Override
	public String getHttpUrl(Object... params) {
	
		return getServerUrl() + String.format(COMMAND_URL_TEMPLATE, encodeParams(params) );
	}
	
	
}
