/*
 * 
 */
package fr.becpg.repo.entity.policy;

import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.AutoNumService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * The Class CodePolicy.
 * 
 * @author querephi
 */
@Service
public class CodePolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnAddAspectPolicy {

	private static final String QUERY_NODE_BY_CODE = " +TYPE:\"%s\" +@bcpg\\:code:\"%s\"  -@sys\\:node-uuid:\"%s\" ";

	/** The logger. */
	private static Log logger = LogFactory.getLog(CodePolicy.class);

	/** The auto num service. */
	private AutoNumService autoNumService;

	/** The search service. */
	private BeCPGSearchService beCPGSearchService;

	/**
	 * Sets the policy component.
	 * 
	 * @param policyComponent
	 *            the new policy component
	 */
	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}


	/**
	 * Sets the auto num service.
	 * 
	 * @param autoNumService
	 *            the new auto num service
	 */
	public void setAutoNumService(AutoNumService autoNumService) {
		this.autoNumService = autoNumService;
	}

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	/**
	 * Inits the.
	 */
	public void doInit() {
		logger.debug("Init CodePolicy...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnAddAspectPolicy.QNAME, BeCPGModel.ASPECT_CODE, new JavaBehaviour(this, "onAddAspect"));

	}
	
	@Override
	public void onAddAspect(NodeRef nodeRef, QName type) {
		
		if (isNotLocked(nodeRef) && !isWorkingCopyOrVersion(nodeRef) ) {

			// check code is already taken. If yes : this object is
			// a copy of an
			// existing node
			String code = (String) nodeService.getProperty(nodeRef, BeCPGModel.PROP_CODE);
			boolean generateCode = true;
			QName typeQName = nodeService.getType(nodeRef);

			if (code != null && !code.isEmpty()) {
				List<NodeRef> ret = beCPGSearchService.luceneSearch(String.format(QUERY_NODE_BY_CODE, typeQName, code, nodeRef.getId()), RepoConsts.MAX_RESULTS_SINGLE_VALUE);
				generateCode = ret != null && !ret.isEmpty();
			}

			// generate a new code
			if (generateCode) {
				String c = autoNumService.getAutoNumValue(typeQName, BeCPGModel.PROP_CODE);
				nodeService.setProperty(nodeRef, BeCPGModel.PROP_CODE, c);
			} else {
				// store autoNum in db
				autoNumService.createOrUpdateAutoNumValue(typeQName, BeCPGModel.PROP_CODE, code);
			}
		}
	}

}
