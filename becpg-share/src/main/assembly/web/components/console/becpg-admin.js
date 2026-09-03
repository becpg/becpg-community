/*******************************************************************************
 *  Copyright (C) 2010-2026 beCPG. 
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
									var containerDiv = document.createElement("div");
									var panelTitle = this.msg("label.connectedUsers").replace(/:$/, "");

									var ret = '<div id="' + this.id + '-show-users-panel" class="connected-users-panel">' +
									          '<div class="bd">' +
									          '<div class="connected-users-title">' + Alfresco.util.encodeHTML(panelTitle) + '</div>' +
									          '<div class="connected-users-table-wrapper">' +
									          '<table class="users-table">';

									var usersList = (response.json && response.json.users) ? response.json.users : [];

									for (var j = 0; j < usersList.length; j++) {
										var user = usersList[j];
										var licenseGroup = user.licenseGroup || "nolicense";
										var isNoLicense = licenseGroup === "nolicense";
										var nameClass = isNoLicense ? "no-license-user" : "theme-color-1";
										var licenseLabel = this.msg("becpg.group." + licenseGroup);

										ret += '<tr>';
										ret += '<td class="user-cell">';
										ret += '<div class="user-info">';
										ret += '<span class="avatar" title="' + Alfresco.util.encodeHTML(user.fullName) + '">';
										ret += Alfresco.Share.userAvatar(user.username, 32);
										ret += '</span><span class="username"><a class="' + nameClass + '" tabindex="0" href="/share/page/user/' + encodeURIComponent(user.username) + '/profile">' + Alfresco.util.encodeHTML(user.fullName) + '</a></span>';
										ret += '</div>';
										ret += '</td>';
										ret += '<td class="user-license">';
										ret += '<span class="license-badge license-' + licenseGroup + '">' + Alfresco.util.encodeHTML(licenseLabel) + '</span>';
										ret += '</td>';
										ret += '</tr>';
									}

									ret += '</table></div></div></div>';

									containerDiv.innerHTML = ret;

									var panelDiv = Dom.getFirstChild(containerDiv);
									this.widgets.panel = Alfresco.util.createYUIPanel(panelDiv, { draggable: false, width: "55em" });

									Dom.addClass(this.widgets.panel.element, "becpg-panel");

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
				        fixedcenter: true,
				        width: "55em"
				    });

				    Dom.addClass(this.widgets.panel.element, "becpg-panel");

				    this.widgets.panel.show();
				    this.widgets.panel.center();

				    // Get references to list containers
				    var ulCurrent = panelDiv.querySelector(".batches-current");
				    var ulQueue = panelDiv.querySelector(".batches-queue");
				    var ulErrors = panelDiv.querySelector(".batches-errors");

				    ulCurrent.innerHTML = '<li class="batch-empty">' + this.msg("label.task.loading") + '</li>';
				    ulQueue.innerHTML = '<li class="batch-empty">' + this.msg("label.task.loading") + '</li>';
				    ulErrors.innerHTML = '<li class="batch-empty">' + this.msg("label.task.loading") + '</li>';

				    // Start polling for updates
				    var intervalId = setInterval(function() {
				        self.updateBatchPanel(ulCurrent, ulQueue, ulErrors, false);
				    }, 500);

				    // Initial update
				    this.updateBatchPanel(ulCurrent, ulQueue, ulErrors, true);

				    // Clean up interval on panel close
				    this.widgets.panel.subscribe("hide", function() {
				        clearInterval(intervalId);
				    });
				},

				createBatchPanelHTML: function() {
				    var containerDiv = document.createElement("div");
				    var panelTitle = this.msg("label.batchCounts").replace(/:$/, "");
				    var ret = '<div id="' + this.id + '-show-batches-panel" class="batch-management-panel">' +
				              '<div class="bd">' +
				              '<div class="batch-panel-title">' + Alfresco.util.encodeHTML(panelTitle) + '</div>' +
				              '<div class="batch-section">' +
				                '<div class="batch-header">' +
				                  '<span class="batch-header-title">' + this.msg("label.task.current") + '</span>' +
				                '</div>' +
				                '<div class="batch-list-wrapper">' +
				                  '<ul class="batches batches-current"></ul>' +
				                '</div>' +
				              '</div>' +
				              '<div class="batch-section">' +
				                '<div class="batch-header">' +
				                  '<span class="batch-header-title">' + this.msg("label.task.pending") + '</span>' +
				                '</div>' +
				                '<div class="batch-list-wrapper">' +
				                  '<ul class="batches batches-queue"></ul>' +
				                '</div>' +
				              '</div>' +
				              '<div class="batch-section">' +
				                '<div class="batch-header">' +
				                  '<span class="batch-header-title">' + this.msg("label.task.errors") + '</span>' +
				                '</div>' +
				                '<div class="batch-list-wrapper">' +
				                  '<ul class="batches batches-errors"></ul>' +
				                '</div>' +
				              '</div>' +
				              '</div></div>';
				    containerDiv.innerHTML = ret;
				    return Dom.getFirstChild(containerDiv);
				},

				updateBatchPanel: function(ulCurrent, ulQueue, ulErrors, isInitial) {
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
				                    if (isInitial && self.widgets.panel) {
				                        self.widgets.panel.center();
				                    }
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
				    li.className = "batch-item" + (isCurrent ? " batch-item-current" : "");
				    li.id = "batch-" + batch.batchId;

				    var html = '<div class="batch-item-header">' +
				                 '<div class="batch-item-info">' +
				                   '<span class="batch-title">' + Alfresco.util.encodeHTML(description) + '</span>' +
				                 '</div>' +
				                 '<div class="batch-item-actions">' +
				                   '<a href="#" class="batch-cancel-link" title="' + this.msg("label.task.cancel") + '"><span class="removeIcon"></span></a>' +
				                 '</div>' +
				               '</div>';

				    if (percent !== undefined && percent !== null && isCurrent) {
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

				    var title = errorBatch.batchDesc || errorBatch.batchId;

				    var html = '<div class="batch-item-header">' +
				                 '<div class="batch-item-info">' +
				                   '<span class="batch-title">' + Alfresco.util.encodeHTML(title) + '</span>' +
				                   '<span class="batch-error-count-badge" title="' + this.msg("label.task.batchErrors.total", errorBatch.numberOfNodes) + '">' + errorBatch.numberOfNodes + '</span>' +
				                 '</div>' +
				               '</div>';

				    li.innerHTML = html;

				    li.onclick = function(e) {
				        YAHOO.util.Event.preventDefault(e);
				        self.handleViewErrorsBatch(errorBatch.batchId, title, li);
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
				
				handleRetryBatchEntry: function(batchId, nodeRef, buttonElement, onComplete) {
				    var self = this;

				    Alfresco.util.Ajax.request({
				        url: Alfresco.constants.PROXY_URI + "/becpg/batch/retry/" + encodeURIComponent(batchId) + "?nodeRef=" + encodeURIComponent(nodeRef),
				        method: Alfresco.util.Ajax.POST,
				        responseContentType: Alfresco.util.Ajax.JSON,
				        successCallback: {
				            fn: function() {
				                if (typeof onComplete === "function") {
				                    onComplete();
				                }
				            }
				        },
				        failureCallback: {
				            fn: function() {
				                if (buttonElement) {
				                    buttonElement.disabled = false;
				                }
				                Alfresco.util.PopupManager.displayMessage({
				                    text: self.msg("message.retry.failure")
				                });
				            }
				        }
				    });
				},

				renderBatchErrorRows: function(entities) {
				    var ret = '';
				    for (var i = 0; i < entities.length; i++) {
				        var item = entities[i];
				        var entityUrl = (window.beCPG && beCPG.util && beCPG.util.entityURL) ?
				            beCPG.util.entityURL(item.siteId, item.nodeRef, item.type) :
				            Alfresco.constants.URL_PAGECONTEXT + "entity-data-lists?nodeRef=" + encodeURIComponent(item.nodeRef);

				        var iconName = Alfresco.util.getFileIcon(item.name || "", item.type, 16);
				        var iconSrc = Alfresco.constants.URL_RESCONTEXT + "components/images/filetypes/" + iconName;

				        ret += '<tr data-noderef="' + Alfresco.util.encodeHTML(item.nodeRef) + '">';
				        ret += '<td class="col-entity">';
				        ret += '<div class="entity-cell-info">';
				        ret += '<img class="entity-icon" src="' + iconSrc + '" width="16" height="16" alt="" />';
				        ret += '<span class="entity-name"><a class="theme-color-1" href="' + Alfresco.util.encodeHTML(entityUrl) + '" target="_blank">' + Alfresco.util.encodeHTML(item.name || item.nodeRef) + '</a></span>';
				        if (item.code) {
				            ret += '<span class="entity-code">(' + Alfresco.util.encodeHTML(item.code) + ')</span>';
				        }
				        ret += '</div>';
				        ret += '</td>';

				        ret += '<td class="col-error">';
				        if (item.error) {
				            ret += '<div class="batch-error-message" title="' + Alfresco.util.encodeHTML(item.error) + '">' + Alfresco.util.encodeHTML(item.error) + '</div>';
				        } else {
				            ret += '<span class="batch-error-none">-</span>';
				        }
				        ret += '</td>';

				        ret += '<td class="col-action">';
				        ret += '<button type="button" class="batch-entity-retry-btn" title="' + this.msg("label.task.batchErrors.retrySingle") + '"><span class="retryIcon"></span></button>';
				        ret += '</td>';
				        ret += '</tr>';
				    }
				    return ret;
				},

				loadBatchErrorsPage: function(batchId, pageIndex, pageSize, panelDiv) {
				    var self = this;
				    var offset = pageIndex * pageSize;
				    var tableWrapper = panelDiv.querySelector('.batch-errors-table-wrapper');
				    var countElement = panelDiv.querySelector('.batch-errors-count');
				    var paginationContainer = panelDiv.querySelector('.batch-errors-pagination');
				    var pageInfoElement = panelDiv.querySelector('.batch-errors-page-info');
				    var prevBtn = panelDiv.querySelector('.batch-errors-prev-btn');
				    var nextBtn = panelDiv.querySelector('.batch-errors-next-btn');

				    Alfresco.util.Ajax.request({
				        url: Alfresco.constants.PROXY_URI + "/becpg/batch/errors/" + encodeURIComponent(batchId) + "?offset=" + offset + "&limit=" + pageSize,
				        method: Alfresco.util.Ajax.GET,
				        responseContentType: Alfresco.util.Ajax.JSON,
				        successCallback: {
				            fn: function(response) {
				                if (response.json) {
				                    var data = response.json;
				                    var entities = data.entities || [];
				                    var totalCount = data.total !== undefined ? data.total : entities.length;

				                    if (countElement) {
				                        countElement.innerText = self.msg("label.task.batchErrors.total", totalCount);
				                    }

				                    if (totalCount === 0 || entities.length === 0) {
				                        if (pageIndex > 0 && totalCount > 0) {
				                            self.loadBatchErrorsPage(batchId, pageIndex - 1, pageSize, panelDiv);
				                            return;
				                        }
				                        tableWrapper.innerHTML = '<div class="batch-empty">' + self.msg("label.task.batchErrors.no-errors") + '</div>';
				                        if (paginationContainer) {
				                            paginationContainer.style.display = 'none';
				                        }
				                        return;
				                    }

				                    var tableHtml = '<table class="batch-errors-table">' +
				                                   '<thead>' +
				                                   '<tr>' +
				                                   '<th class="col-entity">' + self.msg("label.task.batchErrors.entity") + '</th>' +
				                                   '<th class="col-error">' + self.msg("label.task.batchErrors.error") + '</th>' +
				                                   '<th class="col-action">' + self.msg("label.task.batchErrors.action") + '</th>' +
				                                   '</tr>' +
				                                   '</thead>' +
				                                   '<tbody>' +
				                                   self.renderBatchErrorRows(entities) +
				                                   '</tbody></table>';
				                    tableWrapper.innerHTML = tableHtml;

				                    var totalPages = Math.ceil(totalCount / pageSize);
				                    if (totalPages <= 1) {
				                        if (paginationContainer) {
				                            paginationContainer.style.display = 'none';
				                        }
				                    } else {
				                        if (paginationContainer) {
				                            paginationContainer.style.display = 'inline-flex';
				                        }
				                        if (pageInfoElement) {
				                            pageInfoElement.innerText = self.msg("label.task.batchErrors.page", (pageIndex + 1), totalPages);
				                        }
				                        if (prevBtn) {
				                            prevBtn.disabled = pageIndex <= 0;
				                            prevBtn.onclick = function(e) {
				                                YAHOO.util.Event.preventDefault(e);
				                                self.loadBatchErrorsPage(batchId, pageIndex - 1, pageSize, panelDiv);
				                            };
				                        }
				                        if (nextBtn) {
				                            nextBtn.disabled = pageIndex >= totalPages - 1;
				                            nextBtn.onclick = function(e) {
				                                YAHOO.util.Event.preventDefault(e);
				                                self.loadBatchErrorsPage(batchId, pageIndex + 1, pageSize, panelDiv);
				                            };
				                        }
				                    }

				                    if (pageIndex === 0 && self.widgets.errorsPanel) {
				                        self.widgets.errorsPanel.center();
				                    }

				                    var retryRowBtns = tableWrapper.querySelectorAll('.batch-entity-retry-btn');
				                    for (var r = 0; r < retryRowBtns.length; r++) {
				                        (function(btn) {
				                            btn.onclick = function(e) {
				                                YAHOO.util.Event.preventDefault(e);
				                                var tr = Dom.getAncestorByTagName(btn, "tr");
				                                var nodeRef = tr.getAttribute('data-noderef');
				                                btn.disabled = true;
				                                self.handleRetryBatchEntry(batchId, nodeRef, btn, function() {
				                                    self.loadBatchErrorsPage(batchId, pageIndex, pageSize, panelDiv);
				                                });
				                            };
				                        })(retryRowBtns[r]);
				                    }
				                }
				            }
				        },
				        failureCallback: {
				            fn: function() {
				                tableWrapper.innerHTML = '<div class="batch-empty">' + self.msg("label.task.batchErrors.no-errors") + '</div>';
				            }
				        },
				        scope: this
				    });
				},

				handleViewErrorsBatch: function(batchId, batchDesc, button) {
				    var self = this;
				    var pageSize = 20;
				    var titleDesc = batchDesc || batchId;
				    var panelTitle = self.msg("label.task.batchErrors.title", titleDesc);

				    var containerDiv = document.createElement("div");
				    var ret = '<div id="' + self.id + '-batch-errors-panel" class="batch-errors-panel">' +
				              '<div class="bd">' +
				              '<div class="batch-errors-title">' + Alfresco.util.encodeHTML(panelTitle) + '</div>' +
				              '<div class="batch-errors-table-wrapper">' +
				                '<div class="batch-empty">' + self.msg("label.task.loading") + '</div>' +
				              '</div>' +
				              '<div class="batch-errors-footer">' +
				                '<div class="batch-errors-count"></div>' +
				                '<div class="batch-errors-pagination" style="display: none;">' +
				                  '<button type="button" class="batch-errors-page-btn batch-errors-prev-btn">' + self.msg("label.task.batchErrors.prev") + '</button>' +
				                  '<span class="batch-errors-page-info"></span>' +
				                  '<button type="button" class="batch-errors-page-btn batch-errors-next-btn">' + self.msg("label.task.batchErrors.next") + '</button>' +
				                '</div>' +
				                '<div class="batch-errors-actions">' +
				                  '<button type="button" class="batch-errors-retry-all-btn">' + self.msg("label.task.batchErrors.retryAll") + '</button>' +
				                '</div>' +
				              '</div>' +
				              '</div></div>';

				    containerDiv.innerHTML = ret;
				    var panelDiv = Dom.getFirstChild(containerDiv);

				    if (self.widgets.errorsPanel) {
				        self.widgets.errorsPanel.destroy();
				    }

				    self.widgets.errorsPanel = Alfresco.util.createYUIPanel(panelDiv, {
				        draggable: false,
				        fixedcenter: true,
				        width: "60em"
				    });

				    Dom.addClass(self.widgets.errorsPanel.element, "becpg-panel");
				    self.widgets.errorsPanel.show();
				    self.widgets.errorsPanel.center();

				    var retryAllBtn = panelDiv.querySelector('.batch-errors-retry-all-btn');
				    if (retryAllBtn) {
				        retryAllBtn.onclick = function(e) {
				            YAHOO.util.Event.preventDefault(e);
				            self.handleRetryBatch(batchId, retryAllBtn);
				            self.widgets.errorsPanel.hide();
				        };
				    }

				    self.loadBatchErrorsPage(batchId, 0, pageSize, panelDiv);
				}
			});
})();
