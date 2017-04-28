(function()
{
	
/*
	<field id="acme:fieldId">
     <constraint-handlers>
       <constraint type="MANDATORY" validation-handler="beCPG.forms.validation.mandatoryIf|{\"field_bcpg_code\":\"EMPTY\"}" event="keyup" />
       <constraint type="MANDATORY" validation-handler="beCPG.forms.validation.mandatoryIf|{\"field_bcpg_code\":\"PF25\"}" event="keyup" />
       <constraint type="MANDATORY" validation-handler="beCPG.forms.validation.mandatoryIf|{\"field_bcpg_code\":\"NOTEMPTY\"}" event="keyup" />
       <constraint type="MANDATORY" validation-handler="beCPG.forms.validation.hideIf|{\"field_bcpg_code\":\"EMPTY\"}" event="keyup" />
       <!-- Not -->
       <constraint type="MANDATORY" validation-handler="beCPG.forms.validation.hideIf|{\"field_bcpg_code\":\"-PF25\"})" event="keyup" />
       <constraint type="MANDATORY" validation-handler="beCPG.forms.validation.hideIf|{\"field_bcpg_code\":\"PF24\"})" event="keyup" />
       <constraint type="MANDATORY" validation-handler="beCPG.forms.validation.hideIf|{\"field_bcpg_code\":\"EMPTY\"})" event="keyup" />
       
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
		   			
		   			var  match = (args[i].condition == field.value);
		   			
		   			if(args[i].condition.indexOf("|")>0){
		   				var conds = args[i].condition.split("|");
		   				for(var j in conds){
		   					if(conds[j] == field.value){
		   						match = true;
		   						break;
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
	   

})();