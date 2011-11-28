<#import "designer.lib.ftl" as designerLib />
<#escape x as jsonUtils.encodeJSONString(x)>
{
    <#if redirect??>"redirect": "${redirect}",</#if>
    <#if persistedObject??>"persistedObject": "${persistedObject}",</#if>
    <#if assocName??>"assocName": "${assocName}",</#if>
    <#if treeNode??>"treeNode" : {
    	<@designerLib.render treeNode/>
    }</#if>
}
</#escape>