
<@markup id="css" >
   <#-- CSS Dependencies -->
  <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/contextual-help/contextual-help.css" group="contextual-help" />
  <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/contextual-help/opentip.css" group="contextual-help" />
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
    <@script type="text/javascript" src="${url.context}/res/components/contextual-help/opentip-native.js"  group="contextual-help"></@script>
	<@script type="text/javascript" src="${url.context}/res/components/contextual-help/contextual-help.js"  group="contextual-help"></@script>
</@>

<@markup id="widgets">
  	<@createWidgets group="contextual-help"/>
</@>