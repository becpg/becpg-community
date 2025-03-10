package fr.becpg.repo.search;

import fr.becpg.repo.search.data.SearchRuleFilter;
import fr.becpg.repo.search.data.SearchRuleResult;

/**
 * <p>SearchRuleService interface.</p>
 *
 * @author matthieu
 */
public interface SearchRuleService {
	
	/**
	 * <p>search.</p>
	 *
	 * @param filter a {@link fr.becpg.repo.search.data.SearchRuleFilter} object
	 * @return a {@link fr.becpg.repo.search.data.SearchRuleResult} object
	 */
	SearchRuleResult search(SearchRuleFilter filter);
	
}
