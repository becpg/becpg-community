package fr.becpg.repo.importer.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;

import org.alfresco.service.cmr.repository.ContentWriter;
import org.apache.commons.io.input.BOMInputStream;

import fr.becpg.common.csv.CSVReader;
import fr.becpg.config.mapping.AbstractAttributeMapping;
import fr.becpg.repo.importer.ImportFileReader;
import fr.becpg.repo.importer.ImporterException;

/**
 * <p>ImportCSVFileReader class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ImportCSVFileReader implements ImportFileReader {

	private List<String[]> lines = null;

	/**
	 * <p>Constructor for ImportCSVFileReader.</p>
	 *
	 * @param is a {@link java.io.InputStream} object.
	 * @param charset a {@link java.nio.charset.Charset} object.
	 * @param separator a char.
	 * @throws java.io.IOException if any.
	 */
	public ImportCSVFileReader(InputStream is, Charset charset, char separator) throws IOException {
		try (BOMInputStream bomInputStream = BOMInputStream.builder().setInputStream(is).get();
				CSVReader csvReader = new CSVReader(new InputStreamReader(bomInputStream, charset), separator)) {
			lines = csvReader.readAll();
		}
	}

	/**
	 * <p>Getter for the field <code>lines</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<String[]> getLines() {
		return lines;
	}

	/** {@inheritDoc} */
	@Override
	public String[] getLineAt(int importIndex, List<AbstractAttributeMapping> columns) throws ImporterException {
		String[] line = null;

		if ((lines != null) && (importIndex < lines.size())) {
			line = lines.get(importIndex);
		}

		return line;
	}

	/** {@inheritDoc} */
	@Override
	public int getTotalLineCount() {
		return lines.size();
	}

	/** {@inheritDoc} */
	@Override
	public void reportError(int index, String errorMsg, int columnIdx) {
		// DO nothing for CSV
	}

	/** {@inheritDoc} */
	@Override
	public void writeErrorInFile(ContentWriter writer) {
		// DO nothing for CSV

	}

	/** {@inheritDoc} */
	@Override
	public void reportSuccess(int index, int columnIdx) {
		// DO nothing for CSV

	}

}
