
/**
 * This is a generic banner warning that can be used to display warning and error messages
 * to the user.
 * 
 * @module becpg/header/WarningBar
 * @extends dijit/_WidgetBase
 * @mixes dijit/_TemplatedMixin
 * @mixes module:alfresco/core/Core
 * @author Matthieu Laborie
 */
define(["dojo/_base/declare",
        "dijit/_WidgetBase", 
        "dijit/_TemplatedMixin",
        "dojo/text!./templates/WarningBar.html",
        "alfresco/core/Core",
        "dojo/dom-style",
        "dojo/_base/array",
        "dojo/_base/lang",
        "dojo/dom-construct",
        "alfresco/core/CoreXhr",
        "dojo/on", 
        "dojo/dom-class"],
        function(declare, _WidgetBase, _TemplatedMixin, template, AlfCore, domStyle, array, lang, domConstruct, AlfXhr, on, domClass) {
   
   return declare([_WidgetBase, _TemplatedMixin, AlfCore, AlfXhr], {

       /**
        * An array of the i18n files to use with this widget.
        * 
        * @instance
        * @type {object[]}
        * @default [{i18nFile: "./i18n/WarningBar.properties"}]
        */
       i18nRequirements : [
       {
           i18nFile : "./i18n/WarningBar.properties"
       } ],
      /**
       * An array of the CSS files to use with this widget.
       * 
       * @instance
       * @type {object[]}
       * @default [{cssFile:"./css/WarningBar.css"}]
       */
      cssRequirements: [{cssFile:"./css/WarningBar.css"}],
      
      /**
       * The HTML template to use for the widget.
       * @instance
       * @type {String}
       */
      templateString: template,
      
      /**
       * @instance
       * @type {string}
       */
      changeOrderData : null,
      
      warningType : null,

      constructor : function alfresco_header_WarningBar__constructor(args)
      {
          lang.mixin(this, args);
          this.warningType =  args.label;
          if(this.warningType == "EcmWarning"){
        	  this.alfSubscribe("BECPG_ECM_CREATED_SUCCESS", lang.hitch(this, "onEcmCreatedSuccess"));
          }

      },


      onEcmCreatedSuccess : function alfresco_header_WarningBar__onEcmCreatedSuccess(payload)
      {
          this.addEcmBarHtml(payload);
      },
      
      createNoLicenseWarningBar : function alfresco_header_NoLicenseWarningBar__createBar()
      {
    	  this.messageBox.innerHTML = this.message("nolicense.warning.bar.message");
    	  this.stopRecording.style.display = "none";
    	  domStyle.set(this.domNode, "display", "block");
      },
      
      createUnauthorizedWarningBar : function alfresco_header_UnauthorizedWarningBar__createBar()
      {
    	  this.messageBox.innerHTML = this.message("unauthorized.warning.bar.message");
    	  this.stopRecording.style.display = "none";
    	  domStyle.set(this.domNode, "display", "block");
    	  domClass.add(this.warningsNode, "licence-warning");
      },
      
      
      /**
       * @instance
       */
      postCreate: function alfresco_header_WarningBar__postCreate() {
    	  
    	  domClass.remove(this.warningsNode, "licence-warning");
    	  
    	  if(this.warningType == "NoLicenseWarning"){
    	  		this.createNoLicenseWarningBar();
    	  		return;
    	  	}

			if(this.warningType == "UnauthorizedWarning"){
    	  		this.createUnauthorizedWarningBar();
    	  		return;
    	  	}
        
              this.alfPublish("ALF_PREFERENCE_GET", {
                  preference: "fr.becpg.ecm.currentEcmNodeRef",
                  callback: function(ecmNodeRef) {
                      
                      if(ecmNodeRef!=null){
                          var url = Alfresco.constants.PROXY_URI + "becpg/ecm/changeorder/"+ecmNodeRef.replace(":/","")+"/infos";
                          this.serviceXhr(
                          {
                              url : url,
                              method : "GET",
                              successCallback : function (response, originalRequestConfig)
                              {
                                  
                                  if (typeof response == "string")
                                  {
                                      var response = JSON.parse(this.cleanupJSONResponse(response));
                                  }
                                  
                                  this.addEcmBarHtml(response);
                              },
                              failureCallback : function (response, originalRequestConfig)
                              {
                                  domStyle.set(this.domNode, "display", "none");
                              },
                              callbackScope : this
                          });
                       
                      }
                  },
                  callbackScope: this
               });
        
              this.stopRecording.innerHTML = this.message("ecm.warning.bar.stop.recording");
              
              var me = this;
              on(this.stopRecording, "click", function(evt) {
                  if(me.changeOrderData!=null){
                      me.alfPublish("ALF_PREFERENCE_SET", {
                          preference: "fr.becpg.ecm.currentEcmNodeRef",
                          value: null
                       });
                  }
                  me.changeOrderData = null;
                  domStyle.set(me.domNode, "display", "none");
                });
              
      },
      
      
      /**
       * Adds a message to be displayed
       *
       * @instance
       * @param {string} message The message to add
       * @param {number} index The index of the message
       * @param {number} level The severity of the message
       */
      addEcmBarHtml: function alfresco_header_WarningBar__addMessage(changeOrderData) {
         this.changeOrderData = changeOrderData;
          
         domConstruct.create("span", {
             innerHTML: this.message("ecm.warning.bar.message")
          }, this.messageBox);
         
         domConstruct.create("a", {
            href : Alfresco.constants.URL_PAGECONTEXT+"entity-data-lists?list=View-properties&nodeRef="+changeOrderData.nodeRef,
            innerHTML: changeOrderData.name
         }, this.messageBox);
         
         domStyle.set(this.domNode, "display", "block");
      }
      
      
      
   });
});