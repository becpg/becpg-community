define([	"alfresco/core/Core",
		"dojo/_base/declare", 
		    "dijit/_WidgetBase",
		    "dijit/_TemplatedMixin",
		    "dojox/widget/ColorPicker",
		    "dijit/ColorPalette",
		    "dojo/text!./templates/_ColorPickerDropDown.html",
		    "dojo/_base/lang",
	        "dojo/dom-style"
		    ], function( AlfCore,declare,_WidgetBase,Templated, ColorPicker, ColorPalette, template, lang, domStyle ){
		    return declare( [AlfCore, _WidgetBase, Templated], {
		        templateString : template,
		        i18nRequirements: [{i18nFile: "./i18n/_ColorPickerDropDown.properties"}],
		        postCreate : function(){
		        	var me =this;
		        	
		            this.picker = new ColorPicker({
		                animatePoint:false,
		                showHsv: false,
		                showRgb: false,
		                webSafe:false,
		                onChange :  lang.hitch( this, this.onChange )
		            }, this._colorPickerNode);
		         
		            
		            this.palette = new ColorPalette({
		            	palette: "7x10",
		                onChange :  function (val){
		                	me.picker.setColor(val);
		                }
		            }, this._colorPaletteNode);
		            
		            this.palette.startup();
		           
		            if(this.value.length === 7){
		                this.picker.setColor( this.value.trim() );
		            }
		            
		            alert(this.message("form.button.ok.label"));
		            
	            
		            this.inherited( arguments );
		        },
		        onCancel : function(){
		
		        },
		        onExecute : function(){
		
		        }
		    });
		
		});