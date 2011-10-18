<script type="text/javascript">//<![CDATA[
   new beCPG.component.OlapChart("${args.htmlid}", "${instance.object.id}");
   new Alfresco.widget.DashletResizer("${args.htmlid}", "${instance.object.id}");
//]]></script>
<div class="dashlet my-olapchart">
  <div class="title">${msg("header.myOlapChart")}</div>
  <div class="feed">
	  <a class="olapChart-link-bi" href="/saiku-ui/" title="${msg("link.access.bi")}">&nbsp;</a>
  </div>
  <div class="toolbar  flat-button">   
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
			<div class="chart olapChart" id="${args.htmlid}-chart" >${msg("body.msg")}</div>
		</div>
	</div>
</div>
