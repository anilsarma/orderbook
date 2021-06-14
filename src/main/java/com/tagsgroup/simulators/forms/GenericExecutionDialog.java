package com.tagsgroup.simulators.forms;

import com.tagsgroup.simulators.models.ExecTypeModel;
import com.tagsgroup.simulators.models.OrdStatusModel;
import quickfix.field.OrdStatus;

import javax.swing.*;
import java.awt.event.*;
import java.lang.reflect.Field;


public class GenericExecutionDialog extends  JDialog {
    private JPanel contentPanel;
    private JTextField orderQty;
    private JTextField cldOrdID;
    private JTextField orderPrice;
    private JTextField origClientOrdId;
    private JComboBox<String> ordStatus;
    private JTextField leavesQty;
    private JButton cancelButton;
    private JButton OKButton;
    private JLabel ExecutionTitle;
    private JTextField lastShares;
    private JComboBox execType;
    private JTextField avgPrice;

    private boolean status = true;



    private OrdStatusModel orderStatusModel = new OrdStatusModel();
    private ExecTypeModel execTypeModel = new ExecTypeModel();

//

    int dataOrderQty = -1;
    double dataOrderPrice = -1;
    int dataLeaves = -1;

    public GenericExecutionDialog() {
        this.setTitle("Execution Details");

        setContentPane(contentPanel);
        setModal(true);
        getRootPane().setDefaultButton(OKButton);
        ordStatus.setModel(orderStatusModel);
        execType.setModel(execTypeModel);
        OKButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
                status = true;
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
                status = false;
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPanel.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }

        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
// add your code here
        try {
            dataOrderPrice = Double.parseDouble(orderPrice.getText());
            dataOrderQty = (int) Double.parseDouble(orderQty.getText());

            dataLeaves = (int) Double.parseDouble(leavesQty.getText());

            dispose();

            status = true;
        } catch (Exception e) {

            JOptionPane.showMessageDialog(new JFrame(), "invalid value, " + e.getMessage(), "Dialog", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
        status = false;
    }

    public void setOrderDetails(String clOrdId, String origClOrdId, double price, double qty) {
        this.cldOrdID.setText(clOrdId);
        this.origClientOrdId.setText(origClOrdId == null ? "" : origClOrdId);
        this.orderPrice.setText("" + price);
        this.orderQty.setText("" + (int) qty);

        this.ordStatus.setSelectedIndex(0);
        this.avgPrice.setText("" + price);
        this.leavesQty.setText("" + 0);
        this.execType.setSelectedIndex(0);
        this.lastShares.setText("" + 0);
    }

    public boolean showDialog() {
        pack();
        this.setLocationRelativeTo(null);
        setVisible(true);
        return status;
//return details;

    }

    public double getDataOrderQty() {
        return (double) dataOrderQty;
    }

    public void setDataOrderQty(int dataOrderQty) {
        this.dataOrderQty = dataOrderQty;
    }

    public void setDataOrderPrice(double dataOrderPrice) {
        this.dataOrderPrice = dataOrderPrice;
    }

    public double getDataOrderPrice() {
        return dataOrderPrice;
    }

    public char getOrdStatus() {


        String field = orderStatusModel.getElementAt(ordStatus.getSelectedIndex());
        try {

            Field rfield = OrdStatus.class.getDeclaredField(field);

            return rfield.getChar(null);
        } catch (Exception e) {

        }
        return ' ';

    }


    public String getCldOrdID() {
        return cldOrdID.getText();
    }

    public String getOrigClientOrdId() {
        return origClientOrdId.getText();
    }

    public int getDataLeaves() {
        return dataLeaves;
    }

    public int getOrderQty() {
        return Integer.parseInt(orderQty.getText());
    }
    public double  getOrderPrice() {
        return Double.parseDouble(orderPrice.getText());
    }
    public double  getAvgPrice() {
        return Double.parseDouble(avgPrice.getText());
    }
    public int getLeavesQty() {
        return Integer.parseInt(leavesQty.getText());
    }

    public int getLastShares() {
        return Integer.parseInt(lastShares.getText());
    }
    public char getExecType() {
        return ' ';
    }

}
