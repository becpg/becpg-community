
/**
 * beCPG AutoComplete component.
 * 
 * Manage autocomplete with association, 
 * TODO : Is there a better way to override the YAHOO.widget.AutoComplete ?
 */
(function() 
{
   
/**
 * Populates the given &lt;li&gt; element with return value from formatResult().
 *
 * @method _populateListItem
 * @param elListItem {HTMLElement} The LI element.
 * @param oResult {Object} The result object.
 * @param sCurQuery {String} The query string.
 * @private
 */
YAHOO.widget.AutoComplete.prototype._populateListItem = function(elListItem, oResult, sQuery) {

  /*
	*   beCPG - PQU : manage association
	*/
    //elListItem.innerHTML = this.formatResult(oResult, sQuery, elListItem._sResultMatch);
	elListItem.innerHTML = this.formatResult(oResult, sQuery, oResult[1]);
};

/**
 * Updates the text input box value with selected query result. If a delimiter
 * has been defined, then the value gets appended with the delimiter.
 *
 * @method _updateValue
 * @param elListItem {HTMLElement} The &lt;li&gt; element item with which to update the value.
 * @private
 */
YAHOO.widget.AutoComplete.prototype._updateValue = function(elListItem) {
    if(!this.suppressInputUpdate) {    
        var elTextbox = this._elTextbox;
        var sDelimChar = (this.delimChar) ? (this.delimChar[0] || this.delimChar) : null;
        var sResultMatch = elListItem._sResultMatch;
    
        // Calculate the new value
        var sNewValue = "";
        if(sDelimChar) {
            // Preserve selections from past queries
            sNewValue = this._sPastSelections;
            // Add new selection plus delimiter
            sNewValue += sResultMatch + sDelimChar;
            if(sDelimChar != " ") {
                sNewValue += " ";
            }
        }
        else { 
            sNewValue = sResultMatch;
        }
		
		/*
		*   beCPG - PQU : manage association
		*/
        
        // Update input field
        //elTextbox.value = sNewValue;
		
		var resultData = elListItem._oResultData;
		var inputOrig = document.getElementById(elTextbox.id + "-cntrl-orig");
		var inputAdded = document.getElementById(elTextbox.id + "-cntrl-added");
		var inputRemoved = document.getElementById(elTextbox.id + "-cntrl-removed");
		var inputBasket = document.getElementById(elTextbox.id + "-cntrl-basket");
		if(inputOrig != null && inputAdded != null && inputRemoved != null) {
		
			if(inputBasket == null){
				//PQU : cannot remove, because we want a multiple selection, we have to use the button remove to clean the field
				if(inputOrig.value != "" && inputOrig.value != sNewValue) {
					inputRemoved.value = inputOrig.value;
				}
				inputAdded.value = sNewValue;
			}
			else{			
				if(inputAdded.value != "")
					inputAdded.value += ",";

				inputAdded.value += sNewValue;
			}			
		}

		//PQU : multiple selection		
		if(inputBasket != null)
		{
			if(inputBasket.innerHTML != "")
				inputBasket.innerHTML += "<br> ";

			inputBasket.innerHTML += resultData[1];			
			elTextbox.value = "";
		}
		else
		{
			elTextbox.value = resultData[1];
		}
    
        // Scroll to bottom of textarea if necessary
        if(elTextbox.type == "textarea") {
            elTextbox.scrollTop = elTextbox.scrollHeight;
        }
    
        // Move cursor to end
        var end = elTextbox.value.length;
        this._selectText(elTextbox,end,end);
    
        this._elCurListItem = elListItem;
    }
};
  
 
})();
