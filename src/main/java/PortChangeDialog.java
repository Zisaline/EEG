import com.fazecast.jSerialComm.SerialPort;

import javax.swing.*;

public class PortChangeDialog extends JDialog {
    private JButton cancelButton;
    private JButton okButton;
    private JList<SerialPort> portList;
    private JButton refreshButton;
    private JPanel rootPanel;
    private boolean isOk = false;
    private SerialPort port;

    public PortChangeDialog() {
        setContentPane(rootPanel);
        setSize(500, 200);
        setModal(true);
        setLocationRelativeTo(null);
        cancelButton.addActionListener(e -> onCancel());
        displayAvailablePorts();
        refreshButton.addActionListener(e -> onRefresh());
        okButton.addActionListener(e -> onOk());
    }

    private void onCancel() {
        setVisible(false);
    }

    private void onRefresh() {
        displayAvailablePorts();
    }

    private void onOk() {
        isOk = true;
        port = portList.getSelectedValue();
        setVisible(false);
        dispose();
    }

    private void displayAvailablePorts() {
        SerialPort[] serialPorts = SerialPort.getCommPorts();
        portList.setListData(serialPorts);
        if (serialPorts.length > 0) {
            portList.setSelectedIndex(0);
        }
    }

    public boolean isOk() {
        return isOk;
    }

    public SerialPort getPort() {
        return port;
    }
}
