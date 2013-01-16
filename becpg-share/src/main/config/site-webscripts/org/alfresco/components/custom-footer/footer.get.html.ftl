

<@markup id="google-analytics"  target="js" action="after">
   <#assign fc=config.scoped["Edition"]["footer"]>
	<#if fc.getChildValue("analytics-id")??>
		<script type="text/javascript">
			//<![CDATA[ 
			  var _gaq = _gaq || [];
		  _gaq.push(['_setAccount', '${fc.getChildValue("analytics-id")}']);
		  _gaq.push(['_trackPageview']);
		
		  (function() {
		    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
		    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
		    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
		  })();
		
				//]]>
		</script>
	</#if>
</@>
