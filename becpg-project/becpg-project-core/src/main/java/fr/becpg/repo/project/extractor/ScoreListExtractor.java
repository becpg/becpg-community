package fr.becpg.repo.project.extractor;

import java.util.List;

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
public class ScoreListExtractor extends MultiLevelExtractor {


	private static final String SCORE_LIST = "scoreList";

	/** {@inheritDoc} */
	@Override
	public PaginatedExtractedItems extract(DataListFilter dataListFilter, List<AttributeExtractorField> metadataFields) {

		if (dataListFilter.isAllFilter()) {
			return super.extract(dataListFilter, metadataFields);
		} else {
			return super.simpleExtract(dataListFilter, metadataFields);
		}

	
	}

	/** {@inheritDoc} */
	@Override
	public boolean applyTo(DataListFilter dataListFilter) {
		return !dataListFilter.isSimpleItem() && (dataListFilter.getDataListName() != null) && dataListFilter.getDataListName().equals(SCORE_LIST);
	}

}
