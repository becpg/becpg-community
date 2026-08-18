package fr.becpg.repo.product.report;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.springframework.extensions.surf.util.I18NUtil;

import org.alfresco.model.ContentModel;
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
import org.springframework.stereotype.Service;

import fr.becpg.model.ReportModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.json.JsonHelper;
import fr.becpg.repo.report.engine.BeCPGReportEngine;
import fr.becpg.repo.report.entity.EntityReportData;
import fr.becpg.repo.report.pdf.ReportPdfAggregator;
import fr.becpg.repo.report.pdf.ReportPdfAggregator.AggregateReportConfig;
import fr.becpg.repo.report.pdf.ReportPdfAggregator.AnnexSection;
import fr.becpg.report.client.ReportException;
import fr.becpg.report.client.ReportFormat;

@Service("aggregateReportEngine")
public class AggregateReportEngine implements BeCPGReportEngine {

    private static final Log logger = LogFactory.getLog(AggregateReportEngine.class);

    @Autowired
    private NodeService nodeService;

    @Autowired
    private ContentService contentService;

    @Autowired
    private AssociationService associationService;

    @Autowired
    private NamespaceService namespaceService;

    @Autowired
    @Qualifier("reportServerEngine")
    private BeCPGReportEngine reportServerEngine;

    @Autowired
    private AggregateReportModelBuilder aggregateReportModelBuilder;

    @Override
    public boolean isApplicable(NodeRef templateNodeRef, ReportFormat reportFormat) {
        if (reportFormat != ReportFormat.PDF) {
            return false;
        }
        Boolean isAggregate = (Boolean) nodeService.getProperty(templateNodeRef, ReportModel.PROP_REPORT_TPL_IS_AGGREGATE);
        String name = (String) nodeService.getProperty(templateNodeRef, ContentModel.PROP_NAME);
        boolean applicable = (isAggregate != null && isAggregate) || (name != null && name.endsWith(".agg.rptdesign"));
        if (logger.isDebugEnabled()) {
            logger.debug("isApplicable check for template " + templateNodeRef + " (name: " + name + ", isAggregate: " + isAggregate + ") -> " + applicable);
        }
        return applicable;
    }

    @Override
    public void createReport(NodeRef tplNodeRef, EntityReportData reportData, OutputStream out, Map<String, Object> params) throws ReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("Starting aggregate report generation for template: " + tplNodeRef + ", reportData: " + reportData);
        }
        try {
            byte[] bodyPdfBytes = generateCoreBirtBody(tplNodeRef, reportData, params);
            if (logger.isDebugEnabled()) {
                logger.debug("Generated core BIRT body PDF, length: " + (bodyPdfBytes != null ? bodyPdfBytes.length : 0) + " bytes");
            }
            List<NodeRef> assocFiles = associationService.getTargetAssocs(tplNodeRef, ReportModel.ASSOC_REPORT_ASSOCIATED_TPL_FILES);
            if (logger.isDebugEnabled()) {
                logger.debug("Retrieved " + (assocFiles != null ? assocFiles.size() : 0) + " associated template files for " + tplNodeRef);
            }
            AggregateReportConfig config = loadDescriptorConfig(tplNodeRef, assocFiles);
            if (logger.isDebugEnabled()) {
                logger.debug("Loaded descriptor config. Number of configured annexes: " + (config.getAnnexes() != null ? config.getAnnexes().size() : 0));
            }
            Map<String, String> customI18n = loadAssociatedI18nProperties(assocFiles, params);
            if (logger.isDebugEnabled()) {
                logger.debug("Loaded " + customI18n.size() + " custom i18n properties");
            }

            NodeRef entityNodeRef = getEntityNodeRef(params);
            if (logger.isDebugEnabled()) {
                logger.debug("Entity nodeRef for aggregate report: " + entityNodeRef);
            }
            List<AnnexSection> sections = aggregateReportModelBuilder.buildAnnexSections(entityNodeRef, config, customI18n);
            if (logger.isDebugEnabled()) {
                logger.debug("Built " + sections.size() + " annex sections");
                for (AnnexSection sec : sections) {
                    logger.debug("  Section: '" + sec.getTitle() + "' (kind: " + sec.getReportKind() + "), documents count: "
                            + (sec.getDocuments() != null ? sec.getDocuments().size() : 0)
                            + ", emptyPlaceholder: " + sec.getEmptyPlaceholder());
                }
            }
            byte[] logoBytes = loadLogoBytes(config, assocFiles);
            if (logger.isDebugEnabled()) {
                logger.debug("Logo loaded: " + (logoBytes != null ? logoBytes.length + " bytes" : "none"));
            }
            Map<String, String> propertiesMap = gatherEntityProperties(entityNodeRef);
            if (logger.isDebugEnabled()) {
                logger.debug("Gathered " + propertiesMap.size() + " entity properties");
            }

            byte[] finalPdfBytes = assembleFinalPdf(bodyPdfBytes, sections, config, logoBytes, propertiesMap, customI18n);
            if (logger.isDebugEnabled()) {
                logger.debug("Final aggregated PDF assembled successfully, size: " + (finalPdfBytes != null ? finalPdfBytes.length : 0) + " bytes");
            }
            streamOutput(finalPdfBytes, out);
        } catch (Exception e) {
            logger.error("Exception caught during Aggregate Report generation: " + e.getMessage(), e);
            throw new ReportException("Failed to generate aggregated report: " + e.getMessage(), e);
        }
    }

    private NodeRef getEntityNodeRef(Map<String, Object> params) {
        NodeRef entityNodeRef = (NodeRef) params.get(PARAM_ENTITY_NODEREF);
        if (entityNodeRef == null) {
            throw new IllegalArgumentException("PARAM_ENTITY_NODEREF is missing from report parameters");
        }
        return entityNodeRef;
    }

    private byte[] assembleFinalPdf(byte[] bodyPdfBytes, List<AnnexSection> sections, AggregateReportConfig config, byte[] logoBytes, Map<String, String> propertiesMap, Map<String, String> customI18n) throws Exception {
        return ReportPdfAggregator.assemble(
                bodyPdfBytes,
                sections,
                config.getHeader(),
                config.getComponentHeadingStyle(),
                logoBytes,
                propertiesMap,
                config.getTableOfContents(),
                config.getPagination(),
                customI18n,
                config.getPlaceholderStyle()
        );
    }

    private Map<String, String> loadAssociatedI18nProperties(List<NodeRef> assocFiles, Map<String, Object> params) {
        Map<String, String> customI18n = new HashMap<>();
        if (assocFiles == null || assocFiles.isEmpty()) {
            return customI18n;
        }

        String lang = null;
        if (params != null) {
            Object langObj = params.get("lang");
            if (langObj instanceof String s && !s.isBlank()) {
                lang = s;
            }
        }
        if (lang == null || lang.isBlank()) {
            Locale currentLocale = I18NUtil.getLocale();
            lang = currentLocale != null ? currentLocale.getLanguage() : "en";
        }
        if (lang.contains("_")) {
            lang = lang.substring(0, lang.indexOf('_'));
        }

        List<NodeRef> basePropNodes = new ArrayList<>();
        List<NodeRef> langPropNodes = new ArrayList<>();

        for (NodeRef assoc : assocFiles) {
            String name = (String) nodeService.getProperty(assoc, ContentModel.PROP_NAME);
            if (name != null && name.endsWith(".properties")) {
                if (name.toLowerCase().contains("_" + lang.toLowerCase() + ".properties")) {
                    langPropNodes.add(assoc);
                } else if (!name.matches(".*_[a-z]{2}(_[A-Z]{2})?\\.properties")) {
                    basePropNodes.add(assoc);
                }
            }
        }

        for (NodeRef node : basePropNodes) {
            readPropertiesNode(node, customI18n);
        }
        for (NodeRef node : langPropNodes) {
            readPropertiesNode(node, customI18n);
        }

        return customI18n;
    }

    private void readPropertiesNode(NodeRef propNodeRef, Map<String, String> targetMap) {
        ContentReader reader = contentService.getReader(propNodeRef, ContentModel.PROP_CONTENT);
        if (reader != null && reader.exists()) {
            try (InputStream in = reader.getContentInputStream()) {
                Properties props = new Properties();
                props.load(in);
                for (String key : props.stringPropertyNames()) {
                    targetMap.put(key, props.getProperty(key));
                }
            } catch (Exception e) {
                logger.warn("Could not read associated properties file " + propNodeRef + ": " + e.getMessage());
            }
        }
    }

    private byte[] generateCoreBirtBody(NodeRef tplNodeRef, EntityReportData reportData, Map<String, Object> params) throws ReportException {
        ByteArrayOutputStream birtBos = new ByteArrayOutputStream();
        reportServerEngine.createReport(tplNodeRef, reportData, birtBos, params);
        return birtBos.toByteArray();
    }

    private AggregateReportConfig loadDescriptorConfig(NodeRef tplNodeRef, List<NodeRef> assocFiles) throws Exception {
        NodeRef jsonNodeRef = extractJsonAggregatorFile(assocFiles);
        if (jsonNodeRef == null) {
            throw new ReportException("No aggregate descriptor (.json) associated with template " + tplNodeRef);
        }

        ContentReader jsonReader = contentService.getReader(jsonNodeRef, ContentModel.PROP_CONTENT);
        if (jsonReader == null || !jsonReader.exists()) {
            throw new ReportException("Associated aggregate descriptor file content is empty or unreadable: " + jsonNodeRef);
        }

        return JsonHelper.MAPPER.readValue(jsonReader.getContentString(), AggregateReportConfig.class);
    }

    private byte[] loadLogoBytes(AggregateReportConfig config, List<NodeRef> assocFiles) throws Exception {
        String logoName = config.getHeader() != null ? config.getHeader().getLogo() : null;
        if (logoName == null || assocFiles == null) {
            return null;
        }

        for (NodeRef assoc : assocFiles) {
            String name = (String) nodeService.getProperty(assoc, ContentModel.PROP_NAME);
            if (logoName.equalsIgnoreCase(name) || (name != null && name.toLowerCase().startsWith("logo"))) {
                ContentReader r = contentService.getReader(assoc, ContentModel.PROP_CONTENT);
                if (r != null && r.exists()) {
                    try (InputStream is = r.getContentInputStream()) {
                        return is.readAllBytes();
                    }
                }
            }
        }
        return null;
    }

    private Map<String, String> gatherEntityProperties(NodeRef entityNodeRef) {
        Map<String, String> propertiesMap = new HashMap<>();
        Map<QName, Serializable> nodeProps = nodeService.getProperties(entityNodeRef);
        for (Map.Entry<QName, Serializable> entry : nodeProps.entrySet()) {
            String localName = entry.getKey().getLocalName();
            String prefix = entry.getKey().toPrefixString(namespaceService);
            Serializable val = entry.getValue();
            if (val != null) {
                propertiesMap.put(localName, val.toString());
                propertiesMap.put(prefix, val.toString());
            }
        }

        Date modified = (Date) nodeService.getProperty(entityNodeRef, ContentModel.PROP_MODIFIED);
        if (modified != null) {
            String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(modified);
            propertiesMap.put("modified", dateStr);
            propertiesMap.put("cm:modified?date", dateStr);
        }
        return propertiesMap;
    }

    private void streamOutput(byte[] finalPdfBytes, OutputStream out) throws Exception {
        out.write(finalPdfBytes);
        out.flush();
        try {
            out.close();
        } catch (Exception ex) {
            logger.warn("Error closing output stream: " + ex.getMessage());
        }
    }

    private NodeRef extractJsonAggregatorFile(List<NodeRef> assocFiles) {
        if (assocFiles != null) {
            for (NodeRef assoc : assocFiles) {
                String name = (String) nodeService.getProperty(assoc, ContentModel.PROP_NAME);
                if (name != null && name.endsWith(".json")) {
                    return assoc;
                }
            }
        }
        return null;
    }

    @Override
    public boolean isXmlEngine() {
        return true;
    }
}