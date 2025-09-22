
//Todo @Valentin should be an helper because -prepare can change and it's really internal
function findUrlDeliverable() {

    var keyName = deliverable.name.replace(" - prepare", "");

    for (var i = 0; i < project.deliverableList.size(); i++) {
        var del = project.deliverableList.get(i);
        if (del.name == keyName + " - url") {
            if (del.content) {
                return del;
            }
            return null;
        }
    }

    return null;
}


function main() {

    if (task.resources != null && !task.resources.isEmpty()) {
        var urlDeliverable = findUrlDeliverable()
        if (urlDeliverable != null) {
            var document = search.findNode(del.content);
            if (document != null) {
                var recipients = [];
                recipients.push(search.findNode(task.resources.get(0)));

                bSign.prepareForSignature(document, recipients);

                urlDeliverable.url = bSign.getSignatureView(document, recipient, task.nodeRef);
            }
        }
    }

}

main();