

<@uniqueIdDiv>
   <@markup id="html">
   <#assign el = "docview-"+args.htmlid?html>
   	<script type="text/javascript">//<![CDATA[
	   var docListToolbar =   new beCPG.custom.DocListToolbar("${el}",true).setOptions(
			   { 
         siteId: "",
    	 rootNode : "${args.nodeRef}",
    	 disableSiteMode : true,
         hideNavBar: false,
         repositoryBrowsing: false,
         useTitle: false,
         createContentByTemplateEnabled: false,
         createContentActions: [{"icon": "folder", "index": 5.0, "permission": "", "id": "folder", "label": "create-content.folder", "type": "javascript", "params": {"function": "onNewFolder"}}],
       	 mode: "${args.mode!""}"
       }).setMessages(${messages});
   
   var simpleDocLib =  new beCPG.custom.DocumentList("${el}",true).setOptions(
			   {   
         disableSiteMode : true,		 
         siteId :  "",
         containerId :  "documentLibrary",
         rootNode : "${args.nodeRef}",
         currentPath : <#if args.path?? >"${args.path}"<#else>null</#if>,
         usePagination : true,
         sortAscending : true,
         sortField :  "cm:name",
         showFolders : true,
         hideNavBar:  false,
         simpleView :  "null",
         simpleFormId : "wizard-doclib-simple-metadata",
         viewRenderers: [{"widget": "Alfresco.DocumentListSimpleViewRenderer", "index": 10.0, "id": "simple", "label": "Vue simplifi\u00e9e", "iconClass": "simple"}, {"widget": "Alfresco.DocumentListViewRenderer", "index": 20.0, "id": "detailed", "label": "Vue d\u00e9taill\u00e9e", "iconClass": "detailed"}, {"widget": "Alfresco.DocumentListGalleryViewRenderer", "index": 30.0, "id": "gallery", "label": "Affichage Galerie", "iconClass": "gallery"}, {"widget": "Alfresco.DocumentListFilmstripViewRenderer", "index": 40.0, "id": "filmstrip", "label": "Affichage Pellicule", "iconClass": "filmstrip"}, {"jsonConfig": "{ \"actions\": { \"show\": \"true\" }, \"indicators\": { \"show\": \"true\" }, \"selector\": { \"show\": \"true\" }, \"thumbnail\": { \"show\": \"true\" }, \"propertyColumns\": [ { \"property\": \"name\", \"label\": \"label.name\", \"width\": \"300\" }, { \"property\": \"cm:title\", \"label\": \"label.title\" }, { \"property\": \"cm:description\", \"label\": \"label.description\" }, { \"property\": \"cm:creator\", \"label\": \"label.creator\" }, { \"property\": \"cm:created\", \"label\": \"label.created\" }, { \"property\": \"cm:modifier\", \"label\": \"label.modifier\" }, { \"property\": \"modified\", \"label\": \"label.modified\" } ] }", "widget": "Alfresco.DocumentListTableViewRenderer", "index": 50.0, "id": "table", "label": "Affichage Table", "iconClass": "table"}, {"jsonConfig": "{ \"actions\": { \"show\": \"true\" }, \"indicators\": { \"show\": \"true\" }, \"selector\": { \"show\": \"true\" }, \"thumbnail\": { \"show\": \"true\" }, \"propertyColumns\": [ { \"property\": \"name\", \"label\": \"table.audio.label.name\", \"width\": \"300\" }, { \"property\": \"cm:title\", \"label\": \"table.audio.label.title\", \"width\": \"200\" }, { \"property\": \"audio:album\", \"label\": \"table.audio.label.album\", \"width\": \"200\", \"link\": \"true\" }, { \"property\": \"audio:artist\", \"label\": \"table.audio.label.artist\", \"width\": \"200\" }, { \"property\": \"audio:genre\", \"label\": \"table.audio.label.genre\", \"width\": \"100\" }, { \"property\": \"audio:trackNumber\", \"label\": \"table.audio.label.trackNumber\", \"width\": \"100\" }, { \"property\": \"audio:sampleRate\", \"label\": \"table.audio.label.sampleRate\", \"width\": \"100\" }, { \"property\": \"audio:compressor\", \"label\": \"table.audio.label.compressor\", \"width\": \"100\" } ] }", "widget": "Alfresco.DocumentListTableViewRenderer", "index": 60.0, "id": "audio", "label": "Affichage Audio", "iconClass": "table"}, {"jsonConfig": "{ \"actions\": { \"show\": \"true\" }, \"indicators\": { \"show\": \"true\" }, \"selector\": { \"show\": \"true\" }, \"thumbnail\": { \"show\": \"true\" }, \"propertyColumns\": [ { \"property\": \"name\", \"label\": \"label.name\", \"width\": \"300\" }, { \"property\": \"cm:description\", \"label\": \"label.description\", \"width\": \"200\" }, { \"property\": \"tags\", \"label\": \"label.tags\", \"width\": \"100\" }, { \"property\": \"exif:pixelXDimension\", \"label\": \"label.exif.width\", \"width\": \"75\" }, { \"property\": \"exif:pixelYDimension\", \"label\": \"label.exif.height\", \"width\": \"75\" }, { \"property\": \"size\", \"label\": \"label.size\", \"width\": \"75\" }, { \"property\": \"version\", \"label\": \"label.version\", \"width\": \"50\" } ] }", "widget": "Alfresco.DocumentListTableViewRenderer", "index": 100.0, "id": "media_table", "label": "Affichage Media", "iconClass": "table"}],
         viewRendererName :  "simple",
         viewRendererNames : ["simple", "detailed", "gallery", "filmstrip", "table", "audio", "media_table"],
         highlightFile :  "",
         replicationUrlMapping :"{}",
         repositoryBrowsing : false, 
         useTitle : false,
         userIsSiteManager : false,
         associatedToolbar: { _alfValue: "docListToolbar", _alfType: "REFERENCE" },
         commonComponentStyle : "{ \"browse\":{ \"folder\":[ { \"filter\":{ \"name\":\"aspect\", \"match\":[ \"smf:smartFolder\" ] }, \"style\":{ \"css\":\"icon-smart\", \"icons\":{ \"16x16\":{ \"icon\":\"components\/documentlibrary\/images\/smart-folder-16.png\" }, \"32x32\":{ \"icon\":\"components\/documentlibrary\/images\/smart-folder-32.png\" }, \"48x48\":{ \"icon\":\"components\/documentlibrary\/images\/smart-folder-48.png\" }, \"64x64\":{ \"icon\":\"components\/documentlibrary\/images\/smart-folder-64.png\" }, \"256x256\":{ \"icon\":\"components\/documentlibrary\/images\/smart-folder-256.png\" } } } } ] } }",
         suppressComponent : "{ \"social\":{ \"browse\":{ \"folder\":[ { \"filter\":{ \"name\":\"aspect\", \"match\":[ \"smf:smartFolder\" ], \"name\":\"type\", \"match\":[ \"app:folderlink\" ] } } ], \"file\":[ { \"filter\":{ \"name\":\"type\", \"match\":[ \"app:filelink\" ] } } ] }, \"details\":{ \"folder\":[ { \"filter\":{ \"name\":\"aspect\", \"match\":[ \"smf:smartFolder\" ], \"name\":\"type\", \"match\":[ \"app:folderlink\" ] } } ], \"file\":[ { \"filter\":{ \"name\":\"type\", \"match\":[ \"app:filelink\" ] } } ] } }, \"tags\":{ \"browse\":{ \"folder\":[ { \"filter\":{ \"name\":\"aspect\", \"match\":[ \"smf:smartFolder\" ], \"name\":\"type\", \"match\":[ \"app:folderlink\" ] } } ], \"file\":[ { \"filter\":{ \"name\":\"type\", \"match\":[ \"app:filelink\" ] } } ] } }, \"date\":{ \"browse\":{ \"folder\":[ { \"filter\":{ \"name\":\"aspect\", \"match\":[ \"smf:smartFolder\" ] } } ] }, \"details\":{ \"folder\":[ { \"filter\":{ \"name\":\"aspect\", \"match\":[ \"smf:smartFolder\" ] } } ] } } }",
         mode: "${args.mode!"edit"}"
      
       }).setMessages(${messages});
	
      
     Alfresco.DocumentListViewRenderer.prototype.onEventHighlightRow = function DL_VR_onEventHighlightRow(scope, oArgs, rowElement)
      {
         // Call through to get the row highlighted by YUI
         scope.widgets.dataTable.onEventHighlightRow.call(scope.widgets.dataTable, oArgs);
         
         var targetElement;
         if (rowElement)
         {
            targetElement = rowElement;
         }
         else
         {
            targetElement = oArgs.target;
         }

         // elActions is the element id of the active table cell where we'll inject the actions
         var elActions = Dom.get(scope.id + "-actions-" + targetElement.id);

         // Inject the correct action elements into the actionsId element
         if (elActions && elActions.firstChild === null)
         {
            // Retrieve the actionSet for this record
            var oRecord = scope.widgets.dataTable.getRecord(this.getDataTableRecordIdFromRowElement(scope, targetElement));
            if (oRecord !== null)
            {
               var record = oRecord.getData(),
                  jsNode = record.jsNode,
                  actions = record.actions,
                  actionsEl = document.createElement("div"),
                  actionHTML = "",
                  actionsSel;
   
               record.actionParams = {};
               for (var i = 0, ii = actions.length; i < ii; i++)
               {
                 if(scope.options.mode != "view" && (actions[i].id == "document-upload-new-version" || actions[i].id  == "document-delete") || actions[i].id  == "document-download" || actions[i].id  == "document-edit-properties" ) {
                  	actionHTML += scope.renderAction(actions[i], record);
                  }
               }
   
               // Token replacement - action Urls
               actionsEl.innerHTML = YAHOO.lang.substitute(actionHTML, scope.getActionUrls(record));
   
               // Simple or detailed view
               Dom.addClass(actionsEl, "action-set");
               Dom.addClass(actionsEl, this.actionsCssClassName);
   
               // Need the "More >" container?
               actionsSel = YAHOO.util.Selector.query("div", actionsEl);
               if (actionsSel.length > scope.options.actionsSplitAt + this.actionsSplitAtModifier)
               {
                  var moreContainer = Dom.get(scope.id + "-moreActions").cloneNode(true),
                     containerDivs = YAHOO.util.Selector.query("div", moreContainer);
   
                  // Insert the two necessary DIVs before the third action item
                  Dom.insertBefore(containerDivs[0], actionsSel[scope.options.actionsSplitAt]);
                  Dom.insertBefore(containerDivs[1], actionsSel[scope.options.actionsSplitAt]);
   
                  // Now make action items three onwards children of the 2nd DIV
                  var index, moreActions = actionsSel.slice(scope.options.actionsSplitAt);
                  for (index in moreActions)
                  {
                     if (moreActions.hasOwnProperty(index))
                     {
                        containerDivs[1].appendChild(moreActions[index]);
                     }
                  }
               }
   
               elActions.appendChild(actionsEl);
            }
         }

         if (!Dom.hasClass(document.body, "masked"))
         {
            scope.currentActionsMenu = elActions;
            // Show the actions
            Dom.removeClass(elActions, "hidden");
         }
      };
      
      Alfresco.DocumentList.generateFileFolderLinkMarkup = function DL_generateFileFolderLinkMarkup(scope, record) {
		var jsNode = record.jsNode, recordSite = Alfresco.DocumentList.getRecordSite(record), currentSite = scope.options.siteId, recordPath = record.location.path, recordRepoPath = record.location.repoPath, html;

		if (jsNode.isLink) {
			html = "#";
		} else {
			if (jsNode.isContainer) {

				if (beCPG.util.isEntity(record)) {
	
					html = "#";

				} else {

					// fix for MNT-15347 - browsing non primary folders from
					// another site
					if (currentSite !== "" && recordSite !== null && currentSite !== recordSite) {
						recordPath = scope.currentPath;
					}
					if (record.parent.isContainer || record.node.isContainer) {
						// handle folder parent node
						var location = {};
						location.path = recordPath;
						location.file = record.location.file;
						html = '#" class="filter-change" rel="' + Alfresco.DocumentList.generatePathMarkup(location);
					} else if (recordPath === "/") {
						// handle Repository root parent node (special
						// store_root type - not a folder)
						html = '#" class="filter-change" rel="' + Alfresco.DocumentList.generateFilterMarkup({
							filterId : "path",
							filterData : $combine(recordPath, "")
						});
					} else {
						// handle unknown parent node types
						html = '#';
					}
				}
			} else {
				html = "#";
			}
		}

		return '<a href="' + html + '">';
	};
	
	
	
	 new beCPG.component.DocumentsView("${el}",true).setOptions({}).setMessages(${messages});
	
	
		//]]></script>
   
    <div id="${el}" class="documents simple-document">
    
   <div id="${el}-tb-body" class="toolbar no-check-bg">
      <@markup id="documentListToolbar">
         <div id="${el}-headerBar" class="header-bar flat-button theme-bg-2">
            <@markup id="toolbarLeft">
               <div class="left">
                  <div class="hideable toolbar-hidden DocListTree">
                     <#-- FILE SELECT -->
                     <@markup id="fileSelect">
                        <div class="file-select">
                           <button id="${el}-fileSelect-button" name="doclist-fileSelect-button">${msg("menu.select")}&nbsp;&#9662;</button>
                           <div id="${el}-fileSelect-menu" class="yuimenu">
                              <div class="bd">
                                 <ul>
                                    <li><a href="#"><span class="selectDocuments">${msg("menu.select.documents")}</span></a></li>
                                    <li><a href="#"><span class="selectFolders">${msg("menu.select.folders")}</span></a></li>
                                    <li><a href="#"><span class="selectAll">${msg("menu.select.all")}</span></a></li>
                                    <li><a href="#"><span class="selectInvert">${msg("menu.select.invert")}</span></a></li>
                                    <li><a href="#"><span class="selectNone">${msg("menu.select.none")}</span></a></li>
                                 </ul>
                              </div>
                           </div>
                        </div>
                     </@>
                    <#if (args.mode!"") != "view">
	                     <#-- CREATE CONTENT -->
	                     <@markup id="createContent">
	                     <div class="create-content">
	                        <#if createContent?size != 0 || createContentByTemplateEnabled>
	                           <span id="${el}-createContent-button" class="yui-button yui-push-button">
	                              <span class="first-child">
	                                 <button name="createContent">${msg("button.create-content")}&nbsp;&#9662;</button>
	                              </span>
	                           </span>
	                           <div id="${el}-createContent-menu" class="yuimenu">
	                              <div class="bd"></div>
	                           </div>
	                        </#if>
	                     </div>
	                     </@markup>
         			</#if>
                  </div>
                  
                  <#if (args.mode!"") != "view">
	                  <#-- UPLOAD BUTTON -->
	                  <@markup id="uploadButton">
	                     <#if uploadable>
	                        <div class="hideable toolbar-hidden DocListTree">
	                           <div class="file-upload">
	                              <span id="${el}-fileUpload-button" class="yui-button yui-push-button">
	                                 <span class="first-child">
	                                    <button name="fileUpload">${msg("button.upload")}</button>
	                                 </span>
	                              </span>
	                           </div>
	                        </div>
	                     </#if>
	                  </@>
                  
	                  <#-- CLOUD SYNC BUTTONS -->
	                  <@markup id="cloudSyncButtons">
	                     <div class="hideable toolbar-hidden DocListTree">
	                        <div class="sync-to-cloud">
	                           <span id="${el}-syncToCloud-button" class="yui-button yui-push-button hidden">
	                              <span class="first-child">
	                                 <button name="syncToCloud">${msg("button.sync-to-cloud")}</button>
	                              </span>
	                           </span>
	                        </div>
	                     </div>
	                     <div class="hideable toolbar-hidden DocListTree">
	                        <div class="unsync-from-cloud">
	                           <span id="${el}-unsyncFromCloud-button" class="yui-button yui-push-button hidden">
	                              <span class="first-child">
	                                 <button name="unsyncFromCloud">${msg("button.unsync-from-cloud")}</button>
	                              </span>
	                           </span>
	                        </div>
	                     </div>
	                  </@>
                  </#if>
                  <#-- SELECTED ITEMS MENU -->
                  <@markup id="selectedItems">
                     <div class="selected-items hideable toolbar-hidden DocListTree DocListFilter TagFilter DocListCategories">
                        <button class="no-access-check" id="${el}-selectedItems-button" name="doclist-selectedItems-button">${msg("menu.selected-items")}&nbsp;&#9662;</button>
                        <div id="${el}-selectedItems-menu" class="yuimenu">
                           <div class="bd">
                              <ul>
                              <#list actionSet as action>
                                <#if action.id == "onActionDelete" && (args.mode!"") != "view">
                                  <li><a type="${action.asset!""}" rel="${action.permission!""}" href="${action.href}" data-has-aspects="${action.hasAspect}" data-not-aspects="${action.notAspect}"><span class="${action.id}">${msg(action.label)}</span></a></li>
                              	</#if>
                              </#list>
                                 <li><a href="#"><hr /></a></li>
                                 <li><a href="#"><span class="onActionDeselectAll">${msg("menu.selected-items.deselect-all")}</span></a></li>
                              </ul>
                           </div>
                        </div>
                     </div>
                  </@>
                  <!-- <div id="${el}-paginator" class="paginator"></div> -->
               </div>
            </@>
            <@markup id="toolbarRight">
               <div class="right">
                  <div class="options-select" style="display:none">
                     <button id="${el}-options-button" name="doclist-options-button">${msg("button.options")}&nbsp;&#9662;</button>
                     <div id="${el}-options-menu" class="yuimenu" style="display:none;">
                        <div class="bd">
                           <ul>
                              <@markup id="documentListViewFolderAction">
                                 <#if preferences.showFolders!true>
                                    <li><a href="#"><span class="hideFolders">${msg("button.folders.hide")}</span></a></li>
                                  <#else>
                                    <li><a href="#"><span class="showFolders">${msg("button.folders.show")}</span></a></li>
                                 </#if>
                              </@>
                              <@markup id="documentListViewNavBarAction">
                                 <#if preferences.hideNavBar!false>
                                    <li><a href="#"><span class="showPath">${msg("button.navbar.show")}</span></a></li>
                                 <#else>
                                    <li><a href="#"><span class="hidePath">${msg("button.navbar.hide")}</span></a></li>
                                 </#if>
                              </@>
                              <@markup id="documentListViewRssAction">
                                 <li class="drop-down-list-break-below"><a href="#"><span class="rss">${msg("link.rss-feed")}</span></a></li>
                              </@>
                              <@markup id="documentListViewFullWindowAction">
                                 <li><a href="#"><span class="fullWindow">${msg("button.fullwindow.enter")}</span></a></li>
                              </@>
                              <@markup id="documentListViewFullScreenAction">
                                 <li class="drop-down-list-break-below"><a href="#"><span class="fullScreen">${msg("button.fullscreen.enter")}</span></a></li>
                              </@>
                              <@markup id="documentListViewRendererSelect">
                                <#if viewRenderers??>
                                   <#list viewRenderers as viewRenderer>
                                      <li class="${viewRenderer.iconClass}<#if !viewRenderer_has_next> drop-down-list-break-below</#if>"><a href="#"><span class="view ${viewRenderer.id}">${msg(viewRenderer.label)}</span></a></li>
                                   </#list>
                                </#if>
                              </@>
                              <@markup id="documentListViewDefaultViewActions">
                                 <li><a href="#"><span class="removeDefaultView">${msg("button.removeDefaultView")}</span></a></li>
                                 <li><a href="#"><span class="setDefaultView">${msg("button.setDefaultView")}</span></a></li>
                              </@>
                           </ul>
                        </div>
                     </div>
                  </div>
                  <@markup id="documentListSortSelect">
                    <div class="sort-field">
                       <span id="${el}-sortField-button" class="yui-button yui-push-button">
                          <span class="first-child">
                             <button name="doclist-sortField-button"></button>
                          </span>
                       </span>
                       <!-- <span class="separator">&nbsp;</span> -->
                       <select id="${el}-sortField-menu">
                       <#list sortOptions as sort>
                          <option value="${(sort.value!"")?html}" <#if sort.direction??>title="${sort.direction?string}"</#if>>${msg(sort.label)}</option>
                       </#list>
                       </select>
                    </div>
                    <div class="sort-direction">
                       <span id="${el}-sortAscending-button" class="yui-button yui-push-button">
                          <span class="first-child">
                             <button name="doclist-sortAscending-button"></button>
                          </span>
                       </span>
                    </div>
                  </@>
                  <@markup id="galleryViewSlider">
                    <div id="${el}-gallery-slider" class="alf-gallery-slider hidden">
                       <div class="alf-gallery-slider-small"><img src="${url.context}/res/components/documentlibrary/images/gallery-size-small-16.png"></div>
                       <div id="${el}-gallery-slider-bg" class="yui-h-slider alf-gallery-slider-bg"> 
                       <div id="${el}-gallery-slider-thumb" class="yui-slider-thumb alf-gallery-slider-thumb"><img src="${url.context}/res/components/documentlibrary/images/thumb-n.png"></div> 
                    </div>
                    <div class="alf-gallery-slider-large"><img src="${url.context}/res/components/documentlibrary/images/gallery-size-large-16.png"></div>
                    </div>
                  </@>
               </div>
            </@>
         </div>
      </@>
      
      <@markup id="navigationBar">
         <div id="${el}-navBar" class="nav-bar flat-button theme-bg-2">
            <div class="hideable toolbar-hidden DocListTree DocListCategories">
               <div class="folder-up">
                  <span id="${el}-folderUp-button" class="yui-button yui-push-button">
                     <span class="first-child">
                        <button class="no-access-check" name="folderUp"></button>
                     </span>
                  </span>
               </div>
               <div class="separator">&nbsp;</div>
            </div>
            <div id="${el}-breadcrumb" class="breadcrumb hideable toolbar-hidden DocListTree DocListCategories"></div>
            <div id="${el}-description" class="description hideable toolbar-hidden DocListFilter TagFilter"></div>
         </div>
      </@>
   
   </div>
   <!--[if IE]>
      <iframe id="yui-history-iframe" src="${url.context}/res/yui/history/assets/blank.html"></iframe>
   <![endif]-->
   <input id="yui-history-field" type="hidden"></input>
   <div id="${el}-dl-body" class="doclist no-check-bg">
	
   
      <#--
         INFORMATION TEMPLATES
      -->
      <div id="${el}-main-template" class="hidden">
         <div>
         </div>
      </div>
   
      <#-- No items message -->
      <div id="${el}-no-items-template" class="hidden">
         <div class="docListInstructionTitle">${msg("no.items.title")}</div>
      </div>
   
      <#-- Hidden sub-folders message -->
      <div id="${el}-hidden-subfolders-template" class="hidden">
         <div class="docListInstructionTitle">${msg("no.items.title")}</div>
         <div id="${el}-show-folders-template" class="docListInstructionColumn">
            <img class="docListInstructionImage docListLinkedInstruction" src="${url.context}/res/components/documentlibrary/images/help-folder-48.png">
            <a class="docListInstructionTextSmall docListLinkedInstruction"><#-- We don't know the number of hidden subfolders at this point so this needs to be inserted --></a>
         </div>
      </div>
      <#if (args.mode!"") != "view">
	      <#-- HTML 5 drag and drop instructions -->
	      <div id="${el}-dnd-instructions-template" class="hidden">
	         <div id="${el}-dnd-instructions">
	            <span class="docListInstructionTitle">${msg("dnd.drop.title")}</span>
	            <div>
	               <div class="docListInstructionColumn docListInstructionColumnRightBorder">
	                  <img class="docListInstructionImage" src="${url.context}/res/components/documentlibrary/images/help-drop-list-target-96.png">
	                  <span class="docListInstructionText">${msg("dnd.drop.doclist.description")}</span>
	               </div>
	               <div class="docListInstructionColumn">
	                  <img class="docListInstructionImage" src="${url.context}/res/components/documentlibrary/images/help-drop-folder-target-96.png">
	                  <span class="docListInstructionText">${msg("dnd.drop.folder.description")}</span>
	               </div>
	               <div style="clear:both"></div>
	            </div>
	         </div>
	      </div>
	      
	      <#-- Standard upload instructions -->
	      <div id="${el}-upload-instructions-template" class="hidden">
	         <div class="docListInstructionTitle">${msg("standard.upload.title")}</div>
	         <div id="${el}-standard-upload-link-template" class="docListInstructionColumn">
	            <img class="docListInstructionImage docListLinkedInstruction" src="${url.context}/res/components/documentlibrary/images/help-upload-96.png">
	            <span class="docListInstructionText"><a class="docListLinkedInstruction">${msg("standard.upload.description")}</a></span>
	         </div>
	      </div>
	   
	      <#-- Other options? -->
	      <div id="${el}-other-options-template" class="hidden">
	         <div class="docListOtherOptions">${msg("other.options")}</div>
	      </div>
	   
	      <#-- The following DOM structures should be editing with respect to documentlist.js function
	           fired by the Doclists "tableMsgShowEvent" as it uses this structure to associate the
	           image and anchor with the appropriate actions. NOTE: This is only a template that will
	           be cloned, during the cloning the id will be appended with "-instance" to ensure uniqueness
	           within the page, this allows us to locate each DOM node individually. -->
	   
	      <#-- Standard upload (when user has create access) -->
	      <div id="${el}-standard-upload-template" class="hidden">
	        <div id="${el}-standard-upload-link-template">
	           <img class="docListOtherOptionsImage docListLinkedInstruction" src="${url.context}/res/components/documentlibrary/images/help-upload-48.png">
	           <span class="docListOtherOptionsText"><a class="docListLinkedInstruction">${msg("dnd.upload.description")}</a></span>
	        </div>
	      </div>
	   
	      <#-- New Folder (when user has create access) -->
	      <div id="${el}-new-folder-template" class="hidden">
	        <div id="${el}-new-folder-link-template">
	           <img class="docListOtherOptionsImage docListLinkedInstruction" src="${url.context}/res/components/documentlibrary/images/help-new-folder-48.png">
	           <span class="docListOtherOptionsText"><a class="docListLinkedInstruction">${msg("dnd.newfolder.description")}</a></span>
	        </div>
	      </div>
	   
	      <#-- Hidden sub-folders message -->
	      <div id="${el}-show-folders-template" class="hidden">
	         <img class="docListOtherOptionsImage docListLinkedInstruction" src="${url.context}/res/components/documentlibrary/images/help-folder-48.png">
	         <span class="docListOtherOptionsText"><a class="docListLinkedInstruction"><#-- We don't know the number of hidden subfolders at this point so this needs to be inserted --></a></span>
	      </div>
   	</#if>
	      <#--
	         END OF INFORMATION TEMPLATES
	      -->
      <#-- Top Bar: Select, Pagination, Sorting & View controls -->
      <div id="${el}-doclistBar" class="yui-gc doclist-bar flat-button no-check-bg"></div>
      <div class="alf-fullscreen-exit-button" class="hidden">
        <span class="yui-button">
            <span class="first-child">
                <button type="button" title="${msg("button.fullscreen.exit")}" id="${el}-fullscreen-exit-button"></button>
            </span>
         </span>
      </div>
   
      <#-- Main Panel: Document List -->
      <@markup id="documentListContainer">
      <div id="${el}-documents" class="documents"></div>
      <div id="${el}-gallery" class="alf-gallery documents"></div>
      <div id="${el}-gallery-empty" class="hidden documents">
         <div class="yui-dt-liner"></div>
      </div>
      <div id="${el}-filmstrip" class="alf-filmstrip alf-gallery documents">
            <div id="${el}-filmstrip-main-content" class="alf-filmstrip-main-content">
                <div id="${el}-filmstrip-carousel"></div>
                <div id="${el}-filmstrip-nav-main-previous" class="alf-filmstrip-nav-button alf-filmstrip-main-nav-button alf-filmstrip-nav-prev">
                    <img src="${url.context}/res/components/documentlibrary/images/filmstrip-main-nav-prev.png" />
                </div>
                <div id="${el}-filmstrip-nav-main-next" class="alf-filmstrip-nav-button alf-filmstrip-main-nav-button alf-filmstrip-nav-next">
                    <img src="${url.context}/res/components/documentlibrary/images/filmstrip-main-nav-next.png" />
                </div>
            </div>
            <div id="${el}-filmstrip-nav" class="alf-filmstrip-nav">
                <div id="${el}-filmstrip-nav-handle" class="alf-filmstrip-nav-handle"></div>
                <div id="${el}-filmstrip-nav-carousel"></div>
                <div id="${el}-filmstrip-nav-buttons" class="alf-filmstrip-nav-buttons">
                    <div id="${el}-filmstrip-nav-previous" class="alf-filmstrip-nav-button alf-filmstrip-nav-prev">
                        <img src="${url.context}/res/components/documentlibrary/images/filmstrip-content-nav-prev.png" />
                    </div>
                    <div id="${el}-filmstrip-nav-next" class="alf-filmstrip-nav-button alf-filmstrip-nav-next">
                        <img src="${url.context}/res/components/documentlibrary/images/filmstrip-content-nav-next.png" />
                    </div>
                </div>
            </div>
       </div>
      </@>
   
      <#-- Bottom Bar: Paginator -->
      <div id="${el}-doclistBarBottom" class="yui-gc doclist-bar doclist-bar-bottom flat-button">
         <div class="yui-u first">
            <div class="file-select">&nbsp;</div>
            <div id="${el}-paginatorBottom" class="paginator"></div>
         </div>
      </div>
   
      <#--
         RENDERING TEMPLATES
      -->
      <div style="display: none">
   
         <#-- Action Set "More" template -->
         <div id="${el}-moreActions">
            <div class="internal-show-more" id="onActionShowMore"><a href="#" class="show-more" alt="${msg("actions.more")}" aria-haspopup="true"><span>${msg("actions.more")}</span></a></div>
            <div class="more-actions hidden"></div>
         </div>
   
         <#-- Document List Gallery View Templates-->
         <div id="${el}-gallery-item-template" class="alf-gallery-item hidden">
            <div class="alf-gallery-item-thumbnail">
               <div class="alf-header">
                  <div class="alf-select"></div>
                     <a href="javascript:void(0)" class="alf-show-detail">&nbsp;</a>
               </div>
               <div class="alf-label"></div>
            </div>
            <div class="alf-detail" style="display: none;">
               <div class="bd">
                  <div class="alf-detail-thumbnail"></div>
                  <div class="alf-status"></div>
                  <div class="alf-actions"></div>
                  <div style="clear: both;"></div>
                  <div class="alf-description"></div>
               </div>
            </div>
         </div>
         
         <#-- Document List Filmstrip View Templates -->
           <div id="${el}-filmstrip-nav-item-template" class="alf-filmstrip-nav-item hidden">
              <div class="alf-filmstrip-nav-item-thumbnail">
                 <div class="alf-label"></div>
              </div>
           </div>
           <div id="${el}-filmstrip-item-template" class="alf-gallery-item hidden">
              <div class="alf-gallery-item-thumbnail">
                 <div class="alf-header">
                    <div class="alf-select"></div>
                    <a href="javascript:void(0)" class="alf-show-detail">&nbsp;</a>
                    <div class="alf-label"></div>
                 </div>
              </div>
              <div class="alf-detail">
                  <div class="bd">
                      <div class="alf-status"></div>
                      <div class="alf-actions"></div>
                      <div style="clear: both;"></div>
                      <div class="alf-description"></div>
                  </div>
              </div>
           </div>
   
      </div>
   
   </div>
</div>   
   
   </@>
</@>
