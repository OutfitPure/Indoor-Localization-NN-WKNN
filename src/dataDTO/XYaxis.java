package dataDTO;

public class XYaxis {
    private double x;
    private double y;
    //权值
    private double omega;

    public void setX(double x) {
        this.x = x;
    }

    public double getX() {
        return x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getY() {
        return y;
    }

    public void setOmega(double omega) {
        this.omega = omega;
    }

    public double getOmega() {
        return omega;
    }

    @Override
    public String toString() {
        return "XYaxis{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
