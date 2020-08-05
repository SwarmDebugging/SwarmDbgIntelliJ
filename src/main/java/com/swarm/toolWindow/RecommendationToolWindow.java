package com.swarm.toolWindow;

import com.intellij.application.Topics;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.DocumentUtil;
import com.intellij.util.OpenSourceUtil;
import com.intellij.util.ui.JBUI;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.breakpoints.XBreakpointManager;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import com.swarm.models.*;
import com.swarm.services.RecommendationService;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class RecommendationToolWindow extends SimpleToolWindowPanel implements DumbAware, treeSelectionProvider.Handler, Disposable {
    private final Project project;
    private JBTable contentTable;
    private RecommendationService recommendationService = new RecommendationService();

    public RecommendationToolWindow(Project project) {
        super(false, true);
        this.project = project;

        Topics.subscribe(treeSelectionProvider.Handler.TREE_SELECTION_TOPIC, this, this);

        setContent(new JBLabel("Select a task to see breakpoint location recommendations"));
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

        Runnable runnable = () -> breakpointManager.addLineBreakpoint(
                MY_LINE_BREAKPOINT_TYPE,
                fileUrl,
                line,
                MY_LINE_BREAKPOINT_PROPERTIES
        );
        WriteCommandAction.runWriteCommandAction(project, runnable);

        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(new File(fileUrl));
        XDebuggerUtil.getInstance().toggleLineBreakpoint(project, virtualFile, line);
    }

    @Override
    public void dispose() {

    }

    private final class JumpToSourceAction extends DumbAwareAction {
        JumpToSourceAction() {
            super("Jump to the method's source code","Jump to the selected method's source code", AllIcons.FileTypes.Any_type);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            if(treeSelectionProvider.getTreeNode() instanceof Task) {
                Method method = (Method) contentTable.getValueAt(contentTable.getSelectedRow(), 0);
                PsiMethod psiMethod = getPsiMethod(method);
                OpenSourceUtil.navigate(psiMethod);
            } else if(treeSelectionProvider.getTreeNode() instanceof Session) {
                try {
                    Breakpoint breakpoint = (Breakpoint) contentTable.getValueAt(contentTable.getSelectedRow(), contentTable.getSelectedColumn());

                    VirtualFile file = VirtualFileManager.getInstance().findFileByNioPath(Path.of(breakpoint.getType().getFullPath()));
                    if (file == null) {
                        return;
                    }
                    OpenFileDescriptor descriptor = new OpenFileDescriptor(project, file, breakpoint.getLineNumber(), 0);
                    OpenSourceUtil.navigate(descriptor);
                } catch (ClassCastException classCastException){
                    Messages.showInfoMessage("Cannot Jump to source", "No Breakpoint");
                }
            }
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            super.update(e);
            if(contentTable != null) {
                e.getPresentation().setEnabled(contentTable.getSelectedRow() != -1);
            } else {
                e.getPresentation().setEnabled(false);
            }
        }
    }

    private PsiMethod getPsiMethod(Method method) {
        final PsiMethod[] psiMethod = {null};

        ApplicationManager.getApplication().runReadAction(() -> {
            VirtualFile file = VirtualFileManager.getInstance().findFileByNioPath(Path.of(method.getType().getFullPath()));
            if(file == null){
                return;
            }
            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            PsiClass psiClass = PsiTreeUtil.getChildOfType(psiFile, PsiClass.class);

            String methodName = method.getName();
            PsiMethod[] psiMethods = psiClass.findMethodsByName(methodName, false);

            psiMethod[0] = psiMethods[0];

        });
        return psiMethod[0];
    }

    private final class SetBreakpointAction extends DumbAwareAction {
        SetBreakpointAction() {
            super("Set a breakpoint in the method", "Set a line breakpoint in the selected method's first line", AllIcons.Debugger.Db_set_breakpoint);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            if(treeSelectionProvider.getTreeNode() instanceof Task) {
                Method method = (Method) contentTable.getValueAt(contentTable.getSelectedRow(), 0);
                PsiMethod psiMethod = getPsiMethod(method);
                var document = PsiDocumentManager.getInstance(project).getDocument(psiMethod.getContainingFile());
                int methodFirstLine = document.getLineNumber(psiMethod.getTextOffset());
                String line = "";
                do {
                    methodFirstLine++;
                    TextRange range = new TextRange(document.getLineStartOffset(methodFirstLine), document.getLineEndOffset(methodFirstLine));
                    line = document.getText(range);
                } while (line.matches("\\A\\s*\\z"));
                int finalMethodFirstLine = methodFirstLine;
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    addLineBreakpoint(project, method.getType().getFullPath(), finalMethodFirstLine);
                });
            } else if (treeSelectionProvider.getTreeNode() instanceof Session) {
                try {
                    Breakpoint breakpoint = (Breakpoint) contentTable.getValueAt(contentTable.getSelectedRow(), contentTable.getSelectedColumn());

                    addLineBreakpoint(project, breakpoint.getType().getFullPath(), breakpoint.getLineNumber());
                } catch (ClassCastException classCastException){
                    Messages.showInfoMessage("Cannot set a breakpoint", "No Breakpoint");
                }
            }
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            super.update(e);
            if(contentTable != null) {
                e.getPresentation().setEnabled(contentTable.getSelectedRow() != -1);
            } else {
                e.getPresentation().setEnabled(false);
            }
        }
    }

    @Override
    public void treeSelectionAction(Object treeNode) {
        if (treeNode == null) {
            setContent(new JLabel("Select a task to get breakpoint recommendations"));
            contentTable = null;
        } else if(treeNode instanceof Task) {
            Task task = (Task) treeNode;
            ArrayList<Method> recommendedMethods = recommendationService.getRecommendedMethods(task.getId());

            LinkedHashMap<Method, Integer> hm = new LinkedHashMap<>();
            for (Method method : recommendedMethods) {
                Integer j = hm.get(method);
                hm.put(method, (j == null) ? 1 : j + 1);
            }

            Set<Method> methodsAlreadySeen = new HashSet<>();
            recommendedMethods.removeIf(method -> !methodsAlreadySeen.add(method));

            recommendedMethods.sort((method, method2) -> hm.get(method2).compareTo(hm.get(method)));

            TableModel dataModel = new AbstractTableModel() {
                @Override
                public int getRowCount() {
                    return recommendedMethods.size();
                }

                @Override
                public int getColumnCount() {
                    return 3;
                }

                @Override
                public String getColumnName(int column) {
                    if (column == 0) {
                        return "Method";
                    } else if (column == 1) {
                        return "Type";
                    } else {
                        return "Number of debugging events";
                    }
                }

                @Override
                public Object getValueAt(int row, int col) {
                    if (col == 0) {
                        return recommendedMethods.get(row).getName();
                    } else if (col == 1) {
                        return recommendedMethods.get(row).getType().getFullName();
                    } else {
                        Method method = recommendedMethods.get(row);
                        return hm.get(method);
                    }
                }
            };

            contentTable = new JBTable(dataModel);
            contentTable.setSelectionModel(new ForcedListSelectionModel());
            JBScrollPane scrollPane = new JBScrollPane(contentTable);
            setContent(scrollPane);
        } else if(treeNode instanceof Session) {
            Session session = (Session) treeNode;
            ArrayList<Breakpoint> breakpoints = recommendationService.getBreakpointsBySessionId(session.getId());

            TableModel dataModel = new AbstractTableModel() {
                @Override
                public int getRowCount() {
                    return 1;
                }

                @Override
                public int getColumnCount() {
                    return 1;
                }

                @Override
                public String getColumnName(int column) {
                    if (column == 0) {
                        return "First Breakpoint in selected session";
                    } else if (column == 1) {
                        return "Type";
                    } else {
                        return "Number of debugging events";
                    }
                }

                @Override
                public Object getValueAt(int row, int col) {
                    if (col == 0) {
                        if(!breakpoints.isEmpty()) {
                            return breakpoints.get(0);
                        }
                        return "No breakpoint set in this session";
                    } else return "abc";
                }
            };

            contentTable = new JBTable(dataModel);
            contentTable.setSelectionModel(new ForcedListSelectionModel());
            JBScrollPane scrollPane = new JBScrollPane(contentTable);
            setContent(scrollPane);
        }
    }

    private static class ForcedListSelectionModel extends DefaultListSelectionModel {
        private ForcedListSelectionModel() {
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }

        @Override
        public void clearSelection() {
        }

        @Override
        public void removeSelectionInterval(int index0, int index1) {
        }
    }
}