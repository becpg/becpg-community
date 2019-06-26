<#-- Please do not remove -->
<@markup id="google-analytics"  target="js" action="after">
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
