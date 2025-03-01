package vaporMGen;


import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import cs132.vapor.ast.*;

public class LiveIntervals {

    private NodeList cfg;
    private VFunction function;


    public ArrayList<Interval> intervals;
    public ArrayList<Integer> calls;
    public ArrayList<Interval> unused;


    public LiveIntervals(NodeList cfg, VFunction function) {
        this.cfg = cfg;
        this.function = function;

        this.intervals = new ArrayList<>();
        this.calls = new ArrayList<>();
        this.unused = new ArrayList<>();
        ConstructIntervals();
    }

    public void ConstructIntervals() {
        for (VVarRef param : function.params) {
            addDef(param.toString(), param.sourcePos.line);
        }
        LinkedList<Integer> nodeRef = new LinkedList<>(cfg.keySet());
        // Iterate backwards through calls to propagate live-outs
        for(int i = nodeRef.size()-1; i >= 0; i--) {
            Integer line = nodeRef.get(i);
            Node node = cfg.get(line);
            node.fillIntervals();

            if (node.isCall) {
                calls.add(line);
            }

            for (String s : node.out) {
                if (!node.def.contains(s)) {
                    node.in.add(s);
                }
            }
            for (Integer precKey : node.pred) {
                Node pred = cfg.get(precKey);
                pred.out.addAll(node.in);
            }
            for (String dVar : node.def) {
                addDef(dVar, line);
                Interval interval = get(dVar);
                interval.declared = true;
            }
        }
        // Forward Iteration to finish propagating across edges
        for (int i = 0; i < nodeRef.size(); i++) {
            Integer line = nodeRef.get(i);
            Node node = cfg.get(line);
            for (String s : node.out) {
                if (!node.def.contains(s)) {
                    node.in.add(s);
                }
            }
        }
        // Count all in's as read since only care about last use
        for (int i = 0; i < nodeRef.size(); i++) {
            Integer line = nodeRef.get(i);
            Node node = cfg.get(line);
            for (String s : node.in) {
                addRead(s, line);
            }
        }

        for (Interval interval : intervals) {
            Collections.sort(interval.defs);
            Collections.sort(interval.uses);
            Collections.sort(interval.reads);
            // Undeclared variables must begin at function definition.
            if (!interval.declared) {
                interval.start = function.sourcePos.line;
            } else {
                interval.start = interval.defs.get(0);
            }

            // Possibly unused Variable
            if (interval.reads.size() > 0) {
                interval.end = interval.reads.get(interval.reads.size()-1);
            } else {
                unused.add(interval);
                continue;
            }

            // Check if the variable crosses a function call. 
            // This decides whether it should be placed in caller or 
            // callee preserved registers.
            for (int i : calls) {
                if (i > interval.start && i < interval.end) {
                    int nextDef = Integer.MAX_VALUE;
                    int nextRead = 0;
                    for (int r : interval.reads) {
                        if (r > i) {
                            nextRead = r;
                            break;
                        }
                    }
                    for (int d : interval.defs) {
                        if (d >= i) {
                            nextDef = d;
                            break;
                        }
                    }
                    if (nextRead <= nextDef) {
                        interval.crossCall = true;
                    }
                }
            }
        }
        // Get rid of variables that are never read. They do not need to be allocated to a register
        for (Interval notRead : unused) {
            intervals.remove(notRead);
        }
        // Sort according to start time.
        intervals.sort(Interval.getStartComparator());
    }

    private void addRead(String var, int line) {
        Interval interval = get(var);
        if (interval == null) {
            interval = new Interval(var);
            intervals.add(interval);
        }
        interval.reads.add(line);
        interval.addVarUse(false, line);
    }

    private void addDef(String var, int line) {
        Interval interval = get(var);
        if (interval == null) {
            interval = new Interval(var);
            intervals.add(interval);
        }
        interval.defs.add(line);
        interval.addVarUse(true, line);
    }

    private Interval get(String var) {
        for (Interval interval : intervals) {
            if (interval.varName.equals(var)) {
                return interval;
            }
        }
        return null;
    }
}
