package com.swarm.mouseListeners;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.ui.table.JBTable;
import com.swarm.popupMenu.PopupMenuBuilder;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RecommendationMouseListener extends MouseAdapter {

    MouseEvent mouseEvent;
    PopupMenuBuilder popupMenuBuilder;
    int RIGHT_CLICK = MouseEvent.BUTTON3;

    public RecommendationMouseListener(Project project){
        popupMenuBuilder = new PopupMenuBuilder(project);
    }


    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        this.mouseEvent = mouseEvent;
        super.mouseClicked(mouseEvent);
        if (mouseEvent.getButton() == RIGHT_CLICK) {
            selectClickedRow();
            JBPopupMenu popupMenu = popupMenuBuilder.buildRecommendationPopupMenu();
            popupMenu.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
        }
    }

    private void selectClickedRow() {
        JBTable table = (JBTable) mouseEvent.getSource();
        int selRow = table.rowAtPoint(new Point(mouseEvent.getX(), mouseEvent.getY()));
        table.setRowSelectionInterval(selRow,selRow);
        table.requestFocusInWindow();
    }
}
