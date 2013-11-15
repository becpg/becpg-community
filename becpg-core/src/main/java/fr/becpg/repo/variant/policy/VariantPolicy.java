package fr.becpg.repo.variant.policy;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.search.BeCPGSearchService;

@Service
public class VariantPolicy extends AbstractBeCPGPolicy implements CopyServicePolicies.OnCopyCompletePolicy, CheckOutCheckInServicePolicies.OnCheckOut,
		CheckOutCheckInServicePolicies.BeforeCheckIn {

	private static Log logger = LogFactory.getLog(VariantPolicy.class);

	private BeCPGSearchService beCPGSearchService;

	private CopyService copyService;

	private EntityListDAO entityListDAO;

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	public void setCopyService(CopyService copyService) {
		this.copyService = copyService;
	}

	public void doInit() {

		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyCompletePolicy.QNAME, BeCPGModel.TYPE_VARIANT, new JavaBehaviour(this, "onCopyComplete"));
		policyComponent.bindClassBehaviour(CheckOutCheckInServicePolicies.OnCheckOut.QNAME, BeCPGModel.ASPECT_ENTITY_VARIANT, new JavaBehaviour(this, "onCheckOut"));
		policyComponent.bindClassBehaviour(CheckOutCheckInServicePolicies.BeforeCheckIn.QNAME, BeCPGModel.ASPECT_ENTITY_VARIANT, new JavaBehaviour(this, "beforeCheckIn"));

	}

	public static String VARIANTS_TO_UPDATE;

	@Override
	public void onCopyComplete(QName classRef, NodeRef sourceNodeRef, NodeRef destinationRef, boolean copyToNewNode, Map<NodeRef, NodeRef> copyMap) {
		logger.debug("On copy complete Variant");

		
		NodeRef entityNodeRef = nodeService.getPrimaryParent(destinationRef).getParentRef();
		
		String query = LuceneHelper.mandatory(LuceneHelper.getCondAspect(BeCPGModel.ASPECT_ENTITYLIST_VARIANT));
		query += LuceneHelper.getCondEqualValue(BeCPGModel.PROP_VARIANTIDS, sourceNodeRef.toString(), LuceneHelper.Operator.AND);
		
		logger.debug("Search for"+query);
		
		List<NodeRef> result = beCPGSearchService.luceneSearch(query, RepoConsts.MAX_RESULTS_UNLIMITED);

		if (!result.isEmpty()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Found " + result.size() + " entityDataList to check for update variant");
			}
			for (NodeRef entityDataListNodeRef : result) {

				if (entityNodeRef.equals(entityListDAO.getEntity(entityDataListNodeRef))) {
					logger.debug("Ok for replacement");
					@SuppressWarnings("unchecked")
					List<NodeRef> variantIds = (List<NodeRef>) nodeService.getProperty(entityDataListNodeRef, BeCPGModel.PROP_VARIANTIDS);
					variantIds.remove(sourceNodeRef);
					variantIds.add(destinationRef);

					nodeService.setProperty(entityDataListNodeRef, BeCPGModel.PROP_VARIANTIDS, (Serializable) variantIds);
				}
			}
		}
	}

	@Override
	public void onCheckOut(final NodeRef workingCopyNodeRef) {

		AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {
			@Override
			public Void doWork() throws Exception {

				NodeRef origNodeRef = getCheckedOut(workingCopyNodeRef);

				// Copy variants

				List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(origNodeRef, BeCPGModel.ASSOC_VARIANTS, RegexQNamePattern.MATCH_ALL);
				for (ChildAssociationRef childAssoc : childAssocs) {
					if (logger.isDebugEnabled()) {
						logger.debug("Copy variant " + nodeService.getProperty(childAssoc.getChildRef(), ContentModel.PROP_NAME) + " to workingCopy ");
					}

					copyService.copy(childAssoc.getChildRef(), workingCopyNodeRef,BeCPGModel.ASSOC_VARIANTS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, 
	                        QName.createValidLocalName((String)nodeService.getProperty(childAssoc.getChildRef(), ContentModel.PROP_NAME))),false);
				}

				return null;

			}
		}, AuthenticationUtil.getSystemUserName());

	}

	@Override
	public void beforeCheckIn(final NodeRef workingCopyNodeRef, Map<String, Serializable> versionProperties, String contentUrl, boolean keepCheckedOut) {
		AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() {
			@Override
			public Void doWork() throws Exception {
				NodeRef origNodeRef = getCheckedOut(workingCopyNodeRef);

				if(origNodeRef!=null){
					List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(origNodeRef, BeCPGModel.ASSOC_VARIANTS, RegexQNamePattern.MATCH_ALL);
					for (ChildAssociationRef childAssoc : childAssocs) {
						nodeService.removeChildAssociation(childAssoc);
					}
				}

				return null;

			}
		}, AuthenticationUtil.getSystemUserName());
	}

	private NodeRef getCheckedOut(NodeRef nodeRef) {
		NodeRef original = null;
		if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)) {
			List<AssociationRef> assocs = nodeService.getSourceAssocs(nodeRef, ContentModel.ASSOC_WORKING_COPY_LINK);
			// It is a 1:1 relationship
			if (!assocs.isEmpty()) {
				if (logger.isWarnEnabled()) {
					if (assocs.size() > 1) {
						logger.warn("Found multiple " + ContentModel.ASSOC_WORKING_COPY_LINK + " associations to node: " + nodeRef);
					}
				}
				original = assocs.get(0).getSourceRef();
			} else {
				logger.warn("No working copy link found");
			}
		} else{
			logger.warn("Node is not a working copy");
		}

		return original;
	}

}
