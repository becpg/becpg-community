/*
 * 
 */
package fr.becpg.repo.product;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.repo.entity.version.BeCPGVersionMigrator;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.test.BeCPGTestHelper;
import fr.becpg.test.RepoBaseTestCase;

/**
 * The Class ProductVersionServiceTest.
 * 
 * @author querephi
 */
public class ProductVersionMigratorServiceTest extends RepoBaseTestCase {

	/** The logger. */
	private static Log logger = LogFactory.getLog(ProductVersionMigratorServiceTest.class);

	@Resource
	private CheckOutCheckInService checkOutCheckInService;

	@Resource
	private VersionService versionService;

	@Resource
	private EntityVersionService entityVersionService;
	
	@Resource
	private BeCPGVersionMigrator beCPGVersionMigrator;	
	
	@Resource(name = "dbNodeService")
	private NodeService dbNodeService;

	@Test
	@Deprecated
	public void migrateEntityVersionV1_3(){
		
		final String descr1 = "This is a test version 1";
		final String descr2 = "This is a test version 2";
		
		final NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			@Override
			public NodeRef execute() throws Throwable {
				
				NodeRef rawMaterialNodeRef = BeCPGTestHelper.createRawMaterial(testFolderNodeRef, "MP test report");
				Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>();
				aspectProperties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
				nodeService.addAspect(rawMaterialNodeRef, ContentModel.ASPECT_VERSIONABLE, aspectProperties);
				
				
				return rawMaterialNodeRef;
			}
		}, false, true);	
		
		 transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {
				
				//Check out
				NodeRef workingCopyNodeRef = checkOutCheckInService.checkout(rawMaterialNodeRef);			
				
				// check-in
				Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
				versionProperties.put(Version.PROP_DESCRIPTION, descr1);
				versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
				checkOutCheckInService.checkin(workingCopyNodeRef, versionProperties);
				
				//Check out
				workingCopyNodeRef = checkOutCheckInService.checkout(rawMaterialNodeRef);			
				
				// check-in
				versionProperties = new HashMap<String, Serializable>();
				versionProperties.put(Version.PROP_DESCRIPTION, descr2);
				versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
				checkOutCheckInService.checkin(workingCopyNodeRef, versionProperties);
				return workingCopyNodeRef;
				
			}},false,true);
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			@Override
			public NodeRef execute() throws Throwable {
				
				VersionHistory versionHistory = versionService.getVersionHistory(rawMaterialNodeRef);
				logger.info("check versionHistory 1");				
				checkVersionHistory(descr1, descr2, versionHistory);
				assertEquals(3, entityVersionService.getAllVersions(rawMaterialNodeRef).size());
				
				logger.info("versionHistory.getVersion('1.1') " + versionHistory.getVersion("1.1"));
				
				// delete versions in versionStore
				versionService.deleteVersionHistory(rawMaterialNodeRef);
		
				NodeRef entityVersionHistory = entityVersionService.getVersionHistoryNodeRef(rawMaterialNodeRef);
				assertEquals(3, nodeService.getChildAssocs(entityVersionHistory, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL, true).size());
				
				// Create versions in versionStore
				beCPGVersionMigrator.migrateVersionHistory();
				
				versionHistory = versionService.getVersionHistory(rawMaterialNodeRef);
				logger.info("check versionHistory 1");
				checkVersionHistory(descr1, descr2, versionHistory);
				
			return null;
			
			}},false,true);
	}
	
	private void checkVersionHistory(String descr1, String descr2, VersionHistory versionHistory){
		assertEquals(3, versionHistory.getAllVersions().size());
		int checks = 0;
		
		for(Version version : versionHistory.getAllVersions()){		
			logger.info("check version label " + version.getVersionLabel());
			if(version.getVersionLabel().equals("1.0")){
				checks++;
			}
			else if(version.getVersionLabel().equals("1.1")){
				checks++;
			}else if(version.getVersionLabel().equals("2.0")){
				checks++;
			}
		}
		
		assertEquals(3, checks);
	}

}
