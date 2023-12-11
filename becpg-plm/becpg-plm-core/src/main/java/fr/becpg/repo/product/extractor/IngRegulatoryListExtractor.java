package fr.becpg.repo.product.extractor;

import fr.becpg.repo.entity.datalist.data.DataListFilter;

/**
 * <p>
 * IngListExtractor class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class IngRegulatoryListExtractor extends SimpleCharactListExtractor {

	private static final String ING_REGULATORY_LIST = "ingRegulatoryList";

	
	@Override
	public boolean hasWriteAccess() {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean applyTo(DataListFilter dataListFilter) {
		return (dataListFilter.getDataListName() != null) && dataListFilter.getDataListName().equals(ING_REGULATORY_LIST);
	}

}
