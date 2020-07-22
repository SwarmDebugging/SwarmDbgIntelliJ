package com.swarm.toolWindow;

import com.intellij.ui.components.JBList;
import com.swarm.models.Method;
import com.swarm.services.RecommendationService;

import javax.swing.*;
import java.util.ArrayList;

public class RecommendationList extends JBList<Method> {
    private final DefaultListModel<Method> myModel;
    private final RecommendationService recommendationService = new RecommendationService();

    public RecommendationList() {
        myModel = new DefaultListModel<>();
        setModel(myModel);
        setCellRenderer(new RecommendationItemCellRenderer());
        getRecommendations();
    }

    @Override
    public ListModel<Method> getModel() {
        return myModel;
    }

    private void getRecommendations() {
        myModel.removeAllElements();
        ArrayList<Method> recommendedMethods = recommendationService.getRecommendedMethods(3);
        for (Method method: recommendedMethods) {
            myModel.addElement(method);
        }
    }
}
