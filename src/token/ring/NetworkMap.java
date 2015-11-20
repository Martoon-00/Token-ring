package token.ring;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Precautions:
 *
 */
public class NetworkMap implements Serializable {
    private final List<NodeInfo> nodes = new LinkedList<>();

    public NetworkMap(NodeInfo myNode) {
        nodes.add(myNode);
    }

    public void add(NodeInfo nodeInfo) {
        // note, we don't prefer updated info to existed
        if (!nodes.contains(nodeInfo)) {
            nodes.add(nodeInfo);
        }
    }

    public NodeInfo getNextFrom(NodeInfo myNode) {
        add(myNode);

        boolean returnHere = false;
        for (NodeInfo node : nodes) {
            if (returnHere)
                return node;

            if (node.equals(myNode))
                returnHere = true;
        }
        if (returnHere) {
            return nodes.get(0);
        } else {
            throw new Error("Unexpected behaviour, no myNode found in map");
        }
    }

    public int size() {
        return nodes.size();
    }

    public void remove(NodeInfo nodeInfo) {
        nodes.remove(nodeInfo);
    }

    public Stream<NodeInfo> nodeInfos() {
        return nodes.stream();
    }

}
