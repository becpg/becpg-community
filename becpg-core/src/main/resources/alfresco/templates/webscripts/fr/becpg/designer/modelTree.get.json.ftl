<#import "designer.lib.ftl" as designerLib />
<#escape x as jsonUtils.encodeJSONString(x)>
{
  <@designerLib.render tree/>
}
</#escape>