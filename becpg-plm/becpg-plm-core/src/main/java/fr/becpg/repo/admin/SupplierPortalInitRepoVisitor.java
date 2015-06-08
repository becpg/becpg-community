package fr.becpg.repo.admin;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMGroup;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.admin.impl.AbstractInitVisitorImpl;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.helper.ContentHelper;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.DeliverableScriptOrder;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.search.BeCPGQueryBuilder;

@Service	
public class SupplierPortalInitRepoVisitor extends AbstractInitVisitorImpl {

	private static final String SUPPLIER_PJT_TPL_NAME = "plm.supplier.portal.project.tpl.name";
	private static final String SUPPLIER_TASK_NAME = "plm.supplier.portal.task.supplier.name";
	private static final String VALIDATION_TASK_NAME = "plm.supplier.portal.task.validation.name";
	private static final String SUPPLIER_WIZARD_NAME = "plm.supplier.portal.deliverable.wizard.name";
	private static final String SUPPLIER_WIZARD_RAW_MATERIAL_NAME = "plm.supplier.portal.deliverable.wizard.rawmaterial.name";
	private static final String SUPPLIER_PRE_SCRIPT = "plm.supplier.portal.deliverable.scripts.pre.name";

	private static final String XPATH_DICTIONNARY_SCRIPTS = "./app:dictionary/app:scripts";


	@Autowired
	private EntityTplService entityTplService;

	@Autowired
	private AlfrescoRepository<ProjectData> alfrescoRepository;

	@Autowired
	private AuthorityService authorityService;

	@Autowired
	private ContentHelper contentHelper;

	@Override
	public void visitContainer(NodeRef companyHome) {
		
		logger.info("Run SupplierPortalInitRepoVisitor ...");

		NodeRef systemNodeRef = visitFolder(companyHome, RepoConsts.PATH_SYSTEM);

		NodeRef entityTplsNodeRef = visitFolder(systemNodeRef, RepoConsts.PATH_ENTITY_TEMPLATES);

		NodeRef entityTplNodeRef = nodeService.getChildByName(entityTplsNodeRef, ContentModel.ASSOC_CONTAINS, I18NUtil.getMessage(SUPPLIER_PJT_TPL_NAME));
		
		entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_PROPERTIES);

		if (entityTplNodeRef == null) {
			NodeRef scriptFolderNodeRef = BeCPGQueryBuilder.createQuery().selectNodeByPath(companyHome, XPATH_DICTIONNARY_SCRIPTS);

			List<NodeRef> scriptResources = contentHelper.addFilesResources(scriptFolderNodeRef, "classpath*:beCPG/supplier/*.js");
			Set<String> subFolders = new HashSet<String>();
			subFolders.add(RepoConsts.PATH_IMAGES);

			// visit supplier
			Set<QName> dataLists = new LinkedHashSet<QName>();
			dataLists.add(ProjectModel.TYPE_TASK_LIST);
			dataLists.add(ProjectModel.TYPE_DELIVERABLE_LIST);
			dataLists.add(ProjectModel.TYPE_ACTIVITY_LIST);
			entityTplNodeRef = entityTplService.createEntityTpl(entityTplsNodeRef, ProjectModel.TYPE_PROJECT,
					I18NUtil.getMessage(SUPPLIER_PJT_TPL_NAME), true, dataLists, null);

		    NodeRef qualityNodeRef = authorityService.getAuthorityNodeRef(PermissionService.GROUP_PREFIX + PLMGroup.QualityMgr.toString());


			ProjectData pjtTpl = alfrescoRepository.findOne(entityTplNodeRef);

			TaskListDataItem task1 = new TaskListDataItem();
			task1.setTaskName(I18NUtil.getMessage(SUPPLIER_TASK_NAME));
			task1.setDuration(5);

			pjtTpl.getTaskList().add(task1);

			TaskListDataItem task2 = new TaskListDataItem();
			task2.setTaskName(I18NUtil.getMessage(VALIDATION_TASK_NAME));
			task2.setDuration(5);
			task2.setResources(Arrays.asList(qualityNodeRef));
			task2.setRefusedTask(task1);

			pjtTpl.getTaskList().add(task2);
			
			alfrescoRepository.save(pjtTpl);

			task2.setPrevTasks(Arrays.asList(task1.getNodeRef()));
			
			DeliverableListDataItem supplierWizard = new DeliverableListDataItem();
			supplierWizard.setDescription(I18NUtil.getMessage(SUPPLIER_WIZARD_NAME));
			supplierWizard.setUrl("/share/page/wizard?id=supplier&nodeRef={bcpg:suppliers}");
			supplierWizard.setTasks(Arrays.asList(task1.getNodeRef()));

			
			DeliverableListDataItem supplierMPWizard = new DeliverableListDataItem();
			supplierMPWizard.setDescription(I18NUtil.getMessage(SUPPLIER_WIZARD_RAW_MATERIAL_NAME));
			supplierMPWizard.setUrl("/share/page/wizard?id=supplier-mp&nodeRef={pjt:projectEntity}");
			supplierMPWizard.setTasks(Arrays.asList(task1.getNodeRef()));
			

			DeliverableListDataItem preSupplierScript = new DeliverableListDataItem();

			preSupplierScript.setName(I18NUtil.getMessage(SUPPLIER_PRE_SCRIPT));
			preSupplierScript.setScriptOrder(DeliverableScriptOrder.Pre);
			for (NodeRef scriptNodeRef : scriptResources) {
				if (nodeService.getProperty(scriptNodeRef, ContentModel.PROP_NAME).equals("supplierPortalScript.js")) {
					preSupplierScript.setContent(scriptNodeRef);
				}
			}
			preSupplierScript.setTasks(Arrays.asList(task1.getNodeRef()));
			pjtTpl.getDeliverableList().add(preSupplierScript);
			pjtTpl.getDeliverableList().add(supplierWizard);
			pjtTpl.getDeliverableList().add(supplierMPWizard);

			alfrescoRepository.save(pjtTpl);
		}
	}

}
