<#escape x as jsonUtils.encodeJSONString(x)>
[
<#list versions as v>
	{
		
		"nodeRef": "${v.nodeRef}",
		"name": "${v.name}",
		"label": "${v.label}",
		"description": "${v.description!''}",
		"createdDate": "${v.createdDate?string("dd MMM yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
		"creator":
		{
			"userName": "${v.creatorUserName}",
			"firstName": "${v.creatorFirstName}",
			"lastName": "${v.creatorLastName}"
		}
	}<#if (v_has_next)>,</#if>
</#list>
]
</#escape>
