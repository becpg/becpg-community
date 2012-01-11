<#escape x as jsonUtils.encodeJSONString(x)>
{
  "controls":
   [
   <#list controls as control>
    {
        <#if control.id??>"id":"${control.id}"</#if>
	}<#if control_has_next>,</#if>
   </#list>
   ],
   "sets":[{"id":"panel"},{"id":"bordered-panel"},{"id":"fieldset"},{"id":"title"},{"id":"whitespace"}]
}
</#escape>