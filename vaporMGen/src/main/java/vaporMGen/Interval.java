package vaporMGen;

import java.util.ArrayList;
import java.util.Comparator;

public class Interval {

    public String varName;
    public int start;
    public int end;
    public boolean declared = false;
    public boolean crossCall = false;

    public ArrayList<Integer> reads;
    public ArrayList<Integer> defs;
    public ArrayList<varUse> uses;

    public Interval(String varName) {
        this.reads = new ArrayList<>();
        this.uses = new ArrayList<>();
        this.defs = new ArrayList<>();
        this.varName = varName;
        declared = false;
    }

    public class varUse implements Comparable<varUse>{
        // false -> read true -> def
        public boolean isDef;
        public int line;
        public varUse (boolean isDef, int line) {
            this.isDef = isDef;
            this.line = line;
        }

        public int compareTo(varUse other) {
            return Integer.compare(line, other.line);
        }

    }

    public void addVarUse(boolean isDef, int line) {
        this.uses.add(new varUse(isDef, line));
    }

    public static Comparator<Interval> getStartComparator() {
        return new Comparator<Interval>() {
            public int compare(Interval a, Interval b) {
                return Integer.compare(a.start, b.start);
            }
        };
    }
    public static Comparator<Interval> getEndComparator() {
        return new Comparator<Interval>() {
            public int compare(Interval a, Interval b) {
                return Integer.compare(a.end, b.end);
            }
        };
    }

}
