package com.tagsgroup.simulators.forms;

import com.tagsgroup.simulators.SimulatorMain;
import com.tagsgroup.simulators.fix.ContextManager;
import com.tagsgroup.simulators.fix.helpers.ConfigManager;
import com.tagsgroup.simulators.fix.helpers.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class SimulatorTab {
    private static Logger logger = LoggerFactory.getLogger(SimulatorTab.class);
    private JTabbedPane simulatorTab;
    private JFrame frame;
    private Map<String, SimulatorMain> simulators = new HashMap<>();
    private Map<Object, String> panelToVenue = new HashMap<>();

    JMenuBar menuBar;
    JMenuItem openConnection;
    JMenuItem clearBlotter;
    JMenuItem clearAllBlotter;
    JMenuItem closeConnection;
    JMenuItem createSimulator;

    public JMenuItem getOpenConnection() {
        return openConnection;
    }

    public JMenuItem getClearBlotter() {
        return clearBlotter;

    }

    public JMenuItem getClearAllBlotter() {
        return clearAllBlotter;

    }

    public Map<String, SimulatorMain> getSimulators() {
        return simulators;

    }

    public JMenuItem getCloseConnection() {
        return closeConnection;
    }

    public JMenuItem getCreateSimulator() {
        return createSimulator;
    }


    public void removeSimulator(String venue) {
        SimulatorMain simulator = simulators.get(venue);
        if (simulator != null) {
            simulators.remove(venue);
            panelToVenue.remove(simulator.getBlotter().getPanel());
            simulator.stop();
        }
    }

    public void createNewSimulator(ContextManager contextManager, String venue) throws Exception {
        if (simulators.keySet().contains(venue)) {
            // check if visible
            JOptionPane.showMessageDialog(new JFrame(), "Simulator already exists " + venue, "Dialog",
                    JOptionPane.ERROR_MESSAGE);
            return; // already exist
        }
        SimulatorMain mainTab = new SimulatorMain(venue);

        if (simulators.isEmpty()) {
            contextManager.setContext(mainTab);
        }
        simulators.put(mainTab.getVenue(), mainTab);
        ImageIcon icon = null;
        try {
            Image image = Utils.createImageIcon(mainTab.getVenue().toLowerCase() + ".png").getImage();
            image = image.getScaledInstance(18, 18, Image.SCALE_SMOOTH);
            icon = new ImageIcon(image);
        } catch (Exception e) {
            //e.printStackTrace();;
            logger.info("Image Not found {}", e.getMessage());
        }
        simulatorTab.addTab(mainTab.getVenue(), icon, mainTab.getBlotter().getPanel());
        panelToVenue.put(mainTab.getBlotter().getPanel(), mainTab.getVenue());

        simulatorTab.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (e.getSource().getClass().isAssignableFrom(JTabbedPane.class)) {
                    JTabbedPane pane = (JTabbedPane) e.getSource();
                    //System.out .print1n(pane.getName());
                    Component component = pane.getSelectedComponent();
                    //System.out.print1n("Venue:" + panelToVenue.get (component) ) ;
                    String venue = panelToVenue.get(component);
                    SimulatorMain context = null;
                    if (venue != null) {
                        context = simulators.get(venue);
                    }

                    contextManager.setContext(context);
                }
                //logger.info(e);
            }
        });
        Properties properties = ConfigManager.get().getProperty("global");
        String value = simulators.keySet().stream().collect(Collectors.joining(";"));
        properties.put("global.venues", value);
        ConfigManager.get().saveProperties("global", properties);

    }

    public void createGUI(ContextManager contextManager) throws Exception {
        frame = new JFrame("Simulator " + System.getProperty("user.name"));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try {
            frame.setIconImage(Utils.createImageIcon("exchange.png").getImage());
        } catch (Exception e) {
        }

        simulatorTab.setName("Main Tab");
        Properties properties = ConfigManager.get().getProperty("global");
        String venues[] = properties.getOrDefault("global.venues", "XNMS").toString().split("[.;,]");
        simulatorTab.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            {

                                logger.info("Selected:" + simulatorTab.getSelectedIndex());
                                JPopupMenu menu = new JPopupMenu();
                                JMenuItem closer = new JMenuItem(new AbstractAction("Close") {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        Object tab = simulatorTab.getSelectedComponent();
                                        String venue = panelToVenue.get(tab);
                                        removeSimulator(venue);
                                        simulatorTab.removeTabAt(simulatorTab.getSelectedIndex());
                                    }
                                });
                                menu.add(closer);
                                menu.show(simulatorTab, e.getX(), e.getY());
                            }
                        }
                    }
                });

        for (String venue : venues) {
            createNewSimulator(contextManager, venue);
        }

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setContentPane(simulatorTab);
        frame.setSize(200, 800);

        frame.setVisible(true);

        menuBar = new JMenuBar();

        menuBar.setBackground(Color.BLACK);

        JMenu menuFile = new JMenu("File");
        menuBar.add(menuFile);

        frame.setJMenuBar(menuBar);

        openConnection = new JMenuItem("Open Connection");
        menuFile.add(openConnection);

        clearBlotter = new JMenuItem("Clear Blotter");
        menuFile.add(clearBlotter);


        clearAllBlotter = new JMenuItem("Clear All Blotter");
        menuFile.add(clearAllBlotter);


        closeConnection = new JMenuItem("Close Connection ");
        menuFile.add(closeConnection);

        createSimulator = new JMenuItem("Create Simulator");
        menuFile.add(createSimulator);
    }
}
