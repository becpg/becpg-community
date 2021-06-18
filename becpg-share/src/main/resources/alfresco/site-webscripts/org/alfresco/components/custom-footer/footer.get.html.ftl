<@markup id="customFooter-css" target="css"  action="after">
   <#-- CSS Dependencies -->
  <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/basket/basket.css" group="footer"/> 
</@>

<@markup id="customFooter-js"  target="js" action="after">
	
   <@script type="text/javascript" src="${url.context}/res/components/basket/basket.js"  group="footer" />
   
    <#if config.scoped["Analytics"]["providers"]??>
    	<#list config.scoped["Analytics"]["providers"].getChildren("provider")?sort_by(["attributes","index"]) as tmp>
    		<#assign provider=tmp>
    	</#list>
    </#if>
    <#if provider??>
		<#if provider.getChildValue("provider-id") == "google">
			<#if provider.getChildValue("analytics-id")??>
				<script type="text/javascript">
					//<![CDATA[ 
					  var _gaq = _gaq || [];
				  _gaq.push(['_setAccount', '${provider.getChildValue("analytics-id")}']);
				  _gaq.push(['_trackPageview']);
				
				  (function() {
				    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
				    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
				    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
				  })();
				
						//]]>
				</script>
			</#if>
		<#elseif provider.getChildValue("provider-id") == "beCPG">
			<#if provider.getChildValue("provider-url")??>
		  		<img src="${provider.getChildValue("provider-url")}" ></img>
		   </#if>
	   </#if>
   </#if>
</@>

<@markup id="basket"  target="html" action="after">
  <#assign el=args.htmlid?html>
  <div id="${el}-basket" class="basket hidden">
  	  <button class="no-access-check" id="${el}-basket-button" name="basket-button">0</button>
	 <div id="${el}-basket-menu" class="yuimenu" style="visibility:hidden">
      <div class="bd">
			<ul>
				<li><a href="#" ><span class="onActionShowBasket">${msg("menu.basket.show")}</span></a></li>			                
				<li><a href="#" ><span class="onActionDownload">${msg("menu.selected-items.download")}</span></a></li>
				<li><a href="#" ><span class="onActionQuickShare">${msg("menu.selected-items.quick-share")}</span></a></li>
	            <li><a href="#" ><span class="onActionEmptyBasket">${msg("menu.basket.empty")}</span></a></li>
	        </ul>
     </div>
   </div>			
  </div>
</@>
