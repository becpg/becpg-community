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

/**
 * <p>RemoteServiceRegistryImpl class.</p>
 *
 * @author matthieu
 */
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


	/** {@inheritDoc} */
	@Override
	public SiteService siteService() {
		return siteService;
	}

	/** {@inheritDoc} */
	@Override
	public NodeService nodeService() {
		return nodeService;
	}

	/** {@inheritDoc} */
	@Override
	public NodeService mlNodeService() {
		return mlNodeService;
	}

	/** {@inheritDoc} */
	@Override
	public NamespaceService namespaceService() {
		return namespaceService;
	}

	/** {@inheritDoc} */
	@Override
	public ContentService contentService() {
		return contentService;
	}

	/** {@inheritDoc} */
	@Override
	public AttributeExtractorService attributeExtractor() {
		return attributeExtractor;
	}


	/** {@inheritDoc} */
	@Override
	public TransactionService transactionService() {
		return transactionService;
	}

	/** {@inheritDoc} */
	@Override
	public BehaviourFilter policyBehaviourFilter() {
		return policyBehaviourFilter;
	}
	


	/** {@inheritDoc} */
	@Override
	public EntityDictionaryService entityDictionaryService() {
		return entityDictionaryService;
	}

	/** {@inheritDoc} */
	@Override
	public AssociationService associationService() {
		return associationService;
	}

	/** {@inheritDoc} */
	@Override
	public EntityListDAO entityListDAO() {
		return entityListDAO;
	}

	/** {@inheritDoc} */
	@Override
	public VersionService versionService() {
		return versionService;
	}

	/** {@inheritDoc} */
	@Override
	public SysAdminParams sysAdminParams() {
		return sysAdminParams;
	}

	/** {@inheritDoc} */
	@Override
	public LockService lockService() {
		return lockService;
	}

	/** {@inheritDoc} */
	@Override
	public MimetypeService mimetypeService() {
		return serviceRegistry.getMimetypeService();
	}

	/** {@inheritDoc} */
	@Override
	public PermissionService permissionService() {
		return serviceRegistry.getPermissionService();
	}

	/** {@inheritDoc} */
	@Override
	public PersonService personService() {
		return serviceRegistry.getPersonService();
	}

	/** {@inheritDoc} */
	@Override
	public AuthorityService authorityService() {
		return serviceRegistry.getAuthorityService();
	}

	

}
