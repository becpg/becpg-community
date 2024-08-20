package fr.becpg.repo.supplier.impl;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.BasicPasswordGenerator;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMGroup;
import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemGroup;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.authentication.BeCPGUserAccount;
import fr.becpg.repo.authentication.BeCPGUserAccountService;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.PropertiesHelper;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.jscript.SupplierPortalHelper;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.supplier.SupplierPortalService;

@Service("supplierPortalService")
public class SupplierPortalServiceImpl implements SupplierPortalService {

	private static Log logger = LogFactory.getLog(SupplierPortalServiceImpl.class);

	@Autowired
	private AssociationService associationService;

	@Autowired
	private Repository repository;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private RepoService repoService;

	@Autowired
	private SiteService siteService;

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private EntityVersionService entityVersionService;

	@Autowired
	private AlfrescoRepository<ProjectData> alfrescoRepository;

	@Autowired
	private BeCPGUserAccountService beCPGUserAccountService;

	@Autowired
	private AuthorityService authorityService;
	
	@Autowired
	private EntityService entityService;
	
	@Value("${beCPG.sendToSupplier.projectName.format}")
	private String projectNameTpl = "{entity_cm:name} - {supplier_cm:name} - REFERENCING - {date_YYYY}";

	@Value("${beCPG.sendToSupplier.entityName.format}")
	private String entityNameTpl = "{entity_cm:name} - UPDATE - {date_YYYY}";

	@Override
	public String getProjectNameTpl() {
		return projectNameTpl;
	}

	@Override
	public String getEntityNameTpl() {
		return entityNameTpl;
	}

	@Override
	public NodeRef createSupplierProject(NodeRef entityNodeRef, NodeRef projectTemplateNodeRef, List<NodeRef> supplierAccountNodeRefs) {

		Date currentDate = Calendar.getInstance().getTime();

		boolean checkAccounts = (supplierAccountNodeRefs == null) || supplierAccountNodeRefs.isEmpty();

		NodeRef supplierNodeRef = checkSupplierNodeRef(entityNodeRef, checkAccounts);

		if (checkAccounts) {
			supplierAccountNodeRefs = associationService.getTargetAssocs(supplierNodeRef, PLMModel.ASSOC_SUPPLIER_ACCOUNTS);
		}

		NodeRef destNodeRef = associationService.getTargetAssoc(projectTemplateNodeRef, BeCPGModel.PROP_ENTITY_TPL_DEFAULT_DEST);
		if (destNodeRef == null) {
			throw new IllegalStateException(I18NUtil.getMessage("message.project-template.destination.missed"));
		}

		String projectName = repoService.getAvailableName(destNodeRef, createName(entityNodeRef, supplierNodeRef, projectNameTpl, currentDate),
				false);

		ProjectData projectData = new ProjectData();
		projectData.setName(projectName);
		projectData.setParentNodeRef(destNodeRef);
		projectData.setState(ProjectState.InProgress.toString());

		if (entityDictionaryService.isSubClass(nodeService.getType(entityNodeRef), BeCPGModel.TYPE_ENTITY_V2)) {

			NodeRef branchNodeRef = null;

			if (entityNodeRef != supplierNodeRef) {

				NodeRef supplierDestFolder = getOrCreateSupplierDestFolder(supplierNodeRef, supplierAccountNodeRefs);

				String branchName = repoService.getAvailableName(supplierDestFolder,
						createName(entityNodeRef, supplierNodeRef, entityNameTpl, currentDate), false);

				if (nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_BRANCH)) {
					branchNodeRef = entityNodeRef;

					List<AssociationRef> assocs = nodeService.getTargetAssocs(entityNodeRef, BeCPGModel.ASSOC_BRANCH_FROM_ENTITY);

					if (!assocs.isEmpty()) {
						entityNodeRef = assocs.get(0).getTargetRef();
					}
				} else {
					branchNodeRef = entityVersionService.createBranch(entityNodeRef, supplierDestFolder);
				}

				associationService.update(branchNodeRef, BeCPGModel.ASSOC_AUTO_MERGE_TO, entityNodeRef);
				nodeService.setProperty(branchNodeRef, BeCPGModel.PROP_AUTO_MERGE_VERSIONTYPE, VersionType.MAJOR.toString());
				nodeService.setProperty(branchNodeRef, BeCPGModel.PROP_AUTO_MERGE_COMMENTS, projectName);
				nodeService.setProperty(branchNodeRef, ContentModel.PROP_NAME, branchName);

				NodeRef supplierDocumentsFolder = getOrCreateSupplierDocumentsFolder(branchNodeRef);
				
				for (ChildAssociationRef childAssoc : nodeService.getChildAssocs(supplierDocumentsFolder)) {
					NodeRef childNodeRef = childAssoc.getChildRef();
					nodeService.setProperty(childNodeRef, ContentModel.PROP_OWNER, AuthenticationUtil.SYSTEM_USER_NAME);
				}
			} else {
				branchNodeRef = supplierNodeRef;
			}

			projectData.setEntities(Arrays.asList(branchNodeRef));

		}

		projectData.setProjectTpl(projectTemplateNodeRef);

		if (logger.isDebugEnabled()) {
			logger.debug("Creating supplier portal project : " + projectData.getName());
		}

		NodeRef projectNodeRef = alfrescoRepository.save(projectData).getNodeRef();

		associationService.update(projectNodeRef, PLMModel.ASSOC_SUPPLIERS, Collections.singletonList(supplierNodeRef));
		associationService.update(projectNodeRef, PLMModel.ASSOC_SUPPLIER_ACCOUNTS, supplierAccountNodeRefs);

		return projectNodeRef;

	}

	@Override
	public NodeRef getOrCreateSupplierDocumentsFolder(NodeRef entityNodeRef) {
		return getOrCreateDocumentFolder(entityNodeRef, RepoConsts.PATH_SUPPLIER_DOCUMENTS);
		
	}
	
	private NodeRef getOrCreateDocumentFolder(NodeRef entityNodeRef, String path) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(ContentModel.PROP_NAME, TranslateHelper.getTranslatedPath(path));
		NodeRef documentsFolderNodeRef = nodeService.getChildByName(entityNodeRef, ContentModel.ASSOC_CONTAINS,
				(String) properties.get(ContentModel.PROP_NAME));
		if (documentsFolderNodeRef == null) {
			documentsFolderNodeRef = nodeService
					.createNode(entityNodeRef, ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
									QName.createValidLocalName(path)),
							ContentModel.TYPE_FOLDER, properties)
					.getChildRef();
		}
		return documentsFolderNodeRef;
		
	}

	@Override
	public String createName(NodeRef entityNodeRef, NodeRef supplierNodeRef, String nameFormat, Date currentDate) {

		Matcher patternMatcher = Pattern.compile("\\{([^}]+)\\}").matcher(nameFormat);
		StringBuffer sb = new StringBuffer();
		while (patternMatcher.find()) {

			String propQname = patternMatcher.group(1);
			String replacement = "";
			if (propQname.contains("|")) {
				for (String propQnameAlt : propQname.split("\\|")) {
					replacement = extractPropText(propQnameAlt, entityNodeRef, supplierNodeRef, currentDate);
					if ((replacement != null) && !replacement.isEmpty()) {
						break;
					}
				}

			} else {
				replacement = extractPropText(propQname, entityNodeRef, supplierNodeRef, currentDate);
			}

			patternMatcher.appendReplacement(sb, replacement != null ? replacement.replace("$", "") : "");

		}
		patternMatcher.appendTail(sb);

		return sb.toString().replace("-  -", "-").replace("- -", "-").trim().replaceAll("\\-$|\\(\\)", "").trim().replaceAll("\\-$|\\(\\)", "")
				.trim();

	}

	private String extractPropText(String propQname, NodeRef entityNodeRef, NodeRef supplierNodeRef, Date currentDate) {
		if (propQname != null) {
			if ((propQname.indexOf("supplier_") == 0) && (supplierNodeRef != null) && !supplierNodeRef.equals(entityNodeRef)) {

				QName prop = QName.createQName(propQname.replace("supplier_", ""), namespaceService);

				String entityProp = (String) nodeService.getProperty(entityNodeRef, prop);
				String supplierProp = (String) nodeService.getProperty(supplierNodeRef, prop);

				// case of supplier name already contained in entity name
				if ((entityProp != null) && (supplierProp != null) && entityProp.toLowerCase().contains(supplierProp.toLowerCase())) {
					return null;
				}

				return supplierProp;
			} else if (propQname.indexOf("entity_") == 0) {
				return (String) nodeService.getProperty(entityNodeRef, QName.createQName(propQname.replace("entity_", ""), namespaceService));
			} else if (propQname.indexOf("date_") == 0) {
				SimpleDateFormat dateFormat = new SimpleDateFormat(propQname.replace("date_", ""));
				return dateFormat.format(currentDate);
			}

		}
		return "";
	}

	@Override
	public NodeRef getSupplierNodeRef(NodeRef entityNodeRef) {
		NodeRef supplierNodeRef = null;
		if (PLMModel.TYPE_SUPPLIER.equals(nodeService.getType(entityNodeRef))) {
			if (nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_SUB_ENTITY)) {
				supplierNodeRef = associationService.getTargetAssoc(entityNodeRef, BeCPGModel.ASSOC_PARENT_ENTITY);
			}

			if (supplierNodeRef == null) {
				supplierNodeRef = entityNodeRef;
			}

		} else {
			supplierNodeRef = associationService.getTargetAssoc(entityNodeRef, PLMModel.ASSOC_SUPPLIERS);
		}
		return supplierNodeRef;
	}

	private NodeRef checkSupplierNodeRef(NodeRef entityNodeRef, boolean checkAccounts) {
		NodeRef supplierNodeRef = null;

		if (entityNodeRef != null) {
			supplierNodeRef = getSupplierNodeRef(entityNodeRef);

			if (supplierNodeRef != null) {
				if (checkAccounts) {
					List<NodeRef> accountNodeRefs = associationService.getTargetAssocs(supplierNodeRef, PLMModel.ASSOC_SUPPLIER_ACCOUNTS);
					if ((accountNodeRefs == null) || accountNodeRefs.isEmpty()) {
						throw new IllegalStateException(I18NUtil.getMessage("message.supplier-account.missed"));
					}
				}

			} else {
				throw new IllegalStateException(I18NUtil.getMessage("message.supplier.missed"));
			}
		}

		return supplierNodeRef;
	}

	@Override
	public NodeRef getOrCreateSupplierDestFolder(NodeRef supplierNodeRef, List<NodeRef> resources) {

		if (supplierNodeRef != null) {
			
			NodeRef documentsFolderNodeRef = getOrCreateDocumentFolder(supplierNodeRef,RepoConsts.PATH_SUPPLIER_ENTITIES );

		
			SiteInfo siteInfo = siteService.getSite(SupplierPortalHelper.SUPPLIER_SITE_ID);

			if (siteInfo != null) {

				NodeRef documentLibraryNodeRef = siteService.getContainer(SupplierPortalHelper.SUPPLIER_SITE_ID, SiteService.DOCUMENT_LIBRARY);
				if (documentLibraryNodeRef != null) {
					Locale currentLocal = I18NUtil.getLocale();
					Locale currentContentLocal = I18NUtil.getContentLocale();

					try {
						I18NUtil.setLocale(Locale.getDefault());
						I18NUtil.setContentLocale(null);

						migrateOldSupplierDestFolder(supplierNodeRef, documentLibraryNodeRef, documentsFolderNodeRef);

						if (!siteInfo.equals(siteService.getSite(supplierNodeRef))) {
							repoService.moveNode(supplierNodeRef, documentLibraryNodeRef);
						}

						for (NodeRef resourceRef : resources) {
							permissionService.setPermission(supplierNodeRef,
									(String) nodeService.getProperty(resourceRef, ContentModel.PROP_USERNAME), PermissionService.CONTRIBUTOR, true);
						}
					} finally {
						I18NUtil.setLocale(currentLocal);
						I18NUtil.setContentLocale(currentContentLocal);
					}

				}

			} else {
					
				//For old supplier portal dest folder was the userHome this should be deprecated
				NodeRef resourceRef = resources.get(0);
				NodeRef destFolder = repository.getUserHome(resourceRef);

				repoService.moveNode(supplierNodeRef, destFolder);

				permissionService.setPermission(destFolder, PermissionService.GROUP_PREFIX + PLMGroup.ReferencingMgr.toString(),
						PermissionService.CONTRIBUTOR, true);

			}

			return documentsFolderNodeRef;

		}

		return null;
	}



	private void migrateOldSupplierDestFolder(NodeRef supplierNodeRef, NodeRef documentLibraryNodeRef, NodeRef documentsFolderNodeRef) {

		NodeRef destFolder =  nodeService.getChildByName(documentLibraryNodeRef, ContentModel.ASSOC_CONTAINS,
				PropertiesHelper.cleanFolderName(I18NUtil.getMessage("path.referencing")));
		
		if (destFolder != null) {

			NodeRef oldSupplierDestNodeRef = repoService.getFolderByPath(destFolder, supplierNodeRef.getId());

			if (oldSupplierDestNodeRef == null) {

				oldSupplierDestNodeRef = nodeService.getChildByName(destFolder, ContentModel.ASSOC_CONTAINS,
						PropertiesHelper.cleanFolderName((String) nodeService.getProperty(supplierNodeRef, ContentModel.PROP_NAME)));
			}

			if (oldSupplierDestNodeRef != null && !isChildOf(documentsFolderNodeRef, oldSupplierDestNodeRef)) {

				List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(oldSupplierDestNodeRef);

				for (ChildAssociationRef childAssoc : childAssocs) {
					NodeRef childNode = childAssoc.getChildRef();
					nodeService.moveNode(childNode, documentsFolderNodeRef, ContentModel.ASSOC_CONTAINS, childAssoc.getQName());
				}

				nodeService.deleteNode(oldSupplierDestNodeRef);
			}
		}

	}
	
	private boolean isChildOf(NodeRef child, NodeRef targetParent) {
		
		ChildAssociationRef currentParent = nodeService.getPrimaryParent(child);
		
		if (currentParent == null) {
			return false;
		}
		
		if (currentParent.getParentRef() == null) {
			return false;
		}
		
		if (currentParent.getParentRef().equals(targetParent)) {
			return true;
		}
		
		return isChildOf(currentParent.getParentRef(), targetParent);
		
	}

	@Override
	public NodeRef createExternalUser(String email, String firstName, String lastName, boolean notify, Map<QName, Serializable> extraProps) {

		final boolean isAdminOrSystem = AuthenticationUtil.isRunAsUserTheSystemUser() || authorityService.hasAdminAuthority();

		boolean hasAccess = AuthenticationUtil
				.runAsSystem(() -> isAdminOrSystem || authorityService.getAuthoritiesForUser(AuthenticationUtil.getFullyAuthenticatedUser())
						.contains(PermissionService.GROUP_PREFIX + SystemGroup.ExternalUserMgr.toString()));

		if (hasAccess) {

			BasicPasswordGenerator pwdGen = new BasicPasswordGenerator();
			pwdGen.setPasswordLength(10);

			if ((email == null) || email.isBlank()) {
				throw new IllegalStateException(I18NUtil.getMessage("message.supplier.missing-email"));
			}
			
			email = email.toLowerCase();

			if ((firstName == null) || firstName.isBlank()) {
				firstName = email;
			}

			if ((lastName == null) || lastName.isBlank()) {
				firstName = email;
			}

			BeCPGUserAccount userAccount = new BeCPGUserAccount();
			userAccount.setEmail(email);
			userAccount.setUserName(email);
			userAccount.setFirstName(firstName);
			userAccount.setLastName(lastName);
			userAccount.setPassword(pwdGen.generatePassword());
			userAccount.setNotify(notify);
			userAccount.getAuthorities().add(SystemGroup.ExternalUser.toString());

			if(extraProps == null) {
				extraProps = new HashMap<>();
			}
			extraProps.put(	ContentModel.PROP_EMAIL_FEED_DISABLED, true);
			
			userAccount.getExtraProps().putAll(extraProps);

			return beCPGUserAccountService.getOrCreateUser(userAccount);

		}

		throw new IllegalAccessError("You should be member of ExternalUserMgr");
	}

	@Override
	public List<NodeRef> extractSupplierAccountRefs(NodeRef document) {
		NodeRef entityNodeRef = entityService.getEntityNodeRef(document, nodeService.getType(document));
		
		if (entityNodeRef != null) {
			NodeRef supplierNodeRef = getSupplierNodeRef(entityNodeRef);
			
			if (supplierNodeRef != null) {
				return associationService.getTargetAssocs(supplierNodeRef, PLMModel.ASSOC_SUPPLIER_ACCOUNTS);
			}
		}
		
		return new ArrayList<>();
	}
	
}
