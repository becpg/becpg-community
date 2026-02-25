/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.web.scripts.product;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.batch.BatchQueueService;
import fr.becpg.repo.batch.BatchStep;
import fr.becpg.repo.batch.EntityListBatchProcessWorkProvider;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.impl.EntitySourceAssoc;
import fr.becpg.repo.repository.L2CacheSupport;

/**
 * <p>FormulateWUsedWebScript class.</p>
 *
 * @author matthieu
 */
public class FormulateWUsedWebScript extends AbstractWebScript {

	private static final Log logger = LogFactory.getLog(FormulateWUsedWebScript.class);

	private static final String PARAM_NODEREFS = "nodeRefs";

	private NodeService nodeService;
	
	private EntityDictionaryService entityDictionaryService;
	
	private AssociationService associationService;
	
	private BatchQueueService batchQueueService;
	
	private FormulationService<FormulatedEntity> formulationService;
	
	private BehaviourFilter policyBehaviourFilter;
	
	/**
	 * <p>Setter for the field <code>policyBehaviourFilter</code>.</p>
	 *
	 * @param policyBehaviourFilter a {@link org.alfresco.repo.policy.BehaviourFilter} object
	 */
	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	/**
	 * <p>Setter for the field <code>formulationService</code>.</p>
	 *
	 * @param formulationService a {@link fr.becpg.repo.formulation.FormulationService} object
	 */
	public void setFormulationService(FormulationService<FormulatedEntity> formulationService) {
		this.formulationService = formulationService;
	}
	
	/**
	 * <p>Setter for the field <code>batchQueueService</code>.</p>
	 *
	 * @param batchQueueService a {@link fr.becpg.repo.batch.BatchQueueService} object
	 */
	public void setBatchQueueService(BatchQueueService batchQueueService) {
		this.batchQueueService = batchQueueService;
	}
	
	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}
	
	/**
	 * <p>Setter for the field <code>entityDictionaryService</code>.</p>
	 *
	 * @param entityDictionaryService a {@link fr.becpg.repo.entity.EntityDictionaryService} object
	 */
	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		List<NodeRef> nodeRefs = Arrays.asList(req.getParameter(PARAM_NODEREFS).split(",")).stream().map(NodeRef::new).toList();
		for (NodeRef nodeRef : nodeRefs) {
			Set<NodeRef> toFormulate = new HashSet<>();
			QName type = nodeService.getType(nodeRef);
			List<QName> pivotAssocs = entityDictionaryService.getDefaultPivotAssocsFromTargetType(type);
			for (QName pivotAssoc : pivotAssocs) {
				List<EntitySourceAssoc> entitySourceAssocs = associationService.getEntitySourceAssocs(List.of(nodeRef), pivotAssoc, null, false, null);
				toFormulate.addAll(entitySourceAssocs.stream().map(e -> e.getEntityNodeRef()).toList());
			}
			
			String charactName = (String) nodeService.getProperty(nodeRef, BeCPGModel.PROP_CHARACT_NAME);
			
			BatchInfo batchInfo = new BatchInfo(String.format("becpg.batch.formulateWUsed-%s", nodeRef), "becpg.batch.formulateWUsed", charactName);
			BatchStep<NodeRef> step = new BatchStep<>();
			step.setWorkProvider(new EntityListBatchProcessWorkProvider<>(new ArrayList<>(toFormulate)));
			logger.info("toFormulate: " + toFormulate.size() + " from " + nodeRef);
			step.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<>() {
				public void process(NodeRef productNodeRef) throws Throwable {
					try {
						policyBehaviourFilter.disableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
						policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
						policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);

						L2CacheSupport.doInCacheContext(() -> AuthenticationUtil.runAsSystem(() -> {
							formulationService.formulate(productNodeRef);
							return true;
						}), false, true);

					} finally {
						policyBehaviourFilter.enableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
						policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
						policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
					}

				}
			});
			batchQueueService.queueBatch(batchInfo, List.of(step));
		}
	}

}
