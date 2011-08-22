/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.dictionary.constraint;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
	//TODO : est-ce la bonne mani�re de faire ?
	/** The list value service. */
	private static ListValueService listValueService = null;
	
	/** The transaction service. */
	private static TransactionService transactionService = null;
	
	/** The service registry. */
	private static ServiceRegistry serviceRegistry;
	
	/** The paths. */
	private List<String> paths = null;	
	
	/** The constraint type. */
	private String constraintType = null;
	
	/** The constraint prop. */
	private String constraintProp = null;
	
	/**
	 * Sets the list value service.
	 *
	 * @param listValueService the new list value service
	 */
	public void setListValueService(ListValueService listValueService) {
		DynListConstraint.listValueService = listValueService;
	}
	
	/**
	 * Sets the transaction service.
	 *
	 * @param transactionService the new transaction service
	 */
	public void setTransactionService(TransactionService transactionService) {
		DynListConstraint.transactionService = transactionService;
	}
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
		List<String> allowedValues = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<List<String>>(){
 			@Override
			public List<String> execute() throws Throwable {
 				
 				List<String> allowedValues = new ArrayList<String>();
 				
 				for(String path : paths){
 					
 					//logger.debug("getAllowedValues, path: " + path); 		
	 				NamespaceService namespaceService = serviceRegistry.getNamespaceService();
	 				List<String> values = listValueService.getAllowedValues(path, QName.createQName(constraintType, namespaceService), QName.createQName(constraintProp, namespaceService));		 				
	 				allowedValues.addAll(values);
 				} 				
 				
 				return allowedValues;

 			}},false,true);
													
		if(allowedValues.size() == 0){
			//throw new DictionaryException(ERR_NO_VALUES);
			allowedValues.add(UNDIFINED_CONSTRAINT_VALUE);
		}						
		
		super.setAllowedValues(allowedValues);
		return allowedValues;
	}
		
}
