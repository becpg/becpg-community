package fr.becpg.repo.importer;

import java.io.IOException;
import java.util.List;

import org.alfresco.service.cmr.repository.ContentWriter;

import fr.becpg.config.mapping.AbstractAttributeMapping;

public interface ImportFileReader {

	String[] getLineAt(int index, List<AbstractAttributeMapping> columns);

	int getTotalLineCount();

	void reportError(int index, String errorMsg, int columnIdx);

	void writeErrorInFile(ContentWriter writer) throws IOException;

	void reportSuccess(int index, int columnIdx);

}
