/*
 * 
 */
package fr.becpg.repo.entity.datalist.policy;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.datalist.DataListSortService;

/**
 * The Class SortableListPolicy.
 * 
 * @author querephi
 */
public class SortableListPolicy implements NodeServicePolicies.OnAddAspectPolicy {

	private static Log logger = LogFactory.getLog(SortableListPolicy.class);

	private PolicyComponent policyComponent;

	private DataListSortService dataListSortService;

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setDataListSortService(DataListSortService dataListSortService) {
		this.dataListSortService = dataListSortService;
	}

	/**
	 * Inits the.
	 */
	public void init() {
		logger.debug("Init SortableListPolicy...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnAddAspectPolicy.QNAME, BeCPGModel.ASPECT_SORTABLE_LIST, new JavaBehaviour(this, "onAddAspect"));
	}

	@Override
	public void onAddAspect(NodeRef nodeRef, QName aspect) {

		if (aspect.isMatch(BeCPGModel.ASPECT_SORTABLE_LIST)) {

			if (logger.isDebugEnabled()) {
				logger.debug("Add sortable aspect policy ");
			}

			dataListSortService.createSortIndex(nodeRef);
		}
	}

}
