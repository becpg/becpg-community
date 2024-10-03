package fr.becpg.repo.entity.remote;

import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;

import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;


/**
 * <p>RemoteServiceRegisty interface.</p>
 *
 * @author matthieu
 */
public interface RemoteServiceRegisty {

	/**
	 * <p>siteService.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.site.SiteService} object
	 */
	SiteService siteService();

	/**
	 * <p>nodeService.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	NodeService nodeService();

	/**
	 * <p>mlNodeService.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	NodeService mlNodeService();

	/**
	 * <p>namespaceService.</p>
	 *
	 * @return a {@link org.alfresco.service.namespace.NamespaceService} object
	 */
	NamespaceService namespaceService();

	/**
	 * <p>contentService.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.ContentService} object
	 */
	ContentService contentService();

	/**
	 * <p>attributeExtractor.</p>
	 *
	 * @return a {@link fr.becpg.repo.helper.AttributeExtractorService} object
	 */
	AttributeExtractorService attributeExtractor();

	/**
	 * <p>entityDictionaryService.</p>
	 *
	 * @return a {@link fr.becpg.repo.entity.EntityDictionaryService} object
	 */
	EntityDictionaryService entityDictionaryService();

	/**
	 * <p>associationService.</p>
	 *
	 * @return a {@link fr.becpg.repo.helper.AssociationService} object
	 */
	AssociationService associationService();

	/**
	 * <p>entityListDAO.</p>
	 *
	 * @return a {@link fr.becpg.repo.entity.EntityListDAO} object
	 */
	EntityListDAO entityListDAO();

	/**
	 * <p>versionService.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.version.VersionService} object
	 */
	VersionService versionService();

	/**
	 * <p>sysAdminParams.</p>
	 *
	 * @return a {@link org.alfresco.repo.admin.SysAdminParams} object
	 */
	SysAdminParams sysAdminParams();

	/**
	 * <p>lockService.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.lock.LockService} object
	 */
	LockService lockService();

	/**
	 * <p>mimetypeService.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.MimetypeService} object
	 */
	MimetypeService mimetypeService();

	/**
	 * <p>permissionService.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.security.PermissionService} object
	 */
	PermissionService permissionService();

	/**
	 * <p>personService.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.security.PersonService} object
	 */
	PersonService personService();

	/**
	 * <p>authorityService.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.security.AuthorityService} object
	 */
	AuthorityService authorityService();

	/**
	 * <p>transactionService.</p>
	 *
	 * @return a {@link org.alfresco.service.transaction.TransactionService} object
	 */
	TransactionService transactionService();

	/**
	 * <p>policyBehaviourFilter.</p>
	 *
	 * @return a {@link org.alfresco.repo.policy.BehaviourFilter} object
	 */
	BehaviourFilter policyBehaviourFilter();

}
