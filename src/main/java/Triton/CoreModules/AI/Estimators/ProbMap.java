package Triton.CoreModules.AI.Estimators;

public interface ProbMap {

    /* get PDF(probability density function) value of this prob map at point (x,y)*/
    double getProb(double x, double y);

    void display();
}
