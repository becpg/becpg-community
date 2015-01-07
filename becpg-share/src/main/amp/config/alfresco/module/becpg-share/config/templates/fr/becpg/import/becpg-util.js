var BeCPGUtil = {

	getDefaultReport : function getDefaultReport(reports) {
		var defaultReport = null;

		for ( var j in reports) {
			var report = reports[j];
			if (defaultReport == null) {
				// Set First as default
				defaultReport = report;
			}
			if (report.isSelected || report.isDefault) {
				// Override with default
				defaultReport = report;
				if (report.isSelected) {
					// If selected here we are
					break;
				}
			}
		}
		return defaultReport;
	}

}
