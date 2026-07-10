function main() {
    if (args.nodeRef) {
        var node = search.findNode(args.nodeRef);
        if (node) {
            node.properties["qa:batchState"] = "Valid";
            node.save();
            model.success = true;
            return;
        }
    }
    model.success = false;
}
main();