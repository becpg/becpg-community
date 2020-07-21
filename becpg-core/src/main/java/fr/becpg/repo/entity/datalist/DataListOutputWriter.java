package fr.becpg.repo.entity.datalist;

import java.io.IOException;

import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.entity.datalist.data.DataListFilter;

/**
 * <p>DataListOutputWriter interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface DataListOutputWriter {

	/**
	 * <p>write.</p>
	 *
	 * @param req a {@link org.springframework.extensions.webscripts.WebScriptRequest} object.
	 * @param res a {@link org.springframework.extensions.webscripts.WebScriptResponse} object.
	 * @param dataListFilter a {@link fr.becpg.repo.entity.datalist.data.DataListFilter} object.
	 * @param extractedItems a {@link fr.becpg.repo.entity.datalist.PaginatedExtractedItems} object.
	 * @throws java.io.IOException if any.
	 */
	void write(WebScriptRequest req, WebScriptResponse res, DataListFilter dataListFilter,  PaginatedExtractedItems extractedItems) throws IOException;

}
