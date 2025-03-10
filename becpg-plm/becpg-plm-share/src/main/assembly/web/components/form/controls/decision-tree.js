(function() {
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom, 
   			Bubbling = YAHOO.Bubbling ;
   /**
    * DecisionTree constructor.
    * 
    * @param htmlId
    *            {String} The HTML id of the parent element
    * @return {beCPG.component.DecisionTree} The new DecisionTree instance
    * @constructor
    */
   beCPG.component.DecisionTree = function(htmlId, fieldId) {

      beCPG.component.DecisionTree.superclass.constructor.call(this, "beCPG.component.DecisionTree", htmlId, [ "button",
            "container" ]);

      Bubbling.on("afterFormRuntimeInit", this.onAfterFormRuntimeInit, this);
      Bubbling.on("refreshDecisionTree", this.refreshDecisionTree, this);
      
      this.fieldId = fieldId;
      
      return this;
   };

   /**
    * Extend from Alfresco.component.Base
    */
   YAHOO.extend(beCPG.component.DecisionTree, Alfresco.component.Base);

   /**
    * Augment prototype with main class implementation, ensuring overwrite is enabled
    */
   YAHOO.lang
         .augmentObject(
               beCPG.component.DecisionTree.prototype,
               {
                   
                  formRuntime : null, 
                  /**
                   * Object container for initialization options
                   * 
                   * @property options
                   * @type object
                   */
                  options : {
                      disabled: false,
                      prefix : "",
                      data: [],
                      currentValue: []
                  },

                  //Alfresco.forms.validation.mandatory
                  
                  /**
                   * Fired by YUI when parent element is available for scripting.
                   * 
                   * @method onReady
                   */
                  onReady : function DecisionTree_onReady() {
                     
                     var QUESTION_EVENTCLASS = Alfresco.util.generateDomId(null, "question"),
                         LIST_EVENTCLASS = Alfresco.util.generateDomId(null, "list"),
                         COMMENT_EVENTCLASS= Alfresco.util.generateDomId(null, "comment");
                     
                    
                     var htmlForm = "";
                     for(var i = 0; i< this.options.data.length; i++){
                        var question = this.options.data[i],showComment = false, textarea = false , commentLabel = null;
                      
                        if(question.choices){
                        
                           htmlForm += '<fieldset  id="'+this.id+'-question_'+question.id+'" class="hidden">';
                           htmlForm += '<legend title="'+this.msg("form.control.decision-tree."+this.options.prefix+"."+question.id+".description")+'">'
                                    + ( question.id.length < 10 ? question.id.toUpperCase()+' - ' : "")
                                    + (question.label ? question.label: this.msg("form.control.decision-tree."+this.options.prefix+"."+question.id+".label"))
                                    +'</legend>';
                           if(question.note){
                        	   htmlForm += '<span class="decision-tree-note">'+question.note+'</span>';
                           }
                           
                           if(question.upperNote){
                        	   htmlForm += '<span class="decision-tree-note">'+question.upperNote+'</span>';
                           }
                           
                           
                           if(question.url){
                        	   if(question.url instanceof Array){
                                   for(var z=0 ; z<question.url.length ; z++){
                                	   htmlForm += '<span class="decision-tree-url"><a title="' + this.msg("link.title.open-link") + '" href="' + question.url[z]  + '"><img src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/link-16.png" />'+question.url[z]+'</a></span>';
                                   }
                               } else {
                            	   htmlForm += '<span class="decision-tree-url"><a title="' + this.msg("link.title.open-link") + '" href="' + question.url  + '"><img src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/link-16.png" />'+question.url+'</a></span>';
                               }
                              }
                          
                           
                           for(var j = 0; j< question.choices.length; j++){
                              var choice = question.choices[j];

                              if(choice.list!=null){
                                  var listOption = this.getCurrentListOptions(question.id, choice.id );
                                 if(!choice.checkboxes){
                                	 htmlForm +="<p>";
                                 }
                                  var msgKey  =  choice.id == "-" ? "form.control.decision-tree.empty" : "form.control.decision-tree."+this.options.prefix+"."+question.id+"."+choice.id;           

                                  if(choice.multiple && choice.checkboxes){
                                	  for(var z = 0; z< choice.list.length; z++){
                                          var selected = false;
                                          var lbl = choice.list[z];
                                          var val = z;
                                          if(lbl.indexOf('|')>0){
											  lbl = choice.list[z].split('|')[1];
											  val = choice.list[z].split('|')[0];
										  }
                                          if(listOption!=null && listOption!=""){
                                            var values = listOption.split(",");
                                              for(var zz = 0; zz< values.length; zz++){
                                                  if(values[zz] == val){
                                                      selected = true;
                                                      break;
                                                  }
                                              }  
                                          }
                                          htmlForm +='<p><input type="checkbox" id="checkbox-'+this.id+question.id+'_'+choice.id+'_'+z+'" name="--group_'+this.id+question.id+'_'+choice.id+'" '+(this.options.disabled?'disabled':'')+' tabindex="0"  class="'+QUESTION_EVENTCLASS+'"  value="'+val+'" '+( selected ? 'checked="checked"':"")+'>';
                                          htmlForm +='<label for="checkbox-'+this.id+question.id+'_'+choice.id+'_'+z+'" >'+lbl+'</label></p>';
                                	  
                                	  }	  
                                	  
                                  } else {
									  if(choice.label != "hidden"){
                                		  htmlForm +='<label for="'+this.id+'-choice_'+question.id+'_'+choice.id+'">'+(choice.label ? choice.label:  this.msg(msgKey))+'</label>';
                                	  }
	                                  htmlForm +='<select '+(choice.multiple ? 'multiple="true"':"")+' '+(this.options.disabled?'disabled':'')+' tabindex="0" id="'+this.id+'-select_'+question.id+'_'+choice.id+'" class="'+LIST_EVENTCLASS+'" name="--group_'+this.id+question.id+'_'+choice.id+'"  >';
	                                  for(var z = 0; z< choice.list.length; z++){
	                                      var selected = false;
                                      
                                          var lbl = choice.list[z];
                                          var val = choice.list[z];
                                          if(lbl.indexOf('|')>0){
											  lbl = choice.list[z].split('|')[1];
											  val = choice.list[z].split('|')[0];
										  }
	                                      
	                                      
	                                      if(choice.multiple){
	                                          var values = listOption.split(",");
	                                          for(var zz = 0; zz< values.length; zz++){
	                                              if(values[zz] == val){
	                                                  selected = true;
	                                                  break;
	                                              }
	                                          }
	                                      } else {
	                                          selected = listOption == val;
	                                      }
	                                      htmlForm +='<option value="'+val+'" '+( selected ? "selected":"")+'>'+lbl+'</option>';
	                                  }
	                                  htmlForm +='</select>';
	                                  
                                  }
                                  if(! choice.checkboxes){
                                	  htmlForm +="</p>";   
                                  }
                                  
                                  YAHOO.util.Event.addListener(this.id+'-select_'+question.id+'_'+choice.id, "change", function(){
                                      me.toogleVisible();
                                  });
                                  
                              } else {
                              
                                  if(choice.label != "hidden"){
                                	  var checked = this.getCurrentValueChecked(question.id, choice.id );
	                                  htmlForm +="<p>";
	                                  htmlForm +='<input '+(this.options.disabled?'disabled':'')+' tabindex="0" id="'+this.id+'-choice_'+question.id+'_'+choice.id+'" class="'+QUESTION_EVENTCLASS+'" name="--group_'+this.id+question.id+'" type="radio"  '+(checked?'checked="checked"':"")+' />';
	                                  var msgKey  =  choice.id == "-" ? "form.control.decision-tree.empty" : "form.control.decision-tree."+this.options.prefix+"."+question.id+"."+choice.id;
	                                  htmlForm +='<label for="'+this.id+'-choice_'+question.id+'_'+choice.id+'">'+(choice.label ? choice.label:  this.msg(msgKey))+'</label>';
	                                  htmlForm +="</p>";   
                                  }
                                 
                                
                              }
                              
                              if(choice.comment){
                                  showComment = true;
                                  
                                  if(choice.textarea){
                                      textarea = choice.textarea;
                                  }
                               }
                           }
                           
                           if(showComment){
                              htmlForm +='<div id="'+this.id+'-comment_'+question.id+'" class="decision-tree-comments hidden" >';
                              if(choice.label!="hidden") {
                            	  htmlForm +='<label id="'+this.id+'-comment_'+question.id+'-label" for="'+this.id+'-comment_'+question.id+'-input">'+this.msg("form.control.decision-tree."+this.options.prefix+"."+question.id+".comment")+':</label>';
                              }
                              if(this.options.disabled){
                                  htmlForm +='<span id="'+this.id+'-comment_'+question.id+'-input" >'+this.getCurrentValueComment(question.id)+'</span>';
                              } else {
                                  if(textarea){
                                      htmlForm +='<textarea '+(this.options.disabled?'disabled':'')+' tabindex="0" id="'+this.id+'-comment_'+question.id+'-input" class="'+COMMENT_EVENTCLASS+'"  name="--comment_'+this.id+question.id+'" >'+this.getCurrentValueComment(question.id)+'</textarea>';
                                  } else {
                                      htmlForm +='<input '+(this.options.disabled?'disabled':'')+' tabindex="0" id="'+this.id+'-comment_'+question.id+'-input" class="'+COMMENT_EVENTCLASS+'"  type="text"  value="'+this.getCurrentValueComment(question.id)+'" name="--comment_'+this.id+question.id+'" />';
                                  }
                              }
                               htmlForm +='</div>';
                           }
                           
                           if(question.lowerNote){
                        	   htmlForm += '<span class="decision-tree-note">'+question.lowerNote+'</span>';
                           }
                          
                          htmlForm += '</fieldset>';
                        }  else {
                           htmlForm += '<div id="'+this.id+'-question_'+question.id+'" class="hidden decision-tree-message">';
                           htmlForm += '<span>'+(question.label ? question.label:  this.msg("form.control.decision-tree."+this.options.prefix+"."+question.id+".label"))+'</span>';
                           htmlForm += '</div>';
                        }
                     }
                     
                     var ctrlBody = Dom.get(this.id+"-body");
                     ctrlBody.innerHTML = htmlForm;

                     
                     
                     var me = this;
                     
                     var fnOnSelectChoice = function DT__fnOnSelectChoice(layer, args) {
                        var owner = Bubbling.getOwnerByTagName(args[1].input, "input");
                        if (owner !== null) {
	                        if(owner.type != "checkbox"){
		                           var previousState = owner.previousState;
		                           if(previousState == true){
		                        	   owner.checked = false;
		                        	   owner.previousState = false;
		                           } else {
		                        	   owner.checked = true;
		                        	   owner.previousState = true
		                           }
	                        }
	                       me.toogleVisible();
	                       return false;
	                        
                        }
                     };

                     Bubbling.addDefaultAction(QUESTION_EVENTCLASS, fnOnSelectChoice);
                  },
                  
                  refreshDecisionTree : function(){
                       this.toogleVisible();
                  },
                  
                  getCurrentValueChecked: function (qid, cid){
                     for(var i = 0; i< this.options.currentValue.length; i++){
                        var question_id = this.options.currentValue[i].qid;
                        var choice_id = this.options.currentValue[i].cid;
                          if(qid  == question_id  && cid == choice_id ){
                           return true;
                          }
                     }
                     return false;
                  },
                  
                  getCurrentListOptions: function (qid, cid){
                      for(var i = 0; i< this.options.currentValue.length; i++){
                         var question_id = this.options.currentValue[i].qid;
                         var choice_id = this.options.currentValue[i].cid;
                           if(qid  == question_id  && cid == choice_id ){
                            return this.options.currentValue[i].listOptions || "";
                           }
                      }
                      return "";
                   },
                  
                  getCurrentValueComment: function (qid){
                     for(var i = 0; i< this.options.currentValue.length; i++){
                        var question_id = this.options.currentValue[i].qid;
                          if(qid  == question_id  &&  this.options.currentValue[i].comment ){
                           return  this.options.currentValue[i].comment;
                          }
                     }
                     return "";
                  },
                  
                  
                  onAfterFormRuntimeInit : function (layer, args) {
                      if(!this.options.disable && this.formRuntime == null && this.id.indexOf(args[1].runtime.formId.replace("-form",""))>-1){
                          this.formRuntime = args[1].runtime;
                      
                          this.formRuntime.removeValidation = function(fieldId){
                              var foundIndex = -1;
                              for(var j = 0; j<  this.validations.length; j++){
                                  if(this.validations[j].fieldId == fieldId){
                                      foundIndex = j;
                                      break;
                                  }
                              }
                              
                              if(foundIndex>0){
                                  this.validations.splice(foundIndex,1);
                              }
                          };
                      }
                      this.toogleVisible();
                  },
                  
                  
                  toogleVisible : function (){
                     
                     var ret = [],me = this;
                     
                     var visible = [];
                     
                     for(var i = 0; i< this.options.data.length; i++){
                        var question = this.options.data[i];
                        if((i == 0 || question.start == true) && (!this.options.disabled || this.options.currentValue.length>0)){
                           visible.push(question.id);
                        }
                        if(beCPG.util.contains(visible,question.id) && question.choices){
                           
                           var showComment = false;
                           for(var j = 0; j< question.choices.length; j++){
                             var choice = question.choices[j];
                             
                             if(this.formRuntime!=null && question.mandatory){
                                 if(choice.list!=null && !choice.checkboxes){
									if(!choice.hasValidation){
	                                	 choice.hasValidation = true;
	                                     this.formRuntime.addValidation(this.id+'-select_'+question.id+'_'+choice.id, Alfresco.forms.validation.mandatory, null, "keyup");
									}
									
                                 } else {
                                	 if(choice.label != "hidden" && !choice.checkboxes && !choice.hasValidation){
                                		 choice.hasValidation = true;
	                                     this.formRuntime.addValidation(this.id+'-choice_'+question.id+'_'+choice.id, Alfresco.forms.validation.mandatory, null, "keyup");
                                	 }
	                                 if(choice.comment){
	                                      if(choice.label == "hidden" || (choice.list!=null && choice.checkboxes )  || Dom.get(this.id+"-choice_"+question.id+'_'+choice.id).checked){
	                                         if( !choice.hasCommentValidation){
	                                        	 choice.hasCommentValidation = true;
	                                        	 this.formRuntime.addValidation(this.id+"-comment_"+question.id+"-input", Alfresco.forms.validation.mandatory, null, "keyup");
	                                         }
	                                    
	                                         
	                                      } else {
	                                    	  if( choice.hasCommentValidation){
	                                    		  choice.hasCommentValidation = false;
	                                    		  this.formRuntime.removeValidation(this.id+"-comment_"+question.id+"-input");
	                                    	  }
	                                      }
	                                  }
                                	                                      
                                 }
                             }

                             if((choice.list!=null && !choice.checkboxes && Dom.get(this.id+"-select_"+question.id+'_'+choice.id).value!=null)
                                     || (choice.list!=null && choice.checkboxes )  || choice.label == "hidden" ||  Dom.get(this.id+"-choice_"+question.id+'_'+choice.id).checked ){
                             
                            	 var showVisible = false;
                                 if(choice.list!=null){
                                     var value = "";
                                     
                                     if(choice.multiple){
                                    	 
                                    	 if(choice.checkboxes){
                                    	 
	                                    	 var checkboxes = document.getElementsByName('--group_'+this.id+question.id+'_'+choice.id);  
	                                    	 
	                                    	 for(var k = 0; k < checkboxes.length; k++)  
	                                    	    {  
	                                    	        if(checkboxes[k].checked) {
	                                    	        	
	                                    	        	 if(value.length>0){
	                                                         value+=",";
	                                                     }
	                                                     value += ""+checkboxes[k].value;
	                                                     showVisible = true;
	                                    	        } 
	                                    	    }  
                                    	 } else {
                                    		 showVisible = true
                                    		 var selectElem = Dom.get(this.id+"-select_"+question.id+'_'+choice.id);
	                                         for (var j = 0, jj = selectElem.options.length; j < jj; j++)
	                                         {
	                                            if (selectElem.options[j].selected)
	                                            {
	                                               if(value.length>0){
	                                                   value+=",";
	                                               }
	                                               value += selectElem.options[j].value;
	                                            }
	                                         }
                                    	 }
                                     } else {
                                    	 var selectElem = Dom.get(this.id+"-select_"+question.id+'_'+choice.id);
                                          value = selectElem.value;
                                        
                                     }
                                     ret.push({ qid : question.id, cid : choice.id, listOptions :
                                         value });
                                     
                                 } else {
                                	 showVisible = true;
                                	 
                                     ret.push({ qid : question.id, cid : choice.id});
                                 }
                                 
                                 
                                if(choice.cid && showVisible){
                                    if(choice.cid instanceof Array){
                                        for(var z=0 ; z<choice.cid.length ; z++){
                                            visible.push(choice.cid[z]);
                                        }
                                    } else {
                                        visible.push(choice.cid);
                                    }
                                }
                                
                                if(choice.comment){
                                   showComment = true; 
                                   if( choice.commentLabel && choice.commentLabel.length > 0 &&  Dom.get(this.id+'-comment_'+question.id+'-label')!=null) {
									  Dom.get(this.id+'-comment_'+question.id+'-label').innerHTML = choice.commentLabel == "hidden" ? "" : choice.commentLabel ;
								   }
                                } 
                             }
                           }
                           
                            if(Dom.get(this.id+"-comment_"+question.id)) {
							    YAHOO.util.Event.purgeElement(this.id+"-comment_"+question.id+"-input");
	                           if(showComment){
	                              Dom.removeClass(this.id+"-comment_"+question.id,"hidden");
	                              ret[ret.length-1].comment = Dom.get(this.id+"-comment_"+question.id+"-input").value;
	                              YAHOO.util.Event.addListener(this.id+"-comment_"+question.id+"-input", "blur", function() {me.toogleVisible();} );                           
	                           } else {
	                              Dom.addClass(this.id+"-comment_"+question.id,"hidden");
	                           }
                           }
                           
                        }  else if(question.choices && this.formRuntime!=null && question.mandatory){
                            for(var j = 0; j< question.choices.length; j++){
                                var choice = question.choices[j];
                                
                                if(this.formRuntime!=null && question.mandatory){
                                    if(choice.list!=null  && !choice.checkboxes && choice.hasValidation){
                                    	choice.hasValidation = false;
                                        this.formRuntime.removeValidation(this.id+'-select_'+question.id+'_'+choice.id);
                                    } else {
                                    	if(choice.label!="hidden" && choice.hasValidation){
                                    		choice.hasValidation = false;
                                    		this.formRuntime.removeValidation(this.id+'-choice_'+question.id+'_'+choice.id);
                                    	}
                                        if(choice.comment && choice.hasCommentValidation){
                                  		    choice.hasCommentValidation = false;
                                            this.formRuntime.removeValidation(this.id+"-comment_"+question.id+"-input");
                                        }
                                    }
                                }
                            }
                           
                       }
                        
                        
                        if(beCPG.util.contains(visible,question.id)){
                           Dom.removeClass(this.id+"-question_"+question.id,"hidden");
                        } else {
                           Dom.addClass(this.id+"-question_"+question.id,"hidden");
                        }
                     }
                     
                     if(!this.options.disabled){
                         var hiddenInput = Dom.get(this.fieldId);
                         hiddenInput.value = JSON.stringify(ret);
                         YAHOO.Bubbling.fire("mandatoryControlValueUpdated");
                     }
                  }
                    

               }, true);
   
   
   
})();
