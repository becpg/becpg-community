package fr.becpg.repo.entity.datalist.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.helper.ExcelHelper.ExcelFieldTitleProvider;

/**
 * <p>ExcelDataListOutputPlugin interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface ExcelDataListOutputPlugin {

	/**
	 * <p>isDefault.</p>
	 *
	 * @return a boolean.
	 */
	boolean isDefault();

	/**
	 * <p>applyTo.</p>
	 *
	 * @param dataListFilter a {@link fr.becpg.repo.entity.datalist.data.DataListFilter} object.
	 * @return a boolean.
	 */
	boolean applyTo(DataListFilter dataListFilter);
	
    /**
     * <p>getExcelFieldTitleProvider.</p>
     *
     * @param dataListFilter a {@link fr.becpg.repo.entity.datalist.data.DataListFilter} object.
     * @return a {@link fr.becpg.repo.helper.ExcelHelper.ExcelFieldTitleProvider} object.
     */
    ExcelFieldTitleProvider getExcelFieldTitleProvider(DataListFilter dataListFilter);

	/**
	 * <p>decorate.</p>
	 *
	 * @param items a {@link java.util.List} object.
	 * @return a {@link java.util.List} object.
	 * @throws java.io.IOException if any.
	 */
	List<Map<String, Object>> decorate(List<Map<String, Object>> items) throws IOException;
	
	/**
	 * <p>extractExtrasSheet.</p>
	 *
	 * @param dataListFilter a {@link fr.becpg.repo.entity.datalist.data.DataListFilter} object.
	 * @return a {@link fr.becpg.repo.entity.datalist.PaginatedExtractedItems} object.
	 */
	PaginatedExtractedItems extractExtrasSheet(DataListFilter dataListFilter);


}
