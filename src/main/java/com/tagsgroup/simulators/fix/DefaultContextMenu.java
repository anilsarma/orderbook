package com.tagsgroup.simulators.fix;

import javax.swing.*;

import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;


public class DefaultContextMenu extends JPopupMenu {
    boolean enableAck = false;
    boolean cancelAck = false;
    boolean enableFill = false;
    boolean enableCancel = false;
    boolean rejectMenuFlag = false;
    int currentRow = -1;
    int currentCol = -1;
    ArrayList<MenuListener> listener = new ArrayList<>();
    private Clipboard clipboard;
    private UndoManager undoManager;
    private JMenuItem ackMenuItem;
    private JMenuItem cancelMenuItem;
    private JMenuItem fillMenuItem;
    private JMenuItem rejectMenuItem;
    private JMenuItem acceptCancelMenuItem;
    private JTable textComponent;

    public DefaultContextMenu() {
        undoManager = new UndoManager();
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        addPopupMenuItems();

    }

    public int getCurrentRow() {
        return currentRow;
    }

    public void setCurrentRow(int currentRow) {
        this.currentRow = currentRow;
    }

    public int getCurrentCol() {
        return currentCol;
    }

    public void setCurrentCol(int currentCol) {
        this.currentCol = currentCol;
    }

    public JMenuItem getAckMenuItem() {
        return ackMenuItem;
    }

    public JMenuItem getCancelMenuItem() {
        return cancelMenuItem;
    }

    public JMenuItem getFillMenuItem() {
        return fillMenuItem;
    }

    public JMenuItem getRejectMenuItem() {
        return rejectMenuItem;
    }

    public JMenuItem getAcceptCancelMenuItem() {
        return acceptCancelMenuItem;
    }

    public boolean getEnableAck() {
        return enableAck;
    }

    public void setEnableAck(boolean enableAck) {
        this.enableAck = enableAck;
    }

    public boolean getCancelAck() {
        return cancelAck;
    }

    public void setCancelAck(boolean cancelAck) {
        this.cancelAck = cancelAck;
    }

    public boolean getFillMenuStatus() {
        return enableFill;
    }

    public void setFillMenuStatus(boolean enableFill) {
        this.enableFill = enableFill;
    }

    public boolean getEnableCancel() {
        return enableCancel;
    }

    public void setMenuEnableCancel(boolean enableCancel) {
        this.enableCancel = enableCancel;
    }

    public boolean getRejectMenuFlag() {
        return rejectMenuFlag;
    }

    public void setRejectMenultem(boolean rejectMenuFlag) {
        this.rejectMenuFlag = rejectMenuFlag;
    }

    public static DefaultContextMenu addDefaultContextMenu(JTable component) {
        DefaultContextMenu defaultContextMenu = new DefaultContextMenu();
        defaultContextMenu.addTo(component);
        return defaultContextMenu;
    }

    private void addPopupMenuItems() {
        ackMenuItem = new JMenuItem("Ack");
        ackMenuItem.setEnabled(enableAck);
        ackMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        //ackMenuItem. addActionListener(event -> undoManager.undo());
        add(ackMenuItem);

        cancelMenuItem = new JMenuItem("Cancel");

        cancelMenuItem.setEnabled(cancelAck);

        cancelMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

    //cancelMenultem.addActionListener(event -> undoManager.redo());

        add(cancelMenuItem);

        add(new JSeparator());

        fillMenuItem = new JMenuItem("Fill");

        fillMenuItem.setEnabled(enableFill);

        fillMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

    //¥illMenuItem. addActionListener(event -> textComponent.fillMenuItem());

        add(fillMenuItem);

        rejectMenuItem = new JMenuItem("Reject");

        rejectMenuItem.setEnabled(false);

        rejectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

    //rejectMenultem.addActionListener(event -> textComponent.rejectMenulItem()) ;

        add(rejectMenuItem);

        acceptCancelMenuItem = new JMenuItem("Accept Cancel");
        acceptCancelMenuItem.setEnabled(false);
        //acceptCancelMenutItem. setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit()
        // .getMenuShortcutKeyMask()));
        //rejectMenultem.addActionListener(event -> textComponent.rejectMenulItem());
        add(acceptCancelMenuItem);
    }

    public void addTo(JTable textComponent) {
        this.textComponent = textComponent;
        textComponent.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent pressedEvent) {
                if ((pressedEvent.getKeyCode() == KeyEvent.VK_Z) && ((pressedEvent.getModifiersEx() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0)) {
                    if (undoManager.canUndo()) {
                        undoManager.undo();
                    }
                }
                if ((pressedEvent.getKeyCode() == KeyEvent.VK_Y) && ((pressedEvent.getModifiersEx() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0)) {
                    if (undoManager.canRedo()) {
                        undoManager.redo();
                    }
                }
            }
        });
        textComponent.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent releasedEvent) {
                handleContextMenu(releasedEvent);
            }

            @Override

            public void mouseReleased(MouseEvent releasedEvent) {
                handleContextMenu(releasedEvent);
            }

        });
    }

    public void addListener(MenuListener listener) {
        if (listener != null) {
            this.listener.add(listener);
        }

    }

    private void handleContextMenu(MouseEvent releasedEvent) {
        Point p = releasedEvent.getPoint();
        int row = textComponent.rowAtPoint(p);
        int col = textComponent.columnAtPoint(p);
        if (row > -1) {
            for (MenuListener l : listener) {
                l.onRowSelected(row, col);
            }

        }
        System.out.print("Menu Context mouse row:" + row + " Col:" + col);
        if (SwingUtilities.isRightMouseButton(releasedEvent)) {
//setCurrentRow(row) ;
            processClick(releasedEvent);

        } else if (SwingUtilities.isLeftMouseButton(releasedEvent)) {
// row selected.
        }

    }

    private void processClick(MouseEvent event) {
        textComponent = (JTable) event.getSource();
        textComponent.requestFocus();
        ackMenuItem.setEnabled(enableAck);
        cancelMenuItem.setEnabled(cancelAck);
        fillMenuItem.setEnabled(enableFill);
        rejectMenuItem.setEnabled(rejectMenuFlag);
        acceptCancelMenuItem.setEnabled(enableCancel);

// Shows the popup menu
        show(textComponent, event.getX(), event.getY());
    }
}