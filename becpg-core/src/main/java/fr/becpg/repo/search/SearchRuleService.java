package fr.becpg.repo.search;

import fr.becpg.repo.search.data.SearchRuleFilter;
import fr.becpg.repo.search.data.SearchRuleResult;

public interface SearchRuleService {
	
	SearchRuleResult search(SearchRuleFilter filter);
	
}
