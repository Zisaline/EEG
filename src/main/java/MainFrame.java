import com.fazecast.jSerialComm.SerialPort;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.data.time.DynamicTimeSeriesCollection;
import org.jfree.data.time.Second;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;

public class MainFrame extends JFrame {

    private final int nMoments = 10000;
    private final String eegSeriesName = "EEG";
    private DynamicTimeSeriesCollection dataset;
    private JPanel buttonPanel;
    private JButton startButton;
    private JButton changePortButton;
    private JPanel chartPanel;
    private JPanel rootPanel;
    private boolean isPlotting = false;
    private SerialPort port;
    private Thread plottingThread;

    public MainFrame() {
        setContentPane(rootPanel);
        setSize(800, 400);
        setLocationRelativeTo(null);
        dataset = new DynamicTimeSeriesCollection(1, nMoments, new Second());
        dataset.addSeries(new float[1], 0, "Signal");
        dataset.setTimeBase(new Second());

        chartPanel.add(new ChartPanel(ChartFactory.createTimeSeriesChart("EEG",
                "time, sec", "EEG, uV", dataset)));

        startButton.addActionListener(e -> onStart());
        changePortButton.addActionListener(e -> changePort());
    }

    private void onStart() {
        if (!isPlotting) {
            if (port == null) {
                JOptionPane.showMessageDialog(this, "Port is null");
                return;
            }
            startButton.setText("Stop");
            isPlotting = true;
            plottingThread = new Thread(() -> {
                try {
                    plotData();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            });
            plottingThread.start();
        } else {
            startButton.setText("Start");
            isPlotting = false;
        }
    }

    private void changePort() {
        PortChangeDialog dialog = new PortChangeDialog();
        dialog.setVisible(true);
        if (dialog.isOk()) {
            isPlotting = false;
            startButton.setText("Start");
            if (plottingThread != null) {
                try {
                    plottingThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            port = dialog.getPort();
        }
    }

    private void plotData() throws IOException {
        if (!port.openPort()) {
            return;
        }
        InputStream stream = port.getInputStream();
        while (isPlotting) {
            while (stream.available() > 0) {
                float[] newData = {stream.read()};
                SwingUtilities.invokeLater(() -> {
                    dataset.advanceTime();
                    dataset.appendData(newData);
                });
            }
        }
        port.closePort();
    }
}
