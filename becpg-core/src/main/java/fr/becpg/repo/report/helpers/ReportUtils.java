package fr.becpg.repo.report.helpers;

import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.report.client.ReportFormat;

public class ReportUtils {
	
	private ReportUtils() {
		//Do Nothing
	}
	
	
	public static String getReportExtension(String tplName, ReportFormat reportFormat) {

		String format = reportFormat.toString();
		if(ReportFormat.XLSX.equals(reportFormat) && tplName.endsWith(ReportTplService.PARAM_VALUE_XLSMREPORT_EXTENSION)) {
			format = "xlsm";
		}
		
		return format;
	}

}
