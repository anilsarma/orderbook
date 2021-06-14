package com.tagsgroup.simulators.forms;

import com.tagsgroup.simulators.fix.DefaultContextMenu;
import com.tagsgroup.simulators.fix.MenuListener;
import com.tagsgroup.simulators.fix.MessageHolder;
import com.tagsgroup.simulators.fix.helpers.Utils;
import com.tagsgroup.simulators.models.FixMessagesModel;
import com.tagsgroup.simulators.models.LogMessageModel;
import com.tagsgroup.simulators.models.OrderBlotterModel;
import com.tagsgroup.simulators.renderer.FixRenderer;
import com.tagsgroup.simulators.renderer.LogMessageRenderer;
import com.tagsgroup.simulators.renderer.MessageRenderer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.Message;
import quickfix.field.OrdStatus;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.event.MouseEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrderBlotter {
    final private static Logger logger = LoggerFactory.getLogger(OrderBlotter.class);

    private JPanel blotter;
    private JTabbedPane tabbedPanel;
    private JPanel orders;
    private JTable orderTable;
    private JTable messageTable;
    private JScrollPane decodePane;
    private JTextArea decodeArea;
    private JPanel logMessagePanel;
    private JTextField statusField;
    private JCheckBox autoModeCheckBox;
    private JCheckBox fillCheckBox;
    private JCheckBox partialFill;
    private JTable tableLogMessages;


    OrderBlotterModel orderBlotterModel = new OrderBlotterModel();
    FixMessagesModel fixMsgModel = new FixMessagesModel();
    LogMessageModel logMsgModel = new LogMessageModel();
    DefaultContextMenu defaultContextMenu;
    DefaultContextMenu messageDefaultContextMenu;


    public JTextField getStatusField() {
        return statusField;
    }

    public JPanel getBlotter() {
        return blotter;
    }

    public JTable getOrderTable() {
        return orderTable;
    }

    public JTable getMessageTable() {
        return messageTable;
    }

    public JCheckBox getAutoModeCheckBox() {
        return autoModeCheckBox;
    }

    public JCheckBox getFillCheckBox() {
        return fillCheckBox;
    }

    public JCheckBox getPartialFillCheckBox() {
        return partialFill;
    }


    public JTable getLogMessageTable() {
        return tableLogMessages;
    }

    public LogMessageModel getLogMsgModel() {
        return logMsgModel;
    }

    public DefaultContextMenu getDefaultContextMenu() {
        return defaultContextMenu;
    }

    public DefaultContextMenu getMessageDefaultContextMenu() {
        return messageDefaultContextMenu;
    }

    public OrderBlotterModel getOrderBlotterModel() {
        return orderBlotterModel;
    }

    public FixMessagesModel getFixMsgModel() {
        return fixMsgModel;
    }


    public void CreateGUI() {
        getOrderTable().setModel(getOrderBlotterModel());
        getOrderTable().setDefaultRenderer(String.class, new FixRenderer(getOrderBlotterModel()));

        getMessageTable().setModel(getFixMsgModel());
        getMessageTable().setDefaultRenderer(String.class, new MessageRenderer(getFixMsgModel()));

        getLogMessageTable().setModel(getLogMsgModel());
        getLogMessageTable().setDefaultRenderer(String.class, new LogMessageRenderer(getLogMsgModel()));
        getLogMessageTable().setAutoCreateRowSorter(true);
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(getLogMessageTable().getModel());
        getLogMessageTable().setRowSorter(sorter);
        defaultContextMenu = DefaultContextMenu.addDefaultContextMenu(orderTable);
        messageDefaultContextMenu = DefaultContextMenu.addDefaultContextMenu(messageTable);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Turn off metal’s use of bold fonts
                try {
                    getMessageTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                        public void valueChanged(ListSelectionEvent event) {
                            Message msg = fixMsgModel.getMessageAt(getMessageTable().getSelectedRow());
                            String str = Utils.get().getPrintableFix(msg);
                            decodeArea.setText(str);
                            decodeArea.invalidate();
                        }
                    });


                    defaultContextMenu.addListener(new com.tagsgroup.simulators.fix.MenuListener() {
                        @Override
                        public void onRowSelected(int row, int col) {
                            OrderBlotterModel model = ((OrderBlotterModel) orderTable.getModel());
                            MessageHolder holder = model.getMessageHolder(row);
                            String status = Utils.get().getEnumValue(OrdStatus.FIELD, "" + holder.getOrdStatus().getValue());
                            defaultContextMenu.setCurrentRow(row);
                            if (status.toLowerCase().contains("pending") ||
                                    status.toLowerCase().contains("partial")
                                    || status.toLowerCase().contains("replaced")
                                    || status.toLowerCase().contains("new")) {

                                logger.info("Current Status :" + status);
                                defaultContextMenu.setEnableAck(true);
                                defaultContextMenu.setCancelAck(true);
                                defaultContextMenu.setFillMenuStatus(true);
                                defaultContextMenu.setCurrentCol(col);
                                defaultContextMenu.setRejectMenultem(true);

                                if (status.toLowerCase().contains("cancel")) {
                                    defaultContextMenu.setMenuEnableCancel(true);
                                }
                            } else if (status.toLowerCase().contains("new")) {

                                defaultContextMenu.setCancelAck(true);
                                defaultContextMenu.setEnableAck(false);
                                defaultContextMenu.setFillMenuStatus(true);
                            } else {
                                defaultContextMenu.setCancelAck(false);
                                defaultContextMenu.setEnableAck(false);
                                defaultContextMenu.setFillMenuStatus(false);

                            }
                        }
                    });

                    messageDefaultContextMenu.addListener(new MenuListener() {

                        @Override
                        public void onRowSelected(int row, int col) {
                            int clorid = -1;
                            int orgordid = -1;
                            int ordstatus = -1;
                            TableModel model = getMessageTable().getModel();
                            for (int i = 0; i < model.getColumnCount(); i++) {
                                if (model.getColumnName(i).toLowerCase().equalsIgnoreCase("“clordid")) {
                                    clorid = i;
                                } else if (model.getColumnName(i).toLowerCase().equalsIgnoreCase("“origclordid")) {
                                    orgordid = i;
                                } else if (model.getColumnName(i).toLowerCase().equalsIgnoreCase("“ordstatus")) {
                                    ordstatus = i;
                                }
                            }
                            String status = "" + model.getValueAt(row, ordstatus);
                            Matcher m = Pattern.compile("\\((\\d+)\\)").matcher(status);
                            if (m.find()) {
                                status = Utils.get().getEnumValue(OrdStatus.FIELD, m.group(1));
                            }

                            messageDefaultContextMenu.getRejectMenuItem().setText("Generic Ack");
                            messageDefaultContextMenu.getFillMenuItem().setVisible(true);
                            messageDefaultContextMenu.setCurrentRow(row);
                            if (status.toLowerCase().contains("pending") ||
                                    status.toLowerCase().contains("partial")
                                    || status.toLowerCase().contains("replaced")
                                    || status.toLowerCase().contains("new")) {
                                logger.info("Current Status :" + status);
                                messageDefaultContextMenu.setEnableAck(true);
                                messageDefaultContextMenu.setCancelAck(true);
                                messageDefaultContextMenu.setFillMenuStatus(true);
                                messageDefaultContextMenu.setCurrentCol(col);
                                messageDefaultContextMenu.setRejectMenultem(true);

                                if (status.toLowerCase().contains("cancel")) {
                                    messageDefaultContextMenu.setMenuEnableCancel(true);
                                }

                            } else if (status.toLowerCase().contains("new")) {
                                messageDefaultContextMenu.setCancelAck(true);
                                messageDefaultContextMenu.setEnableAck(false);
                                messageDefaultContextMenu.setFillMenuStatus(true);
                            } else {
                                messageDefaultContextMenu.setEnableAck(false);
                                messageDefaultContextMenu.setCancelAck(false);
                                messageDefaultContextMenu.setFillMenuStatus(false);
                                messageDefaultContextMenu.setRejectMenultem(true);

                                // messageDefaultContextMenu. getFillMenuItem().setVisible(false) ;
                            }
                        }
                    });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }


    public JPanel getPanel() {
        return blotter;
    }

    public String getMessageTableField(String fieldName, int row) {
        TableModel model = getMessageTable().getModel();
        for (int i = 0; i < model.getColumnCount(); i++) {
            if (model.getColumnName(i).toLowerCase().equalsIgnoreCase(fieldName)) {
                return model.getValueAt(row, i).toString();
            }
        }

        return null;

    }


    public void clear() {
        getOrderBlotterModel().clear();
        getFixMsgModel().clear();
        getLogMsgModel().clear();

        getOrderBlotterModel().fireTableDataChanged();
        getFixMsgModel().fireTableDataChanged();
        getLogMsgModel().fireTableDataChanged();

    }

    private void createUIComponents() {
        messageTable = new JTable() {

            @Override
            public String getToolTipText(@NotNull MouseEvent event) {
                String tip = null;
                java.awt.Point p = event.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);

                try {
                    tip = getValueAt(rowIndex, colIndex).toString();
                    StringBuilder str = new StringBuilder();
                    str.append("<html>");
                    str.append(("" + tip).replaceAll("\n", "“<br>"));
                    str.append("</html>");
                    tip = str.toString();

                } catch (RuntimeException e1) {
                    //catch null pointer exception if mouse is over an empty line
                }

                return tip;
            };
        };
    }
}

