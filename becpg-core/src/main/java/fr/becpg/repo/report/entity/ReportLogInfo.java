package fr.becpg.repo.report.entity;

import org.alfresco.service.cmr.repository.NodeRef;

public class ReportLogInfo {

	private ReportLogInfoType type;
	
	private String logMessage;
	
	private String displayMessage;
	
	private NodeRef tplNodeRef;
	
	public ReportLogInfo(ReportLogInfoType type, String logMessage, String displayMessage, NodeRef tplNodeRef) {
		this.type = type;
		this.logMessage = logMessage;
		this.displayMessage = displayMessage;
		this.tplNodeRef = tplNodeRef;
	}
	
	public ReportLogInfoType getType() {
		return type;
	}
	
	public String getLogMessage() {
		return logMessage;
	}
	
	public String getDisplayMessage() {
		return displayMessage;
	}
	
	public NodeRef getTplNodeRef() {
		return tplNodeRef;
	}
	
}
