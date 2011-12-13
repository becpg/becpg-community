<#escape x as jsonUtils.encodeJSONString(x)>
{
   "totalRecords": ${totalRecords?c},
   "startIndex": ${startIndex?c},
   "metadata":
   {
      "parent":
      {
      }
   },
   "items":
   [   	
      <#list wUsedItems as wUsedItem>
      {
      	"itemData":
         {         
         	<#if wusedType="compoList">
         		<@renderCompoList wUsedItem />
		   	<#elseif wusedType="packagingList">
		   		<@renderPackagingList wUsedItem />
		   	</#if>         
         }
      }<#if wUsedItem_has_next>,</#if>
      </#list>
   ]
}
</#escape>

<#macro renderCompoList wUsedItem>

	"prop_bcpg_depthLevel":         	
	<@renderProperty wUsedItem.getDepthLevel()!"" />,
	
	"assoc_bcpg_compoListProduct":
	<@renderProduct wUsedItem.getProduct()!"" />,         	         	
	
	"prop_bcpg_compoListQty":
	<@renderProperty wUsedItem.getQty()!"" />,
	
	"prop_bcpg_compoListQtySubFormula":
	<@renderProperty wUsedItem.getQtySubFormula()!"" />,
	
	"prop_bcpg_compoListQtyAfterProcess":
	<@renderProperty wUsedItem.getQtyAfterProcess()!"" />,

	"prop_bcpg_compoListUnit":
	<@renderProperty wUsedItem.getCompoListUnit()!"" />,
	
	"prop_bcpg_compoListLossPerc":
	<@renderProperty wUsedItem.getLossPerc()!"" />,
	
	"prop_bcpg_compoListDeclGrp":
	<@renderProperty wUsedItem.getDeclGrp()!"" />,

	"prop_bcpg_compoListDeclType":
	<@renderProperty wUsedItem.getDeclType()!"" />
	
</#macro>

<#macro renderPackagingList wUsedItem>
	
	"assoc_bcpg_packagingListProduct":
	<@renderProduct wUsedItem.getProduct()!"" />,         	         	
	
	"prop_bcpg_packagingListQty":
	<@renderProperty wUsedItem.getQty()!"" />,

	"prop_bcpg_packagingListUnit":
	<@renderProperty wUsedItem.getPackagingListUnit()!"" />,

	"prop_bcpg_packagingListDeclType":
	<@renderProperty wUsedItem.getPkgLevel()!"" />
	
</#macro>

<#macro renderProperty data>   
{
      <#if data?is_boolean>
   "value": ${data?string},
      <#elseif data?is_number>
   "value": ${data?c},
      <#else>
   "value": "${data}",
      </#if>          
      <#if data?is_boolean>
   "displayValue": ${data?string}
      <#elseif data?is_number>
   <#--beCPG -PQU : we want to display only 1/2/3 digits and not 8 digits, we force us separator (point instead of comma, and replace comma by space)-->
 	<#setting locale="en_US">
   "displayValue": "${"${data}"?replace(',', ' ')}"
      <#else>
   "displayValue": "${data}"
      </#if>      
}
</#macro>

<#macro renderProduct data>
<#assign node = companyhome.nodeByReference[data]>
{

   "value": "${data?string}",
   "metadata": "${node.type?replace('{http://www.bcpg.fr/model/becpg/1.0}','')}",    
   <@getSiteId node />
   "displayValue": "${node.name}"
}
</#macro>

<#macro getSiteId node>
<#if node.qnamePath?contains("/app:company_home/st:sites/")>
	<#assign tmp = node.qnamePath?replace("/app:company_home/st:sites/","")>
	<#assign pos = tmp?index_of("/")>
	<#if (pos>0) >
		<#assign arrDisplayPath = node.displayPath?split("/")>
		"siteId": "${arrDisplayPath[3]}",
	</#if> 	
</#if>   
</#macro>