package com.swarm.listeners;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.psi.PsiManager;
import com.swarm.tools.HTTPRequests;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class fileChangeListener implements BulkFileListener {

    private final Project project;

    public fileChangeListener(Project project) {
        this.project = project;
    }

    @Override
    public void after(@NotNull List<? extends VFileEvent> events) {

        int i = 0;
        String sourceCode = PsiManager.getInstance(project).findFile(events.get(0).getFile()).getText();
        String fullPath = events.get(0).getPath();
        String name = events.get(0).getFile().getPresentableName();
        String fullName = events.get(0).getFile().getName();
        //String result = HTTPRequests.createType(20, fullName, name, fullPath, sourceCode);

    }
}
