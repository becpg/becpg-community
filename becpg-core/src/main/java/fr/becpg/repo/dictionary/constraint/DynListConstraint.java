/*
 *  Copyright (C) 2010-2021 beCPG. All rights reserved.
 */
package fr.becpg.repo.dictionary.constraint;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.ConstraintException;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.InvalidStoreRefException;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.InvalidQNameException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SecurityModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.search.impl.AbstractBeCPGQueryBuilder;

/**
 * Class used to load the dynamic constraints.
 *
 * @author querephi, matthieu
 * @version $Id: $Id
 */
public class DynListConstraint extends ListOfValuesConstraint {

	/** Constant <code>UNDIFINED_CONSTRAINT_VALUE="-"</code> */
	public static final String UNDIFINED_CONSTRAINT_VALUE = "-";

	private static final String ERR_NO_VALUES = "d_dictionary.constraint.list_of_values.no_values";
	private static final String ERR_NON_STRING = "d_dictionary.constraint.string_length.non_string";
	private static final String ERR_INVALID_VALUE = "d_dictionary.constraint.list_of_values.invalid_value";

	private static final Log logger = LogFactory.getLog(DynListConstraint.class);

	private static final String CLASSPATH_PREFIX = "classpath:";
	private static final String REPO_PREFIX = "repo:";

	private static ServiceRegistry serviceRegistry;

	private static BeCPGCacheService beCPGCacheService;
	
	private static ContentService contentService;

	private static Set<String> pathRegistry = new HashSet<>();

	private List<String> paths = null;

	private String constraintType = "bcpg:listValue";
	private String constraintProp = "bcpg:lvValue";
	private String constraintFilterProp = "bcpg:lvType";
	private String constraintCode = null;
	private String constraintFilter = null;

	private String level = null;
	private String levelProp = null;

	private Boolean addEmptyValue = null;

	private List<String> allowedValuesSuffix = null;
	
	/**
	 * <p>Setter for the field <code>contentService</code>.</p>
	 *
	 * @param contentService a {@link org.alfresco.service.cmr.repository.ContentService} object
	 */
	public static void setContentService(ContentService contentService) {
		DynListConstraint.contentService = contentService;
	}

	/**
	 * <p>Setter for the field <code>allowedValuesSuffix</code>.</p>
	 *
	 * @param allowedValuesSuffix a {@link java.util.List} object
	 */
	public void setAllowedValuesSuffix(List<String> allowedValuesSuffix) {
		this.allowedValuesSuffix = allowedValuesSuffix;
	}

	/**
	 * <p>Getter for the field <code>paths</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	public List<String> getPaths() {
		return paths;
	}

	/**
	 * <p>setPath.</p>
	 *
	 * @param paths a {@link java.util.List} object.
	 */
	public void setPath(List<String> paths) {

		if (paths == null) {
			throw new DictionaryException(ERR_NO_VALUES);
		}
		int valueCount = paths.size();
		if (valueCount == 0) {
			throw new DictionaryException(ERR_NO_VALUES);
		}
		this.paths = paths;

		for (String path : paths) {
			if (!path.startsWith(CLASSPATH_PREFIX) && !path.startsWith(REPO_PREFIX)) {
				pathRegistry.add("/app:company_home/" + AbstractBeCPGQueryBuilder.encodePath(path));
			}
		}
	}

	/**
	 * <p>Setter for the field <code>serviceRegistry</code>.</p>
	 *
	 * @param serviceRegistry a {@link org.alfresco.service.ServiceRegistry} object.
	 */
	public static void setServiceRegistry(ServiceRegistry serviceRegistry) {
		DynListConstraint.serviceRegistry = serviceRegistry;
	}

	/**
	 * <p>Setter for the field <code>beCPGCacheService</code>.</p>
	 *
	 * @param beCPGCacheService a {@link fr.becpg.repo.cache.BeCPGCacheService} object.
	 */
	public static void setBeCPGCacheService(BeCPGCacheService beCPGCacheService) {
		DynListConstraint.beCPGCacheService = beCPGCacheService;
	}

	/**
	 * <p>Setter for the field <code>constraintType</code>.</p>
	 *
	 * @param constraintType a {@link java.lang.String} object.
	 */
	public void setConstraintType(String constraintType) {
		this.constraintType = constraintType;
	}

	/**
	 * <p>Setter for the field <code>constraintProp</code>.</p>
	 *
	 * @param constraintProp a {@link java.lang.String} object.
	 */
	public void setConstraintProp(String constraintProp) {
		this.constraintProp = constraintProp;
	}

	/**
	 * <p>Setter for the field <code>constraintCode</code>.</p>
	 *
	 * @param constraintCode a {@link java.lang.String} object.
	 */
	public void setConstraintCode(String constraintCode) {
		this.constraintCode = constraintCode;
	}

	/**
	 * <p>Setter for the field <code>constraintFilterProp</code>.</p>
	 *
	 * @param constraintFilterProp a {@link java.lang.String} object
	 */
	public void setConstraintFilterProp(String constraintFilterProp) {
		this.constraintFilterProp = constraintFilterProp;
	}

	/**
	 * <p>Setter for the field <code>constraintFilter</code>.</p>
	 *
	 * @param constraintFilter a {@link java.lang.String} object
	 */
	public void setConstraintFilter(String constraintFilter) {
		this.constraintFilter = constraintFilter;
	}

	/**
	 * <p>Setter for the field <code>level</code>.</p>
	 *
	 * @param level a {@link java.lang.String} object.
	 */
	public void setLevel(String level) {
		this.level = level;
	}

	/**
	 * <p>Setter for the field <code>levelProp</code>.</p>
	 *
	 * @param levelProp a {@link java.lang.String} object.
	 */
	public void setLevelProp(String levelProp) {
		this.levelProp = levelProp;
	}

	/**
	 * <p>Setter for the field <code>addEmptyValue</code>.</p>
	 *
	 * @param addEmptyValue a {@link java.lang.Boolean} object.
	 */
	public void setAddEmptyValue(Boolean addEmptyValue) {
		this.addEmptyValue = addEmptyValue;
	}

	/**
	 * <p>Getter for the field <code>pathRegistry</code>.</p>
	 *
	 * @return a {@link java.util.Set} object
	 */
	public static Set<String> getPathRegistry() {
		return pathRegistry;
	}

	/** {@inheritDoc} */
	@Override
	public void initialize() {
		checkPropertyNotNull("paths", paths);
		checkPropertyNotNull("constraintType", constraintType);
		checkPropertyNotNull("constraintProp", constraintProp);
		if (level != null) {
			checkPropertyNotNull("levelProp", levelProp);
		}
		logger.debug("Initialize DynListConstraint for " + paths + " " + constraintType);

	}

	/** {@inheritDoc} */
	@Override
	public List<String> getAllowedValues() {
		return getAllowedValues(true);
	}

	/**
	 * <p>getAllowedValues.</p>
	 *
	 * @param filter a boolean
	 * @return a {@link java.util.List} object
	 */
	public List<String> getAllowedValues(boolean filter) {

		Map<String, DynListEntry> values = getDynListEntries();

		if (values.isEmpty()) {
			return Collections.singletonList(UNDIFINED_CONSTRAINT_VALUE);
		}

		List<String> allowedValues = null;

		if (filter) {

			String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();

			allowedValues = AuthenticationUtil.runAsSystem(() -> {
				List<String> valuesToReturn = new ArrayList<>();
				for (Map.Entry<String, DynListEntry> entry : values.entrySet()) {
					if (!Boolean.TRUE.equals(entry.getValue().getIsDeleted()) && ((currentUser == null) || entry.getValue().getGroups() ==null || entry.getValue().getGroups().isEmpty()
							|| entry.getValue().getGroups().stream().anyMatch(key -> serviceRegistry.getAuthorityService()
									.getContainedAuthorities(AuthorityType.USER, key, false).contains(currentUser)))) {
						valuesToReturn.add(entry.getKey());

					}
				}

				return valuesToReturn;
			});

		} else {
			allowedValues = new ArrayList<>(values.keySet());
		}

		if (allowedValuesSuffix != null) {
			List<String> valuesToReturnWithSuffixes = new ArrayList<>();
			allowedValues.forEach(value -> allowedValuesSuffix.forEach(suffix -> valuesToReturnWithSuffixes.add(value + suffix)));
			return valuesToReturnWithSuffixes;
		}

		return allowedValues;
	}

	/** {@inheritDoc} */
	@Override
	protected void evaluateSingleValue(Object value) {
		// convert the value to a String
		String valueStr;
		try {
			valueStr = DefaultTypeConverter.INSTANCE.convert(String.class, value);
		} catch (TypeConversionException e) {
			throw new ConstraintException(ERR_NON_STRING, value);
		}

		if (!getAllowedValues(false).contains(valueStr)) {
			throw new ConstraintException(ERR_INVALID_VALUE, value);
		}

	}

	/**
	 * <p>getDisplayLabel.</p>
	 *
	 * @param constraintAllowableValue a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getDisplayLabel(String constraintAllowableValue) {

		return getDisplayLabel(constraintAllowableValue, I18NUtil.getLocale());
	}

	/**
	 * <p>getMLDisplayLabel.</p>
	 *
	 * @param constraintAllowableValue a {@link java.lang.String} object
	 * @return a {@link org.alfresco.service.cmr.repository.MLText} object
	 */
	public MLText getMLDisplayLabel(String constraintAllowableValue) {

		DynListEntry entry = getDynListEntries().get(constraintAllowableValue);

		if (entry != null) {
			return entry.getValues();
		}

		return new MLText();
	}

	/**
	 * <p>getDisplayLabel.</p>
	 *
	 * @param constraintAllowableValue a {@link java.lang.String} object.
	 * @param locale a {@link java.util.Locale} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getDisplayLabel(String constraintAllowableValue, Locale locale) {

		MLText mlText = getMLDisplayLabel(constraintAllowableValue);

		String ret = MLTextHelper.getClosestValue(mlText, locale);

		if ((ret != null) && !ret.isEmpty()) {
			return ret;
		}

		return constraintAllowableValue;
	}

	/** {@inheritDoc} */
	@Override
	public String getDisplayLabel(String constraintAllowableValue, MessageLookup messageLookup) {
		return getDisplayLabel(constraintAllowableValue);
	}

	private Map<String, DynListEntry> getDynListEntries() {
		return beCPGCacheService.getFromCache(DynListConstraint.class.getName(), createCacheKey(),
				() -> serviceRegistry.getRetryingTransactionHelper().doInTransaction(() -> {
					logger.debug("Fill allowedValues for: " + TenantUtil.getCurrentDomain()+ " "+ createCacheKey());
					Map<String, DynListEntry> allowedValues = new LinkedHashMap<>();

					if (Boolean.TRUE.equals(addEmptyValue)) {
						allowedValues.put("", new DynListEntry());
					}

					AuthenticationUtil.runAsSystem(() -> {
						for (String path : paths) {
							if (path.startsWith(CLASSPATH_PREFIX)) {
								processClasspathResource(path, allowedValues);
							} else if (path.startsWith(REPO_PREFIX)) {
								processRepoResource(path, allowedValues);
							} else {
								processSystemList(path, allowedValues);
							}
						}
						return null;
					});

					return allowedValues;
				}, true, false));
	}

	private void processClasspathResource(String path, Map<String, DynListEntry> allowedValues) {
		ClassPathResource resource = new ClassPathResource(path.replace(CLASSPATH_PREFIX, ""));
		try (InputStream in = resource.getInputStream()) {
			processCsvInputStream(allowedValues, in);
		} catch (IOException e) {
			logger.error(e, e);
		}
	}

	private void processRepoResource(String path, Map<String, DynListEntry> allowedValues) {
		NodeRef result = BeCPGQueryBuilder.createQuery().selectNodeByPath(path.replace(REPO_PREFIX, ""));
		if (result != null ) {
			ContentReader reader = contentService.getReader(result, ContentModel.PROP_CONTENT);
			processCsvInputStream(allowedValues, reader.getContentInputStream());
		}
	}

	private void processCsvInputStream(Map<String, DynListEntry> allowedValues, InputStream in) {
		try (InputStreamReader inReader = new InputStreamReader(in);
				CSVParser csvParser = CSVFormat.DEFAULT.builder()
						.setAllowMissingColumnNames(true)						
						.setDelimiter(';').setQuote('"').setHeader().setSkipHeaderRecord(true).build()
						.parse(inReader)) {

			Set<String> locales = new HashSet<>();
			String[] headers = csvParser.getHeaderMap().keySet().toArray(new String[0]);

			for (String header : headers) {
				if (header.startsWith(constraintProp + "_")) {
					locales.add(header.split("_", 2)[1]);
				}
			}

			boolean found = false;
			for (CSVRecord csvRecord : csvParser) {
				String key = getKeyFromRecord(csvRecord);
				String filterPropValue = csvRecord.get(constraintFilterProp);

				if (isValidEntry(key, filterPropValue)) {
					found = true;
					DynListEntry entry = new DynListEntry();
					MLText mlText = new MLText();

					if (csvRecord.get(constraintProp) != null) {
						mlText.addValue(Locale.getDefault(), csvRecord.get(constraintProp));
					}

					for (String locale : locales) {
						mlText.addValue(MLTextHelper.parseLocale(locale), csvRecord.get(constraintProp + "_" + locale));
					}

					entry.setCode(getKeyFromRecord(csvRecord));
					entry.setValues(mlText);

					allowedValues.put(key, entry);
				}
			}
			if (!found) {
				logger.error("constraintFilter not found: " + constraintFilter);
			}
		} catch (IOException e) {
			logger.error(e, e);
		}
	}
	
	private void processSystemList(String path, Map<String, DynListEntry> allowedValues) {

		NamespaceService namespaceService = serviceRegistry.getNamespaceService();
		QName constraintTypeQname = QName.createQName(constraintType, namespaceService);
		QName constraintPropQname = QName.createQName(constraintProp, namespaceService);
		QName constraintCodeQname = null;
		if (constraintCode != null) {
			constraintCodeQname = QName.createQName(constraintCode, namespaceService);
		} else if (BeCPGModel.TYPE_LIST_VALUE.equals(constraintTypeQname)) {
			constraintCodeQname = BeCPGModel.PROP_LV_CODE;
		}

		boolean wasMLAware = MLPropertyInterceptor.setMLAware(true);
		try {

			List<NodeRef> nodeRefs = BeCPGQueryBuilder.createQuery().selectNodesByPath(
					serviceRegistry.getNodeService().getRootNode(RepoConsts.SPACES_STORE),
					"/app:company_home/" + AbstractBeCPGQueryBuilder.encodePath(path) + "/*");

			sortNodeRefs(nodeRefs);

			for (NodeRef nodeRef : nodeRefs) {
				if (serviceRegistry.getNodeService().exists(nodeRef)
						&& serviceRegistry.getNodeService().getType(nodeRef).equals(constraintTypeQname)) {

					MLText mlText = (MLText) serviceRegistry.getNodeService().getProperty(nodeRef, constraintPropQname);
					if (mlText != null) {

						String key = null;

						if (constraintCodeQname != null) {
							key = (String) serviceRegistry.getNodeService().getProperty(nodeRef, constraintCodeQname);

						}
						if ((key == null) || key.isEmpty()) {
							key = MLTextHelper.getClosestValue(mlText, Locale.getDefault());
						}

						if (key != null) {

							DynListEntry entry = new DynListEntry();
							entry.setCode(key);
							entry.setGroups(serviceRegistry.getNodeService().getTargetAssocs(nodeRef, SecurityModel.ASSOC_READ_GROUPS).stream()
									.map(AssociationRef::getTargetRef)
									.map(n -> (String) serviceRegistry.getNodeService().getProperty(n, ContentModel.PROP_AUTHORITY_NAME))
									.toList());
							entry.setValues(mlText);
							entry.setIsDeleted((Boolean) serviceRegistry.getNodeService().getProperty(nodeRef, BeCPGModel.PROP_IS_DELETED));

							allowedValues.put(key, entry);
						}

					}
				} else {
					logger.warn("Node : " + nodeRef+ " for "+ "/app:company_home/" + AbstractBeCPGQueryBuilder.encodePath(path) + "/*");
					if(serviceRegistry.getNodeService().exists(nodeRef)) {
						logger.warn(" - Doesn't have the expected type  : " + constraintTypeQname+ " / type: "+ serviceRegistry.getNodeService().getType(nodeRef));
					}  else {
						logger.warn(" - Doesn't exists" );
					}
				}
			}

			if (logger.isDebugEnabled()) {
				logger.debug("allowedValues.size() : " + allowedValues.size());
				logger.debug("allowed values: " + allowedValues.toString());
			}
		} catch (InvalidStoreRefException | InvalidQNameException e) {
			logger.warn("Please reload constraint once tenant created: " + e.getMessage());
		} finally {
			MLPropertyInterceptor.setMLAware(wasMLAware);
		}
	}

	private void sortNodeRefs(List<NodeRef> nodeRefs) {
		nodeRefs.sort((o1, o2) -> {
			Integer sort1 = (Integer) serviceRegistry.getNodeService().getProperty(o1, BeCPGModel.PROP_SORT);
			Integer sort2 = (Integer) serviceRegistry.getNodeService().getProperty(o2, BeCPGModel.PROP_SORT);

			if ((sort1 == null) && (sort2 == null)) {
				return 0;
			}
			if (sort1 == null) {
				return -1;
			}
			if (sort2 == null) {
				return 1;
			}

			return sort1.compareTo(sort2);
		});
	}

	private String getKeyFromRecord(CSVRecord csvRecord) {
		return csvRecord.get((constraintCode != null) ? constraintCode : "bcpg:lvCode");
	}

	private boolean isValidEntry(String key, String filterPropValue) {
		return (key != null) && (filterPropValue != null)
				&& ((constraintFilter == null) || constraintFilter.equals("*") || filterPropValue.equals(constraintFilter));
	}

	private String createCacheKey() {
		return getShortName() + (constraintFilter != null ? "_" + constraintFilter : "");
	}

}
