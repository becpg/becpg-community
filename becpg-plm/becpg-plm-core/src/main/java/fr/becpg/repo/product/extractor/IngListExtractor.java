package fr.becpg.repo.product.extractor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.impl.SimpleExtractor;
import fr.becpg.repo.helper.AttributeExtractorService.AttributeExtractorMode;

public class IngListExtractor extends SimpleExtractor {

	private static final String ING_LIST = "ingList";

	private static final String LABEL_ASSOC_KEY = "assoc_bcpg_ingListIng";

	private static final String TOTAL_KEY = "prop_bcpg_ingListQtyPerc";

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
			userAccess.put("wused", false);
			userAccess.put("details", false);

			totalRow.put(PROP_PERMISSIONS, permissions);

			Map<String, Object> totalNodeDataRow = new HashMap<>(20);

			Double totalQtyPerc = computeTotal(ret, TOTAL_KEY);

			HashMap<String, Object> tmp = new HashMap<>(3);

			String totalHeader = I18NUtil.getMessage("entity.datalist.item.details.total");

			tmp.put("metadata", "total");
			tmp.put("displayValue", totalHeader);
			tmp.put("value", totalHeader);

			totalNodeDataRow.put(LABEL_ASSOC_KEY, Arrays.asList(tmp));

			tmp = new HashMap<>(3);
			tmp.put("metadata", "double");
			tmp.put("displayValue", attributeExtractorService.getPropertyFormats(AttributeExtractorMode.JSON).formatDecimal(totalQtyPerc));
			tmp.put("value", totalQtyPerc);

			totalNodeDataRow.put(TOTAL_KEY, tmp);

			totalRow.put(PROP_NODEDATA, totalNodeDataRow);

			ret.addItem(totalRow);

		}

		return ret;

	}

	private Double computeTotal(PaginatedExtractedItems ret, String totalKey) {
		Double total = 0d;

		for (Map<String, Object> row : ret.getPageItems()) {
			NodeRef nodeRef = (NodeRef) row.get(PROP_NODE);
			if (nodeRef != null) {
				Integer depthLevel = (Integer) nodeService.getProperty(nodeRef, BeCPGModel.PROP_DEPTH_LEVEL);
				if ((depthLevel == null) || (depthLevel == 1)) {
					Double value = (Double) nodeService.getProperty(nodeRef, PLMModel.PROP_INGLIST_QTY_PERC);
					if (value != null) {
						total += value;
					}

				}
			}
		}

		return total;
	}

	@Override
	public boolean applyTo(DataListFilter dataListFilter) {
		return (dataListFilter.getDataListName() != null) && dataListFilter.getDataListName().equals(ING_LIST);
	}

}
