package com.swarm.listeners;

import com.intellij.debugger.DebuggerManagerEx;
import com.intellij.debugger.engine.evaluation.EvaluateException;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.DocumentUtil;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.frame.XStackFrame;
import com.swarm.tools.HTTPRequests;
import org.jetbrains.annotations.NotNull;

public class fileEditorListener implements FileEditorManagerListener {

    Project project;

    public fileEditorListener(Project project) {
        this.project = project;
    }

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {

        XDebugSession session;
        int invokingId;
        if((session = XDebuggerManager.getInstance(project).getCurrentSession()) != null && (invokingId = DebugActionListener.invokingMethod.getId()) != 0){
            /*XStackFrame frame = session.getCurrentStackFrame();
            var file = frame.getSourcePosition().getFile();
            int line = frame.getSourcePosition().getLine();*/
            var debuggerManagerEx = DebuggerManagerEx.getInstanceEx(project);
            try {
                var method1 = debuggerManagerEx.getContext().getFrameProxy().location().method();
            } catch (EvaluateException e) {
                e.printStackTrace();
            }
            try {
                var frames = debuggerManagerEx.getContext().getFrameProxy().threadProxy().frames();
            } catch (EvaluateException e) {
                e.printStackTrace();
            }

            /*Document document = FileDocumentManager.getInstance().getDocument(file);
            TextRange range = DocumentUtil.getLineTextRange(document, line);
            String lineContent = document.getText(range);*/

            int response = HTTPRequests.createInvocation("","",invokingId, "linecontent", 100, 101, 20);
            DebugActionListener.invokingMethod.setId(0);
        }
    }
}
