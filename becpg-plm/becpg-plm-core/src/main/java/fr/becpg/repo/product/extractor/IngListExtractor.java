package fr.becpg.repo.product.extractor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.config.format.FormatMode;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.impl.SimpleExtractor;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;

/**
 * <p>
 * IngListExtractor class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class IngListExtractor extends SimpleExtractor {

	private static final String ING_LIST = "ingList";

	private static final String LABEL_ASSOC_KEY = "assoc_bcpg_ingListIng";

	private static final String TOTAL_KEY = "prop_bcpg_ingListQtyPerc";
	private static final String TOTAL_WITHYIELD_KEY = "prop_bcpg_ingListQtyPercWithYield";

	/** {@inheritDoc} */
	@Override
	public PaginatedExtractedItems extract(DataListFilter dataListFilter, List<String> metadataFields) {
		PaginatedExtractedItems ret = super.extract(dataListFilter, metadataFields);

		if (!(RepoConsts.FORMAT_CSV.equals(dataListFilter.getFormat()) || RepoConsts.FORMAT_XLSX.equals(dataListFilter.getFormat()))) {

			Map<String, Object> totalRow = new HashMap<>(20);

			totalRow.put(PROP_TYPE, "total");

			Map<String, Map<String, Boolean>> permissions = new HashMap<>(1);
			Map<String, Boolean> userAccess = new HashMap<>(5);

			permissions.put("userAccess", userAccess);
			userAccess.put("delete", false);
			userAccess.put("create", false);
			userAccess.put("edit", false);
			userAccess.put("sort", false);
			userAccess.put("select", false);
			userAccess.put("wused", false);
			userAccess.put("details", false);

			totalRow.put(PROP_PERMISSIONS, permissions);

			Map<String, Object> totalNodeDataRow = new HashMap<>(20);

			
			Double totalQtyPerc = 0d;
			Double totalQtyPercWithYield= 0d;

			for (Map<String, Object> row : ret.getPageItems()) {
				
				NodeRef nodeRef = new NodeRef(row.get(PROP_NODE).toString());

				if (nodeRef != null) {
					Integer depthLevel = (Integer) nodeService.getProperty(nodeRef, BeCPGModel.PROP_DEPTH_LEVEL);
					if ((depthLevel == null) || (depthLevel == 1)) {
						Double value = (Double) nodeService.getProperty(nodeRef, PLMModel.PROP_INGLIST_QTY_PERC);
						
						if (value != null) {
							totalQtyPerc += value;
						}
						Double yieldValue = (Double) nodeService.getProperty(nodeRef, PLMModel.PROP_INGLIST_QTY_PERCWITHYIELD);
						if (yieldValue != null) {
							totalQtyPercWithYield += yieldValue;
						}
					}
				}
			}
			

			HashMap<String, Object> tmp = new HashMap<>(3);

			String totalHeader = I18NUtil.getMessage("entity.datalist.item.details.total");

			tmp.put("metadata", "total");
			tmp.put("displayValue", totalHeader);
			tmp.put("value", totalHeader);

			totalNodeDataRow.put(LABEL_ASSOC_KEY, Arrays.asList(tmp));

			tmp = new HashMap<>(3);
			tmp.put("metadata", "double");
			tmp.put("displayValue", attributeExtractorService.getPropertyFormats(FormatMode.JSON, false).formatDecimal(totalQtyPerc));
			tmp.put("value", totalQtyPerc);

			totalNodeDataRow.put(TOTAL_KEY, tmp);
			

			tmp = new HashMap<>(3);
			tmp.put("metadata", "double");
			tmp.put("displayValue", attributeExtractorService.getPropertyFormats(FormatMode.JSON, false).formatDecimal(totalQtyPercWithYield));
			tmp.put("value", totalQtyPercWithYield);

			totalNodeDataRow.put(TOTAL_WITHYIELD_KEY, tmp);

			totalRow.put(PROP_NODEDATA, totalNodeDataRow);

			ret.addItem(totalRow);

		}

		return ret;

	}


	/** {@inheritDoc} */
	@Override
	protected Map<String, Object> doExtract(NodeRef nodeRef, QName itemType, List<AttributeExtractorStructure> metadataFields,  FormatMode mode,
			Map<QName, Serializable> properties, final Map<String, Object> props, final Map<NodeRef, Map<String, Object>> cache) {

		return attributeExtractorService.extractNodeData(nodeRef, itemType, properties, metadataFields, mode,
				new AttributeExtractorService.DataListCallBack() {

					@Override
					public List<Map<String, Object>> extractNestedField(NodeRef nodeRef, AttributeExtractorStructure field, FormatMode mode) {
						List<Map<String, Object>> ret = new ArrayList<>();
						if (field.isDataListItems()) {

							if (PLMModel.TYPE_REQCTRLLIST.equals(field.getFieldQname())) {

								NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityListDAO.getEntity(nodeRef));
								NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, field.getFieldQname());

								if (listNodeRef != null) {
									List<NodeRef> results = entityListDAO.getListItems(listNodeRef, field.getFieldQname());

									for (NodeRef itemNodeRef : results) {

										NodeRef ing = associationService.getTargetAssoc(nodeRef, PLMModel.ASSOC_INGLIST_ING);
										if ((ing != null) && ing.equals(associationService.getTargetAssoc(itemNodeRef, PLMModel.ASSOC_RCL_CHARACT))
												|| associationService.getTargetAssocs(itemNodeRef, PLMModel.ASSOC_RCL_SOURCES).contains(ing)
												) {
											addExtracted(itemNodeRef, field, cache, mode, ret);
										}
									}
								}
							}

						} else if (field.isEntityField()) {
							NodeRef entityNodeRef = entityListDAO.getEntity(nodeRef);
							addExtracted(entityNodeRef, field, cache, mode, ret);

						} else {

							if (field.getFieldDef() instanceof AssociationDefinition) {
								List<NodeRef> assocRefs;
								if (((AssociationDefinition) field.getFieldDef()).isChild()) {
									assocRefs = associationService.getChildAssocs(nodeRef, field.getFieldDef().getName());
								} else {
									assocRefs = associationService.getTargetAssocs(nodeRef, field.getFieldDef().getName());
								}
								for (NodeRef itemNodeRef : assocRefs) {
									addExtracted(itemNodeRef, field, cache, mode, ret);
								}

							}
						}

						return ret;
					}

					private void addExtracted(NodeRef itemNodeRef, AttributeExtractorStructure field, Map<NodeRef, Map<String, Object>> cache,
							FormatMode mode, List<Map<String, Object>> ret) {
						if (cache.containsKey(itemNodeRef)) {
							ret.add(cache.get(itemNodeRef));
						} else {
							if (permissionService.hasPermission(itemNodeRef, "Read") == AccessStatus.ALLOWED) {
								if (FormatMode.CSV.equals(mode) || FormatMode.XLSX.equals(mode)) {
									ret.add(extractExport(mode, itemNodeRef, field.getChildrens(), props, cache));
								} else {
									ret.add(extractJSON(itemNodeRef, field.getChildrens(), props, cache));
								}
							}
						}
					}

				});
	}

	/** {@inheritDoc} */
	@Override
	public boolean applyTo(DataListFilter dataListFilter) {
		return (dataListFilter.getDataListName() != null) && dataListFilter.getDataListName().equals(ING_LIST);
	}

}
