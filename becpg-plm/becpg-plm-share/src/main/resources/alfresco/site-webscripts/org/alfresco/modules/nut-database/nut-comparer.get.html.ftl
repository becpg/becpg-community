<#assign el=args.htmlid?html>
<#-- <#assign el="nutComparer"> -->


<div id="${el}-dialog" class="yui-dt">
   <div id="${el}-dialogTitle" class="hd">${msg("nut-compare.title")}</div>
   <div class="bd">
		<div class="body">
			<div id="${el}-tableContainer" class="yui-dt grid">
				<div class="chart detailsChart datagrid nutList" style="visibility: inherit" id="${el}-nuts">
				<#if nutHeaders?? && nuts??>
					<table id="${el}-table">
						<thead>
							<tr class="yui-dt-first yui-dt-last">
							<th class="yui-dt-first"><div class="yui-dt-liner"></div></th>
							<#list nutHeaders as nutHeader>
								<th class="nut-value">
									<div class="yui-dt-liner"><span class="yui-dt-label">${nutHeader?html}</span></div>
								</th>
							</#list>
							</tr>
						</thead>
	
						<tbody class="yui-dt-data">
							<#list nuts as nut>
								<tr class="yui-dt-rec yui-dt-${nut.parity}">
									<td class="yui-dt-first">
										<div class="yui-dt-liner"><span class="nut">${nut.name?html}</span></div>
									</td>
								<#list nut.values as curValue>
									<td class="nut-value"><div class="yui-dt-liner"><#if curValue.value??>${curValue.value}</#if></div></td>
								</#list>
								</tr>
							</#list>
						</tbody>
					</table>
				<#else> 
					<div class="empty"><h3>${msg("nothing-to-compare")}</h3></div>
				</#if>
				</div>
			</div>
		</div>
  </div>
</div>