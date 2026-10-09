package fr.becpg.repo.report.helpers;

import java.net.SocketException;

import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.report.client.ReportFormat;

/**
 * <p>ReportUtils class.</p>
 *
 * @author matthieu
 */
public class ReportUtils {

	private static final String SERVLET_CONNECTOR_PACKAGE = "org.apache.catalina.connector.";

	/**
	 * <p>Constructor for ReportUtils.</p>
	 */
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

	/**
	 * <p>Tells whether a failure means the caller gave up rather than that the
	 * report failed.</p>
	 *
	 * <p>It takes several shapes: the client of the report server raises a
	 * <code>ReportClientAbortException</code> when writing the report back fails,
	 * the connector raises its own <code>ClientAbortException</code>, and a
	 * severed connection surfaces as a {@link java.net.SocketException}. Tomcat
	 * also refuses a write on a closed response with a plain
	 * {@link java.io.IOException} whose message is localized — which is why the
	 * client raises a type of its own instead of leaving that one to be matched on
	 * its text.</p>
	 *
	 * <p>Container and client types are matched by name so that this class depends
	 * on neither.</p>
	 *
	 * @param throwable a {@link java.lang.Throwable} object
	 * @return true when the exchange was abandoned by the caller
	 */
	public static boolean isClientAbort(Throwable throwable) {
		for (Throwable cause = throwable; cause != null; cause = cause.getCause()) {
			String name = cause.getClass().getName();
			if ((cause instanceof SocketException) || name.endsWith("ClientAbortException")
					|| name.endsWith("ReportClientAbortException") || failedWritingTheResponse(cause)) {
				return true;
			}
			if (cause.getCause() == cause) {
				break;
			}
		}
		return false;
	}

	/*
	 * Tomcat refuses a write on a closed response with a plain IOException whose
	 * message is localized, so the message cannot be matched on. Where it was
	 * raised can: the connector's output buffer is only ever reached while writing
	 * back to the caller, which makes a failure there the caller's side of the
	 * exchange by construction.
	 */
	private static boolean failedWritingTheResponse(Throwable cause) {
		StackTraceElement[] frames = cause.getStackTrace();
		return (frames.length > 0) && frames[0].getClassName().startsWith(SERVLET_CONNECTOR_PACKAGE);
	}

}
