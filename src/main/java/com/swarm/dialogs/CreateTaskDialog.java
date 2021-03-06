package com.swarm.dialogs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.swarm.models.Product;
import com.swarm.models.Task;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class CreateTaskDialog extends DialogWrapper {

    private final JPanel panel = new JPanel(new GridBagLayout());
    private final Product product;
    private final JTextField taskTitleField = new JTextField();

    public CreateTaskDialog(@Nullable Project project, Product product) {
        super(project);
        init();
        this.product = product;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        setTitle("Create a New Task");
        GridBagConstraints constraints = createGridBagConstraints();
        panel.add(taskNameLabel(), constraints);
        constraints.gridx = 1;
        panel.add(taskTitleField, constraints);
        panel.setPreferredSize(new Dimension(400,200));

        return panel;
    }

    private GridBagConstraints createGridBagConstraints() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill =GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0.2;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridx = 0;
        constraints.gridy = 0;

        return constraints;
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
        Task task = new Task();

        task.setProduct(product);
        task.setDone(false);
        task.setTitle(taskTitleField.getText());
        task.create();
    }

    private JComponent taskNameLabel() {
        JBLabel label = new JBLabel("Task Name: ");
        label.setComponentStyle(UIUtil.ComponentStyle.SMALL);
        label.setFontColor(UIUtil.FontColor.BRIGHTER);
        label.setBorder(JBUI.Borders.empty(0,5,2,0));

        return label;
    }
}
