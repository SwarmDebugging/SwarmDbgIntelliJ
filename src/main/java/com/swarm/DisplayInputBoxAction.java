package com.swarm;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class DisplayInputBoxAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        MyCustomDialog wrapper = new MyCustomDialog(e.getProject());
        wrapper.showAndGet();
    }
}
