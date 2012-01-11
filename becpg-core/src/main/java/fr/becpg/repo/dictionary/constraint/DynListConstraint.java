/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.dictionary.constraint;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ISO9075;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.listvalue.ListValueService;

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
	
    /**
     * Set the paths where are stored allowed values by the constraint.
     *  
     * @param paths a list of path
     */
	public void setPath(List<String> paths) {
		
		if (paths == null)
        {
            throw new DictionaryException(ERR_NO_VALUES);
        }
        int valueCount = paths.size();
        if (valueCount == 0)
        {
            throw new DictionaryException(ERR_NO_VALUES);
        }
        this.paths = paths;		
	}	
	
	/**
	 * Sets the service registry.
	 *
	 * @param serviceRegistry the new service registry
	 */
	public void setServiceRegistry(ServiceRegistry serviceRegistry){
		DynListConstraint.serviceRegistry = serviceRegistry;
    }
	
	/**
	 * Sets the constraint type.
	 *
	 * @param constraintType the new constraint type
	 */
	public void setConstraintType(String constraintType) {			
		this.constraintType = constraintType;
	}
	
	/**
	 * Sets the constraint prop.
	 *
	 * @param constraintProp the new constraint prop
	 */
	public void setConstraintProp(String constraintProp) {
		this.constraintProp = constraintProp;
	}	
	
	/* (non-Javadoc)
	 * @see org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint#initialize()
	 */
	@Override
	public void initialize()
	{		
		checkPropertyNotNull("paths", paths);
		checkPropertyNotNull("constraintType", constraintType);
		checkPropertyNotNull("constraintProp", constraintProp);
	}
	
	/* (non-Javadoc)
	 * @see org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint#getAllowedValues()
	 */
	@Override
	public List<String> getAllowedValues()
	{				
		List<String> allowedValues = serviceRegistry.getTransactionService().getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<List<String>>(){
 			@Override
			public List<String> execute() throws Throwable {
 				
 				List<String> allowedValues = new ArrayList<String>();
 				
 				for(String path : paths){
 					
 					//logger.debug("getAllowedValues, path: " + path); 		
	 				NamespaceService namespaceService = serviceRegistry.getNamespaceService();
	 				List<String> values = getAllowedValues(path, QName.createQName(constraintType, namespaceService), QName.createQName(constraintProp, namespaceService));		 				
	 				allowedValues.addAll(values);
 				} 				
 				
 				return allowedValues;

 			}},false,true);
													
		if(allowedValues.size() == 0){
			allowedValues.add(UNDIFINED_CONSTRAINT_VALUE);
		}						
		
		super.setAllowedValues(allowedValues);
		return allowedValues;
	}
	
	/**
	 * Get allowed values according to path, type and property (Look in every site).
	 *
	 * @param path the path
	 * @param constraintType the constraint type
	 * @param constraintProp the constraint prop
	 * @return the allowed values
	 */	
	private List<String> getAllowedValues(final String path, final QName constraintType, final QName constraintProp) {
		
		return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<List<String>>()
		        {
		            @Override
					public List<String> doWork() throws Exception
		            {
		            	List<String> allowedValues = new ArrayList<String>();
		            	String encodedPath = encodePath(path);    			
		        		
		        		String queryPath = String.format(RepoConsts.PATH_QUERY_LIST_CONSTRAINTS, encodedPath, constraintType);
		        		logger.debug("queryPath : " + queryPath);
		        		ResultSet resultSet = null;
		        		
		        		try{
		        			resultSet = serviceRegistry.getSearchService().query(RepoConsts.SPACES_STORE, 
		        						SearchService.LANGUAGE_LUCENE, queryPath);
		        	        
		        			logger.debug("resultSet.length() : " + resultSet.length());
		        			
		        	        if (resultSet.length() != 0)
		        	        {
		        	            for (ResultSetRow row : resultSet)
		        	            {
		        	                NodeRef nodeRef = row.getNodeRef();
		        	                String value = (String)serviceRegistry.getNodeService().getProperty(nodeRef, constraintProp);
		        	                if(!allowedValues.contains(value) && value!=null){
		        	                	allowedValues.add(value);
		        	                }
		        	            }                   	
		        	        }   
		        	        
//		        	        logger.debug("allowedValues.size() : " + allowedValues.size());
//		        	        logger.debug("allowed values: " + allowedValues.toString());
		        	        		
		        			return allowedValues;
		        		}
		        		finally{
		        			if(resultSet != null)
		        				resultSet.close();
		        		}
		            }
		        }, AuthenticationUtil.getSystemUserName());		
	}
	
	/**
     * Encode path.
     *
     * @param path the path
     * @return the string
     */
    private String encodePath(String path){
    	
    	StringBuilder pathBuffer = new StringBuilder(64);
    	String[] arrPath = path.split(RepoConsts.PATH_SEPARATOR);
    	
    	for(String folder : arrPath){
    		pathBuffer.append("/cm:");
    		pathBuffer.append(ISO9075.encode(folder));    		 
    	}
    	
    	//remove 1st character '/'
    	return pathBuffer.substring(1);
    }
}
