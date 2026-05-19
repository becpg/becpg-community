var activityLog = [];
function main() {
     var globalUrl = args.url;        // shortName
     var globalPreset = args.preset; // sitePreset

     if (!globalUrl || !globalPreset) {
          status.setCode(status.STATUS_BAD_REQUEST, "Missing url or preset parameter");
          return;
     }

     var pattern = /^[a-zA-Z0-9\-_]+$/;
     if (!pattern.test(globalUrl) || !pattern.test(globalPreset)) {
          status.setCode(status.STATUS_BAD_REQUEST, "Invalid url or preset parameter");
          return;
     }

     var tokens = new Array();
     tokens["siteid"] = globalUrl;
     sitedata.newPreset(globalPreset, tokens);
     activityLog.push("Adding preset '" + globalPreset + "' to site '" + globalUrl + "'");
     model.activityLog = activityLog;
}
main();
