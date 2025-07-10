package fr.becpg.repo.regulatory;

import java.util.List;

/**
 * <p>RequirementAwareEntity interface.</p>
 *
 * @author matthieu
 */
public interface RequirementAwareEntity {


	/**
	 * <p>getReqCtrlList.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	List<RequirementListDataItem> getReqCtrlList();
}
