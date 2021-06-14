package com.tagsgroup.simulators.models;

import com.tagsgroup.simulators.fix.MessageHolder;
import com.tagsgroup.simulators.fix.fix.ColumnDetail;
import com.tagsgroup.simulators.fix.fix.MessagePrinter;
import com.tagsgroup.simulators.fix.helpers.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.DataDictionary;
import quickfix.DefaultMessageFactory;
import quickfix.Message;
import quickfix.MessageFactory;
import quickfix.field.*;

import javax.swing.table.AbstractTableModel;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class FixMessagesModel extends AbstractTableModel {
    private static Logger logger = LoggerFactory.getLogger(FixMessagesModel.class);
    DataDictionary dataDict;
    ArrayList<Message> data = new ArrayList<>();



    ColumnDetail columns[] = {
            new ColumnDetail<String, Message>("Date", String.class, (obj) -> Utils.get().getDateField(TransactTime.FIELD, obj)),
            new ColumnDetail<String, Message>("Time", String.class, (obj) -> Utils.get().getTimeField(TransactTime.FIELD, obj)),
            new ColumnDetail<String, Message>("Local Time", String.class, (obj) ->Utils.get().getLocalTimeField(TransactTime.FIELD, obj)),

            new ColumnDetail<String, Message>("MsgType", String.class, (obj) -> Utils.get().getEnumValue(MsgType.FIELD, obj.getHeader())),
            new ColumnDetail<String, Message>("ClOrdId", String.class,(obj) ->Utils.get().getField(ClOrdID.FIELD, obj)),
            new ColumnDetail<String, Message>("OrigClOrdId", String.class, (obj) ->Utils.get().getField(OrigClOrdID.FIELD, obj)),
            new ColumnDetail<String, Message>("TIF", String.class, (obj) -> Utils.get().getEnumValue(TimeInForce.FIELD, obj)),
            new ColumnDetail<String, Message>("Price", Double.class, (obj) -> Utils.get().getDoubleField(Price.FIELD, obj)),
            new ColumnDetail<String, Message>("Qty", Double.class, (obj) -> Utils.get().getDoubleField(OrderQty.FIELD, obj)),
            new ColumnDetail<String, Message>("LastShares", Double.class, (obj) -> Utils.get().getDoubleField(LastShares.FIELD, obj)),
            new ColumnDetail<String, Message>("CumQty", Double.class, (obj) -> Utils.get().getDoubleField(CumQty.FIELD, obj)),

            new ColumnDetail<String, Message>("OrdStatus", String.class, (obj) -> Utils.get().getEnumValue(OrdStatus.FIELD, obj)),
            new ColumnDetail<String, Message>("ExecType", String.class, (obj) -> Utils.get().getEnumValue(ExecType.FIELD, obj)),
            new ColumnDetail<String, Message>("Text", String.class, (obj) -> Utils.get().getEnumValue(Text.FIELD, obj)),
            new ColumnDetail<String, Message>("FIX", String.class, (obj) -> Utils.get().getFix(obj))

    };

    String value = null;
    public ColumnDetail[] getColumns() {
        return columns;

    }

    public FixMessagesModel() {
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
        Message entry = data.get(rowIndex);
        return columns[columnIndex].getValue(entry);
    }

    public Message getMessageAt(int rowIndex) {
        Message entry = data.get(rowIndex);
        return entry;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        ///
    }

    public void updateData(ArrayList<Message> messages) {
        data = messages;
        fireTableDataChanged();
    }


    public void clear() { data.clear(); }
}