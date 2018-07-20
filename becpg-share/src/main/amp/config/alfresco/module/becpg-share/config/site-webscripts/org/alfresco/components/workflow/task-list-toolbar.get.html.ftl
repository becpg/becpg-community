<@markup id="css" >
   <#-- CSS Dependencies -->
   <@link href="${url.context}/res/components/workflow/task-list-toolbar.css" group="workflow"/>
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
   <@script src="${url.context}/res/components/workflow/task-list-toolbar.js" group="workflow"/>
</@>

<@markup id="widgets">
   <@createWidgets group="workflow"/>
</@>

<@markup id="html">
   <@uniqueIdDiv>
      <#assign el=args.htmlid?html>
      <div id="${el}-body" class="task-list-toolbar toolbar">
         <div id="${el}-headerBar" class="header-bar flat-button theme-bg-2">
           <div class="hideable hidden">
           	    <div class="left">
	           	      <div class="search-box">
						  <input id="${el}-searchText" type="text" maxlength="1024" />
					</div>
				</div>
			     <div class="left">
	             	<div class="search"><button id="${el}-search-button" name="search">${msg("button.search")}</button></div>
           		 </div>
           	    <div class="left">
	               <div class="start-workflow"><button id="${el}-startWorkflow-button" name="startWorkflow">${msg("button.startWorkflow")}</button></div>
               </div>
           
            <div class="right">
            </div>
             </div>
         </div>
      </div>
   </@>
</@>