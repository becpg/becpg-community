package fr.becpg.repo.formulation;

import java.util.List;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.repository.RepositoryEntity;

/**
 * 
 * <!-- Chains -->

	<util:list id="chainAList" scope="singleton" value-type="Handler">
	    <ref bean="handler1"/>
	    <ref bean="handler2"/>
	</util:list>
	
	<bean id="chainA" class="Chain" init-method="init">
	    <property name="handlers">
	        <ref bean="chainAList"/>
	    </property>
	     <property name="formulationService" ref="formulationService"/>
	     <property name="contextClass" value="EntityCLass" />	   
	</bean>
 * 
 * Chain of responsibility executor.
 * 
 * @param <T> Any type passed as context information.
 */
public class FormulationChain<T extends RepositoryEntity> {
	private Log logger = LogFactory.getLog(FormulationChain.class);
	
	private Class<T> contextClass;
	
	private FormulationService<T> formulationService;
	
    private List<FormulationHandler<T>> handlers;
    
    private String chainId; 
    
    
    public void setChainId(String chainId) {
		this.chainId = chainId;
	}


	public String getChainId() {
		return chainId;
	}


	public void setFormulationService(FormulationService<T> formulationService) {
		this.formulationService = formulationService;
	}
    
    
    public void setContextClass(Class<T> contextClass) {
		this.contextClass = contextClass;
	}



	public void init(){
		if (handlers!=null && !handlers.isEmpty()){
			prepareHandlerChain();
	    	formulationService.registerFormulationChain(contextClass, this);
		}
    }
    
    
    

	public void executeChain(T context) throws FormulateException {
        if (handlers!=null && !handlers.isEmpty()){
        	handlers.get(0).start(context);
        }
    }
 
    /**
     * Sets handler list. Intended to be injected.
     * @param handlers Handler list.
     */
    public void setHandlers(List<FormulationHandler<T>> handlers) {
        this.handlers = handlers;
    }
 
    private void prepareHandlerChain() {
  
        
 
        ListIterator<FormulationHandler<T>> handlersIt = handlers.listIterator();
        FormulationHandler<T> current = handlersIt.next();

        if(logger.isDebugEnabled()){
    		logger.debug("Prepare Handler Chain: ");
    		logger.debug("  - First: "+current.getClass().getName());
    	}
     
        while (handlersIt.hasNext()) {
        	
        	FormulationHandler<T> next = handlersIt.next();
        	if(logger.isDebugEnabled()){
        		logger.debug("  - Next: "+next.getClass().getName());
        		
        	}
        	
            current.setNextHandler(next);
            current = next;
        }
    }
}