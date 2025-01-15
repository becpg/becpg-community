/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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
package fr.becpg.repo.jscript;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.external.RemoteUserMapper;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.version.common.VersionUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.quickshare.QuickShareDTO;
import org.alfresco.service.cmr.quickshare.QuickShareService;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.routines.checkdigit.CheckDigitException;
import org.mozilla.javascript.Context;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.ScriptValueConverter;

import fr.becpg.api.BeCPGPublicApi;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.EntityListState;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.authentication.BeCPGTicketService;
import fr.becpg.repo.dictionary.constraint.DynListConstraint;
import fr.becpg.repo.entity.AutoNumService;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.entity.version.VersionHelper;
import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AuthorityHelper;
import fr.becpg.repo.helper.CheckSumHelper;
import fr.becpg.repo.helper.GTINHelper;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.SiteHelper;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.hierarchy.HierarchyService;
import fr.becpg.repo.license.BeCPGLicenseManager;
import fr.becpg.repo.mail.BeCPGMailService;
import fr.becpg.repo.olap.OlapService;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.search.PaginatedSearchCache;
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * Utility script methods
 *
 * @author matthieu
 * @version $Id: $Id
 */
@BeCPGPublicApi
public final class BeCPGScriptHelper extends BaseScopableProcessorExtension {

	private static Log logger = LogFactory.getLog(BeCPGScriptHelper.class);

	private NodeService nodeService;

	private AutoNumService autoNumService;

	private OlapService olapService;

	private QuickShareService quickShareService;

	private NodeService mlNodeService;

	private NamespaceService namespaceService;

	private DictionaryService dictionaryService;

	private EntityDictionaryService entityDictionaryService;

	private EntityVersionService entityVersionService;

	private ServiceRegistry serviceRegistry;

	private AssociationService associationService;

	private EntityService entityService;

	private PaginatedSearchCache paginatedSearchCache;

	private PermissionService permissionService;

	private RepoService repoService;

	private EntityListDAO entityListDAO;

	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	private SiteService siteService;

	private TenantAdminService tenantAdminService;

	private ContentService contentService;

	private VersionService versionService;

	private EntityReportService entityReportService;

	private BeCPGLicenseManager beCPGLicenseManager;

	private BeCPGMailService beCPGMailService;

	private Repository repositoryHelper;

	private FormulationService<FormulatedEntity> formulationService;

	private HierarchyService hierarchyService;

	private FileFolderService fileFolderService;

	private SystemConfigurationService systemConfigurationService;

	private BeCPGTicketService beCPGTicketService;

	private BehaviourFilter policyBehaviourFilter;

	private AuthorityService authorityService;

	private PersonService personService;

	private RemoteUserMapper remoteUserMapper;

	/**
	 * <p>Setter for the field <code>remoteUserMapper</code>.</p>
	 *
	 * @param remoteUserMapper a {@link org.alfresco.repo.security.authentication.external.RemoteUserMapper} object
	 */
	public void setRemoteUserMapper(RemoteUserMapper remoteUserMapper) {
		this.remoteUserMapper = remoteUserMapper;
	}

	/**
	 * <p>Setter for the field <code>personService</code>.</p>
	 *
	 * @param personService a {@link org.alfresco.service.cmr.security.PersonService} object
	 */
	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	/**
	 * <p>Setter for the field <code>authorityService</code>.</p>
	 *
	 * @param authorityService a {@link org.alfresco.service.cmr.security.AuthorityService} object
	 */
	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	private MutableAuthenticationService authenticationService;

	/**
	 * <p>Setter for the field <code>authenticationService</code>.</p>
	 *
	 * @param authenticationService a {@link org.alfresco.service.cmr.security.MutableAuthenticationService} object
	 */
	public void setAuthenticationService(MutableAuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	private boolean useBrowserLocale;

	private boolean showUnauthorizedWarning = true;

	private boolean showEntitiesInTree() {
		return Boolean.parseBoolean(systemConfigurationService.confValue("becpg.doclibtree.showEntities"));
	}

	/**
	 * <p>Setter for the field <code>policyBehaviourFilter</code>.</p>
	 *
	 * @param policyBehaviourFilter a {@link org.alfresco.repo.policy.BehaviourFilter} object
	 */
	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	/**
	 * <p>Setter for the field <code>systemConfigurationService</code>.</p>
	 *
	 * @param systemConfigurationService a {@link fr.becpg.repo.system.SystemConfigurationService} object
	 */
	public void setSystemConfigurationService(SystemConfigurationService systemConfigurationService) {
		this.systemConfigurationService = systemConfigurationService;
	}

	/**
	 * <p>Setter for the field <code>fileFolderService</code>.</p>
	 *
	 * @param fileFolderService a {@link org.alfresco.service.cmr.model.FileFolderService} object
	 */
	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	/**
	 * <p>Setter for the field <code>hierarchyService</code>.</p>
	 *
	 * @param hierarchyService a {@link fr.becpg.repo.hierarchy.HierarchyService} object
	 */
	public void setHierarchyService(HierarchyService hierarchyService) {
		this.hierarchyService = hierarchyService;
	}

	/**
	 * <p>Setter for the field <code>formulationService</code>.</p>
	 *
	 * @param formulationService a {@link fr.becpg.repo.formulation.FormulationService} object
	 */
	public void setFormulationService(FormulationService<FormulatedEntity> formulationService) {
		this.formulationService = formulationService;
	}

	/**
	 * <p>Setter for the field <code>repositoryHelper</code>.</p>
	 *
	 * @param repositoryHelper a {@link org.alfresco.repo.model.Repository} object
	 */
	public void setRepositoryHelper(Repository repositoryHelper) {
		this.repositoryHelper = repositoryHelper;
	}

	/**
	 * <p>Setter for the field <code>beCPGLicenseManager</code>.</p>
	 *
	 * @param beCPGLicenseManager a {@link fr.becpg.repo.license.BeCPGLicenseManager} object
	 */
	public void setBeCPGLicenseManager(BeCPGLicenseManager beCPGLicenseManager) {
		this.beCPGLicenseManager = beCPGLicenseManager;
	}

	/**
	 * <p>Setter for the field <code>beCPGMailService</code>.</p>
	 *
	 * @param beCPGMailService a {@link fr.becpg.repo.mail.BeCPGMailService} object
	 */
	public void setBeCPGMailService(BeCPGMailService beCPGMailService) {
		this.beCPGMailService = beCPGMailService;
	}

	/**
	 * <p>Setter for the field <code>versionService</code>.</p>
	 *
	 * @param versionService a {@link org.alfresco.service.cmr.version.VersionService} object
	 */
	public void setVersionService(VersionService versionService) {
		this.versionService = versionService;
	}

	/**
	 * <p>Setter for the field <code>tenantAdminService</code>.</p>
	 *
	 * @param tenantAdminService a {@link org.alfresco.repo.tenant.TenantAdminService} object
	 */
	public void setTenantAdminService(TenantAdminService tenantAdminService) {
		this.tenantAdminService = tenantAdminService;
	}

	/**
	 * <p>Setter for the field <code>beCPGTicketService</code>.</p>
	 *
	 * @param beCPGTicketService a {@link fr.becpg.repo.authentication.BeCPGTicketService} object
	 */
	public void setBeCPGTicketService(BeCPGTicketService beCPGTicketService) {
		this.beCPGTicketService = beCPGTicketService;
	}

	/**
	 * <p>Setter for the field <code>useBrowserLocale</code>.</p>
	 *
	 * @param useBrowserLocale a boolean.
	 */
	public void setUseBrowserLocale(boolean useBrowserLocale) {
		this.useBrowserLocale = useBrowserLocale;
	}

	/**
	 * <p>Setter for the field <code>permissionService</code>.</p>
	 *
	 * @param permissionService a {@link org.alfresco.service.cmr.security.PermissionService} object.
	 */
	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	/**
	 * <p>Setter for the field <code>olapService</code>.</p>
	 *
	 * @param olapService a {@link fr.becpg.repo.olap.OlapService} object.
	 */
	public void setOlapService(OlapService olapService) {
		this.olapService = olapService;
	}

	/**
	 * <p>Setter for the field <code>autoNumService</code>.</p>
	 *
	 * @param autoNumService a {@link fr.becpg.repo.entity.AutoNumService} object.
	 */
	public void setAutoNumService(AutoNumService autoNumService) {
		this.autoNumService = autoNumService;
	}

	/**
	 * <p>Setter for the field <code>quickShareService</code>.</p>
	 *
	 * @param quickShareService a {@link org.alfresco.service.cmr.quickshare.QuickShareService} object.
	 */
	public void setQuickShareService(QuickShareService quickShareService) {
		this.quickShareService = quickShareService;
	}

	/**
	 * <p>getOrCreateBeCPGCode.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getOrCreateBeCPGCode(ScriptNode sourceNode) {
		return autoNumService.getOrCreateBeCPGCode(sourceNode.getNodeRef());
	}

	/**
	 * <p>getAutoNumValue.</p>
	 *
	 * @param className a {@link java.lang.String} object.
	 * @param propertyName a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} representing the incremented autonum value of the property provided
	 */
	public String getAutoNumValue(String className, String propertyName) {
		return autoNumService.getAutoNumValue(QName.createQName(className, namespaceService), QName.createQName(propertyName, namespaceService));
	}

	/**
	 * <p>setAutoNumValue.</p>
	 *
	 * @param className a {@link java.lang.String} object.
	 * @param propertyName a {@link java.lang.String} object.
	 * @param counter a {@link java.lang.Long} value from which we want to set the autonum
	 * @return a {@link java.lang.Boolean} telling if the autonum value of the property provided has properly been set
	 */
	public boolean setAutoNumValue(String className, String propertyName, Long counter) {
		return autoNumService.setAutoNumValue(QName.createQName(className, namespaceService), QName.createQName(propertyName, namespaceService),
				counter);
	}

	/**
	 * <p>getAutoNumCounter.</p>
	 *
	 * @param className a {@link java.lang.String} object.
	 * @param propertyName a {@link java.lang.String} object.
	 * @return a {@link java.lang.Long} of the value of the counter after incrementation.
	 */
	public Long getAutoNumCounter(String className, String propertyName) {

		String autoNumValue = getAutoNumValue(className, propertyName);

		int endIndex = autoNumValue.length();
		int startIndex = endIndex;

		while (startIndex > 0 && Character.isDigit(autoNumValue.charAt(startIndex - 1))) {
		    startIndex--;
		}

		if (startIndex < endIndex) {
		    String counterStr = autoNumValue.substring(startIndex, endIndex);
		    return Long.parseLong(counterStr);
		} else {
		    return null;
		}
	}

	/**
	 * <p>getAutoNumNodeRef.</p>
	 *
	 * @param className a {@link java.lang.String} object.
	 * @param propertyName a {@link java.lang.String} object.
	 * @return the {@link org.alfresco.service.cmr.repository.NodeRef} of the counter for the property's classname provided.
	 */
	public NodeRef getAutoNumNodeRef(String className, String propertyName) {
		return autoNumService.getAutoNumNodeRef(QName.createQName(className, namespaceService), QName.createQName(propertyName, namespaceService));
	}

	/**
	 * <p>getOrCreateCode.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param propertyName a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getOrCreateCode(ScriptNode sourceNode, String propertyName) {
		return autoNumService.getOrCreateCode(sourceNode.getNodeRef(), QName.createQName(propertyName, namespaceService));
	}

	/**
	 * <p>shareContent.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @return a {@link java.lang.String} object
	 */
	public String shareContent(ScriptNode sourceNode) {

		QuickShareDTO quickShareDTO = quickShareService.shareContent(sourceNode.getNodeRef());

		if (quickShareDTO != null) {
			return quickShareDTO.getId();
		}

		return null;
	}

	/**
	 * <p>Setter for the field <code>mlNodeService</code>.</p>
	 *
	 * @param mlNodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setMlNodeService(NodeService mlNodeService) {
		this.mlNodeService = mlNodeService;
	}

	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object.
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * <p>Setter for the field <code>dictionaryService</code>.</p>
	 *
	 * @param dictionaryService a {@link org.alfresco.service.cmr.dictionary.DictionaryService} object.
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	/**
	 * <p>Setter for the field <code>entityVersionService</code>.</p>
	 *
	 * @param entityVersionService a {@link fr.becpg.repo.entity.version.EntityVersionService} object.
	 */
	public void setEntityVersionService(EntityVersionService entityVersionService) {
		this.entityVersionService = entityVersionService;
	}

	/**
	 * <p>Setter for the field <code>serviceRegistry</code>.</p>
	 *
	 * @param serviceRegistry a {@link org.alfresco.service.ServiceRegistry} object.
	 */
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object.
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	/**
	 * <p>Setter for the field <code>entityDictionaryService</code>.</p>
	 *
	 * @param entityDictionaryService a {@link fr.becpg.repo.entity.EntityDictionaryService} object.
	 */
	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}

	/**
	 * <p>Setter for the field <code>paginatedSearchCache</code>.</p>
	 *
	 * @param paginatedSearchCache a {@link fr.becpg.repo.search.PaginatedSearchCache} object.
	 */
	public void setPaginatedSearchCache(PaginatedSearchCache paginatedSearchCache) {
		this.paginatedSearchCache = paginatedSearchCache;
	}

	/**
	 * <p>isShowEntitiesInTree.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isShowEntitiesInTree() {
		return showEntitiesInTree();
	}

	/**
	 * <p>Setter for the field <code>repoService</code>.</p>
	 *
	 * @param repoService a {@link fr.becpg.repo.helper.RepoService} object.
	 */
	public void setRepoService(RepoService repoService) {
		this.repoService = repoService;
	}

	/**
	 * <p>Setter for the field <code>entityService</code>.</p>
	 *
	 * @param entityService a {@link fr.becpg.repo.entity.EntityService} object.
	 */
	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>entityListDAO</code>.</p>
	 *
	 * @param entityListDAO a {@link fr.becpg.repo.entity.EntityListDAO} object.
	 */
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	/**
	 * <p>isShowUnauthorizedWarning.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isShowUnauthorizedWarning() {
		return showUnauthorizedWarning;
	}

	/**
	 * <p>isShowLicenceWarning.</p>
	 *
	 * @return a boolean
	 */
	public boolean isShowLicenceWarning() {
		return isShowUnauthorizedWarning();
	}

	/**
	 * <p>Setter for the field <code>showUnauthorizedWarning</code>.</p>
	 *
	 * @param showUnauthorizedWarning a boolean.
	 */
	public void setShowUnauthorizedWarning(boolean showUnauthorizedWarning) {
		this.showUnauthorizedWarning = showUnauthorizedWarning;
	}

	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/**
	 * <p>Setter for the field <code>siteService</code>.</p>
	 *
	 * @param siteService a {@link org.alfresco.service.cmr.site.SiteService} object
	 */
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	/**
	 * <p>Setter for the field <code>contentService</code>.</p>
	 *
	 * @param contentService a {@link org.alfresco.service.cmr.repository.ContentService} object
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	/**
	 * <p>Setter for the field <code>entityReportService</code>.</p>
	 *
	 * @param entityReportService a {@link fr.becpg.repo.report.entity.EntityReportService} object
	 */
	public void setEntityReportService(EntityReportService entityReportService) {
		this.entityReportService = entityReportService;
	}

	/**
	 * <p>getMLProperty.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param propQName a {@link java.lang.String} object.
	 * @param locale a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getMLProperty(ScriptNode sourceNode, String propQName, String locale) {
		return getMLProperty(sourceNode, propQName, locale, false);
	}

	/**
	 * <p>getMLProperty.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @param propQName a {@link java.lang.String} object
	 * @param locale a {@link java.lang.String} object
	 * @param exactLocale a {@link java.lang.Boolean} object
	 * @return a {@link java.lang.String} object
	 */
	public String getMLProperty(ScriptNode sourceNode, String propQName, String locale, Boolean exactLocale) {

		MLText mlText = (MLText) mlNodeService.getProperty(sourceNode.getNodeRef(), getQName(propQName));

		if (mlText != null) {
			if (Boolean.TRUE.equals(exactLocale)) {
				return mlText.get(MLTextHelper.parseLocale(locale));
			}
			return MLTextHelper.getClosestValue(mlText, MLTextHelper.parseLocale(locale));
		}
		return null;
	}

	/**
	 * <p>getMLConstraint.</p>
	 *
	 * @param value a {@link java.lang.String} object.
	 * @param propQName a {@link java.lang.String} object.
	 * @param locale a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getMLConstraint(String value, String propQName, String locale) {

		PropertyDefinition propertyDef = dictionaryService.getProperty(QName.createQName(propQName, namespaceService));

		String constraintName = null;
		DynListConstraint dynListConstraint = null;

		if (!propertyDef.getConstraints().isEmpty()) {
			for (ConstraintDefinition constraint : propertyDef.getConstraints()) {
				if (constraint.getConstraint() instanceof DynListConstraint) {
					dynListConstraint = (DynListConstraint) constraint.getConstraint();

				} else if ("LIST".equals(constraint.getConstraint().getType())) {
					constraintName = constraint.getRef().toPrefixString(namespaceService).replace(":", "_");

				}

				if ((constraintName != null) || (dynListConstraint != null)) {
					break;
				}
			}
		}

		if (dynListConstraint != null) {
			return dynListConstraint.getDisplayLabel(value, new Locale(locale));
		}

		return constraintName != null ? TranslateHelper.getConstraint(constraintName, value, new Locale(locale)) : value;
	}

	/**
	 * <p>setMLProperty.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param propQName a {@link java.lang.String} object.
	 * @param locale a {@link java.lang.String} object.
	 * @param value a {@link java.lang.String} object.
	 */
	public void setMLProperty(ScriptNode sourceNode, String propQName, String locale, String value) {
	    MLText mlText = (MLText) mlNodeService.getProperty(sourceNode.getNodeRef(), getQName(propQName));
		if (mlText == null) {
			mlText = new MLText();
		}

		if ((locale != null) && !locale.isBlank()) {
			Locale loc = MLTextHelper.parseLocale(locale);

			if ((value != null) && !value.isEmpty()) {
				if (MLTextHelper.isSupportedLocale(loc)) {
					mlText.addValue(loc, value);
				} else {
					logger.error("Unsupported locale in setMLProperty " + loc + " for " + propQName);
				}
			} else {
				mlText.removeValue(loc);
			}
			mlNodeService.setProperty(sourceNode.getNodeRef(), getQName(propQName), mlText);

		} else {
			logger.error("Null or empty locale in setMLProperty for " + propQName);
		}

	}

	/**
	 * <p>getQName.</p>
	 *
	 * @param qName a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.namespace.QName} object.
	 */
	public QName getQName(String qName) {
		return QName.createQName(qName, namespaceService);
	}

	/**
	 * <p>assocValue.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef assocValue(NodeRef nodeRef, String assocQname) {
		return associationService.getTargetAssoc(nodeRef, getQName(assocQname));
	}

	/**
	 * <p>assocValue.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef assocValue(ScriptNode sourceNode, String assocQname) {
		return assocValue(sourceNode.getNodeRef(), assocQname);
	}

	/**
	 * <p>assocValue.</p>
	 *
	 * @param nodeRef a {@link java.lang.String} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef assocValue(String nodeRef, String assocQname) {
		return assocValue(new NodeRef(nodeRef), assocQname);
	}

	/**
	 * <p>assocValues.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @return a {@link java.lang.Object} object.
	 */
	public Object assocValues(ScriptNode sourceNode, String assocQname) {
		return assocValues(sourceNode.getNodeRef(), assocQname);
	}

	/**
	 * <p>assocValues.</p>
	 *
	 * @param nodeRef a {@link java.lang.String} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @return a {@link java.lang.Object} object.
	 */
	public Object assocValues(String nodeRef, String assocQname) {
		return assocValues(new NodeRef(nodeRef), assocQname);
	}

	/**
	 * <p>assocValues.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @return a {@link java.lang.Object} object.
	 */
	public Object assocValues(NodeRef nodeRef, String assocQname) {
		return wrapValue(associationService.getTargetAssocs(nodeRef, getQName(assocQname)));
	}

	/**
	 * <p>sourceAssocValues.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @return a {@link java.lang.Object} object.
	 */
	public Object sourceAssocValues(ScriptNode sourceNode, String assocQname) {
		return sourceAssocValues(sourceNode.getNodeRef(), assocQname);
	}

	/**
	 * <p>sourceAssocValues.</p>
	 *
	 * @param nodeRef a {@link java.lang.String} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @return a {@link java.lang.Object} object.
	 */
	public Object sourceAssocValues(String nodeRef, String assocQname) {
		return sourceAssocValues(new NodeRef(nodeRef), assocQname);
	}

	/**
	 * <p>sourceAssocValues.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @return a {@link java.lang.Object} object.
	 */
	public Object sourceAssocValues(NodeRef nodeRef, String assocQname) {
		return wrapValue(associationService.getSourcesAssocs(nodeRef, getQName(assocQname)));
	}

	// TODO Perfs
	private Object wrapValue(Object object) {
		return ScriptValueConverter.wrapValue(Context.getCurrentContext().initSafeStandardObjects(), object);
	}

	/**
	 * <p>assocPropValues.</p>
	 *
	 * @param nodeRef a {@link java.lang.String} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @param propQName a {@link java.lang.String} object.
	 * @return a {@link java.lang.Object} object.
	 */
	public Object assocPropValues(String nodeRef, String assocQname, String propQName) {
		return assocPropValues(new NodeRef(nodeRef), assocQname, propQName);
	}

	/**
	 * <p>assocPropValues.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @param propQName a {@link java.lang.String} object.
	 * @return a {@link java.lang.Object} object.
	 */
	public Object assocPropValues(ScriptNode sourceNode, String assocQname, String propQName) {
		return assocPropValues(sourceNode.getNodeRef(), assocQname, propQName);
	}

	/**
	 * <p>assocPropValues.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @param propQName a {@link java.lang.String} object.
	 * @return a {@link java.lang.Object} object.
	 */
	public Object assocPropValues(NodeRef nodeRef, String assocQname, String propQName) {
		List<String> ret = new ArrayList<>();
		for (NodeRef assoc : associationService.getTargetAssocs(nodeRef, getQName(assocQname))) {
			if (assoc != null) {
				String value = (String) nodeService.getProperty(assoc, getQName(propQName));
				if (value != null) {
					ret.add(value);
				}
			}
		}
		return wrapValue(ret);
	}

	/**
	 * <p>assocAssocValue.</p>
	 *
	 * @param nodeRef a {@link java.lang.String} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @param assocAssocsQname a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef assocAssocValue(String nodeRef, String assocQname, String assocAssocsQname) {
		return assocAssocValue(new NodeRef(nodeRef), assocQname, assocAssocsQname);
	}

	/**
	 * <p>assocAssocValue.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @param assocAssocsQname a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef assocAssocValue(ScriptNode sourceNode, String assocQname, String assocAssocsQname) {
		return assocAssocValue(sourceNode.getNodeRef(), assocQname, assocAssocsQname);
	}

	/**
	 * <p>assocAssocValue.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @param assocAssocsQname a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef assocAssocValue(NodeRef nodeRef, String assocQname, String assocAssocsQname) {
		NodeRef assocNodeRef = assocValue(nodeRef, assocQname);
		if (assocNodeRef != null) {
			return associationService.getTargetAssoc(assocNodeRef, getQName(assocAssocsQname));
		}
		return null;
	}

	/**
	 * <p>assocAssocValues.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @param assocAssocsQname a {@link java.lang.String} object.
	 * @return a {@link java.lang.Object} object.
	 */
	public Object assocAssocValues(ScriptNode sourceNode, String assocQname, String assocAssocsQname) {
		return assocAssocValues(sourceNode.getNodeRef(), assocQname, assocAssocsQname);
	}

	/**
	 * <p>assocAssocValues.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @param assocAssocsQname a {@link java.lang.String} object.
	 * @return a {@link java.lang.Object} object.
	 */
	public Object assocAssocValues(NodeRef nodeRef, String assocQname, String assocAssocsQname) {
		List<NodeRef> ret = new ArrayList<>();
		for (NodeRef assocNodeRef : associationService.getTargetAssocs(nodeRef, getQName(assocQname))) {
			if (assocNodeRef != null) {
				ret.addAll(associationService.getTargetAssocs(assocNodeRef, getQName(assocAssocsQname)));
			}
		}
		return wrapValue(ret);
	}

	/**
	 * <p>assocPropValue.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @param propQName a {@link java.lang.String} object.
	 * @return a {@link java.io.Serializable} object.
	 */
	public Serializable assocPropValue(ScriptNode sourceNode, String assocQname, String propQName) {
		return assocPropValue(sourceNode.getNodeRef(), assocQname, propQName);
	}

	/**
	 * <p>assocPropValue.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @param propQName a {@link java.lang.String} object.
	 * @return a {@link java.io.Serializable} object.
	 */
	public Serializable assocPropValue(NodeRef nodeRef, String assocQname, String propQName) {
		NodeRef assocNodeRef = assocValue(nodeRef, assocQname);
		if (assocNodeRef != null) {
			return nodeService.getProperty(assocNodeRef, getQName(propQName));
		}
		return null;
	}

	/**
	 * <p>updateAssoc.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @param assocs a {@link java.lang.Object} object.
	 */
	public void updateAssoc(ScriptNode sourceNode, String assocQname, Object assocs) {
		updateAssoc(sourceNode.getNodeRef(), assocQname, assocs);
	}

	/**
	 * <p>updateAssoc.</p>
	 *
	 * @param nodeRef a {@link java.lang.String} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @param assocs a {@link java.lang.Object} object.
	 */
	public void updateAssoc(String nodeRef, String assocQname, Object assocs) {
		updateAssoc(new NodeRef(nodeRef), assocQname, assocs);
	}

	/**
	 * <p>updateAssoc.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param assocQname a {@link java.lang.String} object.
	 * @param assocs a {@link java.lang.Object} object.
	 */
	public void updateAssoc(NodeRef nodeRef, String assocQname, Object assocs) {

		Object unwrapped = ScriptValueConverter.unwrapValue(assocs);

		if (unwrapped == null) {
			associationService.update(nodeRef, getQName(assocQname), new ArrayList<>());
		} else if (unwrapped instanceof ScriptNode) {
			associationService.update(nodeRef, getQName(assocQname), ((ScriptNode) unwrapped).getNodeRef());
		} else if (unwrapped instanceof NodeRef) {
			associationService.update(nodeRef, getQName(assocQname), (NodeRef) unwrapped);
		} else if (unwrapped instanceof Iterable<?>) {

			List<NodeRef> nodes = new ArrayList<>();
			for (Object element : (Iterable<?>) unwrapped) {
				if (element instanceof ScriptNode) {
					nodes.add(((ScriptNode) element).getNodeRef());

				} else if (element instanceof NodeRef) {
					nodes.add((NodeRef) element);
				}
			}
			associationService.update(nodeRef, getQName(assocQname), nodes);
		}

	}

	/**
	 * <p>updateChecksum.</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 * @param value a {@link java.lang.String} object.
	 * @param checksum a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String updateChecksum(String key, String value, String checksum) {
		return CheckSumHelper.updateChecksum(key, value, checksum);
	}

	/**
	 * <p>isSameChecksum.</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 * @param value a {@link java.lang.String} object.
	 * @param checksum a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public boolean isSameChecksum(String key, String value, String checksum) {
		return CheckSumHelper.isSameChecksum(key, value, checksum);
	}

	/**
	 * <p>findOne.</p>
	 *
	 * @param nodeRef a {@link java.lang.String} object.
	 * @return a {@link fr.becpg.repo.repository.RepositoryEntity} object.
	 */
	public RepositoryEntity findOne(String nodeRef) {
		if (NodeRef.isNodeRef(nodeRef)) {
			return alfrescoRepository.findOne(new NodeRef(nodeRef));
		}
		return null;
	}

	/**
	 * <p>save.</p>
	 *
	 * @param entity a {@link fr.becpg.repo.repository.RepositoryEntity} object
	 * @return a {@link fr.becpg.repo.repository.RepositoryEntity} object
	 */
	public RepositoryEntity save(RepositoryEntity entity) {
		return alfrescoRepository.save(entity);
	}

	/**
	 * <p>setExtraValue.</p>
	 *
	 * @param entity a {@link fr.becpg.repo.repository.RepositoryEntity} object
	 * @param qName a {@link java.lang.String} object
	 * @param value a {@link java.lang.Object} object
	 */
	public void setExtraValue(RepositoryEntity entity, String qName, Object value) {
		entity.getExtraProperties().put(getQName(qName), (Serializable) ScriptValueConverter.unwrapValue(value));
	}

	/**
	 * <p>getMessage.</p>
	 *
	 * @param messageKey a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getMessage(String messageKey) {
		return I18NUtil.getMessage(messageKey, I18NUtil.getLocale());
	}

	/**
	 * <p>getMessage.</p>
	 *
	 * @param messageKey a {@link java.lang.String} object.
	 * @param param a {@link java.lang.Object} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getMessage(String messageKey, Object... param) {
		return I18NUtil.getMessage(messageKey, param);
	}

	/**
	 * <p>getLocalizedMessage.</p>
	 *
	 * @param messageKey a {@link java.lang.String} object
	 * @param locale a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	public String getLocalizedMessage(String messageKey, String locale) {
		Locale localeObject = I18NUtil.getLocale();
		if (locale != null && !locale.isBlank()) {
			localeObject = MLTextHelper.parseLocale(locale);
		}
		return I18NUtil.getMessage(messageKey, localeObject);
	}

	/**
	 * <p>getLocalizedMessage.</p>
	 *
	 * @param messageKey a {@link java.lang.String} object
	 * @param locale a {@link java.lang.String} object
	 * @param param a {@link java.lang.Object} object
	 * @return a {@link java.lang.String} object
	 */
	public String getLocalizedMessage(String messageKey, String locale, Object... param) {
		Locale localeObject = I18NUtil.getLocale();
		if (locale != null && !locale.isBlank()) {
			localeObject = MLTextHelper.parseLocale(locale);
		}
		return I18NUtil.getMessage(messageKey, localeObject, param);
	}

	/**
	 * <p>getOlapSSOUrl.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getOlapSSOUrl() {
		return olapService.getSSOUrl();
	}

	/**
	 * <p>getBeCPGAuthTocken.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getBeCPGAuthTocken() {
		return beCPGTicketService.getCurrentAuthToken();
	}

	/**
	 * <p>createBranch.</p>
	 *
	 * @param entity a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param parent a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param setAutoMerge a boolean.
	 * @return a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 */
	public ScriptNode createBranch(ScriptNode entity, ScriptNode parent, boolean setAutoMerge) {
		NodeRef branchNodeRef = entityVersionService.createBranch(entity.getNodeRef(), parent.getNodeRef());

		if (setAutoMerge) {
			associationService.update(branchNodeRef, BeCPGModel.ASSOC_AUTO_MERGE_TO, entity.getNodeRef());
		}

		return new ScriptNode(branchNodeRef, serviceRegistry);
	}

	/**
	 * <p>createBranch.</p>
	 *
	 * @param entity a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param parent a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @return a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 */
	public ScriptNode createBranch(ScriptNode entity, ScriptNode parent) {
		return createBranch(entity, parent, false);
	}

	/**
	 * <p>mergeBranch.</p>
	 *
	 * @param entity a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param branchTo a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param description a {@link java.lang.String} object.
	 * @param type a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 */
	public ScriptNode mergeBranch(ScriptNode entity, ScriptNode branchTo, String description, String type) {
		NodeRef retNodeRef = entityVersionService.mergeBranch(entity.getNodeRef(), branchTo != null ? branchTo.getNodeRef() : null,
				VersionType.valueOf(type), description);
		if (retNodeRef == null) {
			throw new IllegalStateException("Cannot merge :" + entity.getNodeRef());
		}
		return new ScriptNode(retNodeRef, serviceRegistry);
	}

	/**
	 * <p>updateLastVersionLabel.</p>
	 *
	 * @param entity a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @param versionLabel a {@link java.lang.String} object
	 */
	public void updateLastVersionLabel(ScriptNode entity, String versionLabel) {
		entityVersionService.updateLastVersionLabel(entity.getNodeRef(), versionLabel);
	}

	/**
	 * <p>moveAndRename.</p>
	 *
	 * @param nodeToMove a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param destination a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @return a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 */
	public ScriptNode moveAndRename(ScriptNode nodeToMove, ScriptNode destination) {
		repoService.moveNode(nodeToMove.getNodeRef(), destination.getNodeRef());
		return nodeToMove;
	}

	/**
	 * <p>getAvailableName</p>
	 *
	 * @param folder a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param name a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object
	 */
	public String getAvailableName(ScriptNode folder, String name) {
		return repoService.getAvailableName(folder.getNodeRef(), name, false);
	}

	/**
	 * <p>getAvailableName.</p>
	 *
	 * @param folder a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @param name a {@link java.lang.String} object
	 * @param keepExtension a boolean
	 * @return a {@link java.lang.String} object
	 */
	public String getAvailableName(ScriptNode folder, String name, boolean keepExtension) {
		return repoService.getAvailableName(folder.getNodeRef(), name, false, keepExtension);
	}

	/**
	 * <p>changeEntityListStates.</p>
	 *
	 * @param entity a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param state a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public boolean changeEntityListStates(ScriptNode entity, String state) {
		return entityService.changeEntityListStates(entity.getNodeRef(), EntityListState.valueOf(state));
	}

	/**
	 * <p>copyList.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param destNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param listQname a {@link java.lang.String} object.
	 */
	public void copyList(ScriptNode sourceNode, ScriptNode destNode, String listQname) {
		entityListDAO.copyDataList(entityListDAO.getList(entityListDAO.getListContainer(sourceNode.getNodeRef()), getQName(listQname)),
				destNode.getNodeRef(), true);
	}

	/**
	 * <p>listExist.</p>
	 *
	 * @param node a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param listQname a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public boolean listExist(ScriptNode node, String listQname) {
		NodeRef listContainer = entityListDAO.getListContainer(node.getNodeRef());
		if (listContainer != null) {
			NodeRef listNodeRef = entityListDAO.getList(listContainer, getQName(listQname));
			if (listNodeRef != null) {

				return !entityListDAO.isEmpty(listNodeRef, getQName(listQname));

			}

		}
		return false;

	}

	/**
	 * <p>getListItems.</p>
	 *
	 * @param node a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param listQname a {@link java.lang.String} object.
	 * @return an array of {@link org.alfresco.service.cmr.repository.NodeRef} objects.
	 */
	public NodeRef[] getListItems(ScriptNode node, String listQname) {
		NodeRef listContainer = entityListDAO.getListContainer(node.getNodeRef());
		if (listContainer != null) {
			NodeRef listNodeRef = entityListDAO.getList(listContainer, getQName(listQname));
			if (listNodeRef != null) {

				return entityListDAO.getListItems(listNodeRef, getQName(listQname)).toArray(new NodeRef[] {});

			}

		}
		return new NodeRef[] {};
	}

	/**
	 * <p>getSubTypes.</p>
	 *
	 * @param type a {@link java.lang.String} object.
	 * @return an array of {@link java.lang.String} objects.
	 */
	public String[] getSubTypes(String type) {
		Set<String> ret = new HashSet<>();

		for (QName typeQname : entityDictionaryService.getSubTypes(QName.createQName(type, namespaceService))) {
			ret.add(typeQname.toPrefixString(namespaceService));
		}

		return ret.toArray(new String[ret.size()]);

	}

	/**
	 * <p>getSearchResults.</p>
	 *
	 * @param queryId a {@link java.lang.String} object.
	 * @return an array of {@link java.lang.String} objects.
	 */
	public String[] getSearchResults(String queryId) {
		List<NodeRef> ret = paginatedSearchCache.getSearchResults(queryId);
		if (ret != null) {
			return ret.stream().map(NodeRef::toString).toArray(String[]::new);
		} else {
			logger.warn("No results found for queryId: " + queryId);
		}

		return new String[] {};
	}

	/**
	 * <p>setPermissionAsSystem.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param permission a {@link java.lang.String} object.
	 * @param authority a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public boolean setPermissionAsSystem(ScriptNode sourceNode, String permission, String authority) {
		return setPermissionAsSystem(sourceNode.getNodeRef(), permission, authority);
	}

	/**
	 * <p>setPermissionAsSystem.</p>
	 *
	 * @param nodeRef a {@link java.lang.String} object
	 * @param permission a {@link java.lang.String} object
	 * @param authority a {@link java.lang.String} object
	 * @return a boolean
	 */
	public boolean setPermissionAsSystem(String nodeRef, String permission, String authority) {
		return setPermissionAsSystem(new NodeRef(nodeRef), permission, authority);
	}

	/**
	 * <p>setPermissionAsSystem.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param permission a {@link java.lang.String} object
	 * @param authority a {@link java.lang.String} object
	 * @return a boolean
	 */
	public boolean setPermissionAsSystem(NodeRef nodeRef, String permission, String authority) {
		return AuthenticationUtil.runAsSystem(() -> {
			permissionService.setPermission(nodeRef, authority, permission, true);
			return true;
		});
	}

	/**
	 * <p>allowWrite.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param authority a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public boolean allowWrite(ScriptNode sourceNode, String authority) {
		return allowWrite(sourceNode.getNodeRef(), authority);
	}

	/**
	 * <p>allowWrite.</p>
	 *
	 * @param nodeRef a {@link java.lang.String} object
	 * @param authority a {@link java.lang.String} object
	 * @return a boolean
	 */
	public boolean allowWrite(String nodeRef, String authority) {
		return allowWrite(new NodeRef(nodeRef), authority);
	}

	/**
	 * <p>allowWrite.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param authority a {@link java.lang.String} object
	 * @return a boolean
	 */
	public boolean allowWrite(NodeRef nodeRef, String authority) {
		return AuthenticationUtil.runAsSystem(() -> {
			permissionService.setPermission(nodeRef, authority, PermissionService.EDITOR, true);
			return true;
		});
	}

	/**
	 * <p>allowRead.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param authority a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public boolean allowRead(ScriptNode sourceNode, String authority) {
		return allowRead(sourceNode.getNodeRef(), authority);
	}

	/**
	 * <p>allowRead.</p>
	 *
	 * @param nodeRef a {@link java.lang.String} object
	 * @param authority a {@link java.lang.String} object
	 * @return a boolean
	 */
	public boolean allowRead(String nodeRef, String authority) {
		return allowWrite(new NodeRef(nodeRef), authority);
	}

	/**
	 * <p>allowRead.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param authority a {@link java.lang.String} object
	 * @return a boolean
	 */
	public boolean allowRead(NodeRef nodeRef, String authority) {
		return AuthenticationUtil.runAsSystem(() -> {
			permissionService.setPermission(nodeRef, authority, PermissionService.READ, true);
			return true;
		});
	}

	/**
	 * <p>clearPermissions.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param inherit a boolean.
	 * @return a boolean.
	 */
	public boolean clearPermissions(ScriptNode sourceNode, boolean inherit) {
		return clearPermissions(sourceNode.getNodeRef(), inherit);
	}

	/**
	 * <p>clearPermissions.</p>
	 *
	 * @param nodeRef a {@link java.lang.String} object
	 * @param inherit a boolean
	 * @return a boolean
	 */
	public boolean clearPermissions(String nodeRef, boolean inherit) {
		return clearPermissions(new NodeRef(nodeRef), inherit);
	}

	/**
	 * <p>clearPermissions.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param inherit a boolean
	 * @return a boolean
	 */
	public boolean clearPermissions(NodeRef nodeRef, boolean inherit) {
		return AuthenticationUtil.runAsSystem(() -> {
			Set<AccessPermission> acls = permissionService.getAllSetPermissions(nodeRef);
			for (AccessPermission permission : acls) {
				if (permission.isSetDirectly()) {
					permissionService.deletePermission(nodeRef, permission.getAuthority(), permission.getPermission());
				}
			}
			permissionService.setInheritParentPermissions(nodeRef, inherit);
			return true;
		});
	}

	/**
	 * <p>deleteGroupPermission.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param authority a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public boolean deleteGroupPermission(ScriptNode sourceNode, String authority) {
		return deleteGroupPermission(sourceNode.getNodeRef(), authority);
	}

	/**
	 * <p>deleteGroupPermission.</p>
	 *
	 * @param nodeRef a {@link java.lang.String} object
	 * @param authority a {@link java.lang.String} object
	 * @return a boolean
	 */
	public boolean deleteGroupPermission(String nodeRef, String authority) {
		return deleteGroupPermission(new NodeRef(nodeRef), authority);
	}

	/**
	 * <p>deleteGroupPermission.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param authority a {@link java.lang.String} object
	 * @return a boolean
	 */
	public boolean deleteGroupPermission(NodeRef nodeRef, String authority) {
		return AuthenticationUtil.runAsSystem(() -> {
			Set<AccessPermission> permissions = permissionService.getAllSetPermissions(nodeRef);
			clearPermissions(nodeRef, false);
			for (AccessPermission permission : permissions) {
				if (!permission.getAuthority().equals(authority)) {
					permissionService.setPermission(nodeRef, permission.getAuthority(), permission.getPermission(), true);
				}
			}
			return true;
		});
	}

	/**
	 * <p>getUserLocale.</p>
	 *
	 * @param personNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getUserLocale(ScriptNode personNode) {
		String loc = (String) mlNodeService.getProperty(personNode.getNodeRef(), BeCPGModel.PROP_USER_LOCALE);
		if ((loc == null) || loc.isEmpty()) {
			if (useBrowserLocale) {
				return null;
			} else {
				return MLTextHelper.localeKey(I18NUtil.getLocale());
			}
		}
		return loc;
	}

	/**
	 * <p>getUserContentLocale.</p>
	 *
	 * @param personNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getUserContentLocale(ScriptNode personNode) {
		String loc = (String) mlNodeService.getProperty(personNode.getNodeRef(), BeCPGModel.PROP_USER_CONTENT_LOCAL);
		if ((loc == null) || loc.isEmpty()) {
			if (useBrowserLocale) {
				return null;
			} else {
				loc = MLTextHelper.localeKey(I18NUtil.getContentLocale());
			}
		}
		return loc;
	}

	/**
	 * <p>generateEAN13Code.</p>
	 *
	 * @param prefix a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 * @throws org.apache.commons.validator.routines.checkdigit.CheckDigitException if any.
	 */
	public static String generateEAN13Code(String prefix) throws CheckDigitException {
		return GTINHelper.generateEAN13Code(prefix);
	}

	/**
	 * <p>createEAN13Code.</p>
	 *
	 * @param prefix a {@link java.lang.String} object
	 * @param serialNumber a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 * @throws org.apache.commons.validator.routines.checkdigit.CheckDigitException if any.
	 */
	public static String createEAN13Code(String prefix, String serialNumber) throws CheckDigitException {
		return GTINHelper.createEAN13Code(prefix, serialNumber);
	}

	/**
	 * <p>addDigitToEANPrefix.</p>
	 *
	 * @param eanCode a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 * @throws org.apache.commons.validator.routines.checkdigit.CheckDigitException if any.
	 */
	public static String addDigitToEANPrefix(String eanCode) throws CheckDigitException {
		return GTINHelper.addDigitToEANPrefix(eanCode);
	}

	/**
	 * <p>getDocumentLibraryNodeRef.</p>
	 *
	 * @param siteId a {@link java.lang.String} object
	 * @return a {@link org.alfresco.repo.jscript.ScriptNode} object
	 */
	public ScriptNode getDocumentLibraryNodeRef(String siteId) {

		NodeRef nodeRef = AuthenticationUtil.runAsSystem(() -> siteService.getContainer(siteId, "documentLibrary"));

		return new ScriptNode(nodeRef, serviceRegistry);
	}

	/**
	 * <p>convert.</p>
	 *
	 * @param scriptNode a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @return a {@link java.lang.String} object
	 */
	public String convert(ScriptNode scriptNode) {

		NodeRef notConvertedNode = scriptNode.getNodeRef();

		String name = (String) nodeService.getProperty(notConvertedNode, ContentModel.PROP_NAME);

		String tenantName = "default";

		if (!TenantService.DEFAULT_DOMAIN.equals(tenantAdminService.getCurrentUserDomain())) {
			tenantName = tenantAdminService.getTenant(tenantAdminService.getCurrentUserDomain()).getTenantDomain();
		}

		long start = System.currentTimeMillis();

		NodeRef convertedNode = entityVersionService.convertVersion(notConvertedNode);

		if (convertedNode != null) {
			long timeElapsed = System.currentTimeMillis() - start;

			String message = "Converted entity '" + name + "', from " + notConvertedNode + " to " + convertedNode + ", tenant : " + tenantName
					+ ", time elapsed : " + timeElapsed + " ms";

			logger.info(message);

			return message;
		} else {
			return "The node couldn't be converted";
		}

	}

	/**
	 * <p>copyContent.</p>
	 *
	 * @param from a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @param to a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @throws org.alfresco.service.cmr.repository.ContentIOException if any.
	 */
	public void copyContent(ScriptNode from, ScriptNode to) throws ContentIOException {

		ContentReader reader = contentService.getReader(from.getNodeRef(), ContentModel.PROP_CONTENT);
		ContentWriter writer = contentService.getWriter(to.getNodeRef(), ContentModel.PROP_CONTENT, true);
		writer.setEncoding(reader.getEncoding());
		writer.setMimetype(reader.getMimetype());

		writer.putContent(reader);
	}

	/**
	 * <p>getReportNode.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @return a {@link org.alfresco.repo.jscript.ScriptNode} object
	 */
	public ScriptNode getReportNode(ScriptNode sourceNode) {

		NodeRef sourceNodeRef = sourceNode.getNodeRef();

		return new ScriptNode(entityReportService.getOrRefreshReport(sourceNodeRef, null), serviceRegistry, getScope());
	}

	/**
	 * <p>getReportsOfKind.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @param reportKind a {@link java.lang.String} object
	 * @return an array of {@link org.alfresco.repo.jscript.ScriptNode} objects
	 */
	public ScriptNode[] getReportsOfKind(ScriptNode sourceNode, String reportKind) {

		NodeRef sourceNodeRef = sourceNode.getNodeRef();

		List<NodeRef> reports = entityReportService.getOrRefreshReportsOfKind(sourceNodeRef, reportKind);

		return reports.stream().map(r -> new ScriptNode(r, serviceRegistry, getScope())).toArray(ScriptNode[]::new);

	}

	/**
	 * <p>count.</p>
<<<<<<< 23.2.1
	 *
	 * @param type a {@link java.lang.String} object
=======
	 * @param type
>>>>>>> 5350029 [Feature] Improve ML helper
	 * @return Number of object of type
	 */
	public Long count(String type) {
		return BeCPGQueryBuilder.createQuery().ofType(QName.createQName(type, namespaceService)).inDB().ftsLanguage().count();
	}

	/**
	 * <p>count.</p>
	 *
	 * @param type a {@link java.lang.String} object
	 * @param excludeDefaults a boolean
	 * @return a {@link java.lang.Long} object
	 */
	public Long count(String type, boolean excludeDefaults) {
		if (excludeDefaults) {
			return BeCPGQueryBuilder.createQuery().ofType(QName.createQName(type, namespaceService)).excludeDefaults().inDB().ftsLanguage().count();
		} else {
			return count(type);
		}
	}

	/**
	 * <p>isLicenseValid.</p>
	 *
	 * @return a boolean
	 */
	public boolean isLicenseValid() {
		return beCPGLicenseManager.isLicenseValid();
	}

	/**
	 * <p>isSpecialLicenceUser.</p>
	 *
	 * @return a boolean
	 */
	public boolean isSpecialLicenceUser() {
		return beCPGLicenseManager.isSpecialLicenceUser();
	}

	/**
	 * <p>getTranslatedPath.</p>
	 *
	 * @param name a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	public String getTranslatedPath(String name) {

		String ret = TranslateHelper.getTranslatedPath(name);
		if (ret == null || ret.isBlank()) {
			return name;
		}
		return ret;

	}

	/**
	 * <p>sendMail.</p>
	 *
	 * @param recipientNodeRefs a {@link java.util.List} object
	 * @param subject a {@link java.lang.String} object
	 * @param mailTemplate a {@link java.lang.String} object
	 * @param templateArgs a {@link java.util.Map} object
	 * @param sendToSelf a boolean
	 */
	@SuppressWarnings("unchecked")
	public void sendMail(List<ScriptNode> recipientNodeRefs, String subject, String mailTemplate, Map<String, Object> templateArgs,
			boolean sendToSelf) {
		beCPGMailService.sendMail(recipientNodeRefs.stream().map(ScriptNode::getNodeRef).collect(Collectors.toList()), subject, mailTemplate,
				(Map<String, Object>) ScriptValueConverter.unwrapValue(templateArgs), sendToSelf);
	}

	/**
	 * <p>sendMLAwareMail.</p>
	 *
	 * @param authorities an array of {@link java.lang.String} objects
	 * @param fromEmail a {@link java.lang.String} object
	 * @param subjectKey a {@link java.lang.String} object
	 * @param subjectParams an array of {@link java.lang.Object} objects
	 * @param mailTemplate a {@link java.lang.String} object
	 * @param templateArgs a {@link java.util.Map} object
	 */
	@SuppressWarnings("unchecked")
	public void sendMLAwareMail(String[] authorities, String fromEmail, String subjectKey, Object[] subjectParams, String mailTemplate,
			Map<String, Object> templateArgs) {
		beCPGMailService.sendMLAwareMail(Set.of(authorities), fromEmail, subjectKey, subjectParams, mailTemplate,
				(Map<String, Object>) ScriptValueConverter.unwrapValue(templateArgs));
	}

	/**
	 * <p>extractSiteDisplayPath.</p>
	 *
	 * @param scriptNode a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @return a {@link java.lang.String} object
	 */
	public String extractSiteDisplayPath(ScriptNode scriptNode) {
		return SiteHelper.extractSiteDisplayPath(nodeService.getPath(scriptNode.getNodeRef()), permissionService, nodeService, namespaceService);
	}

	/**
	 * <p>isEntityV2SubType.</p>
	 *
	 * @param scriptNode a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @return a boolean
	 */
	public boolean isEntityV2SubType(ScriptNode scriptNode) {
		return dictionaryService.isSubClass(nodeService.getType(scriptNode.getNodeRef()), BeCPGModel.TYPE_ENTITY_V2);
	}

	/**
	 * <p>generateVersionReport.</p>
	 *
	 * @param node a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @param versionLabel a {@link java.lang.String} object
	 */
	public void generateVersionReport(ScriptNode node, String versionLabel) {

		NodeRef entityNodeRef = node.getNodeRef();

		NodeRef versionNode = VersionUtil
				.convertNodeRef(versionService.getVersionHistory(entityNodeRef).getVersion(versionLabel).getFrozenStateNodeRef());

		if (VersionHelper.isVersion(versionNode) && (nodeService.getProperty(versionNode, BeCPGModel.PROP_ENTITY_FORMAT) != null)) {
			NodeRef extractedNode = entityVersionService.extractVersion(versionNode);
			entityReportService.generateReports(extractedNode, versionNode);
		}
	}

	/**
	 * <p>generateVersionReports.</p>
	 *
	 * @param node a {@link org.alfresco.repo.jscript.ScriptNode} object
	 */
	public void generateVersionReports(ScriptNode node) {
		NodeRef entityNodeRef = node.getNodeRef();
		VersionHistory versionHistory = versionService.getVersionHistory(entityNodeRef);
		for (Version version : versionHistory.getAllVersions()) {
			NodeRef versionNode = VersionUtil.convertNodeRef(version.getFrozenStateNodeRef());
			if (VersionHelper.isVersion(versionNode) && (nodeService.getProperty(versionNode, BeCPGModel.PROP_ENTITY_FORMAT) != null)) {
				NodeRef extractedNode = entityVersionService.extractVersion(versionNode);
				entityReportService.generateReports(extractedNode, versionNode);
			}
		}
	}

	/**
	 * <p>classifyByHierarchy.</p>
	 *
	 * @param productNode a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @param folderNode a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @param propHierarchy a {@link java.lang.String} object
	 * @return a boolean
	 */
	public boolean classifyByHierarchy(ScriptNode productNode, ScriptNode folderNode, String propHierarchy) {
		return classifyByHierarchy(productNode.getNodeRef(), folderNode.getNodeRef(), propHierarchy, null);
	}

	/**
	 * <p>classifyByHierarchy.</p>
	 *
	 * @param productNode a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @param folderNode a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @param propHierarchy a {@link java.lang.String} object
	 * @param locale a {@link java.lang.String} object
	 * @return a boolean
	 */
	public boolean classifyByHierarchy(ScriptNode productNode, ScriptNode folderNode, String propHierarchy, String locale) {
		return classifyByHierarchy(productNode.getNodeRef(), folderNode.getNodeRef(), propHierarchy, locale);
	}

	private boolean classifyByHierarchy(NodeRef productNode, NodeRef folderNode, String propHierarchy, String localeString) {

		QName hierarchyQname = null;

		if (propHierarchy != null && !propHierarchy.isEmpty()) {
			hierarchyQname = getQName(propHierarchy);
		}

		Locale locale = Locale.getDefault();

		if (localeString != null && !localeString.isBlank()) {
			locale = new Locale(localeString);
		}

		return hierarchyService.classifyByHierarchy(folderNode, productNode, hierarchyQname, locale);
	}

	/**
	 * <p>classifyByPropAndHierarchy.</p>
	 *
	 * @param productNode a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @param folderNode a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @param propHierarchy a {@link java.lang.String} object
	 * @param propPathName a {@link java.lang.String} object
	 * @param locale a {@link java.lang.String} object
	 * @return a boolean
	 */
	public boolean classifyByPropAndHierarchy(ScriptNode productNode, ScriptNode folderNode, String propHierarchy, String propPathName,
			String locale) {

		if (propPathName == null || propPathName.isEmpty()) {
			return classifyByHierarchy(productNode, folderNode, propHierarchy, locale);
		} else if (propPathName.split("\\|").length == 1) {

			QName propPathNameQName = getQName(propPathName);

			String subFolderName = nodeService.getProperty(productNode.getNodeRef(), propPathNameQName).toString();

			if (locale != null && !locale.isEmpty()) {
				subFolderName = getMLConstraint(subFolderName, propPathName, locale);
			}

			NodeRef childNodeRef = nodeService.getChildByName(folderNode.getNodeRef(), ContentModel.ASSOC_CONTAINS, subFolderName);

			if (childNodeRef == null) {
				childNodeRef = fileFolderService.create(folderNode.getNodeRef(), subFolderName, ContentModel.TYPE_FOLDER).getNodeRef();
			}

			classifyByHierarchy(productNode.getNodeRef(), childNodeRef, propHierarchy, locale);
		} else {
			String[] assocs = propPathName.split("\\|");

			String assocName = assocs[0];

			String property = assocs[assocs.length - 1];

			NodeRef finalAssoc = classifyPropAndHierarchyExtractAssoc(productNode.getNodeRef(), assocName, new ArrayList<>(Arrays.asList(assocs)));

			String subFolderName = nodeService.getProperty(finalAssoc, getQName(property)).toString();

			if (locale != null && !locale.isEmpty()) {
				subFolderName = getMLConstraint(subFolderName, propPathName, locale);
			}

			NodeRef childNodeRef = nodeService.getChildByName(folderNode.getNodeRef(), ContentModel.ASSOC_CONTAINS, subFolderName);

			if (childNodeRef == null) {
				childNodeRef = fileFolderService.create(folderNode.getNodeRef(), subFolderName, ContentModel.TYPE_FOLDER).getNodeRef();
			}

			classifyByHierarchy(productNode.getNodeRef(), childNodeRef, propHierarchy, locale);

		}

		return false;
	}

	private NodeRef classifyPropAndHierarchyExtractAssoc(NodeRef nodeRef, String assocName, List<String> assocList) {

		if (assocList.isEmpty()) {
			return nodeRef;
		}

		String nextAssocName = assocList.get(0);

		NodeRef nextNode = associationService.getTargetAssoc(nodeRef, getQName(assocName));

		if (nextNode == null) {
			return nodeRef;
		}

		assocList.remove(0);

		return classifyPropAndHierarchyExtractAssoc(nextNode, nextAssocName, assocList);
	}

	/**
	 * <p>getQNameTitle.</p>
	 *
	 * @param qname a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	public String getQNameTitle(String qname) {

		QName type = QName.createQName(qname, namespaceService);

		ClassDefinition classDef = dictionaryService.getClass(type);

		return classDef.getTitle(dictionaryService);
	}

	/**
	 * <p>classifyByDate.</p>
	 *
	 * @param product a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @param path a {@link java.lang.String} object
	 * @param date a {@link java.util.Date} object
	 * @param dateFormat a {@link java.lang.String} object
	 * @return a boolean
	 */
	public boolean classifyByDate(ScriptNode product, String path, Date date, String dateFormat) {

		if (date != null && dateFormat != null) {

			StringBuilder pathBuilder = new StringBuilder(path);

			for (String formatPart : dateFormat.split("/")) {

				pathBuilder.append("/");

				boolean isFirstSubPart = true;

				for (String subFormatPart : formatPart.split(" - ")) {

					if (!isFirstSubPart) {
						pathBuilder.append(" - ");
					}

					SimpleDateFormat subFormat = new SimpleDateFormat(subFormatPart);
					pathBuilder.append(subFormat.format(date));

					isFirstSubPart = false;
				}
			}

			NodeRef parentFolder = repoService.getOrCreateFolderByPaths(repositoryHelper.getRootHome(),
					Arrays.asList(pathBuilder.toString().split("/")));

			if (!ContentModel.TYPE_FOLDER.equals(nodeService.getType(parentFolder))) {
				logger.warn("Incorrect destination node type:" + nodeService.getType(parentFolder));
			} else {
				return repoService.moveNode(product.getNodeRef(), parentFolder);
			}
		}

		return false;
	}

	/**
	 * <p>classifyByDate.</p>
	 *
	 * @param product a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @param documentLibrary a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @param subPath a {@link java.lang.String} object
	 * @param date a {@link java.util.Date} object
	 * @param dateFormat a {@link java.lang.String} object
	 * @return a boolean
	 */
	public boolean classifyByDate(ScriptNode product, ScriptNode documentLibrary, String subPath, Date date, String dateFormat) {

		StringBuilder pathBuilder = new StringBuilder();

		if (subPath != null && !subPath.isBlank()) {
			for (String split : subPath.split("/")) {
				pathBuilder.append("/");
				pathBuilder.append(getTranslatedPath(split));
			}
		}

		if (date != null && dateFormat != null) {

			QName type = nodeService.getType(product.getNodeRef());

			ClassDefinition classDef = dictionaryService.getClass(type);

			NodeRef destinationNodeRef = repoService.getOrCreateFolderByPath(documentLibrary.getNodeRef(), type.getLocalName(),
					classDef.getTitle(dictionaryService));

			for (String formatPart : dateFormat.split("/")) {

				pathBuilder.append("/");

				boolean isFirstSubPart = true;

				for (String subFormatPart : formatPart.split(" - ")) {

					if (!isFirstSubPart) {
						pathBuilder.append(" - ");
					}

					SimpleDateFormat subFormat = new SimpleDateFormat(subFormatPart);
					pathBuilder.append(subFormat.format(date));

					isFirstSubPart = false;
				}
			}

			NodeRef newFolder = repoService.getOrCreateFolderByPaths(destinationNodeRef, Arrays.asList(pathBuilder.toString().split("/")));

			if (!ContentModel.TYPE_FOLDER.equals(nodeService.getType(newFolder))) {
				logger.warn("Incorrect destination node type:" + nodeService.getType(newFolder));
			} else {
				return repoService.moveNode(product.getNodeRef(), newFolder);
			}
		}

		return false;
	}

	/**
	 * <p>formulate.</p>
	 *
	 * @param productNode a {@link org.alfresco.repo.jscript.ScriptNode} object
	 */
	public void formulate(ScriptNode productNode) {
		try {
			policyBehaviourFilter.disableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
			policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
			policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);

			L2CacheSupport.doInCacheContext(() -> AuthenticationUtil.runAsSystem(() -> {
				formulationService.formulate(productNode.getNodeRef());
				return true;
			}), false, true);

		} finally {
			policyBehaviourFilter.enableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
			policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
			policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
		}
	}

	/**
	 * <p>extractPeople.</p>
	 *
	 * @param authorities an array of {@link java.lang.String} objects
	 * @return an array of {@link java.lang.String} objects
	 */
	public String[] extractPeople(String[] authorities) {
		return AuthorityHelper.extractPeople(Set.of(authorities)).toArray(new String[0]);
	}

	/**
	 * <p>floatingLicensesExceeded.</p>
	 *
	 * @param sessionId a {@link java.lang.String} object
	 * @return a boolean
	 */
	public boolean floatingLicensesExceeded(String sessionId) {
		return beCPGLicenseManager.floatingLicensesExceeded(sessionId);
	}

	/**
	 * <p>hasWriteLicense.</p>
	 *
	 * @return a boolean
	 */
	public boolean hasWriteLicense() {
		return beCPGLicenseManager.hasWriteLicense();
	}

	/**
	 * <p>isAccountEnabled.</p>
	 *
	 * @param userName a {@link java.lang.String} object
	 * @return a boolean
	 */
	public boolean isAccountEnabled(String userName) {
		if (!authenticationService.isAuthenticationMutable(userName)
				&& nodeService.hasAspect(personService.getPerson(userName), ContentModel.ASPECT_PERSON_DISABLED)) {
			return false;
		}
		return this.authenticationService.getAuthenticationEnabled(userName);
	}

	/**
	 * <p>enableAccount.</p>
	 *
	 * @param userName a {@link java.lang.String} object
	 */
	public void enableAccount(String userName) {
		if (this.authorityService.isAdminAuthority(AuthenticationUtil.getFullyAuthenticatedUser())) {
			if (!authenticationService.isAuthenticationMutable(userName)) {
				nodeService.removeAspect(personService.getPerson(userName), ContentModel.ASPECT_PERSON_DISABLED);
				return;
			}
			this.authenticationService.setAuthenticationEnabled(userName, true);
		}
	}

	/**
	 * <p>disableAccount.</p>
	 *
	 * @param userName a {@link java.lang.String} object
	 */
	public void disableAccount(String userName) {
		if (this.authorityService.isAdminAuthority(AuthenticationUtil.getFullyAuthenticatedUser())) {
			if (!authenticationService.isAuthenticationMutable(userName)) {
				nodeService.addAspect(personService.getPerson(userName), ContentModel.ASPECT_PERSON_DISABLED, null);
				return;
			}
			this.authenticationService.setAuthenticationEnabled(userName, false);
		}
	}

	/**
	 * <p>isSsoEnabled.</p>
	 *
	 * @return a boolean
	 */
	public boolean isSsoEnabled() {
		return remoteUserMapper != null && (!(remoteUserMapper instanceof ActivateableBean activateableBean) || activateableBean.isActive());
	}

}
