package fr.becpg.repo.product.report;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Component;

import fr.becpg.model.ReportModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.helper.MLTextHelper;
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
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.annotation.PreDestroy;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;

@Component("aggregateReportModelBuilder")
public class AggregateReportModelBuilder {

    private static final Log logger = LogFactory.getLog(AggregateReportModelBuilder.class);

    private static final int SUB_REPORT_MAX_THREADS = 2;
    private static final int SUB_REPORT_QUEUE_CAPACITY = 10;

    private final ExecutorService subReportExecutor = new ThreadPoolExecutor(
            SUB_REPORT_MAX_THREADS,
            SUB_REPORT_MAX_THREADS,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(SUB_REPORT_QUEUE_CAPACITY),
            new ThreadFactory() {
                private final AtomicInteger threadNumber = new AtomicInteger(1);

                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "aggregate-subreport-" + threadNumber.getAndIncrement());
                    thread.setDaemon(true);
                    return thread;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    private final ThreadLocal<Boolean> collectingSubReport = ThreadLocal.withInitial(() -> Boolean.FALSE);

    @PreDestroy
    public void destroy() {
        subReportExecutor.shutdown();
        try {
            if (!subReportExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                subReportExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            subReportExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

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

    @Autowired
    private EntityDictionaryService entityDictionaryService;

    @Autowired
    private TransactionService transactionService;

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
        List<AnnexDocument> docs = collectDocumentsForNode(fpNodeRef, annex.getReportKind(), annex.getMimeTypes(), true);
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
        List<AnnexDocument> docs = collectDocumentsForNodesParallel(compoComponents, annex.getReportKind(), annex.getMimeTypes());
        documents.addAll(docs);
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
        List<AnnexDocument> docs = collectDocumentsForNodesParallel(packagingComponents, annex.getReportKind(), annex.getMimeTypes());
        documents.addAll(docs);
        if (logger.isDebugEnabled()) {
            logger.debug("Total PACKAGING_CHILDREN documents collected: " + documents.size() + " for reportKind: " + annex.getReportKind());
        }
    }

    private List<AnnexDocument> collectDocumentsForNodesParallel(List<NodeRef> entityNodeRefs, String reportKind, List<String> mimeTypes) {
        if (entityNodeRefs == null || entityNodeRefs.isEmpty()) {
            return Collections.emptyList();
        }

        if (entityNodeRefs.size() == 1 || Boolean.TRUE.equals(collectingSubReport.get())) {
            return collectDocumentsSequentially(entityNodeRefs, reportKind, mimeTypes);
        }

        String runAsUser = AuthenticationUtil.getRunAsUser();
        if (runAsUser == null) {
            runAsUser = AuthenticationUtil.getSystemUserName();
        }
        String tenantDomain = TenantUtil.getCurrentDomain();
        Locale locale = I18NUtil.getLocale();
        Locale contentLocale = I18NUtil.getContentLocale();

        final String finalRunAsUser = runAsUser;
        List<Future<List<AnnexDocument>>> futures = new ArrayList<>(entityNodeRefs.size());

        for (NodeRef compNode : entityNodeRefs) {
            futures.add(subReportExecutor.submit(() -> {
                return TenantUtil.runAsTenant(() -> {
                    return AuthenticationUtil.runAs(() -> {
                        I18NUtil.setLocale(locale);
                        I18NUtil.setContentLocale(contentLocale);
                        return collectSubReportDocuments(compNode, reportKind, mimeTypes);
                    }, finalRunAsUser);
                }, tenantDomain);
            }));
        }

        List<AnnexDocument> allDocuments = new ArrayList<>();
        for (int i = 0; i < futures.size(); i++) {
            NodeRef compNode = entityNodeRefs.get(i);
            try {
                List<AnnexDocument> docs = futures.get(i).get();
                if (docs != null) {
                    allDocuments.addAll(docs);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Collected " + (docs != null ? docs.size() : 0) + " documents for component: " + compNode + " (reportKind: " + reportKind + ")");
                }
            } catch (Exception e) {
                logger.error("Failed to generate/collect sub-report for component " + compNode + ": " + e.getMessage(), e);
            }
        }
        return allDocuments;
    }

    /**
     * Collects the components one after the other on the calling thread.
     *
     * Used for a single component, and for every component once we are already running a
     * sub-report: the pool has two threads and each of them waits on the futures it submitted,
     * so a nested aggregate report would queue its own components behind the threads that must
     * run them. CallerRunsPolicy only rescues a full queue, which ten nested components never
     * fill.
     */
    private List<AnnexDocument> collectDocumentsSequentially(List<NodeRef> entityNodeRefs, String reportKind, List<String> mimeTypes) {
        List<AnnexDocument> documents = new ArrayList<>();

        for (NodeRef entityNodeRef : entityNodeRefs) {
            documents.addAll(collectSubReportDocuments(entityNodeRef, reportKind, mimeTypes));
        }

        return documents;
    }

    private List<AnnexDocument> collectSubReportDocuments(NodeRef entityNodeRef, String reportKind, List<String> mimeTypes) {
        Boolean wasCollecting = collectingSubReport.get();
        collectingSubReport.set(Boolean.TRUE);
        try {
            return collectDocumentsForNode(entityNodeRef, reportKind, mimeTypes, false);
        } catch (Exception e) {
            logger.error("Failed to generate/collect sub-report for component " + entityNodeRef + ": " + e.getMessage(), e);
            return Collections.emptyList();
        } finally {
            collectingSubReport.set(wasCollecting);
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
                        boolean isAllowed = isTypeAllowed(type, allowedTypes);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Inspecting composition child component: " + compNodeRef + ", type: " + type + ", isAllowed: " + isAllowed);
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

    private boolean isTypeAllowed(QName type, List<String> allowedTypes) {
        if (allowedTypes == null || allowedTypes.isEmpty()) {
            return true;
        }
        for (String allowed : allowedTypes) {
            try {
                QName allowedQName = QName.createQName(allowed, namespaceService);
                if (allowedQName != null && (type.equals(allowedQName) || entityDictionaryService.isSubClass(type, allowedQName))) {
                    return true;
                }
            } catch (Exception e) {
                // Ignore namespace parsing error and fallback to string matching
            }
            if (allowed.equals(type.toPrefixString(namespaceService)) || allowed.equals(type.getLocalName())) {
                return true;
            }
        }
        return false;
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

    private List<AnnexDocument> collectDocumentsForNode(NodeRef entityNodeRef, String reportKind, List<String> mimeTypes, boolean isRootEntityDocument) {
        List<AnnexDocument> results = new ArrayList<>();
        Set<NodeRef> collectedNodeRefs = new HashSet<>();

        if (logger.isDebugEnabled()) {
            logger.debug("collectDocumentsForNode - entityNodeRef: " + entityNodeRef + ", reportKind: " + reportKind + ", mimeTypes: " + mimeTypes);
        }

        final Locale reportLocale = I18NUtil.getLocale();

        try {
        	if (isRootEntityDocument) {
        		if (logger.isDebugEnabled()) {
        			logger.debug("Triggering getOrRefreshReportsOfKind in same transaction for node " + entityNodeRef + ", reportKind: " + reportKind);
        		}
    			entityReportService.getOrRefreshReportsOfKind(entityNodeRef, reportKind, reportLocale);
        	} else {
        		if (logger.isDebugEnabled()) {
        			logger.debug("Triggering getOrRefreshReportsOfKind in isolated transaction for node " + entityNodeRef + ", reportKind: " + reportKind);
        		}
        		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
        			entityReportService.getOrRefreshReportsOfKind(entityNodeRef, reportKind, reportLocale);
        			return null;
        		}, false, true);
        	}
        } catch (Exception e) {
            logger.error("On-the-fly report refresh failed for node " + entityNodeRef + ": " + e.getMessage(), e);
        }

        Object titleProp = nodeService.getProperty(entityNodeRef, ContentModel.PROP_TITLE);
        String compName = null;
        if (titleProp instanceof MLText mlText) {
            compName = MLTextHelper.getClosestValue(mlText, I18NUtil.getLocale());
        } else if (titleProp != null) {
            compName = titleProp.toString();
        }
        if (compName == null || compName.isEmpty()) {
            compName = (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);
        }

        collectReportsOfKind(entityNodeRef, reportKind, reportLocale, compName, results, collectedNodeRefs);
        if (logger.isDebugEnabled()) {
            logger.debug("After collectReportsOfKind: " + results.size() + " documents for node " + entityNodeRef);
        }

        collectFilesRecursively(entityNodeRef, reportKind, mimeTypes, compName, results, 0, collectedNodeRefs);
        if (logger.isDebugEnabled()) {
            logger.debug("After collectFilesRecursively: " + results.size() + " total documents for node " + entityNodeRef);
        }

        return results;
    }

    private void collectReportsOfKind(NodeRef entityNodeRef, String reportKind, Locale reportLocale, String compName, List<AnnexDocument> results,
            Set<NodeRef> collectedNodeRefs) {
        try {
            List<NodeRef> reportsOfKind = filterReportsForLocale(entityReportService.getReportsOfKind(entityNodeRef, reportKind), reportLocale);
            if (logger.isDebugEnabled()) {
                logger.debug("entityReportService.getReportsOfKind(" + entityNodeRef + ", '" + reportKind + "') returned for locale " + reportLocale
                        + ": " + reportsOfKind);
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

    /**
     * Keeps the reports written in the language the aggregate is being built in.
     *
     * A component that declares several report locales holds one report per language, and
     * embedding them all would repeat every annex in each language of the entity.
     *
     * The unfiltered list is returned when no report carries the requested language, so a
     * component whose report predates the locale property still contributes its annex.
     */
    private List<NodeRef> filterReportsForLocale(List<NodeRef> reportNodeRefs, Locale reportLocale) {
        if (reportNodeRefs == null || reportNodeRefs.isEmpty() || reportLocale == null) {
            return reportNodeRefs;
        }

        List<NodeRef> matchingReports = new ArrayList<>();

        for (NodeRef reportNodeRef : reportNodeRefs) {
            Locale nearestLocale = MLTextHelper.getNearestLocale(reportLocale, getReportLocales(reportNodeRef));
            if (nearestLocale != null && nearestLocale.getLanguage().equals(reportLocale.getLanguage())) {
                matchingReports.add(reportNodeRef);
            }
        }

        return matchingReports.isEmpty() ? reportNodeRefs : matchingReports;
    }

    private Set<Locale> getReportLocales(NodeRef reportNodeRef) {
        Serializable localesProp = nodeService.getProperty(reportNodeRef, ReportModel.PROP_REPORT_LOCALES);

        if (localesProp instanceof List<?> list) {
            List<String> langs = new ArrayList<>(list.size());
            for (Object lang : list) {
                if (lang != null) {
                    langs.add(lang.toString());
                }
            }
            return MLTextHelper.extractLocales(langs);
        }

        if (localesProp instanceof String lang && !lang.isEmpty()) {
            return MLTextHelper.extractLocales(List.of(lang));
        }

        return Collections.emptySet();
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
                        boolean mimeAllowed = mimeTypes == null || mimeTypes.isEmpty() || mimeTypes.contains(mt);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Inspecting file " + fileNodeRef + " (" + file.getName() + "), mimetype: " + mt + ", mimeAllowed: " + mimeAllowed);
                        }
                        if (mimeAllowed) {
                            boolean hasAspect = nodeService.hasAspect(fileNodeRef, ReportModel.ASPECT_REPORT_KIND);
                            if (logger.isDebugEnabled()) {
                                logger.debug("File " + fileNodeRef + " has ASPECT_REPORT_KIND: " + hasAspect);
                            }
                            if (hasAspect) {
                                Serializable rKindsProp = nodeService.getProperty(fileNodeRef, ReportModel.PROP_REPORT_KINDS);
                                List<String> rKinds = null;
                                if (rKindsProp instanceof List<?> list) {
                                    rKinds = (List<String>) list;
                                } else if (rKindsProp instanceof String str && !str.isEmpty()) {
                                    rKinds = Collections.singletonList(str);
                                }
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