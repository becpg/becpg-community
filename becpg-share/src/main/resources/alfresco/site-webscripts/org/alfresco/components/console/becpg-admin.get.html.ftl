<#assign el=args.htmlid?html>

<@markup id="css" >
   <#-- CSS Dependencies -->
 <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/console/becpg-admin.css" group="becpg-admin" />
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
   <@script type="text/javascript" src="${url.context}/res/components/console/consoletool.js" group="becpg-admin" ></@script>
   <@script type="text/javascript" src="${url.context}/res/components/console/becpg-admin.js" group="becpg-admin" ></@script>
</@>


<@markup id="widgets">
   <@createWidgets group="becpg-admin"/> 		
</@>

<@markup id="html">
 <@uniqueIdDiv>
	<div id="${el}-body" class="becpg-admin-console">
		<#if user.isAdmin> 
			 <div class="yui-g">
					 <div class="yui-u first system-info-container">	
						<div class="header-bar">
				         <div class="title">${msg("label.informations")}</div>
				      </div>      
				      <div class="section">
					      <table>
					      <tr><td>
					 			 <div class="yui-u first" >
					 			 	<div id="${el}-gauge-wrapper" class="gauge-wrapper">
										<svg class="gauge-meter">
										    <circle id="${el}-outline_curves" class="circle outline"  cx="50%" cy="50%">
										    </circle>
										    <circle id="${el}-low" class="circle range" cx="50%" cy="50%" stroke="#109618">
										    </circle>
										    <circle id="${el}-avg" class="circle range" cx="50%" cy="50%" stroke="#ff9900">
										    </circle>
										    <circle id="${el}-high" class="circle range" cx="50%" cy="50%" stroke="#dc3912">
										    </circle>
										</svg>
										<img id="${el}-gauge-meter_needle" class="gauge-meter_needle"  src="${url.context}/res/components/images/gauge-needle.svg" alt="">
										<label id="gauge-percentage" for=""></label>
									</div>
					 			 </div>
					 			</td>
					 			<td>
					 			<div class="infos">
					 				<label>${msg("label.freeMemory")}</label>
									<span class="info">${systemInfo.freeMemory?string("0")} Mo</span><br/>
									<label>${msg("label.totalMemory")}</label>
									<span class="info">${systemInfo.totalMemory?string("0")} Mo</span><br/>
									<label>${msg("label.nonHeapMemory")}</label>
									<span class="info">${systemInfo.nonHeapMemoryUsage?string("0")} Mo</span><br/>
									<label>${msg("label.becpgSchema")}</label>
									<#if versionDate??>
										<span class="info">${version?string} - ${versionDate?datetime}</span><br/>
									<#else>
										<span class="info">${systemInfo.becpgSchema}</span><br/>
									</#if>
									<label>${msg("label.connectedUsers")}</label>
									<span class="info">${systemInfo.connectedUsers}</span><br/>
					 			</div>
					 			 <div class="action">
						    		<button type="button" name="${el}-empty-cache" id="${el}-empty-cache-button">${msg("button.empty-cache")}</button>
						    		<label for="${el}-empty-cache-button">${msg("label.empty-cache")}</label>    		
						  		  </div>
						  		  <div class="action">
						    		<button type="button" name="${el}-show-users" id="${el}-show-users-button">${msg("button.show-users")}</button>
						    		<label for="${el}-show-users-button">${msg("label.show-users")}</label>    		
						  		  </div>
					 			 </td>
					 			</tr>
							</table>
						</div>
					</div>
					<div class="yui-u system-action-container">
				     	<div class="header-bar">
				         <div class="title">${msg("label.repository")}</div>
				      </div>      
				      <div class="section">
				      <@markup id="actions">
					    	<div class="action">
					    		<button type="button" name="${el}-init-repo-button" id="${el}-init-repo-button">${msg("button.init-repo")}</button>
					    		<label for="${el}-init-repo-button">${msg("label.init-repo")}</label>   		
					    	</div>
					    	<div class="action">
					    		<button type="button" name="${el}-init-acl-button" id="${el}-init-acl-button">${msg("button.init-acl")}</button>
					    		 <label for="${el}-init-acl-button">${msg("label.init-acl")}</label>   	
					    	</div>
						</div>
					 </@markup>	
				    </div>
			   </div>
		   </#if>
			<div class="yui-g">
				<div class="yui-u first system-charact-container">	
					<div class="header-bar">
			         <div class="title">${msg("label.characts")}</div>
			      </div>      
			      <div class="section">
			      <#list systemEntities as item>
				     		 <div class="action">				
				     		 	${msg("label.characts.edit")}<a href="${url.context}/page/entity-data-lists?nodeRef=${item.nodeRef}"><span class="systemEntity">${item.name}</span></a>
				     		 	<#if item.title?? && item.title?length &gt; 0 && item.title != item.name><span>(${item.title})</span></#if>
				    		   <#if item.description??  && item.description?length &gt; 0 ><p>${item.description}</p></#if>
					    	</div>
					</#list>
					</div>
				</div>
				<div class="yui-u system-folder-container">
			     	<div class="header-bar">
			         <div class="title">${msg("label.systemFolders")}</div>
			      </div>      
			      <div class="section">
			        <#list systemFolders as item>
						<div class="action">				
							${msg("label.systemFolder.access")}<a href="${url.context}/page/repository#filter=path|${item.urlPath?url}"><span class="systemFolder">${item.name}</span></a>
				    		<#if item.title?? && item.title?length &gt; 0 && item.title != item.name><span>(${item.title})</span></#if>
				    		<#if item.description??  && item.description?length &gt; 0 ><p>${item.description}</p></#if>
				    	</div>
				    </#list>
					</div>
			    </div>
		   </div>
	</div>
</@>
</@>