package com.swarm.toolWindow;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import com.swarm.models.Method;

import javax.swing.*;
import java.awt.*;

public class RecommendationItemCellRenderer extends JBLabel implements ListCellRenderer<Method> {
    @Override
    public Component getListCellRendererComponent(JList<? extends Method> jList, Method method, int index, boolean isSelected, boolean hasFocus) {
        final String message = method.getName() + " in class " + method.getType().getName();
        setText(message);
        setBorder(JBUI.Borders.empty(2,10));
        final Color foreground = jList.getForeground();
        setForeground(foreground);
        if(isSelected){
            setForeground(JBColor.BLUE);
        }
        return this;
    }
}
