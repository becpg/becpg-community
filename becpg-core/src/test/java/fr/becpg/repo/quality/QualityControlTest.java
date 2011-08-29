package fr.becpg.repo.quality;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import fr.becpg.model.QualityModel;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.quality.data.ControlPlanData;
import fr.becpg.repo.quality.data.ControlPointData;
import fr.becpg.repo.quality.data.QualityControlData;
import fr.becpg.repo.quality.data.dataList.ControlDefListDataItem;
import fr.becpg.repo.quality.data.dataList.SamplingDefListDataItem;
import fr.becpg.repo.quality.data.dataList.SamplingListDataItem;
import fr.becpg.test.RepoBaseTestCase;

public class QualityControlTest extends RepoBaseTestCase {

	private static String PATH_TESTFOLDER = "QualTestFolder";       
	private static final long HOUR = 3600*1000; // in milli-seconds.
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(QualityControlTest.class);
	
	/** The app ctx. */
	private static ApplicationContext appCtx = ApplicationContextHelper.getApplicationContext();
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The file folder service. */
	private FileFolderService fileFolderService;
	
	/** The product dao. */
	private ProductDAO productDAO;
	
	private ControlPointDAO controlPointDAO;
	private ControlPlanDAO controlPlanDAO;
	private QualityControlDAO qualityControlDAO;
	private WorkItemAnalysisDAO workItemAnalysisDAO;
	private AuthorityService authorityService;
	private QualityControlService qualityControlService;
	
	/** The repository helper. */
	private Repository repositoryHelper;  
	
	private NodeRef controlStepNodeRef;
	private NodeRef methodNodeRef;
	private NodeRef controlPointNodeRef;
	private NodeRef qualityControlNodeRef;
	private NodeRef controlPlanNodeRef;
	
	/* (non-Javadoc)
	 * @see org.alfresco.util.BaseAlfrescoTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {		
		super.setUp();	
		
    	logger.debug("ProductServiceTest:setUp");
    
    	nodeService = (NodeService)appCtx.getBean("nodeService");
    	fileFolderService = (FileFolderService)appCtx.getBean("fileFolderService");  
    	productDAO = (ProductDAO)appCtx.getBean("productDAO");
    	repositoryHelper = (Repository)appCtx.getBean("repositoryHelper");
    	controlPointDAO = (ControlPointDAO)appCtx.getBean("controlPointDAO");
    	controlPlanDAO = (ControlPlanDAO)appCtx.getBean("controlPlanDAO");
    	qualityControlDAO = (QualityControlDAO)appCtx.getBean("qualityControlDAO");
    	workItemAnalysisDAO = (WorkItemAnalysisDAO)appCtx.getBean("workItemAnalysisDAO");
    	authorityService = (AuthorityService)appCtx.getBean("authorityService");
    	qualityControlService = (QualityControlService)appCtx.getBean("qualityControlService");
    }
    
	/* (non-Javadoc)
	 * @see org.alfresco.util.BaseAlfrescoTestCase#tearDown()
	 */
	@Override
    public void tearDown() throws Exception
    {
		try
        {
            authenticationComponent.clearCurrentSecurityContext();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            // Don't let this mask any previous exceptions
        }
        super.tearDown();

    }
	
	private void createControlPlan(NodeRef folderNodeRef){
		
		// create method		
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		String name = "Method";
		properties.put(ContentModel.PROP_NAME, name);
		methodNodeRef = nodeService.createNode(folderNodeRef, 
									ContentModel.ASSOC_CONTAINS, 
									QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), 
									QualityModel.TYPE_CONTROL_METHOD, properties).getChildRef();								
		
		// create control step
		properties.clear();
		name = "Step";
		properties.put(ContentModel.PROP_NAME, name);
		controlStepNodeRef = nodeService.createNode(folderNodeRef, 
									ContentModel.ASSOC_CONTAINS, 
									QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), 
									QualityModel.TYPE_CONTROL_STEP, properties).getChildRef();
		
		// create control point
		ControlPointData controlPointData = new ControlPointData();
		controlPointData.setName("Control point");
		List<ControlDefListDataItem> controlDefList = new ArrayList<ControlDefListDataItem>();
		controlDefList.add(new ControlDefListDataItem(null, "Nutritionnelle", null, null, true, methodNodeRef, nuts));
		controlPointData.setControlDefList(controlDefList);
		controlPointNodeRef = controlPointDAO.create(folderNodeRef, controlPointData);
		
//		// create group
//		Set<String> zones = new HashSet<String>();
//		zones.add(AuthorityService.ZONE_APP_DEFAULT);
//		zones.add(AuthorityService.ZONE_APP_SHARE);
//		zones.add(AuthorityService.ZONE_AUTH_ALFRESCO);
//		String group = "groupQual";			
//		
//		if(!authorityService.authorityExists(PermissionService.GROUP_PREFIX + group)){
//			logger.debug("create group: " + group);				
//			authorityService.createAuthority(AuthorityType.GROUP, group, group, zones);				
//		}
//		NodeRef controlingGroup = authorityService.getA
				
		// create control plan
		ControlPlanData controlPlanData = new ControlPlanData();
		controlPlanData.setName("Control plan");
		List<SamplingDefListDataItem> samplingDefList = new ArrayList<SamplingDefListDataItem>();
		samplingDefList.add(new SamplingDefListDataItem(null, 2, 1, "/4heures", controlPointNodeRef, controlStepNodeRef, null));
		controlPlanData.setSamplingDefList(samplingDefList);
		controlPlanNodeRef = controlPlanDAO.create(folderNodeRef, controlPlanData); 
	}
	
	private void createQualityControl(NodeRef folderNodeRef, List<NodeRef> controlPlansNodeRef, NodeRef productNodeRef){
		
		QualityControlData qualityControlData = new QualityControlData();
		qualityControlData.setName("Quality control");
		qualityControlData.setBatchStart(new Date());
		qualityControlData.setBatchDuration(8);
		qualityControlData.setBatchId("12247904");
		qualityControlData.setOrderId("2394744");
		qualityControlData.setProduct(productNodeRef);
		qualityControlData.setControlPlans(controlPlansNodeRef);		
		
		qualityControlNodeRef = qualityControlDAO.create(folderNodeRef, qualityControlData);
		
		//TODO : add a policy...
		//qualityControlService.createSamplingList(qualityControlNodeRef, controlPlansNodeRef.get(0));
		
	}
	
	public void testCreateQualityControl(){
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>(){
			@Override
			public NodeRef execute() throws Throwable {
				
				/*-- Create test folder --*/
				NodeRef folderNodeRef = nodeService.getChildByName(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS, PATH_TESTFOLDER);			
				if(folderNodeRef != null)
				{
					fileFolderService.delete(folderNodeRef);    		
				}			
				folderNodeRef = fileFolderService.create(repositoryHelper.getCompanyHome(), PATH_TESTFOLDER, ContentModel.TYPE_FOLDER).getNodeRef();
				
				createControlPlan(folderNodeRef);
				List<NodeRef> controlPlansNodeRef = new ArrayList<NodeRef>();
				controlPlansNodeRef.add(controlPlanNodeRef);
				
				NodeRef productNodeRef = createRawMaterial(folderNodeRef, "Raw material");
				
				createQualityControl(folderNodeRef, controlPlansNodeRef, productNodeRef);
				
				return null;

			}},false,true);
		
		// checks
		QualityControlData qualityControlData = qualityControlDAO.find(qualityControlNodeRef);
		assertNotNull("Check QC exists", qualityControlData);
		assertNotNull("Check Sample list", qualityControlData.getSamplingList());
		assertEquals("4 samples", 4, qualityControlData.getSamplingList().size());
		assertEquals("4 samples", 4, qualityControlData.getSamplesCounter());
		int checks=0;
						
		for(SamplingListDataItem sl : qualityControlData.getSamplingList()){
			
			if(sl.getSampleId().equals("12247904/1")){
				assertEquals("check control point", controlPointNodeRef, sl.getControlPoint());
				assertEquals("check control step", controlStepNodeRef, sl.getControlStep());
				assertEquals("check state", null, sl.getSampleState());
				assertEquals("check date", qualityControlData.getBatchStart(), sl.getDateTime());
				checks++;
			}
			else if(sl.getSampleId().equals("12247904/2")){
				assertEquals("check control point", controlPointNodeRef, sl.getControlPoint());
				assertEquals("check control step", controlStepNodeRef, sl.getControlStep());
				assertEquals("check state", null, sl.getSampleState());
				assertEquals("check date", qualityControlData.getBatchStart(), sl.getDateTime());
				checks++;
			}
			else if(sl.getSampleId().equals("12247904/3")){
				assertEquals("check control point", controlPointNodeRef, sl.getControlPoint());
				assertEquals("check control step", controlStepNodeRef, sl.getControlStep());
				assertEquals("check state", null, sl.getSampleState());
				assertEquals("check date", new Date(qualityControlData.getBatchStart().getTime() + 4 * HOUR), sl.getDateTime());
				checks++;
			}
			else if(sl.getSampleId().equals("12247904/4")){
				assertEquals("check control point", controlPointNodeRef, sl.getControlPoint());
				assertEquals("check control step", controlStepNodeRef, sl.getControlStep());
				assertEquals("check state", null, sl.getSampleState());
				assertEquals("check date", new Date(qualityControlData.getBatchStart().getTime() + 4 * HOUR), sl.getDateTime());
				checks++;
			}
		}
		
		assertEquals(4, checks);
	}
}
