package fr.becpg.report.services;

import java.io.IOException;
import java.io.InputStream;

import fr.becpg.report.client.ReportException;

public interface TemplateCacheService {

	Long getTemplateTimeStamp(String templateId);

	Long saveTemplate(String templateId, InputStream in) throws ReportException, IOException;

	InputStream getTemplate(String templateId) throws ReportException;

}
