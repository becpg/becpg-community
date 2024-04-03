/*******************************************************************************
 *  Copyright (C) 2010-2021 beCPG. 
 *   
 *  This file is part of beCPG 
 *   
 *  beCPG is free software: you can redistribute it and/or modify 
 *  it under the terms of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation, either version 3 of the License, or 
 *  (at your option) any later version. 
 *   
 *  beCPG is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 *  GNU Lesser General Public License for more details. 
 *   
 *  You should have received a copy of the GNU Lesser General Public License along with beCPG.
 *   If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
/**
 * AdminConsole tool component.
 * 
 * @namespace Extras
 * @class beCPG.component.AdminConsole
 */
(function() {

	var Dom = YAHOO.util.Dom;

	/**
	 * beCPGAdminConsole constructor.
	 * 
	 * @param {String}
	 *            htmlId The HTML id of the parent element
	 * @return {Extras.ConsoleCreateUsers} The new ConsoleCreateUsers instance
	 * @constructor
	 */
	beCPG.component.AdminConsole = function(htmlId) {

		beCPG.component.AdminConsole.superclass.constructor.call(this, "beCPG.component.AdminConsole", htmlId, [
			"button", "menu", "container", "json"]);

		return this;
	};

	YAHOO
		.extend(beCPG.component.AdminConsole, Alfresco.component.Base,
			{
				/**
				 * Object container for initialization options
				 * 
				 * @property options
				 * @type object
				 */
				options: {
					memory: 0
				},

				/**
				 * Fired by YUI when parent element is available for scripting. Component initialisation, including
				 * instantiation of YUI widgets and event listener binding.
				 * 
				 * @method onReady
				 */
				onReady: function AdminConsole_onReady() {


					this.widgets.initRepoButton = Alfresco.util.createYUIButton(this, "init-repo-button",
						this.onInitRepoClick);
					this.widgets.initAclButton = Alfresco.util.createYUIButton(this, "init-acl-button",
						this.onInitAclClick);
					this.widgets.emptyCacheButton = Alfresco.util.createYUIButton(this, "empty-cache-button",
						this.onEmptyCacheClick);

					this.widgets.showUsersButton = Alfresco.util.createYUIButton(this, "show-users-button",
						this.onShowUsersClick);

					this.widgets.showBatchesButton = Alfresco.util.createYUIButton(this, "show-batches-button",
						this.onShowBatchesClick);

					this.createGauge();
				},


				createGauge: function() {
					var r = 50;
					var circles = Dom.getElementsByClassName('circle');
					var total_circles = circles.length;
					for (var i = 0; i < total_circles; i++) {
						circles[i].setAttribute('r', r);
					}
					var meter_dimension = (r * 2) + 50;
					var wrapper = Dom.get(this.id + '-gauge-wrapper');
					wrapper.style.width = meter_dimension + 'px';
					var cf = 2 * Math.PI * r;
					var semi_cf = cf / 2;
					var semi_cf_1by3 = semi_cf / 5;
					var semi_cf_2by3 = semi_cf_1by3 * 2;

					Dom.get(this.id + '-outline_curves')
						.setAttribute('stroke-dasharray', semi_cf + ',' + cf);
					Dom.get(this.id + '-low')
						.setAttribute('stroke-dasharray', semi_cf + ',' + cf);
					Dom.get(this.id + '-avg')
						.setAttribute('stroke-dasharray', semi_cf_2by3 + ',' + cf);
					Dom.get(this.id + '-high')
						.setAttribute('stroke-dasharray', semi_cf_1by3 + ',' + cf);

					var precLbl = Dom.get('gauge-percentage');
					var meter_needle = Dom.get(this.id + '-gauge-meter_needle');
					var meter_value = semi_cf - ((this.options.memory * semi_cf) / 100);
					meter_needle.style.transform = 'rotate(' + (270 + ((this.options.memory * 180) / 100)) + 'deg)';
					precLbl.textContent = this.options.memory + "%";
				},

				/**
				 * Initialize repository click event handler
				 * 
				 * @method onInitRepoClick
				 * @param e
				 *            {object} DomEvent
				 * @param args
				 *            {array} Event parameters (depends on event type)
				 */
				onInitRepoClick: function AdminConsole_onInitRepoClick(e, args) {
					// Disable the button temporarily
					this.widgets.initRepoButton.set("disabled", true);

					Alfresco.util.Ajax.request({
						url: Alfresco.constants.URL_SERVICECONTEXT + "modules/init-repo",
						method: Alfresco.util.Ajax.GET,
						responseContentType: Alfresco.util.Ajax.JSON,
						successCallback: {
							fn: this.onInitRepoSuccess,
							scope: this
						},
						failureCallback: {
							fn: this.onInitRepoFailure,
							scope: this
						}
					});
				},

				/**
				 * Init repo success handler
				 * 
				 * @method onInitRepoSuccess
				 * @param response
				 *            {object} Server response
				 */
				onInitRepoSuccess: function AdminConsole_onInitRepoSuccess(response) {
					Alfresco.util.PopupManager.displayMessage({
						text: this.msg("message.init-repo.success")
					});
					this.widgets.initRepoButton.set("disabled", false);
				},

				/**
				 * Init repo failure handler
				 * 
				 * @method onInitRepoFailure
				 * @param response
				 *            {object} Server response
				 */
				onInitRepoFailure: function AdminConsole_onInitRepoFailure(response) {
					if (response.json.message !== null) {
						Alfresco.util.PopupManager.displayPrompt({
							text: response.json.message
						});
					} else {
						Alfresco.util.PopupManager.displayMessage({
							text: this.msg("message.init-repo.failure")
						});
					}
					this.widgets.initRepoButton.set("disabled", false);
				},
				/**
				 * Initialize repository click event handler
				 * 
				 * @method onInitRepoClick
				 * @param e
				 *            {object} DomEvent
				 * @param args
				 *            {array} Event parameters (depends on event type)
				 */
				onInitAclClick: function AdminConsole_onInitAclClick(e, args) {
					// Disable the button temporarily
					this.widgets.initAclButton.set("disabled", true);

					Alfresco.util.Ajax.request({
						url: Alfresco.constants.PROXY_URI + "/becpg/admin/repository/reload-acl",
						method: Alfresco.util.Ajax.GET,
						responseContentType: Alfresco.util.Ajax.JSON,
						successCallback: {
							fn: this.onInitAclSuccess,
							scope: this
						},
						failureCallback: {
							fn: this.onInitAclFailure,
							scope: this
						}
					});
				},

				/**
				 * Init repo success handler
				 * 
				 * @method onInitRepoSuccess
				 * @param response
				 *            {object} Server response
				 */
				onInitAclSuccess: function AdminConsole_onInitAclSuccess(response) {
					Alfresco.util.PopupManager.displayMessage({
						text: this.msg("message.init-acl.success")
					});
					this.widgets.initAclButton.set("disabled", false);
				},

				/**
				 * Init repo failure handler
				 * 
				 * @method onInitRepoFailure
				 * @param response
				 *            {object} Server response
				 */
				onInitAclFailure: function AdminConsole_onInitAclFailure(response) {
					if (response.json.message !== null) {
						Alfresco.util.PopupManager.displayPrompt({
							text: response.json.message
						});
					} else {
						Alfresco.util.PopupManager.displayMessage({
							text: this.msg("message.init-acl.failure")
						});
					}
					this.widgets.initAclButton.set("disabled", false);
				},
				/**
				 * Empty cache click event handler
				 * 
				 * @method onEmptyCacheClick
				 * @param e
				 *            {object} DomEvent
				 * @param args
				 *            {array} Event parameters (depends on event type)
				 */
				onEmptyCacheClick: function AdminConsole_onEmptyCacheClick(e, args) {
					// Disable the button temporarily
					this.widgets.emptyCacheButton.set("disabled", true);

					Alfresco.util.Ajax.request({
						url: Alfresco.constants.PROXY_URI + "/becpg/admin/repository/reload-cache",
						method: Alfresco.util.Ajax.GET,
						responseContentType: Alfresco.util.Ajax.JSON,
						successCallback: {
							fn: this.emptyCacheSuccess,
							scope: this
						},
						failureCallback: {
							fn: this.emptyCacheFailure,
							scope: this
						}
					});
				},

				/**
				 * emptyCache success handler
				 * 
				 * @method emptyCacheRepoSuccess
				 * @param response
				 *            {object} Server response
				 */
				emptyCacheSuccess: function AdminConsole_emptyCacheRepoSuccess(response) {
					Alfresco.util.PopupManager.displayMessage({
						text: this.msg("message.empty-cache.success")
					});
					this.widgets.emptyCacheButton.set("disabled", false);
				},

				/**
				 * emptyCache failure handler
				 * 
				 * @method emptyCacheFailure
				 * @param response
				 *            {object} Server response
				 */
				emptyCacheFailure: function AdminConsole_emptyCacheFailure(response) {
					if (response.json.message !== null) {
						Alfresco.util.PopupManager.displayPrompt({
							text: response.json.message
						});
					} else {
						Alfresco.util.PopupManager.displayMessage({
							text: this.msg("message.empty-cache.failure")
						});
					}
					this.widgets.emptyCacheButton.set("disabled", false);
				},

				onShowUsersClick: function AdminConsole_onShowUsersClick(e, args) {

					Alfresco.util.Ajax.request({
						url: Alfresco.constants.PROXY_URI + "/becpg/admin/repository/show-users",
						method: Alfresco.util.Ajax.GET,
						responseContentType: Alfresco.util.Ajax.JSON,
						successCallback: {
							fn: function(response) {
								if (response.json) {
									// Inject the template from the XHR request into a new DIV element
									var containerDiv = document.createElement("div");

									var ret = '<div id="' + this.id + '-show-users-panel" class="about-share"><div class="bd"><ul class="users">';

									for (j in response.json.users) {
										var user = response.json.users[j];
										ret += "<li >";
										ret += '<span class="avatar" title="' + user.fullName + '">';
										ret += Alfresco.Share.userAvatar(user.username, 64);
										ret += '</span><span class="username" ><a id="yui-gen59" class="theme-color-1" tabindex="0" href="/share/page/user/' + user.username + '/profile">' + user.fullName + '</a></span></li>';
									}

									ret += "</ul></div></div>";

									containerDiv.innerHTML = ret;

									var panelDiv = Dom.getFirstChild(containerDiv);
									this.widgets.panel = Alfresco.util.createYUIPanel(panelDiv, { draggable: false, width: "25em" });

									this.widgets.panel.show();

								}
							},
							scope: this
						}
					});
				},

				onShowBatchesClick: function AdminConsole_onShowBatchesClick(e, args) {

					// Inject the template from the XHR request into a new DIV element
					var containerDiv = document.createElement("div");

					var div = document.createElement("div");
					div.id = this.id + "show-batches-panel";
					div.classList.add("about-share");
					var bd = document.createElement("div");
					bd.classList.add("bd");
					bd.style = "padding: 10px";
					div.appendChild(bd);
					var ulCurrent = document.createElement("ul");
					ulCurrent.style = "padding: 10px";
					bd.appendChild(ulCurrent);
					var ulCur = document.createElement("ul");
					ulCur.classList.add("batches");
					ulCur.style = "height: 5em;"
					bd.appendChild(ulCur);

					var headerCurrent = document.createElement("li");
					ulCurrent.appendChild(headerCurrent);
					var labelCurrrent = document.createElement("label");
					headerCurrent.appendChild(labelCurrrent);
					labelCurrrent.innerText = this.msg("label.task.current");
					labelCurrrent.style = "font-weight: bold";

					var ulQueue = document.createElement("ul");
					ulQueue.style = "padding: 10px";
					bd.appendChild(ulQueue);
					var ulQu = document.createElement("ul");
					ulQu.classList.add("batches");
					bd.appendChild(ulQu);

					var headerQueue = document.createElement("li");
					ulQueue.appendChild(headerQueue);
					var labelQueue = document.createElement("label");
					headerQueue.appendChild(labelQueue);
					labelQueue.innerText = this.msg("label.task.pending");
					labelQueue.style = "font-weight: bold";

					containerDiv.appendChild(div);

					var panelDiv = Dom.getFirstChild(containerDiv);
					this.widgets.panel = Alfresco.util.createYUIPanel(panelDiv, { draggable: false, width: "auto" });

					this.widgets.panel.show();

					updateBatchPanel(ulCur, ulQu);
					
					var intervalId = setInterval(function() { updateBatchPanel(ulCur, ulQu, intervalId); } , 500);
					
					div.querySelector(".container-close").addEventListener("click", function() {
						clearInterval(intervalId);
					});
				}
			});
			
	function updateBatchPanel(ulCur, ulQu, intervalId) {
		Alfresco.util.Ajax.request({
			url: Alfresco.constants.PROXY_URI + "/becpg/batch/queue",
			method: Alfresco.util.Ajax.GET,
			responseContentType: Alfresco.util.Ajax.JSON,
			successCallback: {
				fn: function(response) {
					if (response.json) {
						var last = response.json.last;

						if (last) {
							last = JSON.parse(last);
							var batchId = last.batchId;
							var batchDescId = last.batchDescId + (last.stepCount ? (" (" + last.stepCount + "/" + last.stepsMax + ")") : "");
							if (last.currentItem && last.totalItems) {
								batchDescId += " - " + last.currentItem + " / " + last.totalItems;
							}
							var percent = last.percentCompleted;

							if (percent == 100) {
								ulCur.innerHTML = "";
							} else if (ulCur.firstChild) {
								ulCur.children[0].children[0].innerText = batchDescId;
								ulCur.children[1].children[0].firstChild.value = percent;
								ulCur.children[1].children[0].firstChild.title = percent + " %";
								ulCur.children[1].children[1].firstChild.id = batchId;
								ulCur.children[1].children[1].firstChild.style = "cursor:pointer";
							} else {
								var textLine = document.createElement("li");
								textLine.id = batchId;
								ulCur.appendChild(textLine);
								var spanText = document.createElement("span");
								spanText.innerText = batchDescId;
								textLine.appendChild(spanText);
								var meterLine = document.createElement("li");
								ulCur.appendChild(meterLine);
								var spanMeter = document.createElement("span");
								meterLine.appendChild(spanMeter);
								var meter = document.createElement("progress");
								spanMeter.appendChild(meter);
								meter.style = "width:100px";
								meter.value = percent;
								meter.max = "100";
								meter.title = percent + " %";
								var spanButton = document.createElement("span");
								meterLine.appendChild(spanButton);
								var button = document.createElement("a");
								spanButton.appendChild(button);
								button.classList.add("removeIcon");
								button.style = "cursor:pointer";
								if (percent == 100 || last.cancelled) {
									button.style = "display:none";
								}
								button.id = batchId;
								button.onclick = function(event) {
									Alfresco.util.Ajax.request({
										url: Alfresco.constants.PROXY_URI + "/becpg/batch/cancel/" + event.target.id,
										method: Alfresco.util.Ajax.GET,
										responseContentType: Alfresco.util.Ajax.JSON
									});
									event.target.style = "display:none";
								};
							}
						} else {
							ulCur.innerHTML = "";
						}
					}

					if (response.json.queue) {

						for (var j = 0; j < response.json.queue.length; j++) {
							
							var curQueue = JSON.parse(response.json.queue[j]);
							var batchId = curQueue.batchId;
							var batchDescId = curQueue.batchDescId + (curQueue.stepCount ? (" (" + curQueue.stepCount + "/" + curQueue.stepsMax + ")") : "");
							
							if (curQueue.currentItem && curQueue.totalItems) {
								batchDescId += " - " + curQueue.currentItem + " / " + curQueue.totalItems;
							}
							
							var percent = curQueue.percentCompleted;
							var queueLine = ulQu.children[j];
							
							if (queueLine) {
								var textLine = queueLine.children[0];
								textLine.id = batchId;
								textLine.firstChild.innerText = batchDescId;
								textLine.children[1].firstChild.id = batchId;
								textLine.children[1].firstChild.style = "cursor:pointer; float:right; padding-right:20px; padding-top:15px";
								var meterLine = queueLine.children[1];
								if (percent) {
									if (!meterLine) {
										var meterLine = document.createElement("li");
										queueLine.appendChild(meterLine);
										var spanMeter = document.createElement("span");
										meterLine.appendChild(spanMeter);
										var meter = document.createElement("progress");
										spanMeter.appendChild(meter);
										meter.style = "width:100px";
										meter.value = percent;
										meter.max = "100";
										meter.title = percent + " %";
									}
									meterLine.firstChild.value = percent;
									meterLine.firstChild.title = percent + " %";
								} else if (meterLine) {
									meterLine.remove();
								}
							} else {
								var queueLine = document.createElement("li");
								ulQu.appendChild(queueLine);
								var textLine = document.createElement("li");
								textLine.style = "width:100%";
								textLine.id = batchId;
								queueLine.appendChild(textLine);
								
								if (percent) {
									var meterLine = document.createElement("li");
									queueLine.appendChild(meterLine);
									var spanMeter = document.createElement("span");
									meterLine.appendChild(spanMeter);
									var meter = document.createElement("progress");
									spanMeter.appendChild(meter);
									meter.style = "width:100px";
									meter.value = percent;
									meter.max = "100";
									meter.title = percent + " %";
								}
								
								var spanText = document.createElement("span");
								textLine.appendChild(spanText);
								spanText.innerText = batchDescId;
								var spanButton = document.createElement("span");
								textLine.appendChild(spanButton);
								var button = document.createElement("a");
								spanButton.appendChild(button);
								button.classList.add("removeIcon");
								button.style = "cursor:pointer; float:right; padding-right:20px; padding-top:15px";
								button.id = batchId;
								button.onclick = function(event) {
									Alfresco.util.Ajax.request({
										url: Alfresco.constants.PROXY_URI + "/becpg/batch/remove/" + event.target.id,
										method: Alfresco.util.Ajax.GET,
										responseContentType: Alfresco.util.Ajax.JSON
									});
									event.target.style = "display:none";
								};
							}
						}
						
						for (var k = response.json.queue.length; k < ulQu.children.length; k++) {
							ulQu.children[k].remove();
						}
					}

				}
			},
			scope: this
		});
	}

})();
