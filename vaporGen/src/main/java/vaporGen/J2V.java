package vaporGen;

import syntaxtree.*;
import symbolTable.*;
import parser.*;

public class J2V {

    public static void main(String[] args) {
        Node root = null;
        try {
            root = new MiniJavaParser(System.in).Goal();

            SymTableVis<Integer> pv =
                new SymTableVis<Integer>();
            root.accept(pv, 0);
            SymbolTable symt = pv.symTable;
            VaporVis vap = new VaporVis();
            VaporAST vaporRep = root.accept(vap, symt);
            System.out.print(vaporRep.toString());
        } catch (ParseException err) {
            System.out.println(err.getMessage());
            System.exit(1);
        }
    }

}
