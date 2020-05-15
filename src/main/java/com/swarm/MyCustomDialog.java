package com.swarm;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.uiDesigner.core.AbstractLayout;
import com.intellij.util.ui.GridBag;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class MyCustomDialog extends DialogWrapper {

    private JPanel panel = new JPanel(new GridBagLayout());
    private JTextField firstname = new JTextField();
    private JTextField lastname = new JTextField();

    protected MyCustomDialog(@Nullable Project project) {
        super(project);
        init();
        setTitle("Yo");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {

        GridBag gb = new GridBag();
        gb.setDefaultWeightX(1.0);
        gb.setDefaultFill(GridBagConstraints.HORIZONTAL);
        gb.setDefaultInsets(new Insets(0,0,AbstractLayout.DEFAULT_VGAP, AbstractLayout.DEFAULT_HGAP));

        panel.add(label("fisrt name: "), gb.nextLine().next().weightx(0.2));
        panel.add(firstname, gb.next().weightx(0.8));

        panel.setPreferredSize(new Dimension(400,200));

        return panel;
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
        String firstnameText = firstname.getText();
        JOptionPane.showMessageDialog(panel, "name: " + firstnameText);
    }

    private void createDeveloperQuery(String username) {

    }

    private JComponent label(String text) {
        JBLabel label = new JBLabel(text);
        label.setComponentStyle(UIUtil.ComponentStyle.SMALL);
        label.setFontColor(UIUtil.FontColor.BRIGHTER);
        label.setBorder(JBUI.Borders.empty(0,5,2,0));

        return label;
    }
}
