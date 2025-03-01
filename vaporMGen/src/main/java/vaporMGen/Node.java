package vaporMGen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import cs132.vapor.ast.VInstr;

public class Node {

    public VInstr inst;
    public int line;
    public ArrayList<Integer> pred;
    public ArrayList<Integer> succ;

    // Which variables were read or defined in this instruction
    public ArrayList<String> read;
    public ArrayList<String> def;

    public Set<String> in;
    public Set<String> out;

    public boolean isCall;

    public Node(VInstr inst) {
        this();
        this.inst = inst;
        line = inst.sourcePos.line;
    }

    public Node() {
        isCall = false;
        this.pred = new ArrayList<>();
        this.succ = new ArrayList<>();
        this.def = new ArrayList<>();
        this.read = new ArrayList<>();
        this.in = new HashSet<>();
        this.out = new HashSet<>();
    }

    public void addRead(String s) {
        if (!filterNums(s)) {
            if (!s.startsWith(":")) {
                read.add(s);
            }
        }
    }

    public void addDef(String s) {
        if (!filterNums(s)) {
            def.add(s);
        }
    }

    private boolean filterNums(String s) {
        boolean num = false;
        try {
            Integer.parseInt(s);
            num = true;
        } catch (NumberFormatException e) {}
        return num;
    }

    public void fillIntervals() {
        for (String s : read) {
            in.add(s);
        }
        for (String s : def) {
            out.add(s);
        }
    }
}
