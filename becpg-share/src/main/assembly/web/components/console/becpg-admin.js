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
				    var self = this;

				    // Create the panel structure
				    var panelDiv = this.createBatchPanelHTML();

				    // Initialize YUI Panel
				    this.widgets.panel = Alfresco.util.createYUIPanel(panelDiv, {
				        draggable: false,
				        width: "50em"
				    });

				    Dom.addClass(this.widgets.panel.element, "becpg-panel");

				    this.widgets.panel.show();

				    // Get references to list containers
				    var ulCurrent = panelDiv.querySelector(".batches-current");
				    var ulQueue = panelDiv.querySelector(".batches-queue");
				    var ulErrors = panelDiv.querySelector(".batches-errors");

				    ulCurrent.innerHTML = '<li class="batch-empty">' + this.msg("label.task.loading") + '</li>';
				    ulQueue.innerHTML = '<li class="batch-empty">' + this.msg("label.task.loading") + '</li>';
				    ulErrors.innerHTML = '<li class="batch-empty">' + this.msg("label.task.loading") + '</li>';

				    // Start polling for updates
				    var intervalId = setInterval(function() {
				        self.updateBatchPanel(ulCurrent, ulQueue, ulErrors, intervalId);
				    }, 500);

				    // Initial update
				    this.updateBatchPanel(ulCurrent, ulQueue, ulErrors, null);

				    // Clean up interval on panel close
				    this.widgets.panel.subscribe("hide", function() {
				        clearInterval(intervalId);
				    });
				},

				createBatchPanelHTML: function() {
				    var div = document.createElement("div");
				    div.id = this.id + "show-batches-panel";
				    div.innerHTML = '<div class="bd batch-panel">' +
				                      '<div class="batch-section">' +
				                        '<div class="batch-header">' + this.msg("label.task.current") + '</div>' +
				                        '<ul class="batches batches-current"></ul>' +
				                      '</div>' +
				                      '<div class="batch-section">' +
				                        '<div class="batch-header">' + this.msg("label.task.pending") + '</div>' +
				                        '<ul class="batches batches-queue"></ul>' +
				                      '</div>' +
				                      '<div class="batch-section">' +
				                        '<div class="batch-header">' + this.msg("label.task.errors") + '</div>' +
				                        '<ul class="batches batches-errors"></ul>' +
				                      '</div>' +
				                    '</div>';
				    return div;
				},

				updateBatchPanel: function(ulCurrent, ulQueue, ulErrors, intervalId) {
				    var self = this;
				    
				    Alfresco.util.Ajax.request({
				        url: Alfresco.constants.PROXY_URI + "/becpg/batch/queue",
				        method: Alfresco.util.Ajax.GET,
				        responseContentType: Alfresco.util.Ajax.JSON,
				        successCallback: {
				            fn: function(response) {
				                if (response.json) {
				                    self.updateCurrentBatch(ulCurrent, response.json.last);
				                    self.updateQueueBatches(ulQueue, response.json.queue);
				                    self.updateErrorBatches(ulErrors, response.json.errors);
				                }
				            }
				        },
				        scope: this
				    });
				},

				updateCurrentBatch: function(ulCurrent, lastBatch) {
				    var self = this;
				    
				    if (!lastBatch) {
				        ulCurrent.innerHTML = '<li class="batch-empty">' + this.msg("label.task.no-active") + '</li>';
				        return;
				    }

				    var batch = JSON.parse(lastBatch);
				    var batchDescId = this.formatBatchDescription(batch);
				    var percent = batch.percentCompleted;

				    if (percent === 100) {
				        ulCurrent.innerHTML = '<li class="batch-empty">' + this.msg("label.task.no-active") + '</li>';
				        return;
				    }

				    // Update existing or create new
				    if (ulCurrent.querySelector('.batch-item')) {
				        this.updateBatchItem(ulCurrent.querySelector('.batch-item'), batch, batchDescId, percent);
				    } else {
				        ulCurrent.innerHTML = '';
				        var batchItem = this.createBatchItem(batch, batchDescId, percent, true);
				        ulCurrent.appendChild(batchItem);
				    }
				},

				updateQueueBatches: function(ulQueue, queue) {
				    if (!queue || queue.length === 0) {
				        ulQueue.innerHTML = '<li class="batch-empty">' + this.msg("label.task.no-pending") + '</li>';
				        return;
				    }

				    ulQueue.innerHTML = '';

				    for (var i = 0; i < queue.length; i++) {
				        var batch = JSON.parse(queue[i]);
				        var batchDescId = this.formatBatchDescription(batch);
				        var percent = batch.percentCompleted;

				        var batchItem = this.createBatchItem(batch, batchDescId, percent, false);
				        ulQueue.appendChild(batchItem);
				    }
				},

				updateErrorBatches: function(ulErrors, errors) {
				    if (!errors) {
				        ulErrors.innerHTML = '<li class="batch-empty">' + this.msg("label.task.no-errors") + '</li>';
				        return;
				    }

				    var errorBatches = JSON.parse(errors);
				    
				    if (!errorBatches || errorBatches.length === 0) {
				        ulErrors.innerHTML = '<li class="batch-empty">' + this.msg("label.task.no-errors") + '</li>';
				        return;
				    }

				    ulErrors.innerHTML = '';

				    for (var i = 0; i < errorBatches.length; i++) {
				        var errorBatch = errorBatches[i];
				        var errorItem = this.createErrorBatchItem(errorBatch);
				        ulErrors.appendChild(errorItem);
				    }
				},

				formatBatchDescription: function(batch) {
				    var desc = batch.batchDescId;
				    
				    if (batch.stepCount) {
				        desc += " (" + batch.stepCount + "/" + batch.stepsMax + ")";
				    }
				    
				    if (batch.currentItem && batch.totalItems) {
				        desc += " - " + batch.currentItem + " / " + batch.totalItems;
				    }
				    
				    return desc;
				},

				createBatchItem: function(batch, description, percent, isCurrent) {
				    var self = this;
				    var li = document.createElement("li");
				    li.className = "batch-item";
				    li.id = "batch-" + batch.batchId;

				    var html = '<div class="batch-item-header">' +
				                 '<span class="batch-title">' + description + '</span>' +
				                 '<a href="#" class="batch-cancel-link" title="' + this.msg("label.task.cancel") + '"><span class="removeIcon"></span></a>' +
				               '</div>';

				    if (percent !== undefined && percent !== null) {
				        html += '<div class="batch-progress-container">' +
				                    '<div class="batch-progress-bar">' +
				                        '<div class="batch-progress-fill" style="width: ' + percent + '%;"></div>' +
				                    '</div>' +
				                    '<span class="batch-progress-text">' + percent + '%</span>' +
				                '</div>';
				    }

				    li.innerHTML = html;

				    var cancelLink = li.querySelector('.batch-cancel-link');
				    cancelLink.onclick = function(e) {
				        YAHOO.util.Event.preventDefault(e);
				        self.handleBatchAction(batch.batchId, isCurrent, cancelLink);
				    };

				    if (percent === 100 || batch.cancelled) {
				        cancelLink.style.display = 'none';
				    }

				    return li;
				},

				createErrorBatchItem: function(errorBatch) {
				    var self = this;
				    var li = document.createElement("li");
				    li.className = "batch-item batch-error-item";
				    li.id = "error-batch-" + errorBatch.batchId;

				    var html = '<div class="batch-item-header">' +
				                 '<span class="batch-title">' +
				                  errorBatch.batchDesc + ' (' + errorBatch.numberOfNodes + ')' + '<br/>' +
				                 '</span>' +
				                 '<a href="#" class="batch-retry-link" title="' + this.msg("label.task.retry") + '"><span class="retryIcon"></span></a>' +
				                 '<a href="#" class="batch-errors-link" title="' + this.msg("label.task.viewErrors") + '"><span class="viewIcon"></span></a>' +
				               '</div>';

				    li.innerHTML = html;

				    var retryLink = li.querySelector('.batch-retry-link');
				    retryLink.onclick = function(e) {
				        YAHOO.util.Event.preventDefault(e);
				        self.handleRetryBatch(errorBatch.batchId, retryLink);
				    };
					
				    var errorsLink = li.querySelector('.batch-errors-link');
				    errorsLink.onclick = function(e) {
				        YAHOO.util.Event.preventDefault(e);
				        self.handleViewErrorsBatch(errorBatch.batchId, errorsLink);
				    };

				    return li;
				},

				updateBatchItem: function(batchItem, batch, description, percent) {
				    var title = batchItem.querySelector('.batch-title');
				    if (title) {
				        title.innerText = description;
				    }

				    var progressFill = batchItem.querySelector('.batch-progress-fill');
				    var progressText = batchItem.querySelector('.batch-progress-text');
				    
				    if (progressFill && progressText) {
				        progressFill.style.width = percent + "%";
				        progressText.innerText = percent + "%";
				    }

				    var cancelLink = batchItem.querySelector('.batch-cancel-link');
				    if (cancelLink && (percent === 100 || batch.cancelled)) {
				        cancelLink.style.display = 'none';
				    }
				},

				handleBatchAction: function(batchId, isCurrent, button) {
				    var action = isCurrent ? 'cancel' : 'remove';
				    
				    Alfresco.util.Ajax.request({
				        url: Alfresco.constants.PROXY_URI + "/becpg/batch/" + action + "/" + batchId,
				        method: Alfresco.util.Ajax.GET,
				        responseContentType: Alfresco.util.Ajax.JSON,
				        successCallback: {
				            fn: function() {
				                var batchItem = Dom.get("batch-" + batchId);
				                if (batchItem) {
				                    batchItem.parentNode.removeChild(batchItem);
				                }
				            }
				        }
				    });
				},

				handleRetryBatch: function(batchId, button) {
				    var self = this;
				    
				    Alfresco.util.Ajax.request({
				        url: Alfresco.constants.PROXY_URI + "/becpg/batch/retry/" + encodeURIComponent(batchId),
				        method: Alfresco.util.Ajax.POST,
				        responseContentType: Alfresco.util.Ajax.JSON,
				        failureCallback: {
				            fn: function(response) {
				                Alfresco.util.PopupManager.displayMessage({
				                    text: self.msg("message.retry.failure")
				                });
				            }
				        }
				    });
				},
				
				handleViewErrorsBatch: function(batchId, button) {
				    var self = this;
					var url = Alfresco.constants.PROXY_URI + "/becpg/batch/errors/" + encodeURIComponent(batchId);
					window.open(url, "_blank");
				}
			});
})();
