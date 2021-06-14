(function() {
	/**
	 * YUI Library aliases
	 */
	var Dom = YAHOO.util.Dom;

	var $html = Alfresco.util.encodeHTML;
	
	var REMOVE_EVENTCLASS= Alfresco.util.generateDomId(null,"remove-from-basket");




	/**
	 * beCPG.component.Basket constructor.
	 * 
	 * @param {String}
	 *            htmlId The HTML id of the parent element
	 * @return {beCPG.component.Basket} The new Basket instance
	 * @constructor
	 */
	beCPG.component.Basket = function Basket_constructor(htmlId) {
		beCPG.component.Basket.superclass.constructor.call(this, "beCPG.component.Basket", htmlId);

		YAHOO.Bubbling.on("basketChanged", this.onBasketChanged, this);

		return this;
	};

	YAHOO
		.extend(
			beCPG.component.Basket,
			Alfresco.component.Base,
			{

				/**
				 * Fired by YUI when parent element is available for
				 * scripting.
				 * 
				 * @method onReady
				 */
				onReady: function Basket_onReady() {
 
					var me = this;

					this.services.basket = new beCPG.service.Basket();
					

					this.widgets.basketMenu = Alfresco.util.createYUIButton(this, "basket-button", this.onBasketMenu, {
						type: "menu",
						menu: "basket-menu",
						lazyloadmenu: false,
						disabled: true
					});
					
					this.onBasketChanged();
					
					 var fnRemoveHandler = function fnRemoveHandler(layer, args)
			         {
			           	
			         	 var anchor = args[1].anchor,
			              rel = anchor.getAttribute("rel");
			              me.services.basket.toggle({"nodeRef":rel});
                          
                          anchor.parentNode.parentNode.removeChild(anchor.parentNode);
							
              			  return true;
			         };

					YAHOO.Bubbling.addDefaultAction(REMOVE_EVENTCLASS, fnRemoveHandler);
					
				},
				onBasketMenu: function(sType, aArgs) {
					
					var domEvent = aArgs[0], eventTarget = aArgs[1];
					
					// Check mandatory docList module is present

					// Get the function related to the clicked item
					var fn = Alfresco.util.findEventClass(eventTarget);
					if (fn && (typeof this[fn] == "function")) {
						this[fn].call(this, this.services.basket.getRecords());
					}

					Event.preventDefault(domEvent);
				},


				onActionEmptyBasket: function() {
					this.services.basket.empty();
					YAHOO.Bubbling.fire("metadataRefresh");
				},

				onActionShowBasket: function(records) {
					
					var popupHtml = "<div  class=\"basket-popup\"> <ul>";

					for (var i = 0; i < records.length; i++) {
						popupHtml += "<li><span class=\"content\">" + $html(records[i].displayName) + "</span>";
						popupHtml += '<a href="#" rel="' + records[i].nodeRef + '" class="'+REMOVE_EVENTCLASS+' basket-delete" title="'
	           							+ Alfresco.util.message("basket.remove.tip") + '">&nbsp;</a></li>';
					}

					popupHtml += "<ul></div>";

					Alfresco.util.PopupManager.displayPrompt(
						{
							title: this.msg("title.basket.content"),
							text: popupHtml,
							noEscape: true,
							modal: true
						});

				},
				onBasketChanged: function() {
					if (Alfresco.doclib && Alfresco.doclib.Actions) {
						var count = this.services.basket.count();
						

						var menuItems = this.widgets.basketMenu.getMenu().getItems();
						for (var m in menuItems) {
							this.widgets.basketMenu.set("label", count + "");
							break;
						}
						if (count > 0) {
							Dom.removeClass(this.id + "-basket", "hidden");
							
							this.widgets.basketMenu.set("disabled", false);
							
						} else {
							Dom.addClass(this.id + "-basket", "hidden");
							this.widgets.basketMenu.set("disabled", true);
						}
					}
				}


			});

	if (Alfresco.doclib && Alfresco.doclib.Actions) {
		YAHOO.lang.augmentProto(beCPG.component.Basket, Alfresco.doclib.Actions);
	}

})();