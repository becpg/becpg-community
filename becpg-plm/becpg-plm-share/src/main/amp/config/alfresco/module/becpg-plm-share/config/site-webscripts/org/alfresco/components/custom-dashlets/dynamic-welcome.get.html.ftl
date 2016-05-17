
<@markup  id="customDynamicWelcome-css" target="css" action="replace">
 	<#include "../form/form.css.ftl"/>
	<@link href="${url.context}/res/css/fixForm.css" group="dashlets" />
	<@link rel="stylesheet" type="text/css" href="${url.context}/res/components/dashlets/dynamic-welcome.css" group="dashlets" />
	
</@>


<@markup  id="customDynamicWelcome-js" target="js" action="replace">
   <@script type="text/javascript" src="${url.context}/res/components/dashlets/dynamic-welcome.js" group="dashlets"/> 
   <#include "../form/form.js.ftl"/>
   <@script type="text/javascript" src="${url.context}/res/modules/simple-dialog.js" group="dashlets"/>
   <@script type="text/javascript" src="${url.context}/res/components/dashlets/custom-dynamic-welcome.js" group="dashlets"/>
</@>


<@markup id="customHtml" target="html" action="replace">
   <@uniqueIdDiv>
      <#if showDashlet>
         <#assign el=args.htmlid?html>
         <div id="${el}-get-started-panel-container" class="dashlet dynamic-welcome">
            <div class="alf-welcome-hide-button">
                <span id="${el}-hide-button" class="yui-button yui-push-button">
                    <span class="first-child">
                        <button>${msg(args.dashboardType + ".welcome.close")}</button>
                    </span>
                </span>
            </div>
            <div class="welcome-body">

               <#-- OVERVIEW CONTAINER -->
               <@markup id="overviewContainer">
                  <div class="welcome-info">
                     <h1>${msg(title, user.fullName, site)?html}</h1>
                     <#if description??>
                        <p class="welcome-info-text">${msg(description)}</p>
                     </#if>
                     <h2>${msg("get.started.message")}</h2>
                  </div>
               </@markup>

               
                   <#-- DESCRIPTIONS CONTAINER -->
                   <@markup id="actionsContainer">
                      <div>
                         <div class="welcome-right-container">
                            <div class="welcome-middle-right-container welcome-border-container">
                               <div class="welcome-middle-left-container welcome-border-container">
                                  <div class="welcome-left-container welcome-border-container">
                                     <#list columns as column>
                                        <#if column??>
                                           <div class="welcome-details-column welcome-details-column-${column_index}">
                                              <div class="welcome-details-column-image">
                                                 <img src="${url.context}${column.imageUrl}"/>
                                              </div>
                                              <div class="welcome-details-column-info">
                                                 <h3>${msg(column.title)}</h3>
                                                 <#-- The following section allows us to insert arguments into the
                                                   description using the "descriptionArgs" property of the column
                                                   data. We construct a FreeMarker expression as a string iterating
                                                   over any supplied arguments and then evaluate it. -->
                                                 <#assign descArgs = "msg(column.description" />
                                                 <#if column.descriptionArgs??>
                                                    <#list column.descriptionArgs as x>
                                                       <#assign descArgs = descArgs + ",\"" + x?rtf?html + "\"">
                                                    </#list>
                                                 </#if>
                                                 <#assign descArgs = descArgs + ")">
                                                 <#assign displayText = descArgs?replace("{", "'{'")?replace("}", "'}'")?eval>
                                                 <p class="welcome-details-column-info-text">${displayText?replace("%7B", "{")}</p>
                                              </div>
                                              <div class="welcome-height-adjuster" style="height:0;">&nbsp;</div>
                                           </div>
                                        </#if>
                                     </#list>
                                  </div>
                               </div>
                            </div>
                         </div>
                         <div class="welcome-height-adjuster" style="height:0;">&nbsp;</div>
                      </div>
                   </@markup>
    
                   <#-- ACTIONS -->
                   <@markup id="actionsContainer">
                      <div class="welcome-details">
                         <div class="welcome-right-container">
                            <div class="welcome-middle-right-container">
                               <div class="welcome-middle-left-container">
                                  <div class="welcome-left-container">
                                     <#list columns as column>
                                        <#if column??>
                                           <div class="welcome-details-column welcome-details-column-${column_index}">
                                              <div class="welcome-details-column-info">
                                                 <#if column.actionMsg??>
                                                    <div class="welcome-details-column-info-vertical-spacer"></div>
                                                    <a <#if column.actionId??>id="${el}${column.actionId}" </#if>
                                                       <#if column.actionHref??>href="${column.actionHref}" </#if>
                                                       <#if column.actionTarget??>target="${column.actionTarget}" </#if>>
                                                          <span>${msg(column.actionMsg)}</span>
                                                    </a>
                                                 <#else>
                                                    <div class="welcome-details-column-info-vertical-spacer"></div>
                                                 </#if>
                                              </div>
                                           </div>
                                        </#if>
                                     </#list>
                                  </div>
                               </div>
                            </div>
                         </div>
                         <div class="welcome-height-adjuster">&nbsp;</div>
                      </div>
                   </@markup>    
              </div>
          </div>
         </#if>
   </@>
</@>