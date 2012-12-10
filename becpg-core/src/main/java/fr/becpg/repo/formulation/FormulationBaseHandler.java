package fr.becpg.repo.formulation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class FormulationBaseHandler<T> implements FormulationHandler<T> {
 

	private Log logger = LogFactory.getLog(FormulationBaseHandler.class);
	
    private FormulationHandler<T> nextHandler;
 
    public void setNextHandler(FormulationHandler<T> next) {
        nextHandler = next;
    }
 
    public void start(T context) throws FormulateException {
        // Calls "this" handler logic
    	if(logger.isDebugEnabled()){
    		logger.debug("Call handler : "+this.getClass().getName());
    	}
    	
        boolean processed = process(context);
        if ( processed && nextHandler != null){
            // Note that next handler's method is called through "start", not "process"
            nextHandler.start(context);
        }
    }
}
