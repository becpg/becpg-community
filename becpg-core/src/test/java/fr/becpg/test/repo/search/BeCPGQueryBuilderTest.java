/*
Copyright (C) 2010-2015 beCPG. 
 
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
package fr.becpg.test.repo.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * @author matthieu
 *
 */
public  class BeCPGQueryBuilderTest {
	
	private static final Log logger = LogFactory.getLog(BeCPGQueryBuilderTest.class);
	
	@Test
	public void testQueryGeneration() {
		
		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().excludeDefaults();
		
		logger.info(queryBuilder.toString());
		
		queryBuilder = BeCPGQueryBuilder.createQuery()
		.ofType(BeCPGModel.TYPE_ENTITY_V2)
		.isNotNull(BeCPGModel.PROP_CODE)
		.excludeAspect(BeCPGModel.ASPECT_ENTITY_TPL)
		.withAspect(BeCPGModel.ASPECT_COMPOSITE_VERSION)
		.addSort(BeCPGModel.PROP_CODE,true);
		
//		List<NodeRef> ret = BeCPGQueryBuilder.createQuery()
//				.ofType(ContentModel.TYPE_FOLDER)
//				.isNotNull(ContentModel.PROP_MODIFIED)
//				.excludeAspect(ContentModel.ASPECT_WORKING_COPY)
//				.withAspect(ContentModel.ASPECT_TITLED)
//				.addSort(ContentModel.PROP_MODIFIED,true).list();
//
//List<NodeRef> ret = BeCPGQueryBuilder.createQuery()
//	.ofType(ContentModel.TYPE_FOLDER).maxResults(RepoConsts.MAX_RESULTS_UNLIMITED).list();
//
//NodeRef nodeRef  = BeCPGQueryBuilder.createQuery()
//	.ofType(ContentModel.TYPE_FOLDER)
//	.andPropEquals(ContentModel.PROP_NAME, "Test").singleValue();

		
	
		System.out.println(queryBuilder.toString());
		
//		BeCPGQueryBuilder.createQuery()
//		.parent(listContainer)
//		.andBetween(BeCPGModel.PROP_SORT, String.valueOf(startSort + 1), "MAX")
//		.andBetween(BeCPGModel.PROP_DEPTH_LEVEL, "1", Integer.toString(level))
//		.isNotNull(BeCPGModel.PROP_SORT).addSort(BeCPGModel.PROP_SORT, true).singleValue();

		
	}

}
