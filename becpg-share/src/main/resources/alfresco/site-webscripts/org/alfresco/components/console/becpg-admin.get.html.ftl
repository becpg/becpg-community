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
									<span class="info">${freeMem?string("0")} Mo</span><br/>
									<label>${msg("label.totalMemory")}</label>
									<span class="info">${systemInfo.totalMemory?string("0")} Mo</span><br/>
									<label>${msg("label.maxMemory")}</label>
									<span class="info">${systemInfo.maxMemory?string("0")} Mo</span><br/>
									<label>${msg("label.diskPerc")}</label>
									<meter min="0"
											low="10"
											high="70"
											optimum="50"
											value="${diskPerc?string("0")}"
											max="100"
											title="${diskPerc?string("0")} %"></meter><br/>
									<label>${msg("label.becpgSchema")}</label>
									<#if versionDate??>
										<span class="info">${version?string} - ${versionDate?datetime}</span><br/>
									<#else>
										<span class="info">${systemInfo.becpgSchema}</span><br/>
									</#if>
									<#if systemInfo.withoutLicenseUsers?? &&  systemInfo.withoutLicenseUsers &gt; 0>
									  <label>${msg("label.withoutLicenseUsers")}</label>
									  <span class="error">${systemInfo.withoutLicenseUsers?string("0")}</span><br/>
									</#if>
									<label>${msg("label.connectedUsers")}</label>
									<span class="info">${systemInfo.connectedUsers}</span><br/>
									<label>${msg("label.namedReadUsers")}</label>
									<#if systemInfo.namedReadUsers gt systemInfo.license.allowedNamedRead>
										<span class="error">${systemInfo.namedReadUsers} / ${systemInfo.license.allowedNamedRead}</span><br/>
									<#else>
										<span class="info">${systemInfo.namedReadUsers} / ${systemInfo.license.allowedNamedRead}</span><br/>
									</#if>
									<label>${msg("label.namedWriteUsers")}</label>
									<#if systemInfo.namedWriteUsers gt systemInfo.license.allowedNamedWrite>
										<span class="error">${systemInfo.namedWriteUsers} / ${systemInfo.license.allowedNamedWrite}</span><br/>
									<#else>
										<span class="info">${systemInfo.namedWriteUsers} / ${systemInfo.license.allowedNamedWrite}</span><br/>
									</#if>
									<label>${msg("label.concurrentReadUsers")}</label>
									<#if systemInfo.concurrentReadUsers gt systemInfo.license.allowedConcurrentRead>
										<span class="error">${systemInfo.concurrentReadUsers} / ${systemInfo.license.allowedConcurrentRead}</span><br/>
									<#else>
										<span class="info">${systemInfo.concurrentReadUsers} / ${systemInfo.license.allowedConcurrentRead}</span><br/>
									</#if>
									<label>${msg("label.concurrentWriteUsers")}</label>
									<#if systemInfo.concurrentWriteUsers gt systemInfo.license.allowedConcurrentWrite>
										<span class="error">${systemInfo.concurrentWriteUsers} / ${systemInfo.license.allowedConcurrentWrite}</span><br/>
									<#else>
										<span class="info">${systemInfo.concurrentWriteUsers} / ${systemInfo.license.allowedConcurrentWrite}</span><br/>
									</#if>
									<label>${msg("label.concurrentSupplierUsers")}</label>
									<#if systemInfo.concurrentSupplierUsers gt systemInfo.license.allowedConcurrentSupplier>
										<span class="error">${systemInfo.concurrentSupplierUsers} / ${systemInfo.license.allowedConcurrentSupplier}</span><br/>
									<#else>
										<span class="info">${systemInfo.concurrentSupplierUsers} / ${systemInfo.license.allowedConcurrentSupplier}</span><br/>
									</#if>
									<label>${msg("label.batchCounts")}</label>
									<span class="info">${systemInfo.batchCounts}</span><br/>
					 			</div>
					 			 </td>
					 			</tr>
							</table>
						</div>
					</div>
					<div class="yui-u system-action-container">
				     	<div class="header-bar">
				         <div class="title">${msg("label.actions")}</div>
				      </div>      
				      <div class="section">
				      <@markup id="actions">
					    	<div class="action">
					    		<button type="button" name="${el}-init-repo-button" id="${el}-init-repo-button">${msg("button.init-repo")}</button>
					    		<label for="${el}-init-repo-button">${msg("label.init-repo")}</label>   		
					    	</div>
					  		  <div class="action">
					    		<button type="button" name="${el}-empty-cache" id="${el}-empty-cache-button">${msg("button.empty-cache")}</button>
					    		<label for="${el}-empty-cache-button">${msg("label.empty-cache")}</label>    		
					  		  </div>
					  		  <div class="action">
					    		<button type="button" name="${el}-show-users" id="${el}-show-users-button">${msg("button.show-users")}</button>
					    		<label for="${el}-show-users-button">${msg("label.show-users")}</label>    		
					  		  </div>
					  		  <div class="action">
					    		<button type="button" name="${el}-show-batches" id="${el}-show-batches-button">${msg("button.show-batches")}</button>
					    		<label for="${el}-show-batches-button">${msg("label.show-batches")}</label>    		
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