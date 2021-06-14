package com.tagsgroup.simulators;

import com.tagsgroup.simulators.fix.ConnectionDetails;
import com.tagsgroup.simulators.fix.ContextManager;
import com.tagsgroup.simulators.fix.conn.TibMsgTopicConsumer;
import com.tagsgroup.simulators.fix.conn.TibjmsMsgTopicProducer;
import com.tagsgroup.simulators.fix.helpers.ConfigManager;
import com.tagsgroup.simulators.forms.CreateSimulator;
import com.tagsgroup.simulators.forms.OpenConnection;
import com.tagsgroup.simulators.forms.OrderBlotter;
import com.tagsgroup.simulators.forms.SimulatorTab;
import com.tagsgroup.simulators.models.Caller;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

public class Main {

    public static void main(String[] args) throws Exception {
        final ContextManager context = new ContextManager();
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        SimulatorTab main = new SimulatorTab();
        main.createGUI(context);

        main.getCreateSimulator().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CreateSimulator dialog = new CreateSimulator();
                String venue = dialog.showDialog();
                if (venue != null) {
                    try {
                        main.createNewSimulator(context, venue);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(new JFrame(), "“Error while create Simulator" + ex.getMessage(), "Dialog", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        main.getClearBlotter().addActionListener(e -> {
            SimulatorMain sim = context.getContext();
            sim.clearFixMessagesChroincalMap();
        });

        main.getClearAllBlotter().addActionListener(e -> {
            for (SimulatorMain s : main.getSimulators().values()) {
                try {
                    s.clearFixMessagesChroincalMap();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });


        main.getOpenConnection().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OpenConnection dialog = new OpenConnection();
                ConnectionDetails connectionDetails = null; // get current context.
                SimulatorMain sim = context.getContext();
                dialog.setConnectionDetails(sim.getConnectionDetails());
                ConnectionDetails details = dialog.showDialog();
                if (details != null) {
                    if (!ConfigManager.get().getConnectionDetails(sim.getVenue()).equals(details)) {
                        Properties properties = ConfigManager.get().getProperty(sim.getVenue());
                        details.marshall(properties); //
                        try {
                            ConfigManager.get().saveProperties(sim.getVenue(), properties);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(new JFrame(),
                                    "Failed to save properties file " + ConfigManager.get().getFileName(sim.getVenue()),
                                    "Dialog",
                                    JOptionPane.ERROR_MESSAGE);

                        }

                    } else {
                        // no change
                        System.out.println("no change");
                    }
                    if (sim.getTibMsgTopicConsumer() != null) {
                        sim.getTibMsgTopicConsumer().stop();

                    }
                    if (sim.getTibjmsMsgTopicProducer() != null) {
                        sim.getTibjmsMsgTopicProducer().stop();

                    }

                    String server = details.getServerField() + ":" + details.getPortField();
                    try {

                        sim.setTibMsgTopicConsumer(new TibMsgTopicConsumer(server, details.getUserNameField(), details.getPasswordField()));
                        sim.setTibjmsMsgTopicProducer(new TibjmsMsgTopicProducer(server, details.getUserNameField(), details.getPasswordField()));

                        String userName = System.getProperty("user.name");

                        sim.getTibMsgTopicConsumer().addTopicListener(userName + ".1", details.getTopicInField(), sim.getTibMsgConsumerCallback());

                        sim.getTibMsgTopicConsumer().start();

                        StringBuilder msg = new StringBuilder();

                        msg.append("Connected to server " + details.getUserNameField() + "@" + server);

                        JOptionPane.showMessageDialog(new JFrame(), msg.toString(), "Dialog", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {

                        JOptionPane.showMessageDialog(new JFrame(), "“Failed to connect to " + server + " error:" + ex.getMessage(), "Dialog", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }
}
