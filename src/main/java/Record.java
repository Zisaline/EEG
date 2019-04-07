import java.util.ArrayList;
import java.util.Date;

public class Record {
    private final String name;
    private final Date date = new Date();
    private ArrayList<Double> xData = new ArrayList<>();
    private ArrayList<Double> yData = new ArrayList<>();

    public Record(String name) {
        this.name = name;
    }

    public void addData(double x, double y) {
        xData.add(x);
        yData.add(y);
    }

    public String getName() {
        return name;
    }

    public Date getDate() {
        return date;
    }

    public ArrayList<Double> getxData() {
        return xData;
    }

    public ArrayList<Double> getyData() {
        return yData;
    }
}
