<#escape x as jsonUtils.encodeJSONString(x)>
{
   "totalResults": 1,
   "overallSuccess": true,
   "successCount": 1,
   "failureCount": 0,
   "results":
   [      
      {
      	"action": "checkinAsset",
         "id": "${noderef.name}",
         "nodeRef": "${noderef.nodeRef}",
         "success": true
      }
   ]
}
</#escape>