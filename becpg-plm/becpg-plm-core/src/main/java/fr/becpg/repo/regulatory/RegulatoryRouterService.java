package fr.becpg.repo.regulatory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.regulatory.decernis.RegulatoryMode;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.repo.repository.RepositoryEntity;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class RegulatoryRouterService {
    private static final Logger logger = LoggerFactory.getLogger(RegulatoryRouterService.class);

    private AbstractRegulatoryService decernisRegulatoryService;
    private AbstractRegulatoryService becpgRegulatoryService;
    private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
    private BehaviourFilter policyBehaviourFilter;

    public RegulatoryRouterService(@Qualifier("decernisRegulatoryService") AbstractRegulatoryService decernisRegulatoryService,
                                   @Qualifier("becpgRegulatoryService") AbstractRegulatoryService becpgRegulatoryService,
                                   AlfrescoRepository<RepositoryEntity> alfrescoRepository,
                                   @Qualifier("policyBehaviourFilter") BehaviourFilter policyBehaviourFilter) {
        this.decernisRegulatoryService = decernisRegulatoryService;
        this.becpgRegulatoryService = becpgRegulatoryService;
        this.alfrescoRepository = alfrescoRepository;
        this.policyBehaviourFilter = policyBehaviourFilter;
    }

    /**
     * <p>checkCompliance.</p>
     *
     * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
     * @param async   a boolean
     * @return a {@link ComplianceResult} object
     */
    public ComplianceResult checkCompliance(NodeRef nodeRef, boolean async) {
        policyBehaviourFilter.disableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
        policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
        policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);

        ComplianceResult result = new ComplianceResult();

        L2CacheSupport.doInCacheContext(() -> AuthenticationUtil.runAsSystem(() -> {
            ProductData productData = (ProductData) alfrescoRepository.findOne(nodeRef);
            RegulatoryMode regulatoryMode = productData.getRegulatoryMode();

            if (RegulatoryMode.REGULATORY.equals(regulatoryMode)) {
                return becpgRegulatoryService.doCheck(async, result, productData);
            } else if (regulatoryMode != null && !RegulatoryMode.DISABLED.equals(regulatoryMode)) {
                return decernisRegulatoryService.doCheck(async, result, productData);
            } else {
                result.setStatus(ComplianceResult.Status.NOT_APPLICABLE);
                logger.debug("Product is not compatible for compliance check");
                return false;
            }
        }), false, true);

        return result;
    }
}
