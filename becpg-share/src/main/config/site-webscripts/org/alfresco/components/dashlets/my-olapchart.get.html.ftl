<#assign id = args.htmlid>
<#assign jsid = args.htmlid?js_string>
<script type="text/javascript">//<![CDATA[
   var olapChart = new beCPG.dashlet.OlapChart("${jsid}").setOptions(
   {
      siteId: "${page.url.templateArgs.site!""}",
      regionId: "${args['region-id']?js_string}"
   }).setMessages(${messages});
 
   new Alfresco.widget.DashletResizer("${jsid}", "${instance.object.id}");

   var saikuAccessEvent = new YAHOO.util.CustomEvent("openSaikuClick");
   saikuAccessEvent.subscribe(olapChart.openSaikuClick, olapChart, true);
   
    new Alfresco.widget.DashletTitleBarActions("${args.htmlid}").setOptions(
   {
      actions:
      [
         {
            cssClass: "saiku",
            eventOnClick: saikuAccessEvent,
            tooltip: "${msg("link.access.bi")?js_string}"
         },
         {
            cssClass: "help",
            bubbleOnClick:
            {
               message: "${msg("dashlet.help")?js_string}"
            },
            tooltip: "${msg("dashlet.help.tooltip")?js_string}"
         }
      ]
   });
   
//]]></script>
<div class="dashlet my-olapchart">
  <div class="title">${msg("header.myOlapChart")}</div>
  <div class="toolbar flat-button">   
      <input id="${args.htmlid}-charPicker-button" type="button" name="${args.htmlid}-charPicker-button" value="${msg("button.chart.choose")}" ></input>
      <select id="${args.htmlid}-charPicker-select" name="${args.htmlid}-charPicker-select"></select>
      <input id="${args.htmlid}-chartTypePicker-button" type="button" name="${args.htmlid}-chartTypePicker-button" value="${msg("button.chart.choose")}" ></input>
      <select id="${args.htmlid}-chartTypePicker-select" name="${args.htmlid}-chartTypePicker-select">
      	<option value="barChart">${msg("picker.chart.type.bar")}</option>
      	<option value="lineChart">${msg("picker.chart.type.line")}</option>
      	<option value="columnChart">${msg("picker.chart.type.column")}</option>
      	<option value="pieChart">${msg("picker.chart.type.pie")}</option>
      	<option value="chartData">${msg("picker.chart.type.data")}</option>
      </select>
   </div>
	<div class="body " >
		<div id="${args.htmlid}-chartContainer" class="yui-content">
			<div class="chart olapChart" id="${args.htmlid}-chart" >
				<div class="empty"><h3>${msg("empty.title")}</h3><span>${msg("empty.description")}</span></div>
			</div>
		</div>
	</div>
</div>
