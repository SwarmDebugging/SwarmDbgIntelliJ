package com.swarm.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.swarm.models.Developer;
import com.swarm.models.Product;
import com.swarm.tools.HTTPUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class CreateProductDialog extends DialogWrapper {

    private final JPanel panel = new JPanel(new GridBagLayout());
    private final Developer developer;
    private final JTextField productNameField = new JTextField();

    public CreateProductDialog(@Nullable Project project, Developer developer) {
        super(project);
        init();
        this.developer = developer;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {

        setTitle("Create a New Product");
        GridBagConstraints constraints = createGridBagConstraints();
        panel.add(label("Product Name: "), constraints);
        constraints.gridx = 1;
        panel.add(productNameField, constraints);
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

    @Override
    protected void doOKAction() {
        super.doOKAction();
        Product product = new Product();
        product.setName(productNameField.getText());
        product.setDeveloper(developer);
        product.create();
        //HTTPUtils.createProduct(productNameField.getText(), developerId);
    }

    private JComponent label(String text) {
        JBLabel label = new JBLabel(text);
        label.setComponentStyle(UIUtil.ComponentStyle.SMALL);
        label.setFontColor(UIUtil.FontColor.BRIGHTER);
        label.setBorder(JBUI.Borders.empty(0,5,2,0));

        return label;
    }
}
