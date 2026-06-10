package fr.becpg.repo.regulatory;

import org.alfresco.service.cmr.repository.NodeRef;

public interface RegulatoryService {
    ComplianceResult checkCompliance(NodeRef nodeRef, boolean async);
}
