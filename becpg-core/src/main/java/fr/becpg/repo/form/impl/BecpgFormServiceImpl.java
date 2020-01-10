package fr.becpg.repo.form.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormService;
import org.alfresco.repo.forms.Item;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

import fr.becpg.common.BeCPGException;
import fr.becpg.repo.form.BecpgFormService;

public class BecpgFormServiceImpl implements BecpgFormService, ApplicationContextAware, InitializingBean {

	ApplicationContext applicationContext;

	List<String> resourceBundles;
	List<String> configs;

	FormService formService;

	private NodeService nodeService;

	private NamespaceService namespaceService;

	private ShareFormParser shareFormParser;

	private static Log logger = LogFactory.getLog(BecpgFormServiceImpl.class);

	Map<String, Map<String, BecpgFormDefinition>> definitions = new HashMap<>();

	private static final String PREFIX_FILE = "file:";

	private static final String WILDCARD = "*";

	public FormService getFormService() {
		return formService;
	}

	public void setFormService(FormService formService) {
		this.formService = formService;
	}

	public NodeService getNodeService() {
		return nodeService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public NamespaceService getNamespaceService() {
		return namespaceService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public ShareFormParser getShareFormParser() {
		return shareFormParser;
	}

	public void setShareFormParser(ShareFormParser shareFormParser) {
		this.shareFormParser = shareFormParser;
	}

	public void setConfigs(List<String> configs) {
		this.configs = configs;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		reloadConfig();

	}

	@Override
	public void reloadConfig() throws IOException {
		definitions = new HashMap<>();
		
		for (String config : processWildcards(configs)) {
			Resource resource = applicationContext.getResource(config);
			try (InputStream in = resource.getInputStream()) {
				shareFormParser.visitConfig(definitions, in);

			} catch (Exception e) {
				logger.error("Cannot load config", e);
			}

		}
	}

	@Override
	public JSONObject getForm(String itemKind, String itemId, String formId, String siteId) throws BeCPGException, JSONException {

		Item item = new Item(itemKind, itemId);

		BecpgFormDefinition definition = getFormDefinition(item, formId, siteId);

		Form form = formService.getForm(new Item(itemKind, itemId), definition.getFields(), definition.getForcedFields(), null);

		return definition.merge(form);
	}

	private BecpgFormDefinition getFormDefinition(Item item, String formId, String siteId) throws BeCPGException {

		String id = item.getId();
		if ("node".equals(item.getKind())) {
			id = nodeService.getType(new NodeRef(item.getId())).toPrefixString(namespaceService);

		}

		Map<String, BecpgFormDefinition> forms = definitions.get(id);
		if (forms != null) {
			if (forms.containsKey(formId + "-" + siteId)) {
				return forms.get(formId + "-" + siteId);
			}

			if (forms.containsKey(formId)) {
				return forms.get(formId);
			}

			if (forms.containsKey("default")) {
				return forms.get("default");
			}
		}

		throw new BeCPGException("No form exists for id:" + id + " formId: " + formId);
	}

	private List<String> processWildcards(List<String> configs) {
		List<String> ret = new ArrayList<>();
		for (String config : configs) {
			ret.addAll(processWildcard(config));
		}

		return ret;
	}

	// file:${dir.root}/designer/*.xml
	// file:C:\Alfresco\alf_data\*.xml

	private List<String> processWildcard(String sourceString) {
		List<String> ret = new ArrayList<>();
		if ((sourceString != null) && sourceString.startsWith(PREFIX_FILE) && sourceString.contains(WILDCARD)) {
			char separator = guessSeparator(sourceString);
			logger.debug("processWildCards: " + sourceString);
			File dir = new File(sourceString.substring(PREFIX_FILE.length(), sourceString.lastIndexOf(separator)));
			if ((dir != null) && dir.exists()) {
				FileFilter fileFilter = new WildcardFileFilter(sourceString.substring(sourceString.lastIndexOf(separator) + 1));
				File[] files = dir.listFiles(fileFilter);
				if (files != null) {
					for (File file : files) {
						logger.debug("Add config file : " + PREFIX_FILE + file.getAbsolutePath());
						ret.add(PREFIX_FILE + file.getAbsolutePath());
					}
				}
			}
		} else {
			logger.debug("Add config file : " + sourceString);
			ret.add(sourceString);
		}
		return ret;
	}

	private char guessSeparator(String sourceString) {
		return sourceString.lastIndexOf('\\') > -1 ? '\\' : '/';
	}

}
