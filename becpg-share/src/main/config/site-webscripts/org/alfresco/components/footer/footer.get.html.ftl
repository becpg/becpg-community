<#assign fc=config.scoped["Edition"]["footer"]>
<#if fc.getChildValue("analytics-id")??>
<script type="text/javascript">

  var _gaq = _gaq || [];
  _gaq.push(['_setAccount', '${fc.getChildValue("analytics-id")}']);
  _gaq.push(['_trackPageview']);

  (function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
  })();

</script>
</#if>
<div class="footer ${fc.getChildValue("css-class")!"footer-com"}">
   <span class="copyright">
      <img src="${url.context}/components/images/${fc.getChildValue("logo")!"alfresco-share-logo.png"}" alt="${fc.getChildValue("alt-text")!"Alfresco Community"}" height="27" width="212" />
      <span>${msg(fc.getChildValue("label")!"label.copyright")}</span>
   </span>
</div>
