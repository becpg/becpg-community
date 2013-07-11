package fr.becpg.repo.formulation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StopWatch;

public abstract class FormulationBaseHandler<T> implements FormulationHandler<T> {
 

	private Log logger = LogFactory.getLog(FormulationBaseHandler.class);
	
    private FormulationHandler<T> nextHandler;
 
    public void setNextHandler(FormulationHandler<T> next) {
        nextHandler = next;
    }
 
    public void start(T context) throws FormulateException {
    	
    	StopWatch watch = null;
		if(logger.isDebugEnabled()){
		   watch = new StopWatch();
			watch.start();
		}
    	
        boolean processed = process(context);
        
        if(logger.isDebugEnabled()){
        	watch.stop();
        	logger.debug("Call handler : "+this.getClass().getName()+" takes " + watch.getTotalTimeSeconds() + " seconds");
        }
        
        if (processed && nextHandler != null){
            // Note that next handler's method is called through "start", not "process"
            nextHandler.start(context);
        }
    }
}
