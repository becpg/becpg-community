package fr.becpg.repo.entity.remote.impl;

import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.ServiceRegistry;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.remote.RemoteServiceRegisty;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;

@Service
public class RemoteServiceRegistryImpl implements RemoteServiceRegisty{
	
	@Autowired
	@Qualifier("ServiceRegistry")
	private ServiceRegistry serviceRegistry;

	@Autowired
	@Qualifier("SiteService")
	private SiteService siteService;

	@Autowired
	@Qualifier("NodeService")
	private NodeService nodeService;

	@Autowired
	@Qualifier("mlAwareNodeService")
	protected NodeService mlNodeService;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	@Qualifier("ContentService")
	private ContentService contentService;

	@Autowired
	private AttributeExtractorService attributeExtractor;


	@Autowired
	private TransactionService transactionService;

	@Autowired
	private BehaviourFilter policyBehaviourFilter;


	@Autowired
	private EntityDictionaryService entityDictionaryService;

	@Autowired
	private AssociationService associationService;

	@Autowired
	private EntityListDAO entityListDAO;

	@Autowired
	private VersionService versionService;

	@Autowired
	private SysAdminParams sysAdminParams;

	@Autowired
	private LockService lockService;


	@Override
	public SiteService siteService() {
		return siteService;
	}

	@Override
	public NodeService nodeService() {
		return nodeService;
	}

	@Override
	public NodeService mlNodeService() {
		return mlNodeService;
	}

	@Override
	public NamespaceService namespaceService() {
		return namespaceService;
	}

	@Override
	public ContentService contentService() {
		return contentService;
	}

	@Override
	public AttributeExtractorService attributeExtractor() {
		return attributeExtractor;
	}


	@Override
	public TransactionService transactionService() {
		return transactionService;
	}

	@Override
	public BehaviourFilter policyBehaviourFilter() {
		return policyBehaviourFilter;
	}
	


	@Override
	public EntityDictionaryService entityDictionaryService() {
		return entityDictionaryService;
	}

	@Override
	public AssociationService associationService() {
		return associationService;
	}

	@Override
	public EntityListDAO entityListDAO() {
		return entityListDAO;
	}

	@Override
	public VersionService versionService() {
		return versionService;
	}

	@Override
	public SysAdminParams sysAdminParams() {
		return sysAdminParams;
	}

	@Override
	public LockService lockService() {
		return lockService;
	}

	@Override
	public MimetypeService mimetypeService() {
		return serviceRegistry.getMimetypeService();
	}

	@Override
	public PermissionService permissionService() {
		return serviceRegistry.getPermissionService();
	}

	@Override
	public PersonService personService() {
		return serviceRegistry.getPersonService();
	}

	@Override
	public AuthorityService authorityService() {
		return serviceRegistry.getAuthorityService();
	}

	

}
