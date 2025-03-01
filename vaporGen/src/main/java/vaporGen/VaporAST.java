package vaporGen;

import java.util.ArrayList;


public class VaporAST {
    private static int nesting = 2;
    private String vaporStr;
    private ArrayList<VaporAST> blocks;
    private ArrayList<VaporAST> children;

    private boolean isPartial = false;
    private boolean errorCheckToggle = false;

    private String idenKey;

    private vaporType vType;

    private enum vaporType{
        STATEMENT,
        PARTIAL
    }

    public static VaporAST newVaporStatement(String str) {
        VaporAST vapor = new VaporAST(str);
        vapor.vType = vaporType.STATEMENT;
        return vapor;
    }
     
    public static VaporAST newVaporPartial(String str) {
        VaporAST vapor = new VaporAST(str);
        vapor.vType = vaporType.PARTIAL;
        vapor.isPartial = true;
        return vapor;
    }

    private VaporAST(String str) {
        this.vaporStr = str;
        this.blocks = new ArrayList<>();
        this.children = new ArrayList<>();
    }

    private VaporAST() {
        this("");
    }

    public void addBlock(VaporAST block) {
        if (block != null) {
            blocks.add(block);
        }
    }

    public void addChild(VaporAST child) {
        if (child != null) {
            children.add(child);
        }
    }
    
    public void addChild(String vStr) {
        if (vStr != null) {
            children.add(newVaporStatement(vStr));
        }
    }

    public void setChecked() {
        errorCheckToggle = true;
    }

    public boolean wasChecked() {
        if (errorCheckToggle) {
            errorCheckToggle = false;
            return true;
        }
        return false;
    }
    
    public boolean isPartial() {
        return isPartial;
    }

    public String getIdentifier() {
        return idenKey;
    }

    public void addIdenKey(String iden) {
        this.idenKey = iden;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        switch (vType) {
            case STATEMENT:
                if (vaporStr != "") {
                    sb.append(vaporStr+"\n");
                }
                // ordering for output
                if (blocks.size() > 0) {
                    for (VaporAST block : blocks) {
                        String blockStr = block.toString();
                        if (blockStr.startsWith("const") || blockStr.startsWith("var")) {
                            sb.append(blockStr+"\n");
                        }
                    }
                    for (VaporAST block : blocks) {
                        String blockStr = block.toString();
                        if (blockStr.startsWith("func")) {
                            sb.append(blockStr+"\n");
                        }
                    }
                }
                for (VaporAST child : children) {
                    for (int i = 0; i < nesting; i++) {
                        sb.append(" ");
                    }
                    // sb.append("\t");
                    sb.append(child.toString());
                }
                return sb.toString();
            case PARTIAL:
                sb.append(vaporStr);
                return sb.toString();
        }
        return "";
    }

}
