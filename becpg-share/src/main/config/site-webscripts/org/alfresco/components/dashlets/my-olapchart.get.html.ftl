<script type="text/javascript">//<![CDATA[
   new beCPG.component.OlapChart("${args.htmlid}").setOptions(
   {
      
   });
  // new Alfresco.widget.DashletResizer("${args.htmlid}", "${instance.object.id}");
//]]></script>
<div class="dashlet my-olapchart">
  <div class="title">${msg("header.myOlapChart")}</div>
  <div class="toolbar  flat-button">
      <span class="yui-button-align">
         <span class="first-child">
            <a href="#" id="${args.htmlid}-createOlaChart-button" class="theme-color-1">${msg("link.createOlapGraph")}</a>
         </span>
      </span>
      <input id="${args.htmlid}-charPicker-button" type="button" name="charPicker-button" value="${msg("button.chart.choose")}" ></input>
      <select id="${args.htmlid}-charPicker-select"></select>
   </div>
	<div class="body" style="min-width:200px; min-height:200px;">
		<div id="${args.htmlid}-chart" >Unable to load Flash content. The YUI Charts Control requires Flash Player 9.0.45 or higher. 
			You can download the latest version of Flash Player from the <a href="http://www.adobe.com/go/getflashplayer">Adobe Flash Player Download Center</a>.</p>
		</div>
	</div>
</div>
