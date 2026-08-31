{
   "status": "${scanStatus!""}",
   "message": "${msgText!""}",
   "productFound": <#if productFound??>${productFound?string("true", "false")}<#else>false</#if>,
   "batchIdFound": <#if batchIdFound??>${batchIdFound?string("true", "false")}<#else>false</#if>,
   "codeErp": "${codeErp!""}",
   "productName": "${(productName!"")?js_string}",
   "batchId": "${(batchId!"")?js_string}"
}