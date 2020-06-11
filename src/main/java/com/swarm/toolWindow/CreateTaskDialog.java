package com.swarm.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.uiDesigner.core.AbstractLayout;
import com.intellij.util.ui.GridBag;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.swarm.tools.HTTPRequests;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class CreateTaskDialog extends DialogWrapper {

    private final JPanel panel = new JPanel(new GridBagLayout());
    private final int productId;
    private final JTextField taskTextFiled = new JTextField();
    private final int developerId;

    public CreateTaskDialog(@Nullable Project project, int productId, int developerId) {
        super(project);
        init();
        setTitle("Log into Your Swarm Debugging Account");
        this.productId = productId;
        this.developerId = developerId;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {

        GridBag gb = new GridBag();
        gb.setDefaultInsets(JBUI.insets(0, 0, AbstractLayout.DEFAULT_VGAP, AbstractLayout.DEFAULT_HGAP));

        GridBagConstraints gridBagConstraints = new GridBagConstraints();

        gridBagConstraints.fill =GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        panel.add(label("Task Name: "), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        panel.add(taskTextFiled, gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;

        panel.setPreferredSize(new Dimension(400,200));

        return panel;
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
        HTTPRequests.createTask(productId, taskTextFiled.getText(), false, developerId);
    }

    private JComponent label(String text) {
        JBLabel label = new JBLabel(text);
        label.setComponentStyle(UIUtil.ComponentStyle.SMALL);
        label.setFontColor(UIUtil.FontColor.BRIGHTER);
        label.setBorder(JBUI.Borders.empty(0,5,2,0));

        return label;
    }
}
