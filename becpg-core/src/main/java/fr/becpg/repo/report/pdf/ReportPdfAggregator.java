package fr.becpg.repo.report.pdf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.becpg.repo.helper.MessageHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.*;
import java.util.*;

public class ReportPdfAggregator {

    private static final Log logger = LogFactory.getLog(ReportPdfAggregator.class);

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HeaderModel implements Serializable {
        private static final long serialVersionUID = 1L;
        private String logo;
        private String title;
        private String subtitle;
        private String date;
        private Integer startNumberingAt = 1;

        public String getLogo() { return logo; }
        public void setLogo(String logo) { this.logo = logo; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getSubtitle() { return subtitle; }
        public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public Integer getStartNumberingAt() { return startNumberingAt; }
        public void setStartNumberingAt(Integer startNumberingAt) { this.startNumberingAt = startNumberingAt; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ComponentHeadingStyle implements Serializable {
        private static final long serialVersionUID = 1L;
        private String font;
        private Integer size = 11;
        private String color = "#1F3864";
        private String prefix = "➣ ";

        public String getFont() { return font; }
        public void setFont(String font) { this.font = font; }
        public Integer getSize() { return size; }
        public void setSize(Integer size) { this.size = size; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        public String getPrefix() { return prefix; }
        public void setPrefix(String prefix) { this.prefix = prefix; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AnnexConfig implements Serializable {
        private static final long serialVersionUID = 1L;
        private String reportKind;
        private String title;
        private String scope;
        private boolean recurse;
        private List<String> componentTypes;
        private boolean componentHeading = true;
        private String sort;
        private boolean dedup = true;
        private List<String> mimeTypes = Collections.singletonList("application/pdf");
        private String emptyPlaceholder;
        private boolean required = false;
        private String pkgLevel;

        public String getReportKind() { return reportKind; }
        public void setReportKind(String reportKind) { this.reportKind = reportKind; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getScope() { return scope; }
        public void setScope(String scope) { this.scope = scope; }
        public boolean isRecurse() { return recurse; }
        public void setRecurse(boolean recurse) { this.recurse = recurse; }
        public List<String> getComponentTypes() { return componentTypes; }
        public void setComponentTypes(List<String> componentTypes) { this.componentTypes = componentTypes; }
        public boolean isComponentHeading() { return componentHeading; }
        public void setComponentHeading(boolean componentHeading) { this.componentHeading = componentHeading; }
        public String getSort() { return sort; }
        public void setSort(String sort) { this.sort = sort; }
        public boolean isDedup() { return dedup; }
        public void setDedup(boolean dedup) { this.dedup = dedup; }
        public List<String> getMimeTypes() { return mimeTypes; }
        public void setMimeTypes(List<String> mimeTypes) { this.mimeTypes = mimeTypes; }
        public String getEmptyPlaceholder() { return emptyPlaceholder; }
        public void setEmptyPlaceholder(String emptyPlaceholder) { this.emptyPlaceholder = emptyPlaceholder; }
        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }
        public String getPkgLevel() { return pkgLevel; }
        public void setPkgLevel(String pkgLevel) { this.pkgLevel = pkgLevel; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TableOfContentsModel implements Serializable {
        private static final long serialVersionUID = 1L;
        private boolean enabled = false;
        private String title = "TABLE OF CONTENTS";
        private String font = "Helvetica-Bold";
        private Integer size = 16;
        private String color = "#1F3864";
        private Integer position = 0;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getFont() { return font; }
        public void setFont(String font) { this.font = font; }
        public Integer getSize() { return size; }
        public void setSize(Integer size) { this.size = size; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        public Integer getPosition() { return position != null ? position : 0; }
        public void setPosition(Integer position) { this.position = position; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaginationModel implements Serializable {
        private static final long serialVersionUID = 1L;
        private boolean enabled = false;
        private String format = "Page ${page} / ${total}";
        private Integer startNumberingAt = 1;
        private String font = "Helvetica";
        private Integer size = 8;
        private String color = "#404040";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getFormat() { return format; }
        public void setFormat(String format) { this.format = format; }
        public Integer getStartNumberingAt() { return startNumberingAt != null ? startNumberingAt : 1; }
        public void setStartNumberingAt(Integer startNumberingAt) { this.startNumberingAt = startNumberingAt; }
        public String getFont() { return font; }
        public void setFont(String font) { this.font = font; }
        public Integer getSize() { return size; }
        public void setSize(Integer size) { this.size = size; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AggregateReportConfig implements Serializable {
        private static final long serialVersionUID = 1L;
        private HeaderModel header;
        private TableOfContentsModel tableOfContents;
        private PaginationModel pagination;
        private ComponentHeadingStyle componentHeadingStyle;
        private List<AnnexConfig> annexes;

        public HeaderModel getHeader() { return header; }
        public void setHeader(HeaderModel header) { this.header = header; }
        public TableOfContentsModel getTableOfContents() { return tableOfContents; }
        public void setTableOfContents(TableOfContentsModel tableOfContents) { this.tableOfContents = tableOfContents; }
        public PaginationModel getPagination() { return pagination; }
        public void setPagination(PaginationModel pagination) { this.pagination = pagination; }
        public ComponentHeadingStyle getComponentHeadingStyle() { return componentHeadingStyle; }
        public void setComponentHeadingStyle(ComponentHeadingStyle componentHeadingStyle) { this.componentHeadingStyle = componentHeadingStyle; }
        public List<AnnexConfig> getAnnexes() { return annexes; }
        public void setAnnexes(List<AnnexConfig> annexes) { this.annexes = annexes; }
    }

    public static class AnnexDocument {
        private final String componentName;
        private final byte[] pdfBytes;
        private boolean isBeCPGDoc;

        public AnnexDocument(String componentName, byte[] pdfBytes) {
            this.componentName = componentName;
            this.pdfBytes = pdfBytes;
            this.isBeCPGDoc = hasExistingBeCPGLayout(pdfBytes);
        }

        public String getComponentName() { return componentName; }
        public byte[] getPdfBytes() { return pdfBytes; }
        public boolean isBeCPGDoc() { return isBeCPGDoc; }
    }

    public static class AnnexSection {
        private final String reportKind;
        private final String title;
        private final List<AnnexDocument> documents;
        private final String emptyPlaceholder;

        public AnnexSection(String reportKind, String title, List<AnnexDocument> documents) {
            this(reportKind, title, documents, null);
        }

        public AnnexSection(String reportKind, String title, List<AnnexDocument> documents, String emptyPlaceholder) {
            this.reportKind = reportKind;
            this.title = title;
            this.documents = documents;
            this.emptyPlaceholder = emptyPlaceholder;
        }

        public String getReportKind() { return reportKind; }
        public String getTitle() { return title; }
        public List<AnnexDocument> getDocuments() { return documents; }
        public String getEmptyPlaceholder() { return emptyPlaceholder; }
    }

    private static class InsertionPoint {
        private AnnexSection section;
        private int bodyPage;
    }

    private static class MergePart {
        private boolean isBody;
        private int bodyStart;
        private int bodyEnd;
        private AnnexSection section;
        private byte[] generatedPlaceholder;
    }

    public static class TokenLocator extends PDFTextStripper {
        public static class FoundToken {
            public String token;
            public int pageIndex;
            public float x;
            public float y;
            public float width;
            public float height;
        }

        private final List<FoundToken> foundTokens = new ArrayList<>();
        private int currentPageIndex = 0;

        public TokenLocator() throws IOException {
            super();
        }

        public List<FoundToken> findTokens(PDDocument document) throws IOException {
            int numPages = document.getNumberOfPages();
            logger.info("[TokenLocator] Scanning " + numPages + " pages for ToC placeholders {{page:key}}...");
            for (int i = 0; i < numPages; i++) {
                currentPageIndex = i;
                setStartPage(i + 1);
                setEndPage(i + 1);
                Writer dummy = new StringWriter();
                writeText(document, dummy);
            }
            logger.info("[TokenLocator] Scan complete. Total placeholders found: " + foundTokens.size());
            return foundTokens;
        }

        @Override
        protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
            int start = 0;
            while ((start = string.indexOf("{{page:", start)) != -1) {
                int end = string.indexOf("}}", start);
                if (end != -1 && end > start) {
                    String token = string.substring(start, end + 2);
                    logger.info("[TokenLocator] Found token text: " + token + " on page index " + currentPageIndex);

                    if (start < textPositions.size() && (end + 1) < textPositions.size()) {
                        TextPosition firstChar = textPositions.get(start);
                        TextPosition lastChar = textPositions.get(end + 1);

                        FoundToken ft = new FoundToken();
                        ft.token = token;
                        ft.pageIndex = currentPageIndex;
                        ft.x = firstChar.getXDirAdj();
                        ft.y = firstChar.getYDirAdj();
                        ft.width = lastChar.getXDirAdj() + lastChar.getWidthDirAdj() - firstChar.getXDirAdj();
                        ft.height = firstChar.getHeightDir();

                        foundTokens.add(ft);
                    }
                    start = end + 2;
                } else {
                    break;
                }
            }
        }
    }

    public static class PageNumberLocator extends PDFTextStripper {
        public static class FoundPageNumber {
            public int pageIndex;
            public float x;
            public float y;
            public float height;
        }

        private final List<FoundPageNumber> pageNumbers = new ArrayList<>();
        private int currentPageIndex = 0;
        private float currentPageHeight = 842.0f;
        private int currentPageCount = 1;

        public PageNumberLocator() throws IOException {
        }

        public List<FoundPageNumber> locatePageNumbers(PDDocument document) throws IOException {
            this.currentPageCount = document.getNumberOfPages();
            for (int i = 0; i < currentPageCount; i++) {
                currentPageIndex = i;
                setStartPage(i + 1);
                setEndPage(i + 1);
                PDPage page = document.getPage(i);
                currentPageHeight = page.getMediaBox().getHeight();
                Writer dummy = new StringWriter();
                writeText(document, dummy);
            }
            return pageNumbers;
        }

        @Override
        protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
            if (string != null && string.contains("Page")) {
                int startIdx = string.indexOf("Page");
                TextPosition pageChar = textPositions.get(startIdx);
                FoundPageNumber fpn = new FoundPageNumber();
                fpn.pageIndex = currentPageIndex;
                fpn.x = pageChar.getXDirAdj();
                fpn.y = currentPageHeight - pageChar.getYDirAdj();
                fpn.height = pageChar.getHeightDir();
                pageNumbers.add(fpn);
            }
        }
    }

    public static byte[] assemble(byte[] bodyPdf, List<AnnexSection> sections, HeaderModel header, ComponentHeadingStyle headingStyle, byte[] logoBytes, Map<String, String> properties) throws Exception {
        return assemble(bodyPdf, sections, header, headingStyle, logoBytes, properties, null);
    }

    public static byte[] assemble(byte[] bodyPdf, List<AnnexSection> sections, HeaderModel header, ComponentHeadingStyle headingStyle, byte[] logoBytes, Map<String, String> properties, TableOfContentsModel tocConfig) throws Exception {
        return assemble(bodyPdf, sections, header, headingStyle, logoBytes, properties, tocConfig, null);
    }

    public static byte[] assemble(byte[] bodyPdf, List<AnnexSection> sections, HeaderModel header, ComponentHeadingStyle headingStyle, byte[] logoBytes, Map<String, String> properties, TableOfContentsModel tocConfig, PaginationModel paginationConfig) throws Exception {
        return assemble(bodyPdf, sections, header, headingStyle, logoBytes, properties, tocConfig, paginationConfig, null);
    }

    public static byte[] assemble(byte[] bodyPdf, List<AnnexSection> sections, HeaderModel header, ComponentHeadingStyle headingStyle, byte[] logoBytes, Map<String, String> properties, TableOfContentsModel tocConfig, PaginationModel paginationConfig, Map<String, String> customI18n) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("[ReportPdfAggregator] Starting assemble operation. Body PDF length: " + (bodyPdf != null ? bodyPdf.length : 0) + " bytes, sections: " + (sections != null ? sections.size() : 0));
            if (sections != null) {
                for (AnnexSection sec : sections) {
                    logger.debug("  - AnnexSection title: '" + sec.getTitle() + "', kind: " + sec.getReportKind() + ", documents count: " + (sec.getDocuments() != null ? sec.getDocuments().size() : 0));
                    if (sec.getDocuments() != null) {
                        for (AnnexDocument ad : sec.getDocuments()) {
                            logger.debug("    * AnnexDocument component: '" + ad.getComponentName() + "', size: " + (ad.getPdfBytes() != null ? ad.getPdfBytes().length : 0) + " bytes, isBeCPGDoc: " + ad.isBeCPGDoc());
                        }
                    }
                }
            }
        }
        if (bodyPdf == null || bodyPdf.length == 0) {
            throw new IllegalArgumentException("Body PDF is empty");
        }

        try (PDDocument doc = Loader.loadPDF(bodyPdf)) {
            int numBodyPages = doc.getNumberOfPages();
            if (logger.isDebugEnabled()) {
                logger.debug("Core body PDF pages count: " + numBodyPages);
            }
            Map<String, Integer> outlineBookmarks = scanOutlineBookmarks(doc);
            if (logger.isDebugEnabled()) {
                logger.debug("Scanned outline bookmarks: " + outlineBookmarks);
            }
            List<TokenLocator.FoundToken> textTokens = new TokenLocator().findTokens(doc);

            List<InsertionPoint> insertions = resolveInsertionPoints(sections, numBodyPages, outlineBookmarks, textTokens);
            List<MergePart> baseParts = buildBaseParts(insertions, numBodyPages);
            List<MergePart> parts = buildFinalParts(baseParts, numBodyPages, tocConfig, sections);

            Map<AnnexSection, Integer> sectionToMergedPageMap = new HashMap<>();
            Map<AnnexDocument, Integer> docToMergedPageMap = new HashMap<>();
            calculateMergedPageMetrics(parts, sectionToMergedPageMap, docToMergedPageMap);

            generateRealTocIfEnabled(parts, sections, sectionToMergedPageMap, tocConfig, customI18n);

            Map<Integer, Integer> originalToMergedPageMap = new HashMap<>();
            byte[] mergedPdfBytes = executeSequentialMerge(parts, bodyPdf, docToMergedPageMap, originalToMergedPageMap);

            byte[] result = postProcessMergedPdf(mergedPdfBytes, bodyPdf, numBodyPages, sections, sectionToMergedPageMap, docToMergedPageMap, textTokens, outlineBookmarks, header, logoBytes, properties, headingStyle, tocConfig, paginationConfig, originalToMergedPageMap, customI18n);
            if (logger.isDebugEnabled()) {
                logger.debug("[ReportPdfAggregator] Assemble operation completed successfully. Final PDF size: " + result.length + " bytes");
            }
            return result;
        }
    }

    private static Map<String, Integer> scanOutlineBookmarks(PDDocument doc) {
        Map<String, Integer> outlineBookmarks = new HashMap<>();
        if (doc.getDocumentCatalog().getDocumentOutline() != null) {
            traverseOutline(doc.getDocumentCatalog().getDocumentOutline().getFirstChild(), outlineBookmarks, doc);
        }
        return outlineBookmarks;
    }

    private static List<InsertionPoint> resolveInsertionPoints(List<AnnexSection> sections, int numBodyPages, Map<String, Integer> outlineBookmarks, List<TokenLocator.FoundToken> textTokens) throws Exception {
        List<InsertionPoint> insertions = new ArrayList<>();
        for (AnnexSection section : sections) {
            String rk = section.getReportKind();
            InsertionPoint ins = new InsertionPoint();
            ins.section = section;

            int pageIdx = -1;
            if (outlineBookmarks.containsKey("becpg.annex." + rk)) {
                pageIdx = outlineBookmarks.get("becpg.annex." + rk);
            }

            if (pageIdx == -1) {
                pageIdx = numBodyPages - 1;
            }

            ins.bodyPage = pageIdx;
            insertions.add(ins);
            if (logger.isDebugEnabled()) {
                logger.debug("Resolved insertion point for section '" + section.getTitle() + "' (kind: " + rk + ") -> body page index: " + pageIdx);
            }
        }
        return insertions;
    }

    private static List<MergePart> buildBaseParts(List<InsertionPoint> insertions, int numBodyPages) throws Exception {
        Map<Integer, List<InsertionPoint>> insertionsByPage = new LinkedHashMap<>();
        for (InsertionPoint ins : insertions) {
            insertionsByPage.computeIfAbsent(ins.bodyPage, k -> new ArrayList<>()).add(ins);
        }

        List<MergePart> baseParts = new ArrayList<>();
        int lastBodyPage = 0;

        for (Map.Entry<Integer, List<InsertionPoint>> entry : insertionsByPage.entrySet()) {
            int bodyPage = entry.getKey();
            List<InsertionPoint> pageInsertions = entry.getValue();

            if (bodyPage >= lastBodyPage) {
                MergePart partBody = new MergePart();
                partBody.isBody = true;
                partBody.bodyStart = lastBodyPage;
                partBody.bodyEnd = bodyPage;
                baseParts.add(partBody);

                for (InsertionPoint ins : pageInsertions) {
                    MergePart partAnnex = new MergePart();
                    partAnnex.isBody = false;
                    partAnnex.section = ins.section;
                    if (ins.section.getDocuments() == null || ins.section.getDocuments().isEmpty()) {
                        String phText = ins.section.getEmptyPlaceholder();
                        if (phText == null || phText.trim().isEmpty()) {
                            phText = "To be annexed after the first manufacturing";
                        }
                        partAnnex.generatedPlaceholder = generatePlaceholderPdf(phText);
                    }
                    baseParts.add(partAnnex);
                }
                lastBodyPage = bodyPage + 1;
            }
        }

        if (lastBodyPage < numBodyPages) {
            MergePart partBody = new MergePart();
            partBody.isBody = true;
            partBody.bodyStart = lastBodyPage;
            partBody.bodyEnd = numBodyPages - 1;
            baseParts.add(partBody);
        }
        return baseParts;
    }

    private static List<MergePart> buildFinalParts(List<MergePart> baseParts, int numBodyPages, TableOfContentsModel tocConfig, List<AnnexSection> sections) throws Exception {
        List<MergePart> parts = new ArrayList<>();
        boolean tocEnabled = tocConfig != null && tocConfig.isEnabled();
        MergePart partTocPlaceholder = null;

        if (tocEnabled) {
            partTocPlaceholder = new MergePart();
            partTocPlaceholder.isBody = false;
            partTocPlaceholder.section = null;
            partTocPlaceholder.generatedPlaceholder = generatePlaceholderPdf("TABLE OF CONTENTS PLACEHOLDER");
        }

        if (!tocEnabled) {
            parts.addAll(baseParts);
        } else {
            int tocPos = tocConfig != null ? tocConfig.getPosition() : 0;
            if (tocPos == 0) {
                parts.add(partTocPlaceholder);
                parts.addAll(baseParts);
            } else if (tocPos == 1) {
                for (MergePart part : baseParts) {
                    parts.add(part);
                    if (part.isBody && part.bodyEnd == numBodyPages - 1) {
                        parts.add(partTocPlaceholder);
                    }
                }
            } else {
                int targetSecIdx = tocPos - 2;
                AnnexSection targetSection = (targetSecIdx < sections.size()) ? sections.get(targetSecIdx) : null;
                if (targetSection != null) {
                    for (MergePart part : baseParts) {
                        parts.add(part);
                        if (part.section == targetSection) {
                            parts.add(partTocPlaceholder);
                        }
                    }
                } else {
                    parts.addAll(baseParts);
                    parts.add(partTocPlaceholder);
                }
            }
        }
        return parts;
    }

    private static void calculateMergedPageMetrics(List<MergePart> parts, Map<AnnexSection, Integer> sectionToMergedPageMap, Map<AnnexDocument, Integer> docToMergedPageMap) throws Exception {
        int runningPageCount = 0;
        for (MergePart part : parts) {
            if (part.isBody) {
                runningPageCount += (part.bodyEnd - part.bodyStart) + 1;
            } else {
                if (part.section != null) {
                    sectionToMergedPageMap.put(part.section, runningPageCount);
                }
                if (part.generatedPlaceholder != null) {
                    runningPageCount += 1;
                } else {
                    for (AnnexDocument ad : part.section.getDocuments()) {
                        if (ad.getPdfBytes() != null && ad.getPdfBytes().length > 0) {
                            docToMergedPageMap.put(ad, runningPageCount);
                            try (PDDocument adDoc = loadDocumentOrConvertImage(ad.getPdfBytes())) {
                                runningPageCount += adDoc.getNumberOfPages();
                            }
                        }
                    }
                }
            }
        }
    }

    private static void generateRealTocIfEnabled(List<MergePart> parts, List<AnnexSection> sections, Map<AnnexSection, Integer> sectionToMergedPageMap, TableOfContentsModel tocConfig, Map<String, String> customI18n) throws Exception {
        boolean tocEnabled = tocConfig != null && tocConfig.isEnabled();
        if (tocEnabled) {
            MergePart partTocPlaceholder = null;
            for (MergePart part : parts) {
                if (!part.isBody && part.section == null) {
                    partTocPlaceholder = part;
                    break;
                }
            }
            if (partTocPlaceholder != null) {
                Map<String, Integer> sectionPageNumbers = new HashMap<>();
                for (Map.Entry<AnnexSection, Integer> entry : sectionToMergedPageMap.entrySet()) {
                    sectionPageNumbers.put(entry.getKey().getReportKind(), entry.getValue() + 1);
                }
                partTocPlaceholder.generatedPlaceholder = generateDynamicTocPage(sections, sectionPageNumbers, tocConfig, customI18n);
            }
        }
    }

    private static byte[] executeSequentialMerge(List<MergePart> parts, byte[] bodyPdf, Map<AnnexDocument, Integer> docToMergedPageMap, Map<Integer, Integer> originalToMergedPageMap) throws Exception {
        PDFMergerUtility merger = new PDFMergerUtility();
        PDDocument finalDocMerged = new PDDocument();
        List<PDDocument> docsToClose = new ArrayList<>();
        byte[] mergedPdfBytes;
        int mergedPageCount = 0;

        try {
            for (MergePart part : parts) {
                if (part.isBody) {
                    byte[] bodyPartBytes = getPagesSegment(bodyPdf, part.bodyStart, part.bodyEnd);
                    PDDocument segmentDoc = Loader.loadPDF(bodyPartBytes);
                    docsToClose.add(segmentDoc);
                    merger.appendDocument(finalDocMerged, segmentDoc);

                    for (int p = part.bodyStart; p <= part.bodyEnd; p++) {
                        originalToMergedPageMap.put(p, mergedPageCount);
                        logger.info("[ReportPdfAggregator] Map body page: original index " + p + " -> merged index " + mergedPageCount);
                        mergedPageCount++;
                    }
                } else {
                    if (part.generatedPlaceholder != null) {
                        PDDocument phDoc = Loader.loadPDF(part.generatedPlaceholder);
                        docsToClose.add(phDoc);
                        merger.appendDocument(finalDocMerged, phDoc);
                        mergedPageCount++;
                    } else {
                        for (AnnexDocument ad : part.section.getDocuments()) {
                            if (ad.getPdfBytes() != null && ad.getPdfBytes().length > 0) {
                                logger.info("[ReportPdfAggregator] Appending component doc '" + ad.getComponentName() + "' starting on merged index " + mergedPageCount + "...");
                                PDDocument adDoc = loadDocumentOrConvertImage(ad.getPdfBytes());
                                docsToClose.add(adDoc);
                                merger.appendDocument(finalDocMerged, adDoc);
                                docToMergedPageMap.put(ad, mergedPageCount);
                                mergedPageCount += adDoc.getNumberOfPages();
                            }
                        }
                    }
                }
            }
            ByteArrayOutputStream mergedOutputStream = new ByteArrayOutputStream();
            finalDocMerged.save(mergedOutputStream);
            mergedPdfBytes = mergedOutputStream.toByteArray();
        } finally {
            finalDocMerged.close();
            for (PDDocument d : docsToClose) {
                try { d.close(); } catch (Exception e) {}
            }
        }
        return mergedPdfBytes;
    }

    private static byte[] postProcessMergedPdf(byte[] mergedPdfBytes, byte[] bodyPdf, int numBodyPages, List<AnnexSection> sections, Map<AnnexSection, Integer> sectionToMergedPageMap, Map<AnnexDocument, Integer> docToMergedPageMap, List<TokenLocator.FoundToken> textTokens, Map<String, Integer> outlineBookmarks, HeaderModel header, byte[] logoBytes, Map<String, String> properties, ComponentHeadingStyle headingStyle, TableOfContentsModel tocConfig, PaginationModel paginationConfig, Map<Integer, Integer> originalToMergedPageMap, Map<String, String> customI18n) throws Exception {
        boolean tocEnabled = tocConfig != null && tocConfig.isEnabled();
        boolean paginationEnabled = paginationConfig != null && paginationConfig.isEnabled();

        try (PDDocument finalDoc = Loader.loadPDF(mergedPdfBytes)) {
            if (headingStyle != null) {
                stampComponentHeadings(finalDoc, sections, docToMergedPageMap, headingStyle);
            }

            rewriteToCPageNumbers(finalDoc, textTokens, sections, sectionToMergedPageMap, outlineBookmarks, originalToMergedPageMap);

            if (paginationEnabled) {
                coverOldPageNumbers(finalDoc, bodyPdf, numBodyPages, originalToMergedPageMap, sections, docToMergedPageMap);
            }

            if (tocEnabled) {
                addClickableToCLinks(finalDoc, sections, sectionToMergedPageMap);
            }

            if (header != null || paginationEnabled) {
                stampRunningHeadersFooters(finalDoc, header, logoBytes, properties, originalToMergedPageMap, sections, docToMergedPageMap, tocEnabled, paginationConfig, paginationEnabled, customI18n);
            }

            ByteArrayOutputStream finalOut = new ByteArrayOutputStream();
            finalDoc.save(finalOut);
            return finalOut.toByteArray();
        }
    }

    private static void stampComponentHeadings(PDDocument finalDoc, List<AnnexSection> sections, Map<AnnexDocument, Integer> docToMergedPageMap, ComponentHeadingStyle headingStyle) throws IOException {
        for (AnnexSection section : sections) {
            if (section.getDocuments() != null) {
                for (AnnexDocument ad : section.getDocuments()) {
                    Integer pIdx = docToMergedPageMap.get(ad);
                    if (pIdx != null && !ad.isBeCPGDoc()) {
                        stampComponentHeading(finalDoc, pIdx, ad.getComponentName(), headingStyle);
                    }
                }
            }
        }
    }

    private static void rewriteToCPageNumbers(PDDocument finalDoc, List<TokenLocator.FoundToken> textTokens, List<AnnexSection> sections, Map<AnnexSection, Integer> sectionToMergedPageMap, Map<String, Integer> outlineBookmarks, Map<Integer, Integer> originalToMergedPageMap) throws IOException {
        for (TokenLocator.FoundToken ft : textTokens) {
            if (ft.token.startsWith("{{page:")) {
                String rk = ft.token.substring(7, ft.token.length() - 2).trim();
                Integer targetMergedPage = null;

                for (AnnexSection section : sections) {
                    if (section.getReportKind() != null && section.getReportKind().trim().equals(rk)) {
                        targetMergedPage = sectionToMergedPageMap.get(section);
                        break;
                    }
                }

                if (targetMergedPage == null && outlineBookmarks.containsKey("becpg.tocsec." + rk)) {
                    int originalSecPage = outlineBookmarks.get("becpg.tocsec." + rk);
                    targetMergedPage = originalToMergedPageMap.get(originalSecPage);
                }

                int mergedEntryPageIdx = originalToMergedPageMap.getOrDefault(ft.pageIndex, ft.pageIndex);
                if (mergedEntryPageIdx < finalDoc.getNumberOfPages()) {
                    if (targetMergedPage != null) {
                        overwriteTokenWithPageNumber(finalDoc, mergedEntryPageIdx, ft, targetMergedPage + 1, targetMergedPage);
                    } else {
                        overwriteTokenWithText(finalDoc, mergedEntryPageIdx, ft, "N/A");
                    }
                }
            }
        }
    }

    private static void coverOldPageNumbers(PDDocument finalDoc, byte[] bodyPdf, int numBodyPages, Map<Integer, Integer> originalToMergedPageMap, List<AnnexSection> sections, Map<AnnexDocument, Integer> docToMergedPageMap) throws IOException {
        List<PageNumberLocator.FoundPageNumber> birtPageNums = new ArrayList<>();
        try (PDDocument originalBodyDoc = Loader.loadPDF(bodyPdf)) {
            PageNumberLocator pageNumLocator = new PageNumberLocator();
            birtPageNums.addAll(pageNumLocator.locatePageNumbers(originalBodyDoc));
        }

        for (PageNumberLocator.FoundPageNumber fpn : birtPageNums) {
            Integer mappedIdx = originalToMergedPageMap.get(fpn.pageIndex);
            if (mappedIdx != null) {
                fpn.pageIndex = mappedIdx;
            }
        }

        for (AnnexSection section : sections) {
            if (section.getDocuments() != null) {
                for (AnnexDocument ad : section.getDocuments()) {
                    if (ad.isBeCPGDoc()) {
                        Integer startPageIdx = docToMergedPageMap.get(ad);
                        if (startPageIdx != null) {
                            try (PDDocument adDoc = loadDocumentOrConvertImage(ad.getPdfBytes())) {
                                PageNumberLocator pageNumLocator = new PageNumberLocator();
                                List<PageNumberLocator.FoundPageNumber> annexPageNums = pageNumLocator.locatePageNumbers(adDoc);
                                for (PageNumberLocator.FoundPageNumber fpn : annexPageNums) {
                                    fpn.pageIndex = startPageIdx + fpn.pageIndex;
                                    birtPageNums.add(fpn);
                                }
                            }
                        }
                    }
                }
            }
        }

        for (PageNumberLocator.FoundPageNumber fpn : birtPageNums) {
            if (fpn.pageIndex < finalDoc.getNumberOfPages()) {
                PDPage page = finalDoc.getPage(fpn.pageIndex);
                float pdfY = fpn.y;
                try (PDPageContentStream canvas = new PDPageContentStream(finalDoc, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                    canvas.setNonStrokingColor(java.awt.Color.WHITE);
                    canvas.addRect(fpn.x - 10, pdfY - 5, 150, fpn.height + 10);
                    canvas.fill();
                }
            }
        }

        int totalPages = finalDoc.getNumberOfPages();
        for (int p = 0; p < totalPages; p++) {
            PDPage page = finalDoc.getPage(p);
            float width = page.getMediaBox().getWidth();
            try (PDPageContentStream canvas = new PDPageContentStream(finalDoc, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                canvas.setNonStrokingColor(java.awt.Color.WHITE);
                canvas.addRect(width - 180, 5, 165, 50);
                canvas.fill();
            }
        }
    }

    private static void addClickableToCLinks(PDDocument finalDoc, List<AnnexSection> sections, Map<AnnexSection, Integer> sectionToMergedPageMap) throws IOException {
        PDPage tocPage = finalDoc.getPage(1);
        float y = tocPage.getMediaBox().getHeight() - 130;
        for (AnnexSection sec : sections) {
            if (sec.getDocuments() == null || sec.getDocuments().isEmpty()) {
                continue;
            }
            Integer targetMergedPage = sectionToMergedPageMap.get(sec);
            if (targetMergedPage != null) {
                PDAnnotationLink link = new PDAnnotationLink();
                PDBorderStyleDictionary border = new PDBorderStyleDictionary();
                border.setWidth(0);
                link.setBorderStyle(border);

                PDRectangle rect = new PDRectangle();
                rect.setLowerLeftX(50);
                rect.setLowerLeftY(y - 5);
                rect.setUpperRightX(tocPage.getMediaBox().getWidth() - 50);
                rect.setUpperRightY(y + 12);
                link.setRectangle(rect);

                PDActionGoTo action = new PDActionGoTo();
                PDPageDestination dest = new PDPageFitWidthDestination();
                dest.setPage(finalDoc.getPage(targetMergedPage));
                action.setDestination(dest);
                link.setAction(action);

                tocPage.getAnnotations().add(link);
            }
            y -= 25;
        }
    }

    private static void stampRunningHeadersFooters(PDDocument finalDoc, HeaderModel header, byte[] logoBytes, Map<String, String> properties, Map<Integer, Integer> originalToMergedPageMap, List<AnnexSection> sections, Map<AnnexDocument, Integer> docToMergedPageMap, boolean tocEnabled, PaginationModel paginationConfig, boolean paginationEnabled, Map<String, String> customI18n) throws IOException {
        Set<Integer> noHeaderPageIndexes = new HashSet<>(originalToMergedPageMap.values());
        if (tocEnabled) {
            noHeaderPageIndexes.add(1);
        }
        for (AnnexSection section : sections) {
            if (section.getDocuments() != null) {
                for (AnnexDocument ad : section.getDocuments()) {
                    if (ad.isBeCPGDoc()) {
                        Integer startPageIdx = docToMergedPageMap.get(ad);
                        if (startPageIdx != null) {
                            try (PDDocument adDoc = loadDocumentOrConvertImage(ad.getPdfBytes())) {
                                int pages = adDoc.getNumberOfPages();
                                for (int p = startPageIdx; p < startPageIdx + pages; p++) {
                                    noHeaderPageIndexes.add(p);
                                }
                            }
                        }
                    }
                }
            }
        }
        stampHeaderFooter(finalDoc, header, logoBytes, properties, noHeaderPageIndexes, paginationConfig, paginationEnabled, customI18n);
    }

    private static void traverseOutline(PDOutlineItem item, Map<String, Integer> bookmarkPages, PDDocument document) {
        if (item == null) return;
        String title = item.getTitle();
        if (title != null) {
            try {
                int pageNum = resolvePageNum(item, document);
                if (pageNum != -1) {
                    bookmarkPages.put(title, pageNum);
                }
            } catch (Exception e) {
                logger.error("Error resolving outline page number: " + e.getMessage(), e);
            }
        }
        traverseOutline(item.getFirstChild(), bookmarkPages, document);
        traverseOutline(item.getNextSibling(), bookmarkPages, document);
    }

    private static int resolvePageNum(PDOutlineItem item, PDDocument doc) throws IOException {
        if (item.getDestination() != null) {
            return findPageNum(item.getDestination(), doc);
        } else if (item.getAction() instanceof PDActionGoTo) {
            PDActionGoTo action = (PDActionGoTo) item.getAction();
            return findPageNum(action.getDestination(), doc);
        }
        return -1;
    }

    private static int findPageNum(org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination dest, PDDocument doc) throws IOException {
        if (dest instanceof PDPageDestination) {
            PDPageDestination pageDest = (PDPageDestination) dest;
            PDPage page = pageDest.getPage();
            if (page != null) {
                return doc.getPages().indexOf(page);
            }
            return pageDest.getPageNumber();
        }
        return -1;
    }

    private static byte[] getPagesSegment(byte[] pdf, int startPage0, int endPage0) throws IOException {
        try (PDDocument source = Loader.loadPDF(pdf); PDDocument target = new PDDocument()) {
            for (int i = startPage0; i <= endPage0; i++) {
                target.addPage(source.getPage(i));
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            target.save(out);
            return out.toByteArray();
        }
    }

    public static String resolveI18nKey(String key, Map<String, String> customI18n) {
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
            // Fallback to key
        }
        return key;
    }

    private static String sanitizeTextForFont(String text, org.apache.pdfbox.pdmodel.font.PDFont font) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            try {
                font.encode(String.valueOf(c));
                sb.append(c);
            } catch (Exception e) {
                sb.append('?');
            }
        }
        return sb.toString();
    }

    private static byte[] generatePlaceholderPdf(String text) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream canvas = new PDPageContentStream(doc, page)) {
                canvas.beginText();
                PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                canvas.setFont(font, 12);
                canvas.setNonStrokingColor(java.awt.Color.DARK_GRAY);
                canvas.newLineAtOffset(100, 500);
                canvas.showText(sanitizeTextForFont(text, font));
                canvas.endText();
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }

    private static void stampComponentHeading(PDDocument doc, int pageIndex, String componentName, ComponentHeadingStyle style) throws IOException {
        PDPage page = doc.getPage(pageIndex);
        float height = page.getMediaBox().getHeight();
        try (PDPageContentStream canvas = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
            canvas.beginText();
            java.awt.Color color = java.awt.Color.decode(style.getColor() != null ? style.getColor() : "#1F3864");
            canvas.setNonStrokingColor(color);
            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            canvas.setFont(font, style.getSize() != null ? style.getSize() : 11);
            canvas.newLineAtOffset(50, height - 85);
            String text = (style.getPrefix() != null ? style.getPrefix() : "") + componentName.toUpperCase();
            canvas.showText(sanitizeTextForFont(text, font));
            canvas.endText();
        }
    }

    private static void overwriteTokenWithPageNumber(PDDocument doc, int entryPageIndex, TokenLocator.FoundToken ft, int resolvedPageNumber, int targetPageIndex) throws IOException {
        PDPage page = doc.getPage(entryPageIndex);
        float pdfY = page.getMediaBox().getHeight() - ft.y;
        try (PDPageContentStream canvas = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
            canvas.setNonStrokingColor(java.awt.Color.WHITE);
            canvas.addRect(ft.x - 2, pdfY - 2, ft.width + 4, ft.height + 4);
            canvas.fill();

            String pageStr = String.valueOf(resolvedPageNumber);
            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            float fontSize = 9.0f;
            float strWidth = font.getStringWidth(pageStr) / 1000.0f * fontSize;
            float drawX = ft.x + ft.width - strWidth;

            canvas.beginText();
            canvas.setNonStrokingColor(new java.awt.Color(31, 56, 100));
            canvas.setFont(font, fontSize);
            canvas.newLineAtOffset(drawX, pdfY);
            canvas.showText(pageStr);
            canvas.endText();

            PDAnnotationLink link = new PDAnnotationLink();
            PDBorderStyleDictionary border = new PDBorderStyleDictionary();
            border.setWidth(0);
            link.setBorderStyle(border);

            PDRectangle rect = new PDRectangle();
            rect.setLowerLeftX(drawX - 2);
            rect.setLowerLeftY(pdfY - 2);
            rect.setUpperRightX(drawX + strWidth + 2);
            rect.setUpperRightY(pdfY + ft.height + 2);
            link.setRectangle(rect);

            PDActionGoTo action = new PDActionGoTo();
            PDPageDestination dest = new PDPageFitWidthDestination();
            dest.setPage(doc.getPage(targetPageIndex));
            action.setDestination(dest);
            link.setAction(action);

            page.getAnnotations().add(link);
        }
    }

    private static void overwriteTokenWithText(PDDocument doc, int entryPageIndex, TokenLocator.FoundToken ft, String text) throws IOException {
        PDPage page = doc.getPage(entryPageIndex);
        float pdfY = page.getMediaBox().getHeight() - ft.y;
        try (PDPageContentStream canvas = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
            canvas.setNonStrokingColor(java.awt.Color.WHITE);
            canvas.addRect(ft.x - 2, pdfY - 2, ft.width + 4, ft.height + 4);
            canvas.fill();

            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            float fontSize = 8.0f;
            float strWidth = font.getStringWidth(text) / 1000.0f * fontSize;
            float drawX = ft.x + ft.width - strWidth;

            canvas.beginText();
            canvas.setNonStrokingColor(java.awt.Color.GRAY);
            canvas.setFont(font, fontSize);
            canvas.newLineAtOffset(drawX, pdfY + 1);
            canvas.showText(text);
            canvas.endText();
        }
    }

    private static PDDocument loadDocumentOrConvertImage(byte[] bytes) throws IOException {
        try {
            return Loader.loadPDF(bytes);
        } catch (IOException e) {
            return convertImageToPdfDocument(bytes);
        }
    }

    private static PDDocument convertImageToPdfDocument(byte[] bytes) throws IOException {
        PDDocument doc = new PDDocument();
        try {
            PDImageXObject image = PDImageXObject.createFromByteArray(doc, bytes, "annex-image");
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            float margin = 40;
            float scale = Math.min((pageWidth - 2 * margin) / image.getWidth(), (pageHeight - 2 * margin) / image.getHeight());
            if (scale > 1.0f) {
                scale = 1.0f;
            }

            float drawWidth = image.getWidth() * scale;
            float drawHeight = image.getHeight() * scale;
            try (PDPageContentStream canvas = new PDPageContentStream(doc, page)) {
                canvas.drawImage(image, (pageWidth - drawWidth) / 2, (pageHeight - drawHeight) / 2, drawWidth, drawHeight);
            }
            return doc;
        } catch (Exception ex) {
            doc.close();
            throw new IOException("Failed to load as PDF or convert image: " + ex.getMessage(), ex);
        }
    }

    private static void stampHeaderFooter(PDDocument doc, HeaderModel header, byte[] logoBytes, Map<String, String> properties, Collection<Integer> bodyPageIndexes, PaginationModel paginationConfig, boolean paginationEnabled, Map<String, String> customI18n) throws IOException {
        PDImageXObject logoImg = null;
        if (logoBytes != null && logoBytes.length > 0) {
            try {
                logoImg = PDImageXObject.createFromByteArray(doc, logoBytes, "logo");
            } catch (Exception e) {
                logger.error("Failed to parse logo bytes for overlay", e);
            }
        }

        int totalPages = doc.getNumberOfPages();
        int startPage = 1;
        if (paginationConfig != null && paginationConfig.getStartNumberingAt() != null) {
            startPage = paginationConfig.getStartNumberingAt();
        } else if (header != null && header.getStartNumberingAt() != null) {
            startPage = header.getStartNumberingAt();
        }

        String title = header != null ? resolveI18nKey(resolvePlaceholders(header.getTitle(), properties), customI18n) : "";
        String subtitle = header != null ? resolveI18nKey(resolvePlaceholders(header.getSubtitle(), properties), customI18n) : "";
        String dateStr = header != null ? resolvePlaceholders(header.getDate(), properties) : "";

        for (int i = startPage - 1; i < totalPages; i++) {
            PDPage page = doc.getPage(i);
            PDRectangle mediaBox = page.getMediaBox();
            float width = mediaBox.getWidth();
            float height = mediaBox.getHeight();

            boolean isBodyPage = bodyPageIndexes != null && bodyPageIndexes.contains(i);

            try (PDPageContentStream canvas = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                if (header != null && !isBodyPage) {
                    canvas.setStrokingColor(java.awt.Color.LIGHT_GRAY);
                    canvas.setLineWidth(0.5f);
                    canvas.moveTo(50, height - 60);
                    canvas.lineTo(width - 50, height - 60);
                    canvas.stroke();

                    canvas.beginText();
                    canvas.setNonStrokingColor(new java.awt.Color(31, 56, 100));
                    PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                    canvas.setFont(boldFont, 8);
                    canvas.newLineAtOffset(50, height - 45);
                    canvas.showText(sanitizeTextForFont(title != null ? title : "", boldFont));
                    canvas.endText();

                    PDType1Font regularFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                    if (subtitle != null && !subtitle.isEmpty()) {
                        canvas.beginText();
                        canvas.setNonStrokingColor(java.awt.Color.DARK_GRAY);
                        canvas.setFont(regularFont, 8);
                        canvas.newLineAtOffset(50, height - 55);
                        canvas.showText(sanitizeTextForFont(subtitle, regularFont));
                        canvas.endText();
                    }

                    if (logoImg != null) {
                        float logoWidth = 60;
                        float logoHeight = 20;
                        canvas.drawImage(logoImg, width - 50 - logoWidth, height - 50, logoWidth, logoHeight);
                    } else if (dateStr != null && !dateStr.isEmpty()) {
                        canvas.beginText();
                        canvas.setNonStrokingColor(java.awt.Color.GRAY);
                        canvas.setFont(regularFont, 8);
                        canvas.newLineAtOffset(width - 150, height - 45);
                        canvas.showText(sanitizeTextForFont(dateStr, regularFont));
                        canvas.endText();
                    }
                }

                if (paginationEnabled) {
                    canvas.setNonStrokingColor(java.awt.Color.WHITE);
                    canvas.addRect(0, 0, width, 45);
                    canvas.fill();
                }

                if (paginationEnabled) {
                    canvas.setStrokingColor(java.awt.Color.LIGHT_GRAY);
                    canvas.setLineWidth(0.5f);
                    canvas.moveTo(50, 50);
                    canvas.lineTo(width - 50, 50);
                    canvas.stroke();

                    String pageFormat = "Page ${page} / ${total}";
                    if (paginationConfig != null && paginationConfig.getFormat() != null && !paginationConfig.getFormat().isEmpty()) {
                        pageFormat = paginationConfig.getFormat();
                    }
                    String pageText = pageFormat.replace("${page}", String.valueOf(i + 1)).replace("${total}", String.valueOf(totalPages));

                    canvas.beginText();
                    canvas.setNonStrokingColor(java.awt.Color.DARK_GRAY);
                    PDType1Font regularFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                    canvas.setFont(regularFont, 8);
                    canvas.newLineAtOffset(width - 120, 38);
                    canvas.showText(sanitizeTextForFont(pageText, regularFont));
                    canvas.endText();
                }
            }
        }
    }

    private static String resolvePlaceholders(String template, Map<String, String> properties) {
        if (template == null) return "";
        String result = template;
        if (properties != null) {
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                if (entry.getValue() != null) {
                    result = result.replace("${" + entry.getKey() + "}", entry.getValue());
                }
            }
        }
        result = result.replaceAll("\\$\\{[^}]+\\}", "");
        return result.trim();
    }

    private static boolean hasExistingBeCPGLayout(byte[] pdfBytes) {
        if (pdfBytes == null || pdfBytes.length == 0) return false;
        try (PDDocument doc = Loader.loadPDF(pdfBytes)) {
            if (doc.getNumberOfPages() > 0) {
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setStartPage(1);
                stripper.setEndPage(1);
                String text = stripper.getText(doc);
                if (text != null && (text.contains("beCPG") || text.contains("Fiche Technique") || text.contains("FICHE TECHNIQUE") || text.contains("Fiche R&D") || text.contains("Fiche Coûts"))) {
                    return true;
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return false;
    }

    private static byte[] generateDynamicTocPage(List<AnnexSection> sections, Map<String, Integer> sectionPages, TableOfContentsModel config) throws IOException {
        return generateDynamicTocPage(sections, sectionPages, config, null);
    }

    private static byte[] generateDynamicTocPage(List<AnnexSection> sections, Map<String, Integer> sectionPages, TableOfContentsModel config, Map<String, String> customI18n) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            PDRectangle mediaBox = page.getMediaBox();
            float width = mediaBox.getWidth();
            float height = mediaBox.getHeight();

            try (PDPageContentStream canvas = new PDPageContentStream(doc, page)) {
                canvas.beginText();
                java.awt.Color titleColor = java.awt.Color.decode(config.getColor() != null ? config.getColor() : "#1F3864");
                canvas.setNonStrokingColor(titleColor);
                PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                canvas.setFont(boldFont, config.getSize() != null ? config.getSize() : 16);
                canvas.newLineAtOffset(50, height - 80);
                canvas.showText(sanitizeTextForFont(resolveI18nKey(config.getTitle(), customI18n), boldFont));
                canvas.endText();

                canvas.setStrokingColor(java.awt.Color.LIGHT_GRAY);
                canvas.setLineWidth(1.0f);
                canvas.moveTo(50, height - 95);
                canvas.lineTo(width - 50, height - 95);
                canvas.stroke();

                float y = height - 130;
                PDType1Font regularFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                for (AnnexSection sec : sections) {
                    if (sec.getDocuments() == null || sec.getDocuments().isEmpty()) {
                        continue;
                    }
                    Integer pNum = sectionPages.get(sec.getReportKind());
                    if (pNum != null) {
                        String sectionTitle = resolveI18nKey(sec.getTitle(), customI18n);
                        canvas.beginText();
                        canvas.setNonStrokingColor(java.awt.Color.DARK_GRAY);
                        canvas.setFont(regularFont, 10);
                        canvas.newLineAtOffset(50, y);
                        canvas.showText(sanitizeTextForFont(sectionTitle, regularFont));
                        canvas.endText();

                        canvas.beginText();
                        canvas.newLineAtOffset(width - 70, y);
                        canvas.showText(String.valueOf(pNum));
                        canvas.endText();

                        canvas.setStrokingColor(java.awt.Color.LIGHT_GRAY);
                        canvas.setLineWidth(0.5f);
                        canvas.setLineDashPattern(new float[]{1, 3}, 0);
                        canvas.moveTo(150 + regularFont.getStringWidth(sectionTitle) / 100f, y + 2);
                        canvas.lineTo(width - 90, y + 2);
                        canvas.stroke();
                    }
                    y -= 25;
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }
}