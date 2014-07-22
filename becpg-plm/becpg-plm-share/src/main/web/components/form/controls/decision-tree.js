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

                  /**
                   * Fired by YUI when parent element is available for scripting.
                   * 
                   * @method onReady
                   */
                  onReady : function DecisionTree_onReady() {
                     
                     var QUESTION_EVENTCLASS = Alfresco.util.generateDomId(null, "question"),
                         COMMENT_EVENTCLASS= Alfresco.util.generateDomId(null, "comment");
                     
                    
                     var htmlForm = "";
                     for(var i = 0; i< this.options.data.length; i++){
                        var question = this.options.data[i],showComment = false;
                      
                        if(question.choices){
                        
                           htmlForm += '<fieldset  id="'+this.id+'-question_'+question.id+'" class="hidden">';
                           htmlForm += '<legend title="'+this.msg("form.control.decision-tree."+this.options.prefix+"."+question.id+".description")+'">'
                                    +question.id.toUpperCase()+' - '
                                    + (question.label ? question.label: this.msg("form.control.decision-tree."+this.options.prefix+"."+question.id+".label"))
                                    +'</legend>';
                           for(var j = 0; j< question.choices.length; j++){
                              var choice = question.choices[j];
                              var checked = this.getCurrentValueChecked(question.id, choice.id );
                              htmlForm +="<p>";
                              htmlForm +='<input '+(this.options.disabled?'disabled':'')+' tabindex="0" id="'+this.id+'-choice_'+question.id+'_'+choice.id+'" class="'+QUESTION_EVENTCLASS+'" name="--group_'+question.id+'" type="radio"  '+(checked?"checked":"")+' />';
                              var msgKey  =  choice.id == "-" ? "form.control.decision-tree.empty" : "form.control.decision-tree."+this.options.prefix+"."+question.id+"."+choice.id;
                              htmlForm +='<label for="'+this.id+'-choice_'+question.id+'_'+choice.id+'">'+(choice.label ? choice.label:  this.msg(msgKey))+'</label>';
                              htmlForm +="</p>";   
                             
                              if(choice.comment){
                                 showComment = true;
                              }
                           }
                           
                           if(showComment){
                              htmlForm +='<div id="'+this.id+'-comment_'+question.id+'" class="decision-tree-comments hidden" >';
                              htmlForm +='<label for="'+this.id+'-comment_'+question.id+'-input">'+this.msg("form.control.decision-tree."+this.options.prefix+"."+question.id+".comment")+':</label>';
                              htmlForm +='<input '+(this.options.disabled?'disabled':'')+' tabindex="0" id="'+this.id+'-comment_'+question.id+'-input" class="'+COMMENT_EVENTCLASS+'"  type="textarea"  value="'+this.getCurrentValueComment(question.id)+'" name="--comment_'+question.id+'" />';
                              htmlForm +='</div>';
                           }
                          
                          htmlForm += '</fieldset>';
                        }  else {
                           htmlForm += '<div id="'+this.id+'-question_'+question.id+'" class="hidden decision-tree-message">';
                           htmlForm += '<span>'+this.msg("form.control.decision-tree."+this.options.prefix+"."+question.id+".label")+'</span>';
                           htmlForm += '</div>';
                        }
                     }
                     
                     var ctrlBody = Dom.get(this.id+"-body");
                     ctrlBody.innerHTML = htmlForm;

                     
                     
                     var me = this;
                     me.toogleVisible();
                     
                     var fnOnSelectChoice = function DT__fnOnSelectChoice(layer, args) {
                        var owner = Bubbling.getOwnerByTagName(args[1].input, "input");
                        if (owner !== null) {
                           owner.checked = true;
                           me.toogleVisible();
                        }
                        return false;
                     };
                     
                     
                     
                     Bubbling.addDefaultAction(QUESTION_EVENTCLASS, fnOnSelectChoice);
                     
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
                  
                  getCurrentValueComment: function (qid){
                     for(var i = 0; i< this.options.currentValue.length; i++){
                        var question_id = this.options.currentValue[i].qid;
                          if(qid  == question_id  &&  this.options.currentValue[i].comment ){
                           return  this.options.currentValue[i].comment;
                          }
                     }
                     return "";
                  },
                  
                  toogleVisible : function (){
                     
                     var ret = [],me = this;
                     
                     var visible = [];
                     
                     for(var i = 0; i< this.options.data.length; i++){
                        var question = this.options.data[i];
                        if(i == 0){
                           visible.push(question.id);
                        }
                        if(visible.indexOf(question.id) > -1 && question.choices){
                           
                           var showComment = false;
                           for(var j = 0; j< question.choices.length; j++){
                             var choice = question.choices[j];
                             if(Dom.get(this.id+"-choice_"+question.id+'_'+choice.id).checked){
                                ret.push({ qid : question.id, cid : choice.id});
                                
                                if(choice.cid){
                                    if(choice.cid instanceof Array){
                                        for(var z =0 ; z <choice.cid ; z++){
                                            visible.push(choice.cid[z]);
                                        }
                                    } else {
                                        visible.push(choice.cid);
                                    }
                                }
                                
                                if(choice.comment){
                                   showComment = true; 
                                } 
                             }
                           }
                           
                           if(showComment){
                              Dom.removeClass(this.id+"-comment_"+question.id,"hidden");
                              ret[ret.length-1].comment = Dom.get(this.id+"-comment_"+question.id+"-input").value;
                              YAHOO.util.Event.addListener(this.id+"-comment_"+question.id+"-input", "blur", function(e) {me.toogleVisible();} );
                              
                           } else if(Dom.get(this.id+"-comment_"+question.id)){
                              YAHOO.util.Event.purgeElement(this.id+"-comment_"+question.id+"-input");
                              Dom.addClass(this.id+"-comment_"+question.id,"hidden");
                             
                           }
                           
                        } 
                        
                        
                        if(visible.indexOf(question.id) > -1){
                           Dom.removeClass(this.id+"-question_"+question.id,"hidden");
                        } else {
                           Dom.addClass(this.id+"-question_"+question.id,"hidden");
                        }
                     }
                     
                     if(!this.options.disabled){
                         var hiddenInput = Dom.get(this.fieldId);
                         hiddenInput.value = JSON.stringify(ret);
                     }
                  }
                    

               }, true);

})();
