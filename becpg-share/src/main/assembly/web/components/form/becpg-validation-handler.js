(function()
{
	
/*
<!-- general comments field is porp/assoc -->

	<field id="acme:fieldId">
		<constraint-handlers>
			<!-- mandatoryIf --> 
			<constraint type="MANDATORY" validation-handler="beCPG.forms.validation.mandatoryIf" event='keyup,change@{"prop":"field_bcpg_code", "condition" :"EMPTY"}' />
			<constraint type="MANDATORY" validation-handler="beCPG.forms.validation.mandatoryIf" event='keyup,change@{"prop":"field_bcpg_code", "condition" :"NOTEMPTY\"}' />
			<constraint type="MANDATORY" validation-handler="beCPG.forms.validation.mandatoryIf" event='keyup,change@{"prop":"field_bcpg_code", "condition" :"PF25"}' />
			<!-- hideIf -->
			<constraint type="MANDATORY" validation-handler="beCPG.forms.validation.hideIf" event='keyup,change@{"prop":"field_bcpg_code", "condition" :""}' />
			<constraint type="MANDATORY" validation-handler="beCPG.forms.validation.hideIf" event='keyup,change@{"prop":"field_bcpg_code", "condition" :"PF24"}' />
			<constraint type="MANDATORY" validation-handler="beCPG.forms.validation.hideIf" event='keyup,change@{"prop":"field_bcpg_code", "condition" :"PF24|PF25"}' />
	        <!-- hideIf with regex (hide if not PF24)-->
	        <constraint type="MANDATORY" validation-handler="beCPG.forms.validation.hideIf" event='keyup,change@{"prop":"field_bcpg_code", "condition" :"RegExp_^(?!PF24$).*"}' />
	        <!-- GTIN -->
	        <constraint type="MANDATORY" validation-handler="beCPG.forms.validation.GTIN" event="keyup" />
     	</constraint-handlers>
    </field>
	
	http://docs.alfresco.com/5.1/concepts/dev-extensions-share-form-field-validation-handlers.html
	
*/
	 if(beCPG.forms == null) beCPG.forms = {};

	 if(beCPG.forms.validation == null) beCPG.forms.validation = {};

	 beCPG.forms.validation.GTIN = function mandatory(field, args, event, form, silent, message)
	   {
	      var barcode  = field.value;
	      
	      if(barcode == null || barcode.length == 0){
	    	  return true;
	      }
	   // checksum calculation for GTIN-8, GTIN-12, GTIN-13, GTIN-14, and SSCC
	   // based on http://www.gs1.org/barcodes/support/check_digit_calculator

	     // check length
	     if (barcode.length < 8 || barcode.length > 18 ||
	         (barcode.length != 8 && barcode.length != 12 && 
	         	barcode.length != 13 && barcode.length != 14 && 
	         	barcode.length != 18)) {
	       return false;
	     }

	     var lastDigit = Number(barcode.substring(barcode.length - 1));
         
	     if (isNaN(lastDigit)) { return false; } // not a valid upc/ean

	     var arr = barcode.substring(0,(barcode.length -1)).split("");
	     var checkSum = 0, oddTotal = 0, evenTotal = 0, poids = 0;

	     for (var i=0; i<arr.length; i++) {

	       if (isNaN(arr[i])) { return false; } // can't be a valid upc/ean we're checking for

           (barcode.length == 13) ? poids = (i+1) : poids = i;

	       if ( poids % 2 == 0) { 
	       		oddTotal += (Number(arr[i]) * 3); 
            } else { 
            	evenTotal += Number(arr[i]); 
            }
	     }

	     checkSum = (10 - ((evenTotal + oddTotal) % 10)) % 10;

	     // true if they are equal
	     return checkSum == lastDigit;

	   };
	
	   /*
		args must be on the form : 
		*	args =	[ 
		*				{  "prop" : "prop_bcpg_erpCode",  "condition" : "PF25" },
		*	   			{"prop" : "prop_erpCode",  "condition" : "NOEMPTY"}
		*	   			];
	 */
	   beCPG.forms.validation.mandatoryIf = function mandatory(field, args, event, form, silent, message)
	   {
	   		var mandatory = true;
	   		
	   		for(var i in args) {
	   			
	   			var propM = document.getElementsByName(args[i].prop)[0].value;

		   		switch(args[i].condition){
		   			case "EMPTY":
		   				if(  propM != null && propM.trim().length == 0  ) {
		   					if(field.value.trim().length ==0)
		   						mandatory = false;
		   				}
		   				break;
		   			case "NOEMPTY": 
		   				if( propM != null && propM.trim().length != 0) {
		   					if(field.value.trim().length ==0)
		   						mandatory = false;
		   				}
		   				break;
		   			case propM: 
		   				if(field.value.trim().length == 0) {
				   			mandatory = false;
		   				}
		   				break;
		   		}
	   		}
	   		return mandatory;
	   };
	   
	   //can hide multiple elements.
	 beCPG.forms.validation.hideIf = function mandatory(field, args, event, form, silent, message)
	   {	
		   	for(var i in args){
		   		var propName = form.formId.substring(0,form.formId.length-5)+"_"+args[i].prop;
		   		var inputEl =  YAHOO.util.Dom.get(propName);
		   			while(inputEl!=null){
		   				inputEl = inputEl.parentNode;
		   				if(YAHOO.util.Dom.hasClass(inputEl,"form-field")){
		   					break;
		   				}
		   			}
		   		if(inputEl!=null){
		   			var match;
		   			if(args[i].condition != null && args[i].condition.indexOf("RegExp_") == 0){
		   				var regex = new RegExp(args[i].condition.replace("RegExp_", ""));
			   			match = field.value.match(regex) != null;
		   			}else {
		   				match = (args[i].condition == field.value);
		   				if(args[i].condition.indexOf("|") != -1){
		   					var conds = args[i].condition.split("|");
		   					for(var j in conds){
		   						regex = new RegExp();
		   						if(field.value == conds[j]){
		   							match = true;
		   							break;
		   						}
		   					}
		   				}
		   			}

		   			
			   		if(match){
			   			YAHOO.util.Dom.addClass(inputEl,"hidden");
				   	} else {
				   		YAHOO.util.Dom.removeClass(inputEl,"hidden");
				   	}
		   		} 
		   	} 	
		   	return true;

	 }; 
	 
	 
	  beCPG.forms.validation.filterBy = function mandatory(field, args, event, form, silent, message)
       {    
            var indexes = [];

               for (var i in args) {
                   var propName = form.formId.substring(0, form.formId.length-5) + "_" + args[i].prop;
                   var parentEl = YAHOO.util.Dom.get(propName);

                   if (parentEl!=null && args[i].condition == "STARTWITH") {
                    var regex = new RegExp("^" + parentEl.value);

                    for (var j = 0; j < field.options.length; j++) {
                        if (field.options[j].value.match(regex) == null) {
                            YAHOO.util.Dom.addClass(field.options[j], "hidden");
                            if (indexes.indexOf(j) != -1) { indexes.splice(j, 1); }
                        } else {
                            YAHOO.util.Dom.removeClass(field.options[j], "hidden");
                            if (indexes.indexOf(j) == -1) { indexes.push(j); }
                        }
                    }
                   }
               }

               // Update child selected option after filtering
               if (YAHOO.util.Dom.hasClass(field.options[field.selectedIndex], "hidden")) {
                if (indexes.length > 0) {
                    field.selectedIndex = indexes[0];
                } else {
                    field.selectedIndex = -1;
                }
            }

            return true;
     };
	   

})();