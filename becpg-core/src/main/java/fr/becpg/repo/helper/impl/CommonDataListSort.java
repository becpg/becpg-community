package fr.becpg.repo.helper.impl;

import java.util.Comparator;
import java.util.Date;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

import fr.becpg.model.BeCPGModel;

/**
 * <p>CommonDataListSort class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class CommonDataListSort implements Comparator<NodeRef>{

	private NodeService nodeService;
	
	
	/**
	 * <p>Constructor for CommonDataListSort.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public CommonDataListSort(NodeService nodeService) {
		super();
		this.nodeService = nodeService;
	}


	/** {@inheritDoc} */
	@Override
	public int compare(NodeRef o1, NodeRef o2) {
		Integer sort1 = (Integer) nodeService.getProperty(o1, BeCPGModel.PROP_SORT);
		Integer sort2 = (Integer) nodeService.getProperty(o2, BeCPGModel.PROP_SORT);

		if (((sort1 != null) && sort1.equals(sort2)) || ((sort1 == null) && (sort2 == null))) {

			Date created1 = (Date) nodeService.getProperty(o1, ContentModel.PROP_CREATED);
			Date created2 = (Date) nodeService.getProperty(o2, ContentModel.PROP_CREATED);

			if (created1 == created2) {
				return 0;
			}

			if (created1 == null) {
				return -1;
			}

			if (created2 == null) {
				return 1;
			}

			return created1.compareTo(created2);
		}

		if (sort1 == null) {
			return -1;
		}

		if (sort2 == null) {
			return 1;
		}

		return sort1.compareTo(sort2);
	}
	

}
