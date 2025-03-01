package mipsGen;

import java.io.IOException;

import cs132.vapor.ast.VFunction;
import cs132.vapor.ast.VaporProgram;
import cs132.util.ProblemException;
import cs132.vapor.parser.VaporParser;
import mipsGen.DataSeg;
import mipsGen.FunctionGen;
import mipsGen.Printer;
import cs132.vapor.ast.VBuiltIn.Op;

import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;

public class VM2M {

    public static void main(String[] args) throws Exception {
        try {
            VaporProgram v = parseVapor(System.in, System.err);
            Printer p = new Printer(v.functions, v.dataSegments);
            // p.printInstructions();
            //
            DataSeg dataSegs = new DataSeg(v.dataSegments);
            System.out.print(dataSegs.getDataSection());
            String header = textHead();
            System.out.println(header);

            ArrayList<String> staticStrs = new ArrayList<>();
            for (VFunction function : v.functions) {
                FunctionGen functionGen = new FunctionGen(function, staticStrs);
                String body = functionGen.getFunction();
                System.out.println(body);
            }
            String trailer = dataTrailer(staticStrs);
            System.out.println(trailer);
            
        } catch (IOException err) {
            System.out.println(err.getMessage());
            System.exit(1);
        }
    }

    private static String textHead() {
        String header = ""
            +".text\n\n"
            +"  jal Main\n"
            +"  li $v0 10\n"
            +"  syscall\n";
        return header;
    }

    private static String dataTrailer(ArrayList<String> staticStrs) {
        StringBuilder statics = new StringBuilder();
        for (int i = 0; i < staticStrs.size(); i++) {
            String s = staticStrs.get(i);
            statics.append("_str"+i+": .asciiz "+s.substring(0, s.length()-2)+"\\n\"");
            if (i < staticStrs.size()-1)
                statics.append("\n");
        }
        String trailer = ""
            +"_print:\n"
            +"  li $v0 1\n"
            +"  syscall\n"
            +"  la $a0 _newline\n"
            +"  li $v0 4\n"
            +"  syscall\n"
            +"  jr $ra\n"
            +"\n"
            +"_error:\n"
            +"  li $v0 4\n"
            +"  syscall\n"
            +"  li $v0 10\n"
            +"  syscall\n"
            +"\n"
            +"_heapAlloc:\n"
            +"  li $v0 9\n"
            +"  syscall\n"
            +"  jr $ra\n"
            +"\n"
            +".data\n"
            +".align 0\n"
            +"_newline: .asciiz \"\\n\"\n"
            +statics.toString();
        return trailer;
    }

    public static VaporProgram parseVapor(InputStream in, PrintStream err) throws IOException {
        Op[] ops = {
            Op.Add, Op.Sub, Op.MulS, Op.Eq, Op.Lt, Op.LtS,
            Op.PrintIntS, Op.HeapAllocZ, Op.Error,
        };
        boolean allowLocals = false;
        String[] registers = {
            "v0", "v1",
            "a0", "a1", "a2", "a3",
            "t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7",
            "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7",
            "t8",
        };
        boolean allowStack = true;

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
