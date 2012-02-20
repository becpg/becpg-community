package fr.becpg.report.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public interface BeCPGReportService {

	void generateReport(String templateId, String format,
			InputStream dataSource, OutputStream out, Map<String, byte[]> images) throws IOException;

}
