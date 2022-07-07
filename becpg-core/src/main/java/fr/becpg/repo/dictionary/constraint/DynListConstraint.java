/*
 *  Copyright (C) 2010-2021 beCPG. All rights reserved.
 */
package fr.becpg.repo.dictionary.constraint;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.ConstraintException;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.alfresco.service.cmr.repository.InvalidStoreRefException;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.alfresco.service.namespace.InvalidQNameException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
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

	private static ServiceRegistry serviceRegistry;

	private static BeCPGCacheService beCPGCacheService;
	
	private static Set<String> pathRegistry = new HashSet<>();
	
	private List<String> paths = null;

	private String constraintType = null;
	private String constraintProp = null;
	private String constraintCode = null;

	private String level = null;
	private String levelProp = null;

	private Boolean addEmptyValue = null;

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
			pathRegistry.add("/app:company_home/" + AbstractBeCPGQueryBuilder.encodePath(path));
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
		return getAllowedValues(false);
	}

	public List<String> getAllowedValues(boolean includeDeletedValues) {

		Map<String, MLText> values = getMLAwareAllowedValuesInternal(includeDeletedValues);

		if (values.isEmpty()) {
			return Collections.singletonList(UNDIFINED_CONSTRAINT_VALUE);
		} else {
			return new LinkedList<>(values.keySet());
		}

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

		if (!getAllowedValues(true).contains(valueStr)) {
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
	 * <p>getDisplayLabel.</p>
	 *
	 * @param constraintAllowableValue a {@link java.lang.String} object.
	 * @param locale a {@link java.util.Locale} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getDisplayLabel(String constraintAllowableValue, Locale locale) {

		MLText mlText = getMLAwareAllowedValues().get(constraintAllowableValue);

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

	/**
	 * <p>getMLAwareAllowedValues.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<String, MLText> getMLAwareAllowedValues() {
		return getMLAwareAllowedValuesInternal(false);
	}

	private Map<String, MLText> getMLAwareAllowedValuesInternal(boolean includeDeletedValues) {
		return beCPGCacheService.getFromCache(DynListConstraint.class.getName(), getShortName() + includeDeletedValues,
				() -> serviceRegistry.getRetryingTransactionHelper().doInTransaction(() -> {

					logger.debug("Fill allowedValues  for :" + TenantUtil.getCurrentDomain());

					Map<String, MLText> allowedValues = new LinkedHashMap<>();

					if (Boolean.TRUE.equals(addEmptyValue)) {
						allowedValues.put("", null);
					}

					AuthenticationUtil.runAsSystem(() -> {

						NamespaceService namespaceService = serviceRegistry.getNamespaceService();
						QName constraintTypeQname = QName.createQName(constraintType, namespaceService);
						QName constraintPropQname = QName.createQName(constraintProp, namespaceService);
						QName constraintCodeQname = null;
						if (constraintCode != null) {
							constraintCodeQname = QName.createQName(constraintCode, namespaceService);
						} else if (BeCPGModel.TYPE_LIST_VALUE.equals(constraintTypeQname)) {
							constraintCodeQname = BeCPGModel.PROP_LV_CODE;
						}

						for (String path : paths) {
							
							boolean wasMLAware = MLPropertyInterceptor.setMLAware(true);
							try {
								
								List<NodeRef> nodeRefs = BeCPGQueryBuilder.createQuery().selectNodesByPath(
										serviceRegistry.getNodeService().getRootNode(RepoConsts.SPACES_STORE),
										"/app:company_home/" + AbstractBeCPGQueryBuilder.encodePath(path) + "/*");

								Collections.sort(nodeRefs, (o1, o2) -> {
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

								for (NodeRef nodeRef : nodeRefs) {
									if (serviceRegistry.getNodeService().exists(nodeRef)
											&& serviceRegistry.getNodeService().getType(nodeRef).equals(constraintTypeQname)) {
										
										MLText mlText = (MLText) serviceRegistry.getNodeService().getProperty(nodeRef, constraintPropQname);
										if (mlText != null) {

											if (includeDeletedValues || !Boolean.TRUE.equals(serviceRegistry.getNodeService().getProperty(nodeRef, BeCPGModel.PROP_IS_DELETED))) {
												String key = null;
												
												if (constraintCodeQname != null) {
													key = (String) serviceRegistry.getNodeService().getProperty(nodeRef, constraintCodeQname);
													
												}
												if ((key == null) || key.isEmpty()) {
													key = mlText.getClosestValue(Locale.getDefault());
												}
												allowedValues.put(key, mlText);
											}
										}
									} else {
										logger.warn("Node doesn't exist : " + nodeRef);
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
						return null;

					});

					return allowedValues;

				}, true, false));
	}

}
