#!/bin/bash
## Author: matthieu

. ../common.sh

if [ $# -ne 5 ]
   then
      echo "Usage: $0 <instance name> <adminpassword> <dbhosts> <dbusername> <dbpassword>"
      exit 0
fi

export SERVER=$INSTANCE_DIR/$1
export MODEL=`pwd`/1.4-migrate-dynamicCharachList-to-dynamicCharactList.xml
echo ${MODEL} 
# set Alfresco admin credentials
# username to reset password
ALF_USERNAME=admin
# new password
ALF_PASSWORD=$2

# set the alfresco webapp path
ALFRESCO_WEBAPP=$SERVER/webapps/alfresco
# set the extention shared path
EXTENSION_PATH=$SERVER/becpg/classes
# Directory containing db drivers and servlet-api.jar
EXTRA_LIB=$SERVER/lib

if [ ! -d $ALFRESCO_WEBAPP ] ; then
	echo "Webapp: ${ALFRESCO_WEBAPP} not found. Exit." 
	exit
fi

if [ ! -d $EXTRA_LIB ] ; then
	echo "$EXTRA_LIB not found..."
	EXTRA_LIB=/opt/tomcat/shared/lib
	echo "...trying $EXTRA_LIB"
fi

# Following only needed for Sun JVMs before to 1.5 update 8
export JAVA_OPTS="-Xms512m -Xmx1024m -Xss1024k -XX:MaxPermSize=256m -XX:NewSize=256m -Ddb.driver=org.gjt.mm.mysql.Driver -Ddb.url=jdbc:mysql://$3/$1 -Ddb.username=$4 -Ddb.password=$5 -Ddir.root=${SERVER}/becpg/data"
echo "Starting beanshell..."

WEBINFLIB=${ALFRESCO_WEBAPP}/WEB-INF/lib
MODULEPATH=${ALFRESCO_WEBAPP}/WEB-INF/classes/alfresco/module
CLASSESDIRPATH=${ALFRESCO_WEBAPP}/WEB-INF/classes
TOMCATLIB=$SERVER/../../tomcat-7.0.23.A.RELEASE/lib

CLASSPATH=${WEBINFLIB}:${MODULEPATH}:${CLASSESDIRPATH}:${EXTENSION_PATH}

if [ -d $EXTRA_LIB ] ; then
    for EXTRA_JARNAME in $( ls ${EXTRA_LIB} )
        do
           CLASSPATH=${CLASSPATH}:${EXTRA_LIB}/${EXTRA_JARNAME}
        done
else
	echo "$EXTRA_LIB not found."
fi

if [ -d $TOMCATLIB ] ; then
    for EXTRA_JARNAME in $( ls ${TOMCATLIB} )
        do
           CLASSPATH=${CLASSPATH}:${TOMCATLIB}/${EXTRA_JARNAME}
        done
else
	echo "$TOMCATLIB not found."
fi

for JARNAME in $( ls ${WEBINFLIB} )
do
    CLASSPATH=${CLASSPATH}:${WEBINFLIB}/${JARNAME}
done
#echo ${EXTENSION_PATH}
java ${JAVA_OPTS} -classpath ${CLASSPATH} bsh.Interpreter <<EOF 
print ("Loading context... can take a little while...");
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Model;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.BeCPGModel;

org.springframework.context.ApplicationContext ctx = org.alfresco.util.ApplicationContextHelper.getApplicationContext();
org.alfresco.service.ServiceRegistry serviceRegistry = (org.alfresco.service.ServiceRegistry) ctx.getBean(org.alfresco.service.ServiceRegistry.SERVICE_REGISTRY);
serviceRegistry.getAuthenticationService().authenticate("${ALF_USERNAME}", "${ALF_PASSWORD}".toCharArray());

final SearchService searchService = serviceRegistry.getSearchService();
final DictionaryDAO dictionaryDAO = (DictionaryDAO)ctx.getBean("dictionaryDAO");
final NodeService nodeService = serviceRegistry.getNodeService();
final TenantAdminService tenantService = (TenantAdminService) ctx.getBean("tenantAdminService");
	
void migrate(String systemUserName){

print("migrate with user :" + systemUserName);

	AuthenticationUtil.runAs(new RunAsWork()
         {
     		public Object doWork() throws Exception
             {
     			
     			M2Model m2Model = M2Model.createModel(new FileInputStream("${MODEL}"));
     			dictionaryDAO.putModel(m2Model);
     			dictionaryDAO.getModels();
     			
     		   SearchParameters sp = new SearchParameters();
     		    sp.addStore(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"));
     		    sp.setLanguage(SearchService.LANGUAGE_LUCENE);
     		    sp.setQuery("+TYPE:\"bcpg:rawMaterial\" ");	
     		    sp.addLocale(Locale.getDefault());
     		    sp.setLimitBy(LimitBy.UNLIMITED);
     					
     		    ResultSet result =  searchService.query(sp);
     		    
	    		print("migration size: " + result.getNodeRefs().size());

     		    for(NodeRef nodeRef : result.getNodeRefs()){
     		    	print("check WUsed aspect: "+nodeRef);
     		    	
			if(nodeService.getSourceAssocs(nodeRef, BeCPGModel.ASSOC_COMPOLIST_PRODUCT).size() == 0){
	     		    	print("delete unused product: "+nodeRef);
	     		    	NodeRef entityNodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
				// nodeService.deleteNode(entityNodeRef);
			}     		    			     		    	
     		    }
     			return null;
             }
         }, systemUserName);

}
	
//migrate(AuthenticationUtil.getSystemUserName());
migrate("admin@demo.becpg.fr");

EOF