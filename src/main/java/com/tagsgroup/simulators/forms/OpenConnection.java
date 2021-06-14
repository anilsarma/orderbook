package com.tagsgroup.simulators.forms;

import com.tagsgroup.simulators.fix.ConnectionDetails;

import javax.swing.*;
import java.awt.event.*;

public class OpenConnection extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField serverField;
    private JTextField portField;
    private JTextField userNameField;
    private JPasswordField passwordField;
    private JTextField topicInField;
    private JTextField topicOutField;
    private JTextField durableField;


    public OpenConnection() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
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

        contentPane.registerKeyboardAction(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    }


    public void setConnectionDetails(ConnectionDetails details) {
        this.details = details;
        serverField.setText(details.getServerField());
        portField.setText("" + details.getPortField());
        userNameField.setText(details.getUserNameField());
        passwordField.setText(details.getPasswordField());
        topicInField.setText(details.getTopicInField());
        topicOutField.setText(details.getTopicOutField());
        durableField.setText(details.getDurableName());
    }

    ConnectionDetails details = null;

    private void onOK() {

        // add your code here

        try {
            int port = Integer.parseInt(portField.getText());

            dispose();

            ConnectionDetails d = new ConnectionDetails(serverField.getText(), port, userNameField.getText(), new String(passwordField.getPassword()),
                    topicInField.getText(), topicOutField.getText(), durableField.getText());

            details = d;
        } catch (Exception e) {

            JOptionPane.showMessageDialog(new JFrame(), "invalid value for port, integer expected " + e.getMessage(), "Dialog", JOptionPane.ERROR_MESSAGE);

        }
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
        details = null;
    }

    public ConnectionDetails showDialog() {
        pack();
        this.setResizable(false);
        setVisible(true);
        return details;

    }

    public static void main(String[] args) {

        OpenConnection dialog = new OpenConnection();
        dialog.showDialog();
        System.exit( 0);
    }
}