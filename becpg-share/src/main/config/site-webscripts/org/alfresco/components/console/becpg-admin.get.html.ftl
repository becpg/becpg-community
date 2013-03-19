

<@markup id="css" >
   <#-- CSS Dependencies -->
 <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/console/becpg-admin.css" group="becpg-admin" />
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
   <@script type="text/javascript" src="${url.context}/res/components/console/consoletool.js" group="becpg-admin" ></@script>
	<@script type="text/javascript" src="${url.context}/res/components/console/becpg-admin.js" group="becpg-admin" ></@script>
	<@script type='text/javascript' src='https://www.google.com/jsapi' group="becpg-admin" ></@script>
</@>





<@markup id="html">
 <@uniqueIdDiv>
   
	<#assign el=args.htmlid?html>

	<script type="text/javascript">//<![CDATA[
	   new beCPG.component.beCPGAdminConsole("${el}").setMessages(${messages});
	
	      google.load('visualization', '1', {packages:['gauge']});
	      google.setOnLoadCallback(drawChart);
	      function drawChart() {
	       var memory = Math.round((${systemInfo.totalMemory?c}-${systemInfo.freeMemory?c})/${systemInfo.totalMemory?c}*100);
	        var data = google.visualization.arrayToDataTable([
	          ['Label', 'Value'],
	          ['Memory', memory]
	        ]);
	
	        var options = {
	          width: 400, height: 120,
	          redFrom: 90, redTo: 100,
	          yellowFrom: 80, yellowTo: 90,
	          greenFrom:60, greenTo: 80,
	          minorTicks: 5
	        };
	
	        var chart = new google.visualization.Gauge(document.getElementById('${el}-gauge-div'));
	        chart.draw(data, options);
	      }
	     //]]>
	</script>
	<div id="${el}-body" class="becpg-admin-console">
		 <div class="system-info-container">	
					<div class="header-bar">
			         <div class="title">${msg("label.informations")}</div>
			      </div>      
			      <div class="section">
				      <table>
				      <tr><td>
				 			 <div class="yui-u first"  id='${el}-gauge-div'></div>
				 			</td>
				 			<td>
				 			<div class="infos">
				 				<label>Free memory</label>
								<span class="info">${systemInfo.freeMemory} octets</span><br/>
								<label>Total memory</label>
								<span class="info">${systemInfo.totalMemory} octets</span><br/>
								<label>Non heap memory</label>
								<span class="info">${systemInfo.nonHeapMemoryUsage} octets</span><br/>
				 			</div>
				 			 <div class="action">
					    		<button type="button" name="${el}-empty-cache" id="${el}-empty-cache-button">${msg("button.empty-cache")}</button>
					    		<label for="${el}-empty-cache-button">${msg("label.empty-cache")}</label>    		
					  		  </div>
				 			 </td>
				 			</tr>
						</table>
					</div>
			</div>
			<div class="yui-gd">
				<div class="yui-u first jobs-list-container">	
					<div class="header-bar">
			         <div class="title">${msg("label.characts")}</div>
			      </div>      
			      <div class="section">
			      <#list systemEntities as item>
				     		 <div class="action">				
				     		 	${msg("label.characts.edit")}<a href="${url.context}/page/entity-data-lists?nodeRef=${item.nodeRef}"><span class="systemEntity">${item.name}</span></a>
					    	</div>
					</#list>
					</div>
				</div>
				<div class="yui-u job-detail-container">
			     	<div class="header-bar">
			         <div class="title">${msg("label.repository")}</div>
			      </div>      
			      <div class="section">
						<div class="action">				
							<button type="button" name="${el}-reload-model-button" id="${el}-reload-model-button">${msg("button.reload-model")}</button>
				         <label for="${el}-reload-model-button">${msg("label.reload-model")}</label>               
				    	</div>
				    	<div class="action">				
							<button type="button" name="${el}-reload-config-button" id="${el}-reload-config-button">${msg("button.reload-config")}</button>
				         <label for="${el}-reload-config-button">${msg("label.reload-config")}</label>               
				    	</div>
				    	<div class="action">
				    		<button type="button" name="${el}-init-repo-button" id="${el}-init-repo-button">${msg("button.init-repo")}</button>
				    		<label for="${el}-init-repo-button">${msg("label.init-repo")}</label>    		
				    	</div>
				    	<div class="action">
				    		<button type="button" name="${el}-init-acl-button" id="${el}-init-acl-button">${msg("button.init-acl")}</button>
				    		<label for="${el}-init-acl-button">${msg("label.init-acl")}</label>    		
				    	</div>
				    	
					</div>
			    </div>
		   </div>
	</div>
</@>
</@>