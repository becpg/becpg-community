package fr.becpg.repo.importer;

import java.io.IOException;
import java.util.List;

import org.alfresco.service.cmr.repository.ContentWriter;

import fr.becpg.config.mapping.AbstractAttributeMapping;

/**
 * <p>ImportFileReader interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface ImportFileReader {

	/**
	 * <p>getLineAt.</p>
	 *
	 * @param index a int.
	 * @param columns a {@link java.util.List} object.
	 * @return an array of {@link java.lang.String} objects.
	 * @throws fr.becpg.repo.importer.ImporterException if any.
	 */
	String[] getLineAt(int index, List<AbstractAttributeMapping> columns) throws ImporterException;

	/**
	 * <p>getTotalLineCount.</p>
	 *
	 * @return a int.
	 */
	int getTotalLineCount();

	/**
	 * <p>reportError.</p>
	 *
	 * @param index a int.
	 * @param errorMsg a {@link java.lang.String} object.
	 * @param columnIdx a int.
	 */
	void reportError(int index, String errorMsg, int columnIdx);

	/**
	 * <p>writeErrorInFile.</p>
	 *
	 * @param writer a {@link org.alfresco.service.cmr.repository.ContentWriter} object.
	 * @throws java.io.IOException if any.
	 */
	void writeErrorInFile(ContentWriter writer) throws IOException;

	/**
	 * <p>reportSuccess.</p>
	 *
	 * @param index a int.
	 * @param columnIdx a int.
	 */
	void reportSuccess(int index, int columnIdx);

}
