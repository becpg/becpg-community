package fr.becpg.repo.entity.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.alfresco.repo.dictionary.DictionaryComponent;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.cache.AsynchronouslyRefreshedCacheRegistry;
import org.alfresco.util.cache.RefreshableCacheEvent;
import org.alfresco.util.cache.RefreshableCacheListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.surf.util.ParameterCheck;

import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.RepositoryEntityDefReader;

/**
 * Enhanced implementation of EntityDictionaryService with performance optimizations
 * including improved caching, reduced object creation, and optimized data structures.
 *
 * @author matthieu Fast and cached access to dataDictionary
 * @version $Id: $Id
 */
public class EntityDictionaryServiceImpl extends DictionaryComponent
		implements DictionaryService, EntityDictionaryService, RefreshableCacheListener, InitializingBean {

	private static final Log logger = LogFactory.getLog(EntityDictionaryServiceImpl.class);

	// Constants for cache keys
	private static final String PROP_DEF_CACHE_SUFFIX = ".propDef";
	private static final String CLASS_DEF_CACHE_SUFFIX = ".classDef";
	private static final String SUB_TYPES_CACHE_SUFFIX = ".getSubTypes.";
	private static final String SUB_ASPECTS_CACHE_SUFFIX = ".getSubAspects.";
	private static final String PREFIX_STRING_CACHE_SUFFIX = ".toPrefixString";
	private static final String IS_SUB_CLASS_CACHE_SUFFIX = ".isSubClass";
	private static final String ASSOC_INDEX_CACHE_SUFFIX = ".assocIndex";
	private static final String ASSOC_INDEX_PROPERTY_SUFFIX = "AssocIndex";

	private static final String CACHE_SEPARATOR = "_";
	private static final String MODEL_OVERRIDE_PREFIX = "model.override.";
	private static final String TITLE_SUFFIX = ".title";
	private static final String DESCRIPTION_SUFFIX = ".description";
	private static final String COLON_REPLACEMENT = "_";
	private static final String COMPILED_MODELS_CACHE = "compiledModelsCache";

	private DictionaryDAO dictionaryDAO;
	private BeCPGCacheService beCPGCacheService;
	private AsynchronouslyRefreshedCacheRegistry registry;
	private RepositoryEntityDefReader<RepositoryEntity> repositoryEntityDefReader;
	private NamespaceService namespaceService;
	private MessageService messageService;

	// Use ConcurrentHashMap for thread-safe access without synchronization overhead
	private final Map<QName, QName> propDefMapping = new ConcurrentHashMap<>();
	private final Map<QName, Set<QName>> extraAssocsDefMapping = new ConcurrentHashMap<>();

	// Cache for frequently computed strings to avoid repeated computations
	private final Map<QName, String> prefixStringCache = new ConcurrentHashMap<>();
	private final Map<String, String> overrideKeyCache = new ConcurrentHashMap<>();

	// Setters
	/**
	 * <p>Setter for the field <code>messageService</code>.</p>
	 *
	 * @param messageService a {@link org.alfresco.repo.i18n.MessageService} object
	 */
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}

	/**
	 * <p>Setter for the field <code>repositoryEntityDefReader</code>.</p>
	 *
	 * @param repositoryEntityDefReader a {@link fr.becpg.repo.repository.RepositoryEntityDefReader} object
	 */
	public void setRepositoryEntityDefReader(RepositoryEntityDefReader<RepositoryEntity> repositoryEntityDefReader) {
		this.repositoryEntityDefReader = repositoryEntityDefReader;
	}

	/**
	 * <p>Setter for the field <code>registry</code>.</p>
	 *
	 * @param registry a {@link org.alfresco.util.cache.AsynchronouslyRefreshedCacheRegistry} object
	 * @since 23.4.2.28
	 */
	public void setRegistry(AsynchronouslyRefreshedCacheRegistry registry) {
		this.registry = registry;
	}

	/**
	 * <p>Setter for the field <code>beCPGCacheService</code>.</p>
	 *
	 * @param beCPGCacheService a {@link fr.becpg.repo.cache.BeCPGCacheService} object
	 * @since 23.4.2.28
	 */
	public void setBeCPGCacheService(BeCPGCacheService beCPGCacheService) {
		this.beCPGCacheService = beCPGCacheService;
	}

	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/** {@inheritDoc} */
	@Override
	public void setDictionaryDAO(DictionaryDAO dictionaryDAO) {
		super.setDictionaryDAO(dictionaryDAO);
		this.dictionaryDAO = dictionaryDAO;
	}

	// Repository entity methods - delegated to reader
	/** {@inheritDoc} */
	@Override
	public QName getDefaultPivotAssoc(QName dataListItemType) {
		return repositoryEntityDefReader.getDefaultPivoAssocName(dataListItemType);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isMultiLevelDataList(QName dataListItemType) {
		return repositoryEntityDefReader.isMultiLevelDataList(dataListItemType);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isMultiLevelLeaf(QName entityType) {
		return repositoryEntityDefReader.isMultiLevelLeaf(entityType);
	}

	/** {@inheritDoc} */
	@Override
	public QName getMultiLevelSecondaryPivot(QName dataListItemType) {
		return repositoryEntityDefReader.getMultiLevelSecondaryPivot(dataListItemType);
	}

	/** {@inheritDoc} */
	@Override
	public QName getMultiLevelGroupProperty(QName dataListItemType) {
		return repositoryEntityDefReader.getMultiLevelGroupProperty(dataListItemType);
	}

	// Mapping registration methods
	/** {@inheritDoc} */
	@Override
	public void registerPropDefMapping(QName orig, QName dest) {
		propDefMapping.put(orig, dest);
	}

	/** {@inheritDoc} */
	@Override
	public void registerExtraAssocsDefMapping(QName orig, QName dest) {
		extraAssocsDefMapping.computeIfAbsent(orig, k -> ConcurrentHashMap.newKeySet()).add(dest);
	}

	/** {@inheritDoc} */
	@Override
	public List<AssociationDefinition> getPivotAssocDefs(QName sourceType) {
		return getPivotAssocDefs(sourceType, false);
	}

	/** {@inheritDoc} */
	@Override
	public List<AssociationDefinition> getPivotAssocDefs(QName sourceType, boolean exactMatch) {
		// Use ArrayList with initial capacity to avoid resizing
		List<AssociationDefinition> ret = new ArrayList<>(16);

		// Cache associations list to avoid repeated calls
		Collection<QName> allAssociations = getAllAssociations();

		for (QName assocQName : allAssociations) {
			AssociationDefinition assocDef = getAssociation(assocQName);
			if (assocDef != null) {
				QName targetClassName = assocDef.getTargetClass().getName();
				if ((exactMatch && targetClassName.equals(sourceType)) || (!exactMatch && isSubClass(targetClassName, sourceType))) {
					ret.add(assocDef);
				}
			}
		}

		// Add extra associations if present
		Set<QName> extraAssocs = extraAssocsDefMapping.get(sourceType);
		if (extraAssocs != null && !extraAssocs.isEmpty()) {
			for (QName assocQName : extraAssocs) {
				AssociationDefinition assocDef = getAssociation(assocQName);
				if (assocDef != null) {
					ret.add(assocDef);
				}
			}
		}

		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public QName getTargetType(QName assocName) {
		AssociationDefinition assocDef = getAssociation(assocName);
		return assocDef != null ? assocDef.getTargetClass().getName() : null;
	}

	/** {@inheritDoc} */
	@Override
	public ClassAttributeDefinition findMatchingPropDef(QName itemType, QName newItemType, QName fieldQname) {
		// Check mapping first
		QName mappedQName = propDefMapping.get(fieldQname);
		if (mappedQName != null) {
			return getPropDef(mappedQName);
		}

		// Try name-based matching with caching
		String fieldLocalName = fieldQname.getLocalName();
		String itemLocalName = itemType.getLocalName();

		if (fieldLocalName.contains(itemLocalName)) {
			QName newQname = QName.createQName(fieldQname.getNamespaceURI(), fieldLocalName.replace(itemLocalName, newItemType.getLocalName()));
			ClassAttributeDefinition ret = getPropDef(newQname);
			if (ret != null) {
				return ret;
			}
		}

		return getPropDef(fieldQname);
	}

	/** {@inheritDoc} */
	@Override
	public ClassAttributeDefinition getPropDef(final QName fieldQname) {
		String cacheKey = buildCacheKey(fieldQname.toString(), PROP_DEF_CACHE_SUFFIX);

		return beCPGCacheService.getFromCache(EntityDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			ClassAttributeDefinition propDef = getProperty(fieldQname);
			if (propDef == null) {
				propDef = getAssociation(fieldQname);
			}
			return propDef;
		});
	}

	/** {@inheritDoc} */
	@Override
	public boolean isAssoc(QName assocName) {
		return getAssociation(assocName) != null;
	}

	/** {@inheritDoc} */
	@Override
	public Collection<QName> getSubTypes(QName typeQname) {
		return getSubTypes(typeQname, true);
	}

	/** {@inheritDoc} */
	@Override
	public Collection<QName> getSubTypes(QName superType, boolean follow) {
		String cacheKey = buildCacheKey(superType.toString(), SUB_TYPES_CACHE_SUFFIX, String.valueOf(follow));

		return beCPGCacheService.getFromCache(EntityDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			Collection<QName> result = dictionaryDAO.getSubTypes(superType, follow);
			return result != null ? Collections.unmodifiableCollection(result) : Collections.emptyList();
		});
	}

	/** {@inheritDoc} */
	@Override
	public Collection<QName> getSubAspects(QName superAspect, boolean follow) {
		String cacheKey = buildCacheKey(superAspect.toString(), SUB_ASPECTS_CACHE_SUFFIX, String.valueOf(follow));

		return beCPGCacheService.getFromCache(EntityDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			Collection<QName> result = dictionaryDAO.getSubAspects(superAspect, follow);
			return result != null ? Collections.unmodifiableCollection(result) : Collections.emptyList();
		});
	}

	/** {@inheritDoc} */
	@Override
	public String toPrefixString(QName propertyQName) {
		return prefixStringCache.computeIfAbsent(propertyQName, qname -> {
			String cacheKey = buildCacheKey(qname.toString(), PREFIX_STRING_CACHE_SUFFIX);
			return beCPGCacheService.getFromCache(EntityDictionaryServiceImpl.class.getName(), cacheKey,
					() -> qname.toPrefixString(namespaceService));
		});
	}

	/** {@inheritDoc} */
	@Override
	public boolean isSubClass(QName className, QName ofClassName) {
		if (className == null || ofClassName == null) {
			return false;
		}

		if (className.equals(ofClassName)) {
			return true;
		}

		String cacheKey = buildCacheKey(className.toString(), CACHE_SEPARATOR, ofClassName.toString(), IS_SUB_CLASS_CACHE_SUFFIX);

		return beCPGCacheService.getFromCache(EntityDictionaryServiceImpl.class.getName(), cacheKey, () -> computeIsSubClass(className, ofClassName));
	}

	/** {@inheritDoc} */
	@Override
	public void onRefreshableCacheEvent(RefreshableCacheEvent refreshableCacheEvent) {
		if (COMPILED_MODELS_CACHE.equals(refreshableCacheEvent.getCacheId())) {
			if (logger.isInfoEnabled()) {
				logger.info("Refreshing CachedDictionaryService cache: " + refreshableCacheEvent.getCacheId());
			}
			beCPGCacheService.clearCache(EntityDictionaryServiceImpl.class.getName());
			prefixStringCache.clear();
			overrideKeyCache.clear();
		}
	}

	/** {@inheritDoc} */
	@Override
	public String getCacheId() {
		return EntityDictionaryServiceImpl.class.getName();
	}

	/** {@inheritDoc} */
	@Override
	public void afterPropertiesSet() throws Exception {
		registry.register(this);
	}

	/** {@inheritDoc} */
	@Override
	public String getTitle(ClassAttributeDefinition attributeDefinition, QName nodeType) {
		if (nodeType != null) {
			String overrideKey = computeOverrideKey(attributeDefinition, nodeType);
			String title = messageService.getMessage(overrideKey + TITLE_SUFFIX);
			if (title != null && !title.isBlank()) {
				return title;
			}
		}
		return attributeDefinition.getTitle(this);
	}

	/** {@inheritDoc} */
	@Override
	public String getDescription(ClassAttributeDefinition attributeDefinition, QName nodeType) {
		if (nodeType != null) {
			String overrideKey = computeOverrideKey(attributeDefinition, nodeType);
			String description = messageService.getMessage(overrideKey + DESCRIPTION_SUFFIX);
			if (description != null && !description.isBlank()) {
				return description;
			}
		}
		return attributeDefinition.getDescription(this);
	}

	/** {@inheritDoc} */
	@Override
	public QName getAssocIndexQName(QName assocQName) {
		String cacheKey = buildCacheKey(assocQName.toString(), ASSOC_INDEX_CACHE_SUFFIX);

		return beCPGCacheService.getFromCache(EntityDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			QName indexPropertyQName = QName.createQName(assocQName.getNamespaceURI(), assocQName.getLocalName() + ASSOC_INDEX_PROPERTY_SUFFIX);
			ClassAttributeDefinition indexProp = getPropDef(indexPropertyQName);
			return indexProp != null ? indexProp.getName() : null;
		});
	}

	/** {@inheritDoc} */
	@Override
	public ClassDefinition getClass(QName name) {
		if (name == null) {
			return null;
		}
		String cacheKey = buildCacheKey(name.toString(), CLASS_DEF_CACHE_SUFFIX);

		return beCPGCacheService.getFromCache(EntityDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			return super.getClass(name);
		});
	}

	// Private helper methods

	/**
	 * Efficiently builds cache keys by concatenating strings
	 */
	private String buildCacheKey(String... parts) {
		if (parts.length == 1) {
			return parts[0];
		}

		StringBuilder sb = new StringBuilder();
		for (String part : parts) {
			sb.append(part);
		}
		return sb.toString();
	}

	/**
	 * Computes the actual isSubClass logic separated for better readability
	 */
	private boolean computeIsSubClass(QName className, QName ofClassName) {
		// Validate arguments
		ParameterCheck.mandatory("className", className);
		ParameterCheck.mandatory("ofClassName", ofClassName);

		ClassDefinition classDef = getClass(className);
		if (classDef == null) {
			return false;
		}

		ClassDefinition ofClassDef = getClass(ofClassName);
		if (ofClassDef == null) {
			return false;
		}

		// Only check if both ends are either a type or an aspect
		if (classDef.isAspect() != ofClassDef.isAspect()) {
			return false;
		}

		// Walk up the hierarchy
		while (classDef != null) {
			if (classDef.equals(ofClassDef)) {
				return true;
			}

			// Move to parent class
			QName parentClassName = classDef.getParentName();
			classDef = (parentClassName == null) ? null : getClass(parentClassName);
		}

		return false;
	}

	/**
	 * Computes override keys with caching to avoid repeated string operations
	 */
	private String computeOverrideKey(ClassAttributeDefinition attributeDefinition, QName nodeType) {
		String key = nodeType.toString() + "|" + attributeDefinition.getName().toString();

		return overrideKeyCache.computeIfAbsent(key, k -> {
			String nodeTypePrefix = nodeType.toPrefixString(namespaceService).replace(":", COLON_REPLACEMENT);
			String attrNamePrefix = attributeDefinition.getName().toPrefixString(namespaceService).replace(":", COLON_REPLACEMENT);
			return MODEL_OVERRIDE_PREFIX + nodeTypePrefix + "." + attrNamePrefix;
		});
	}
}
