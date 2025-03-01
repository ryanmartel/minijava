package vaporMGen;

import java.io.IOException;

import cs132.vapor.ast.VDataSegment;
import cs132.vapor.ast.VFunction;
import cs132.vapor.ast.VaporProgram;
import cs132.util.ProblemException;
import cs132.vapor.parser.VaporParser;
import cs132.vapor.ast.VBuiltIn.Op;
import cs132.vapor.ast.VOperand.Static;
import vaporMGen.LiveIntervals;
import vaporMGen.CFG;
import vaporMGen.ImplAllocations;
import vaporMGen.LinearScanAlloc;
import vaporMGen.NodeList;

import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.PrintStream;

public class V2VM {

    public static void main(String[] args) throws Exception {
        try {
            VaporProgram v = parseVapor(System.in, System.err);
            StringBuilder vaporMProgram = new StringBuilder();
            // Const segments
            for (VDataSegment dataSegment : v.dataSegments) {
                StringBuilder dataSeg = new StringBuilder();
                if (dataSegment.mutable) {
                    dataSeg.append("var ");
                } else {
                    dataSeg.append("const ");
                }
                dataSeg.append(dataSegment.ident+"\n");
                for (Static value : dataSegment.values) {
                    dataSeg.append("  "+value.toString()+"\n");
                }
                vaporMProgram.append(dataSeg.toString()+"\n");
            }
            // Function body
            for (VFunction function : v.functions) {
                // Control Flow Graph
                CFG cfgGen = new CFG(function);
                NodeList cfg = cfgGen.getCFG();
                // Live intervals
                LiveIntervals liveIntervals = new LiveIntervals(cfg, function);
                // Assign Registers
                LinearScanAlloc linearScanAlloc = new LinearScanAlloc(liveIntervals);
                // Create output
                ImplAllocations allocs = new ImplAllocations(cfg, linearScanAlloc.sVarsMax, function, linearScanAlloc);
                vaporMProgram.append(allocs.bodyStr+"\n\n");
            }
            System.out.println(vaporMProgram.toString());

        } catch (IOException err) {
            System.out.println(err.getMessage());
            System.exit(1);
        }
    }

    public static VaporProgram parseVapor(InputStream in, PrintStream err) throws IOException {
        Op[] ops = {
            Op.Add, Op.Sub, Op.MulS, Op.Eq, Op.Lt, Op.LtS,
            Op.PrintIntS, Op.HeapAllocZ, Op.Error,
        };
        boolean allowLocals = true;
        String[] registers = null;
        boolean allowStack = false;

        VaporProgram program;
        try {
            program = VaporParser.run(new InputStreamReader(in), 1, 1,
                    java.util.Arrays.asList(ops),
                    allowLocals, registers, allowStack);
        }
        catch (ProblemException ex) {
            err.println(ex.getMessage());
            return null;
        }

        return program;
    }
}
