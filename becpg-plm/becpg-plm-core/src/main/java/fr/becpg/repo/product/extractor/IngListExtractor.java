package fr.becpg.repo.product.extractor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.config.format.FormatMode;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.impl.MultiLevelExtractor;
import fr.becpg.repo.helper.impl.AttributeExtractorField;

/**
 * <p>
 * IngListExtractor class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class IngListExtractor extends MultiLevelExtractor {

	private static final String ING_LIST = "ingList";

	private static final String LABEL_ASSOC_KEY = "assoc_bcpg_ingListIng";

	private static final String TOTAL_KEY = "prop_bcpg_ingListQtyPerc";
	private static final String TOTAL_WITHYIELD_KEY = "prop_bcpg_ingListQtyPercWithYield";
	private static final String TOTAL_WITHYIELDSECONDARY_KEY = "prop_bcpg_ingListQtyPercWithSecondaryYield";

	private static final String TYPE_DOUBLE = "double";
	private static final String TYPE_TOTAL = "total";

	/** {@inheritDoc} */
	@Override
	public PaginatedExtractedItems extract(DataListFilter dataListFilter, List<AttributeExtractorField> metadataFields) {

		PaginatedExtractedItems ret;

		if (dataListFilter.isAllFilter()) {
			ret = super.extract(dataListFilter, metadataFields);
		} else {
			ret = super.simpleExtract(dataListFilter, metadataFields);
		}

		if (!(RepoConsts.FORMAT_CSV.equals(dataListFilter.getFormat()) || RepoConsts.FORMAT_XLSX.equals(dataListFilter.getFormat()))) {

			Map<String, Object> totalRow = new HashMap<>(20);

			totalRow.put(PROP_TYPE, TYPE_TOTAL);
			totalRow.put(PROP_LEAF, true);

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
			Double totalQtyPercWithYield = 0d;
			Double totalQtyPercWithSecondaryYield = 0d;

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

						Double secondaryYieldValue = (Double) nodeService.getProperty(nodeRef, PLMModel.PROP_INGLIST_QTY_PERCWITHSECONDARYYIELD);
						if (secondaryYieldValue != null) {
							totalQtyPercWithSecondaryYield += secondaryYieldValue;
						}
					}
				}
			}

			String totalHeader = I18NUtil.getMessage("entity.datalist.item.details.total");

			totalNodeDataRow.put(LABEL_ASSOC_KEY, Arrays.asList(createColumn(TYPE_TOTAL, totalHeader, totalHeader)));

			totalNodeDataRow.put(TOTAL_KEY, createColumn(TYPE_DOUBLE,
					attributeExtractorService.getPropertyFormats(FormatMode.JSON, false).formatDecimal(totalQtyPerc), totalQtyPerc));

			totalNodeDataRow.put(TOTAL_WITHYIELD_KEY,
					createColumn(TYPE_DOUBLE,
							attributeExtractorService.getPropertyFormats(FormatMode.JSON, false).formatDecimal(totalQtyPercWithYield),
							totalQtyPercWithYield));

			totalNodeDataRow.put(TOTAL_WITHYIELDSECONDARY_KEY,
					createColumn(TYPE_DOUBLE,
							attributeExtractorService.getPropertyFormats(FormatMode.JSON, false).formatDecimal(totalQtyPercWithSecondaryYield),
							totalQtyPercWithSecondaryYield));

			totalRow.put(PROP_NODEDATA, totalNodeDataRow);

			ret.addItem(totalRow);

		}

		return ret;

	}

	private Object createColumn(String metadata, String displayValue, Object value) {
		HashMap<String, Object> tmp = new HashMap<>(3);
		tmp.put("metadata", metadata);
		tmp.put("displayValue", displayValue);
		tmp.put("value", value);

		return tmp;
	}

	/** {@inheritDoc} */
	@Override
	public boolean applyTo(DataListFilter dataListFilter) {
		return !dataListFilter.isSimpleItem() && (dataListFilter.getDataListName() != null) && dataListFilter.getDataListName().equals(ING_LIST);
	}

}
