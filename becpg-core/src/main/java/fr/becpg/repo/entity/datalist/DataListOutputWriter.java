package fr.becpg.repo.entity.datalist;

import java.io.IOException;

import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.entity.datalist.data.DataListFilter;

public interface DataListOutputWriter {

	void write(WebScriptResponse res, DataListFilter dataListFilter,  PaginatedExtractedItems extractedItems) throws IOException;

}
