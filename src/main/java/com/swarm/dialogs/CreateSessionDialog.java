package com.swarm.dialogs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class CreateSessionDialog extends DialogWrapper {

    private final JPanel panel = new JPanel(new GridBagLayout());
    private final JTextField descriptionTextField = new JTextField();


    public CreateSessionDialog(Project project) {
        super(project);
        init();
    }


    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        setTitle("Create a New Session");
        GridBagConstraints constraints = createGridBagConstraints();
        panel.add(productCreationLabel(), constraints);
        constraints.gridx = 1;
        panel.add(descriptionTextField, constraints);
        panel.setPreferredSize(new Dimension(400,200));

        return panel;
    }

    private GridBagConstraints createGridBagConstraints() {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill =GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;

        return gridBagConstraints;
    }

    private JComponent productCreationLabel() {
        JBLabel label = new JBLabel("Session Description: ");
        label.setComponentStyle(UIUtil.ComponentStyle.SMALL);
        label.setFontColor(UIUtil.FontColor.BRIGHTER);
        label.setBorder(JBUI.Borders.empty(0,5,2,0));

        return label;
    }

    public boolean showAndGet() {
        return super.showAndGet();
    }

    public String getDescription() {
        return descriptionTextField.getText();
    }
}
