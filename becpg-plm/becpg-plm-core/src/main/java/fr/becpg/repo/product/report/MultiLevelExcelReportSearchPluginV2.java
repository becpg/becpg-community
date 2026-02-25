package fr.becpg.repo.product.report;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.MPMModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.datalist.MultiLevelDataListService;
import fr.becpg.repo.entity.datalist.WUsedListService;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.helper.ExcelHelper;
import fr.becpg.repo.helper.ExcelHelper.ExcelCellStyles;
import fr.becpg.repo.helper.JsonFormulaHelper;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;
import fr.becpg.repo.product.data.CurrentLevelQuantities;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.formulation.PackagingHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * Excel report plugin for multi-level extraction (Composition, Process, Packaging) from finished products and semi-finished products.
 *
 * <h2>Architecture</h2>
 * <ul>
 * <li><b>Composition, Process, Packaging (without WUsed)</b>: Uses ProductReportExtractorPlugin logic with CurrentLevelQuantities for precise calculations</li>
 * <li><b>WUsed and other list types</b>: Uses MultiLevelDataListService for generic multi-level exploration</li>
 * </ul>
 *
 * <h2>Configuration in Excel Template</h2>
 * <p>First row defines the list type and parameters:</p>
 * <pre>
 * TYPE    bcpg:packagingList    MultiLevel
 * TYPE    bcpg:compoList         AllLevel
 * TYPE    mpm:processList        MaxLevel2
 * TYPE    bcpg:packagingList    wUsedAllLevel
 * </pre>
 *
 * <h3>Supported Parameters</h3>
 * <ul>
 * <li><b>AllLevel</b>: Export all levels (unlimited depth)</li>
 * <li><b>MaxLevel[N]</b>: Export up to level N (e.g., MaxLevel2 for 2 levels)</li>
 * <li><b>OnlyLevel[N]</b>: Export only level N items</li>
 * <li><b>wUsed[Parameter]</b>: Use "Where Used" mode with MultiLevelDataListService (e.g., wUsedAllLevel, wUsedMaxLevel2)</li>
 * <li><b>[Parameter]IncludeEmpty</b>: Include rows for entities even when lists are empty (e.g., AllLevelIncludeEmpty, MaxLevel2IncludeEmpty)</li>
 * </ul>
 *
 * <h2>Available Excel Columns (Second Row)</h2>
 *
 * <h3>Common Columns (All Lists)</h3>
 * <ul>
 * <li><b>prop_bcpg_depthLevel</b>: Depth level in the hierarchy (1, 2, 3...)</li>
 * <li><b>prop_bcpg_qtyForCost</b>: Calculated quantity for cost</li>
 * <li><b>prop_cm_name</b>: Item name</li>
 * <li><b>prop_bcpg_code</b>: Item code</li>
 * </ul>
 *
 * <h3>Packaging List Columns (bcpg:packagingList)</h3>
 * <ul>
 * <li><b>prop_bcpg_packagingListQty</b>: Packaging quantity</li>
 * <li><b>prop_bcpg_packagingListQtyForProduct</b>: Calculated quantity for product</li>
 * <li><b>prop_bcpg_packagingListUnit</b>: Packaging unit</li>
 * <li><b>prop_bcpg_packagingListProduct</b>: Associated packaging material</li>
 * <li><b>prop_bcpg_packagingListPkgLevel</b>: Packaging level (Primary, Secondary, Tertiary, Inner)</li>
 * <li><b>prop_bcpg_productDropPackagingOfComponents</b>: Flag indicating if packaging should be dropped</li>
 * <li><b>prop_bcpg_packagingListLossPerc</b>: Loss percentage</li>
 * </ul>
 *
 * <h3>Composition List Columns (bcpg:compoList)</h3>
 * <ul>
 * <li><b>prop_bcpg_compoListQty</b>: Component quantity</li>
 * <li><b>prop_bcpg_compoListQtyForProduct</b>: Calculated quantity for product</li>
 * <li><b>prop_bcpg_compoListUnit</b>: Component unit</li>
 * <li><b>prop_bcpg_compoListProduct</b>: Associated component (raw material, semi-finished product)</li>
 * <li><b>prop_bcpg_compoListLossPerc</b>: Loss percentage</li>
 * <li><b>prop_bcpg_compoListQtySubFormula</b>: Sub-formula for quantity calculation</li>
 * </ul>
 *
 * <h3>Process List Columns (mpm:processList)</h3>
 * <ul>
 * <li><b>prop_mpm_plQty</b>: Process quantity</li>
 * <li><b>prop_bcpg_processListQtyForProduct</b>: Calculated quantity for product</li>
 * <li><b>prop_mpm_plResource</b>: Associated resource</li>
 * <li><b>prop_mpm_plQtyResource</b>: Resource quantity</li>
 * <li><b>prop_mpm_plUnit</b>: Process unit</li>
 * <li><b>prop_mpm_plDuration</b>: Process duration</li>
 * </ul>
 *
 * <h3>Entity Properties (Prefixed with entity_)</h3>
 * <p>Access parent product properties using entity_ prefix:</p>
 * <ul>
 * <li><b>entity_prop_cm_name</b>: Parent product name</li>
 * <li><b>entity_prop_bcpg_code</b>: Parent product code</li>
 * <li><b>entity_prop_bcpg_erpCode</b>: Parent product ERP code</li>
 * </ul>
 *
 * <h3>Dynamic Characteristics (Prefixed with dyn_)</h3>
 * <p>Access dynamic characteristics from composition/packaging/process lists:</p>
 * <ul>
 * <li><b>dyn_[CharacteristicName]</b>: Dynamic characteristic value (spaces replaced by underscores)</li>
 * </ul>
 *
 * <h3>WUsed Mode Additional Columns</h3>
 * <p>When using wUsed parameter, additional columns are available:</p>
 * <ul>
 * <li><b>wUsedEntity_[property]</b>: Properties from the entity using this component</li>
 * <li><b>prop_bcpg_parent</b>: Parent entity code</li>
 * </ul>
 *
 * <h3>Formula Columns</h3>
 * <p>Custom calculated columns using SpEL expressions:</p>
 * <ul>
 * <li><b>formula|[SpEL Expression]</b>: Evaluated formula (e.g., formula|entity.name + ' - ' + product.name)</li>
 * <li><b>image|[SpEL Expression]</b>: Image URL formula</li>
 * </ul>
 *
 * <h2>Example Configuration</h2>
 * <pre>
 * Row 1: TYPE    bcpg:packagingList    MultiLevel
 * Row 2: bcpg:code    prop_bcpg_packagingListProduct    prop_bcpg_packagingListQty    prop_bcpg_packagingListQtyForProduct    prop_bcpg_depthLevel    entity_prop_cm_name
 * Row 3: (Leave empty or add # for comments)
 * Row 4+: Data will be populated here
 * </pre>
 *
 * <h2>IncludeEmpty Feature</h2>
 * <p>When the IncludeEmpty parameter is added, the plugin will create a row for entities even when their respective lists are empty:</p>
 * <ul>
 * <li><b>AllLevelIncludeEmpty</b>: Export all levels and include rows for entities with empty lists</li>
 * <li><b>MaxLevel2IncludeEmpty</b>: Export up to level 2 and include rows for entities with empty lists</li>
 * <li><b>OnlyLevel1IncludeEmpty</b>: Export only level 1 and include rows for entities with empty lists</li>
 * </ul>
 * <p>This is useful for ensuring that all entities are represented in the export, even when they don't have specific list items.</p>
 *
 * <h2>Behavior</h2>
 * <ul>
 * <li>Automatically recurses into semi-finished products and finished products in composition</li>
 * <li>Calculates quantities at each level using CurrentLevelQuantities</li>
 * <li>Handles packaging kits recursively</li>
 * <li>Supports up to 20 levels of depth (prevents infinite loops)</li>
 * <li>Respects effective filters (only exports effective items)</li>
 * <li>Applies formulas and evaluates SpEL expressions</li>
 * <li>When IncludeEmpty is specified, creates entity rows even when hasPackagingListEl, hasCompoListEl, or hasProcessListEl returns false</li>
 * </ul>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class MultiLevelExcelReportSearchPluginV2 extends DynamicCharactExcelReportSearchPlugin {

    private static final String PARAM_SUFFIX_LEVEL = "Level";
    private static final String PARAM_PREFIX_WUSED = "wUsed";
    private static final String TOKEN_MAX = "Max";
    private static final String TOKEN_ONLY = "Only";
    private static final String TOKEN_ALL = "All";
    private static final String TOKEN_INCLUDE_EMPTY = "IncludeEmpty";
    private static final String ONLY_LEVEL_PREFIX = "OnlyLevel";
    private static final int MAX_RECURSION_DEPTH = 20;
    private static final int DEPTH_UNLIMITED = -1;
    private static final String PERMISSION_READ = "Read";
    private static final String HEADER_VALUES = "VALUES";
    private static final String KEY_PACKAGING_QTY_FOR_PRODUCT = "prop_bcpg_packagingListQtyForProduct";
    private static final String KEY_COMPO_QTY_FOR_PRODUCT = "prop_bcpg_compoListQtyForProduct";
    private static final String KEY_PROCESS_QTY_FOR_PRODUCT = "prop_bcpg_processListQtyForProduct";
    private static final String KEY_QTY_FOR_COST = "prop_bcpg_qtyForCost";
    private static final String KEY_DEPTH_LEVEL = "prop_bcpg_depthLevel";
    private static final String KEY_PARENT = "prop_bcpg_parent";
    private static final String PREFIX_DYNAMIC_CHAR_COLUMN = "prop_bcpg_dynamicCharactColumn";
    private static final String PREFIX_WUSED_ENTITY = "wUsedEntity_";
    private static final String KEY_PACKAGING_LOSS_PERC = "prop_" + PLMModel.PROP_PACKAGINGLIST_LOSS_PERC.getLocalName();
    private static final String KEY_PACKAGING_QTY = "prop_" + PLMModel.PROP_PACKAGINGLIST_QTY.getLocalName();
    private static final String KEY_COMPO_LOSS_PERC = "prop_" + PLMModel.PROP_COMPOLIST_LOSS_PERC.getLocalName();

    private final PackagingHelper packagingHelper;
    private final AlfrescoRepository<BeCPGDataObject> alfrescoRepository;
    private final MultiLevelDataListService multiLevelDataListService;
    private final WUsedListService wUsedListService;

    @Autowired
    /**
     * <p>Constructor for MultiLevelExcelReportSearchPluginV2.</p>
     *
     * @param packagingHelper a {@link fr.becpg.repo.product.formulation.PackagingHelper} object
     * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object
     * @param multiLevelDataListService a {@link fr.becpg.repo.entity.datalist.MultiLevelDataListService} object
     * @param wUsedListService a {@link fr.becpg.repo.entity.datalist.WUsedListService} object
     */
    public MultiLevelExcelReportSearchPluginV2(PackagingHelper packagingHelper, AlfrescoRepository<BeCPGDataObject> alfrescoRepository,
            MultiLevelDataListService multiLevelDataListService, WUsedListService wUsedListService) {
        this.packagingHelper = packagingHelper;
        this.alfrescoRepository = alfrescoRepository;
        this.multiLevelDataListService = multiLevelDataListService;
        this.wUsedListService = wUsedListService;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDefault() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isApplicable(QName itemType, String[] parameters) {
        String parameter = (parameters != null) && (parameters.length > 0) ? parameters[0] : null;
        return (PLMModel.TYPE_PACKAGINGLIST.equals(itemType) || PLMModel.TYPE_COMPOLIST.equals(itemType) || MPMModel.TYPE_PROCESSLIST.equals(itemType))
                && ((parameter != null) && parameter.contains(PARAM_SUFFIX_LEVEL));
    }

    /** {@inheritDoc} */
    @Override
    public int fillSheet(XSSFSheet sheet, List<NodeRef> searchResults, QName mainType, QName itemType, int rownum, String[] parameters,
            AttributeExtractorStructure keyColumn, List<AttributeExtractorStructure> metadataFields, Map<NodeRef, Map<String, Object>> cache) {

        String parameter = (parameters != null) && (parameters.length > 0) ? parameters[0] : null;
        boolean wUsed = false;
        boolean includeEmpty = false;
        QName pivotAssoc = null;
        String depthLevel;

        if (parameter != null) {
            wUsed = parameter.contains(PARAM_PREFIX_WUSED);
            includeEmpty = parameter.contains(TOKEN_INCLUDE_EMPTY);
            if (wUsed) {
                parameter = parameter.replace(PARAM_PREFIX_WUSED, "");
                pivotAssoc = entityDictionaryService.getDefaultPivotAssoc(itemType);
                mainType = entityDictionaryService.getTargetType(pivotAssoc);
            }
            if (includeEmpty) {
                parameter = parameter.replace(TOKEN_INCLUDE_EMPTY, "");
            }
            depthLevel = parameter.replace(PARAM_SUFFIX_LEVEL, "").replace(TOKEN_MAX, "").replace(TOKEN_ONLY, "");
        } else {
            depthLevel = TOKEN_ALL;
        }

        ExcelCellStyles excelCellStyles = new ExcelCellStyles(sheet.getWorkbook());
        final int depthLevelNum = TOKEN_ALL.equals(depthLevel) ? DEPTH_UNLIMITED : Integer.parseInt(depthLevel);
        final Map<NodeRef, Map<QName, Serializable>> wUsedAssocCache = wUsed ? new HashMap<>() : null;

        // Use ProductReportExtractorPlugin logic for Composition, Process, Packaging
        if (PLMModel.TYPE_COMPOLIST.equals(itemType) || PLMModel.TYPE_PACKAGINGLIST.equals(itemType) || MPMModel.TYPE_PROCESSLIST.equals(itemType)) {
            if (!wUsed) {
                rownum = fillSheetWithExtractorLogic(sheet, searchResults, mainType, itemType, rownum, parameters, keyColumn, metadataFields,
                        cache, excelCellStyles, depthLevelNum, includeEmpty);
            } else {
                // WUsed for these types uses MultiLevelDataListService
                rownum = fillSheetWithMultiLevelService(sheet, searchResults, mainType, itemType, rownum, parameters, keyColumn, metadataFields,
                        cache, excelCellStyles, depthLevelNum, pivotAssoc, wUsedAssocCache);
            }
        } else {
            // Other types use MultiLevelDataListService
            rownum = fillSheetWithMultiLevelService(sheet, searchResults, mainType, itemType, rownum, parameters, keyColumn, metadataFields,
                    cache, excelCellStyles, depthLevelNum, pivotAssoc, wUsedAssocCache);
        }

        return rownum;
    }

    /**
     * Fills the Excel sheet using extractor logic for Composition, Process, and Packaging lists.
     * Supports the IncludeEmpty parameter to create rows for entities even when lists are empty.
     *
     * @param sheet the Excel sheet to fill
     * @param searchResults the list of entity node references to process
     * @param mainType the main entity type
     * @param itemType the list item type (bcpg:compoList, bcpg:packagingList, mpm:processList)
     * @param rownum the starting row number
     * @param parameters the configuration parameters (e.g., "AllLevel", "MaxLevel2", "AllLevelIncludeEmpty")
     * @param keyColumn the key column for entity identification
     * @param metadataFields the metadata fields to extract
     * @param cache the cache for storing extracted data
     * @param excelCellStyles the Excel cell styles
     * @param depthLevelNum the maximum depth level to export
     * @param includeEmpty whether to create rows for entities with empty lists
     * @return the new row number after processing
     */
    private int fillSheetWithExtractorLogic(XSSFSheet sheet, List<NodeRef> searchResults, QName mainType, QName itemType, int rownum,
            String[] parameters, AttributeExtractorStructure keyColumn, List<AttributeExtractorStructure> metadataFields,
            Map<NodeRef, Map<String, Object>> cache, ExcelCellStyles excelCellStyles, int depthLevelNum, boolean includeEmpty) {

        String parameter = (parameters != null) && (parameters.length > 0) ? parameters[0] : null;
        boolean isOnlyLevel = parameter != null && parameter.contains(TOKEN_ONLY);

        for (NodeRef entityNodeRef : searchResults) {
            QName entityType = nodeService.getType(entityNodeRef);
            if (mainType.equals(entityType) || entityDictionaryService.isSubClass(entityType, mainType)) {
                Serializable key = extractKey(entityNodeRef, keyColumn);
                ProductData productData = (ProductData) alfrescoRepository.findOne(entityNodeRef);

                Map<String, Object> entityItems = getEntityProperties(entityNodeRef, mainType, metadataFields, cache);
                entityItems.putAll(getDynamicProperties(entityNodeRef, itemType));

                String filter = EffectiveFilters.EFFECTIVE;

                if (PLMModel.TYPE_PACKAGINGLIST.equals(itemType)) {
                    rownum = fillPackagingSheet(sheet, entityNodeRef, productData, rownum, key, metadataFields, cache, entityItems,
                            excelCellStyles, filter, depthLevelNum, isOnlyLevel, parameter, includeEmpty);
                } else if (PLMModel.TYPE_COMPOLIST.equals(itemType)) {
                    rownum = fillCompositionSheet(sheet, entityNodeRef, productData, rownum, key, metadataFields, cache, entityItems,
                            excelCellStyles, filter, depthLevelNum, isOnlyLevel, parameter, includeEmpty);
                } else if (MPMModel.TYPE_PROCESSLIST.equals(itemType)) {
                    rownum = fillProcessSheet(sheet, entityNodeRef, productData, rownum, key, metadataFields, cache, entityItems,
                            excelCellStyles, filter, depthLevelNum, isOnlyLevel, parameter, includeEmpty);
                }
            }
        }

        return rownum;
    }

    private int fillSheetWithMultiLevelService(XSSFSheet sheet, List<NodeRef> searchResults, QName mainType, QName itemType, int rownum,
            String[] parameters, AttributeExtractorStructure keyColumn, List<AttributeExtractorStructure> metadataFields,
            Map<NodeRef, Map<String, Object>> cache, ExcelCellStyles excelCellStyles, int depthLevelNum, QName pivotAssoc,
            Map<NodeRef, Map<QName, Serializable>> wUsedAssocCache) {

        for (NodeRef entityNodeRef : searchResults) {
            QName entityType = nodeService.getType(entityNodeRef);
            if (mainType.equals(entityType) || entityDictionaryService.isSubClass(entityType, mainType)) {
                Serializable key = extractKey(entityNodeRef, keyColumn);

                final MultiLevelListData listData;
                if (pivotAssoc == null) {
                    DataListFilter dataListFilter = new DataListFilter();
                    dataListFilter.setDataType(itemType);
                    Map<String, String> criteriaMap = new HashMap<>();
                    criteriaMap.put(DataListFilter.PROP_DEPTH_LEVEL,
                            depthLevelNum == DEPTH_UNLIMITED ? TOKEN_ALL : String.valueOf(depthLevelNum));
                    dataListFilter.setCriteriaMap(criteriaMap);
                    dataListFilter.setEntityNodeRefs(Collections.singletonList(entityNodeRef));
                    listData = multiLevelDataListService.getMultiLevelListData(dataListFilter);
                } else {
                    listData = wUsedListService.getWUsedEntity(entityNodeRef, pivotAssoc, depthLevelNum);
                }

                Map<String, Object> entityItems = getEntityProperties(entityNodeRef, mainType, metadataFields, cache);
                entityItems.putAll(getDynamicProperties(entityNodeRef, itemType));

                rownum = appendNextLevel(listData, sheet, itemType, metadataFields, cache, rownum, key, null, parameters, entityItems,
                        new HashMap<>(), excelCellStyles, mainType, wUsedAssocCache);
            }
        }

        return rownum;
    }

    private Serializable extractKey(NodeRef entityNodeRef, AttributeExtractorStructure keyColumn) {
        Serializable key = keyColumn != null ? nodeService.getProperty(entityNodeRef, keyColumn.getFieldDef().getName()) : null;
        if (key == null) {
            key = nodeService.getProperty(entityNodeRef, BeCPGModel.PROP_CODE);
        }
        if (key == null) {
            key = nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);
        }
        return key;
    }

    private int fillPackagingSheet(XSSFSheet sheet, NodeRef entityNodeRef, ProductData productData, int rownum, Serializable key,
            List<AttributeExtractorStructure> metadataFields, Map<NodeRef, Map<String, Object>> cache, Map<String, Object> entityItems,
            ExcelCellStyles excelCellStyles, String filter, int depthLevelNum, boolean isOnlyLevel, String parameter, boolean includeEmpty) {

        // Check if we should export level 1 (MaxLevel filtering)
        if (depthLevelNum != DEPTH_UNLIMITED && 1 > depthLevelNum) {
            return rownum;
        }
        
        // Check if we should export level 1 (OnlyLevel filtering)
        if (isOnlyLevel && !parameter.equals(ONLY_LEVEL_PREFIX + "1")) {
            return rownum;
        }

        // Export packaging list of the main product
        if (productData.hasPackagingListEl(new EffectiveFilters<>(filter))) {
            for (PackagingListDataItem dataItem : productData.getPackagingList(new EffectiveFilters<>(filter))) {
                        rownum = fillPackagingRow(sheet, entityNodeRef, new CurrentLevelQuantities(alfrescoRepository, productData, dataItem), dataItem,
                                metadataFields, cache, rownum, key, entityItems, excelCellStyles, 1, false, false, depthLevelNum, isOnlyLevel, parameter);
            }
        } else if (includeEmpty) {
            // Create empty row when includeEmpty is true and list is empty
            rownum = createEmptyEntityRow(sheet, entityNodeRef, rownum, key, metadataFields, entityItems, excelCellStyles, 1);
        }

        // Export packaging from semi-finished products in composition
        if (productData.hasCompoListEl(new EffectiveFilters<>(filter))) {
            for (CompoListDataItem compoItem : productData.getCompoList(new EffectiveFilters<>(filter))) {
                if ((compoItem.getProduct() != null) && nodeService.exists(compoItem.getProduct())) {
                    QName compoType = nodeService.getType(compoItem.getProduct());
                    if (PLMModel.TYPE_SEMIFINISHEDPRODUCT.equals(compoType) || PLMModel.TYPE_FINISHEDPRODUCT.equals(compoType)) {
                        rownum = loadPackagingListItemForCompo(sheet, entityNodeRef, rownum, key,
                                new CurrentLevelQuantities(alfrescoRepository, packagingHelper, productData, compoItem), metadataFields, cache,
                                entityItems, excelCellStyles, 1,
                                (productData.getDropPackagingOfComponents() != null) && productData.getDropPackagingOfComponents(),
                                depthLevelNum, isOnlyLevel, parameter);
                    }
                }
            }
        }

        return rownum;
    }

    private int fillCompositionSheet(XSSFSheet sheet, NodeRef entityNodeRef, ProductData productData, int rownum, Serializable key,
            List<AttributeExtractorStructure> metadataFields, Map<NodeRef, Map<String, Object>> cache, Map<String, Object> entityItems,
            ExcelCellStyles excelCellStyles, String filter, int depthLevelNum, boolean isOnlyLevel, String parameter, boolean includeEmpty) {

        // Check if we should export level 1 (MaxLevel filtering)
        if (depthLevelNum != DEPTH_UNLIMITED && 1 > depthLevelNum) {
            return rownum;
        }
        
        // Check if we should export level 1 (OnlyLevel filtering)
        if (isOnlyLevel && !parameter.equals(ONLY_LEVEL_PREFIX + "1")) {
            return rownum;
        }

        if (productData.hasCompoListEl(new EffectiveFilters<>(filter))) {
            for (CompoListDataItem dataItem : productData.getCompoList(new EffectiveFilters<>(filter))) {
                if ((dataItem.getProduct() != null) && nodeService.exists(dataItem.getProduct())) {
                    rownum = loadCompoListItem(sheet, entityNodeRef, rownum, key,
                            new CurrentLevelQuantities(alfrescoRepository, packagingHelper, productData, dataItem), metadataFields, cache,
                            entityItems, excelCellStyles, 1, depthLevelNum, isOnlyLevel, parameter);
                }
            }
        } else if (includeEmpty) {
            // Create empty row when includeEmpty is true and list is empty
            rownum = createEmptyEntityRow(sheet, entityNodeRef, rownum, key, metadataFields, entityItems, excelCellStyles, 1);
        }

        return rownum;
    }

    private int fillProcessSheet(XSSFSheet sheet, NodeRef entityNodeRef, ProductData productData, int rownum, Serializable key,
            List<AttributeExtractorStructure> metadataFields, Map<NodeRef, Map<String, Object>> cache, Map<String, Object> entityItems,
            ExcelCellStyles excelCellStyles, String filter, int depthLevelNum, boolean isOnlyLevel, String parameter, boolean includeEmpty) {

        // Check if we should export level 1 (MaxLevel filtering)
        if (depthLevelNum != DEPTH_UNLIMITED && 1 > depthLevelNum) {
            return rownum;
        }
        
        // Check if we should export level 1 (OnlyLevel filtering)
        if (isOnlyLevel && !parameter.equals(ONLY_LEVEL_PREFIX + "1")) {
            return rownum;
        }

        if (productData.hasProcessListEl(new EffectiveFilters<>(filter))) {
            for (ProcessListDataItem dataItem : productData.getProcessList(new EffectiveFilters<>(filter))) {
                rownum = loadProcessListItem(sheet, entityNodeRef, rownum, key,
                        new CurrentLevelQuantities(nodeService, alfrescoRepository, productData, dataItem), dataItem, metadataFields, cache,
                        entityItems, excelCellStyles, 1, depthLevelNum, isOnlyLevel, parameter);
            }
        } else if (includeEmpty) {
            // Create empty row when includeEmpty is true and list is empty
            rownum = createEmptyEntityRow(sheet, entityNodeRef, rownum, key, metadataFields, entityItems, excelCellStyles, 1);
        }

        // Export process from semi-finished products in composition
        if (productData.hasCompoListEl(new EffectiveFilters<>(filter))) {
            for (CompoListDataItem compoItem : productData.getCompoList(new EffectiveFilters<>(filter))) {
                if ((compoItem.getProduct() != null) && nodeService.exists(compoItem.getProduct())) {
                    QName compoType = nodeService.getType(compoItem.getProduct());
                    if (PLMModel.TYPE_SEMIFINISHEDPRODUCT.equals(compoType) || PLMModel.TYPE_FINISHEDPRODUCT.equals(compoType)) {
                        rownum = loadProcessListItemForCompo(sheet, entityNodeRef, rownum, key,
                                new CurrentLevelQuantities(alfrescoRepository, packagingHelper, productData, compoItem), metadataFields, cache,
                                entityItems, excelCellStyles, 1, depthLevelNum, isOnlyLevel, parameter);
                    }
                }
            }
        }

        return rownum;
    }

    private int loadPackagingListItemForCompo(XSSFSheet sheet, NodeRef entityNodeRef, int rownum, Serializable key,
            CurrentLevelQuantities currentLevelQuantities, List<AttributeExtractorStructure> metadataFields,
            Map<NodeRef, Map<String, Object>> cache, Map<String, Object> entityItems, ExcelCellStyles excelCellStyles, int level,
            boolean dropPackagingOfComponents, int depthLevelNum, boolean isOnlyLevel, String parameter) {

        // Check if we should export this level (MaxLevel filtering)
        if (depthLevelNum != DEPTH_UNLIMITED && level > depthLevelNum) {
            return rownum;
        }
        
        // Check if we should export this level (OnlyLevel filtering)
        if (isOnlyLevel && !parameter.equals(ONLY_LEVEL_PREFIX + level)) {
            return rownum;
        }

        if (level > MAX_RECURSION_DEPTH) {
            return rownum;
        }

        ProductData componentProductData = currentLevelQuantities.getComponentProductData();

        if (componentProductData.hasPackagingListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {

            // Create a row for the component itself
            Map<String, Object> compoItem = new HashMap<>(entityItems);
            compoItem.put(KEY_PACKAGING_LOSS_PERC, currentLevelQuantities.getLossRatio());
            compoItem.put(KEY_PACKAGING_QTY,
                    currentLevelQuantities.getCompoListItem().getQtySubFormula() != null
                            ? currentLevelQuantities.getCompoListItem().getQtySubFormula()
                            : "");
            compoItem.put(KEY_PACKAGING_QTY_FOR_PRODUCT, currentLevelQuantities.getQtyForProduct());
            compoItem.put(KEY_QTY_FOR_COST, currentLevelQuantities.getQtyForCost());
            compoItem.put(KEY_DEPTH_LEVEL, level);

            Row row = sheet.createRow(rownum++);
            int cellNum = 0;
            Cell cell = row.createCell(cellNum++);
            cell.setCellValue(HEADER_VALUES);

            if (key != null) {
                cell = row.createCell(cellNum++);
                cell.setCellValue(String.valueOf(key));
            }

            ExcelHelper.appendExcelField(metadataFields, null, compoItem, sheet, row, cellNum, rownum, null, excelCellStyles);

            // Export packaging items of the component
            for (PackagingListDataItem packagingListDataItem : componentProductData
                    .getPackagingList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
                rownum = fillPackagingRow(sheet, entityNodeRef,
                        new CurrentLevelQuantities(alfrescoRepository, packagingListDataItem, currentLevelQuantities), packagingListDataItem,
                        metadataFields, cache, rownum, key, entityItems, excelCellStyles, level + 1, dropPackagingOfComponents, true, depthLevelNum, isOnlyLevel, parameter);
            }
        }

        // Recurse into sub-components
        if (componentProductData.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
            for (CompoListDataItem subDataItem : componentProductData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
                if ((subDataItem.getProduct() != null) && nodeService.exists(subDataItem.getProduct())) {
                    QName subType = nodeService.getType(subDataItem.getProduct());
                    if (PLMModel.TYPE_SEMIFINISHEDPRODUCT.equals(subType) || PLMModel.TYPE_FINISHEDPRODUCT.equals(subType)) {
                        rownum = loadPackagingListItemForCompo(sheet, entityNodeRef, rownum, key,
                                new CurrentLevelQuantities(alfrescoRepository, packagingHelper, subDataItem, currentLevelQuantities),
                                metadataFields, cache, entityItems, excelCellStyles, level + 1,
                                dropPackagingOfComponents
                                        || ((componentProductData.getDropPackagingOfComponents() != null)
                                                && componentProductData.getDropPackagingOfComponents()),
                                depthLevelNum, isOnlyLevel, parameter);
                    }
                }
            }
        }

        return rownum;
    }

    private int fillPackagingRow(XSSFSheet sheet, NodeRef entityNodeRef, CurrentLevelQuantities currentLevelQuantities,
            PackagingListDataItem dataItem, List<AttributeExtractorStructure> metadataFields, Map<NodeRef, Map<String, Object>> cache,
            int rownum, Serializable key, Map<String, Object> entityItems, ExcelCellStyles excelCellStyles, int level, boolean dropPackagingOfComponents, boolean isPackagingOfComponent, int depthLevelNum, boolean isOnlyLevel, String parameter) {

        // Check if we should export this level (MaxLevel filtering)
        if (depthLevelNum != DEPTH_UNLIMITED && level > depthLevelNum) {
            return rownum;
        }
        
        // Check if we should export this level (OnlyLevel filtering)
        if (isOnlyLevel && !parameter.equals(ONLY_LEVEL_PREFIX + level)) {
            return rownum;
        }

        if (permissionService.hasPermission(dataItem.getNodeRef(), PERMISSION_READ) == AccessStatus.ALLOWED) {

            Map<QName, Serializable> properties = nodeService.getProperties(dataItem.getNodeRef());
            Map<String, Object> item = doExtract(dataItem.getNodeRef(), PLMModel.TYPE_PACKAGINGLIST, metadataFields, properties, cache);

            if (entityItems != null) {
                item.putAll(entityItems);
            }

            // Add calculated quantities
            item.put(KEY_PACKAGING_QTY_FOR_PRODUCT, currentLevelQuantities.getQtyForProduct());
            item.put(KEY_QTY_FOR_COST, currentLevelQuantities.getQtyForCost());
            item.put(KEY_DEPTH_LEVEL, level);

            PackagingLevel packLevel = dataItem.getPkgLevel();
            if (packLevel == null) {
                packLevel = PackagingLevel.Primary;
            }

            item.put("prop_" + PLMModel.PROP_PRODUCT_DROP_PACKAGING_OF_COMPONENTS.getLocalName(),
                    (!packLevel.equals(PackagingLevel.Primary) && isPackagingOfComponent) || dropPackagingOfComponents);

            // Apply formulas
            for (AttributeExtractorStructure metadataField : metadataFields) {
                if (metadataField.isFormulaField()) {
                    if (metadataField.getFieldName().startsWith("formula") || metadataField.getFieldName().startsWith("image")) {
                        item.put(metadataField.getFieldName(), eval(entityNodeRef, dataItem.getNodeRef(), metadataField.getFormula(), item));
                    } else {
                        item.put(metadataField.getFieldName(), metadataField.getFormula());
                    }
                }
            }

            Row row = sheet.createRow(rownum++);

            int cellNum = 0;
            Cell cell = row.createCell(cellNum++);
            cell.setCellValue(HEADER_VALUES);

            if (key != null) {
                cell = row.createCell(cellNum++);
                cell.setCellValue(String.valueOf(key));
            }

            ExcelHelper.appendExcelField(metadataFields, null, item, sheet, row, cellNum, rownum, null, excelCellStyles);

            // Handle packaging kits recursively
            if (PLMModel.TYPE_PACKAGINGKIT.equals(nodeService.getType(dataItem.getProduct()))) {
                ProductData packagingKitData = (ProductData) alfrescoRepository.findOne(dataItem.getProduct());
                if (packagingKitData.hasPackagingListEl()) {
                    for (PackagingListDataItem p : packagingKitData.getPackagingList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
                        rownum = fillPackagingRow(sheet, entityNodeRef,
                                new CurrentLevelQuantities(alfrescoRepository, p, currentLevelQuantities), p, metadataFields, cache, rownum,
                                key, entityItems, excelCellStyles, level + 1, dropPackagingOfComponents, isPackagingOfComponent, depthLevelNum, isOnlyLevel, parameter);
                    }
                }
            }
        }

        return rownum;
    }

    private int loadCompoListItem(XSSFSheet sheet, NodeRef entityNodeRef, int rownum, Serializable key,
            CurrentLevelQuantities currentLevelQuantities, List<AttributeExtractorStructure> metadataFields,
            Map<NodeRef, Map<String, Object>> cache, Map<String, Object> entityItems, ExcelCellStyles excelCellStyles, int level, int depthLevelNum, boolean isOnlyLevel, String parameter) {

        // Check if we should export this level (MaxLevel filtering)
        if (depthLevelNum != DEPTH_UNLIMITED && level > depthLevelNum) {
            return rownum;
        }
        
        // Check if we should export this level (OnlyLevel filtering)
        if (isOnlyLevel && !parameter.equals(ONLY_LEVEL_PREFIX + level)) {
            return rownum;
        }

        if (level > MAX_RECURSION_DEPTH) {
            return rownum;
        }

        if (permissionService.hasPermission(currentLevelQuantities.getCompoListItem().getNodeRef(), PERMISSION_READ) == AccessStatus.ALLOWED) {

            Map<QName, Serializable> properties = nodeService.getProperties(currentLevelQuantities.getCompoListItem().getNodeRef());
            Map<String, Object> item = doExtract(currentLevelQuantities.getCompoListItem().getNodeRef(), PLMModel.TYPE_COMPOLIST,
                    metadataFields, properties, cache);

            if (entityItems != null) {
                item.putAll(entityItems);
            }

            // Add calculated quantities
            item.put(KEY_COMPO_QTY_FOR_PRODUCT, currentLevelQuantities.getQtyForProduct());
            item.put(KEY_QTY_FOR_COST, currentLevelQuantities.getQtyForCost());
            item.put(KEY_DEPTH_LEVEL, level);
            item.put(KEY_COMPO_LOSS_PERC, currentLevelQuantities.getLossRatio());

            // Apply formulas
            for (AttributeExtractorStructure metadataField : metadataFields) {
                if (metadataField.isFormulaField()) {
                    if (metadataField.getFieldName().startsWith("formula") || metadataField.getFieldName().startsWith("image")) {
                        item.put(metadataField.getFieldName(),
                                eval(entityNodeRef, currentLevelQuantities.getCompoListItem().getNodeRef(), metadataField.getFormula(), item));
                    } else {
                        item.put(metadataField.getFieldName(), metadataField.getFormula());
                    }
                }
            }

            Row row = sheet.createRow(rownum++);

            int cellNum = 0;
            Cell cell = row.createCell(cellNum++);
            cell.setCellValue(HEADER_VALUES);

            if (key != null) {
                cell = row.createCell(cellNum++);
                cell.setCellValue(String.valueOf(key));
            }

            ExcelHelper.appendExcelField(metadataFields, null, item, sheet, row, cellNum, rownum, null, excelCellStyles);

            // Recurse into sub-components
            ProductData componentProductData = currentLevelQuantities.getComponentProductData();
            if ((PLMModel.TYPE_SEMIFINISHEDPRODUCT.equals(nodeService.getType(currentLevelQuantities.getCompoListItem().getProduct()))
                    || PLMModel.TYPE_FINISHEDPRODUCT.equals(nodeService.getType(currentLevelQuantities.getCompoListItem().getProduct())))
                    && componentProductData.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {

                for (CompoListDataItem subDataItem : componentProductData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
                    if (subDataItem.getProduct() != null) {
                        rownum = loadCompoListItem(sheet, entityNodeRef, rownum, key,
                                new CurrentLevelQuantities(alfrescoRepository, packagingHelper, subDataItem, currentLevelQuantities),
                                metadataFields, cache, entityItems, excelCellStyles, level + 1, depthLevelNum, isOnlyLevel, parameter);
                    }
                }
            }
        }

        return rownum;
    }

    private int loadProcessListItem(XSSFSheet sheet, NodeRef entityNodeRef, int rownum, Serializable key,
            CurrentLevelQuantities currentLevelQuantities, ProcessListDataItem dataItem, List<AttributeExtractorStructure> metadataFields,
            Map<NodeRef, Map<String, Object>> cache, Map<String, Object> entityItems, ExcelCellStyles excelCellStyles, int level, int depthLevelNum, boolean isOnlyLevel, String parameter) {

        // Check if we should export this level (MaxLevel filtering)
        if (depthLevelNum != DEPTH_UNLIMITED && level > depthLevelNum) {
            return rownum;
        }
        
        // Check if we should export this level (OnlyLevel filtering)
        if (isOnlyLevel && !parameter.equals(ONLY_LEVEL_PREFIX + level)) {
            return rownum;
        }

        if (level > MAX_RECURSION_DEPTH) {
            return rownum;
        }

        if (permissionService.hasPermission(dataItem.getNodeRef(), PERMISSION_READ) == AccessStatus.ALLOWED) {

            Map<QName, Serializable> properties = nodeService.getProperties(dataItem.getNodeRef());
            Map<String, Object> item = doExtract(dataItem.getNodeRef(), MPMModel.TYPE_PROCESSLIST, metadataFields, properties, cache);

            if (entityItems != null) {
                item.putAll(entityItems);
            }

            // Add calculated quantities
            item.put(KEY_PROCESS_QTY_FOR_PRODUCT, currentLevelQuantities.getQtyForProduct());
            item.put(KEY_QTY_FOR_COST, currentLevelQuantities.getQtyForCost());
            item.put(KEY_DEPTH_LEVEL, level);

            // Apply formulas
            for (AttributeExtractorStructure metadataField : metadataFields) {
                if (metadataField.isFormulaField()) {
                    if (metadataField.getFieldName().startsWith("formula") || metadataField.getFieldName().startsWith("image")) {
                        item.put(metadataField.getFieldName(), eval(entityNodeRef, dataItem.getNodeRef(), metadataField.getFormula(), item));
                    } else {
                        item.put(metadataField.getFieldName(), metadataField.getFormula());
                    }
                }
            }

            Row row = sheet.createRow(rownum++);

            int cellNum = 0;
            Cell cell = row.createCell(cellNum++);
            cell.setCellValue("VALUES");

            if (key != null) {
                cell = row.createCell(cellNum++);
                cell.setCellValue(String.valueOf(key));
            }

            ExcelHelper.appendExcelField(metadataFields, null, item, sheet, row, cellNum, rownum, null, excelCellStyles);

            // Recurse into sub-processes
            if (currentLevelQuantities.getComponentProductData() != null) {
                ProductData componentProductData = currentLevelQuantities.getComponentProductData();
                if (componentProductData.hasProcessListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
                    for (ProcessListDataItem subDataItem : componentProductData
                            .getProcessList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
                        rownum = loadProcessListItem(sheet, entityNodeRef, rownum, key,
                                new CurrentLevelQuantities(nodeService, alfrescoRepository, subDataItem, currentLevelQuantities), subDataItem,
                                metadataFields, cache, entityItems, excelCellStyles, level + 1, depthLevelNum, isOnlyLevel, parameter);
                    }
                }
            }
        }

        return rownum;
    }

    private int loadProcessListItemForCompo(XSSFSheet sheet, NodeRef entityNodeRef, int rownum, Serializable key,
            CurrentLevelQuantities currentLevelQuantities, List<AttributeExtractorStructure> metadataFields,
            Map<NodeRef, Map<String, Object>> cache, Map<String, Object> entityItems, ExcelCellStyles excelCellStyles, int level, int depthLevelNum, boolean isOnlyLevel, String parameter) {

        // Check if we should export this level (MaxLevel filtering)
        if (depthLevelNum != DEPTH_UNLIMITED && level > depthLevelNum) {
            return rownum;
        }
        
        // Check if we should export this level (OnlyLevel filtering)
        if (isOnlyLevel && !parameter.equals(ONLY_LEVEL_PREFIX + level)) {
            return rownum;
        }

        if (level > 20) {
            return rownum;
        }

        ProductData componentProductData = currentLevelQuantities.getComponentProductData();

        if (componentProductData.hasProcessListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {

            // Export process items of the component
            for (ProcessListDataItem processListDataItem : componentProductData
                    .getProcessList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
                rownum = loadProcessListItem(sheet, entityNodeRef, rownum, key,
                        new CurrentLevelQuantities(nodeService, alfrescoRepository, processListDataItem, currentLevelQuantities),
                        processListDataItem, metadataFields, cache, entityItems, excelCellStyles, level + 1, depthLevelNum, isOnlyLevel, parameter);
            }
        }

        // Recurse into sub-components
        if (componentProductData.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
            for (CompoListDataItem subDataItem : componentProductData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
                if ((subDataItem.getProduct() != null) && nodeService.exists(subDataItem.getProduct())) {
                    QName subType = nodeService.getType(subDataItem.getProduct());
                    if (PLMModel.TYPE_SEMIFINISHEDPRODUCT.equals(subType) || PLMModel.TYPE_FINISHEDPRODUCT.equals(subType)) {
                        rownum = loadProcessListItemForCompo(sheet, entityNodeRef, rownum, key,
                                new CurrentLevelQuantities(alfrescoRepository, packagingHelper, subDataItem, currentLevelQuantities),
                                metadataFields, cache, entityItems, excelCellStyles, level + 1, depthLevelNum, isOnlyLevel, parameter);
                    }
                }
            }
        }

        return rownum;
    }

    /**
     * Creates an empty row for an entity when the list is empty but includeEmpty is true.
     *
     * @param sheet the Excel sheet
     * @param entityNodeRef the entity node reference
     * @param rownum the current row number
     * @param key the entity key
     * @param metadataFields the metadata fields
     * @param entityItems the entity properties
     * @param excelCellStyles the Excel cell styles
     * @param level the depth level
     * @return the new row number
     */
    private int createEmptyEntityRow(XSSFSheet sheet, NodeRef entityNodeRef, int rownum, Serializable key,
            List<AttributeExtractorStructure> metadataFields, Map<String, Object> entityItems, ExcelCellStyles excelCellStyles, int level) {

        Row row = sheet.createRow(rownum++);

        int cellNum = 0;
        Cell cell = row.createCell(cellNum++);
        cell.setCellValue(HEADER_VALUES);

        if (key != null) {
            cell = row.createCell(cellNum++);
            cell.setCellValue(String.valueOf(key));
        }

        // Create empty item with entity properties and depth level
        Map<String, Object> emptyItem = new HashMap<>();
        if (entityItems != null) {
            emptyItem.putAll(entityItems);
        }
        emptyItem.put(KEY_DEPTH_LEVEL, level);

        ExcelHelper.appendExcelField(metadataFields, null, emptyItem, sheet, row, cellNum, rownum, null, excelCellStyles);

        return rownum;
    }

    // Method from MultiLevelExcelReportSearchPlugin for other list types
    /**
     * <p>appendNextLevel.</p>
     *
     * @param listData a {@link fr.becpg.repo.entity.datalist.data.MultiLevelListData} object
     * @param sheet a {@link org.apache.poi.xssf.usermodel.XSSFSheet} object
     * @param itemType a {@link org.alfresco.service.namespace.QName} object
     * @param metadataFields a {@link java.util.List} object
     * @param cache a {@link java.util.Map} object
     * @param rownum a int
     * @param key a {@link java.io.Serializable} object
     * @param parentQty a {@link java.lang.Double} object
     * @param parameters an array of {@link java.lang.String} objects
     * @param entityItems a {@link java.util.Map} object
     * @param dynamicCharactColumnCache a {@link java.util.Map} object
     * @param excelCellStyles a {@link fr.becpg.repo.helper.ExcelHelper.ExcelCellStyles} object
     * @param wUsedEntityType a {@link org.alfresco.service.namespace.QName} object
     * @param wUsedAssocCache a {@link java.util.Map} object
     * @return a int
     */
    protected int appendNextLevel(MultiLevelListData listData, XSSFSheet sheet, QName itemType,
            List<AttributeExtractorStructure> metadataFields, Map<NodeRef, Map<String, Object>> cache, int rownum, Serializable key,
            Double parentQty, String[] parameters, Map<String, Object> entityItems, Map<String, List<String>> dynamicCharactColumnCache,
            ExcelCellStyles excelCellStyles, QName wUsedEntityType, Map<NodeRef, Map<QName, Serializable>> wUsedAssocCache) {

        for (Entry<NodeRef, MultiLevelListData> entry : listData.getTree().entrySet()) {
            NodeRef itemNodeRef = entry.getKey();
            if (itemType.equals(nodeService.getType(itemNodeRef))) {
                if (permissionService.hasPermission(itemNodeRef, PERMISSION_READ) == AccessStatus.ALLOWED) {

                    Map<QName, Serializable> properties = nodeService.getProperties(itemNodeRef);
                    Map<String, Object> item = doExtract(itemNodeRef, itemType, metadataFields, properties, cache);

                    for (Entry<String, Object> itemEntry : item.entrySet()) {
                        String itemKey = itemEntry.getKey();
                        Object itemValue = itemEntry.getValue();
                        if (itemKey.startsWith(PREFIX_DYNAMIC_CHAR_COLUMN)) {
                            if (JsonFormulaHelper.isJsonString(itemValue)) {
                                if (dynamicCharactColumnCache.get(itemKey) == null) {
                                    dynamicCharactColumnCache.put(itemKey, new ArrayList<>());
                                }
                                dynamicCharactColumnCache.get(itemKey).add((String) itemValue);
                                Object value = JsonFormulaHelper.cleanCompareJSON((String) itemValue);
                                item.put(itemKey, value);
                            } else if (dynamicCharactColumnCache.get(itemKey) != null) {
                                for (String subValues : dynamicCharactColumnCache.get(itemKey)) {
                                    Object subValue = JsonFormulaHelper.extractComponentValue(subValues, itemNodeRef.getId());
                                    if (subValue != null) {
                                        item.put(itemKey, subValue);
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if (entityItems != null) {
                        item.putAll(entityItems);
                    }

                    item.put(KEY_DEPTH_LEVEL, entry.getValue().getDepth());
                    item.put(KEY_PARENT, nodeService.getProperty(listData.getEntityNodeRef(), BeCPGModel.PROP_CODE));

                    if (wUsedAssocCache != null) {
                        item.putAll(wUsedAssocCache
                                .computeIfAbsent(itemNodeRef, unused -> nodeService.getProperties(entityListDAO.getEntity(itemNodeRef)))
                                .entrySet().stream().filter(property -> property.getValue() != null)
                                .collect(Collectors.toMap(
                                        property -> PREFIX_WUSED_ENTITY
                                                + property.getKey().toPrefixString(namespaceService).replaceFirst(":", "_"),
                                        Entry::getValue)));
                    }

                    String parameter = (parameters != null) && (parameters.length > 0) ? parameters[0] : null;

                    if ((parameter == null) || !parameter.contains(ONLY_LEVEL_PREFIX)
                            || parameter.equals(ONLY_LEVEL_PREFIX + entry.getValue().getDepth())) {
                        Row row = sheet.createRow(rownum++);

                        int cellNum = 0;
                        Cell cell = row.createCell(cellNum++);
                        cell.setCellValue(HEADER_VALUES);

                        if (key != null) {
                            cell = row.createCell(cellNum++);
                            cell.setCellValue(String.valueOf(key));
                        }

                        ExcelHelper.appendExcelField(metadataFields, null, item, sheet, row, cellNum, rownum, null, excelCellStyles);
                    }

                    rownum = appendNextLevel(entry.getValue(), sheet, itemType, metadataFields, cache, rownum, key, parentQty, parameters,
                            entityItems, dynamicCharactColumnCache, excelCellStyles, wUsedEntityType, wUsedAssocCache);
                }
            }
        }
        return rownum;
    }
}
