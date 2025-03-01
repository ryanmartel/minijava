package vaporMGen;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class NodeList {

    private LinkedHashMap<Integer, Node> nodeList;

    public NodeList() {
        this.nodeList = new LinkedHashMap<>();
    }

    public void addNode(Node n) {
        Integer lineNum = n.line;
        Node exists = nodeList.get(lineNum);
        if (exists != null) {
            if (n.isCall) {
                exists.isCall = true;
            }
            exists.line = lineNum;
            exists.inst = n.inst;
            for (int i : n.succ) {
                exists.succ.add(i);
            }
            for (int i : n.pred) {
                exists.pred.add(i);
            }
            for (String s : n.read) {
                exists.read.add(s);
            }
            for (String s : n.def) {
                exists.def.add(s);
            }
        } else {
            nodeList.put(lineNum, n);
        }
        
    }

    public void addEdge(Integer from, Integer to) {
        if (nodeList.get(from) == null) {
            Node n = new Node();
            nodeList.put(from, n);
        }
        if (nodeList.get(to) == null) {
            Node n = new Node();
            nodeList.put(to, n);
        }
        nodeList.get(from).succ.add(to);
        nodeList.get(to).pred.add(from);
    }

    public Set<Map.Entry<Integer, Node>> entrySet() {
        return nodeList.entrySet();
    }

    public Set<Integer> keySet() {
        return nodeList.keySet();
    }

    // Line number
    public Node get(Integer i) {
        return nodeList.get(i);
    }
}
