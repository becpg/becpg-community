package fr.becpg.repo.entity.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.alfresco.repo.dictionary.DictionaryComponent;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.cache.AsynchronouslyRefreshedCacheRegistry;
import org.alfresco.util.cache.RefreshableCacheEvent;
import org.alfresco.util.cache.RefreshableCacheListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.surf.util.ParameterCheck;

import fr.becpg.repo.cache.BeCPGCacheService;

/**
 * Enhanced implementation of EntityDictionaryService with performance optimizations
 * including improved caching, reduced object creation, and optimized data structures.
 *
 * @author matthieu Fast and cached access to dataDictionary
 * @version $Id: $Id
 */
public class CachedDictionaryServiceImpl extends DictionaryComponent implements DictionaryService, RefreshableCacheListener, InitializingBean {

	private static final Log logger = LogFactory.getLog(CachedDictionaryServiceImpl.class);

	// Constants for cache keys
	private static final String CLASS_DEF_CACHE_SUFFIX = ".classDef";
	private static final String SUB_TYPES_CACHE_SUFFIX = ".getSubTypes.";
	private static final String SUB_ASPECTS_CACHE_SUFFIX = ".getSubAspects.";
	private static final String IS_SUB_CLASS_CACHE_SUFFIX = ".isSubClass";
	private static final String ALL_MODELS_CACHE_SUFFIX = ".getAllModels.";
	private static final String MODEL_CACHE_SUFFIX = ".getModel";
	private static final String ALL_DATA_TYPES_CACHE_SUFFIX = ".getAllDataTypes";
	private static final String DATA_TYPES_CACHE_SUFFIX = ".getDataTypes";
	private static final String ALL_TYPES_CACHE_SUFFIX = ".getAllTypes.";
	private static final String TYPES_CACHE_SUFFIX = ".getTypes";
	private static final String ALL_ASPECTS_CACHE_SUFFIX = ".getAllAspects.";
	private static final String ASPECTS_CACHE_SUFFIX = ".getAspects";
	private static final String ALL_ASSOCIATIONS_CACHE_SUFFIX = ".getAllAssociations.";
	private static final String ASSOCIATIONS_CACHE_SUFFIX = ".getAssociations";
	private static final String DATA_TYPE_CACHE_SUFFIX = ".getDataType";
	private static final String TYPE_CACHE_SUFFIX = ".getType";
	private static final String ASPECT_CACHE_SUFFIX = ".getAspect";
	private static final String ANONYMOUS_TYPE_CACHE_SUFFIX = ".getAnonymousType";
	private static final String PROPERTY_CACHE_SUFFIX = ".getProperty";
	private static final String PROPERTY_DEFS_CACHE_SUFFIX = ".getPropertyDefs";
	private static final String ASSOCIATION_CACHE_SUFFIX = ".getAssociation";
	private static final String ALL_PROPERTIES_CACHE_SUFFIX = ".getAllProperties";
	private static final String PROPERTIES_CACHE_SUFFIX = ".getProperties";
	private static final String CONSTRAINT_CACHE_SUFFIX = ".getConstraint";
	private static final String CONSTRAINTS_CACHE_SUFFIX = ".getConstraints";
	private static final String MODEL_BY_URI_CACHE_SUFFIX = ".getModelByNamespaceUri";

	private static final String CACHE_SEPARATOR = "_";
	private static final String COMPILED_MODELS_CACHE = "compiledModelsCache";

	private DictionaryDAO dictionaryDAO;
	private MessageLookup messageLookup;
	
	protected BeCPGCacheService beCPGCacheService;
	protected AsynchronouslyRefreshedCacheRegistry registry;



	// Cache for frequently computed strings to avoid repeated computations
	private final Map<QName, String> prefixStringCache = new ConcurrentHashMap<>();
	private final Map<String, String> overrideKeyCache = new ConcurrentHashMap<>();

	public void setRegistry(AsynchronouslyRefreshedCacheRegistry registry) {
		this.registry = registry;
	}

	public void setBeCPGCacheService(BeCPGCacheService beCPGCacheService) {
		this.beCPGCacheService = beCPGCacheService;
	}

	public void setMessageLookup(MessageLookup messageLookup) {
		super.setMessageLookup(messageLookup);
		this.messageLookup = messageLookup;
	}
	

	public void setDictionaryDAO(DictionaryDAO dictionaryDAO) {
		super.setDictionaryDAO(dictionaryDAO);
		this.dictionaryDAO = dictionaryDAO;
	}

	@Override
	public Collection<QName> getSubTypes(QName superType, boolean follow) {
		String cacheKey = buildCacheKey(superType.toString(), SUB_TYPES_CACHE_SUFFIX, String.valueOf(follow));

		return beCPGCacheService.getFromCache(CachedDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			Collection<QName> result = dictionaryDAO.getSubTypes(superType, follow);
			return result != null ? Collections.unmodifiableCollection(result) : Collections.emptyList();
		});
	}

	@Override
	public Collection<QName> getSubAspects(QName superAspect, boolean follow) {
		String cacheKey = buildCacheKey(superAspect.toString(), SUB_ASPECTS_CACHE_SUFFIX, String.valueOf(follow));

		return beCPGCacheService.getFromCache(CachedDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			Collection<QName> result = dictionaryDAO.getSubAspects(superAspect, follow);
			return result != null ? Collections.unmodifiableCollection(result) : Collections.emptyList();
		});
	}

	@Override
	public boolean isSubClass(QName className, QName ofClassName) {
		if (className == null || ofClassName == null) {
			return false;
		}

		if (className.equals(ofClassName)) {
			return true;
		}

		String cacheKey = buildCacheKey(className.toString(), CACHE_SEPARATOR, ofClassName.toString(), IS_SUB_CLASS_CACHE_SUFFIX);

		return beCPGCacheService.getFromCache(CachedDictionaryServiceImpl.class.getName(), cacheKey, () -> computeIsSubClass(className, ofClassName));
	}

	@Override
	public void onRefreshableCacheEvent(RefreshableCacheEvent refreshableCacheEvent) {
		if (COMPILED_MODELS_CACHE.equals(refreshableCacheEvent.getCacheId())) {
			if (logger.isInfoEnabled()) {
				logger.info("Refreshing CachedDictionaryService cache: " + refreshableCacheEvent.getCacheId());
			}
			beCPGCacheService.clearCache(CachedDictionaryServiceImpl.class.getName());
			prefixStringCache.clear();
			overrideKeyCache.clear();
		}
	}

	@Override
	public String getCacheId() {
		return CachedDictionaryServiceImpl.class.getName();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		registry.register(this);
	}

	@Override
	public ClassDefinition getClass(QName name) {
		if (name == null) {
			return null;
		}
		String cacheKey = buildCacheKey(name.toString(), CLASS_DEF_CACHE_SUFFIX);

		return beCPGCacheService.getFromCache(CachedDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			return dictionaryDAO.getClass(name);
		});
	}

	// Private helper methods

	/**
	 * Efficiently builds cache keys by concatenating strings
	 */
	protected String buildCacheKey(String... parts) {
		if (parts.length == 1) {
			return parts[0];
		}
		if (parts.length == 2) {
			return parts[0] + parts[1];
		}

		// Estimate total length to avoid StringBuilder resizing
		int totalLength = 0;
		for (String part : parts) {
			if (part != null) {
				totalLength += part.length();
			}
		}
		
		StringBuilder sb = new StringBuilder(totalLength);
		for (String part : parts) {
			if (part != null) {
				sb.append(part);
			}
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.dictionary.DictionaryService#getAllModels()
	 */
	@Override
	public Collection<QName> getAllModels() {
		return getAllModels(true);
	}

	@Override
	public Collection<QName> getAllModels(boolean includeInherited) {
		String cacheKey = buildCacheKey(ALL_MODELS_CACHE_SUFFIX, String.valueOf(includeInherited));

		return beCPGCacheService.getFromCache(CachedDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			Collection<QName> result = dictionaryDAO.getModels(includeInherited);
			return result != null ? Collections.unmodifiableCollection(result) : Collections.emptyList();
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.dictionary.DictionaryService#getModel(org.alfresco.repo.ref.QName)
	 */
	@Override
	public ModelDefinition getModel(QName model) {
		if (model == null) {
			return null;
		}
		String cacheKey = buildCacheKey(model.toString(), MODEL_CACHE_SUFFIX);

		return beCPGCacheService.getFromCache(CachedDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			return dictionaryDAO.getModel(model);
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.dictionary.DictionaryService#getAllPropertyTypes()
	 */
	@Override
	public Collection<QName> getAllDataTypes() {
		String cacheKey = ALL_DATA_TYPES_CACHE_SUFFIX;

		return beCPGCacheService.getFromCache(CachedDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			Collection<QName> models = getAllModels();
			// Pre-size collection to avoid resizing
			Collection<QName> propertyTypes = new ArrayList<QName>(models.size() * 10); // Estimate
			for (QName model : models) {
				propertyTypes.addAll(getDataTypes(model));
			}
			return Collections.unmodifiableCollection(propertyTypes);
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.dictionary.DictionaryService#getPropertyTypes(org.alfresco.repo.ref.QName)
	 */
	@Override
	public Collection<QName> getDataTypes(QName model) {
		if (model == null) {
			return Collections.emptyList();
		}
		String cacheKey = buildCacheKey(model.toString(), DATA_TYPES_CACHE_SUFFIX);

		return beCPGCacheService.getFromCache(CachedDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			Collection<DataTypeDefinition> propertyTypes = dictionaryDAO.getDataTypes(model);
			Collection<QName> qnames = new ArrayList<QName>(propertyTypes.size());
			for (DataTypeDefinition def : propertyTypes) {
				qnames.add(def.getName());
			}
			return Collections.unmodifiableCollection(qnames);
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.dictionary.DictionaryService#getAllTypes()
	 */
	@Override
	public Collection<QName> getAllTypes() {
		return getAllTypes(true);
	}

	@Override
	public Collection<QName> getAllTypes(boolean includeInherited) {
		String cacheKey = buildCacheKey(ALL_TYPES_CACHE_SUFFIX, String.valueOf(includeInherited));

		return beCPGCacheService.getFromCache(CachedDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			Collection<QName> result = dictionaryDAO.getTypes(includeInherited);
			return result != null ? Collections.unmodifiableCollection(result) : Collections.emptyList();
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.dictionary.DictionaryService#getTypes(org.alfresco.repo.ref.QName)
	 */
	@Override
	public Collection<QName> getTypes(QName model) {
		if (model == null) {
			return Collections.emptyList();
		}
		String cacheKey = buildCacheKey(model.toString(), TYPES_CACHE_SUFFIX);

		return beCPGCacheService.getFromCache(CachedDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			Collection<TypeDefinition> types = dictionaryDAO.getTypes(model);
			Collection<QName> qnames = new ArrayList<QName>(types.size());
			for (TypeDefinition def : types) {
				qnames.add(def.getName());
			}
			return Collections.unmodifiableCollection(qnames);
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.dictionary.DictionaryService#getAllAspects()
	 */
	@Override
	public Collection<QName> getAllAspects() {
		return getAllAspects(true);
	}
	@Override
	public Collection<QName> getAllAspects(boolean includeInherited) {
		String cacheKey = buildCacheKey(ALL_ASPECTS_CACHE_SUFFIX, String.valueOf(includeInherited));

		return beCPGCacheService.getFromCache(CachedDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			Collection<QName> result = dictionaryDAO.getAspects(includeInherited);
			return result != null ? Collections.unmodifiableCollection(result) : Collections.emptyList();
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.dictionary.DictionaryService#getAllAssociations()
	 */
	@Override
	public Collection<QName> getAllAssociations() {
		return getAllAssociations(true);
	}

	@Override
	public Collection<QName> getAllAssociations(boolean includeInherited) {
		String cacheKey = buildCacheKey(ALL_ASSOCIATIONS_CACHE_SUFFIX, String.valueOf(includeInherited));

		return beCPGCacheService.getFromCache(CachedDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			Collection<QName> result = dictionaryDAO.getAssociations(includeInherited);
			return result != null ? Collections.unmodifiableCollection(result) : Collections.emptyList();
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.dictionary.DictionaryService#getAspects(org.alfresco.repo.ref.QName)
	 */
	@Override
	public Collection<QName> getAspects(QName model) {
		if (model == null) {
			return Collections.emptyList();
		}
		String cacheKey = buildCacheKey(model.toString(), ASPECTS_CACHE_SUFFIX);

		return beCPGCacheService.getFromCache(CachedDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			Collection<AspectDefinition> aspects = dictionaryDAO.getAspects(model);
			Collection<QName> qnames = new ArrayList<QName>(aspects.size());
			for (AspectDefinition def : aspects) {
				qnames.add(def.getName());
			}
			return Collections.unmodifiableCollection(qnames);
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.dictionary.DictionaryService#getAssociations(org.alfresco.repo.ref.QName)
	 */
	@Override
	public Collection<QName> getAssociations(QName model) {
		if (model == null) {
			return Collections.emptyList();
		}
		String cacheKey = buildCacheKey(model.toString(), ASSOCIATIONS_CACHE_SUFFIX);

		return beCPGCacheService.getFromCache(CachedDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			Collection<AssociationDefinition> associations = dictionaryDAO.getAssociations(model);
			Collection<QName> qnames = new ArrayList<QName>(associations.size());
			for (AssociationDefinition def : associations) {
				qnames.add(def.getName());
			}
			return Collections.unmodifiableCollection(qnames);
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.dictionary.DictionaryService#getPropertyType(org.alfresco.repo.ref.QName)
	 */
	@Override
	public DataTypeDefinition getDataType(QName name) {
		if (name == null) {
			return null;
		}
		String cacheKey = buildCacheKey(name.toString(), DATA_TYPE_CACHE_SUFFIX);

		return beCPGCacheService.getFromCache(CachedDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			return dictionaryDAO.getDataType(name);
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.service.cmr.dictionary.DictionaryService#getDataType(java.lang.Class)
	 */
	@Override
	public DataTypeDefinition getDataType(Class<?> javaClass) {
		if (javaClass == null) {
			return null;
		}
		String cacheKey = buildCacheKey(javaClass.getName(), DATA_TYPE_CACHE_SUFFIX);

		return beCPGCacheService.getFromCache(CachedDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			return dictionaryDAO.getDataType(javaClass);
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.dictionary.DictionaryService#getType(org.alfresco.repo.ref.QName)
	 */
	@Override
	public TypeDefinition getType(QName name) {
		if (name == null) {
			return null;
		}
		String cacheKey = buildCacheKey(name.toString(), TYPE_CACHE_SUFFIX);

		return beCPGCacheService.getFromCache(CachedDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			return dictionaryDAO.getType(name);
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.dictionary.DictionaryService#getAspect(org.alfresco.repo.ref.QName)
	 */
	@Override
	public AspectDefinition getAspect(QName name) {
		if (name == null) {
			return null;
		}
		String cacheKey = buildCacheKey(name.toString(), ASPECT_CACHE_SUFFIX);

		return beCPGCacheService.getFromCache(CachedDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			return dictionaryDAO.getAspect(name);
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.dictionary.DictionaryService#getAnonymousType(org.alfresco.repo.ref.QName, java.util.Collection)
	 */
	@Override
	public TypeDefinition getAnonymousType(QName type, Collection<QName> aspects) {
		if (type == null) {
			return null;
		}
		String aspectsKey = aspects != null ? aspects.toString() : "null";
		String cacheKey = buildCacheKey(type.toString(), CACHE_SEPARATOR, aspectsKey, ANONYMOUS_TYPE_CACHE_SUFFIX);

		return beCPGCacheService.getFromCache(CachedDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			return dictionaryDAO.getAnonymousType(type, aspects);
		});
	}

	/**
	 * 
	* {@inheritDoc}
	 */
	@Override
	public TypeDefinition getAnonymousType(QName name) {
		TypeDefinition typeDef = getType(name);
		List<AspectDefinition> aspects = typeDef.getDefaultAspects(true);
		List<QName> aspectNames = new ArrayList<QName>(aspects.size());
		getMandatoryAspects(typeDef, aspectNames);
		return getAnonymousType(typeDef.getName(), aspectNames);
	}

	/**
	 * Gets a flattened list of all mandatory aspects for a given class
	 * 
	 * @param classDef  the class
	 * @param aspects  a list to hold the mandatory aspects
	 */
	
	private void getMandatoryAspects(ClassDefinition classDef, List<QName> aspects) {
		for (AspectDefinition aspect : classDef.getDefaultAspects()) {
			QName aspectName = aspect.getName();
			if (!aspects.contains(aspectName)) {
				aspects.add(aspect.getName());
				getMandatoryAspects(aspect, aspects);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.dictionary.DictionaryService#getProperty(org.alfresco.repo.ref.QName, org.alfresco.repo.ref.QName)
	 */
	@Override
	public PropertyDefinition getProperty(QName className, QName propertyName) {
		if (className == null || propertyName == null) {
			return null;
		}
		String cacheKey = buildCacheKey(className.toString(), CACHE_SEPARATOR, propertyName.toString(), PROPERTY_CACHE_SUFFIX);

		return beCPGCacheService.getFromCache(CachedDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			PropertyDefinition propDef = null;
			ClassDefinition classDef = dictionaryDAO.getClass(className);
			if (classDef != null) {
				Map<QName, PropertyDefinition> propDefs = classDef.getProperties();
				propDef = propDefs.get(propertyName);
			}
			return propDef;
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.service.cmr.dictionary.DictionaryService#getPropertyDefs(org.alfresco.service.namespace.QName)
	 */
	@Override
	public Map<QName, PropertyDefinition> getPropertyDefs(QName className) {
		if (className == null) {
			return null;
		}
		String cacheKey = buildCacheKey(className.toString(), PROPERTY_DEFS_CACHE_SUFFIX);

		return beCPGCacheService.getFromCache(CachedDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			ClassDefinition classDef = dictionaryDAO.getClass(className);
			if (classDef != null) {
				return classDef.getProperties();
			}
			return null;
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.dictionary.DictionaryService#getProperty(org.alfresco.repo.ref.QName)
	 */
	@Override
	public PropertyDefinition getProperty(QName propertyName) {
		if (propertyName == null) {
			return null;
		}
		String cacheKey = buildCacheKey(propertyName.toString(), PROPERTY_CACHE_SUFFIX);

		return beCPGCacheService.getFromCache(CachedDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			return dictionaryDAO.getProperty(propertyName);
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.dictionary.DictionaryService#getAssociation(org.alfresco.repo.ref.QName)
	 */
	@Override
	public AssociationDefinition getAssociation(QName associationName) {
		if (associationName == null) {
			return null;
		}
		String cacheKey = buildCacheKey(associationName.toString(), ASSOCIATION_CACHE_SUFFIX);

		return beCPGCacheService.getFromCache(CachedDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			return dictionaryDAO.getAssociation(associationName);
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.service.cmr.dictionary.DictionaryService#getAllProperties(org.alfresco.service.namespace.QName)
	 */
	@Override
	public Collection<QName> getAllProperties(QName dataType) {
		if (dataType == null) {
			return Collections.emptyList();
		}
		String cacheKey = buildCacheKey(dataType.toString(), ALL_PROPERTIES_CACHE_SUFFIX);

		return beCPGCacheService.getFromCache(CachedDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			Collection<QName> models = getAllModels();
			// Use HashSet for better performance and automatic deduplication
			Collection<QName> properties = new HashSet<QName>(models.size() * 20); // Estimate
			for (QName model : models) {
				properties.addAll(getProperties(model, dataType));
			}
			return Collections.unmodifiableCollection(properties);
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.service.cmr.dictionary.DictionaryService#getAllProperties(org.alfresco.service.namespace.QName, org.alfresco.service.namespace.QName)
	 */
	@Override
	public Collection<QName> getProperties(QName model, QName dataType) {
		if (model == null) {
			return Collections.emptyList();
		}
		String dataTypeKey = dataType != null ? dataType.toString() : "null";
		String cacheKey = buildCacheKey(model.toString(), CACHE_SEPARATOR, dataTypeKey, PROPERTIES_CACHE_SUFFIX);

		return beCPGCacheService.getFromCache(CachedDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			Collection<PropertyDefinition> propDefs = dictionaryDAO.getProperties(model, dataType);
			HashSet<QName> props = new HashSet<QName>(propDefs.size());
			for (PropertyDefinition def : propDefs) {
				props.add(def.getName());
			}
			return Collections.unmodifiableCollection(props);
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.service.cmr.dictionary.DictionaryService#getProperties(org.alfresco.service.namespace.QName)
	 */
	@Override
	public Collection<QName> getProperties(QName model) {
		if (model == null) {
			return Collections.emptyList();
		}
		String cacheKey = buildCacheKey(model.toString(), PROPERTIES_CACHE_SUFFIX);

		return beCPGCacheService.getFromCache(CachedDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			Collection<PropertyDefinition> propDefs = dictionaryDAO.getProperties(model);
			HashSet<QName> props = new HashSet<QName>(propDefs.size());
			for (PropertyDefinition def : propDefs) {
				props.add(def.getName());
			}
			return Collections.unmodifiableCollection(props);
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.service.cmr.dictionary.DictionaryService#getConstraint(org.alfresco.service.namespace.QName)
	 */
	@Override
	public ConstraintDefinition getConstraint(QName constraintQName) {
		if (constraintQName == null) {
			return null;
		}
		String cacheKey = buildCacheKey(constraintQName.toString(), CONSTRAINT_CACHE_SUFFIX);

		return beCPGCacheService.getFromCache(CachedDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			return dictionaryDAO.getConstraint(constraintQName);
		});
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.service.cmr.dictionary.DictionaryService#getConstraints(org.alfresco.service.namespace.QName)
	 */
	public Collection<ConstraintDefinition> getConstraints(QName model) {
		if (model == null) {
			return Collections.emptyList();
		}
		String cacheKey = buildCacheKey(model.toString(), CONSTRAINTS_CACHE_SUFFIX);

		return beCPGCacheService.getFromCache(CachedDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			Collection<ConstraintDefinition> result = dictionaryDAO.getConstraints(model);
			return result != null ? Collections.unmodifiableCollection(result) : Collections.emptyList();
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.service.cmr.dictionary.DictionaryService#getConstraints(org.alfresco.service.namespace.QName, boolean)
	 */
	public Collection<ConstraintDefinition> getConstraints(QName model, boolean referenceableDefsOnly) {
		if (model == null) {
			return Collections.emptyList();
		}
		String cacheKey = buildCacheKey(model.toString(), CACHE_SEPARATOR, String.valueOf(referenceableDefsOnly), CONSTRAINTS_CACHE_SUFFIX);

		return beCPGCacheService.getFromCache(CachedDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			Collection<ConstraintDefinition> result = dictionaryDAO.getConstraints(model, referenceableDefsOnly);
			return result != null ? Collections.unmodifiableCollection(result) : Collections.emptyList();
		});
	}

	public void init() {
		dictionaryDAO.init();
	}

	public void destroy() {
		dictionaryDAO.destroy();
	}

	public void onEnableTenant() {
		dictionaryDAO.reset(); 
	}

	public void onDisableTenant() {
		dictionaryDAO.destroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.i18n.MessageLookup#getMessage(java.lang.String)
	 */
	@Override
	public String getMessage(String messageKey) {
		return messageLookup.getMessage(messageKey);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.i18n.MessageLookup#getMessage(java.lang.String, java.util.Locale)
	 */
	@Override
	public String getMessage(String messageKey, Locale locale) {
		return messageLookup.getMessage(messageKey, locale);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.i18n.MessageLookup#getMessage(java.lang.String, java.lang.Object[])
	 */
	@Override
	public String getMessage(String messageKey, Object... params) {
		return messageLookup.getMessage(messageKey, params);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.i18n.MessageLookup#getMessage(java.lang.String, java.util.Locale, java.lang.Object[])
	 */
	@Override
	public String getMessage(String messageKey, Locale locale, Object... params) {
		return messageLookup.getMessage(messageKey, locale, params);
	}

	@Override
	public ModelDefinition getModelByNamespaceUri(String uri) {
		if (uri == null) {
			return null;
		}
		String cacheKey = buildCacheKey(uri, MODEL_BY_URI_CACHE_SUFFIX);

		return beCPGCacheService.getFromCache(CachedDictionaryServiceImpl.class.getName(), cacheKey, () -> {
			// Use cached models to avoid repeated DAO calls
			Collection<QName> models = getAllModels();
			for (QName modelQname : models) {
				if (uri.equals(modelQname.getNamespaceURI())) {
					return getModel(modelQname);
				}
			}
			return null;
		});
	}

}