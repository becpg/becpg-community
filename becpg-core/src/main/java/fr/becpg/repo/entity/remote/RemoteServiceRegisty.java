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


public interface RemoteServiceRegisty {

	SiteService siteService();

	NodeService nodeService();

	NodeService mlNodeService();

	NamespaceService namespaceService();

	ContentService contentService();

	AttributeExtractorService attributeExtractor();

	EntityDictionaryService entityDictionaryService();

	AssociationService associationService();

	EntityListDAO entityListDAO();

	VersionService versionService();

	SysAdminParams sysAdminParams();

	LockService lockService();

	MimetypeService mimetypeService();

	PermissionService permissionService();

	PersonService personService();

	AuthorityService authorityService();

	TransactionService transactionService();

	BehaviourFilter policyBehaviourFilter();

}
