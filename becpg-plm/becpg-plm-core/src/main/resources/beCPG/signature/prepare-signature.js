
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
            var document = search.findNode(urlDeliverable.content);
            if (document != null) {
                var recipient = search.findNode(task.resources.get(0));
                var recipients = [];
                recipients.push(recipient);

                bSign.prepareForSignature(document, recipients);

                urlDeliverable.url = bSign.getSignatureView(document, recipient, task.nodeRef);
            }
        }
    }

}

main();