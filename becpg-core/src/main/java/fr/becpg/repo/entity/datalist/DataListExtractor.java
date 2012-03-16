package fr.becpg.repo.entity.datalist;

import java.util.List;

import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.DataListPagination;

/**
 * Used to extract datalist datas
 * @author matthieu
 *
 */
public interface DataListExtractor {

	PaginatedExtractedItems extract(DataListFilter dataListFilter, List<String> metadataFields, DataListPagination pagination, boolean hasWriteAccess);

}
