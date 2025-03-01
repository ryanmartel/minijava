package vaporMGen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class LinearScanAlloc {

    // Order of increasing start time
    private LiveIntervals liveIntervals;

    // Registers available for allocation
    private String[] sReg = {"s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7"};
    private String[] tReg = {"t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7"};

    // Is register currently assigned
    private boolean[] sFilled = new boolean[sReg.length];
    private boolean[] tFilled = new boolean[tReg.length];

    // Number of available registers
    private int sAvail = 8;
    private int tAvail = 8;


    // Keep track of the maximum callee registers used
    // this is used to decide size of local stack array
    public int sVarsMax = 0;
    private int sCurr = 0;

    // Variable => Register
    public HashMap<String, String> registerMap;

    // Variable => Spill
    public HashMap<SpillPoints, String> spillMap;

    // Unused variables. These are not allocated registers but must be tracked 
    // so that they are not output duing code generation
    public ArrayList<String> unusedVars;

    // Order of increasing end time
    private LinkedList<Interval> active;

    // Simple spill point wrapper,
    // could be extended for restore point
    public class SpillPoints {
        public int spillPoint;

        public SpillPoints(int spillPoint) {
            this.spillPoint = spillPoint;
        }
    }

    public LinearScanAlloc(LiveIntervals intervals) {
        this.liveIntervals = intervals;
        this.registerMap = new HashMap<>();
        this.spillMap = new HashMap<>();
        this.unusedVars = new ArrayList<>();
        for (Interval unused : intervals.unused) {
            unusedVars.add(unused.varName);
        }
        // active is kept in order of increading end time
        this.active = new LinkedList<>();
        LinearScan();
    }

    private void LinearScan() {
        for (Interval interval : liveIntervals.intervals) {
            ExpireOldIntervals(interval);
            // T registers full. Spill a T register
            if (!interval.crossCall && tAvail == 0) {
                SpillAtInterval(interval, false);
            // S registers full. Spill an S register
            } else if (interval.crossCall && sAvail == 0){
                SpillAtInterval(interval, true);
            } else {
                // T REGISTER -- Does not need to be preserved across calls
                if (!interval.crossCall) {
                    for (int i = 0; i < tFilled.length; i++) {
                        if (tFilled[i] == false) {
                            // update available registers
                            tFilled[i] = true;
                            tAvail--;
                            // place in register map
                            registerMap.put(interval.varName, tReg[i]);
                            // place in active
                            active.add(interval);
                            // Sort active by end time
                            Collections.sort(active, Interval.getEndComparator());
                            break;
                        }
                    }
                // S REGISTER -- Preserved by caller across calls
                } else {
                    for (int i = 0; i < sFilled.length; i++) {
                        if (sFilled[i] == false) {
                            // update available registers
                            sFilled[i] = true;
                            sAvail--;
                            // Keep track of maximum callee-saved registers in use
                            sCurr++;
                            if (sCurr > sVarsMax) {
                                sVarsMax = sCurr;
                            }
                            // place in register map
                            registerMap.put(interval.varName, sReg[i]);
                            // place in active
                            active.add(interval);
                            // Sort active by end time
                            Collections.sort(active, Interval.getEndComparator());
                            break;
                        }
                    }
                }
            }
        }
    }

    private void ExpireOldIntervals(Interval in) {
        if (active.size() == 0) {
            // nothing to see here
            return;
        }
        // Is it empty, is next expired?
        while(active.peek() != null && (active.peek().end < in.start)) {
            // remove interval from active
            Interval out = active.poll();
            String oldReg = registerMap.get(out.varName);
            // Expired T register
            if (!out.crossCall) {
                // Find index of oldReg
                for (int i = 0; i < tReg.length; i++) {
                    // mark register empty
                    if (oldReg.equals(tReg[i])) {
                        tAvail++;
                        tFilled[i] = false;
                        break;
                    }
                }
            // Expired S register
            } else {
                for (int i = 0; i < sReg.length; i++) {
                    // Mark register empty
                    if (oldReg.equals(sReg[i])) {
                        sAvail++;
                        sFilled[i] = false;
                        // keep track of callee register max
                        sCurr--;
                        break;
                    }
                }
            }
            
        }
    }

    private void SpillAtInterval(Interval in, boolean crossCall) {
        Interval last = null;
        for (int i = active.size()-1; i >= 0; i--) {
            // Spill appropiate type of register. true -> S, false -> T
            if (active.get(i).crossCall == crossCall) {
                last = active.get(i);
                break;
            }
        }
        // Spill out of register
        if (last.end > in.end) {
            registerMap.put(in.varName, registerMap.get(last.varName));
            spillMap.put(new SpillPoints(in.start), last.varName);
            active.remove(last);
            active.add(in);
            Collections.sort(active, Interval.getEndComparator());
        // Spill new variable
        } else {
            spillMap.put(new SpillPoints(in.start), in.varName);
        }
    }

}


