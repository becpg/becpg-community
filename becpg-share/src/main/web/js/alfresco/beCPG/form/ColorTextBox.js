define([	"dijit/form/ValidationTextBox",
		    "dojo/_base/declare",
		    "dojo/text!./templates/ColorTextBox.html",
		    "dijit/_HasDropDown",
		    "alfresco/beCPG/form/_ColorPickerDropDown",
		    "dojo/_base/lang",
		    "dojo/dom-style"
		    ], function( ValidationTextBox, declare, template,
		        _HasDropDown, _ColorPickerDropDown, lang, domStyle ){
		    return declare([  ValidationTextBox , _HasDropDown ], {
		        templateString : template,
		        regExp: "(?:[0-9a-fA-F]{3}){1,2}",
		        cssRequirements: [{cssFile:"./css/ColorTextBox.css"}],
		        postCreate : function(){
		        	alert("pwwet");
		            this.connect(this.focusNode, "onclick", "openDropDown");
		            this.watch( "value", lang.hitch( this, function(attr, oldVal, newVal){
		
		                if(newVal === ""){
		                    domStyle.set(this._buttonNode, "background", "transparent" );
		                } else {
		                    domStyle.set(this._buttonNode, "background", "#" + newVal );
		                }
		            }) );
		            this.inherited( arguments );
		        },
		        openDropDown: function(/*Function*/ callback){
		
		            if( this.dropDown ){
		                this.dropDown.destroyRecursive();
		            }
		            var _changeMethod = function(){
		                var hex = this.picker.get('value');
		                if (hex.substring(0, 1) === '#') {
		                    hex = hex.substring(1);
		                    this.parent.set('value', hex);
		                }
		            };
		
		            var lastHex = this.get('value');
		
		            this.dropDown = new _ColorPickerDropDown({
		                parent : this,
		                value : "#" + lastHex,
		                onCancel : lang.hitch( this.dropDown, function(){
		                     this.parent.set('value', lastHex );
		                }),
		                onChange :lang.hitch( this.dropDown, _changeMethod ),
		                onExecute : lang.hitch( this.dropDown, _changeMethod )
		            });
		
		            this.inherited(arguments);
		        },
		        closeDropDown: function() {
		            this.inherited(arguments);
		            if (this.dropDown) {
		                this.dropDown.destroy();
		                this.dropDown = null;
		            }
		        }
		
		
		
		    });
		
		});