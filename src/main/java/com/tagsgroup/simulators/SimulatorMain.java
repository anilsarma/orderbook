package com.tagsgroup.simulators;

import com.tagsgroup.simulators.fix.ConnectionDetails;
import com.tagsgroup.simulators.fix.conn.TibMsgConsumerCallback;
import com.tagsgroup.simulators.fix.conn.TibMsgTopicConsumer;
import com.tagsgroup.simulators.fix.conn.TibjmsMsgTopicProducer;
import com.tagsgroup.simulators.fix.helpers.*;
import com.tagsgroup.simulators.fix.MessageHolder;
import com.tagsgroup.simulators.forms.ExecutionDetails;
import com.tagsgroup.simulators.forms.GenericExecutionDialog;
import com.tagsgroup.simulators.forms.OrderBlotter;
import net.openhft.chronicle.map.ChronicleMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.*;
import quickfix.field.*;

import quickfix.fix44.ExecutionReport;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;


import javax.jms.TextMessage;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class SimulatorMain {

    static MessagePrinter printer = new MessagePrinter();
    static MessageFactory messageFactory = new DefaultMessageFactory();
    DataDictionary dataDict;
    private static Logger logger = LoggerFactory.getLogger(SimulatorMain.class);
    ChronicleMaps cm;
    private ChronicleMap<CharSequence, Message> map;
    HashMap<String, String> clordidmap = new HashMap<>();

//static TibMsgTopicConsumer tib;

    //static TibjmsMsgTopicProducer prod;
    TibMsgConsumerCallback callback;

    String venue = "XNYS";

    ConnectionDetails connectionDetails;

    TibMsgTopicConsumer tib;

    TibjmsMsgTopicProducer producer;

    HashMap<String, MessageHolder> messages = new HashMap<>();
    OrderBlotter blotter = new OrderBlotter();

    public SimulatorMain(String venue) throws Exception {
        if (venue != null) {
            this.venue = venue;
        }

        venue = this.venue;
        dataDict = new DataDictionary(Utils.getResource("FIX44.xml"));

        File newMessagesFile = ConfigManager.get().getMemoryMapFile(this.venue);

// cm = new ChronicleMaps(new File("./data/messages." + this.venue + ".map"));
        cm = new ChronicleMaps(newMessagesFile);

        map = cm.getMap(quickfix.Message.class);

        for (Map.Entry<CharSequence, quickfix.Message> e : map.entrySet()) {
            String msgType = e.getValue().getHeader().getString(35);
            if (msgType.equals("D")) {
                logger.info("{} got message {}", getVenue(), msgType);
                NewOrderSingle nos = (NewOrderSingle) e.getValue();
                MessageHolder holder = new MessageHolder(dataDict, nos);
                messages.put(nos.getClOrdID().getValue(), holder);

                handleNewOrderSingle(true, nos);

            }
        }

        for (int i = 0; i < map.size(); i++) {
            quickfix.Message msg = map.get("" + (i + 1));
            String msgType = msg.getHeader().getString(35);
            if (msgType.equals("D")) {
            } else {
                String clOrdId = msg.getField(new StringField(ClOrdID.FIELD)).getValue();
                String origClOrdId = "";
                if (msg.isSetField(OrigClOrdID.FIELD)) {
                    origClOrdId = msg.getField(new StringField(OrigClOrdID.FIELD)).getValue();

                }

                if (!origClOrdId.isEmpty() && !origClOrdId.equals(clOrdId)) {
                    addClOrdId(clOrdId, origClOrdId);

                }
                clOrdId = resolve(clOrdId);
                MessageHolder holder = messages.get(clOrdId);
                if (holder != null) {
                    holder.recover(msg);
                } else {
                    logger.info("{} Messages for order {} not found", getVenue(), clOrdId);

                }
            }
        }

        connectionDetails = ConfigManager.get().getConnectionDetails(venue);
        logger.info("{} close time {}", getVenue(), ((18 * 60 + 30) * 60_000_000_000L));


        try {
            producer = new TibjmsMsgTopicProducer(connectionDetails.getServerPort(), connectionDetails.getUserNameField(), connectionDetails.getPasswordField());
            tib = new TibMsgTopicConsumer(connectionDetails.getServerPort(), connectionDetails.getUserNameField(), connectionDetails.getPasswordField());
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
            JOptionPane.showMessageDialog(new JFrame(), e.getMessage());
        }

        blotter.CreateGUI();

        try {

            Properties properties = ConfigManager.get().getProperty(SimulatorMain.this.venue);
            Object value = properties.get("checkbox.auto.mode");
            if (value != null) {
                blotter.getAutoModeCheckBox().setSelected(value.toString().equalsIgnoreCase("true") ? true : false);
            }
            value = properties.get("checkbox.fill.mode");
            if (value != null) {
                blotter.getFillCheckBox().setSelected(value.toString().equalsIgnoreCase("true") ? true : false);
            }

            value = properties.get("checkbox.partial.fill.mode");
            if (value != null) {
                blotter.getPartialFillCheckBox().setSelected(value.toString().equalsIgnoreCase("true") ? true : false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        blotter.getAutoModeCheckBox().addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                Properties properties = ConfigManager.get().getProperty(SimulatorMain.this.venue);
                boolean value = (e.getStateChange() == ItemEvent.SELECTED) ? true : false;
                properties.setProperty("checkbox.auto.mode", value ? "true" : "false");
                try {
                    ConfigManager.get().saveProperties(SimulatorMain.this.venue, properties);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        blotter.getFillCheckBox().addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                Properties properties = ConfigManager.get().getProperty(SimulatorMain.this.venue);
                boolean value = (e.getStateChange() == ItemEvent.SELECTED) ? true : false;
                properties.setProperty("“checkbox.fill.mode", value ? "true" : "false");
                try {
                    ConfigManager.get().saveProperties(SimulatorMain.this.venue, properties);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        blotter.getPartialFillCheckBox().addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                Properties properties = ConfigManager.get().getProperty(SimulatorMain.this.venue);
                boolean value = (e.getStateChange() == ItemEvent.SELECTED) ? true : false;
                properties.setProperty("“checkbox.partial.fill.mode", value ? "true" : "false");
                try {
                    ConfigManager.get().saveProperties(SimulatorMain.this.venue, properties);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });

        blotter.getDefaultContextMenu().getAckMenuItem().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = blotter.getDefaultContextMenu().getCurrentRow();

                if (row > -1 && row < blotter.getOrderBlotterModel().getRowCount()) {
                    MessageHolder messageHolder = blotter.getOrderBlotterModel().getMessageHolder(row); // client order id
                    String clOrdId = messageHolder.getKeyClOrdId();
                    logger.info("{} got client order id {}", getVenue(), clOrdId);
                    try {
                        handleSendAck(clOrdId);
                        blotter.getOrderBlotterModel().fireTableDataChanged();
                        blotter.getFixMsgModel().fireTableDataChanged();
                    } catch (Exception e1) {
                        JOptionPane.showMessageDialog(new JFrame(), e1.getMessage());
                        e1.printStackTrace();
                    }
                }
            }

        });
        blotter.getDefaultContextMenu().getCancelMenuItem().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = blotter.getDefaultContextMenu().getCurrentRow();

                if (row > -1 && row < blotter.getOrderBlotterModel().getRowCount()) {
                    MessageHolder messageHolder = blotter.getOrderBlotterModel().getMessageHolder(row); // client order id
                    String clOrdId = messageHolder.getKeyClOrdId();
                    logger.info("{} got client order id {}", getVenue(), clOrdId);
                    try {
                        handleCancelOrder(clOrdId);
                        blotter.getOrderBlotterModel().fireTableDataChanged();
                        blotter.getFixMsgModel().fireTableDataChanged();
                    } catch (Exception e1) {
                        JOptionPane.showMessageDialog(new JFrame(), e1.getMessage());
                        e1.printStackTrace();

                    }

                }
            }
        });

        blotter.getDefaultContextMenu().getFillMenuItem().addActionListener((e) -> {
            int row = blotter.getDefaultContextMenu().getCurrentRow();
            if (row > -1 && row < blotter.getOrderBlotterModel().getRowCount()) {
                MessageHolder messageHolder = blotter.getOrderBlotterModel().getMessageHolder(row); // client order id
                String clOrdId = messageHolder.getKeyClOrdId();
                logger.info("{} got client order id {}", getVenue(), clOrdId);
                try {
                    handleFillOrder(clOrdId, null);
                    blotter.getOrderBlotterModel().fireTableDataChanged();
                    blotter.getFixMsgModel().fireTableDataChanged();
                } catch (Exception e1) {
                    JOptionPane.showMessageDialog(new JFrame(), e1.getMessage());
                    e1.printStackTrace();

                }
            }
        });

        blotter.getDefaultContextMenu().getAcceptCancelMenuItem().addActionListener((e) -> {
            int row = blotter.getDefaultContextMenu().getCurrentRow();
            if (row > -1 && row < blotter.getOrderBlotterModel().getRowCount()) {
                MessageHolder messageHolder = blotter.getOrderBlotterModel().getMessageHolder(row); // client order id
                String clOrdId = messageHolder.getKeyClOrdId();
                logger.info("{} got client order id {}", getVenue(), clOrdId);
                try {
                    handleAcceptCancelOrder(clOrdId);
                    blotter.getOrderBlotterModel().fireTableDataChanged();
                    blotter.getFixMsgModel().fireTableDataChanged();
                } catch (Exception e1) {
                    JOptionPane.showMessageDialog(new JFrame(), e1.getMessage());
                    e1.printStackTrace();
                }
            }
        });


        blotter.getMessageDefaultContextMenu().getFillMenuItem().addActionListener((e) -> {
            int row = blotter.getMessageDefaultContextMenu().getCurrentRow();
            if (row > -1 && row < blotter.getFixMsgModel().getRowCount()) {
                String clOrdId = blotter.getMessageTableField("clordid", row);
                logger.info("{} got client order id {}", getVenue(), clOrdId);
                try {
                    handleFillOrder(clOrdId, null);
                    blotter.getOrderBlotterModel().fireTableDataChanged();
                    blotter.getFixMsgModel().fireTableDataChanged();

                    JOptionPane.showMessageDialog(new JFrame(), "Client Order Id" + clOrdId);

                } catch (Exception e1) {
                    JOptionPane.showMessageDialog(new JFrame(), e1.getMessage());
                    e1.printStackTrace();

                }
            }
        });
        blotter.getMessageDefaultContextMenu().getRejectMenuItem().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = blotter.getMessageDefaultContextMenu().getCurrentRow();
                if (row > -1 && row < blotter.getFixMsgModel().getRowCount()) {
                    String clOrdId = blotter.getMessageTableField("clordid", row);
                    String origClOrdId = blotter.getMessageTableField("origclordid", row);
                    logger.info("{} got client order id {}", getVenue(), clOrdId);
                    try {
                        handleCustomMessage(clOrdId, origClOrdId);
                        blotter.getOrderBlotterModel().fireTableDataChanged();
                        blotter.getFixMsgModel().fireTableDataChanged();

                        JOptionPane.showMessageDialog(new JFrame(), "Client Order Id" + clOrdId);

                    } catch (Exception e1) {
                        JOptionPane.showMessageDialog(new JFrame(), e1.getMessage());
                        e1.printStackTrace();
                    }
                }
            }
        });
        SwingUtilities.invokeLater(() -> {
            blotter.getOrderBlotterModel().updateData(messages);
        });

        // default context for selecting the order in the order book.
        blotter.getDefaultContextMenu().addListener((row, col) -> {
            MessageHolder holder = blotter.getOrderBlotterModel().getMessageHolder(row);
            if (holder != null) {
                blotter.getFixMsgModel().updateData(holder.getMessages());
            }
        });

        callback = (durableid, topicName, message) -> {
            if (message instanceof TextMessage) {
                try {
                    String fix = ((TextMessage) message).getText();

                    int ck = MessageUtils.checksum(fix);
                    fix = fix.substring(0, fix.length() - 4) + String.format("%03d", ck) + "\01";
                    quickfix.Message msg = MessageUtils.parse(messageFactory, dataDict, fix);
                    String msgType = msg.getHeader().getString(35);
                    map.put("" + (map.size() + 1), msg);
                    if (msgType.equals("D")) {
                        NewOrderSingle nos = (NewOrderSingle) msg;
                        handleNewOrderSingle(false, nos);
                    } else if (msgType.equals("G")) {
                        OrderCancelReplaceRequest nos = (OrderCancelReplaceRequest) msg;
                        handleOrderCancelReplaceRequest(nos);
                    } else if (msgType.equalsIgnoreCase("F")) {
                        OrderCancelRequest cancel = (OrderCancelRequest) msg;
                        handleOrderCancelrequest(cancel);
                    } else {
                        error("un-handled F message {}", msg);
                    }

                    blotter.getOrderBlotterModel().updateData(messages);
                    blotter.getFixMsgModel().fireTableDataChanged();
                    //error("class info here:" + msg.getClass());
                    //msg = messageFactory.create("FIX.4.4", "D");
                    //printer.print(dataDict, msg);
                    //fix = fix.replace("\@1", "|");
                    //System.out.print1n(fix) ;
                    info(message.getJMSDestination() + ":\n" + fix);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        tib.addTopicListener(connectionDetails.getDurableName(), connectionDetails.getTopicInField(), callback);
        tib.start();
        timer.start();
    }

    Timer timer = new Timer(1000, (x) -> onTimeout(x));

    public void stop() {
        tib.stop();
        producer.stop();
        timer.stop();
    }

    public void onTimeout(ActionEvent e) {
        blotter.getStatusField().setText(" " + new TransactTime().getValue() + " [" + connectionDetails.toString() + "]");
    }


    public void clearFixMessagesChroincalMap() {
        map.clear();
        getBlotter().clear();
    }


    // move this elsewhere.
    String resolve(String clOrdId) {
        String orig = clordidmap.get(clOrdId);
        if(orig == null) {
            return clOrdId;
        }
        if(orig.isEmpty()) {
            return clOrdId;
        }
        if(clOrdId.equals(orig)) {
            return clOrdId;
        }
        return resolve(orig);
    }

    void addClOrdId(String clOrdId, String origClOrdId) {
        clordidmap.put(clOrdId, origClOrdId);
    }

    public OrderBlotter getBlotter() {
        return blotter;
    }

    public String getVenue() {
        return venue;
    }

    public ConnectionDetails getConnectionDetails() {
        return connectionDetails;
    }

    public TibMsgTopicConsumer getTibMsgTopicConsumer() {
        return tib;
    }

    public void setTibMsgTopicConsumer(TibMsgTopicConsumer tib) {
        this.tib = tib;
    }

    public TibjmsMsgTopicProducer getTibjmsMsgTopicProducer() {
        return producer;
    }

    public void setTibjmsMsgTopicProducer(TibjmsMsgTopicProducer producer) {
        this.producer = producer;
    }

    public TibMsgConsumerCallback getTibMsgConsumerCallback() {
        return callback;
    }

    protected javax.jms.Message sendMessage(Object obj) throws Exception {
        return producer.sendTopicMessage(connectionDetails.getTopicOutField(), obj.toString());
    }


    protected void handleNewOrderSingle(boolean recovery, NewOrderSingle msg) throws Exception {
        info(msg.toRawString());
        info("NewOrderSingle ClientOrderID:" + msg.getClOrdID().getValue());
        //printer.print(dataDict, msg);
        MessageHolder holder = new MessageHolder(dataDict, msg);
        messages.put(msg.getClOrdID().getValue(), holder);

        if (!recovery && blotter.getAutoModeCheckBox().isSelected()) {
            String clOrdId = holder.getKeyClOrdId();
            logger.info("{} got client order id {}", getVenue(), clOrdId);
            try {
                handleSendAck(clOrdId);
                blotter.getOrderBlotterModel().fireTableDataChanged();
                blotter.getFixMsgModel().fireTableDataChanged();

                ExecutionDetails details = new ExecutionDetails();
                details.setOrderDetails(holder.getClordId(), holder.getOrigClOrdID(), holder.getPrice(), holder.getQty());
                details.setDataOrderQty((int) (holder.getQty() - holder.getCumQty()));
                details.setDataOrderPrice(holder.getPrice());

                if (holder.getOrdType().equals(OrdType.MARKET)) {
                    details.setDataOrderPrice(Utils.getPrice(holder.getSymbol().getValue()));
                }

                logger.info("Selected {} ", details.getOrdStatus());

                if (blotter.getPartialFillCheckBox().isSelected()) {
                    // a percentage
                    double random = Math.min(1, Math.random()); // cannot be greater than 1.
                    int qty = (int) (details.getDataOrderQty() * random);
                    if (qty < details.getDataOrderQty()) {
                        qty += 100;
                        qty = (int) (qty / 100) * 100;
                        if (qty >= details.getDataOrderQty()) {
                            qty = 0;
                        }

                    }
                    if (qty > 0) {
                        if (qty < details.getDataOrderQty()) {
                            details.setFixOrdStatus(new OrdStatus(OrdStatus.PARTIALLY_FILLED));
                        }
                        details.setDataOrderQty(qty);
                    }
                }

                final Timer timer = new Timer(10, (e) -> {
                    try {
                        if (blotter.getFillCheckBox().isSelected()) {
                            handleFillOrder(clOrdId, details);
                            if (details.getOrdStatus() == OrdStatus.PARTIALLY_FILLED) {
                                handleCancelOrder(clOrdId);
                            }

                        } else {
                            handleCancelOrder(clOrdId);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
                timer.setRepeats(false);
                timer.start();

            } catch (Exception e1) {
                JOptionPane.showMessageDialog(new JFrame(), e1.getMessage());
                e1.printStackTrace();
            }
        }
    }

    protected void handleOrderCancelReplaceRequest(OrderCancelReplaceRequest msg) throws Exception {
        error(msg.toRawString());
        info("OrderCancelReplaceRequest ClientOrderID: {}, OrigCl0OrdID:", msg.getClOrdID().getValue(), msg.getOrigClOrdID().getValue());

        String origClOrdId = msg.getOrigClOrdID().getValue();
        String clOrdId = msg.getClOrdID().getValue();

        String resolvedClOrdId = resolve(origClOrdId);
        MessageHolder holder = messages.get(resolvedClOrdId);

        if (holder == null) {
            error("OrderCancelReplaceRequest orig order not found, ClientOrderID: {}, OrigClOrdID:",
                    msg.getClOrdID().getValue(), msg.getOrigClOrdID().getValue());
            return;
        }

        if (!origClOrdId.isEmpty() && !origClOrdId.equals(clOrdId)) {
            addClOrdId(clOrdId, origClOrdId);
        }
        holder.update(msg);
    }


    protected void handleOrderCancelrequest(OrderCancelRequest msg) throws Exception {
        error(msg.toRawString());
        info("OrderCancelRequest ClientOrderID: {}, OrigClOrdID:", msg.getClOrdID().getValue(),
                msg.getOrigClOrdID().getValue());

        String origClOrdId = msg.getOrigClOrdID().getValue();
        String clOrdId = msg.getClOrdID().getValue();
        String resolvedClOrdId = resolve(origClOrdId);
        MessageHolder holder = messages.get(resolvedClOrdId);
        if (holder == null) {
            error("OrderCancelReplaceRequest orig order not found, ClientOrderID: {}, OrigClOrdID:",
                    msg.getClOrdID().getValue(), msg.getOrigClOrdID().getValue());
            return;
        }
        if (!origClOrdId.isEmpty() && !origClOrdId.equals(clOrdId)) {
            addClOrdId(clOrdId, origClOrdId);
        }
        if (OrderUtil.isOut(holder.getOrdStatus())) {
            error("Order Too Late to cancel “" + msg.getClOrdID().getValue());
        }
        holder.checkParentUpdate();
        holder.update(msg);
    }

    protected void handleSendAck(String clordId) throws Exception {
        MessageHolder holder = messages.get(clordId);
        if (holder == null) {
            String msg = String.format("Message not found for ClOrdId: %s ", clordId);
            JOptionPane.showMessageDialog(new JFrame(), msg, "Error", JOptionPane.ERROR_MESSAGE);
            logger.error("{} ", getVenue(), msg);
            return;
        }
        //ExecutionReport(OrderID orderID, ExecID execID, ExecType execType, OrdStatus ordStatus, Side side,
        // LeavesQty leavesQty, CumQty cumQty, AvgPx avgPx)

        char ordStatusc = holder.getOrdStatus().getValue();
        if (ordStatusc != OrdStatus.PENDING_NEW && ordStatusc != OrdStatus.PENDING_REPLACE) {
            String msg = Utils.format("{} Cannot Ack order {}, incorrect state currently {}",
                    new Object[]{getVenue(),
                            clordId,
                            Utils.get().getEnumValue(holder.getOrdStatus().getTag(), "" + holder.getOrdStatus().getValue())});
            error(msg);
            JOptionPane.showMessageDialog(new JFrame(), msg, "“Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        ExecType execType = new ExecType(ExecType.NEW);
        OrdStatus ordStatus = new OrdStatus(OrdStatus.NEW);
        if (!holder.getOrigClOrdID().isEmpty()) {
            execType = new ExecType(ExecType.REPLACED);
            ordStatus = new OrdStatus(OrdStatus.REPLACED);
        }
        ExecutionReport exec = new ExecutionReport(new OrderID(holder.getOrderID()),
                new ExecID(Long.toHexString(System.nanoTime())), execType, ordStatus,
                new Side(holder.nos.getSide().getValue()), new LeavesQty(holder.getLeaves()),
                new CumQty(holder.getCumQty()), new AvgPx(holder.getAvgPrice()));

        exec.getHeader().setString(SenderCompID.FIELD, holder.nos.getHeader().getString(TargetCompID.FIELD));
        exec.getHeader().setString(TargetCompID.FIELD, holder.nos.getHeader().getString(SenderCompID.FIELD));

        if (exec.getHeader().isSetField(TargetSubID.FIELD)) {
            exec.getHeader().setString(SenderSubID.FIELD, holder.nos.getHeader().getString(TargetSubID.FIELD));
        }
        if (exec.getHeader().isSetField(SenderSubID.FIELD)) {
            exec.getHeader().setString(TargetSubID.FIELD, holder.nos.getHeader().getString(SenderSubID.FIELD));
        }

        exec.setString(Symbol.FIELD, holder.nos.getString(Symbol.FIELD));
        exec.setString(ClOrdID.FIELD, holder.getClordId());
        exec.set(execType);
        exec.set(ordStatus);
        if (!holder.getOrigClOrdID().isEmpty()) {
            exec.setString(OrigClOrdID.FIELD, holder.getOrigClOrdID());
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            StringField time = new StringField(TransactTime.FIELD);

            String fmt = dateFormat.format(new Date());
            time.setValue(fmt);

            exec.setField(TransactTime.FIELD, time);
        } catch (Exception e) {
            e.printStackTrace();
        }

        info("reply:" + exec.toString());
        sendMessage(exec);
        map.put("" + (map.size() + 1), exec);
        quickfix.Message msg = MessageUtils.parse(messageFactory, dataDict, exec.toString());
        holder.add(msg);
        if (holder.getOrdStatus().getValue() == OrdStatus.PENDING_NEW) {
            holder.setOrdStatus(OrdStatus.NEW);
        } else {
            holder.setOrdStatus(OrdStatus.REPLACED);
        }
    }

    protected void handleAcceptCancelOrder(String clordId) throws Exception {

        MessageHolder holder = messages.get(clordId);

        if (holder == null) {
            String msg = Utils.format("{} Message not found for ClOrdId: {} ", getVenue(), clordId);
            JOptionPane.showMessageDialog(new JFrame(), msg, "Error", JOptionPane.ERROR_MESSAGE);
            error(msg);
            return;

        }

        //ExecutionReport(OrderID orderID, ExecID execID, ExecType execType, OrdStatus ordStatus, Side side,
        // LeavesQty leavesQty, CumQty cumQty, AvgPx avgPx)

        char ordStatusc = holder.getOrdStatus().getValue();
        if (ordStatusc != OrdStatus.PENDING_CANCEL) {
            String msg = Utils.format("{} Cannot Ack Cancel order {}, incorrect state currently {}",
                    getVenue(),
                    clordId,
                    Utils.get().getEnumValue(holder.getOrdStatus().getTag(), "" + holder.getOrdStatus().getValue()));
            error(msg);
            JOptionPane.showMessageDialog(new JFrame(), msg, "“Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        ExecType execType = new ExecType(ExecType.CANCELED);
        OrdStatus ordStatus = new OrdStatus(OrdStatus.CANCELED);

        ExecutionReport exec = new ExecutionReport(new OrderID(holder.getOrderID()),
                new ExecID(Long.toHexString(System.nanoTime())), execType, ordStatus,
                new Side(holder.nos.getSide().getValue()), new LeavesQty(0),new CumQty(holder.getCumQty()),
                new AvgPx(holder.getAvgPrice()));

        exec.getHeader().setString(SenderCompID.FIELD, holder.nos.getHeader().getString(TargetCompID.FIELD));
        exec.getHeader().setString(TargetCompID.FIELD, holder.nos.getHeader().getString(SenderCompID.FIELD));

        if (exec.getHeader().isSetField(TargetSubID.FIELD)) {
            exec.getHeader().setString(SenderSubID.FIELD, holder.nos.getHeader().getString(TargetSubID.FIELD));
        }
        if (exec.getHeader().isSetField(SenderSubID.FIELD)) {
            exec.getHeader().setString(TargetSubID.FIELD, holder.nos.getHeader().getString(SenderSubID.FIELD));
        }

        exec.setString(Symbol.FIELD, holder.nos.getString(Symbol.FIELD));
        exec.setString(ClOrdID.FIELD, holder.getClordId());
        exec.set(execType);
        exec.set(ordStatus);
        if (!holder.getOrigClOrdID().isEmpty()) {
            exec.setString(OrigClOrdID.FIELD, holder.getOrigClOrdID());
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("“yyyyMMdd-HH:mm:ss.SSS");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            StringField time = new StringField(TransactTime.FIELD);

            String fmt = dateFormat.format(new Date());
            time.setValue(fmt);

            exec.setField(TransactTime.FIELD, time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        info("reply:" + exec.toString());

        sendMessage(exec);

        map.put("" + (map.size() + 1), exec);

        quickfix.Message msg = MessageUtils.parse(messageFactory, dataDict, exec.toString());
        holder.add(msg);

        holder.setOrdStatus(OrdStatus.CANCELED);

        //printer.print(dataDict, exec);
    }

    private void handleCancelOrder(String clordId) throws Exception {

        MessageHolder holder = messages.get(clordId);

        if (holder == null) {
            String msg = String.format("{} Message not found for ClOrdId: {}", getVenue(), clordId);
            JOptionPane.showMessageDialog(new JFrame(), msg, "Error", JOptionPane.ERROR_MESSAGE);
            error(msg);
            return;
        }

        if (holder.getLeaves() == 0) {
            JOptionPane.showMessageDialog(new JFrame(),"Nothing to cancel LeavesQty = 0", "“Error", JOptionPane.ERROR_MESSAGE);
            return;

        }

        //ExecutionReport(OrderID orderID, ExecID execID, ExecType execType, OrdStatus ordStatus, Side side,

        // LeavesQty leavesQty, CumQty cumQty, AvgPx avgPx)

        char ordStatusc = holder.getOrdStatus().getValue();
        if (ordStatusc != OrdStatus.PENDING_NEW && ordStatusc != OrdStatus.PENDING_REPLACE && ordStatusc != OrdStatus.NEW && ordStatusc != OrdStatus.REPLACED && ordStatusc != OrdStatus.PARTIALLY_FILLED) {
            String msg = String.format("{} Cannot Cancel order {}, incorrect state currently {}", getVenue(), clordId, Utils.get().getEnumValue(holder.getOrdStatus().getTag(), "" + holder.getOrdStatus().getValue()));
            error(msg);
            JOptionPane.showMessageDialog(new JFrame(), msg, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        ExecType execType = new ExecType(ExecType.CANCELED);
        OrdStatus ordStatus = new OrdStatus(OrdStatus.CANCELED);
        if (!holder.getOrigClOrdID().isEmpty()) {
            execType = new ExecType(ExecType.CANCELED);
            ordStatus = new OrdStatus(OrdStatus.CANCELED);
        }
        holder.setCanceledQty(holder.getLeaves());
        ExecutionReport exec = new ExecutionReport(new OrderID(holder.getOrderID()),
                new ExecID(Long.toHexString(System.nanoTime())), execType, ordStatus,
                new Side(holder.nos.getSide().getValue()), new LeavesQty(holder.getLeaves()),
                new CumQty(holder.getCumQty()), new AvgPx(holder.getAvgPrice()));

        exec.getHeader().setString(SenderCompID.FIELD, holder.nos.getHeader().getString(TargetCompID.FIELD));
        exec.getHeader().setString(TargetCompID.FIELD, holder.nos.getHeader().getString(SenderCompID.FIELD));

        if (exec.getHeader().isSetField(TargetSubID.FIELD)) {
            exec.getHeader().setString(SenderSubID.FIELD, holder.nos.getHeader().getString(TargetSubID.FIELD));
        }
        if (exec.getHeader().isSetField(SenderSubID.FIELD)) {
            exec.getHeader().setString(TargetSubID.FIELD, holder.nos.getHeader().getString(SenderSubID.FIELD));
        }

        exec.setString(Symbol.FIELD, holder.nos.getString(Symbol.FIELD));
        exec.setString(ClOrdID.FIELD, holder.getClordId());
        exec.set(execType);
        exec.set(ordStatus);
        if (!holder.getOrigClOrdID().isEmpty()) {
            exec.setString(OrigClOrdID.FIELD, holder.getOrigClOrdID());
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            StringField time = new StringField(TransactTime.FIELD);

            String fmt = dateFormat.format(new Date());
            time.setValue(fmt);

            exec.setField(TransactTime.FIELD, time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        info("reply:" + exec.toString());
        sendMessage(exec);
        map.put("" + (map.size() + 1), exec);
        quickfix.Message msg = MessageUtils.parse(messageFactory, dataDict, exec.toString());
        holder.add(msg);
        if (holder.getOrdStatus().getValue() == OrdStatus.PENDING_NEW) {
            holder.setOrdStatus(OrdStatus.CANCELED);
        } else {
            holder.setOrdStatus(OrdStatus.CANCELED);
        }
    }

    private void handleFillOrder(String clordId, ExecutionDetails dialog) throws Exception {

        MessageHolder holder = messages.get(clordId);

        if (holder == null) {
            String msg = Utils.format("{} Message not found for ClOrdId: {} ", getVenue(), clordId);
            JOptionPane.showMessageDialog(new JFrame(), msg,"Error",JOptionPane.ERROR_MESSAGE);
            error(msg);
            return;
        }

        if (dialog == null) {
            dialog = new ExecutionDetails();
// dialog.setLocation();
            dialog.setOrderDetails(holder.getClordId(), holder.getOrigClOrdID(), holder.getPrice(), holder.getQty());
            if (!dialog.showDialog()) {
                return;
            }
        }

        logger.info("Selected {} ", dialog.getOrdStatus());

        char ordStatusChar = dialog.getOrdStatus();
        clordId = dialog.getCldOrdID();
        String origClOrdId = dialog.getOrigClientOrdId();

        double fillqty = dialog.getDataOrderQty();
        double fillPrice = dialog.getDataOrderPrice();
        double leaves = holder.getLeaves();
        if (dialog.getDataLeaves() > -1) {
            leaves = dialog.getDataLeaves();
        }
        if (fillqty > leaves) {
            JOptionPane.showMessageDialog(new JFrame(), "“invalid qty, cannot be more than order leaves" + leaves, "Dialog", JOptionPane.ERROR_MESSAGE);
        }

        holder.setTotalNotional(fillPrice * fillqty);

        holder.setCumQty(holder.getCumQty() + fillqty);
        holder.setLastQty(fillqty);

        //ExecutionReport(OrderID orderID, ExecID execID, ExecType execType, OrdStatus ordStatus, Side side,
        // LeavesQty leavesQty, CumQty cumQty, AvgPx avgPx)

        char ordStatusc = holder.getOrdStatus().getValue();
        if (ordStatusc != OrdStatus.PENDING_CANCEL && ordStatusc != OrdStatus.PENDING_NEW && ordStatusc != OrdStatus.PENDING_REPLACE
                && ordStatusc != OrdStatus.PARTIALLY_FILLED && ordStatusc != OrdStatus.REPLACED && ordStatusc != OrdStatus.NEW) {
            String msg = Utils.format("{} Cannot Fill order {}, incorrect state currently {}", getVenue(),
                    clordId,
                    Utils.get().getEnumValue(holder.getOrdStatus().getTag(), "" + holder.getOrdStatus().getValue()));
            error(msg);
            JOptionPane.showMessageDialog(new JFrame(), msg, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        ExecType execType = new ExecType(holder.isFilled() ? ExecType.TRADE : ExecType.TRADE);
        OrdStatus ordStatus = new OrdStatus(holder.isFilled() ? OrdStatus.FILLED : OrdStatus.PARTIALLY_FILLED);
        if (ordStatusChar != ' ') {
            ordStatus = new OrdStatus(ordStatusChar);
        }

        if (!holder.getOrigClOrdID().isEmpty()) {
            // execType = new ExecType(ExecType. TRADE) ;
            //ordStatus = new OrdStatus(OrdStatus.FILLED) ;
        }

        ExecutionReport exec = new ExecutionReport(new OrderID(holder.getOrderID()),
                new ExecID(Long.toHexString(System.nanoTime())), execType, ordStatus,
                new Side(holder.nos.getSide().getValue()), new LeavesQty(leaves),
                new CumQty(holder.getCumQty()), new AvgPx(holder.getAvgPrice()));

        //exec.setField(FillQty.FIELD, holder.
        //exec.setField(CumQty.FIELD, holder.nos.getOrderQty());
        exec.setDouble(OrderQty.FIELD, holder.getQty());
        //exec.setDouble(Price.FIELD, holder.getPrice());
        exec.setDouble(LastPx.FIELD, holder.getPrice());

        exec.setDouble(AvgPx.FIELD, holder.getAvgPrice());

        exec.set(new LastQty(fillqty));
        exec.setDouble(CumQty.FIELD, holder.getCumQty());
        exec.setDouble(LeavesQty.FIELD, leaves);

        exec.getHeader().setString(SenderCompID.FIELD, holder.nos.getHeader().getString(TargetCompID.FIELD));
        exec.getHeader().setString(TargetCompID.FIELD, holder.nos.getHeader().getString(SenderCompID.FIELD));

        if (exec.getHeader().isSetField(TargetSubID.FIELD)) {
            exec.getHeader().setString(SenderSubID.FIELD, holder.nos.getHeader().getString(TargetSubID.FIELD));
        }

        if (exec.getHeader().isSetField(SenderSubID.FIELD)) {
            exec.getHeader().setString(TargetSubID.FIELD, holder.nos.getHeader().getString(SenderSubID.FIELD));
        }

        //exec.setChar(ExecTransType.FIELD, ExecTransType.NEW) ;

        exec.setString(Symbol.FIELD, holder.nos.getString(Symbol.FIELD));
        exec.setString(ClOrdID.FIELD, dialog.getCldOrdID());
        //exec.set(execType) ;

        //exec.set(ordStatus) ;

        //exec.setField(FillQty.FIELD, holder.
        //exec.setField(CumQty.FIELD, holder.nos.getOrderQty());

        if (!origClOrdId.isEmpty()) {
            exec.setString(OrigClOrdID.FIELD, origClOrdId);
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            StringField time = new StringField(TransactTime.FIELD);

            String fmt = dateFormat.format(new Date());
            time.setValue(fmt);

            exec.setField(TransactTime.FIELD, time);
        } catch (Exception e) {

            e.printStackTrace();
        }

        info("reply:" + exec.toString());
        new Thread() {
            @Override
            public void run() {
                super.run();

                try {
                    Thread.sleep(10000);
                    sendMessage(exec);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

        map.put("" + (map.size() + 1), exec);

        quickfix.Message msg = MessageUtils.parse(messageFactory, dataDict, exec.toString());
        holder.add(msg);

        holder.setOrdStatus(ordStatus.getValue());
        blotter.getLogMsgModel().fireTableDataChanged();
    }

    private void handleCustomMessage(String clordId, String origClOrdId) throws Exception {
        MessageHolder holder = messages.get(clordId);
        if (holder == null) {
            holder = messages.get(origClOrdId);
        }

        if (holder == null) {
            String msg = Utils.format("{} Message not found for ClOrdId: {} ", getVenue(), clordId);
            JOptionPane.showMessageDialog(new JFrame(), msg,"Error", JOptionPane.ERROR_MESSAGE);
            error(msg);
            return;

        }

        GenericExecutionDialog dialog = new GenericExecutionDialog();
        dialog.setOrderDetails(holder.getClordId(), holder.getOrigClOrdID(), holder.getPrice(), holder.getQty());

        if (!dialog.showDialog()) {
            return;
        }

        logger.info("Selected {} ", dialog.getOrdStatus());

        char ordStatusChar = dialog.getOrdStatus();
        clordId = dialog.getCldOrdID();
        origClOrdId = dialog.getOrigClientOrdId();

        double fillqty = dialog.getDataOrderQty();
//double fillPrice = dialog.getDataOrderPrice();

        if (fillqty > holder.getLeaves()) {
            JOptionPane.showMessageDialog(new JFrame(),
                    "“Warning, invalid qty, cannot be more than order leaves" + holder.getLeaves(), "Dialog", JOptionPane.ERROR_MESSAGE);
        }
        char ordStatusc = holder.getOrdStatus().getValue();
        if (ordStatusc != OrdStatus.PENDING_CANCEL && ordStatusc != OrdStatus.PENDING_NEW && ordStatusc != OrdStatus.PENDING_REPLACE
                && ordStatusc != OrdStatus.PARTIALLY_FILLED && ordStatusc != OrdStatus.REPLACED && ordStatusc != OrdStatus.NEW) {
            String msg = Utils.format("{} Cannot Fill order {}, incorrect state currently {}", getVenue(),
                    clordId,
                    Utils.get().getEnumValue(holder.getOrdStatus().getTag(), "" + holder.getOrdStatus().getValue()));
            error(msg);
            JOptionPane.showMessageDialog(new JFrame(), msg, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        ExecType execType = new ExecType(dialog.getExecType());
        OrdStatus ordStatus = new OrdStatus(dialog.getOrdStatus());
        if (ordStatusChar != ' ') {
            ordStatus = new OrdStatus(ordStatusChar);
        }

        ExecutionReport exec = new ExecutionReport(new OrderID(holder.getOrderID()),
                new ExecID(Long.toHexString(System.nanoTime())), execType, ordStatus,
                new Side(holder.nos.getSide().getValue()), new LeavesQty(dialog.getLeavesQty()),
                new CumQty(dialog.getOrderQty()), new AvgPx(dialog.getAvgPrice()));

        exec.setDouble(OrderQty.FIELD, dialog.getOrderQty());
        exec.setDouble(LastPx.FIELD, dialog.getOrderPrice());
        exec.setDouble(AvgPx.FIELD, dialog.getAvgPrice());

        exec.set(new LastQty(fillqty));
        exec.setDouble(CumQty.FIELD, dialog.getOrderQty());
        exec.setDouble(LeavesQty.FIELD, dialog.getLeavesQty());
        exec.setDouble(LastShares.FIELD, dialog.getLastShares());


        exec.getHeader().setString(SenderCompID.FIELD, holder.nos.getHeader().getString(TargetCompID.FIELD));
        exec.getHeader().setString(TargetCompID.FIELD, holder.nos.getHeader().getString(SenderCompID.FIELD));

        if (exec.getHeader().isSetField(TargetSubID.FIELD)) {
            exec.getHeader().setString(SenderSubID.FIELD, holder.nos.getHeader().getString(TargetSubID.FIELD));
        }

        if (exec.getHeader().isSetField(SenderSubID.FIELD)) {
            exec.getHeader().setString(TargetSubID.FIELD, holder.nos.getHeader().getString(SenderSubID.FIELD));
        }

//exec.setChar(ExecTransType.FIELD, ExecTransType.NEW) ;
        exec.setString(Symbol.FIELD, holder.nos.getString(Symbol.FIELD));
        exec.setString(ClOrdID.FIELD, dialog.getCldOrdID());

        if (!origClOrdId.isEmpty()) {
            exec.setString(OrigClOrdID.FIELD, origClOrdId);
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            StringField time = new StringField(TransactTime.FIELD);

            String fmt = dateFormat.format(new Date());
            time.setValue(fmt);

            exec.setField(TransactTime.FIELD, time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        info("reply:" + exec.toString());
        new Thread() {
            @Override
            public void run() {
                super.run();

                try {
                    Thread.sleep(10000);
                    sendMessage(exec);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }.start();

        map.put("" + (map.size() + 1), exec);

        quickfix.Message msg = MessageUtils.parse(messageFactory, dataDict, exec.toString());
        holder.add(msg);

        holder.setOrdStatus(ordStatus.getValue());
    }

    public void error(String str, Object... args) {

        Utils.ignore_exception(() -> blotter.getLogMsgModel().updateData("ERROR", Utils.format(str, args)));
        blotter.getLogMsgModel().fireTableDataChanged();
        logger.error(str, args);
    }

    public void info(String str, Object... args) {

        Utils.ignore_exception(() -> blotter.getLogMsgModel().updateData("INFO", Utils.format(str, args)));
        blotter.getLogMsgModel().fireTableDataChanged();
        logger.info(str, args);
    }

}
