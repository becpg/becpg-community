package fr.becpg.repo.report.template;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.report.client.ReportFormat;

public class ReportTplInformation {

	ReportType reportType;
	ReportFormat reportFormat;
	QName nodeType;
	boolean isSystemTpl;
	boolean isDefaultTpl;
	List<NodeRef> resources;
	List<String> supportedLocale;
	Map<QName,Serializable> reportKindAspectProperties;
	String textParameter;
	

	public ReportType getReportType() {
		return reportType;
	}

	public void setReportType(ReportType reportType) {
		this.reportType = reportType;
	}

	public ReportFormat getReportFormat() {
		return reportFormat;
	}

	public void setReportFormat(ReportFormat reportFormat) {
		this.reportFormat = reportFormat;
	}

	public QName getNodeType() {
		return nodeType;
	}

	public void setNodeType(QName nodeType) {
		this.nodeType = nodeType;
	}

	public boolean isSystemTpl() {
		return isSystemTpl;
	}

	public void setSystemTpl(boolean isSystemTpl) {
		this.isSystemTpl = isSystemTpl;
	}

	public boolean isDefaultTpl() {
		return isDefaultTpl;
	}

	public void setDefaultTpl(boolean isDefaultTpl) {
		this.isDefaultTpl = isDefaultTpl;
	}

	public List<NodeRef> getResources() {
		return resources;
	}

	public void setResources(List<NodeRef> resources) {
		this.resources = resources;
	}

	public List<String> getSupportedLocale() {
		return supportedLocale;
	}

	public void setSupportedLocale(List<String> supportedLocale) {
		this.supportedLocale = supportedLocale;
	}


	public Map<QName, Serializable> getReportKindAspectProperties() {
		return reportKindAspectProperties;
	}

	public void setReportKindAspectProperties(Map<QName, Serializable> reportKindAspectProperties) {
		this.reportKindAspectProperties = reportKindAspectProperties;
	}

	
	
	public String getTextParameter() {
		return textParameter;
	}

	public void setTextParameter(String textParameter) {
		this.textParameter = textParameter;
	}

	@Override
	public int hashCode() {
		return Objects.hash(isDefaultTpl, isSystemTpl, nodeType, reportFormat, reportKindAspectProperties, reportType, resources, supportedLocale,
				textParameter);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReportTplInformation other = (ReportTplInformation) obj;
		return isDefaultTpl == other.isDefaultTpl && isSystemTpl == other.isSystemTpl && Objects.equals(nodeType, other.nodeType)
				&& reportFormat == other.reportFormat && Objects.equals(reportKindAspectProperties, other.reportKindAspectProperties)
				&& reportType == other.reportType && Objects.equals(resources, other.resources)
				&& Objects.equals(supportedLocale, other.supportedLocale) && Objects.equals(textParameter, other.textParameter);
	}

	
	
}
