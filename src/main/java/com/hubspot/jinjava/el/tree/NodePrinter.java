package com.hubspot.jinjava.el.tree;


import java.io.PrintWriter;
import java.util.Stack;

/**
 * Node pretty printer for debugging purposes.
 *
 * @author Christoph Beck
 */
public class NodePrinter {
    private static boolean isLastSibling(Node node, Node parent) {
        if (parent != null) {
            return node == parent.getChild(parent.getCardinality() - 1);
        }
        return true;
    }

    private static void dump(PrintWriter writer, Node node, Stack<Node> predecessors) {
        if (!predecessors.isEmpty()) {
            Node parent = null;
            for (Node predecessor: predecessors) {
                if (isLastSibling(predecessor, parent)) {
                    writer.print("   ");
                } else {
                    writer.print("|  ");
                }
                parent = predecessor;
            }
            writer.println("|");
        }
        Node parent = null;
        for (Node predecessor: predecessors) {
            if (isLastSibling(predecessor, parent)) {
                writer.print("   ");
            } else {
                writer.print("|  ");
            }
            parent = predecessor;
        }
        writer.print("+- ");
        writer.println(node.toString());

        predecessors.push(node);
        for (int i = 0; i < node.getCardinality(); i++) {
            dump(writer, node.getChild(i), predecessors);
        }
        predecessors.pop();
    }

    public static void dump(PrintWriter writer, Node node) {
        dump(writer, node, new Stack<>());
    }
}

