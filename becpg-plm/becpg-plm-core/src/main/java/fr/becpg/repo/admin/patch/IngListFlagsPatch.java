/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG. 
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
package fr.becpg.repo.admin.patch;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorkerAdaptor;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.helper.IngListLegacyFlags;

/**
 * Aligns the multi-valued bcpg:ingListFlags property and the deprecated boolean properties of every
 * ingredient line (GMO, ionized, processing aid, support). A line written before the flags existed gets
 * its flags from the booleans; a line whose booleans were removed by the first version of this patch
 * gets them back from its flags. Both stay stored afterwards, kept in sync by the ingredient line policy.
 *
 * @author matthieu
 */
public class IngListFlagsPatch extends AbstractBeCPGPatch {

    private static final Log logger = LogFactory.getLog(IngListFlagsPatch.class);

    private static final String MSG_SUCCESS = "patch.bcpg.plm.ingListFlagsSyncPatch.result";

    private BehaviourFilter policyBehaviourFilter;

    private RuleService ruleService;

    /**
     * <p>Setter for the field <code>policyBehaviourFilter</code>.</p>
     *
     * @param policyBehaviourFilter the behaviour filter, disabled while migrating
     */
    public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
        this.policyBehaviourFilter = policyBehaviourFilter;
    }

    /**
     * <p>Setter for the field <code>ruleService</code>.</p>
     *
     * @param ruleService the rule service, disabled while migrating
     */
    public void setRuleService(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    /** {@inheritDoc} */
    @Override
    protected String applyInternal() throws Exception {
        String tenantDomain = TenantUtil.getCurrentDomain();
        BatchProcessor<NodeRef> batchProcessor = createBatchTypeProcessor(PLMModel.TYPE_INGLIST, true);
        batchProcessor.processLong(createWorker(tenantDomain), true);
        return I18NUtil.getMessage(MSG_SUCCESS);
    }

    private BatchProcessWorker<NodeRef> createWorker(String tenantDomain) {
        return new BatchProcessWorkerAdaptor<>() {
            @Override
            public void process(NodeRef ingListNodeRef) throws Throwable {
                AuthenticationUtil.runAs(() -> {
                    migrateInTransaction(ingListNodeRef);
                    return null;
                }, tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain));
            }
        };
    }

    private void migrateInTransaction(NodeRef ingListNodeRef) {
        policyBehaviourFilter.disableBehaviour();
        ruleService.disableRules();
        IntegrityChecker.setWarnInTransaction();
        try {
            migrate(ingListNodeRef);
        } finally {
            ruleService.enableRules();
            policyBehaviourFilter.enableBehaviour();
        }
    }

    private void migrate(NodeRef ingListNodeRef) {
        if (!nodeService.exists(ingListNodeRef)) {
            logger.warn("Ingredient line does not exist: " + ingListNodeRef);
            return;
        }
        Map<QName, Serializable> properties = nodeService.getProperties(ingListNodeRef);
        Map<QName, Serializable> changes = IngListLegacyFlags.synchronize(properties, properties);
        if (!changes.isEmpty()) {
            nodeService.addProperties(ingListNodeRef, changes);
        }
    }
}
