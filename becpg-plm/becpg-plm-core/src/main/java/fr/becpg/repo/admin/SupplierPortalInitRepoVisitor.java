package fr.becpg.repo.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMGroup;
import fr.becpg.model.PLMModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.admin.impl.AbstractInitVisitorImpl;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.ContentHelper;
import fr.becpg.repo.jscript.SupplierPortalHelper;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.DeliverableScriptOrder;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>SupplierPortalInitRepoVisitor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class SupplierPortalInitRepoVisitor extends AbstractInitVisitorImpl {

	private static final String SUPPLIER_PJT_TPL_NAME = "plm.supplier.portal.project.tpl.name";
	private static final String SUPPLIER_TASK_NAME = "plm.supplier.portal.task.supplier.name";
	private static final String SIGNATURE_TASK_NAME = "plm.supplier.portal.task.signature.name";
	private static final String VALIDATION_TASK_NAME = "plm.supplier.portal.task.validation.name";
	private static final String SUPPLIER_WIZARD_NAME = "plm.supplier.portal.deliverable.wizard.name";
	private static final String SUPPLIER_WIZARD_RAW_MATERIAL_NAME = "plm.supplier.portal.deliverable.wizard.rawmaterial.name";
	private static final String SUPPLIER_PRE_SCRIPT = "plm.supplier.portal.deliverable.scripts.pre.name";
	private static final String SIGNATURE_PRE_SCRIPT = "plm.supplier.portal.deliverable.scripts.sign.pre.name";
	private static final String SIGNATURE_URL = "plm.supplier.portal.deliverable.sign.url.name";
	private static final String SIGNATURE_POST_SCRIPT = "plm.supplier.portal.deliverable.scripts.sign.post.name";
	private static final String VALIDATE_POST_SCRIPT = "plm.supplier.portal.deliverable.scripts.post.name";

	private static final String SUPPLIER_SITE_PRESET = "supplier-site-dashboard";

	private static final String XPATH_DICTIONNARY_SCRIPTS = "./app:dictionary/app:scripts";

	@Autowired
	private SiteService siteService;

	@Autowired
	private EntityTplService entityTplService;

	@Autowired
	private AlfrescoRepository<ProjectData> alfrescoRepository;

	@Autowired
	private ContentHelper contentHelper;

	@Autowired
	private AssociationService associationService;

	/** {@inheritDoc} */
	@Override
	public List<SiteInfo> visitContainer(NodeRef companyHome) {
		logger.info("Run SupplierPortalInitRepoVisitor ...");

		List<SiteInfo> ret = new ArrayList<>();

		NodeRef systemNodeRef = visitFolder(companyHome, RepoConsts.PATH_SYSTEM);

		NodeRef entityTplsNodeRef = visitFolder(systemNodeRef, RepoConsts.PATH_ENTITY_TEMPLATES);

		NodeRef entityTplNodeRef = nodeService.getChildByName(entityTplsNodeRef, ContentModel.ASSOC_CONTAINS,
				I18NUtil.getMessage(SUPPLIER_PJT_TPL_NAME));

		SiteInfo siteInfo = siteService.getSite(SupplierPortalHelper.SUPPLIER_SITE_ID);
		NodeRef documentLibraryNodeRef = null;
		if (siteInfo == null) {
			siteInfo = siteService.createSite(SUPPLIER_SITE_PRESET, SupplierPortalHelper.SUPPLIER_SITE_ID,
					I18NUtil.getMessage("plm.supplier.portal.site.title"), "", SiteVisibility.PRIVATE);

			siteService.setMembership(siteInfo.getShortName(), PermissionService.GROUP_PREFIX + PLMGroup.ReferencingMgr.toString(),
					SiteModel.SITE_MANAGER);

			// pre-create doclib
			documentLibraryNodeRef = siteService.createContainer(SupplierPortalHelper.SUPPLIER_SITE_ID, SiteService.DOCUMENT_LIBRARY,
					ContentModel.TYPE_FOLDER, null);

			ret.add(siteInfo);
		} else {
			documentLibraryNodeRef = siteService.getContainer(SupplierPortalHelper.SUPPLIER_SITE_ID, SiteService.DOCUMENT_LIBRARY);

		}

		if (entityTplNodeRef == null) {
			NodeRef scriptFolderNodeRef = BeCPGQueryBuilder.createQuery().selectNodeByPath(companyHome, XPATH_DICTIONNARY_SCRIPTS);

			List<NodeRef> scriptResources = contentHelper.addFilesResources(scriptFolderNodeRef, "classpath*:beCPG/supplier/*.js");

			// visit supplier
			Set<QName> dataLists = new LinkedHashSet<>();
			dataLists.add(ProjectModel.TYPE_TASK_LIST);
			dataLists.add(ProjectModel.TYPE_DELIVERABLE_LIST);
			dataLists.add(BeCPGModel.TYPE_ACTIVITY_LIST);
			entityTplNodeRef = entityTplService.createEntityTpl(entityTplsNodeRef, ProjectModel.TYPE_PROJECT,
					I18NUtil.getMessage(SUPPLIER_PJT_TPL_NAME), true, false, dataLists, null);

			entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_PROPERTIES);
			entityTplService.createView(entityTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_DOCUMENTS);

			NodeRef qualityNodeRef = authorityService.getAuthorityNodeRef(PermissionService.GROUP_PREFIX + PLMGroup.QualityMgr.toString());

			ProjectData pjtTpl = alfrescoRepository.findOne(entityTplNodeRef);

			TaskListDataItem task1 = new TaskListDataItem();
			task1.setTaskName(I18NUtil.getMessage(SUPPLIER_TASK_NAME));
			task1.setDuration(5);

			pjtTpl.getTaskList().add(task1);
			
			TaskListDataItem signatureTask = new TaskListDataItem();
			signatureTask.setTaskName(I18NUtil.getMessage(SIGNATURE_TASK_NAME));
			signatureTask.setDuration(1);

			pjtTpl.getTaskList().add(signatureTask);


			TaskListDataItem task2 = new TaskListDataItem();
			task2.setTaskName(I18NUtil.getMessage(VALIDATION_TASK_NAME));
			task2.setDuration(5);
			task2.setResources(Collections.singletonList(qualityNodeRef));
			task2.setRefusedTask(task1);

			pjtTpl.getTaskList().add(task2);

			alfrescoRepository.save(pjtTpl);

			signatureTask.setPrevTasks(Collections.singletonList(task1.getNodeRef()));
			task2.setPrevTasks(Collections.singletonList(signatureTask.getNodeRef()));

			DeliverableListDataItem supplierWizard = new DeliverableListDataItem();
			supplierWizard.setDescription(I18NUtil.getMessage(SUPPLIER_WIZARD_NAME));
			supplierWizard.setUrl("/share/page/wizard?id=supplier&nodeRef={bcpg:suppliers}");
			supplierWizard.setTasks(Collections.singletonList(task1.getNodeRef()));

			DeliverableListDataItem supplierMPWizard = new DeliverableListDataItem();
			supplierMPWizard.setDescription(I18NUtil.getMessage(SUPPLIER_WIZARD_RAW_MATERIAL_NAME));
			supplierMPWizard.setUrl("/share/page/wizard?id=supplier-mp&nodeRef={pjt:projectEntity}");
			supplierMPWizard.setTasks(Collections.singletonList(task1.getNodeRef()));

			DeliverableListDataItem preSupplierScript = new DeliverableListDataItem();
			preSupplierScript.setDescription(I18NUtil.getMessage(SUPPLIER_PRE_SCRIPT));
			preSupplierScript.setScriptOrder(DeliverableScriptOrder.Pre);
			preSupplierScript.setTasks(Collections.singletonList(task1.getNodeRef()));

			DeliverableListDataItem preSignatureScript = new DeliverableListDataItem();
			preSignatureScript.setDescription(I18NUtil.getMessage(SIGNATURE_PRE_SCRIPT));
			preSignatureScript.setScriptOrder(DeliverableScriptOrder.Pre);
			preSignatureScript.setTasks(Collections.singletonList(signatureTask.getNodeRef()));
			
			DeliverableListDataItem signatureUrl = new DeliverableListDataItem();
			signatureUrl.setDescription(I18NUtil.getMessage(SIGNATURE_URL));
			signatureUrl.setScriptOrder(DeliverableScriptOrder.None);
			signatureUrl.setTasks(Collections.singletonList(signatureTask.getNodeRef()));

			DeliverableListDataItem postSignatureScript = new DeliverableListDataItem();
			postSignatureScript.setDescription(I18NUtil.getMessage(SIGNATURE_POST_SCRIPT));
			postSignatureScript.setScriptOrder(DeliverableScriptOrder.Post);
			postSignatureScript.setTasks(Collections.singletonList(signatureTask.getNodeRef()));
			
			DeliverableListDataItem postValidationScript = new DeliverableListDataItem();
			postValidationScript.setDescription(I18NUtil.getMessage(VALIDATE_POST_SCRIPT));
			postValidationScript.setScriptOrder(DeliverableScriptOrder.Post);
			postValidationScript.setTasks(Collections.singletonList(task2.getNodeRef()));

			for (NodeRef scriptNodeRef : scriptResources) {
				String name = (String) nodeService.getProperty(scriptNodeRef, ContentModel.PROP_NAME);
				if (name.equals("supplierPortalScript.js")) {
					preSupplierScript.setContent(scriptNodeRef);
				} else if (name.equals("validateProjectEntity.js")) {
					postValidationScript.setContent(scriptNodeRef);
				} else if (name.equals("send-for-signature.js")) {
					preSignatureScript.setContent(scriptNodeRef);
				} else if (name.equals("checkin-signature.js")) {
					postSignatureScript.setContent(scriptNodeRef);
				}
			}

			pjtTpl.getDeliverableList().add(preSupplierScript);
			pjtTpl.getDeliverableList().add(supplierWizard);
			pjtTpl.getDeliverableList().add(supplierMPWizard);
			pjtTpl.getDeliverableList().add(preSignatureScript);
			pjtTpl.getDeliverableList().add(signatureUrl);
			pjtTpl.getDeliverableList().add(postSignatureScript);
			pjtTpl.getDeliverableList().add(postValidationScript);

			pjtTpl.getAspects().add(PLMModel.ASPECT_SUPPLIERS);

			alfrescoRepository.save(pjtTpl);
		}

		if ((entityTplNodeRef != null) && (documentLibraryNodeRef != null)
				&& (associationService.getTargetAssoc(entityTplNodeRef, BeCPGModel.PROP_ENTITY_TPL_DEFAULT_DEST) == null)) {

			associationService.update(entityTplNodeRef, BeCPGModel.PROP_ENTITY_TPL_DEFAULT_DEST, documentLibraryNodeRef);
		}

		return ret;

	}

	/** {@inheritDoc} */
	@Override
	public Integer initOrder() {
		return 4;
	}

}
