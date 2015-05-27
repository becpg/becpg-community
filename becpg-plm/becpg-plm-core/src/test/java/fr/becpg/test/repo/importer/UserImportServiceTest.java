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
package fr.becpg.test.repo.importer;

import java.io.IOException;
import java.util.Date;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import fr.becpg.repo.importer.user.UserImporterService;
import fr.becpg.test.PLMBaseTestCase;


public class UserImportServiceTest  extends PLMBaseTestCase {

	
	public static final String COMPANY_HOME_PATH_QUERY = "PATH:\"/app:company_home/.\"";
	

	/** The node service. */
	@Resource
	private SearchService searchService;
	

	@Resource
	UserImporterService userImporterService;

	private static Log logger = LogFactory.getLog(UserImportServiceTest.class);


	
	private NodeRef createCSV() throws IOException {
		
		   ResultSet resultSet = searchService.query( new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"),
				   SearchService.LANGUAGE_LUCENE, COMPANY_HOME_PATH_QUERY);
	       NodeRef destNodeRef = resultSet.getNodeRef(0);
		
		Date now = new Date();
		
		String qname = QName.createValidLocalName("importusercsv-"+now.getTime());
        ChildAssociationRef assocRef = nodeService.createNode(
        		destNodeRef,
              ContentModel.ASSOC_CONTAINS,
              QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, qname),
              ContentModel.TYPE_CONTENT);
        
        NodeRef nodeRef = assocRef.getChildRef();
        nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, qname);
        nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, qname);
        nodeService.setProperty(nodeRef, ContentModel.PROP_DESCRIPTION, qname);
    	ContentWriter writer = this.contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
		if (writer != null) {
			
			
			ClassPathResource resource = new ClassPathResource("beCPG/import/User.csv");
	    	
			writer.setMimetype("text");
			writer.putContent(resource.getInputStream());
			
			
        	if (logger.isDebugEnabled()) {
        		logger.debug("File successfully modified");
        	} 
		} else {
			logger.error("Cannot write node");
		}
        
		return nodeRef;
	}
	
	@Test
	public void testImportUserCSV(){
	 transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
 			public NodeRef execute() throws Throwable {
 				NodeRef csv  = createCSV();
 				userImporterService.importUser(csv);
 				Assert.assertEquals(1, wiser.getMessages().size());
 				return null;

 			}},false,true);
 			
		
	}
	
}
