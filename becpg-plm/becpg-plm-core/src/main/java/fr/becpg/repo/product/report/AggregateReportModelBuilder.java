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
            return sections;
        }

        for (AnnexConfig annex : config.getAnnexes()) {
            List<AnnexDocument> documents = new ArrayList<>();
            String scope = annex.getScope();

            if ("ENTITY".equalsIgnoreCase(scope)) {
                collectEntityAnnex(fpNodeRef, annex, documents);
            } else if ("COMPO_CHILDREN".equalsIgnoreCase(scope)) {
                collectCompoAnnex(fpNodeRef, annex, documents);
            } else if ("PACKAGING_CHILDREN".equalsIgnoreCase(scope)) {
                collectPackagingAnnex(fpNodeRef, annex, documents);
            }

            String resolvedTitle = resolveI18nKey(annex.getTitle(), customI18n);
            String resolvedPlaceholder = resolveI18nKey(annex.getEmptyPlaceholder(), customI18n);

            if (documents.isEmpty()) {
                if (annex.isRequired()) {
                    sections.add(new AnnexSection(annex.getReportKind(), resolvedTitle, documents, resolvedPlaceholder));
                }
            } else {
                sections.add(new AnnexSection(annex.getReportKind(), resolvedTitle, documents, resolvedPlaceholder));
            }
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
        List<AnnexDocument> docs = collectDocumentsForNode(fpNodeRef, annex.getReportKind(), annex.getMimeTypes(), false);
        documents.addAll(docs);
    }

    private void collectCompoAnnex(NodeRef fpNodeRef, AnnexConfig annex, List<AnnexDocument> documents) {
        List<NodeRef> compoComponents = new ArrayList<>();
        collectCompoComponents(fpNodeRef, compoComponents, new HashSet<>(), annex.isRecurse(), annex.getComponentTypes());
        if (annex.isDedup()) {
            compoComponents = new ArrayList<>(new LinkedHashSet<>(compoComponents));
        }
        for (NodeRef compNode : compoComponents) {
            List<AnnexDocument> docs = collectDocumentsForNode(compNode, annex.getReportKind(), annex.getMimeTypes(), true);
            documents.addAll(docs);
        }
    }

    private void collectPackagingAnnex(NodeRef fpNodeRef, AnnexConfig annex, List<AnnexDocument> documents) {
        List<NodeRef> packagingComponents = new ArrayList<>();
        collectPackagingComponents(fpNodeRef, packagingComponents, annex.getPkgLevel());
        if (annex.isDedup()) {
            packagingComponents = new ArrayList<>(new LinkedHashSet<>(packagingComponents));
        }
        for (NodeRef pkgNode : packagingComponents) {
            List<AnnexDocument> docs = collectDocumentsForNode(pkgNode, annex.getReportKind(), annex.getMimeTypes(), true);
            documents.addAll(docs);
        }
    }

    private void collectCompoComponents(NodeRef productNodeRef, List<NodeRef> collected, Set<NodeRef> visited, boolean recurse, List<String> allowedTypes) {
        if (productNodeRef == null || !visited.add(productNodeRef)) {
            return;
        }
        try {
            ProductData productData = (ProductData) alfrescoRepository.findOne(productNodeRef);
            if (productData == null) {
                return;
            }
            List<CompoListDataItem> compoList = productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE));
            if (compoList != null) {
                for (CompoListDataItem item : compoList) {
                    NodeRef compNodeRef = item.getProduct();
                    if (compNodeRef != null) {
                        QName type = nodeService.getType(compNodeRef);
                        String prefixType = type.toPrefixString(namespaceService);
                        if (allowedTypes == null || allowedTypes.isEmpty() || allowedTypes.contains(prefixType)) {
                            if (!collected.contains(compNodeRef)) {
                                collected.add(compNodeRef);
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
                return;
            }
            List<PackagingListDataItem> pkgList = productData.getPackagingList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE));
            if (pkgList != null) {
                for (PackagingListDataItem item : pkgList) {
                    NodeRef pkgNodeRef = item.getProduct();
                    if (pkgNodeRef != null) {
                        if (targetPkgLevel != null && !targetPkgLevel.isEmpty()) {
                            PackagingLevel level = item.getPkgLevel();
                            if (level == null || !level.name().equalsIgnoreCase(targetPkgLevel)) {
                                continue;
                            }
                        }
                        if (!collected.contains(pkgNodeRef)) {
                            collected.add(pkgNodeRef);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Exception while traversing packaging for product: " + e.getMessage(), e);
        }
    }

    private List<AnnexDocument> collectDocumentsForNode(NodeRef entityNodeRef, String reportKind, List<String> mimeTypes, boolean shouldRefresh) {
        List<AnnexDocument> results = new ArrayList<>();
        Set<NodeRef> collectedNodeRefs = new HashSet<>();

        if (shouldRefresh) {
            try {
                entityReportService.getOrRefreshReportsOfKind(entityNodeRef, reportKind);
            } catch (Exception e) {
                logger.error("On-the-fly report refresh failed for node " + entityNodeRef + ": " + e.getMessage(), e);
            }
        }

        String compName = (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_TITLE);
        if (compName == null || compName.isEmpty()) {
            compName = (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);
        }

        collectReportsOfKind(entityNodeRef, reportKind, compName, results, collectedNodeRefs);
        collectFilesRecursively(entityNodeRef, reportKind, mimeTypes, compName, results, 0, collectedNodeRefs);
        return results;
    }

    private void collectReportsOfKind(NodeRef entityNodeRef, String reportKind, String compName, List<AnnexDocument> results, Set<NodeRef> collectedNodeRefs) {
        try {
            List<NodeRef> reportsOfKind = entityReportService.getReportsOfKind(entityNodeRef, reportKind);
            if (reportsOfKind != null) {
                for (NodeRef reportNodeRef : reportsOfKind) {
                    if (collectedNodeRefs.add(reportNodeRef)) {
                        ContentReader reader = contentService.getReader(reportNodeRef, ContentModel.PROP_CONTENT);
                        if (reader != null && reader.exists()) {
                            try (InputStream in = reader.getContentInputStream()) {
                                byte[] bytes = in.readAllBytes();
                                if (bytes != null && bytes.length > 0) {
                                    results.add(new AnnexDocument(compName, bytes));
                                }
                            }
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
            return;
        }
        List<FileInfo> fileInfos = fileFolderService.listFiles(folderNodeRef);
        if (fileInfos != null) {
            for (FileInfo file : fileInfos) {
                if (file.isFolder()) {
                    collectFilesRecursively(file.getNodeRef(), reportKind, mimeTypes, compName, results, depth + 1, collectedNodeRefs);
                } else {
                    NodeRef fileNodeRef = file.getNodeRef();
                    if (collectedNodeRefs.contains(fileNodeRef)) {
                        continue;
                    }
                    ContentReader reader = contentService.getReader(fileNodeRef, ContentModel.PROP_CONTENT);
                    if (reader != null && reader.exists()) {
                        String mt = reader.getMimetype();
                        if (mimeTypes == null || mimeTypes.contains(mt)) {
                            boolean hasAspect = nodeService.hasAspect(fileNodeRef, ReportModel.ASPECT_REPORT_KIND);
                            if (hasAspect) {
                                List<String> rKinds = (List<String>) nodeService.getProperty(fileNodeRef, ReportModel.PROP_REPORT_KINDS);
                                if (rKinds != null && rKinds.contains(reportKind)) {
                                    try (InputStream in = reader.getContentInputStream()) {
                                        byte[] bytes = in.readAllBytes();
                                        results.add(new AnnexDocument(compName, bytes));
                                        collectedNodeRefs.add(fileNodeRef);
                                    } catch (Exception e) {
                                        logger.error("Error reading content stream of file node " + fileNodeRef + ": " + e.getMessage(), e);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}