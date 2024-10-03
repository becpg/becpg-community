package fr.becpg.repo.report.template;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.report.client.ReportFormat;

/**
 * <p>ReportTplInformation class.</p>
 *
 * @author matthieu
 */
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
	

	/**
	 * <p>Getter for the field <code>reportType</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.report.template.ReportType} object
	 */
	public ReportType getReportType() {
		return reportType;
	}

	/**
	 * <p>Setter for the field <code>reportType</code>.</p>
	 *
	 * @param reportType a {@link fr.becpg.repo.report.template.ReportType} object
	 */
	public void setReportType(ReportType reportType) {
		this.reportType = reportType;
	}

	/**
	 * <p>Getter for the field <code>reportFormat</code>.</p>
	 *
	 * @return a {@link fr.becpg.report.client.ReportFormat} object
	 */
	public ReportFormat getReportFormat() {
		return reportFormat;
	}

	/**
	 * <p>Setter for the field <code>reportFormat</code>.</p>
	 *
	 * @param reportFormat a {@link fr.becpg.report.client.ReportFormat} object
	 */
	public void setReportFormat(ReportFormat reportFormat) {
		this.reportFormat = reportFormat;
	}

	/**
	 * <p>Getter for the field <code>nodeType</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.namespace.QName} object
	 */
	public QName getNodeType() {
		return nodeType;
	}

	/**
	 * <p>Setter for the field <code>nodeType</code>.</p>
	 *
	 * @param nodeType a {@link org.alfresco.service.namespace.QName} object
	 */
	public void setNodeType(QName nodeType) {
		this.nodeType = nodeType;
	}

	/**
	 * <p>isSystemTpl.</p>
	 *
	 * @return a boolean
	 */
	public boolean isSystemTpl() {
		return isSystemTpl;
	}

	/**
	 * <p>setSystemTpl.</p>
	 *
	 * @param isSystemTpl a boolean
	 */
	public void setSystemTpl(boolean isSystemTpl) {
		this.isSystemTpl = isSystemTpl;
	}

	/**
	 * <p>isDefaultTpl.</p>
	 *
	 * @return a boolean
	 */
	public boolean isDefaultTpl() {
		return isDefaultTpl;
	}

	/**
	 * <p>setDefaultTpl.</p>
	 *
	 * @param isDefaultTpl a boolean
	 */
	public void setDefaultTpl(boolean isDefaultTpl) {
		this.isDefaultTpl = isDefaultTpl;
	}

	/**
	 * <p>Getter for the field <code>resources</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	public List<NodeRef> getResources() {
		return resources;
	}

	/**
	 * <p>Setter for the field <code>resources</code>.</p>
	 *
	 * @param resources a {@link java.util.List} object
	 */
	public void setResources(List<NodeRef> resources) {
		this.resources = resources;
	}

	/**
	 * <p>Getter for the field <code>supportedLocale</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	public List<String> getSupportedLocale() {
		return supportedLocale;
	}

	/**
	 * <p>Setter for the field <code>supportedLocale</code>.</p>
	 *
	 * @param supportedLocale a {@link java.util.List} object
	 */
	public void setSupportedLocale(List<String> supportedLocale) {
		this.supportedLocale = supportedLocale;
	}


	/**
	 * <p>Getter for the field <code>reportKindAspectProperties</code>.</p>
	 *
	 * @return a {@link java.util.Map} object
	 */
	public Map<QName, Serializable> getReportKindAspectProperties() {
		return reportKindAspectProperties;
	}

	/**
	 * <p>Setter for the field <code>reportKindAspectProperties</code>.</p>
	 *
	 * @param reportKindAspectProperties a {@link java.util.Map} object
	 */
	public void setReportKindAspectProperties(Map<QName, Serializable> reportKindAspectProperties) {
		this.reportKindAspectProperties = reportKindAspectProperties;
	}

	
	
	/**
	 * <p>Getter for the field <code>textParameter</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getTextParameter() {
		return textParameter;
	}

	/**
	 * <p>Setter for the field <code>textParameter</code>.</p>
	 *
	 * @param textParameter a {@link java.lang.String} object
	 */
	public void setTextParameter(String textParameter) {
		this.textParameter = textParameter;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(isDefaultTpl, isSystemTpl, nodeType, reportFormat, reportKindAspectProperties, reportType, resources, supportedLocale,
				textParameter);
	}

	/** {@inheritDoc} */
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
