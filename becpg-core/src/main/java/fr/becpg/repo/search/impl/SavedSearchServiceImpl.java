package fr.becpg.repo.search.impl;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemGroup;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.search.SavedSearchService;
import fr.becpg.repo.search.data.SavedSearch;

/**
 * author Matthieu
 */
@Service("SavedSearchService")
public class SavedSearchServiceImpl implements SavedSearchService {

	@Autowired
	private ContentService contentService;

	@Autowired
	@Qualifier("NodeService")
	private NodeService nodeService;

	@Autowired
	private AlfrescoRepository<SavedSearch> alfrescoRepository;

	@Autowired
	private RepoService repoService;

	@Autowired
	private Repository repository;

	@Autowired
	private SiteService siteService;

	@Autowired
	private AuthorityService authorityService;

	@Override
	public String getSavedSearchContent(SavedSearch savedSearch) {

		ContentReader reader = contentService.getReader(savedSearch.getNodeRef(), ContentModel.PROP_CONTENT);

		if (reader != null) {
			return reader.getContentString();
		}
		return null;
	}

	@Override
	public NodeRef createOrUpdate(SavedSearch savedSearch, String jsonString) {

		NodeRef destNodeRef = getSaveSearchFolder(savedSearch);

		if (destNodeRef != null) {
			NodeRef savedSearchNodeRef = savedSearch.getNodeRef() != null ? savedSearch.getNodeRef()
					: nodeService.getChildByName(destNodeRef, ContentModel.ASSOC_CONTAINS, savedSearch.getName());

			if (savedSearchNodeRef != null) {

				savedSearch.setNodeRef(savedSearchNodeRef);
				alfrescoRepository.save(savedSearch);

				if (!savedSearch.getParentNodeRef().equals(destNodeRef)) {
					savedSearch.setParentNodeRef(destNodeRef);
					repoService.moveNode(savedSearch.getNodeRef(), destNodeRef);
				}
			} else {

				savedSearch = alfrescoRepository.create(destNodeRef, savedSearch);
			}

			ContentWriter writer = contentService.getWriter(savedSearch.getNodeRef(), ContentModel.PROP_CONTENT, true);
			if (writer != null) {
				writer.putContent(jsonString);
			}

			return savedSearch.getNodeRef();
		}
		throw new IllegalStateException("Cannot create savedSearch");
	}

	@Override
	public NodeRef getSaveSearchFolder(SavedSearch savedSearch) {

		NodeRef containerNodeRef = null;

		if (!Boolean.TRUE.equals(savedSearch.getIsGlobal())) {
			NodeRef person = repository.getPerson();
			if ((person != null) && (repository.getUserHome(person) != null)) {

				String folderPath = "app:saved_searches";

				String folderName = I18NUtil.getMessage("spaces.savedsearches.name");
				if (folderName == null) {
					folderName = folderPath;
				}
				containerNodeRef = repoService.getFolderByPath(repository.getUserHome(person), folderPath);

				if (containerNodeRef == null) {
					MLText mlTitle = TranslateHelper.getTranslatedKey("spaces.savedsearches.name");
					MLText mlDescription = TranslateHelper.getTranslatedKey("spaces.savedsearches.description");

					containerNodeRef = repoService.getOrCreateFolderByPath(repository.getUserHome(person), folderPath, folderName);
					nodeService.setProperty(containerNodeRef, ContentModel.PROP_TITLE, mlTitle);
					nodeService.setProperty(containerNodeRef, ContentModel.PROP_DESCRIPTION, mlDescription);
				}

			}
		}

		if (containerNodeRef == null) {
			if (!isSearchManagerUser()) {
				return null;
			}
			containerNodeRef = BeCPGQueryBuilder.createQuery().selectNodeByPath(repository.getCompanyHome(), "./app:dictionary/app:saved_searches");
		}

		if (savedSearch.getSiteId() != null) {
			if (Boolean.TRUE.equals(savedSearch.getIsGlobal())) {
				String role = siteService.getMembersRole(savedSearch.getSiteId(), AuthenticationUtil.getFullyAuthenticatedUser());
				if (!(AuthenticationUtil.isRunAsUserTheSystemUser() || SiteModel.SITE_MANAGER.equals(role) || SiteModel.SITE_COLLABORATOR.equals(role))) {
					return null;
				}
			}

			containerNodeRef = repoService.getOrCreateFolderByPath(containerNodeRef, savedSearch.getSiteId(), savedSearch.getSiteId());
		}

		if (savedSearch.getSearchType() != null) {
			String folderName = savedSearch.getSearchType().replace(":", "_");
			containerNodeRef = repoService.getOrCreateFolderByPath(containerNodeRef, folderName, folderName);

		}

		return containerNodeRef;

	}

	@Override
	public List<SavedSearch> findSavedSearch(SavedSearch filter) {
		BeCPGQueryBuilder query = BeCPGQueryBuilder.createQuery().ofType(BeCPGModel.TYPE_SAVED_SEARCH)
				.andPropQuery(BeCPGModel.PROP_SAVED_SEARCH_TYPE, filter.getSearchType()).inDB();

		//I asume that there is few results
		return query.list().stream().map(id -> alfrescoRepository.findOne(id)).filter(s -> (s.getSiteId() == null)
				|| ((filter.getSiteId() != null) && filter.getSiteId().equals(s.getSiteId()))).toList();
	}

	public boolean isSearchManagerUser() {
		if (AuthenticationUtil.isRunAsUserTheSystemUser()) {
			return true;
		}
		for (String currAuth : authorityService.getAuthoritiesForUser(AuthenticationUtil.getFullyAuthenticatedUser())) {
			if ((PermissionService.GROUP_PREFIX + SystemGroup.SavedSearchMgr.toString()).equals(currAuth)) {
				return true;
			}
		}
		return false;
	}

}
