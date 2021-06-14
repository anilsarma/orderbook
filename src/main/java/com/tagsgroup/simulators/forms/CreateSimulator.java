package com.tagsgroup.simulators.forms;

import javax.swing.*;
import java.awt.event.*;

public class CreateSimulator extends  JDialog{
    private JPanel content;
    private JLabel title;
    private JTextField textField1;
    private JButton OK;
    private JButton cancel;

    String selectedVenue = null;


    public CreateSimulator() {
        setContentPane(content);
        setModal(true);
        getRootPane().setDefaultButton(OK);

        OK.addActionListener( (e)-> onOK() );
        cancel.addActionListener( (e)-> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });


        content.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
// add your code here
        try {
            dispose();

            selectedVenue = textField1.getText();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(new JFrame(), "invalid value, " + e.getMessage(), "Dialog", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onCancel() {
        dispose();
    }


    public String showDialog() {
        selectedVenue =null;
        pack();
        this.setResizable(false);
        setVisible(true);
        return selectedVenue;
    }

    public static void main(String[] args) {

        CreateSimulator dialog = new CreateSimulator();
        dialog.showDialog();
        System.exit( 0);
    }
}

