/**
 * Alfresco Preferences.
 *
 * @namespace beCPG.service
 * @class Alfresco.service.Preferences
 */
(function() {
	
	var BASKET_KEY_PREFIX = "fr.becpg.basket.";
	
	/**
	 * Preferences constructor.
	 *
	 * @return {beCPG.service.Basket} The new Alfresco.service.Preferences instance
	 * @constructor
	 */
	beCPG.service.Basket = function Basket_constructor() {
		beCPG.service.Basket.superclass.constructor.call(this);
		return this;
	};

	YAHOO.extend(beCPG.service.Basket, Alfresco.service.BaseService,
		{

			toggle: function(record) {
				if (this.isbrowserSupported()) {
					if (this.isInBasket(record)) {
						localStorage.removeItem(this.key(record));
					} else {
						localStorage.setItem(this.key(record), JSON.stringify(record));
					}
					YAHOO.Bubbling.fire("basketChanged");
				}

			},
			add : function(record) {
				if (this.isbrowserSupported()) {
					if (!this.isInBasket(record)) {
						localStorage.setItem(this.key(record), JSON.stringify(record));
						YAHOO.Bubbling.fire("basketChanged");
					}
					
				}

			},
			isInBasket: function(record) {
				if (this.isbrowserSupported()) {
					var key = this.key(record);
					var item = localStorage.getItem(key);
					
					return  item!= null;
				}
				return false;
			},

			isbrowserSupported: function() {
				if ('localStorage' in window && window['localStorage'] !== null) {
					return true;
				}
				return false;
			},

			key: function(record) {
				return BASKET_KEY_PREFIX + record.nodeRef.replace(':/','').replaceAll(/\//g,'_');
			},

			getRecords: function() {
				var nodes = new Array();
				if (this.isbrowserSupported()) {
					for (var i = 0; i < localStorage.length; i++) {
						var key = localStorage.key(i);
						if (key.indexOf(BASKET_KEY_PREFIX) == 0) {
							nodes.push(JSON.parse(localStorage.getItem(key)));
						}
					}
				}
				return nodes;
			},
			
			empty: function() {
				if (this.isbrowserSupported()) {
					var hasresults = true;
					
					while(hasresults){
						hasresults = false;
						for (var i = 0; i < localStorage.length; i++) {
							var key = localStorage.key(i);
							if (key.indexOf(BASKET_KEY_PREFIX) == 0) {
								localStorage.removeItem(key);
								hasresults = true;
							}
						}
					}
					YAHOO.Bubbling.fire("basketChanged");
				}
			},

			count: function() {
				return this.getRecords().length;
			}
		});
})();