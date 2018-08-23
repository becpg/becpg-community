
<#assign el=args.htmlid?html>
<#assign nodeRef=args.nodeRef?html>
<#assign processStates=["all", "active","archived"]>
<#include "../../include/alfresco-macros.lib.ftl" />

<div id="${el}" class="detailsDialog">
  <div id="${el}-dialogTitle" class="hd">${msg("header.process")}</div>
   
	
	<div id="${el}-filters-process" class="bd entity-process">
	<#--  Dialog toolbar filter -->
	<#if processes?? && processes?size &gt; 0>
	<div class="yui-g">
	 <div class="yui-u first">
		<span class="align-left yui-button yui-menu-button" id="${el}-filters">
	        <span class="first-child">
	           	<select id="process-type" class="process-list-filter-action process-state">
			     <#list processTypes as processType>
			        <option value="${processType?html}">${msg("filter.process." + processType)}</option>
			     </#list>
	    		</select>
	        </span>
	    </span>
	 </div>
	 <div class="yui-u u">
	    <span class="align-left yui-button yui-menu-button" id="${el}-sub-filters">
	        <span class="first-child">
			    <select id="process-state" class="process-list-filter-action process-type">
			     <#list processStates as state>
			        <option value="${state?html}">${msg("filter.process." + state)}</option>
			     </#list>
			    </select>
	        </span>
	    </span>
	 </div>
	</div>
	</#if>
	<#-- Processes -->
	<div class="info">
           <#if processes?? && processes?size &gt; 0>
      	      ${msg("label.partOfProcess")}
           <#else>
              ${msg("label.partOfNoProcess")}
           </#if>
    </div>
    <#if processes?? && processes?size &gt; 0>
       <hr/>
       <div class="processes">
              <#list processes as process>
                <#assign processState><#if process.isActive>active<#else>archived</#if></#assign>
                 <div class="process process-element ${process.type}-${processState} ${processState}-${process.type} ${process.type} ${processState} <#if !process_has_next>process-last</#if>">
                    <#if process.initiator?? && process.initiator.avatarUrl??>
                    <img src="${url.context}/proxy/alfresco/${process.initiator.avatarUrl}" alt="${msg("label.avatar")}"/>
                    <#else>
                    <img src="${url.context}/res/components/images/no-user-photo-64.png" alt="${msg("label.avatar")}"/>
                    </#if>
                    <#if process.id??>
                    <a href="${siteURL("workflow-details?workflowId=" + process.id?js_string + "&nodeRef=" + (args.nodeRef!""))}">
                    <#else>
                    <a href="${siteURL("entity-data-lists?nodeRef=" + process.nodeRef +"&list=View-properties")}">
                    </#if>
                    <#if process.message?? && process.message?length &gt; 0>${process.message?html}<#else>${msg("process.no_description")?html}</#if></a>
                    <div class="title">${process.title?html}</div>
                    <div class="title">${process.startDate}  <#if process.dueDate??> - ${process.dueDate}<#else></#if></div> 
                    <div class="clear"></div>
                 </div>
              </#list>
          </div>
   </#if>
  </div>
		
</div>  

<script type="text/javascript">//<![CDATA[
 (function () {
		 new beCPG.component.EntityProcess("${el}");
	
 })();

//]]></script>


