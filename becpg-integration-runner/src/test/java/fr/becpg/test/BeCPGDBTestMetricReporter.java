package fr.becpg.test;

import org.alfresco.metrics.db.DBMetricsReporter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

@Service("dbMetricsReporterImpl")
public class BeCPGDBTestMetricReporter implements DBMetricsReporter {

	private Boolean enabled = false;
	
	private static Log logger = LogFactory.getLog(BeCPGDBTestMetricReporter.class);
	
	@Override
	public void reportQueryExecutionTime(long milliseconds, String queryTpe, String statementID) {
		logger.info(queryTpe+" "+statementID+" "+milliseconds);
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public boolean isQueryMetricsEnabled() {
		return enabled;
	}

	@Override
	public boolean isQueryStatementsMetricsEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
	
}
