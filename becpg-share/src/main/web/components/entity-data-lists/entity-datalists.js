/**
 * Entity Data Lists: EntityDataLists component. Displays a list of entitydatalists
 * 
 * @namespace beCPG
 * @class beCPG.component.EntityDataLists
 */
(function() {
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom, Event = YAHOO.util.Event, Selector = YAHOO.util.Selector;

   /**
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML, $combine = Alfresco.util.combinePaths;

   /**
    * EntityDataLists constructor.
    * 
    * @param htmlId
    *            {String} The HTML id of the parent element
    * @return {beCPG.component.EntityDataLists} The new EntityDataLists instance
    * @constructor
    */
   beCPG.component.EntityDataLists = function(htmlId) {
      return beCPG.component.EntityDataLists.superclass.constructor.call(this, htmlId);
   };

   /**
    * Extend from Alfresco.component.DataLists
    */
   YAHOO
         .extend(
               beCPG.component.EntityDataLists,
               Alfresco.component.DataLists,
               {
                  /**
                   * Current entity
                   */
                  entity : null,
                  /**
                   * Object container for initialization options
                   * 
                   * @property options
                   * @type object
                   */
                  options : {
                     // ### beCPG : replace containerId, siteId by entityNodeRef
                     /**
                      * Current entityNodeRef.
                      * 
                      * @property entityNodeRef
                      * @type string
                      * @default ""
                      */
                     entityNodeRef : "",

                     /**
                      * ListId representing currently selected list
                      * 
                      * @property listId
                      * @type string
                      */
                     listId : "",

                     /**
                      * List types when creating new Data Lists
                      * 
                      * @property listTypes
                      * @type Array
                      */
                     listTypes : []
                  },

                  /**
                   * Fired by YUI when parent element is available for scripting.
                   * 
                   * @method onReady
                   */
                  onReady : function DataLists_onReady() {

                     this.widgets.spinner = new YAHOO.widget.Panel(this.id + "-waitPanel", {
                        width : "240px",
                        fixedcenter : true,
                        close : false,
                        draggable : false,
                        zindex : 99,
                        modal : true,
                        visible : false
                     });

                     this.widgets.spinner.setHeader(this.msg("message.loading"));
                     this.widgets.spinner
                           .setBody('<div style="text-align:center;"><img src="' + Alfresco.constants.URL_RESCONTEXT + '/components/images/rel_interstitial_loading.gif" /></div>');
                     this.widgets.spinner.render(document.body);

                     this.widgets.newList = Alfresco.util.createYUIButton(this, "newListButton", this.onNewList, {
                        disabled : true
                     });
                     // Retrieve the lists from the specified Site & Container
                     this.populateDataLists({
                        fn : function DataLists_onReady_callback() {
                           this.renderDataLists();

                           // Select current list, if relevant
                           if (this.options.listId.length > 0) {
                              YAHOO.Bubbling.fire("activeDataListChanged", {
                                 list : this.options.listId,
                                 dataList : this.dataLists[this.options.listId],
                                 entity : this.entity,
                                 scrollTo : true
                              });
                           } else {
                              YAHOO.Bubbling.fire("hideFilter");
                           }

                           if (this.dataListsLength === 0 || window.location.hash === "#new") {
                              this.widgets.newList.fireEvent("click");
                           }
                        },
                        scope : this
                     });
                  },

                  /**
                   * Retrieves the Data Lists from the Repo
                   * 
                   * @method populateDataLists
                   * @param callback
                   *            {Object} Optional callback literal {fn, scope, obj} whose function is invoked once list
                   *            has been retrieved
                   */
                  populateDataLists : function DataLists_populateDataLists(p_callback) {

                     if (document.referrer === null || document.referrer.indexOf("entity-data-lists") < 1) {
                        this.showLoadingAjaxSpinner(true);
                     }

                     /**
                      * Success handler for Data Lists request
                      * 
                      * @method fnSuccess
                      * @param response
                      *            {Object} Ajax response object literal
                      * @param obj
                      *            {Object} Callback object from original function call
                      */
                     var fnSuccess = function DataLists_pDL_fnSuccess(response, p_obj) {
                        this.showLoadingAjaxSpinner(false);

                        var lists = response.json.datalists, list;

                        if (response.json.listTypes) {
                           this.options.listTypes = response.json.listTypes;
                        }

                        this.dataLists = {};
                        this.containerNodeRef = new Alfresco.util.NodeRef(response.json.container);
                        this.entity = response.json.entity;
                        this.widgets.newList.set("disabled", !response.json.permissions.create);

                        for ( var i = 0, ii = lists.length; i < ii; i++) {
                           list = lists[i];
                           this.dataLists[list.name] = list;
                        }
                        this.dataListsLength = lists.length;

                        if (p_callback && (typeof p_callback.fn == "function")) {
                           p_callback.fn.call(p_callback.scope || this, p_callback.obj);
                        }
                     };

                     /**
                      * Failure handler for Data Lists request
                      * 
                      * @method fnFailure
                      * @param response
                      *            {Object} Ajax response object literal
                      */
                     var fnFailure = function DataLists_pDL_fnFailure(response) {
                        this.showLoadingAjaxSpinner(false);

                        if (response.status == 401) {
                           // Our session has likely timed-out, so refresh to
                           // offer the login page
                           window.location.reload();
                        } else {
                           this.dataLists = null;
                           this.containerNodeRef = null;
                           this.widgets.newList.set("disabled", true);
                           var errorMsg = "";
                           try {
                              errorMsg = $html(YAHOO.lang.JSON.parse(response.responseText).message);
                           } catch (e) {
                              errorMsg = this.msg("message.error-unknown");
                           }
                           Alfresco.util.PopupManager.displayMessage({
                              text : errorMsg
                           });

                        }
                     };

                     Alfresco.util.Ajax.jsonGet({
                        url : $combine(Alfresco.constants.PROXY_URI, "becpg/entitylists/node",
                              this.options.entityNodeRef.replace(":/", "")),
                        successCallback : {

                           fn : fnSuccess,
                           obj : p_callback,
                           scope : this
                        },
                        failureCallback : {
                           fn : fnFailure,
                           scope : this
                        }
                     });
                  },

                  showLoadingAjaxSpinner : function(showSpinner) {
                     if (showSpinner) {
                        this.widgets.spinner.show();
                     } else {
                        this.widgets.spinner.hide();
                     }

                  },

                  /**
                   * Renders the Data Lists into the DOM
                   * 
                   * @method renderDataLists
                   * @param highlightName
                   *            {String} Optional name of list to highlight after rendering
                   */
                  renderDataLists : function DataLists_renderDataLists(p_highlightName) {
                     var me = this, listsContainer = Dom.get(this.id + "-lists"), selectedClass = "selected";

                     listsContainer.innerHTML = "";

                     /**
                      * Click handler for selecting Data List
                      * 
                      * @method fnOnClick
                      */
                     var fnOnClick = function DataLists_renderDataLists_fnOnClick() {
                        return function DataLists_renderDataLists_onClick() {
                           var lis = Selector.query("li", listsContainer);
                           Dom.removeClass(lis, selectedClass);
                           Dom.addClass(this, selectedClass);
                           return true;
                        };
                     };

                     /**
                      * Click handler for edit Data List
                      * 
                      * @method fnEditOnClick
                      * @param listName
                      *            {String} Name of the Data List
                      */
                     var fnEditOnClick = function DataLists_renderDataLists_fnEditOnClick(listName, enabled) {
                        return function DataLists_renderDataLists_onEditClick(e) {
                           if (enabled) {
                              me.onEditList(listName);
                           }
                           Event.stopEvent(e || window.event);
                        };
                     };

                     /**
                      * Click handler for edit Data List
                      * 
                      * @method fnDeleteOnClick
                      * @param listName
                      *            {String} Name of the Data List
                      */
                     var fnDeleteOnClick = function DataLists_renderDataLists_fnDeleteOnClick(listName, enabled) {
                        return function DataLists_renderDataLists_onEditClick(e) {
                           if (enabled) {
                              me.onDeleteList(listName);
                           }
                           Event.stopEvent(e || window.event);
                        };
                     };

                     try {
                        var lists = this.dataLists, list, permissions, elHighlight = null, container, el, elEdit, elDelete, elLink, elText;

                        if (this.dataListsLength === 0) {
                           listsContainer.innerHTML = '<div class="no-lists">' + this.msg("message.no-lists") + '</div>';
                        } else {
                           container = document.createElement("ul");
                           listsContainer.appendChild(container);

                           // Create the DOM structure: <li onclick><a
                           // class='filter-link' title href><span class='edit'
                           // onclick></span><span class='delete'
                           // onclick></span>"text"</a></li>
                           for ( var index in lists) {
                              if (lists.hasOwnProperty(index)) {

                                 list = lists[index];

                                 if (this.options.listId === null || this.options.listId.length < 1) {
                                    this.options.listId = list.name;
                                 }

                                 permissions = list.permissions;

                                 // Build the DOM elements
                                 el = document.createElement("li");
                                 el.onclick = fnOnClick();
                                 elEdit = document.createElement("span");
                                 if (permissions.edit) {
                                    elEdit.className = "edit";
                                    elEdit.title = this.msg("label.edit-list");
                                    elEdit.onclick = fnEditOnClick(list.name, true);
                                 } else {
                                    elEdit.className = "edit-disabled";
                                    elEdit.onclick = fnEditOnClick(list.name, false);
                                 }
                                 elDelete = document.createElement("span");
                                 if (permissions["delete"]) {
                                    elDelete.className = "delete";
                                    elDelete.title = this.msg("label.delete-list");
                                    elDelete.onclick = fnDeleteOnClick(list.name, true);
                                 } else {
                                    elDelete.className = "delete-disabled";
                                    elDelete.onclick = fnDeleteOnClick(list.name, false);
                                 }
                                 elLink = document.createElement("a");
                                 // ### beCPG : isEditaleList ?
                                 if (list.editableList) {
                                    elLink.className = "filter-link-editable-list";
                                 } else {
                                    elLink.className = "filter-link";
                                 }
                                 elLink.title = list.description;
                                 // ### beCPG : change url to entity-data-lists
                                 // and add the nodeRef reference
                                 // elLink.href = "data-lists?list=" +
                                 // $html(list.name);
                                 elLink.href = "entity-data-lists?list=" + $html(list.name) + "&nodeRef=" + $html(this.options.entityNodeRef);
                                 elText = document.createTextNode(list.title);

                                 // Build the DOM structure with the new
                                 // elements
                                 elLink.appendChild(elDelete);
                                 elLink.appendChild(elEdit);
                                 elLink.appendChild(elText);
                                 el.appendChild(elLink);
                                 container.appendChild(el);

                                 // Mark current list as selected
                                 if (list.name == this.options.listId) {
                                    Dom.addClass(el, "selected");
                                 }

                                 // Make a note of a highlight request match
                                 if (list.name == p_highlightName) {
                                    elHighlight = el;
                                 }
                              }
                           }

                           if (elHighlight) {
                              Alfresco.util.Anim.pulse(elHighlight);
                           }
                        }
                     } catch (e) {
                        listsContainer.innerHTML = '<span class="error">' + this.msg("message.error-unknown") + '</span>';
                     }
                  }

               });
})();
