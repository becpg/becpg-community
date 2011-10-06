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
		},
		<#if isEntity>
		"startEffectivity": "<#if v.startEffectivity?? >${v.startEffectivity?string("dd MMM yyyy HH:mm:ss 'GMT'Z '('zzz')'")}</#if>",
		"endEffectivity": "<#if v.endEffectivity?? >${v.endEffectivity?string("dd MMM yyyy HH:mm:ss 'GMT'Z '('zzz')'")}</#if>",
		</#if>
		"isEntity":${isEntity?string},
	}<#if (v_has_next)>,</#if>
</#list>
]
</#escape>
