package Triton.Geometry;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import Triton.Shape.Line2D;

public class Field {
    public int fieldLength;
    public int fieldWidth;
    public int goalWidth;
    public int goalDepth;
    public int boundaryWidth;

    public List<String> lineNameList = new ArrayList<String>();
    public HashMap<String, Line2D> lineSegments = new HashMap<String, Line2D>();

    public float centerCircleRadius;

    public boolean isEmpty() {
        return lineSegments.isEmpty();
    }

    @Override
    public String toString() {
        String s = "";

        s += "[[Field Configuration]]=============================\n"; 
        
        s += "Field(length, width) = (" + fieldLength + ", " 
                                        + fieldWidth + ")\n";
        s += "Goal(depth, width) = (" + goalDepth + ", "
                                      + goalWidth + ")\n";
        s += "Boundary Width = " + boundaryWidth + "\n";

        s += "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n";

        
        for(String name : lineNameList) {
            Line2D line = lineSegments.get(name);
            s += line.getName() + ": " + line + " Thickness: " + line.getThickness() + "\n";
        }

        s += "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& \n";

        return s;
    }
}