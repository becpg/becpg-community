<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/admin/admin-common.lib.js">

function main() {
    model.tools = Admin.getConsoleTools("becpg-audit");
    model.metadata = Admin.getServerMetaData();

    model.plugins = [
        {
            id: "formulation",
            label: msg.get("audit.tab.formulation"),
            columns: [
                { key: "startedAt", label: msg.get("audit.col.startedAt"), format: "date", isDb: true },
                { key: "completedAt", label: msg.get("audit.col.completedAt"), format: "date" },
                { key: "duration", label: msg.get("audit.col.duration"), format: "duration" },
                { key: "entityName", label: msg.get("audit.col.entityName") },
                { key: "chainId", label: msg.get("audit.col.chainId") },
                { key: "entityNodeRef", label: msg.get("audit.col.nodeRef"), format: "noderef" }
            ]
        },
        {
            id: "batch",
            label: msg.get("audit.tab.batch"),
            columns: [
                { key: "startedAt", label: msg.get("audit.col.startedAt"), format: "date", isDb: true },
                { key: "completedAt", label: msg.get("audit.col.completedAt"), format: "date" },
                { key: "duration", label: msg.get("audit.col.duration"), format: "duration" },
                { key: "batchId", label: msg.get("audit.col.batchId") },
                { key: "batchUser", label: msg.get("audit.col.batchUser") },
                { key: "totalItems", label: msg.get("audit.col.totalItems") },
                { key: "totalErrors", label: msg.get("audit.col.totalErrors"), format: "errors" },
                { key: "isCompleted", label: msg.get("audit.col.status"), format: "completed" }
            ]
        },
        {
            id: "report",
            label: msg.get("audit.tab.report"),
            columns: [
                { key: "startedAt", label: msg.get("audit.col.startedAt"), format: "date", isDb: true },
                { key: "completedAt", label: msg.get("audit.col.completedAt"), format: "date" },
                { key: "duration", label: msg.get("audit.col.duration"), format: "duration" },
                { key: "name", label: msg.get("audit.col.reportName") },
                { key: "format", label: msg.get("audit.col.format"), format: "badge" },
                { key: "locale", label: msg.get("audit.col.locale") },
                { key: "datasourceSize", label: msg.get("audit.col.datasourceSize"), format: "size" },
                { key: "entityNodeRef", label: msg.get("audit.col.nodeRef"), format: "noderef" }
            ]
        },
        {
            id: "export_search",
            label: msg.get("audit.tab.export_search"),
            columns: [
                { key: "startedAt", label: msg.get("audit.col.startedAt"), format: "date", isDb: true },
                { key: "completedAt", label: msg.get("audit.col.completedAt"), format: "date" },
                { key: "duration", label: msg.get("audit.col.duration"), format: "duration" },
                { key: "filename", label: msg.get("audit.col.filename") },
                { key: "username", label: msg.get("audit.col.username") },
                { key: "template", label: msg.get("audit.col.template") },
                { key: "resultsSize", label: msg.get("audit.col.resultsSize") },
                { key: "async", label: msg.get("audit.col.async"), format: "async" }
            ]
        },
        {
            id: "activity",
            label: msg.get("audit.tab.activity"),
            columns: [
                { key: "prop_cm_created", label: msg.get("audit.col.date"), format: "date", isDb: true },
                { key: "prop_bcpg_alUserId", label: msg.get("audit.col.username") },
                { key: "prop_bcpg_alType", label: msg.get("audit.col.type"), format: "badge" },
                { key: "entityNodeRef", label: msg.get("audit.col.nodeRef"), format: "noderef" },
                { key: "prop_bcpg_alData", label: msg.get("audit.col.data"), format: "inspect" }
            ]
        }
    ];
}

main();
