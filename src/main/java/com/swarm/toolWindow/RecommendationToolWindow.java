package com.swarm.toolWindow;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.ui.components.JBList;
import com.intellij.util.OpenSourceUtil;
import com.intellij.util.ui.JBUI;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.breakpoints.XBreakpointManager;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import com.swarm.models.Method;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.nio.file.Path;

public class RecommendationToolWindow extends SimpleToolWindowPanel implements DumbAware {
    private final Project project;
    private JBList<Method> recommendationList;

    public RecommendationToolWindow(Project project) {
        super(false, true);
        this.project = project;

        recommendationList = new RecommendationList();
        setContent(recommendationList);
        createToolBar();
    }

    private void createToolBar() {
        final DefaultActionGroup group = new DefaultActionGroup();
        group.add(new JumpToSourceAction());
        group.add(new SetBreakpointAction());
        final ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("swarm", group, false);
        setToolbar(JBUI.Panels.simplePanel(actionToolbar.getComponent()));
    }

    @Override
    public JComponent getContent() {
        return this.getComponent();
    }

    private PsiMethod getPsiMethod(Method method) {
        final PsiMethod[] psiMethod = {null};

        ApplicationManager.getApplication().runReadAction(() -> {
            VirtualFile file = VirtualFileManager.getInstance().findFileByNioPath(Path.of(method.getType().getFullPath()));
            //FileEditorManager.getInstance(project).openFile(file,true);

            PsiJavaFile psiFile = (PsiJavaFile) PsiUtilBase.getPsiFile(project, file);
            PsiClass[] classes = psiFile.getClasses();
            PsiClass psiClass = null;
            if (classes.length == 1) {
                psiClass = classes[0];
            } else {
                for (PsiClass psiclass : classes) {
                    if (psiclass.getQualifiedName().equals(method.getType().getName())) {
                        psiClass = psiclass;
                    }
                }
            }
            String methodName = method.getName();
            PsiMethod[] psiMethods = psiClass.findMethodsByName(methodName, false);
            if (psiMethods.length == 0) {
                psiMethods = psiClass.getMethods();
                for (PsiMethod forMethod : psiMethods) {
                    //PsiMethod[] PsiMethodsMethods = method.g
                }
            } else if (psiMethods.length == 1) {
                psiMethod[0] = psiMethods[0];
            }

        });
        return psiMethod[0];
    }

    private void addLineBreakpoint(final Project project, final String fileUrl, final int line) {
        class MyBreakpointProperties extends XBreakpointProperties<MyBreakpointProperties> {
            public String myOption;

            public MyBreakpointProperties() {}

            @Override
            public MyBreakpointProperties getState() {
                return this;
            }

            @Override
            public void loadState(final MyBreakpointProperties state) {
                myOption = state.myOption;
            }
        }

        class MyLineBreakpointType extends XLineBreakpointType<MyBreakpointProperties> {
            public MyLineBreakpointType() {
                super("testId", "testTitle");
            }

            @Override
            public MyBreakpointProperties createBreakpointProperties(VirtualFile file, final int line) {
                return null;
            }

            @Override
            public MyBreakpointProperties createProperties() {
                return new MyBreakpointProperties();
            }
        }

        final XBreakpointManager breakpointManager = XDebuggerManager.getInstance(project).getBreakpointManager();
        final MyLineBreakpointType MY_LINE_BREAKPOINT_TYPE = new MyLineBreakpointType();
        final MyBreakpointProperties MY_LINE_BREAKPOINT_PROPERTIES = new MyBreakpointProperties();

        // add new line break point
        Runnable runnable = () -> breakpointManager.addLineBreakpoint(
                MY_LINE_BREAKPOINT_TYPE,
                fileUrl,
                line,
                MY_LINE_BREAKPOINT_PROPERTIES
        );
        WriteCommandAction.runWriteCommandAction(project, runnable);

        // toggle breakpoint to activate
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(new File(fileUrl));
        XDebuggerUtil.getInstance().toggleLineBreakpoint(project, virtualFile, line);
    }

    private final class JumpToSourceAction extends DumbAwareAction {
        JumpToSourceAction() {
            super("Jump to the method's source code","Jump to the selected method's source code", AllIcons.FileTypes.Any_type);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Method method = recommendationList.getSelectedValue();
            PsiMethod psiMethod = getPsiMethod(method);
            OpenSourceUtil.navigate(psiMethod);
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            super.update(e);
            e.getPresentation().setEnabled(recommendationList.getSelectedValue() != null);
        }
    }

    private final class SetBreakpointAction extends DumbAwareAction {
        SetBreakpointAction() {
            super("Set a breakpoint in the method", "Set a line breakpoint in the selected method's first line", AllIcons.Debugger.Db_set_breakpoint);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Method method = recommendationList.getSelectedValue();
            PsiMethod psiMethod = getPsiMethod(method);
            var document = PsiDocumentManager.getInstance(project).getDocument(psiMethod.getContainingFile());
            int lineNumber = document.getLineNumber(psiMethod.getTextOffset());
            WriteCommandAction.runWriteCommandAction(project, () -> {
                addLineBreakpoint(project, method.getType().getFullPath(), lineNumber + 1);
            });
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            super.update(e);
            e.getPresentation().setEnabled(recommendationList.getSelectedValue() != null);
        }
    }
}