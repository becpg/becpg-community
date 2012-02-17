package fr.becpg.report.services;

import java.io.InputStream;

public interface TemplateCacheService {

	Long getTemplateTimeStamp(String templateId);

	Long saveTemplate(String templateId, InputStream in);

}
