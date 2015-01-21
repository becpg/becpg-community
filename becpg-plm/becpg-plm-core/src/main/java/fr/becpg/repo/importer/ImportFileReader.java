package fr.becpg.repo.importer;

import java.io.IOException;

import org.alfresco.service.cmr.repository.ContentWriter;

public interface ImportFileReader {

	String[] getLineAt(int index);

	int getTotalLineCount();

	void reportError(int index, String errorMsg, int columnIdx);

	void writeErrorInFile(ContentWriter writer) throws IOException;

	void reportSuccess(int index, int columnIdx);

}
