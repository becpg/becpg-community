{
    "userName": "${userName}",
    "groups": [
    <#list groups as group>
        "${group.properties.authorityName}" <#if group_has_next>,</#if>
    </#list>
    ]
}