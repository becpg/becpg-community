<#include "../../include/alfresco-macros.lib.ftl" />
<script type="text/javascript">//<![CDATA[
  var languageForm =  new Alfresco.component.ShareFormManager("${args.htmlid}").setOptions(
   {
      failureMessage: "user-language-mgr.update.failed",
      defaultUrl: "${siteURL("profile")}"
   }).setMessages(${messages});

   
   languageForm.onFormSubmitSuccess =  function (response)
    {
    	var me = this;
    
       Alfresco.util.Ajax.request(
	   {
	      method: Alfresco.util.Ajax.GET,
	      url: Alfresco.constants.URL_PAGECONTEXT+"user/admin/profile?resetLocale=true",
	      successCallback: {
	       fn : function (){
	      	 me.navigateForward(true);
	      	},
	      	scope: this
	      }
	    });
    };
   
//]]></script>
<div class="form-manager">
</div>