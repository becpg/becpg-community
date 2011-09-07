package fr.becpg.model;

import org.alfresco.service.namespace.QName;

public class ReportModel {

	public static final String REPORT_URI = "http://www.bcpg.fr/model/report/1.0";

	public static final String REPORT_PREFIX = "rep";

	public static final QName MODEL = QName.createQName(REPORT_URI, "repmodel");
	
	// reportType
	public static final QName TYPE_REPORT_TPL = QName.createQName(REPORT_URI,
	"reportTpl");
	public static final QName PROP_REPORT_TPL_TYPE = QName.createQName(REPORT_URI,
	"reportTplType");
	public static final QName PROP_REPORT_TPL_CLASS_NAME = QName.createQName(REPORT_URI,
	"reportTplClassName");
	public static final QName PROP_REPORT_TPL_IS_SYSTEM = QName.createQName(REPORT_URI,
	"reportTplIsSystem");
	public static final QName PROP_REPORT_TPL_IS_DEFAULT = QName.createQName(REPORT_URI,
	"reportTplIsDefault");
	public static final QName PROP_REPORT_TPL_FORMAT = QName.createQName(REPORT_URI,
	"reportTplFormat");
		
	// reportNode
	public static final QName ASPECT_REPORT_NODE = QName.createQName(REPORT_URI,
			"reportNodeAspect");
	public static final QName PROP_REPORT_NODE_GENERATED = QName.createQName(REPORT_URI,
			"reportNodeGenerated");
	
	// reportTpls
	public static final QName ASPECT_REPORT_TEMPLATES = QName.createQName(REPORT_URI,
			"productMicrobioCriteriaAspect");
	public static final QName ASSOC_REPORT_TEMPLATES = QName.createQName(REPORT_URI,
			"reportTpls");
	
	
}
