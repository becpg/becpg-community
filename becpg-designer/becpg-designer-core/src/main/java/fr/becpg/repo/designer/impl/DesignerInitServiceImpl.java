/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG. 
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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG.
 *  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.designer.impl;

import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.repo.designer.DesignerInitService;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 * 
 */
@Service("designerInitService")
public class DesignerInitServiceImpl implements DesignerInitService {


	private static final String PATH_MODELS = "./app:dictionary/app:models";

	private static final String PATH_WORKFLOWS = "./app:dictionary/app:workflow_defs";
	
	private static final String PATH_CONFIGS = "./app:dictionary/cm:configs";
	

	@Autowired
	@Qualifier("repositoryHelper")
	private Repository repository;

	@Override
	public NodeRef getWorkflowsNodeRef() {

		return BeCPGQueryBuilder.createQuery().selectNodeByPath(repository.getCompanyHome(), PATH_WORKFLOWS);
	}

	@Override
	public NodeRef getModelsNodeRef() {

		return BeCPGQueryBuilder.createQuery().selectNodeByPath(repository.getCompanyHome(), PATH_MODELS);
	}

	@Override
	public NodeRef getConfigsNodeRef() {

		return BeCPGQueryBuilder.createQuery().selectNodeByPath(repository.getCompanyHome(), PATH_CONFIGS);
		
	}

}
