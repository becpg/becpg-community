package fr.becpg.olap.mondrian;

import mondrian.olap.Util.PropertyList;
import mondrian.spi.DynamicSchemaProcessor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import fr.becpg.olap.InstanceManager;
import fr.becpg.olap.InstanceManager.Instance;
import fr.becpg.olap.authentication.AlfrescoUserDetails;
import fr.becpg.olap.http.GetMondrianSchemaCommand;

public class AlfrescoSchemaProcessor implements DynamicSchemaProcessor {

	private static Log logger = LogFactory.getLog(AlfrescoSchemaProcessor.class);
	
	InstanceManager instanceManager;
	
	@Override
	public String processSchema(String schemaUrl, PropertyList connectInfo) throws Exception {
		if(logger.isDebugEnabled()){
			logger.debug("Retrieve mondrian schema for: "+schemaUrl);
			logger.debug("Connection info: "+connectInfo);
		}
		
		//retrieve instance manager from webapp ctx
		if(instanceManager == null){
			instanceManager = MondrianApplicationContextProvider.getApplicationContext().getBean("instanceManager",InstanceManager.class);
		}
		
		 Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		 
		 if(auth!=null && auth.getPrincipal()!=null &&   (auth.getPrincipal() instanceof AlfrescoUserDetails)){
			Instance instance = ((AlfrescoUserDetails)auth.getPrincipal()).getInstance();
			HttpClient httpClient =  instanceManager.createInstanceSession(instance);
			GetMondrianSchemaCommand schemaCommand = new GetMondrianSchemaCommand(instance.getInstanceUrl());
			
			return schemaCommand.getSchema(httpClient, instance.getId());
			 
		 }
		
		 return "<Schema name=\"beCPG OLAP Schema\"></Schema>";
		
	}

}
