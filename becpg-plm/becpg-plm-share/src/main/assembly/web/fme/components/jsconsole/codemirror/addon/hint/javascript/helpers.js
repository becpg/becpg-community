(function() {	
    var defs = {
        "isNullOrEmpty": {
            "!type": "fn(value: any) -> boolean",
            "!doc": "(alias isEmpty)\n\n<span class=\"param\">@param</span> {<span class=\"type\">any</span>} <span class=\"var\">value</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">boolean</span>} true if value is empty or null\n"
        },
        "isEmpty": {
            "!type": "fn(value: any) -> boolean",
            "!doc": "(alias isNullOrEmpty)\n\n<span class=\"param\">@param</span> {<span class=\"type\">any</span>} <span class=\"var\">value</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">boolean</span>} true if value is empty or null\n"
        },
        "orEmpty": {
            "!type": "fn(value: any, value: any, defaultValue: any) -> any",
            "!doc": "<span class=\"param\">@param</span> {<span class=\"type\">any</span>} <span class=\"var\">value</span>\n<span class=\"param\">@param</span> {<span class=\"type\">any</span>} [<span class=\"var\">defaultValue</span>] the default value to use if value param is null\n<span class=\"returns\">@returns</span> {<span class=\"type\">any</span>} value or empty if null;\n"
        },
        "getProp": {
            "!type": "fn(node: ?, propName: string) -> any",
            "!doc": "(alias propValue)\n\n<span class=\"param\">@param</span> {(<span class=\"type\">ScriptNode</span>|<span class=\"type\">NodeRef</span>|<span class=\"type\">string</span>)} <span class=\"var\">node</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">propName</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">any</span>} node property value or empty\n"
        },
        "propValue": {
            "!type": "fn(node: ?, propName: string) -> any",
            "!doc": "(alias getProp)\n\n<span class=\"param\">@param</span> {(<span class=\"type\">ScriptNode</span>|<span class=\"type\">string</span>)} <span class=\"var\">node</span> string\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">propName</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">any</span>} node property value or empty\n"
        },
        "getNode": {
            "!type": "fn(node: ?) -> ScriptNode",
            "!doc": "<span class=\"param\">@param</span> {(<span class=\"type\">ScriptNode</span>|<span class=\"type\">NodeRef</span>|<span class=\"type\">string</span>)} <span class=\"var\">node</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">ScriptNode</span>} node or get the node if node param is a nodeRef string\n"
        },
        "getMLProp": {
            "!type": "fn(node: ?, propName: string, locale: string) -> string",
            "!doc": "(alias mlPropValue)\n\n<span class=\"param\">@param</span> {(<span class=\"type\">ScriptNode</span>|<span class=\"type\">NodeRef</span>|<span class=\"type\">string</span>)} <span class=\"var\">node</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">propName</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">locale</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">string</span>} node multilingual property value for given locale or empty\n"
        },
        "mlPropValue": {
            "!type": "fn(node: ?, propName: string, locale: string) -> string",
            "!doc": "(alias getMLProp)\n\n<span class=\"param\">@param</span> {(<span class=\"type\">ScriptNode</span>|<span class=\"type\">NodeRef</span>|<span class=\"type\">string</span>)} <span class=\"var\">node</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">propName</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">locale</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">string</span>} node multilingual property value for given locale or empty\n"
        },
        "getMLConstraint": {
            "!type": "fn(propValue: string, propName: string, locale: string) -> string",
            "!doc": "(alias mlPropConstraint)\n\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">propValue</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">propName</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">locale</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">string</span>} display value or empty for multilingual constraint value\n"
        },
        "mlPropConstraint": {
            "!type": "fn(propValue: string, propName: string, locale: string) -> string",
            "!doc": "(alias getMLConstraint)\n\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">propValue</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">propName</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">locale</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">string</span>} display value or empty for multilingual constraint value\n"
        },
        "assocValue": {
            "!type": "fn(node: ?, assocName: string) -> NodeRef",
            "!doc": "<span class=\"param\">@param</span> {(<span class=\"type\">ScriptNode</span>|<span class=\"type\">NodeRef</span>|<span class=\"type\">string</span>)} <span class=\"var\">node</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">assocName</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">NodeRef</span>} first matching nodeRef for given assocName\n"
        },
        "assocValues": {
            "!type": "fn(node: ?, assocName: string) -> [NodeRef]",
            "!doc": "<span class=\"param\">@param</span> {(<span class=\"type\">ScriptNode</span>|<span class=\"type\">NodeRef</span>|<span class=\"type\">string</span>)} <span class=\"var\">node</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">assocName</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">NodeRef[]</span>} nodeRef array for given assocName\n"
        },
        "sourceAssocValues": {
            "!type": "fn(node: ?, assocName: string) -> [NodeRef]",
            "!doc": "<span class=\"param\">@param</span> {(<span class=\"type\">ScriptNode</span>|<span class=\"type\">NodeRef</span>|<span class=\"type\">string</span>)} <span class=\"var\">node</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">assocName</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">NodeRef[]</span>} nodeRef array for given source assocName\n"
        },
        "assocPropValues": {
            "!type": "fn(node: ?, assocName: string, propName: string) -> [any]",
            "!doc": "<span class=\"param\">@param</span> {(<span class=\"type\">ScriptNode</span>|<span class=\"type\">NodeRef</span>|<span class=\"type\">string</span>)} <span class=\"var\">node</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">assocName</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">propName</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">any[]</span>} association property array of values\n"
        },
        "assocAssocValues": {
            "!type": "fn(node: ?, assocName: string, assocAssocName: string) -> [NodeRef]",
            "!doc": "<span class=\"param\">@param</span> {(<span class=\"type\">ScriptNode</span>|<span class=\"type\">NodeRef</span>|<span class=\"type\">string</span>)} <span class=\"var\">node</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">assocName</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">assocAssocName</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">NodeRef[]</span>} association association nodeRef array\n"
        },
        "assocPropValue": {
            "!type": "fn(node: ?, assocName: string, propName: string) -> any",
            "!doc": "(alias getAssoc)\n\n<span class=\"param\">@param</span> {(<span class=\"type\">ScriptNode</span>|<span class=\"type\">NodeRef</span>|<span class=\"type\">string</span>)} <span class=\"var\">node</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">assocName</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">propName</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">any</span>} association property value\n"
        },
        "assocAssocValue": {
            "!type": "fn(node: ?, assocName: string, assocAssocName: string) -> NodeRef",
            "!doc": "<span class=\"param\">@param</span> {(<span class=\"type\">ScriptNode</span>|<span class=\"type\">NodeRef</span>|<span class=\"type\">string</span>)} <span class=\"var\">node</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">assocName</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">assocAssocName</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">NodeRef</span>} association association nodeRef\n"
        },
        "getAssoc": {
            "!type": "fn(node: ?, assocName: string, propName: string) -> any",
            "!doc": "(alias assocPropValue)\n\n<span class=\"param\">@param</span> {(<span class=\"type\">ScriptNode</span>|<span class=\"type\">NodeRef</span>|<span class=\"type\">string</span>)} <span class=\"var\">node</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">assocName</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} [<span class=\"var\">propName</span>=\"cm:name\"] optional, default propName value is \"cm:name\"\n<span class=\"returns\">@returns</span> {<span class=\"type\">any</span>} association property value\n"
        },
        "updateAssoc": {
            "!type": "fn(node: ?, assocName: string, values: ?) -> void",
            "!doc": "Update assoc with values\n\n<span class=\"param\">@param</span> {(<span class=\"type\">ScriptNode</span>|<span class=\"type\">NodeRef</span>|<span class=\"type\">string</span>)} <span class=\"var\">node</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">assocName</span>\n<span class=\"param\">@param</span> {(<span class=\"type\">ScriptNode</span>|<span class=\"type\">ScriptNode[]</span>|<span class=\"type\">NodeRef</span>|<span class=\"type\">NodeRef[]</span>)} <span class=\"var\">values</span>\n    if values is empty, array or null, assoc will be cleared before update\n<span class=\"returns\">@returns</span> {<span class=\"type\">void</span>}\n"
        },
        "removeAssocs": {
            "!type": "fn(node: ?, assocName: string) -> void",
            "!doc": "Remove Associations\n\n<span class=\"param\">@param</span> {(<span class=\"type\">ScriptNode</span>|<span class=\"type\">NodeRef</span>|<span class=\"type\">string</span>)} <span class=\"var\">node</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">assocName</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">void</span>}\n"
        },
        "copyAssocPropValue": {
            "!type": "fn(node: ?, node: string, assocName: string, propName: string, nodePropName: string) -> boolean",
            "!doc": "Copy association property value to node property\n\n<span class=\"param\">@param</span> {(<span class=\"type\">ScriptNode</span>|<span class=\"type\">NodeRef</span>|<span class=\"type\">string</span>)} <span class=\"var\">node</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">assocName</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">propName</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">nodePropName</span> target node property name\n<span class=\"returns\">@returns</span> {<span class=\"type\">boolean</span>} true if property has changed\n"
        },
        "setValue": {
            "!type": "fn(node: ?, propName: string, value: any) -> boolean",
            "!doc": "Set property value checking if property changed\n\n<span class=\"param\">@param</span> {(<span class=\"type\">ScriptNode</span>|<span class=\"type\">NodeRef</span>|<span class=\"type\">string</span>)} <span class=\"var\">node</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">propName</span>\n<span class=\"param\">@param</span> {<span class=\"type\">any</span>} <span class=\"var\">value</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">boolean</span>} true if property has changed\n"
        },
        "setExtraValue": {
            "!type": "fn(entity: RepositoryEntity, propName: string, value: any) -> boolean",
            "!doc": "Set property value on repository entity\n\n<span class=\"param\">@param</span> {<span class=\"type\">RepositoryEntity</span>} <span class=\"var\">entity</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">propName</span>\n<span class=\"param\">@param</span> {<span class=\"type\">any</span>} <span class=\"var\">value</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">boolean</span>} true\n"
        },
        "copyAssocAssocValue": {
            "!type": "fn(node: ?, node: string, assocName: string, assocAssocName: string, nodeAssocName: string) -> void",
            "!doc": "Copy association association value to node association\n\n<span class=\"param\">@param</span> {(<span class=\"type\">ScriptNode</span>|<span class=\"type\">NodeRef</span>|<span class=\"type\">string</span>)} <span class=\"var\">node</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">assocName</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">assocAssocName</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">nodeAssocName</span> target node association name\n<span class=\"returns\">@returns</span> {<span class=\"type\">void</span>}\n"
        },
        "i18n": {
            "!type": "fn(key: string, params: [any]) -> ",
            "!doc": "<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">key</span>\n<span class=\"param\">@param</span> {<span class=\"type\">any[]</span>} [<span class=\"var\">params</span>] optional array of params\n<span class=\"returns\">@returns</span> i18n message for current locale\n"
        },
        "updateMLText": {
            "!type": "fn(node: ScriptNode, propQName: string, locale: string, value: string) -> void",
            "!doc": "Update multilingual value\n\n<span class=\"param\">@param</span> {<span class=\"type\">ScriptNode</span>} <span class=\"var\">node</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">propQName</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">locale</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">value</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">void</span>}\n"
        },
        "removeForbiddenChar": {
            "!type": "fn(value: string) -> string",
            "!doc": "(alias cleanName)\n\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">value</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">string</span>} valid cm:name value\n"
        },
        "cleanName": {
            "!type": "fn(value: string) -> string",
            "!doc": "(alias removeForbiddenChar)\n\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">value</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">string</span>} valid cm:name value\n"
        },
        "concatName": {
            "!type": "fn(name: string, value: string, separator: string) -> string",
            "!doc": "<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">name</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">value</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} [<span class=\"var\">separator</span>=\" \"] optional, default separator is \" \"\n<span class=\"returns\">@returns</span> {<span class=\"type\">string</span>} concatenated value of name + separator + value\n"
        },
        "classifyByHierarchy": {
            "!type": "fn(productNode: ScriptNode, folderNode: ScriptNode, propHierarchy: string) -> boolean",
            "!doc": "Classify node by hierarchy\n\n<span class=\"param\">@param</span> {<span class=\"type\">ScriptNode</span>} <span class=\"var\">productNode</span>\n<span class=\"param\">@param</span> {<span class=\"type\">ScriptNode</span>} <span class=\"var\">folderNode</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} [?<span class=\"var\">propHierarchy</span>] optional\n<span class=\"returns\">@returns</span> {<span class=\"type\">boolean</span>} true if success\n"
        },
        "classifyByDate": {
            "!type": "fn(productNode: ScriptNode, path: string, date: Date, date: string, dateFormat: string, documentLibrary: ScriptNode) -> boolean",
            "!doc": "Classify node by date\n\n<span class=\"param\">@param</span> {<span class=\"type\">ScriptNode</span>} <span class=\"var\">productNode</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">path</span>\n<span class=\"param\">@param</span> {<span class=\"type\">Date</span>} <span class=\"var\">date</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">dateFormat</span>\n<span class=\"param\">@param</span> {<span class=\"type\">ScriptNode</span>} [<span class=\"var\">documentLibrary</span>] optional\n<span class=\"returns\">@returns</span> {<span class=\"type\">boolean</span>} true if success\n"
        },
        "formulate": {
            "!type": "fn(product: ScriptNode) -> void",
            "!doc": "Triggers formulation on product\n\n<span class=\"param\">@param</span> {<span class=\"type\">ScriptNode</span>} <span class=\"var\">product</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">void</span>}\n"
        },
        "classifyByPropAndHierarchy": {
            "!type": "fn(productNode: ScriptNode, productNode: string, folderNode: ScriptNode, propHierarchy: string, propPathName: string, propPathName: string, locale: string) -> void",
            "!doc": "Classify node by prop and hierarchy (beta)\n\n<span class=\"param\">@param</span> {<span class=\"type\">ScriptNode</span>} <span class=\"var\">productNode</span>\n<span class=\"param\">@param</span> {<span class=\"type\">ScriptNode</span>} <span class=\"var\">folderNode</span> classification start on this folderNode\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">propHierarchy</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">propPathName</span> correspond to a property of 'productNode'\n    or a property of a assocs of productNode, exemple: \"bcpg:plant|bcpg:geoOrigine|bcpg:isoCode\".\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">locale</span> if 'propPathName' is a ML Property,\n    locale specifies which language to use to naming subfolder.\n<span class=\"returns\">@returns</span> {<span class=\"type\">void</span>}\n"
        },
        "getOrCreateFolder": {
            "!type": "fn(folderNode: ScriptNode, targetFolder: string) -> ScriptNode",
            "!doc": "Get or create target folder if it doesn't exist in folderNode\n\n<span class=\"param\">@param</span> {<span class=\"type\">ScriptNode</span>} <span class=\"var\">folderNode</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">targetFolder</span> target folder name\n<span class=\"returns\">@returns</span> {<span class=\"type\">ScriptNode</span>} target folder node\n"
        },
        "getOrCreateFolderByPath": {
            "!type": "fn(entity: ScriptNode, path: string) -> ScriptNode",
            "!doc": "Get or create folder in entity if given path is valid\n\n<span class=\"param\">@param</span> {<span class=\"type\">ScriptNode</span>} <span class=\"var\">entity</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">path</span> target folder path\n<span class=\"returns\">@returns</span> {<span class=\"type\">ScriptNode</span>} target folder node\n"
        },
        "isInSite": {
            "!type": "fn(productNode: ScriptNode, siteId: string) -> boolean",
            "!doc": "<span class=\"param\">@param</span> {<span class=\"type\">ScriptNode</span>} <span class=\"var\">productNode</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">siteId</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">boolean</span>} true if productNode is in siteId\n"
        },
        "isInUserFolder": {
            "!type": "fn(productNode: ScriptNode) -> boolean",
            "!doc": "<span class=\"param\">@param</span> {<span class=\"type\">ScriptNode</span>} <span class=\"var\">productNode</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">boolean</span>} true if productNode is in user folder\n"
        },
        "isInFolder": {
            "!type": "fn(productNode: ScriptNode) -> boolean",
            "!doc": "Warning not efficient at all!!\n\n<span class=\"param\">@param</span> {<span class=\"type\">ScriptNode</span>} <span class=\"var\">productNode</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">boolean</span>} true if productNode is in folderNode\n"
        },
        "createBranch": {
            "!type": "fn(node: ScriptNode, dest: ScriptNode, autoMerge: boolean) -> void",
            "!doc": "Create new branch of entity\n\n<span class=\"param\">@param</span> {<span class=\"type\">ScriptNode</span>} <span class=\"var\">node</span>\n<span class=\"param\">@param</span> {<span class=\"type\">ScriptNode</span>} <span class=\"var\">dest</span>\n<span class=\"param\">@param</span> {<span class=\"type\">boolean</span>} <span class=\"var\">autoMerge</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">void</span>}\n"
        },
        "mergeBranch": {
            "!type": "fn(node: ScriptNode, branchToNode: ScriptNode, description: string, type: string) -> void",
            "!doc": "Merge branch\n\n<span class=\"param\">@param</span> {<span class=\"type\">ScriptNode</span>} <span class=\"var\">node</span>\n<span class=\"param\">@param</span> {<span class=\"type\">ScriptNode</span>} <span class=\"var\">branchToNode</span> can be null if autoMerge\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">description</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">type</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">void</span>}\n"
        },
        "getAvailableName": {
            "!type": "fn(dest: ScriptNode, name: string) -> string",
            "!doc": "Get an available name adding (n) if same name exists in destination\n\n<span class=\"param\">@param</span> {<span class=\"type\">ScriptNode</span>} <span class=\"var\">dest</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">name</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">string</span>} available name\n"
        },
        "moveAndRename": {
            "!type": "fn(node: ScriptNode, dest: ScriptNode) -> void",
            "!doc": "Move node and rename if same name exists in destination\n\n<span class=\"param\">@param</span> {<span class=\"type\">ScriptNode</span>} <span class=\"var\">node</span>\n<span class=\"param\">@param</span> {<span class=\"type\">ScriptNode</span>} <span class=\"var\">dest</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">void</span>}\n"
        },
        "generateEAN13Code": {
            "!type": "fn(prefix: string) -> void",
            "!doc": "Generate EAN13 code with autonum corresponding to prefix\n\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">prefix</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">void</span>}\n"
        },
        "getDocumentLibraryNodeRef": {
            "!type": "fn(siteId: string) -> ScriptNode",
            "!doc": "<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">siteId</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">ScriptNode</span>} document library folder node for site\n"
        },
        "setPermissionAsSystem": {
            "!type": "fn(node: ?, permission: string, authority: string) -> void",
            "!doc": "Set permissions as system\n<span class=\"example\">@example</span> bcpg.setPermissionAsSystem(document, \"Consumer\", \"GROUP_EVERYONE\");\n\n<span class=\"param\">@param</span> {(<span class=\"type\">ScriptNode</span>|<span class=\"type\">NodeRef</span>|<span class=\"type\">string</span>)} <span class=\"var\">node</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">permission</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">authority</span> username or group name\n<span class=\"returns\">@returns</span> {<span class=\"type\">void</span>}\n"
        },
        "allowWrite": {
            "!type": "fn(node: ?, authority: string) -> void",
            "!doc": "Set write permissions as system bypassing rights\n<span class=\"example\">@example</span> bcpg.allowWrite(document, \"GROUP_EVERYONE\");\n\n<span class=\"param\">@param</span> {(<span class=\"type\">ScriptNode</span>|<span class=\"type\">NodeRef</span>|<span class=\"type\">string</span>)} <span class=\"var\">node</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">authority</span> username or group name\n<span class=\"returns\">@returns</span> {<span class=\"type\">void</span>}\n"
        },
        "getQNameTitle": {
            "!type": "fn(qname: string) -> string",
            "!doc": "<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">qname</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">string</span>} the qname title for the provided qname\n"
        },
        "allowRead": {
            "!type": "fn(node: ?, authority: string) -> void",
            "!doc": "Set read permissions as system bypassing rights\n<span class=\"example\">@example</span> bcpg.allowRead(document, \"GROUP_EVERYONE\");\n\n<span class=\"param\">@param</span> {(<span class=\"type\">ScriptNode</span>|<span class=\"type\">NodeRef</span>|<span class=\"type\">string</span>)} <span class=\"var\">node</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">authority</span> username or group name\n<span class=\"returns\">@returns</span> {<span class=\"type\">void</span>}\n"
        },
        "clearPermissions": {
            "!type": "fn(node: ?, inherit: boolean) -> void",
            "!doc": "Remove permissions set on node, as system\n\n<span class=\"param\">@param</span> {(<span class=\"type\">ScriptNode</span>|<span class=\"type\">NodeRef</span>|<span class=\"type\">string</span>)} <span class=\"var\">node</span>\n<span class=\"param\">@param</span> {<span class=\"type\">boolean</span>} <span class=\"var\">inherit</span> also remove inherit permission\n<span class=\"returns\">@returns</span> {<span class=\"type\">void</span>}\n"
        },
        "deleteGroupPermission": {
            "!type": "fn(node: ?, group: string) -> void",
            "!doc": "Remove specific group permissions\n<span class=\"example\">@example</span> deleteGroupPermission(document, \"GROUP_SG_FTEBIC_MANAGER\");\n\n<span class=\"param\">@param</span> {(<span class=\"type\">ScriptNode</span>|<span class=\"type\">NodeRef</span>|<span class=\"type\">string</span>)} <span class=\"var\">node</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">group</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">void</span>}\n"
        },
        "copyList": {
            "!type": "fn(sourceNode: ScriptNode, destNode: ScriptNode, listQname: string) -> void",
            "!doc": "Copy one list from sourceNode to destNode\n\n<span class=\"param\">@param</span> {<span class=\"type\">ScriptNode</span>} <span class=\"var\">sourceNode</span>\n<span class=\"param\">@param</span> {<span class=\"type\">ScriptNode</span>} <span class=\"var\">destNode</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">listQname</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">void</span>}\n"
        },
        "listExist": {
            "!type": "fn(node: ScriptNode, listQname: string) -> boolean",
            "!doc": "<span class=\"param\">@param</span> {<span class=\"type\">ScriptNode</span>} <span class=\"var\">node</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">listQname</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">boolean</span>} true if list exists and non empty\n"
        },
        "listItems": {
            "!type": "fn(node: ScriptNode, listQname: string) -> [NodeRef]",
            "!doc": "<span class=\"param\">@param</span> {<span class=\"type\">ScriptNode</span>} <span class=\"var\">node</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">listQname</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">NodeRef[]</span>} items nodeRef array\n"
        },
        "toISO8601": {
            "!type": "fn(dateObject: Date, options: object) -> string",
            "!doc": "Converts a JavaScript native Date object into a ISO8601-formatted string\n\nOriginal code:\n   dojo.date.stamp.toISOString\n   Copyright (c) 2005-2008, The Dojo Foundation\n   All rights reserved.\n   BSD license (http://trac.dojotoolkit.org/browser/dojo/trunk/LICENSE)\n\n<span class=\"method\">@method</span> toISO8601\n<span class=\"param\">@param</span> <span class=\"var\">dateObject</span> {<span class=\"type\">Date</span>} JavaScript Date object\n<span class=\"param\">@param</span> <span class=\"var\">options</span> {<span class=\"type\">object</span>} Optional conversion options\n   zulu = true|false\n   selector = \"time|date\"\n   milliseconds = true|false\n<span class=\"return\">@return</span> {<span class=\"type\">string</span>}\n<span class=\"static\">@static</span>\n"
        },
        "submitForm": {
            "!type": "fn(entity: ScriptNode, formDataJson: JSON) -> void",
            "!doc": "Applies formulary data (properties and associations) to an entity\n\n<span class=\"param\">@param</span> {<span class=\"type\">ScriptNode</span>} <span class=\"var\">entity</span>\n<span class=\"param\">@param</span> {<span class=\"type\">JSON</span>} <span class=\"var\">formDataJson</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">void</span>}\n"
        },
        "getEntityListFromNode": {
            "!type": "fn(product: ScriptNode, listName: string) -> ScriptNode",
            "!doc": "<span class=\"example\">@example</span> getEntityListFromNode(product, \"compoList\");\n\n<span class=\"param\">@param</span> {<span class=\"type\">ScriptNode</span>} <span class=\"var\">product</span>\n<span class=\"param\">@param</span> {<span class=\"type\">string</span>} <span class=\"var\">listName</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">ScriptNode</span>} entityList named listName from given product\n"
        },
        "isOnCreateEntity": {
            "!type": "fn(node: ?) -> boolean",
            "!doc": "<span class=\"param\">@param</span> {(<span class=\"type\">ScriptNode</span>|<span class=\"type\">NodeRef</span>)} <span class=\"var\">node</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">boolean</span>} true if we are creating entity\n"
        },
        "isOnMergeEntity": {
            "!type": "fn(node: ?) -> boolean",
            "!doc": "<span class=\"param\">@param</span> {(<span class=\"type\">ScriptNode</span>|<span class=\"type\">NodeRef</span>)} <span class=\"var\">node</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">boolean</span>} true if we are merging entity\n"
        },
        "isOnMergeMinorVersion": {
            "!type": "fn(node: ?) -> boolean",
            "!doc": "<span class=\"param\">@param</span> {(<span class=\"type\">ScriptNode</span>|<span class=\"type\">NodeRef</span>)} <span class=\"var\">node</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">boolean</span>} true if we are merging entity to minor version\n"
        },
        "isOnMergeMajorVersion": {
            "!type": "fn(node: ?) -> boolean",
            "!doc": "<span class=\"param\">@param</span> {(<span class=\"type\">ScriptNode</span>|<span class=\"type\">NodeRef</span>)} <span class=\"var\">node</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">boolean</span>} true if we are merging entity to major version\n"
        },
        "isOnCopyEntity": {
            "!type": "fn(node: ?) -> boolean",
            "!doc": "<span class=\"param\">@param</span> {(<span class=\"type\">ScriptNode</span>|<span class=\"type\">NodeRef</span>)} <span class=\"var\">node</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">boolean</span>} true if we are copying entity\n"
        },
        "isOnFormulateEntity": {
            "!type": "fn(node: ?) -> boolean",
            "!doc": "<span class=\"param\">@param</span> {(<span class=\"type\">ScriptNode</span>|<span class=\"type\">NodeRef</span>)} <span class=\"var\">node</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">boolean</span>} true if we are formulating entity\n"
        },
        "isOnBranchEntity": {
            "!type": "fn(node: ?) -> boolean",
            "!doc": "<span class=\"param\">@param</span> {(<span class=\"type\">ScriptNode</span>|<span class=\"type\">NodeRef</span>)} <span class=\"var\">node</span>\n<span class=\"returns\">@returns</span> {<span class=\"type\">boolean</span>} true if we are branching entity\n"
        }
    };

    CodeMirror.tern.addDef(defs);
})();