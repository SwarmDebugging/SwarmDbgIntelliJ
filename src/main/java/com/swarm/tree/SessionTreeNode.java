package com.swarm.tree;

import com.swarm.models.Session;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class SessionTreeNode extends DefaultMutableTreeNode {

    protected DefaultTreeModel model;

    private Session session;
    private final String toolTip;

    public SessionTreeNode(Session session) {
        super(session.getDescription());
        this.model = null;
        this.session = session;
        this.toolTip = session.getDescription();
    }

    public void setModel(DefaultTreeModel model) {
        this.model = model;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }
}
