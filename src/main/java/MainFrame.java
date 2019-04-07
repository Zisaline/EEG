import com.fasterxml.jackson.databind.ObjectMapper;
import com.fazecast.jSerialComm.SerialPort;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;

public class MainFrame extends JFrame {

    private final int nMoments = 1000;
    private final String eegSeriesName = "EEG";
    private final XChartPanel<XYChart> xChartPanel;
    private final ArrayList<Double> xData;
    private final ArrayList<Double> yData;
    private JPanel buttonPanel;
    private JButton startButton;
    private JButton changePortButton;
    private JPanel chartPanel;
    private JPanel rootPanel;
    private final XYChart chart;
    private final double timeStep = ((float) 1 / 500);
    private final int limit = 1000;
    private boolean isPlotting = false;
    private SerialPort port;
    private Thread plottingThread;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-DD--HH-mm-ss");
    private final ObjectMapper mapper = new ObjectMapper();
    private JCheckBox mockDataCheckBox;
    private JButton recordButton;
    private JButton stopRecordButton;
    private double time = 0;
    private boolean isRecording = false;
    private Record record;

    public MainFrame() {
        setContentPane(rootPanel);
        setSize(800, 400);
        setLocationRelativeTo(null);
        xData = new ArrayList<>();
        yData = new ArrayList<>();
        xData.add((double) 0);
        yData.add((double) 0);
        chart = QuickChart.getChart("EEG", "time, sec", "EEG, uV", "EEG", xData, yData);
        xChartPanel = new XChartPanel<>(chart);
        chartPanel.add(xChartPanel);
        chartPanel.validate();

        startButton.addActionListener(e -> onStart());
        changePortButton.addActionListener(e -> changePort());
        recordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onRecord();
            }
        });
        stopRecordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onStopRecord();
            }
        });
    }

    private void onStopRecord() {
        isRecording = false;
        recordButton.setEnabled(true);
        stopRecordButton.setVisible(false);
        try {
            mapper.writeValue(new File(record.getName() + '-' + dateFormat.format(new Date())), record);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void onRecord() {
        if (!isPlotting) {
            JOptionPane.showMessageDialog(this, "Can't record while not plotting");
            return;
        }
        recordButton.setEnabled(false);
        stopRecordButton.setVisible(true);
        String nameOfRecord = JOptionPane.showInputDialog("Type name of record");
        record = new Record(nameOfRecord);
        isRecording = true;
        new Thread(this::updateRecordTimer).start();
    }

    private void updateRecordTimer() {
        LocalDateTime date = LocalDateTime.now();
        while (isRecording) {
            Duration duration = Duration.between(date, LocalDateTime.now());
            String s = String.format("%d:%02d%n", duration.toMinutes(), duration.minusMinutes(duration.toMinutes()).getSeconds());
            recordButton.setText("Recording (" + s + ")");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        recordButton.setText("Record");
    }

    private void onStart() {
        if (!isPlotting) {
            if (!mockDataCheckBox.isSelected() && port == null) {
                JOptionPane.showMessageDialog(this, "Port is null");
                return;
            }
            isPlotting = true;
            startButton.setText("Stop");
            mockDataCheckBox.setEnabled(false);
            plottingThread = new Thread(() -> {
                try {
                    if (mockDataCheckBox.isSelected()) {
                        plotMockData();
                    } else {
                        plotPortData();
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            });
            plottingThread.start();
        } else {
            if (isRecording)
                onStopRecord();
            isPlotting = false;
            startButton.setText("Start");
            mockDataCheckBox.setEnabled(true);
        }
    }

    private void plotMockData() {
        while (isPlotting) {
            double value = (Math.random() * 200 - 100);
            plot(value);
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void changePort() {
        if (mockDataCheckBox.isSelected()) {
            JOptionPane.showMessageDialog(this, "Can't change port while mocking is enabled");
            return;
        }
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

    private void plotPortData() throws IOException {
        if (!port.openPort()) {
            return;
        }
        InputStream stream = port.getInputStream();
        while (isPlotting) {
            while (stream.available() > 0) {
                double newData = stream.read();
                plot(newData);
            }
        }
        port.closePort();
    }

    private void plot(double newData) {
        SwingUtilities.invokeLater(() -> {
            time += timeStep;
            xData.add(time);
            yData.add(newData);
            if (xData.size() > limit) {
                xData.remove(0);
                yData.remove(0);
            }
            if (record != null && isRecording) {
                record.addData(time, newData);
            }
            chart.updateXYSeries("EEG", xData, yData, null);
            xChartPanel.repaint();
        });
    }
}
