/*
 *  Copyright (C) 2010-2016 beCPG. All rights reserved.
 */
package fr.becpg.repo.dictionary.constraint;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.search.impl.AbstractBeCPGQueryBuilder;

/**
 * Class used to load the dynamic constraints.
 *
 * @author querephi, matthieu
 */
public class DynListConstraint extends ListOfValuesConstraint {

	public static final String DYN_LIST_CACHE_NAME = "DynListConstraintCache";
	public static final String UNDIFINED_CONSTRAINT_VALUE = "-";

	private static final String ERR_NO_VALUES = "d_dictionary.constraint.list_of_values.no_values";
	private static final String ERR_NON_STRING = "d_dictionary.constraint.string_length.non_string";
	private static final String ERR_INVALID_VALUE = "d_dictionary.constraint.list_of_values.invalid_value";

	private static final Log logger = LogFactory.getLog(DynListConstraint.class);

	private static ServiceRegistry serviceRegistry;

	private static BeCPGCacheService beCPGCacheService;

	private List<String> paths = null;

	private String constraintType = null;
	private String constraintProp = null;
	private String constraintCode = null;

	private String level = null;
	private String levelProp = null;

	private Boolean addEmptyValue = null;

	public void setPath(List<String> paths) {

		if (paths == null) {
			throw new DictionaryException(ERR_NO_VALUES);
		}
		int valueCount = paths.size();
		if (valueCount == 0) {
			throw new DictionaryException(ERR_NO_VALUES);
		}
		this.paths = paths;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		DynListConstraint.serviceRegistry = serviceRegistry;
	}

	public void setBeCPGCacheService(BeCPGCacheService beCPGCacheService) {
		DynListConstraint.beCPGCacheService = beCPGCacheService;
	}

	public void setConstraintType(String constraintType) {
		this.constraintType = constraintType;
	}

	public void setConstraintProp(String constraintProp) {
		this.constraintProp = constraintProp;
	}

	public void setConstraintCode(String constraintCode) {
		this.constraintCode = constraintCode;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public void setLevelProp(String levelProp) {
		this.levelProp = levelProp;
	}

	public void setAddEmptyValue(Boolean addEmptyValue) {
		this.addEmptyValue = addEmptyValue;
	}

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

	@Override
	public List<String> getAllowedValues() {

		Map<String, MLText> values = getMLAwareAllowedValues();

		if (values.isEmpty()) {
			return Collections.singletonList(UNDIFINED_CONSTRAINT_VALUE);
		} else {
			return new LinkedList<String>(values.keySet());
		}

	}

	@Override
	protected void evaluateSingleValue(Object value) {
		// convert the value to a String
		String valueStr;
		try {
			valueStr = DefaultTypeConverter.INSTANCE.convert(String.class, value);
		} catch (TypeConversionException e) {
			throw new ConstraintException(ERR_NON_STRING, value);
		}

		if (!getAllowedValues().contains(valueStr)) {
			throw new ConstraintException(ERR_INVALID_VALUE, value);
		}

	}

	public String getDisplayLabel(String constraintAllowableValue) {

		return getDisplayLabel(constraintAllowableValue,I18NUtil.getLocale());
	}
	
	public String getDisplayLabel(String constraintAllowableValue, Locale locale) {

		MLText mlText = getMLAwareAllowedValues().get(constraintAllowableValue);

		if ((mlText != null) && (mlText.getClosestValue(locale) != null) && !mlText.getClosestValue(locale).isEmpty()) {
			return mlText.getClosestValue(locale);
		}

		return constraintAllowableValue;
	}

	@Override
	public String getDisplayLabel(String constraintAllowableValue, MessageLookup messageLookup) {
		return getDisplayLabel(constraintAllowableValue);
	}

	private Map<String, MLText> getMLAwareAllowedValues() {
		return beCPGCacheService.getFromCache(DYN_LIST_CACHE_NAME, getShortName(),
				() -> serviceRegistry.getTransactionService().getRetryingTransactionHelper().doInTransaction(() -> {

					logger.debug("Fill allowedValues  for :" + TenantUtil.getCurrentDomain());

					Map<String, MLText> allowedValues = new LinkedHashMap<String, MLText>();

					if ((addEmptyValue != null) && addEmptyValue) {
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
											String key = null;

											if (constraintCodeQname != null) {
												key = (String) serviceRegistry.getNodeService().getProperty(nodeRef, constraintCodeQname);

											}
											if ((key == null) || key.isEmpty()) {
												key = mlText.getClosestValue(Locale.getDefault());
											}

											allowedValues.put(key, mlText);
										}
									} else {
										logger.warn("Node doesn't exist : " + nodeRef);
									}
								}

								if (logger.isDebugEnabled()) {
									logger.debug("allowedValues.size() : " + allowedValues.size());
									logger.debug("allowed values: " + allowedValues.toString());
								}
							} catch (InvalidStoreRefException e) {
								logger.warn("Please reload constraint once tenant created: " + e.getMessage());
							} finally {
								MLPropertyInterceptor.setMLAware(wasMLAware);
							}
						}
						return null;

					});

					return allowedValues;

				} , true, false));
	}

}
