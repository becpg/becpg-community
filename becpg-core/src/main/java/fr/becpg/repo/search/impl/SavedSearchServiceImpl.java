package fr.becpg.repo.search.impl;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.repo.search.SavedSearchService;

@Service
@Qualifier("SavedSearchService")
public class SavedSearchServiceImpl implements SavedSearchService {

	@Override
	public String getSavedSearchContent(NodeRef nodeRef) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeRef createOrUpdate(NodeRef savedSearchNodeRef, String searchType, String siteId, String jsonString, boolean isGlobalSavedSearch) {
		// TODO Auto-generated method stub
		return null;
	}

}
