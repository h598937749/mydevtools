package com.mysoft.devtools.actions.toolactions;

import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.spring.CommonSpringModel;
import com.intellij.spring.model.utils.SpringModelUtils;
import com.mysoft.devtools.utils.CollectExtension;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;

/**
 * @author hezd 2023/5/31
 */
@ExtensionMethod(CollectExtension.class)
public class ExecuteSqlAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
//        ExecuteSqlDialog dialog = new ExecuteSqlDialog();
//        dialog.show();

        Caret caret = e.getData(PlatformDataKeys.CARET);
        String injectableWithUnderscores = "_" + caret.getSelectedText() + "_";
        VirtualFile file = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        Project project = e.getProject();
        JSFile jsFile = (JSFile) PsiManager.getInstance(project).findFile(file);

        String content = jsFile.getText();  // 获取 JavaScript 文件内容
        JSCallExpression defineFunc = PsiTreeUtil.findChildOfType(jsFile, JSCallExpression.class);


        GlobalSearchScope scope = ProjectScope.getAllScope(project);
        PsiClass baseClass = JavaPsiFacade.getInstance(project).findClass("com.mysoft.czxt.cgplanmng.service.appservice.BudgetsAppService", scope);
        CommonSpringModel psiClassSpringModel = SpringModelUtils.getInstance().getPsiClassSpringModel(baseClass);
    }


}
