package fr.becpg.repo.report.template;

public enum ReportType {

	System, //default
	Document,
	Search;
			
	public static ReportType parse(String r){
		
		ReportType reportType = ReportType.System;
		
		if(r != null){
			reportType = ReportType.valueOf(r);
		}
		
		return reportType;
	}
}
