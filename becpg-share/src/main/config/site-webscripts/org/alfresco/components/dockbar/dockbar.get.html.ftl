
<@markup id="css" >
   <#-- CSS Dependencies -->
  <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/dockbar/dockbar.css" group="dockbar"/>
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
	<@script type="text/javascript" src="${url.context}/res/components/documentlibrary/becpg/fileIcons.js"  group="documentlibrary"></@script>
	<@script type="text/javascript" src="${url.context}/res/components/dockbar/dockbar.js"  group="dockbar"></@script>
</@>


<@markup id="widgets">
  	<@createWidgets group="dockbar"/>
</@>


<@markup id="html">
   <@uniqueIdDiv>
		<#assign el = args.htmlid?html>
		<div class="dockbar">
			<div id="floatLayer">
				<div style="position: static">
					<div id="onglet_outils">
						<div id="onglet_outils_int">
							<div id="onglet_tete">
								<div id="onglet_open_btn">
									<a href="#"  id="${el}-onglet_open_btn_a"  class="clear" >&nbsp;</a>
								</div>
								<div id="onglet_lock_btn">
									<a href="#" id="${el}-onglet_lock_btn_a" class="lock clear">&nbsp;</a>
								</div>
							</div>
							<table id="onglet_contenu" cellpadding="0" cellspacing="0">
								<tr>
									<td id="ombre_angle">&nbsp;</td>
									<td rowspan="2" id="dockbar_content">
										<div id="${el}-products"></div>
									</td>
								</tr>
								<tr>
									<td id="ombre_y">&nbsp;</td>
								</tr>
							</table>
							<div id="onglet_pied"></div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</@>
</@>