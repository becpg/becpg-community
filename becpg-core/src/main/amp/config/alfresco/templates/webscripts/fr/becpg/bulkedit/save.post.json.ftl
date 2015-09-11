<#escape x as jsonUtils.encodeJSONString(x)>
{
    <#if newValue?is_number>"value": ${newValue?c}"<#else>"value": ${newValue?string}</#if>
}
</#escape>