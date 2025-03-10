/*
 *
 */
package fr.becpg.repo.entity.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.AutoNumService;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * The Class AutoNumServiceImpl.
 *
 * @author querephi
 * @version $Id: $Id
 */
@Service("autoNumService")
public class AutoNumServiceImpl implements AutoNumService {

	private static final String NAME = "%s - %s";

	private static final Long DEFAULT_AUTO_NUM = 1L;

	private static final String PREFIX_MSG_PFX = "autonum.prefix.";

	private static final String DEFAULT_PREFIX = "";

	private static final Pattern DEFAULT_PATTERN = java.util.regex.Pattern.compile("(^[A-Z]+)(\\d+$)");

	private static final Log logger = LogFactory.getLog(AutoNumServiceImpl.class);

	@Autowired
	private NodeService nodeService;

	@Autowired
	private RepoService repoService;

	@Autowired
	private Repository repositoryHelper;

	@Autowired
	private DictionaryService dictionaryService;

	@Autowired
	private BeCPGCacheService beCPGCacheService;

	@Autowired
	private BehaviourFilter policyBehaviourFilter;

	/**
	 * {@inheritDoc}
	 *
	 * Get the next autoNum value.
	 */
	@Override
	public synchronized String getAutoNumValue(QName className, QName propertyName) {

		Long autoNumValue = DEFAULT_AUTO_NUM;
		NodeRef autoNumNodeRef = getAutoNumNodeRef(className, propertyName);

		String prefix;

		// get value store in db
		if ((autoNumNodeRef != null) && nodeService.exists(autoNumNodeRef)) {

			Long v = (Long) nodeService.getProperty(autoNumNodeRef, BeCPGModel.PROP_AUTO_NUM_VALUE);
			prefix = getPrefix(autoNumNodeRef, DEFAULT_PREFIX);

			if (v != null) {
				autoNumValue = v;
				autoNumValue++;
			}

			// update autonum node in db
			nodeService.setProperty(autoNumNodeRef, BeCPGModel.PROP_AUTO_NUM_VALUE, autoNumValue);
		} else {
			// create autonum node in db
			prefix = getDefaultPrefix(className, propertyName);
			createAutoNum(className, propertyName, autoNumValue, prefix);
		}

		return formatCode(prefix, autoNumValue);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>setAutoNumValue.</p>
	 */
	@Override
	public synchronized boolean setAutoNumValue(QName className, QName propertyName, Long counter) {

		NodeRef autoNumNodeRef = getAutoNumNodeRef(className, propertyName);

		// get value store in db
		if ((autoNumNodeRef != null) && nodeService.exists(autoNumNodeRef) && (counter != null)) {
			// update autonum node in db
			nodeService.setProperty(autoNumNodeRef, BeCPGModel.PROP_AUTO_NUM_VALUE, counter);
			return true;
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Delete the AutoNum object in db.
	 */
	@Override
	public void deleteAutoNumValue(QName className, QName propertyName) {

		NodeRef autoNumNodeRef = getAutoNumNodeRef(className, propertyName);
		if (autoNumNodeRef != null) {

			beCPGCacheService.removeFromCache(AutoNumServiceImpl.class.getName(), className.toString() + "-" + propertyName.toString());

			if (nodeService.exists(autoNumNodeRef)) {
				nodeService.deleteNode(autoNumNodeRef);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public String getAutoNumMatchPattern(QName type, QName propertyName) {
		String prefix = DEFAULT_PREFIX;
		if (!dictionaryService.getSubTypes(type, true).isEmpty()) {
			for (QName subType : dictionaryService.getSubTypes(type, true)) {
				if (prefix.length() != 0) {
					prefix += "|";
				}
				prefix += getAutoNumPrefix(subType, propertyName);
			}

		} else {
			prefix = getAutoNumPrefix(type, propertyName);
		}
		return getMatchPattern(prefix);
	}

	private String getMatchPattern(String prefix) {
		return "(^" + prefix + ")" + "(\\d*$)";
	}

	private String getAutoNumPrefix(QName type, QName propertyName) {
		String prefix;
		NodeRef autoNumNodeRef = getAutoNumNodeRef(type, propertyName);
		if (autoNumNodeRef != null) {
			prefix = getPrefix(autoNumNodeRef, DEFAULT_PREFIX);
		} else {
			prefix = getDefaultPrefix(type, propertyName);
		}
		return prefix;
	}

	/** {@inheritDoc} */
	@Override
	public String getPrefixedCode(QName type, QName propertyName, Long autoNumValue) {
		String prefix = getAutoNumPrefix(type, propertyName);
		return formatCode(prefix, autoNumValue);
	}

	/** {@inheritDoc} */
	@Override
	public synchronized String getOrCreateCode(NodeRef nodeRef, QName codeQName) {

		boolean isEnabledBehaviour = policyBehaviourFilter.isEnabled(ContentModel.ASPECT_AUDITABLE);
		try {

			policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);

			// check code is already taken. If yes : this object is a copy of an
			// existing node
			String code = (String) nodeService.getProperty(nodeRef, codeQName);
			boolean generateCode = true;
			QName typeQName = nodeService.getType(nodeRef);

			if ((code != null) && !code.isEmpty()) {

				generateCode = BeCPGQueryBuilder.createQuery().ofType(typeQName).andPropEquals(codeQName, code).andNotID(nodeRef).inDB()
						.singleValue() != null;

			}

			// generate a new code
			if (generateCode) {
				code = getAutoNumValue(typeQName, codeQName);
				nodeService.setProperty(nodeRef, codeQName, code);
			} else {
				// store autoNum in db
				createOrUpdateAutoNumValue(typeQName, codeQName, code);
			}
			return code;
		} finally {
			if(isEnabledBehaviour) {
				policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
			}
		}

	}

	/** {@inheritDoc} */
	@Override
	public String getOrCreateBeCPGCode(NodeRef nodeRef) {
		return getOrCreateCode(nodeRef, BeCPGModel.PROP_CODE);
	}

	/**
	 * Store AutoNum value in db.
	 *
	 * @param className
	 *            the class name
	 * @param propertyName
	 *            the property name
	 * @param autoNumValue
	 *            the auto num value
	 */
	private String createOrUpdateAutoNumValue(QName className, QName propertyName, String autoNumCode) {

		Long autoNumValue = DEFAULT_AUTO_NUM;

		String prefix = getDefaultPrefix(className, propertyName);
		java.util.regex.Matcher ma = DEFAULT_PATTERN.matcher(autoNumCode);
		if (ma.matches()) {
			prefix = ma.group(1);
			autoNumCode = ma.group(2);
		}
		try {
			autoNumValue = Long.parseLong(autoNumCode);
		} catch (NumberFormatException e) {
			logger.warn("cannot parse autoNum : " + autoNumCode, e);
		}

		NodeRef autoNumNodeRef = getAutoNumNodeRef(className, propertyName);

		if ((autoNumNodeRef != null) && nodeService.exists(autoNumNodeRef)) {
			Long oldValue = (Long) nodeService.getProperty(autoNumNodeRef, BeCPGModel.PROP_AUTO_NUM_VALUE);
			if ((oldValue == null) || (oldValue < autoNumValue)) {
				nodeService.setProperty(autoNumNodeRef, BeCPGModel.PROP_AUTO_NUM_VALUE, autoNumValue);
			}
		} else {
			createAutoNum(className, propertyName, autoNumValue, prefix);
		}
		return formatCode(prefix, autoNumValue);
	}

	/**
	 * {@inheritDoc}
	 *
	 * get AutoNum value from db.
	 */
	@Override
	public NodeRef getAutoNumNodeRef(final QName className, final QName propertyName) {

		return beCPGCacheService.getFromCache(AutoNumServiceImpl.class.getName(), className.toString() + "-" + propertyName.toString(),
				() -> BeCPGQueryBuilder.createQuery().ofType(BeCPGModel.TYPE_AUTO_NUM)
						.andPropEquals(BeCPGModel.PROP_AUTO_NUM_CLASS_NAME, className.toString())
						.andPropEquals(BeCPGModel.PROP_AUTO_NUM_PROPERTY_NAME, propertyName.toString()).inDB().ftsLanguage().singleValue());

	}

	private String getDefaultPrefix(QName className, QName propertyName) {
		String ret = I18NUtil.getMessage(PREFIX_MSG_PFX + className.getLocalName() + "." + propertyName.getLocalName());
		if ((ret == null) || (ret.length() < 1)) {
			return DEFAULT_PREFIX;
		}
		return ret;
	}

	/**
	 * Create AutoNum value in db.
	 *
	 * @param className
	 *            the class name
	 * @param propertyName
	 *            the property name
	 * @param autoNumValue
	 *            the auto num value
	 * @return the node ref
	 */
	private Long createAutoNum(QName className, QName propertyName, Long autoNumValue, String autoNumPrefix) {

		beCPGCacheService.removeFromCache(AutoNumServiceImpl.class.getName(), className.toString() + "-" + propertyName.toString());

		NodeRef systemNodeRef = repoService.getOrCreateFolderByPath(repositoryHelper.getCompanyHome(), RepoConsts.PATH_SYSTEM,
				TranslateHelper.getTranslatedPath(RepoConsts.PATH_SYSTEM));
		NodeRef autoNumFolderNodeRef = repoService.getOrCreateFolderByPath(systemNodeRef, RepoConsts.PATH_AUTO_NUM,
				TranslateHelper.getTranslatedPath(RepoConsts.PATH_AUTO_NUM));

		String name = String.format(NAME, className.getLocalName(), propertyName.getLocalName());
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(ContentModel.PROP_NAME, name);
		properties.put(BeCPGModel.PROP_AUTO_NUM_CLASS_NAME, className);
		properties.put(BeCPGModel.PROP_AUTO_NUM_PROPERTY_NAME, propertyName);
		properties.put(BeCPGModel.PROP_AUTO_NUM_VALUE, autoNumValue);
		properties.put(BeCPGModel.PROP_AUTO_NUM_PREFIX, autoNumPrefix);
		nodeService.createNode(autoNumFolderNodeRef, ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(name)), BeCPGModel.TYPE_AUTO_NUM, properties);

		return autoNumValue;
	}

	private String getPrefix(NodeRef autoNumNodeRef, String defaultPrefix) {
		String prefix = (String) nodeService.getProperty(autoNumNodeRef, BeCPGModel.PROP_AUTO_NUM_PREFIX);
		if (prefix == null) {
			return defaultPrefix;
		}
		return prefix;
	}

	private String formatCode(String prefix, Long autoNumValue) {
		return prefix + autoNumValue;

	}

}
