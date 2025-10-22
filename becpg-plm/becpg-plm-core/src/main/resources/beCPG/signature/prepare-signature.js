
function main() {

    if (task.resources != null && !task.resources.isEmpty()) {
        var urlDeliverable = bSignProject.findUrlDeliverable(project, deliverable);
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