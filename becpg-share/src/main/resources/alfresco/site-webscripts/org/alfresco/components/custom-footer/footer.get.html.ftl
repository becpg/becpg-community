<@markup id="customFooter-css" target="css"  action="after">
   <#-- CSS Dependencies -->
  <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/basket/basket.css" group="footer"/> 
  <#if isAIEnable??>
  <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/watson/watson.css" group="footer"/>
  </#if>
</@>

<@markup id="customFooter-js"  target="js" action="after">
	
   <@script type="text/javascript" src="${url.context}/res/components/basket/basket.js"  group="footer" />
   <#if isAIEnable??>
   <@script type="text/javascript" src="${url.context}/res/components/watson/watson.js" group="footer"/>
   </#if>
   
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


<@markup id="watson"  target="html" action="after">
<#if isAIEnable??>
	<div id="watson-container" class="expanded round">
		<div id="watson-chatbot-container">
			<div id="watson-chatbot-header">
				<svg xmlns="http://www.w3.org/2000/svg" width="60" height="60" viewBox="0 0 100 100">
					<g fill-rule="nonzero" fill="none">
					<path
						d="M35.203 35.373l.007-.001c7.049-7.748 18.925-8.657 27.074-2.073h-.001l11.238-11.225.008.007a35.67 35.67 0 00-33.89-7.526 35.613 35.613 0 00-24.218 24.841 25.635 25.635 0 0119.782-4.023z"
						fill="#EA4335"></path>
					<path
						d="M84.282 39.397a35.666 35.666 0 00-10.753-17.316l-.008-.007-11.238 11.224.001.001a19.751 19.751 0 017.384 15.397v1.98a9.911 9.911 0 018.588 4.946 9.885 9.885 0 01-.001 9.9 9.911 9.911 0 01-8.589 4.946H49.834V86.3h19.834c11.28.005 21.25-7.321 24.6-18.077 3.352-10.756-.696-22.439-9.986-28.827z"
						fill="#4285F4"></path>
					<path d="M49.778 70.468H30.035a9.847 9.847 0 01-4.083-.89l.014.007-11.49 11.476a25.648 25.648 0 0015.556 5.24h19.802V70.468h-.056z" fill="#34A853"></path>
					<path
						d="M35.203 35.373C23.42 32.953 11.53 39.005 6.565 49.948c-4.964 10.943-1.68 23.861 7.91 31.114l11.49-11.477-.013-.006a9.887 9.887 0 01-5.728-10.413 9.896 9.896 0 018.418-8.394 9.905 9.905 0 0110.417 5.737l11.49-11.477a25.807 25.807 0 00-15.346-9.66z"
						fill="#FBBC05"></path></g></svg>
				<div class="header-content">
					<h2>${msg("watson.welcome.message")}</h2>
				</div>
			</div>
			<div id="watson-chatbot-chat-frame" class="transition show">
				<iframe
					src="${url.context}/proxy/ai/watson/chat" referrerpolicy="origin" sandbox="allow-scripts allow-forms allow-same-origin allow-popups allow-popups-to-escape-sandbox"></iframe>
			</div>
			<div id="watson-chatbot-footer">
				<div class="footer-content">
					<p>${msg("watson.footer.message")}</p>
				</div>
			</div>
			<button id="watson-chatbot-chat-activate-bar" class="chat-activate-bar">
				<svg width="48" height="48" viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg">
					<g fill="none" fill-rule="evenodd">
					<circle fill="#00ffbd" cx="24" cy="24" r="24"></circle>
					<path d="M12 12h24v24H12z"></path>
					<path d="M32 14H16c-1.1 0-1.99.9-1.99 2L14 34l4-4h14c1.1 0 2-.9 2-2V16c0-1.1-.9-2-2-2zm0 14H16V16h16v12zm-14-4h8v2h-8v-2zm0-3h12v2H18v-2zm0-3h12v2H18v-2z" fill="#FFF"
						fill-rule="nonzero"></path></g></svg>
				<div class="icon-close">
					<svg xmlns="http://www.w3.org/2000/svg" width="18" height="24" viewBox="0 0 24 24">
						<path d="M0 10h18v4h-24z"></path></svg>
				</div>
			</button>
		</div>
	</div>
</#if>	
</@>
