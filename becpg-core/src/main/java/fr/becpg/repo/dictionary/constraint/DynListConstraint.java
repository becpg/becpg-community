/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.dictionary.constraint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.PermissionEvaluationMode;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.LuceneHelper;

// TODO: Auto-generated Javadoc
/**
 * Class used to load the dynamic constraints.
 * 
 * @author querephi
 */
public class DynListConstraint extends ListOfValuesConstraint {

	/** The Constant ERR_NO_VALUES. */
	private static final String ERR_NO_VALUES = "d_dictionary.constraint.list_of_values.no_values";


	/** The Constant UNDIFINED_CONSTRAINT_VALUE. */
	public static final String UNDIFINED_CONSTRAINT_VALUE = "-";

	/** The logger. */
	private static Log logger = LogFactory.getLog(DynListConstraint.class);

	/** The service registry. */
	private static ServiceRegistry serviceRegistry;

	/** The paths. */
	private List<String> paths = null;

	/** The constraint type. */
	private String constraintType = null;

	/** The constraint prop. */
	private String constraintProp = null;

	/** The level in multi level case */
	private String level = null;

	/** The level prop in multi level case */
	private String levelProp = null;
	
	private Boolean addEmptyValue = null;

	/**
	 * Set the paths where are stored allowed values by the constraint.
	 * 
	 * @param paths
	 *            a list of path
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
	}

	/**
	 * Sets the service registry.
	 * 
	 * @param serviceRegistry
	 *            the new service registry
	 */
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		DynListConstraint.serviceRegistry = serviceRegistry;
	}

	/**
	 * Sets the constraint type.
	 * 
	 * @param constraintType
	 *            the new constraint type
	 */
	public void setConstraintType(String constraintType) {
		this.constraintType = constraintType;
	}

	/**
	 * Sets the constraint prop.
	 * 
	 * @param constraintProp
	 *            the new constraint prop
	 */
	public void setConstraintProp(String constraintProp) {
		this.constraintProp = constraintProp;
	}

	/**
	 * 
	 * @param level
	 */
	public void setLevel(String level) {
		this.level = level;
	}

	/**
	 * 
	 * @param levelProp
	 */
	public void setLevelProp(String levelProp) {
		this.levelProp = levelProp;
	}

	public void setAddEmptyValue(Boolean addEmptyValue) {
		this.addEmptyValue = addEmptyValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint#initialize
	 * ()
	 */
	@Override
	public void initialize() {
		checkPropertyNotNull("paths", paths);
		checkPropertyNotNull("constraintType", constraintType);
		checkPropertyNotNull("constraintProp", constraintProp);
		if (level != null) {
			checkPropertyNotNull("levelProp", levelProp);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint#
	 * getAllowedValues()
	 */
	@Override
	public List<String> getAllowedValues() {

		
		List<String> allowedValues = serviceRegistry.getTransactionService().getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<List<String>>() {
			@Override
			public List<String> execute() throws Throwable {

				List<String> allowedValues = new ArrayList<String>();
				
				if(addEmptyValue != null && addEmptyValue && !allowedValues.contains("")){
					allowedValues.add("");
				}

				for (String path : paths) {

					// logger.debug("getAllowedValues, path: " + path);
					NamespaceService namespaceService = serviceRegistry.getNamespaceService();
					List<String> values = getAllowedValues(path, QName.createQName(constraintType, namespaceService), QName.createQName(constraintProp, namespaceService));
					allowedValues.addAll(values);
				}

				return allowedValues;

			}
		}, false, true);

		if (allowedValues.isEmpty()) {
			allowedValues.add(UNDIFINED_CONSTRAINT_VALUE);
		}

		super.setAllowedValues(allowedValues);
		return allowedValues;
	}

	/**
	 * Get allowed values according to path, type and property (Look in every
	 * site).
	 * 
	 * @param path
	 *            the path
	 * @param constraintType
	 *            the constraint type
	 * @param constraintProp
	 *            the constraint prop
	 * @return the allowed values
	 */
	private List<String> getAllowedValues(final String path, final QName constraintType, final QName constraintProp) {

		return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<List<String>>() {
			@Override
			public List<String> doWork() throws Exception {
				List<String> allowedValues = new ArrayList<String>();
				String encodedPath = LuceneHelper.encodePath(path.substring(1));

				String queryPath = String.format(RepoConsts.PATH_QUERY_LIST_CONSTRAINTS, encodedPath, constraintType);

				ResultSet resultSet = null;
				SearchParameters sp = new SearchParameters();
				sp.addStore(RepoConsts.SPACES_STORE);
				sp.setLanguage(SearchService.LANGUAGE_LUCENE);
				sp.setQuery(queryPath);
				sp.setLimitBy(LimitBy.UNLIMITED);
				sp.addLocale(Locale.getDefault());
				sp.setPermissionEvaluation(PermissionEvaluationMode.EAGER);
				sp.excludeDataInTheCurrentTransaction(false);
				sp.addSort("@" + BeCPGModel.PROP_SORT, true);
				try {
					resultSet = serviceRegistry.getSearchService().query(sp);
					if (logger.isDebugEnabled()) {
						logger.debug("queryPath : " + queryPath);
						logger.debug("resultSet.length() : " + resultSet.length());
					}

					if (resultSet.length() != 0) {
						for (ResultSetRow row : resultSet) {
							NodeRef nodeRef = row.getNodeRef();
							if (serviceRegistry.getNodeService().exists(nodeRef)) {
								String value = (String) serviceRegistry.getNodeService().getProperty(nodeRef, constraintProp);
								if (!allowedValues.contains(value) && value != null && checkLevel(nodeRef)) {
									allowedValues.add(value);
								}
							} else {
								logger.warn("Node doesn't exist : " + nodeRef);
							}
						}
					}
					if (logger.isDebugEnabled()) {
						logger.debug("allowedValues.size() : " + allowedValues.size());
						logger.debug("allowed values: " + allowedValues.toString());
					}
					return allowedValues;
				} finally {
					if (resultSet != null)
						resultSet.close();
				}
			}

			private boolean checkLevel(NodeRef nodeRef) {
				if (level != null) {
					try {
						int l = Integer.parseInt(level);

						return l == computeLevel(nodeRef, QName.createQName(levelProp, serviceRegistry.getNamespaceService()));

					} catch (Exception e) {
						logger.warn("Cannot check level", e);
					}
				}

				return true;
			}

			private int computeLevel(NodeRef nodeRef, QName createQName) {
				Set<QName> qnames = new HashSet<QName>();
				qnames.add(createQName);

				NodeRef parentNode = (NodeRef) serviceRegistry.getNodeService().getProperty(nodeRef, createQName);
				if (parentNode != null) {
					return 1 + computeLevel(parentNode, createQName);
				}

				return 0;
			}
		}, AuthenticationUtil.getSystemUserName());
	}

	
}
