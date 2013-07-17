package fr.becpg.repo.report.jscript;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.report.template.ReportTplService;

public class ReportAssociationDecorator extends fr.becpg.repo.jscript.app.BaseAssociationDecorator {
	private static Log logger = LogFactory.getLog(ReportAssociationDecorator.class);

	private ReportTplService reportTplService;

	private PreferenceService preferenceService;

	private static final String PREF_REPORT_PREFIX = "fr.becpg.repo.report.";
	private static final String PREF_REPORT_SUFFIX = ".view";
	
    private final static String CONTENT_DOWNLOAD_API_URL = "api/node/content/{0}/{1}/{2}/{3}";

	public void setReportTplService(ReportTplService reportTplService) {
		this.reportTplService = reportTplService;
	}

	public void setPreferenceService(PreferenceService preferenceService) {
		this.preferenceService = preferenceService;
	}

	@SuppressWarnings("unchecked")
	public JSONAware decorate(QName propertyName, NodeRef nodeRef, List<NodeRef> assocs) {
		JSONArray array = new JSONArray();

		String username = AuthenticationUtil.getFullyAuthenticatedUser();
		String typeName = nodeService.getType(nodeRef).toPrefixString(namespaceService).replace(":", "_");

		Map<String, Serializable> preferences = preferenceService.getPreferences(username);

		String reportName = (String) preferences.get(PREF_REPORT_PREFIX + typeName + PREF_REPORT_SUFFIX);

		if (logger.isDebugEnabled()) {
			logger.debug("Getting: " + reportName + " from preference for: " + username + " and type: " + typeName);
		}

		for (NodeRef obj : assocs) {
			if(permissionService.hasPermission(obj, "Read") == AccessStatus.ALLOWED){
				try {
					JSONObject jsonObj = new JSONObject();
					
					String name = (String )this.nodeService.getProperty(obj, ContentModel.PROP_NAME);
					
					jsonObj.put("name", name);
					NodeRef reportTemplateNodeRef = reportTplService.getAssociatedReportTemplate(obj);
					if (reportTemplateNodeRef != null) {
						String templateName = (String) this.nodeService.getProperty(reportTemplateNodeRef, ContentModel.PROP_NAME);
						if(templateName.endsWith(RepoConsts.REPORT_EXTENSION_BIRT)){
							templateName = templateName.replace("." + RepoConsts.REPORT_EXTENSION_BIRT, "");
						}
						jsonObj.put("templateName", templateName);
						jsonObj.put("isDefault", this.nodeService.getProperty(reportTemplateNodeRef, ReportModel.PROP_REPORT_TPL_IS_DEFAULT));
						if (templateName.equalsIgnoreCase(reportName)) {
							jsonObj.put("isSelected", true);
						} else {
							jsonObj.put("isSelected", false);
						}
					}

					jsonObj.put("nodeRef", obj.toString());
					
					try {
						String contentURL = MessageFormat.format(
						            CONTENT_DOWNLOAD_API_URL, new Object[]{
						            		obj.getStoreRef().getProtocol(),
						            		obj.getStoreRef().getIdentifier(),
						            		obj.getId(),
						                    URLEncoder.encode(name,"UTF-8")});

						jsonObj.put("contentURL", contentURL);
					} catch (UnsupportedEncodingException e) {
						logger.error(e,e);
					}
					
					array.add(jsonObj);
				} catch (InvalidNodeRefException e) {
					logger.warn("Report with nodeRef " + obj.toString() + " does not exist.");
				}
			}			
		}

		return array;
	}

}