package com.tagsgroup.simulators.models;

import com.tagsgroup.simulators.Main;
import com.tagsgroup.simulators.fix.MessageHolder;

import com.tagsgroup.simulators.fix.fix.ColumnDetail;
import com.tagsgroup.simulators.fix.fix.MessagePrinter;
import com.tagsgroup.simulators.fix.helpers.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.DataDictionary;
import quickfix.DefaultMessageFactory;
import quickfix.MessageFactory;
import quickfix.field.TransactTime;

import javax.swing.table.AbstractTableModel;

import java.io.InputStream;
import java.util.HashMap;

public class OrderBlotterModel extends AbstractTableModel {
    private static Logger logger = LoggerFactory.getLogger(OrderBlotterModel.class);

    DataDictionary dataDict;
    static MessagePrinter printer = new MessagePrinter();
    static MessageFactory messageFactory = new DefaultMessageFactory();
    HashMap<String, MessageHolder> data = new HashMap<>();

    String keys[] = {};

    ColumnDetail columns[] = {
            new ColumnDetail<String, MessageHolder>("Date", String.class, (obj) -> Utils.get().getDateField(TransactTime.FIELD, obj.getNewOrderSingle())),
            new ColumnDetail<String, MessageHolder>("Time", String.class, (obj) -> Utils.get().getTimeField(TransactTime.FIELD, obj.getNewOrderSingle())),
            new ColumnDetail<String, MessageHolder>("Local Time", String.class, (obj) -> Utils.get().getLocalTimeField(TransactTime.FIELD, obj.getNewOrderSingle())),

            new ColumnDetail<String, MessageHolder>("User", String.class, (obj) -> obj.getKeyClOrdId()),
            new ColumnDetail<String, MessageHolder>("OrdStatus", String.class, (obj) -> printer.getValueName(dataDict, obj.getOrdStatus().getTag(), "" + obj.getOrdStatus().getValue())),
            new ColumnDetail<String, MessageHolder>("ClOrdId", String.class, (obj) -> obj.getKeyClOrdId()),
            new ColumnDetail<String, MessageHolder>("OrderId", String.class, (obj) -> obj.getOrderID()),
            new ColumnDetail<String, MessageHolder>("Side", String.class, (obj) -> printer.getValueName(dataDict, obj.getSide().getTag(), "" + obj.getSide().getValue())),
            new ColumnDetail<String, MessageHolder>("Price", Double.class, (obj) -> obj.getPrice()),

            new ColumnDetail<String, MessageHolder>("Qty", Double.class, (obj) -> obj.getQty()),
            new ColumnDetail<String, MessageHolder>("Open Qty", Double.class, (obj) -> 0.0),
            new ColumnDetail<String, MessageHolder>("Exec Qty", Double.class, (obj) -> obj.getCumQty()),
            new ColumnDetail<String, MessageHolder>("Symbol", String.class, (obj) -> obj.getSymbol().getValue()),

            new ColumnDetail<String, MessageHolder>("TIF", String.class, (obj) -> printer.getValueName(dataDict, obj.getTimeInForce().getTag(), "" + obj.getTimeInForce().getValue())),
            new ColumnDetail<String, MessageHolder>("Company", String.class, (obj) -> obj.getSenderCompID().getValue()),
            new ColumnDetail<String, MessageHolder>("Tranact Time", String.class, (obj) -> obj.getTransactTime().getValue().toString()),
            new ColumnDetail<String, MessageHolder>("129", String.class, (obj) -> obj.getDeliverToSubID()),
            new ColumnDetail<String, MessageHolder>("18", String.class, (obj) -> obj.getExecInst().getValue()),
            new ColumnDetail<String, MessageHolder>("OrdType", String.class, (obj) -> obj.getOrdType().getValue()),
    };

    String value = null;
    public ColumnDetail[] getColumns() {
        return columns;

    }

    public OrderBlotterModel() {
        try {
            InputStream is = this.getClass().getClassLoader().getResource("FIX44.xml").openStream();
            dataDict = new DataDictionary(is);
            //dataDict = new DataDictionary("FIX44.xml") ;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getRowCount() {
        return data.size();

    }

    public int getColumnCount() {
        return columns.length;
    }

    public String getColumnName(int columnIndex) {
        return columns[columnIndex].getName();
    }

    public Class<?> getColumnClass(int columnIndex) {
        return columns[columnIndex].getType();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if(value!=null) {
            return value;
        }
        MessageHolder entry = data.get(keys[rowIndex]);
        return columns[columnIndex].getValue(entry);
    }

    public MessageHolder getMessageHolder(int rowIndex) {
        MessageHolder entry = data.get(keys[rowIndex]);
        return entry;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        ///
    }

    public void updateData(HashMap<String, MessageHolder> messages) {
        keys = messages.entrySet().stream().sorted( (o1, o2) ->
            o1.getValue().getTransactTime().getValue().compareTo(o2.getValue().getTransactTime().getValue())
        ).map(x->x.getKey()).toArray(String[]::new);
        data = messages;
        fireTableDataChanged();
    }


    public void clear() { data.clear(); }
}