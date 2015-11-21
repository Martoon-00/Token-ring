package token.ring;

import java.io.Serializable;
import java.net.InetAddress;

public class NodeInfo implements Serializable {
    public final InetAddress address;

    private final UniqueValue unique;

    public NodeInfo(InetAddress address, UniqueValue unique) {
        this.address = address;
        this.unique = unique;
    }

    @Override
    public String toString() {
        return String.format("NodeInfo %s {%s}", unique, address);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeInfo nodeInfo = (NodeInfo) o;

        return unique.equals(nodeInfo.unique);

    }

    @Override
    public int hashCode() {
        return unique.hashCode();
    }
}
