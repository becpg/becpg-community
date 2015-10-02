<#include "include/alfresco-template.ftl" />
<@templateHeader>
  
	<@link href="${url.context}/res/css/beCPG.css" group="print-details"  media="print" />
	<@link href="${url.context}/res/yui/calendar/assets/calendar.css" group="print-details" media="print"/>
	<@link href="${url.context}/res/components/object-finder/object-finder.css" group="print-details" media="print"/>

	<#if config.global.forms?exists && config.global.forms.dependencies?exists && config.global.forms.dependencies.css?exists>
	   <#list config.global.forms.dependencies.css as cssFile>
	      <@link href="${url.context}/res${cssFile}" group="print-details" media="print"/>
	   </#list>
	</#if>
 	<@link href="${url.context}/res/components/entity-data-lists/entity-print.css" group="print-details" media="print" />

</@>

<@templateBody>
   <@markup id="bd">
	   <div id="bd">
	     <@region id="document-metadata" scope="template"/>
		 <@region id="comments" scope="template"/>
	   </div>
	   <script type="text/javascript">
	   	  setTimeout(function() {
		   YAHOO.util.Dom.getElementsByClassName("viewmode-mltext", "span",null, function()
	         { 
				  var me = this;
				  var fieldId = me.id.split("-")[this.id.split("-").length-1]; 
                   Alfresco.util.Ajax.request(
                            {
                             url :  Alfresco.constants.PROXY_URI+"/becpg/form/multilingual/field/"+fieldId+"?nodeRef=${url.args.nodeRef}" ,           
                                successCallback :
                                {
                                    fn : function(response){
	                                    
	                                    if(response.json){
		                                    var ret="";
		                                    for(var i in response.json.items){
		                                      var field = response.json.items[i];
		                                      if(ret.length>0){
		                                       ret+=", ";
		                                      }
		                                      if(field.value && field.value.length>0){
		                                   	      ret+= '<span class="locale-icon"><img class="icon16_11" title="'+field.locale+'" tabindex="0" src="${url.context}/res/components/images/flags/'+field.locale+'.png"></span>';
		                                          ret+= '<span  class="viewmode-value" >'+field.value+'</span>';
												}
		                                    }
		                                    me.innerHTML = ret;
	                                    }
                                    },
                                    scope : this
                                },
                                scope : this,
                                execScripts : true
                            });	

	         })}, 2000);
	   
	   </script>
	   
	</@>
</@>
