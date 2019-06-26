<#assign el=args.htmlid?html>

<div id="${el}-dialog" class="change-type">
   <div id="${el}-dialogTitle" class="hd">${msg("title")}</div>
   <div class="bd">
      <form id="${el}-form" action="" method="post" class="form-container">
         <div class="form-fields">
	         <div class="set">
		           <div class="form-field">
					<label for="${el}-processScript">${msg("label.processScript.choose")}:<span class="mandatory-indicator">*</span></label>
					<select id="${el}-processScript" name="processScriptNodeRef" >
				         <#list processScripts as processScript>
				          <option value="${processScript.nodeRef}" >${processScript.title}</option>
				        </#list>
				    </select>
				</div>
			</div>
         <div class="bdft">
            <input type="button" id="${el}-ok" value="${msg("button.ok")}" tabindex="0" />
            <input type="button" id="${el}-cancel" value="${msg("button.cancel")}" tabindex="0" />
         </div>
      </form>
   </div>
</div>

