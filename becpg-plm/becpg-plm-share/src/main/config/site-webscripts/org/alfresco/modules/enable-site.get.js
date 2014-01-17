var activityLog = [];
function main() {
     var globalUrl = args.url;        // shortName
     var globalPreset = args.preset; // sitePreset
     var tokens = new Array();
     tokens["siteid"] = globalUrl;
     sitedata.newPreset(globalPreset, tokens);
     activityLog.push("Adding preset '" + globalPreset + "' to site '" + globalUrl + "'");
     model.activityLog = activityLog;
}
main();
