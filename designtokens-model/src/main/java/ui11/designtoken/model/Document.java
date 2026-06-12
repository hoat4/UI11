package ui11.designtoken.model;

// specben nem nagyon nevezik sehogy se.
// néha file-nak nevezik, de 7.4. §-ban említik hogy "design token document structure".

public class Document extends Element {

    public Group rootGroup;

    public Node findGroupOrToken(String path) {
        if (path.contains("{") || path.contains("}") || path.isEmpty())
            throw new IllegalArgumentException("invalid group or token path: " + path);

        Node current = rootGroup;
        int begin = 0;
        boolean endOfPath = false;
        do {
            int nextSeparator = path.indexOf('.', begin);
            String name;
            if (nextSeparator == -1) {
                endOfPath = true;
                name = path.substring(begin);
            } else {
                name = path.substring(begin, nextSeparator);
                begin = nextSeparator + 1;
            }
            switch (current) {
                case Token<?> token -> {
                    throw new RuntimeException("can't resolve \"" + path + "\", because " + token.path() + " is already a " +
                            "token, not a group");
                }
                case Group group -> {
                    Node child = group.children.get(name);
                    if (child == null)
                        throw new RuntimeException("can't resolve \"" + path + "\", because no child named \"" + name + "\"" +
                                " exists in \"" + group.path() + "\"");
                    current = child;
                }
            }
        } while (!endOfPath);

        return current;
    }

    public ValueElement findElement(String jsonPointer) {
        throw new RuntimeException("TODO");
    }
}
