package fr.becpg.report.web;

import javax.servlet.http.HttpServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract  class AbstractReportServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1573546882782206148L;

	
	protected static final Log logger = LogFactory.getLog(AbstractReportServlet.class);
	
}
