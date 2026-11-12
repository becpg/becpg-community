var PRODUCT_LIST_PREFS_PREFIX = "org.alfresco.share.product.list.";

var PRODUCT_LIST_REPOSITORY_SCOPE = "home";

var PRODUCT_LIST_DEFAULT_TYPE = "product";

/**
 * Returns the id of the product repository preferences, kept apart for each site so that the
 * last type selected in one site does not leak into another.
 *
 * @param site the short name of the current site, or null for the repository-wide page
 * @return the preferences id
 */
function getProductListPrefsId(site)
{
   return PRODUCT_LIST_PREFS_PREFIX + (site != null && site.length > 0 ? site : PRODUCT_LIST_REPOSITORY_SCOPE);
}

/**
 * Resolves the type the product repository opens on. A link naming the type explicitly wins
 * (#37657), then the last type the user selected (#38110), and the generic product type when
 * there is neither, on the site pages as on the repository-wide page.
 *
 * @param preferences the product repository preferences of the current scope
 * @return the type to display
 */
function getProductListType(preferences)
{
   if (page.url.args.list)
   {
      return page.url.args.list;
   }
   return preferences.type != null ? preferences.type : PRODUCT_LIST_DEFAULT_TYPE;
}
