package fr.becpg.repo.product.report;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import fr.becpg.model.ReportModel;
import fr.becpg.repo.helper.MessageHelper;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.repo.report.pdf.ReportPdfAggregator.AggregateReportConfig;
import fr.becpg.repo.report.pdf.ReportPdfAggregator.AnnexConfig;
import fr.becpg.repo.report.pdf.ReportPdfAggregator.AnnexDocument;
import fr.becpg.repo.report.pdf.ReportPdfAggregator.AnnexSection;
import fr.becpg.repo.repository.AlfrescoRepository;

import java.io.InputStream;
import java.util.*;

@Component("aggregateReportModelBuilder")
public class AggregateReportModelBuilder {

    private static final Log logger = LogFactory.getLog(AggregateReportModelBuilder.class);

    @Autowired
    private NodeService nodeService;

    @Autowired
    private ContentService contentService;

    @Autowired
    private FileFolderService fileFolderService;

    @Autowired
    @Qualifier("alfrescoRepository")
    private AlfrescoRepository<ProductData> alfrescoRepository;

    @Autowired
    private NamespaceService namespaceService;

    @Autowired
    private EntityReportService entityReportService;

    public List<AnnexSection> buildAnnexSections(NodeRef fpNodeRef, AggregateReportConfig config) {
        return buildAnnexSections(fpNodeRef, config, null);
    }

    public List<AnnexSection> buildAnnexSections(NodeRef fpNodeRef, AggregateReportConfig config, Map<String, String> customI18n) {
        List<AnnexSection> sections = new ArrayList<>();
        if (config.getAnnexes() == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("No annexes configured in AggregateReportConfig for entity: " + fpNodeRef);
            }
            return sections;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Building annex sections for entity " + fpNodeRef + " with " + config.getAnnexes().size() + " configured annexes");
        }

        for (AnnexConfig annex : config.getAnnexes()) {
            List<AnnexDocument> documents = new ArrayList<>();
            String scope = annex.getScope();

            if (logger.isDebugEnabled()) {
                logger.debug("Processing annex config - reportKind: " + annex.getReportKind() + ", scope: " + scope + ", title: " + annex.getTitle()
                        + ", required: " + annex.isRequired() + ", recurse: " + annex.isRecurse() + ", pkgLevel: " + annex.getPkgLevel());
            }

            if ("ENTITY".equalsIgnoreCase(scope)) {
                collectEntityAnnex(fpNodeRef, annex, documents);
            } else if ("COMPO_CHILDREN".equalsIgnoreCase(scope)) {
                collectCompoAnnex(fpNodeRef, annex, documents);
            } else if ("PACKAGING_CHILDREN".equalsIgnoreCase(scope)) {
                collectPackagingAnnex(fpNodeRef, annex, documents);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Unknown annex scope: " + scope + " for reportKind: " + annex.getReportKind());
                }
            }

            String resolvedTitle = resolveI18nKey(annex.getTitle(), customI18n);
            String resolvedPlaceholder = resolveI18nKey(annex.getEmptyPlaceholder(), customI18n);

            if (documents.isEmpty()) {
                if (annex.isRequired()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Annex section '" + resolvedTitle + "' (kind: " + annex.getReportKind() + ") has no documents but is required. Adding placeholder section.");
                    }
                    sections.add(new AnnexSection(annex.getReportKind(), resolvedTitle, documents, resolvedPlaceholder));
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Annex section '" + resolvedTitle + "' (kind: " + annex.getReportKind() + ") has no documents and is not required. Skipping.");
                    }
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Adding annex section '" + resolvedTitle + "' (kind: " + annex.getReportKind() + ") with " + documents.size() + " documents");
                }
                sections.add(new AnnexSection(annex.getReportKind(), resolvedTitle, documents, resolvedPlaceholder));
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Finished building annex sections for entity " + fpNodeRef + ". Total sections built: " + sections.size());
        }

        return sections;
    }

    private String resolveI18nKey(String key, Map<String, String> customI18n) {
        if (key == null || key.trim().isEmpty()) {
            return key;
        }
        if (customI18n != null && customI18n.containsKey(key)) {
            String val = customI18n.get(key);
            if (val != null && !val.trim().isEmpty()) {
                return val;
            }
        }
        try {
            String msg = MessageHelper.getMessage(key);
            if (msg != null && !msg.trim().isEmpty()) {
                return msg;
            }
        } catch (Exception e) {
            logger.debug("Failed to resolve i18n key: " + key, e);
        }
        return key;
    }

    private void collectEntityAnnex(NodeRef fpNodeRef, AnnexConfig annex, List<AnnexDocument> documents) {
        if (logger.isDebugEnabled()) {
            logger.debug("Collecting ENTITY annex for node: " + fpNodeRef + ", reportKind: " + annex.getReportKind());
        }
        List<AnnexDocument> docs = collectDocumentsForNode(fpNodeRef, annex.getReportKind(), annex.getMimeTypes());
        documents.addAll(docs);
        if (logger.isDebugEnabled()) {
            logger.debug("Collected " + docs.size() + " ENTITY documents for node: " + fpNodeRef + ", reportKind: " + annex.getReportKind());
        }
    }

    private void collectCompoAnnex(NodeRef fpNodeRef, AnnexConfig annex, List<AnnexDocument> documents) {
        if (logger.isDebugEnabled()) {
            logger.debug("Collecting COMPO_CHILDREN annex for node: " + fpNodeRef + ", reportKind: " + annex.getReportKind() + ", recurse: " + annex.isRecurse() + ", allowedTypes: " + annex.getComponentTypes());
        }
        List<NodeRef> compoComponents = new ArrayList<>();
        collectCompoComponents(fpNodeRef, compoComponents, new HashSet<>(), annex.isRecurse(), annex.getComponentTypes());
        if (annex.isDedup()) {
            compoComponents = new ArrayList<>(new LinkedHashSet<>(compoComponents));
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Found " + compoComponents.size() + " composition components for node: " + fpNodeRef + ": " + compoComponents);
        }
        for (NodeRef compNode : compoComponents) {
            List<AnnexDocument> docs = collectDocumentsForNode(compNode, annex.getReportKind(), annex.getMimeTypes());
            if (logger.isDebugEnabled()) {
                logger.debug("Collected " + docs.size() + " documents for composition component: " + compNode + " (reportKind: " + annex.getReportKind() + ")");
            }
            documents.addAll(docs);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Total COMPO_CHILDREN documents collected: " + documents.size() + " for reportKind: " + annex.getReportKind());
        }
    }

    private void collectPackagingAnnex(NodeRef fpNodeRef, AnnexConfig annex, List<AnnexDocument> documents) {
        if (logger.isDebugEnabled()) {
            logger.debug("Collecting PACKAGING_CHILDREN annex for node: " + fpNodeRef + ", reportKind: " + annex.getReportKind() + ", pkgLevel: " + annex.getPkgLevel());
        }
        List<NodeRef> packagingComponents = new ArrayList<>();
        collectPackagingComponents(fpNodeRef, packagingComponents, annex.getPkgLevel());
        if (annex.isDedup()) {
            packagingComponents = new ArrayList<>(new LinkedHashSet<>(packagingComponents));
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Found " + packagingComponents.size() + " packaging components for node: " + fpNodeRef + ": " + packagingComponents);
        }
        for (NodeRef pkgNode : packagingComponents) {
            List<AnnexDocument> docs = collectDocumentsForNode(pkgNode, annex.getReportKind(), annex.getMimeTypes());
            if (logger.isDebugEnabled()) {
                logger.debug("Collected " + docs.size() + " documents for packaging component: " + pkgNode + " (reportKind: " + annex.getReportKind() + ")");
            }
            documents.addAll(docs);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Total PACKAGING_CHILDREN documents collected: " + documents.size() + " for reportKind: " + annex.getReportKind());
        }
    }

    private void collectCompoComponents(NodeRef productNodeRef, List<NodeRef> collected, Set<NodeRef> visited, boolean recurse, List<String> allowedTypes) {
        if (productNodeRef == null || !visited.add(productNodeRef)) {
            if (logger.isDebugEnabled() && productNodeRef != null) {
                logger.debug("Already visited composition node: " + productNodeRef + ", skipping recursion");
            }
            return;
        }
        try {
            ProductData productData = (ProductData) alfrescoRepository.findOne(productNodeRef);
            if (productData == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("ProductData not found for node: " + productNodeRef);
                }
                return;
            }
            List<CompoListDataItem> compoList = productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE));
            if (logger.isDebugEnabled()) {
                logger.debug("Retrieved compoList (size: " + (compoList != null ? compoList.size() : 0) + ") for product: " + productNodeRef);
            }
            if (compoList != null) {
                for (CompoListDataItem item : compoList) {
                    NodeRef compNodeRef = item.getProduct();
                    if (compNodeRef != null) {
                        QName type = nodeService.getType(compNodeRef);
                        String prefixType = type.toPrefixString(namespaceService);
                        boolean isAllowed = allowedTypes == null || allowedTypes.isEmpty() || allowedTypes.contains(prefixType);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Inspecting composition child component: " + compNodeRef + ", type: " + prefixType + ", isAllowed: " + isAllowed);
                        }
                        if (isAllowed) {
                            if (!collected.contains(compNodeRef)) {
                                collected.add(compNodeRef);
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Added composition component to list: " + compNodeRef);
                                }
                            }
                        }
                        if (recurse) {
                            collectCompoComponents(compNodeRef, collected, visited, recurse, allowedTypes);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Exception while traversing composition for product: " + e.getMessage(), e);
        }
    }

    private void collectPackagingComponents(NodeRef productNodeRef, List<NodeRef> collected, String targetPkgLevel) {
        try {
            ProductData productData = (ProductData) alfrescoRepository.findOne(productNodeRef);
            if (productData == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("ProductData not found for packaging node: " + productNodeRef);
                }
                return;
            }
            List<PackagingListDataItem> pkgList = productData.getPackagingList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE));
            if (logger.isDebugEnabled()) {
                logger.debug("Retrieved packagingList (size: " + (pkgList != null ? pkgList.size() : 0) + ") for product: " + productNodeRef);
            }
            if (pkgList != null) {
                for (PackagingListDataItem item : pkgList) {
                    NodeRef pkgNodeRef = item.getProduct();
                    if (pkgNodeRef != null) {
                        PackagingLevel level = item.getPkgLevel();
                        boolean levelMatches = true;
                        if (targetPkgLevel != null && !targetPkgLevel.isEmpty()) {
                            levelMatches = level != null && level.name().equalsIgnoreCase(targetPkgLevel);
                        }
                        if (logger.isDebugEnabled()) {
                            logger.debug("Inspecting packaging child component: " + pkgNodeRef + ", level: " + level + ", targetPkgLevel: " + targetPkgLevel + ", matches: " + levelMatches);
                        }
                        if (levelMatches) {
                            if (!collected.contains(pkgNodeRef)) {
                                collected.add(pkgNodeRef);
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Added packaging component to list: " + pkgNodeRef);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Exception while traversing packaging for product: " + e.getMessage(), e);
        }
    }

    private List<AnnexDocument> collectDocumentsForNode(NodeRef entityNodeRef, String reportKind, List<String> mimeTypes) {
        List<AnnexDocument> results = new ArrayList<>();
        Set<NodeRef> collectedNodeRefs = new HashSet<>();

        if (logger.isDebugEnabled()) {
            logger.debug("collectDocumentsForNode - entityNodeRef: " + entityNodeRef + ", reportKind: " + reportKind + ", mimeTypes: " + mimeTypes);
        }

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Triggering getOrRefreshReportsOfKind for node " + entityNodeRef + ", reportKind: " + reportKind);
            }
            entityReportService.getOrRefreshReportsOfKind(entityNodeRef, reportKind);
        } catch (Exception e) {
            logger.error("On-the-fly report refresh failed for node " + entityNodeRef + ": " + e.getMessage(), e);
        }

        String compName = (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_TITLE);
        if (compName == null || compName.isEmpty()) {
            compName = (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);
        }

        collectReportsOfKind(entityNodeRef, reportKind, compName, results, collectedNodeRefs);
        if (logger.isDebugEnabled()) {
            logger.debug("After collectReportsOfKind: " + results.size() + " documents for node " + entityNodeRef);
        }

        collectFilesRecursively(entityNodeRef, reportKind, mimeTypes, compName, results, 0, collectedNodeRefs);
        if (logger.isDebugEnabled()) {
            logger.debug("After collectFilesRecursively: " + results.size() + " total documents for node " + entityNodeRef);
        }

        return results;
    }

    private void collectReportsOfKind(NodeRef entityNodeRef, String reportKind, String compName, List<AnnexDocument> results, Set<NodeRef> collectedNodeRefs) {
        try {
            List<NodeRef> reportsOfKind = entityReportService.getReportsOfKind(entityNodeRef, reportKind);
            if (logger.isDebugEnabled()) {
                logger.debug("entityReportService.getReportsOfKind(" + entityNodeRef + ", '" + reportKind + "') returned: " + reportsOfKind);
            }
            if (reportsOfKind != null) {
                for (NodeRef reportNodeRef : reportsOfKind) {
                    if (collectedNodeRefs.add(reportNodeRef)) {
                        ContentReader reader = contentService.getReader(reportNodeRef, ContentModel.PROP_CONTENT);
                        if (reader != null && reader.exists()) {
                            try (InputStream in = reader.getContentInputStream()) {
                                byte[] bytes = in.readAllBytes();
                                if (bytes != null && bytes.length > 0) {
                                    results.add(new AnnexDocument(compName, bytes));
                                    if (logger.isDebugEnabled()) {
                                        logger.debug("Collected report node " + reportNodeRef + " (" + bytes.length + " bytes) for reportKind: " + reportKind);
                                    }
                                } else {
                                    if (logger.isDebugEnabled()) {
                                        logger.debug("Report node " + reportNodeRef + " content is empty");
                                    }
                                }
                            }
                        } else {
                            if (logger.isDebugEnabled()) {
                                logger.debug("ContentReader missing or does not exist for report node: " + reportNodeRef);
                            }
                        }
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Report node " + reportNodeRef + " already collected, skipping");
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to collect rep:report files for node " + entityNodeRef + ": " + e.getMessage(), e);
        }
    }

    private void collectFilesRecursively(NodeRef folderNodeRef, String reportKind, List<String> mimeTypes, String compName, List<AnnexDocument> results, int depth, Set<NodeRef> collectedNodeRefs) {
        if (depth > 2) {
            if (logger.isDebugEnabled()) {
                logger.debug("Max depth reached (" + depth + "), skipping folder: " + folderNodeRef);
            }
            return;
        }
        List<FileInfo> fileInfos = fileFolderService.list(folderNodeRef);
        if (logger.isDebugEnabled()) {
            logger.debug("collectFilesRecursively at depth " + depth + " for folder " + folderNodeRef + " found " + (fileInfos != null ? fileInfos.size() : 0) + " items");
        }
        if (fileInfos != null) {
            for (FileInfo file : fileInfos) {
                if (file.isFolder()) {
                    collectFilesRecursively(file.getNodeRef(), reportKind, mimeTypes, compName, results, depth + 1, collectedNodeRefs);
                } else {
                    NodeRef fileNodeRef = file.getNodeRef();
                    if (collectedNodeRefs.contains(fileNodeRef)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("File node " + fileNodeRef + " already collected, skipping");
                        }
                        continue;
                    }
                    ContentReader reader = contentService.getReader(fileNodeRef, ContentModel.PROP_CONTENT);
                    if (reader != null && reader.exists()) {
                        String mt = reader.getMimetype();
                        boolean mimeAllowed = mimeTypes == null || mimeTypes.contains(mt);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Inspecting file " + fileNodeRef + " (" + file.getName() + "), mimetype: " + mt + ", mimeAllowed: " + mimeAllowed);
                        }
                        if (mimeAllowed) {
                            boolean hasAspect = nodeService.hasAspect(fileNodeRef, ReportModel.ASPECT_REPORT_KIND);
                            if (logger.isDebugEnabled()) {
                                logger.debug("File " + fileNodeRef + " has ASPECT_REPORT_KIND: " + hasAspect);
                            }
                            if (hasAspect) {
                                List<String> rKinds = (List<String>) nodeService.getProperty(fileNodeRef, ReportModel.PROP_REPORT_KINDS);
                                boolean kindMatches = rKinds != null && rKinds.contains(reportKind);
                                if (logger.isDebugEnabled()) {
                                    logger.debug("File " + fileNodeRef + " reportKinds property: " + rKinds + ", matches '" + reportKind + "': " + kindMatches);
                                }
                                if (kindMatches) {
                                    try (InputStream in = reader.getContentInputStream()) {
                                        byte[] bytes = in.readAllBytes();
                                        results.add(new AnnexDocument(compName, bytes));
                                        collectedNodeRefs.add(fileNodeRef);
                                        if (logger.isDebugEnabled()) {
                                            logger.debug("Collected recursive file " + fileNodeRef + " (" + file.getName() + ", size: " + bytes.length + " bytes)");
                                        }
                                    } catch (Exception e) {
                                        logger.error("Error reading content stream of file node " + fileNodeRef + ": " + e.getMessage(), e);
                                    }
                                }
                            }
                        }
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("ContentReader missing or does not exist for file node: " + fileNodeRef);
                        }
                    }
                }
            }
        }
    }
}