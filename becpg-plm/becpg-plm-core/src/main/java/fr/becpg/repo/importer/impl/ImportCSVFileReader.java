package fr.becpg.repo.importer.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;

import org.alfresco.service.cmr.repository.ContentWriter;
import org.apache.commons.io.input.BOMInputStream;

import fr.becpg.common.csv.CSVReader;
import fr.becpg.repo.importer.ImportFileReader;

public class ImportCSVFileReader implements ImportFileReader {


	
	private List<String[]> lines = null;


	public ImportCSVFileReader(InputStream is, Charset charset, char separator) throws IOException{
		CSVReader csvReader = new CSVReader(new InputStreamReader(new BOMInputStream(is,false), charset), separator);
		lines = csvReader.readAll();
	}



	public List<String[]> getLines() {
		return lines;
	}


	@Override
	public String[] getLineAt(int importIndex) {
		String[] line = null;

		if (lines != null && importIndex < lines.size()) {
			line = lines.get(importIndex);
		}

		return line;
	}

	@Override
	public int getTotalLineCount() {
		return lines.size();
	}


	@Override
	public void reportError(int index, String errorMsg, int columnIdx) {
		// DO nothing for CSV
	}


	@Override
	public void writeErrorInFile(ContentWriter writer) {
		// DO nothing for CSV
		
	}



	@Override
	public void reportSuccess(int index, int columnIdx) {
		// DO nothing for CSV
		
	}
	
	
}
