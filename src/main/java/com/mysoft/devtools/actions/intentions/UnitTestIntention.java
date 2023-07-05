package com.mysoft.devtools.actions.intentions;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * 单元测试意图
 *
 * @author hezd   2023/7/5
 */
public class UnitTestIntention implements IntentionAction {
    @Override
    public @IntentionName @NotNull String getText() {
        return "测试";
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "测试";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return true;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {

    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }
}
