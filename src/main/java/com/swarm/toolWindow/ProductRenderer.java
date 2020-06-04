package com.swarm.toolWindow;

import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

public class ProductRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if(value instanceof JLabel) {
            this.setText(((JLabel)value).getText());
            this.setToolTipText(((JLabel)value).getToolTipText());
            this.setBorder(JBUI.Borders.empty(2, 10, 2, 0));
        }

        if(!isSelected) {
            this.setBackground((Color) null);
        }
        return this;
    }
}
