package fr.becpg.repo.report.helpers;

import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.report.client.ReportFormat;

/**
 * <p>ReportUtils class.</p>
 *
 * @author matthieu
 */
public class ReportUtils {
	
	private ReportUtils() {
		//Do Nothing
	}
	
	
	/**
	 * <p>getReportExtension.</p>
	 *
	 * @param tplName a {@link java.lang.String} object
	 * @param reportFormat a {@link fr.becpg.report.client.ReportFormat} object
	 * @return a {@link java.lang.String} object
	 */
	public static String getReportExtension(String tplName, ReportFormat reportFormat) {

		String format = reportFormat.toString();
		if(ReportFormat.XLSX.equals(reportFormat) && tplName.endsWith(ReportTplService.PARAM_VALUE_XLSMREPORT_EXTENSION)) {
			format = "xlsm";
		}
		
		return format;
	}

}
