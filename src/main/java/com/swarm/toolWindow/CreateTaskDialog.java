package com.swarm.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.swarm.models.Developer;
import com.swarm.models.Product;
import com.swarm.models.Session;
import com.swarm.models.Task;
import com.swarm.tools.HTTPUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class CreateTaskDialog extends DialogWrapper {

    private final JPanel panel = new JPanel(new GridBagLayout());
    private final Product product;
    private final JTextField taskTitleField = new JTextField();
    private final Developer developer;

    public CreateTaskDialog(@Nullable Project project, Product product, Developer developer) {
        super(project);
        init();
        this.product = product;
        this.developer = developer;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        setTitle("Create a New Task");
        GridBagConstraints constraints = createGridBagConstraints();
        panel.add(label("Task Name: "), constraints);
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

        Session session = new Session();
        session.setTask(task);
        session.setDeveloper(developer);
        session.createSessionForDeveloperLinking();
    }

    private JComponent label(String text) {
        JBLabel label = new JBLabel(text);
        label.setComponentStyle(UIUtil.ComponentStyle.SMALL);
        label.setFontColor(UIUtil.FontColor.BRIGHTER);
        label.setBorder(JBUI.Borders.empty(0,5,2,0));

        return label;
    }
}
