package com.tagsgroup.simulators.fix;

import com.tagsgroup.simulators.fix.fix.MessagePrinter;
import com.tagsgroup.simulators.fix.helpers.OrderUtil;
import quickfix.DataDictionary;
import quickfix.Message;
import quickfix.StringField;
import quickfix.field.*;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class MessageHolder {
    private static MessagePrinter printer = new MessagePrinter();
    public NewOrderSingle nos;
    public OrderCancelReplaceRequest pending;
    public OrderCancelRequest pendingcancel;
    DataDictionary dataDictionary;
    String keyClOrdId;
    String clordId;
    String origClOrdID = "";
    String orderID;
    OrdStatus ordStatus = new OrdStatus(OrdStatus.PENDING_NEW);
    OrdStatus parentOrdStatus = null;
    OrdType ordType = new OrdType(OrdType.LIMIT);
    TimeInForce timeInForce = new TimeInForce(TimeInForce.IMMEDIATE_OR_CANCEL);
    TransactTime transactTime = new TransactTime(LocalDateTime.now());
    Side side = new Side(Side.BUY);
    Symbol symbol;
    double cumQty =0;
    double qty =0;

//double avgPrice = @;

    double price = Double.NaN;

// double execQty = 0;

    double totalNotional = 0;

    double lastQty = 8; // not recovered.
    double canceledQty = 0;

    ArrayList<Message> messages = new ArrayList<>();

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public MessageHolder(DataDictionary dataDictionary, NewOrderSingle nos) {
        messages.add(nos);
        this.dataDictionary = dataDictionary;
        this.orderID = Long.toHexString(System.nanoTime());
        this.nos = nos;

        try {
            this.ordStatus.setValue(OrdStatus.PENDING_NEW);

            qty = nos.getOrderQty().getValue();
            keyClOrdId = nos.getString(ClOrdID.FIELD);
            clordId = nos.getString(ClOrdID.FIELD);
            if (nos.isSetPrice()) {

                price = nos.getPrice().getValue();
            }

            if (nos.isSetOrdType()) {
                ordType = nos.getOrdType();
            }
            if (nos.isSetTimeInForce()) {
                timeInForce = nos.getTimeInForce();
            }
            if (nos.isSetTransactTime()) {
                transactTime = nos.getTransactTime();
            }
            if (nos.isSetSide()) {
                side = nos.getSide();
            }

            if (nos.isSetSymbol()) {
                symbol = nos.getSymbol();
            }
        } catch (Exception e) {

        }
    }

    public boolean isFilled() {
        if (getCumQty() >= getQty()) {
            return true;
        }
        return false;
    }

    public void recover(Message msg) {
        try {
            String msgType = msg.getHeader().getString(35);
            if (msgType.equals("D")) {
                quickfix.fix44.NewOrderSingle nos = (NewOrderSingle) msg;
            } else if (msgType.equals("G")) {
                quickfix.fix44.OrderCancelReplaceRequest replace = (OrderCancelReplaceRequest) msg;
                if (cumQty == 0) {
                    ordStatus = new OrdStatus(OrdStatus.PENDING_REPLACE);
                }

                clordId = replace.getClOrdID().getValue();
                origClOrdID = replace.getOrigClOrdID().getValue();
                pending = replace;

            } else if (msgType.equals("8")) {
                ExecutionReport executionReport = (ExecutionReport) msg;
                try {
                    ordStatus = executionReport.getOrdStatus();
                } catch (Exception e) {
                    e.printStackTrace();
                    ordStatus = executionReport.getOrdStatus();
                }
                cumQty = executionReport.getCumQty().getValue();
            } else if (msgType.equals("F")) {
                quickfix.fix44.OrderCancelRequest cancel = (OrderCancelRequest) msg;
                checkParentUpdate();
                ordStatus = new OrdStatus(OrdStatus.PENDING_CANCEL);
                clordId = cancel.getClOrdID().getValue();
                origClOrdID = cancel.getOrigClOrdID().getValue();
                pendingcancel = cancel;

            }
            // need to han
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            messages.add(msg);
        }
    }

    public void checkParentUpdate() {
        if (parentOrdStatus == null && OrderUtil.isOut(ordStatus)) {
            if (parentOrdStatus == null) {
                parentOrdStatus = new OrdStatus(ordStatus.getValue());
            }
        }
    }

    public void add(quickfix.Message msg) {
        messages.add(msg);
    }

    public TransactTime getTransactTime() {
        return transactTime;
    }

    public void setTransactTime(TransactTime transactTime) {
        this.transactTime = transactTime;
    }

    public String getDeliverToSubID() {
        try {
            return nos.getHeader().getField(new StringField(129)).getValue();
        } catch (Exception e) {
        }
        return "";
    }


    public ExecInst getExecInst() {
        try {
            return nos.getExecInst();
        } catch (Exception e) {
            return new ExecInst("");
        }

    }

    public OrdStatus getOrdStatus() {
        if (parentOrdStatus != null) {
            return parentOrdStatus;
        }
        return ordStatus;

    }

    public void setOrdStatus(char ordStatus) {
        this.ordStatus.setValue(ordStatus) ;
    }

    public void setParentOrdStatus(char ordStatus) {
        this.parentOrdStatus = new OrdStatus(ordStatus);
    }

    public void update(OrderCancelReplaceRequest order) throws Exception {
        messages.add(order);
        origClOrdID = order.getOrigClOrdID().getValue();
        clordId = order.getClOrdID().getValue();

        qty = order.getOrderQty().getValue();
// check the qum outside.
        try {
            this.ordStatus.setValue(OrdStatus.PENDING_REPLACE);
            if (order.isSetPrice()) {
                price = order.getPrice().getValue();
            }

            if (order.isSetOrdType()) {
                ordType = order.getOrdType();
            }
            if (order.isSetTimeInForce()) {
                timeInForce = order.getTimeInForce();
            }
        } catch (Exception e) {

        }
        pending = order;
    }

    public void update(OrderCancelRequest order) throws Exception {
        messages.add(order);
        origClOrdID = order.getOrigClOrdID().getValue();
        clordId = order.getClOrdID().getValue();

        qty = order.getOrderQty().getValue();

// check the qum outside.
        this.ordStatus.setValue(OrdStatus.PENDING_CANCEL);
        pendingcancel = order;

    }

    public NewOrderSingle getNewOrderSingle() {
        return nos;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }


    public double getLeaves() {
        return getQty() - getCumQty() - getCanceledQty();
    }

    public double getCanceledQty() {
        return canceledQty;
    }

    public void setCanceledQty(double canceledQty) {
        this.canceledQty = canceledQty;
    }

    public double getCumQty() {
        return cumQty;
    }

    public void setCumQty(double cumQty) {
        this.cumQty = cumQty;
    }

    public double getQty() {
        return qty;
    }

    public void setQty(double qty) {
        this.qty = qty;
    }

    public double getTotalNotional() {
        return totalNotional;
    }

    public void setTotalNotional(double totalNotional) {
        this.totalNotional = totalNotional;
    }

    public double getLastQty() {
        return lastQty;
    }

    public void setLastQty(double lastQty) {
        this.lastQty = lastQty;
    }

    public double getAvgPrice() {
        if(getCumQty()==0) {
            return 0;
        }
        return getTotalNotional()/getCumQty();
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getKeyClOrdId() {
        return keyClOrdId;
    }

    public void setKeyClOrdId(String keyClOrdId) {
        this.keyClOrdId = keyClOrdId;
    }

    public String getClordId() {
        return clordId;
    }

    public void setClordId(String clordId) {
        this.clordId = clordId;
    }

    public String getOrigClOrdID() {
        return origClOrdID;
    }

    public void setOrigClOrdID(String origClOrdID) {
        this.origClOrdID = origClOrdID;
    }

    public OrdType getOrdType() {
        return ordType;
    }

    public void setOrdType(OrdType ordType) {
        this.ordType = ordType;
    }

    public TimeInForce getTimeInForce() {
        return timeInForce;
    }

    public void setTimeInForce(TimeInForce timeInForce) {
        this.timeInForce = timeInForce;
    }

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }


    public StringField getSenderCompID() {
        try {
            return getNewOrderSingle().getHeader().getField(new StringField(SenderCompID.FIELD));
        } catch (Exception e) {
            return new StringField(SenderCompID.FIELD, "");
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder. append(printer.getValueName(dataDictionary, ordStatus.getTag(),  "" + ordStatus.getValue()));
                builder.append(" Cl0rdId:" + getKeyClOrdId());
        if (!getOrigClOrdID().isEmpty()) {
            builder.append(" OrigCl0rdId:" + getOrigClOrdID());
        }

        builder.append(" Qty:" + getQty());
        builder.append(" Price:" + getPrice());

        builder.append(" OrdType:" + printer.getValueName(dataDictionary, ordType.getTag(), "" + ordType.getValue()));
        builder.append(" TIF:" + printer.getValueName(dataDictionary, timeInForce.getTag(),
                "“" + timeInForce.getValue()));
// builder.append(" TimeInForce:" + printer.getValueName(dataDictionary, timeInForce.getTag(), "" +
// timeInForce.getValue()));

        builder.append(" CumQty:" + getCumQty());
        builder.append(" Leaves:" + getLeaves());





        builder.append(" AvgPrice:" + getTotalNotional()/getCumQty());
        return builder.toString();
    }

}