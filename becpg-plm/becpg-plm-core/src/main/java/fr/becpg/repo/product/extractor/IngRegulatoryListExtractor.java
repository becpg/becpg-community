package fr.becpg.repo.product.extractor;

import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.impl.SimpleExtractor;

/**
 * <p>
 * IngListExtractor class.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class IngRegulatoryListExtractor extends SimpleExtractor {

	private static final String ING_REGULATORY_LIST = "ingRegulatoryList";

	
	/** {@inheritDoc} */
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
