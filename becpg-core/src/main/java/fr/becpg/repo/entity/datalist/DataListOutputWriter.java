package fr.becpg.repo.entity.datalist;

import java.io.IOException;

import org.springframework.extensions.webscripts.WebScriptResponse;

public interface DataListOutputWriter {

	void write(WebScriptResponse res, PaginatedExtractedItems extractedItems) throws IOException;

}
