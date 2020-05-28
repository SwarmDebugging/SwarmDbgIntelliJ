package com.swarm.listeners;

import com.intellij.debugger.engine.ContextUtil;
import com.intellij.lang.jvm.JvmParameter;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.impl.source.tree.java.MethodElement;
import com.intellij.psi.util.PsiClassUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.xml.JavaMethod;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointListener;
import com.swarm.States;
import com.swarm.tools.HTTPRequests;
import org.jetbrains.annotations.NotNull;

public class breakpointListener implements XBreakpointListener<XBreakpoint<?>> {

    private final Project project;

    public breakpointListener(Project project) {
        this.project = project;
    }

    @Override
    public void breakpointAdded(@NotNull XBreakpoint breakpoint) {
        NotificationGroup notificationGroup = new NotificationGroup("NotificationBreakpointAdded", NotificationDisplayType.BALLOON, true);

        //i need lineNumber, method which needs name and signature, type which need full name, full path, name and source code

        PsiJavaFile file = (PsiJavaFile) PsiManager.getInstance(project).findFile(breakpoint.getSourcePosition().getFile());

        String typeName = file.getName();
        String typePath = file.getVirtualFile().getPath();
        String sourceCode = file.getText();
        String typeFullName;
        if (!(typeFullName = file.getPackageName()).equals("")) {
            typeFullName += ".";
        }
        typeFullName += typeName;

        int typeId = HTTPRequests.createType(States.currentSessionId, typeFullName, typeName, typePath, sourceCode);

        int lineNumber = breakpoint.getSourcePosition().getLine();
        int offset = breakpoint.getSourcePosition().getOffset();
        var element = file.findElementAt(offset);

        if(element == null) {
            //that means that there's no element where the breakpoint is(unlikely)
            return;
        }

        var method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        if(method == null && element.getNextSibling() instanceof PsiMethod) {
            method = PsiTreeUtil.getNextSiblingOfType(element, PsiMethod.class);
        } else if(method == null){
            //the breakpoint is not a method(could be on a class or a field)
            return;
        }



        var children = method.getChildren();
        String methodName = method.getName();
        String methodReturnType = method.getReturnType().getPresentableText();
        var parameters = method.getParameters();

        var methodSignature = method.getSignature(PsiSubstitutor.EMPTY);

        int methodId = 0;
    }

    @Override
    public void breakpointRemoved(@NotNull XBreakpoint<?> breakpoint) {
        NotificationGroup notificationGroup = new NotificationGroup("NotificationBreakpointAdded", NotificationDisplayType.BALLOON, true);

        int x = breakpoint.getSourcePosition().getLine();

        notificationGroup.createNotification("Hello from first plugin",
                "line number: " + x,
                NotificationType.INFORMATION,
                null).notify(project);
    }

    private String encodeSignature(JvmParameter[] parameters, String returnType) {
        //TODO Is it better to encode signature or to decode in the stepping events???
        return "Encoded signature";
    }
}
