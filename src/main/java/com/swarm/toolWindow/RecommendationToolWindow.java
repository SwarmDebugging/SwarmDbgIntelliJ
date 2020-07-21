package com.swarm.toolWindow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.OpenSourceUtil;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.breakpoints.XBreakpointManager;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import com.swarm.models.Method;
import com.swarm.services.RecommendationService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;

public class RecommendationToolWindow extends SimpleToolWindowPanel implements DumbAware {
    private final Project project;
    private final RecommendationService recommendationService;
    private final JPanel panel = new JPanel();

    public RecommendationToolWindow(Project project) {
        super(false, true);
        this.project = project;
        recommendationService = new RecommendationService();

        panel.setLayout(new GridLayout(2, 3));

        /*we get the 2 most used methods but we need to check if they still exist, maybe we need the server to send all the methods*/
        ArrayList<Method> recommendedMethods = recommendationService.getRecommendedMethods(3);

        for (Method method : recommendedMethods) {
            JLabel classNameLabel = new JLabel();
            classNameLabel.setText(method.getName() + " in class " + method.getType().getName());

            JLabel goToMethodLabel = new JLabel();
            goToMethodLabel.setText("Go To Method");
            goToMethodLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);

                    PsiMethod psiMethod = getPsiMethod(method);
                    OpenSourceUtil.navigate(psiMethod);

                    /* OpenFileDescriptor descriptor = new OpenFileDescriptor(project, file, offset);
                    descriptor.navigate(true);*/
                }
            });

            JLabel setBreakpointLabel = new JBLabel();
            setBreakpointLabel.setText("set a breakpoint on the first line of the method");
            setBreakpointLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);

                    PsiMethod psiMethod = getPsiMethod(method);
                    var document = PsiDocumentManager.getInstance(project).getDocument(psiMethod.getContainingFile());
                    int lineNumber = document.getLineNumber(psiMethod.getTextOffset());
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        addLineBreakpoint(project, method.getType().getFullPath(), lineNumber + 1);
                    });
                }
            });

            panel.add(classNameLabel);
            panel.add(goToMethodLabel);
            panel.add(setBreakpointLabel);
        }

        setContent(panel);
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


}
