package mipsGen;


import cs132.vapor.ast.VDataSegment;
import cs132.vapor.ast.VOperand.Static;

public class DataSeg {

    private VDataSegment[] dataSegments;

    public DataSeg(VDataSegment[] dataSegments) {
        this.dataSegments = dataSegments;
    }


    public String getDataSection() {
        StringBuilder data = new StringBuilder();
        data.append(".data\n\n");
        for (VDataSegment dataSegment : dataSegments) {
            data.append(dataSegment.ident+":\n");
            for (Static value : dataSegment.values) {
                data.append("  "+value.toString().substring(1)+"\n");
            }
            data.append("\n");
        }
        return data.toString();
    }
}
