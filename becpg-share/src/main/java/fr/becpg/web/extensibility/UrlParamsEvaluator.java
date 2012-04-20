package fr.becpg.web.extensibility;

import java.util.Map;

import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.extensibility.impl.DefaultSubComponentEvaluator;

/**
 * Test url parameters agains evaluator params
 * @author matthieu
 *
 */
public class UrlParamsEvaluator extends DefaultSubComponentEvaluator
{
	
	
    /**
     * Returns true all url parameters values match 
     *
     * @param context
     * @param params
     * @return true if all url parameters values match 
     */
    @Override
    public boolean evaluate(RequestContext context, Map<String, String> params)
    {
    	
    	
    	for(Map.Entry<String, String> param : params.entrySet() ){
    		if(!param.getValue().equals(context.getParameter(param.getKey()))){
    			return false;
    		}
    	}
        return true;
    }
}
