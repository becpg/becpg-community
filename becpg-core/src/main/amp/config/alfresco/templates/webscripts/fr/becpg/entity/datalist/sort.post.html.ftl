<#escape x as jsonUtils.encodeJSONString(x)>
{
   <#if origSort ??>origSort : ${origSort},</#if>
   <#if destSort ??>destSort : ${destSort},</#if>
   status : "OK"
}
</#escape>