package fr.becpg.repo.form.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.form.BecpgFormService;
import fr.becpg.repo.form.column.decorator.ColumnDecorator;
import fr.becpg.repo.form.column.decorator.DataGridFormFieldTitleProvider;

/**
 * <p>BecpgFormServiceImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class BecpgFormServiceImpl implements BecpgFormService, ApplicationContextAware, InitializingBean {

	ApplicationContext applicationContext;

	List<String> resourceBundles;

	List<String> configs;

	List<ColumnDecorator> decorators = new LinkedList<>();

	FormService formService;

	private NodeService nodeService;

	private NamespaceService namespaceService;

	private ShareFormParser shareFormParser;

	private static Log logger = LogFactory.getLog(BecpgFormServiceImpl.class);

	Map<String, Map<String, BecpgFormDefinition>> definitions = new HashMap<>();

	private static final String PREFIX_FILE = "file:";

	private static final String WILDCARD = "*";

	/**
	 * <p>Getter for the field <code>formService</code>.</p>
	 *
	 * @return a {@link org.alfresco.repo.forms.FormService} object.
	 */
	public FormService getFormService() {
		return formService;
	}

	/**
	 * <p>Setter for the field <code>decorators</code>.</p>
	 *
	 * @param decorators a {@link java.util.List} object.
	 */
	public void setDecorators(List<ColumnDecorator> decorators) {
		this.decorators = decorators;
	}

	/**
	 * <p>Setter for the field <code>formService</code>.</p>
	 *
	 * @param formService a {@link org.alfresco.repo.forms.FormService} object.
	 */
	public void setFormService(FormService formService) {
		this.formService = formService;
	}

	/**
	 * <p>Getter for the field <code>nodeService</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public NodeService getNodeService() {
		return nodeService;
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Getter for the field <code>namespaceService</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.namespace.NamespaceService} object.
	 */
	public NamespaceService getNamespaceService() {
		return namespaceService;
	}

	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object.
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * <p>Getter for the field <code>shareFormParser</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.form.impl.ShareFormParser} object.
	 */
	public ShareFormParser getShareFormParser() {
		return shareFormParser;
	}

	/**
	 * <p>Setter for the field <code>shareFormParser</code>.</p>
	 *
	 * @param shareFormParser a {@link fr.becpg.repo.form.impl.ShareFormParser} object.
	 */
	public void setShareFormParser(ShareFormParser shareFormParser) {
		this.shareFormParser = shareFormParser;
	}

	/**
	 * <p>Setter for the field <code>configs</code>.</p>
	 *
	 * @param configs a {@link java.util.List} object.
	 */
	public void setConfigs(List<String> configs) {
		this.configs = configs;
	}

	/** {@inheritDoc} */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	/** {@inheritDoc} */
	@Override
	public void afterPropertiesSet() throws Exception {
		reloadConfig();

	}

	@Override
	public void registerDecorator(ColumnDecorator columnDecorator) {
		this.decorators.add(columnDecorator);
	}

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
	@Override
	public BecpgFormDefinition getForm(String itemKind, String itemId, String formId, String siteId, List<String> fields, List<String> forcedFields,
			NodeRef entityNodeRef) throws BeCPGException, JSONException {

		Item item = new Item(itemKind, itemId);
		DataGridFormFieldTitleProvider resolver = null;
		BecpgFormDefinition definition = getFormDefinition(this.definitions, item, formId, siteId);
		if (definition == null) {
			definition = new BecpgFormDefinition(fields, forcedFields);
		}

		boolean override = false;
		if (entityNodeRef != null) {
			if (nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_CUSTOM_FORM_DEFINITIONS)) {
				String customFormDefinition = (String) nodeService.getProperty(entityNodeRef, BeCPGModel.PROP_CUSTOM_FORM_DEFINITIONS);
				if ((customFormDefinition != null) && !customFormDefinition.isBlank()) {
					try {
						BecpgFormDefinition customFormDef = getFormDefinition(parseDefinitions(customFormDefinition), item, formId, siteId);
						if (customFormDef != null) {
							definition = customFormDef;
							override = true;
						}
					} catch (Exception e) {
						logger.error("Cannot load config", e);
					}
				}
			}
			for (ColumnDecorator decorator : decorators) {
				if (decorator.match(item)) {
					resolver = decorator.createTitleResolver(entityNodeRef, item);
					break;
				}
			}
		}

		Form form = formService.getForm(new Item(itemKind, itemId), definition.getFields(), definition.getForcedFields());

		BecpgFormDefinition def = definition.merge(form, resolver);

		JSONObject mergeDef = def.getMergeDef();
		mergeDef.put("override", override);
		def.setMergeDef(mergeDef);

		return def;
	}

	private Map<String, Map<String, BecpgFormDefinition>> parseDefinitions(String customFormDefinition) throws JSONException {
		Map<String, Map<String, BecpgFormDefinition>> ret = new HashMap<>();
		BecpgFormParser becpgFormParser = new BecpgFormParser();
		becpgFormParser.visitConfig(ret, customFormDefinition);
		return ret;
	}

	private BecpgFormDefinition getFormDefinition(Map<String, Map<String, BecpgFormDefinition>> defs, Item item, String formId, String siteId)
			throws BeCPGException {

		String id = item.getId();
		if ("node".equals(item.getKind())) {
			id = nodeService.getType(new NodeRef(item.getId())).toPrefixString(namespaceService);

		}

		Map<String, BecpgFormDefinition> forms = defs.get(id);
		if (forms != null) {
			if ((formId != null) && formId.contains("export")) {
				if (forms.containsKey(formId)) {
					return forms.get(formId);
				} else if (formId.contains("WUsed") && forms.containsKey("datagridWUsed")) {
					return forms.get("datagridWUsed");
				} else if (forms.containsKey("datagrid")) {
					return forms.get("datagrid");
				}
			}

			if ((siteId != null) && forms.containsKey(formId + "-" + siteId)) {
				return forms.get(formId + "-" + siteId);
			}

			if ((formId != null) && forms.containsKey(formId)) {
				return forms.get(formId);
			}

			if (forms.containsKey("default")) {
				return forms.get("default");
			}
		}

		return null;
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
			if (dir.exists()) {
				FileFilter fileFilter = WildcardFileFilter.builder().setWildcards(sourceString.substring(sourceString.lastIndexOf(separator) + 1))
						.get();
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
