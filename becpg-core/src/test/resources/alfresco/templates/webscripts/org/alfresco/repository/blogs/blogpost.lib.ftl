<#import "../generic-paged-results.lib.ftl" as gen/>

<#-- Renders a person object. -->
<#macro renderPerson person fieldName>
<#escape x as jsonUtils.encodeJSONString(x)>
	"${fieldName}":
	{
		<#if person.assocs["cm:avatar"]??>
		"avatarRef": "${person.assocs["cm:avatar"][0].nodeRef?string}",
		</#if>
		"username": "${person.properties["cm:userName"]}",
		"firstName": "${person.properties["cm:firstName"]!""}",
		"lastName": "${person.properties["cm:lastName"]!""}"
	},
</#escape>
</#macro>


<#macro addContent item>
<#escape x as jsonUtils.encodeJSONString(x)>
   <#assign safecontent=stringUtils.stripUnsafeHTML(item.node.content)>
	<#if (contentLength?? && contentLength > -1 && (safecontent?length > contentLength))>
		"content": "${safecontent?substring(0, contentLength)}",
	<#else>
		"content": "${safecontent}",
	</#if>
</#escape>
</#macro>

<#--
	This template renders a blog post.
-->
<#macro blogpostJSON item>
<#escape x as jsonUtils.encodeJSONString(x)>
{
	"url": "blog/post/node/${item.node.nodeRef?replace('://','/')}",
	"commentsUrl": "/node/${item.node.nodeRef?replace('://','/')}/comments",
	"nodeRef": "${item.node.nodeRef}",
	"name": "${item.node.properties.name!''}",
	"title": "${item.node.properties.title!''}",
	<@addContent item=item />
	<#if item.author??>
	<@renderPerson person=item.author fieldName="author" />
	<#else>
	"author":
	{
		"username": "${item.node.properties.creator}"
	},
	</#if>
	"createdOn": "${item.createdDate?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
	"modifiedOn": "${item.modifiedDate?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
	"permissions":
	{
		"edit": ${item.node.hasPermission("Write")?string},
		"delete": ${item.node.hasPermission("Delete")?string}
	},
	"commentCount": ${item.commentCount?c},
	"tags": [<#list item.tags as x>"${x}"<#if x_has_next>, </#if></#list>],
	<#-- draft vs internal published -->
	"isDraft": ${item.isDraft?string},
	<#if (! item.isDraft)>
		"releasedOn": "${item.releasedDate?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
	</#if>
	<#-- true if the post has been updated -->
	"isUpdated": ${item.isUpdated?string},
	<#if (item.isUpdated)>
		"updatedOn": "${item.updatedDate?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
	</#if>
	<#if (item.node.properties["blg:published"]?? && item.node.properties["blg:published"] == true)>
	"publishedOn": "${item.node.properties["blg:posted"]?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
	"updatedOn": "${item.node.properties["blg:lastUpdate"]?string("MMM dd yyyy HH:mm:ss 'GMT'Z '('zzz')'")}",
	"postId": "${item.node.properties["blg:postId"]!''}",
	"postLink": "${item.node.properties["blg:link"]!''}",
	"outOfDate": ${item.outOfDate?string},
	</#if>
	<#-- external publishing - last to make sure that we correctly end the response without a comma -->
	"isPublished": ${(item.node.properties["blg:published"]!'false')?string}
}
</#escape>
</#macro>

<#macro renderPostList>
{
	"metadata":
	{
		"blogPermissions":
		{
			"create": ${blog.hasPermission("CreateChildren")?string},
			"edit": ${blog.hasPermission("Write")?string},
			"delete": ${blog.hasPermission("Delete")?string}
		},
		"externalBlogConfig": ${externalBlogConfig?string}
	},
<@gen.pagedResults data=data ; item>
	<@blogpostJSON item=item />
</@gen.pagedResults>
}
</#macro>

<#macro renderPost>
{
	"metadata":
	{
		"externalBlogConfig": ${externalBlogConfig?string}
	},
	"item": <@blogpostJSON item=item />
}
</#macro>