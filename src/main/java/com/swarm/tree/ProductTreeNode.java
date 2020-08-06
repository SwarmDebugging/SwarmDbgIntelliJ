package com.swarm.tree;

import com.swarm.models.Product;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class ProductTreeNode extends DefaultMutableTreeNode {

    protected DefaultTreeModel model;

    private Product product;
    private final String toolTip;

    public ProductTreeNode(Product product) {
        super(product.getName());
        this.model = null;
        this.product = product;
        this.toolTip = product.getName();
    }

    public void setModel(DefaultTreeModel model) {
        this.model = model;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
