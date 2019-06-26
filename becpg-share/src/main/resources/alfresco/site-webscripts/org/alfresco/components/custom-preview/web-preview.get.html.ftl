<@markup id="customWebPreview-js" target="js" action="before">
	<@inlineScript group="web-preview">
			beCPG.constants.SHOW_DOWNLOAD_LINKS = ${showAdditionalDownloadLinks?string};
		    beCPG.constants.IS_REPORT =  ${isReport?string}
	  </@>
</@markup>


