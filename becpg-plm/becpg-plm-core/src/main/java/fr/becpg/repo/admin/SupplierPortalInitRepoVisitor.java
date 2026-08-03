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
import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.ProjectRepoConsts;
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

	/** Constant <code>SUPPLIER_PJT_TPL_NAME="plm.supplier.portal.project.tpl.name"</code> */
	public static final String SUPPLIER_PJT_TPL_NAME = "plm.supplier.portal.project.tpl.name";
	/** Constant <code>SUPPLIER_TASK_NAME="plm.supplier.portal.task.supplier.name"</code> */
	private static final String SUPPLIER_TASK_NAME = "plm.supplier.portal.task.supplier.name";
	/** Constant <code>VALIDATION_TASK_NAME="plm.supplier.portal.task.validation.nam"{trunked}</code> */
	private static final String VALIDATION_TASK_NAME = "plm.supplier.portal.task.validation.name";
	/** Constant <code>SUPPLIER_WIZARD_NAME="plm.supplier.portal.deliverable.wizard."{trunked}</code> */
	private static final String SUPPLIER_WIZARD_NAME = "plm.supplier.portal.deliverable.wizard.name";
	/** Constant <code>SUPPLIER_PRE_SCRIPT="plm.supplier.portal.deliverable.scripts"{trunked}</code> */
	private static final String SUPPLIER_PRE_SCRIPT = "plm.supplier.portal.deliverable.scripts.pre.name";
	/** Constant <code>SIGNATURES_PREPARATION_SCRIPT="plm.supplier.portal.deliverable.scripts"{trunked}</code> */
	private static final String SIGNATURES_PREPARATION_SCRIPT = "plm.supplier.portal.deliverable.scripts.signature.name";

	/** Constant <code>SUPPLIER_SPEC_NAME="plm.supplier.portal.specification.name"</code> */
	private static final String SUPPLIER_SPEC_NAME = "plm.supplier.portal.specification.name";

	/** Constant <code>SUPPLIER_SITE_PRESET="supplier-site-dashboard"</code> */
	private static final String SUPPLIER_SITE_PRESET = "supplier-site-dashboard";

	/** Constant <code>XPATH_DICTIONARY_SCRIPTS="./app:dictionary/app:scripts"</code> */
	private static final String XPATH_DICTIONARY_SCRIPTS = "./app:dictionary/app:scripts";

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
		
		NodeRef projectTplsNodeRef = visitFolder(entityTplsNodeRef, ProjectRepoConsts.PATH_PROJECT_TEMPLATES); 

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
		
		NodeRef projectTplNodeRef = nodeService.getChildByName(projectTplsNodeRef, ContentModel.ASSOC_CONTAINS,
				I18NUtil.getMessage(SUPPLIER_PJT_TPL_NAME));
		
		if (projectTplNodeRef == null) {
			projectTplNodeRef = nodeService.getChildByName(entityTplsNodeRef, ContentModel.ASSOC_CONTAINS,
					I18NUtil.getMessage(SUPPLIER_PJT_TPL_NAME));
			
			if (projectTplNodeRef!= null) {
				nodeService.moveNode(projectTplNodeRef, projectTplsNodeRef, ContentModel.ASSOC_CONTAINS,
						nodeService.getPrimaryParent(projectTplNodeRef).getQName());
			}
		}
		
		// supplier scripts
		NodeRef scriptFolderNodeRef = BeCPGQueryBuilder.createQuery().selectNodeByPath(companyHome, XPATH_DICTIONARY_SCRIPTS);
		List<NodeRef> scriptResources = contentHelper.addFilesResources(scriptFolderNodeRef, "classpath*:beCPG/supplier/*.js");
		
		if (projectTplNodeRef == null) {

			/*
			    Référencement -> Pre On créer la branche dans l'espace fournisseur et on assign le wizard
			    Validation -> On laisse chez le fournisseur (gère juste la relecture et le refus)
			    Signature -> Pre On copy le rapport de type supplier (On ferme la tâche si rien), Post on sign le document 
			    Notification -> Pre on merge la branch et envoi un mail au fournisseur avec le doc sign en shareId (créer un template de mail multilingue)
			*/
			
			
			// visit supplier
			Set<QName> dataLists = new LinkedHashSet<>();
			dataLists.add(ProjectModel.TYPE_TASK_LIST);
			dataLists.add(ProjectModel.TYPE_DELIVERABLE_LIST);
			dataLists.add(BeCPGModel.TYPE_ACTIVITY_LIST);
			
			projectTplNodeRef = entityTplService.createEntityTpl(projectTplsNodeRef, ProjectModel.TYPE_PROJECT,
					I18NUtil.getMessage(SUPPLIER_PJT_TPL_NAME), true, false, dataLists, null);

			entityTplService.createView(projectTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_PROPERTIES);
			entityTplService.createView(projectTplNodeRef, BeCPGModel.TYPE_ENTITYLIST_ITEM, RepoConsts.VIEW_DOCUMENTS);

			NodeRef qualityNodeRef = authorityService.getAuthorityNodeRef(PermissionService.GROUP_PREFIX + PLMGroup.QualityMgr.toString());

			ProjectData pjtTpl = alfrescoRepository.findOne(projectTplNodeRef);

			TaskListDataItem task1 = new TaskListDataItem();
			task1.setTaskName(I18NUtil.getMessage(SUPPLIER_TASK_NAME));
			task1.setDuration(5);

			pjtTpl.getTaskList().add(task1);


			TaskListDataItem task2 = new TaskListDataItem();
			task2.setTaskName(I18NUtil.getMessage(VALIDATION_TASK_NAME));
			task2.setDuration(5);
			task2.setResources(Collections.singletonList(qualityNodeRef));
			task2.setRefusedTask(task1);

			pjtTpl.getTaskList().add(task2);

			alfrescoRepository.save(pjtTpl);

			task2.setPrevTasks(Collections.singletonList(task1.getNodeRef()));

			DeliverableListDataItem supplierMPWizard = new DeliverableListDataItem();
			supplierMPWizard.setDescription(I18NUtil.getMessage(SUPPLIER_WIZARD_NAME));
			supplierMPWizard.setUrl("/share/page/wizard?id=supplier-{pjt:projectEntity|@type}&nodeRef={pjt:projectEntity}");
			supplierMPWizard.setTasks(Collections.singletonList(task1.getNodeRef()));

			DeliverableListDataItem preSupplierScript = new DeliverableListDataItem();
			preSupplierScript.setDescription(I18NUtil.getMessage(SUPPLIER_PRE_SCRIPT));
			preSupplierScript.setScriptOrder(DeliverableScriptOrder.Pre);
			preSupplierScript.setTasks(Collections.singletonList(task1.getNodeRef()));
			
			DeliverableListDataItem signaturesPreparationScript = new DeliverableListDataItem();
			signaturesPreparationScript.setDescription(I18NUtil.getMessage(SIGNATURES_PREPARATION_SCRIPT));
			signaturesPreparationScript.setScriptOrder(DeliverableScriptOrder.Pre);
			signaturesPreparationScript.setTasks(Collections.singletonList(task2.getNodeRef()));

			for (NodeRef scriptNodeRef : scriptResources) {
				String name = (String) nodeService.getProperty(scriptNodeRef, ContentModel.PROP_NAME);
				if (name.equals("supplierPortalScript.js")) {
					preSupplierScript.setContent(scriptNodeRef);
				} else if (name.equals("supplierPortalPrepareSignatures.js")) {
					signaturesPreparationScript.setContent(scriptNodeRef);
				}
			}

			pjtTpl.getDeliverableList().add(preSupplierScript);
			pjtTpl.getDeliverableList().add(supplierMPWizard);
			pjtTpl.getDeliverableList().add(signaturesPreparationScript);

			pjtTpl.getAspects().add(PLMModel.ASPECT_SUPPLIERS);

			alfrescoRepository.save(pjtTpl);
		}

		if ((projectTplNodeRef != null) && (documentLibraryNodeRef != null)
				&& (associationService.getTargetAssoc(projectTplNodeRef, BeCPGModel.PROP_ENTITY_TPL_DEFAULT_DEST) == null)) {

			associationService.update(projectTplNodeRef, BeCPGModel.PROP_ENTITY_TPL_DEFAULT_DEST, documentLibraryNodeRef);
		}

		visitDefaultRawMaterialSpecification(entityTplsNodeRef);

		return ret;

	}

	/**
	 * <p>Creates the default raw material specification and attaches it to the raw material
	 * template, so that a supplier asked to reference a raw material can read what is expected of
	 * it.</p>
	 *
	 * <p>Create-if-absent on both counts: nothing is created when a specification of that name
	 * already exists, and the association is only written when the template carries none. An
	 * instance that defines its own specification is left untouched — this visitor runs on every
	 * startup and has to stay replayable.</p>
	 *
	 * @param entityTplsNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	private void visitDefaultRawMaterialSpecification(NodeRef entityTplsNodeRef) {

		NodeRef rawMaterialTplNodeRef = entityTplService.getEntityTpl(PLMModel.TYPE_RAWMATERIAL);

		if (rawMaterialTplNodeRef == null) {
			logger.debug("No raw material template, skipping the default specification");
			return;
		}

		NodeRef productTplsNodeRef = visitFolder(entityTplsNodeRef, PlmRepoConsts.PATH_PRODUCT_TEMPLATES);

		String specName = I18NUtil.getMessage(SUPPLIER_SPEC_NAME);
		NodeRef specNodeRef = nodeService.getChildByName(productTplsNodeRef, ContentModel.ASSOC_CONTAINS, specName);

		if (specNodeRef == null) {
			Set<QName> specLists = new LinkedHashSet<>();
			specLists.add(PLMModel.TYPE_SPEC_COMPATIBILTY_LIST);

			specNodeRef = entityTplService.createEntityTpl(productTplsNodeRef, PLMModel.TYPE_PRODUCT_SPECIFICATION,
					specName, true, false, specLists, Collections.singleton(RepoConsts.PATH_SUPPLIER_DOCUMENTS));

			logger.info("Created the default raw material specification: " + specName);
		}

		if (associationService.getTargetAssoc(rawMaterialTplNodeRef, PLMModel.ASSOC_PRODUCT_SPECIFICATIONS) == null) {
			associationService.update(rawMaterialTplNodeRef, PLMModel.ASSOC_PRODUCT_SPECIFICATIONS, specNodeRef);
			logger.info("Attached the default specification to the raw material template");
		}
	}

	/** {@inheritDoc} */
	@Override
	public Integer initOrder() {
		return 4;
	}

}
