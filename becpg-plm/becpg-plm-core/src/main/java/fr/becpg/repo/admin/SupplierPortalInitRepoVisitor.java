package fr.becpg.repo.admin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.NamespaceService;
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

	/**
	 * The catalogue the supplier wizard displays its completeness against — <b>templated on the
	 * entity type</b>, exactly like the wizard id right next to it.
	 *
	 * {@code {pjt:projectEntity|@type}} expands to the local name of the entity's type, so one
	 * deliverable serves every family: a raw material resolves {@code supplierPortal-rawMaterial},
	 * a packaging {@code supplierPortal-packagingMaterial}. That is why the shipped catalogues are
	 * one per type rather than one grouping several: the id has to be predictable from the type,
	 * since nothing here can branch.
	 */
	private static final String SUPPLIER_PORTAL_CATALOG_ID = "supplierPortal-{pjt:projectEntity|@type}";

	/** Constant <code>SUPPLIER_SPEC_NAME="plm.supplier.portal.specification.name"</code> */
	private static final String SUPPLIER_SPEC_NAME = "plm.supplier.portal.specification.name";

	/** Constant <code>TECHNICAL_SHEET_TYPE_NAME="plm.supplier.portal.documentType.techn"{trunked}</code> */
	private static final String TECHNICAL_SHEET_TYPE_NAME = "plm.supplier.portal.documentType.technicalSheet.name";

	/** Constant <code>SUPPLIER_SITE_PRESET="supplier-site-dashboard"</code> */
	private static final String SUPPLIER_SITE_PRESET = "supplier-site-dashboard";

	/** Constant <code>XPATH_DICTIONARY_SCRIPTS="./app:dictionary/app:scripts"</code> */
	private static final String XPATH_DICTIONARY_SCRIPTS = "./app:dictionary/app:scripts";

	/** Constant <code>XPATH_DOCUMENT_TYPES="./cm:System/cm:Characts/bcpg:entityList"{trunked}</code> */
	private static final String XPATH_DOCUMENT_TYPES = "./cm:System/cm:Characts/bcpg:entityLists/cm:DocumentTypes";

	/** <code>bcpg:docTypeIsMandatory</code> — see DocumentTypeItem. */
	private static final QName PROP_DOC_TYPE_IS_MANDATORY = QName.createQName(BeCPGModel.BECPG_URI, "docTypeIsMandatory");

	/** <code>bcpg:docTypeLinkedTypes</code> — what makes a document type synchronised. */
	private static final QName PROP_DOC_TYPE_LINKED_TYPES = QName.createQName(BeCPGModel.BECPG_URI, "docTypeLinkedTypes");

	@Autowired
	private SiteService siteService;

	@Autowired
	private NamespaceService namespaceService;

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
			// `catalogId` : c'est ainsi qu'un catalogue se relie a un assistant.
			// `wizard-mgr.get.js` (becpg-share) n'instancie son panneau de completude
			// que si l'URL le nomme, et le portail lit le meme parametre sur ce meme
			// livrable. Le lien vit donc dans le modele de projet, ou un client peut
			// le changer, et non dans le code des deux interfaces.
			supplierMPWizard.setUrl("/share/page/wizard?id=supplier-{pjt:projectEntity|@type}&nodeRef={pjt:projectEntity}"
					+ "&catalogId=" + SUPPLIER_PORTAL_CATALOG_ID);
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

		visitDefaultTechnicalSheetType(companyHome);

		visitSupplierPortalCatalogs(companyHome);

		return ret;

	}

	/**
	 * <p>Creates the catalogues the supplier portal's wizards are scored against.</p>
	 *
	 * <p>The portal's <em>Conformité</em> step shows whatever catalogue applies to the entity
	 * being filled in. On a stock instance that means the client's own catalogues — INCO, GS1 —
	 * which were written for an internal user filling a finished product, not for a supplier
	 * filling the handful of fields it is actually asked for. The supplier therefore read a
	 * completeness score computed against fields nobody asked it to provide.</p>
	 *
	 * <p><b>The SpEL filter is what makes this safe</b>, and it is why these catalogues can ship
	 * enabled. {@code entityFilter} carries {@code suppliers != null && !suppliers.isEmpty()},
	 * which {@code EntityCatalogServiceImpl.isMatchFilter()} evaluates against the entity itself:
	 * the catalogue applies to a sheet entrusted to a supplier and to nothing else. No score of an
	 * existing park moves by one point.</p>
	 *
	 * <p>An earlier version used {@code "entityFilter": "wizard"} instead — "only when a
	 * {@code catalogId} is explicitly asked for". It was safe in the same way and useless for the
	 * purpose: a catalogue filtered that way is skipped at formulation, so it never enters
	 * {@code bcpg:entityScore}, and the completeness a supplier reads on its dashboard and on its
	 * product list could never include it. The SpEL condition puts it in the stored score, where
	 * every listing picks it up for free; the portal then shows the score of the catalogues whose
	 * id starts with {@code supplierPortal-} and leaves the customer's own out of the average.</p>
	 *
	 * <p>{@code bcpg:supplier} is the exception: {@code SupplierData} has no {@code suppliers}
	 * association, so the condition would throw on it. Its catalogue ships <b>unfiltered</b> — a
	 * supplier record is by nature a portal object — which does mean the score of every supplier
	 * record moves at its next formulation. It is the one deliberate exception here.</p>
	 *
	 * <p>{@code catalogId} is named in the deliverable URL as well
	 * ({@link #SUPPLIER_PORTAL_CATALOG_ID}), and that is a different mechanism serving a different
	 * screen: it is what makes Share's wizard paint its completeness panel. The two coexist.</p>
	 *
	 * <p>{@code locales} is declared for the same reason the catalogue exists: it is the field
	 * {@code getLocales()} already reads to score completeness language by language, and the one
	 * the portal reads to decide which languages to offer on a multilingual field (#33085).
	 * Declaring it here means the entry languages of a supplier wizard are a property of what the
	 * client asks for, not of how the portal happens to be translated.</p>
	 *
	 * <p>Four locales are shipped — <b>en, fr, es, de</b> — and the number matters as much as the
	 * choice: {@code locales} does not mean "the languages to offer for entry", it means
	 * <b>evaluate this catalogue once per language</b>. {@code EntityCatalogServiceImpl} emits one
	 * row per locale, so the list length multiplies what {@code bcpg:entityScore} stores on every
	 * single supplier sheet. Measured on dev with eleven locales: 12 rows and 9,5 kB on one raw
	 * material, against roughly 1 kB before. Four is the deliberate trade-off between covering the
	 * markets a supplier usually writes for and not inflating a stored property park-wide. A
	 * customer narrows or widens it by editing the catalogue, which is the point of putting the
	 * list there rather than in the portal.</p>
	 *
	 * <p>Create-if-absent by file name, like every other resource this visitor installs: an
	 * instance that has edited these catalogues keeps its version, and the visitor is replayable.
	 * <b>It does not run at startup</b> — {@code InitVisitorService} is driven by
	 * {@code GET becpg/admin/repository/init-repo} ({@code AdminModuleWebScript}), so a deployment
	 * alone seeds nothing and that action has to be called once after it.</p>
	 *
	 * @param companyHome a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	private void visitSupplierPortalCatalogs(NodeRef companyHome) {

		NodeRef catalogsNodeRef = BeCPGQueryBuilder.createQuery().selectNodeByPath(companyHome, RepoConsts.CATALOGS_PATH);

		if (catalogsNodeRef == null) {
			// PLMInitRepoVisitor creates the folder; on an instance where it has not run yet
			// there is nothing to fill, and the next startup will pick this up.
			logger.warn("No PropertyCatalogs folder, skipping the supplier portal catalogs");
			return;
		}

		contentHelper.addFilesResources(catalogsNodeRef, "classpath*:beCPG/supplier/catalogs/*.json");
	}

	/**
	 * <p>Creates the default supplier technical sheet document type, so that the supplier portal
	 * opens on a <b>named</b> requirement rather than on a free deposit.</p>
	 *
	 * <p>The portal asks for exactly one document before any data entry: the technical sheet. It
	 * is the only one that does not depend on what has been entered — every other expected type is
	 * derived by {@code DocumentFormulationHandler} from the claims, the labels and the hierarchy,
	 * so at step one that list is empty or wrong. The portal recognises the sheet by the
	 * {@code bcpg:documentType} the customer configured; on an instance where none is configured
	 * the step degrades to a free deposit, the supplier reads "aucun type « fiche technique »
	 * configuré", and the AI extraction has no requirement to attach its suggestions to.</p>
	 *
	 * <p><b>Created unconditionally</b>, like the default specification just above: this visitor
	 * already provisions the supplier site, the referencing project template and that
	 * specification without asking, and a flag for this one alone would have been the odd one
	 * out. It was gated at first out of caution about touching existing repositories; the gate
	 * was the wrong answer to a real concern, and the right one is the next paragraph.</p>
	 *
	 * <p>Because creating the type is <b>not</b> neutral: {@code DocumentFormulationHandler}
	 * materialises it as an expected document on every raw material at its next formulation. It
	 * is therefore created <b>non mandatory</b>, which is what keeps it harmless — an expected
	 * document that is not mandatory adds a line to the checklist and moves no documentary
	 * completeness score, no approval status and no supplier's dossier.</p>
	 *
	 * <p>Create-if-absent, matched on the name: an instance that already declares its own
	 * technical sheet type is left alone. This visitor runs on every startup, so it has to stay
	 * replayable.</p>
	 *
	 * @param companyHome a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	private void visitDefaultTechnicalSheetType(NodeRef companyHome) {

		NodeRef documentTypesNodeRef = BeCPGQueryBuilder.createQuery().selectNodeByPath(companyHome, XPATH_DOCUMENT_TYPES);

		if (documentTypesNodeRef == null) {
			logger.warn("No DocumentTypes characteristic list, skipping the default technical sheet type");
			return;
		}

		String typeName = I18NUtil.getMessage(TECHNICAL_SHEET_TYPE_NAME);

		if (nodeService.getChildByName(documentTypesNodeRef, ContentModel.ASSOC_CONTAINS, typeName) != null) {
			return;
		}

		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(ContentModel.PROP_NAME, typeName);
		properties.put(BeCPGModel.PROP_CHARACT_NAME, typeName);
		// NOT mandatory, and this is what makes creating it by default safe: the sheet is what
		// the portal *opens on*, not a document whose absence must suspend an approval. Mandatory,
		// it would move the documentary completeness — and so the derived approval status — of
		// every raw material of every instance, on upgrade, without anyone asking.
		properties.put(PROP_DOC_TYPE_IS_MANDATORY, Boolean.FALSE);
		// The link to the raw material type is what makes it a SYNCHRONISED document type
		// (DocumentTypeItem.isSynchronisedDocumentType): without it the type exists but no
		// requirement is ever materialised, and the portal is no better off than before.
		properties.put(PROP_DOC_TYPE_LINKED_TYPES,
				(Serializable) Collections.singletonList(PLMModel.TYPE_RAWMATERIAL.toPrefixString(namespaceService)));

		nodeService.createNode(documentTypesNodeRef, ContentModel.ASSOC_CONTAINS,
				QName.createQName(BeCPGModel.BECPG_URI, typeName), BeCPGModel.TYPE_DOCUMENT_TYPE, properties);

		logger.info("Created the default supplier technical sheet document type: " + typeName);
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
