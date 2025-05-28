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
 * 
 * @namespace beCPG.util
 */
(function() {

	beCPG.util.launchAsyncDownload = function(fileName, tplName, url, postParams) {

	
		fileName = fileName.replace(/[^a-zA-ZÀ-ÿ0-9\s.,:\-]/g, ""); // remove special characters
		    
		if (fileName.indexOf(".zip") > 0 || tplName.indexOf(".xlsx") > 0 ||  tplName.indexOf(".xlsm") > 0 || tplName.indexOf(".rptdesign") > 0) {

			url += "&async=true";

			var downloadDialog = Alfresco.getArchiveAndDownloadInstance();

			if (fileName.indexOf(".xlsx") > 0 || fileName.indexOf(".xlsm") > 0) {
				downloadDialog.mimeType = "-excel";
			} else if (fileName.indexOf(".pdf") > 0) {
				downloadDialog.mimeType = "-pdf";
			} else if (fileName.indexOf(".doc") > 0 || fileName.indexOf(".docx") > 0 || fileName.indexOf(".odt") > 0) {
				downloadDialog.mimeType = "-doc";
			} else if (fileName.indexOf(".ppt") > 0 || fileName.indexOf(".pptx") > 0) {
				downloadDialog.mimeType = "-ppt";
			} else {
				downloadDialog.mimeType = "";
			}



			downloadDialog._resetGUI = function() {
				// Reset references and the gui before
				// showing it
				this.widgets.cancelOkButton.set("disabled", false);
				this._currentArchiveNodeURL = "";
				Dom.setStyle(this.id + "-aggregate-progress-span", "left", "-300px");
				Dom.get(this.id + "-file-count-span").innerHTML = "";
				Dom.get(this.id + "-aggregate-status-span").innerHTML = this.msg("status.label" + this.mimeType);
			};

			downloadDialog.updateProgress = function(json) {
				// Remove any commas from the number to
				// prevent NaN errors
				var done = json.done.replace(/,/g, "");
				var total = json.total.replace(/,/g, "");
				var overallProgress = total != 0 ? (done / total) : 0;
				var overallLeft = (-300 + (overallProgress * 300));
				Dom.setStyle(this.id + "-aggregate-progress-span", "left", overallLeft + "px");
				Dom.get(this.id + "-file-count-span").innerHTML = this.msg("file.status" + this.mimeType, json.filesAdded,
					json.totalFiles);
			};

			downloadDialog.handleArchiveComplete = function() {
				// Hide the panel and initiate the download...
				this.widgets.cancelOkButton.set("disabled", false);
				this.panel.hide();

				// Create an empty form and post it to a hidden ifram using GET to avoid confusing the browser to believe we
				// are leaving the current page (which would abort the currently running requests, i.e. deletion of the archive

				var form = document.createElement("form");
				form.method = "GET";
                form.style.display = "none";
                form.action = Alfresco.constants.PROXY_URI + "becpg/report/node/" + this._currentArchiveNodeURL + "/content/" + Alfresco.util.encodeURIPath(this._currentArchiveName);
                document.body.appendChild(form);


				var d = form.ownerDocument;
				var input1 = d.createElement("input");
				input1.name = "isSearch";
				input1.value = "true";
				form.appendChild(input1);

				var input2 = d.createElement("input");
				input2.name = "a";
				input2.value = "true";
				form.appendChild(input2);


				var iframe = d.createElement("iframe");
				iframe.style.display = "none";
				YAHOO.util.Dom.generateId(iframe, "downloadArchive");
				iframe.name = iframe.id;
				document.body.appendChild(iframe);

				// makes it possible to target the frame properly in IE.
				window.frames[iframe.name].name = iframe.name;

				form.target = iframe.name;
				form.submit();
			};


			downloadDialog.showExport = function(reportName) {

				// Reset the dialog...
				this._resetGUI();

				// Enable the Esc key listener
				this.widgets.escapeListener.enable();
				this.panel.setFirstLastFocusable();
				this.panel.show();

				this._currentArchiveName = reportName;

				// Kick off the request...

				// Post the details of the nodeRefs to
				// archive...
				
				if(postParams){
					Alfresco.util.Ajax.jsonPost({
						url: url,
						dataObj : postParams,
						successCallback: {
							fn: this.archiveInitReqSuccess,
							scope: this
						},
						failureCallback: {
							fn: this.archiveInitReqFailure,
							scope: this
						}
					});
				} else {
					Alfresco.util.Ajax.request({
					method: Alfresco.util.Ajax.GET,
					url: url,
					responseContentType: "application/json",
					successCallback: {
						fn: this.archiveInitReqSuccess,
						scope: this
					},
					failureCallback: {
						fn: this.archiveInitReqFailure,
						scope: this
					}
				});
				}
				
				

			}

			downloadDialog.showExport(fileName);

		} else {
			document.location.href = url;
		}

	};

})();
