package fr.becpg.repo.regulatory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;
import fr.becpg.model.SystemGroup;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.regulatory.decernis.RegulatoryMode;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.repo.repository.RepositoryEntity;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
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
    private AuthorityService authorityService;

    public RegulatoryRouterService(@Qualifier("decernisRegulatoryService") AbstractRegulatoryService decernisRegulatoryService,
                                   @Qualifier("becpgRegulatoryService") AbstractRegulatoryService becpgRegulatoryService,
                                   AlfrescoRepository<RepositoryEntity> alfrescoRepository,
                                   @Qualifier("policyBehaviourFilter") BehaviourFilter policyBehaviourFilter,
                                   AuthorityService authorityService) {
        this.decernisRegulatoryService = decernisRegulatoryService;
        this.becpgRegulatoryService = becpgRegulatoryService;
        this.alfrescoRepository = alfrescoRepository;
        this.policyBehaviourFilter = policyBehaviourFilter;
        this.authorityService = authorityService;
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

        if (!isRegulatoryUser()) {
            // the regulatory check, beCPG regulatory or Decernis, is reserved to GROUP_RegulatoryUser
            result.setStatus(ComplianceResult.Status.NOT_AUTHORIZED);
            return result;
        }

        L2CacheSupport.doInCacheContext(() -> AuthenticationUtil.runAsSystem(() -> {
            ProductData productData = (ProductData) alfrescoRepository.findOne(nodeRef);
            RegulatoryMode regulatoryMode = productData.getRegulatoryMode();

            if (RegulatoryMode.REGULATORY.equals(regulatoryMode)) {
                // The beCPG regulatory plugin is fast enough to run synchronously so the UI can wait
                // for the response and refresh, like formulation does. Unlike the Decernis plugin,
                // it does not need the asynchronous batch + email notification flow (#34620).
                return becpgRegulatoryService.doCheck(false, result, productData);
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

    /**
     * @return whether the current user may run the regulatory check: a member of GROUP_RegulatoryUser, an
     * administrator, or the system (a scheduled job)
     */
    private boolean isRegulatoryUser() {
        if (AuthenticationUtil.isRunAsUserTheSystemUser() || authorityService.hasAdminAuthority()) {
            return true;
        }
        return authorityService.getAuthoritiesForUser(AuthenticationUtil.getRunAsUser())
                .contains(PermissionService.GROUP_PREFIX + SystemGroup.RegulatoryUser);
    }
}
