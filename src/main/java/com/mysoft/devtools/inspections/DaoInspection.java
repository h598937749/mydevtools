package com.mysoft.devtools.inspections;

import com.intellij.codeInspection.*;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiTypesUtil;
import com.mysoft.devtools.bundles.InspectionBundle;
import com.mysoft.devtools.dtos.QualifiedNames;
import com.mysoft.devtools.utils.psi.PsiClassExtension;
import com.mysoft.devtools.utils.psi.PsiExpressionExtension;
import com.mysoft.devtools.utils.psi.PsiTypeExtension;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 1、列名列参数不能使用Lambda表达式
 * 2、不能使用eq null，ne null，应该使用isNull或 isNotNull
 * 3、检查列名和参数值类型是否一致
 *
 * @author hezd   2023/5/27
 */
@ExtensionMethod({PsiExpressionExtension.class, PsiClassExtension.class, PsiTypeExtension.class})
public class DaoInspection extends AbstractBaseJavaLocalInspectionTool {
    private final List<String> VALUE_TYPES = Arrays.asList("Object", "Collection<?>");

    private final LambdaLocalQuickFix lambdaLocalQuickFix = new LambdaLocalQuickFix();
    private static PsiClass SFUNCTION_PSICLASS;

    private static PsiType SFUNCTION_PSITYPE;
    private static PsiClass BASE_MAPPER_PSICLASS;

    private static PsiClass LAMBDA_QUERY_WRAPPER_PSICLASS;

    private static PsiClass getSFunctionPsiClass(Project project) {
        if (SFUNCTION_PSICLASS == null) {
            SFUNCTION_PSICLASS = PsiClassExtension.getPsiClass(project, QualifiedNames.S_FUNCTION_QUALIFIED_NAME);
        }
        return SFUNCTION_PSICLASS;
    }

    private static PsiType getSFunctionPsiType(Project project) {
        if (SFUNCTION_PSITYPE == null) {
            SFUNCTION_PSITYPE = PsiTypeExtension.createTypeFromText(project, QualifiedNames.S_FUNCTION_QUALIFIED_NAME);
        }
        return SFUNCTION_PSITYPE;
    }

    private static PsiClass getBaseMapperPsiClass(Project project) {
        if (BASE_MAPPER_PSICLASS == null) {
            BASE_MAPPER_PSICLASS = PsiClassExtension.getPsiClass(project, QualifiedNames.BASE_MAPPER_QUALIFIED_NAME);
        }
        return BASE_MAPPER_PSICLASS;
    }

    private static PsiClass getLambdaQueryWrapperPsiClass(Project project) {
        if (LAMBDA_QUERY_WRAPPER_PSICLASS == null) {
            LAMBDA_QUERY_WRAPPER_PSICLASS = PsiClassExtension.getPsiClass(project, QualifiedNames.LAMBDA_QUERY_WRAPPER_QUALIFIED_NAME);
        }
        return LAMBDA_QUERY_WRAPPER_PSICLASS;
    }

    /**
     * 检查 BaseMapper 的方法调用规范
     */
    private void checkerBaseMapper(CheckerDTO checkerDTO) {
        PsiClass psiClass = checkerDTO.getMethod().getContainingClass();

        if (psiClass == null || !psiClass.isInheritors(getBaseMapperPsiClass(checkerDTO.getProject()))) {
            return;
        }

        PsiType columnPsiType = checkerDTO.getSignParameters()[checkerDTO.getColumnIndex()].getType();
        PsiClass columnTypeClass = PsiTypesUtil.getPsiClass(columnPsiType);

        //兼容这种写法SFunction<T, R> keyFunc
        if (columnTypeClass == null || !columnTypeClass.isInheritors(getSFunctionPsiClass(checkerDTO.getProject()))) {
            return;
        }

        PsiExpression columnPsiExpression = checkerDTO.getPsiExpressions()[checkerDTO.getColumnIndex()];
        if (columnPsiExpression instanceof PsiLambdaExpression) {
            checkerDTO.getHolder().registerProblem(columnPsiExpression, InspectionBundle.message("inspection.platform.service.dao.problem.lambda.descriptor"), ProblemHighlightType.ERROR, lambdaLocalQuickFix);
        }
    }

    /**
     * 检查 LambdaQueryWrapper 的方法调用规范
     */
    private void checkerLambdaQueryWrapper(CheckerDTO checkerDTO) {
        if (checkerDTO.getColumnIndex() == -1 || checkerDTO.getColumnIndex() + 1 >= checkerDTO.getSignParameters().length) {
            return;
        }
        PsiClass psiClass = checkerDTO.getMethod().getContainingClass();
//        if (psiClass == null || !psiClass.isInheritors(getLambdaQueryWrapperPsiClass(checkerDTO.getProject()))) {
//            return;
//        }

        if (!Objects.equals(psiClass.getPackageName(), "com.baomidou.mybatisplus.core.conditions.interfaces")) {
            return;
        }

        PsiType valueType = checkerDTO.getSignParameters()[checkerDTO.getColumnIndex() + 1].getType();
        if (!VALUE_TYPES.contains(valueType.getPresentableText())) {
            return;
        }
        /*
         * Children isNull(boolean condition, R column)
         * default Children inSql(R column, String inValue)
         * Children gtSql(boolean condition, R column, String inValue)
         * */
        //参数1 一般是列名
        PsiExpression columnPsiExpression = checkerDTO.getPsiExpressions()[checkerDTO.getColumnIndex()];
        if (columnPsiExpression instanceof PsiLambdaExpression) {
            checkerDTO.getHolder().registerProblem(columnPsiExpression, InspectionBundle.message("inspection.platform.service.dao.problem.lambda.descriptor"), ProblemHighlightType.ERROR, lambdaLocalQuickFix);
        }
        PsiType columnPsiType = columnPsiExpression.tryGetPsiType();
        PsiClass columnTypeClass = PsiTypesUtil.getPsiClass(columnPsiType);

        //兼容这种写法SFunction<T, R> keyFunc
        if (columnTypeClass != null && columnTypeClass.isInheritors(getSFunctionPsiClass(checkerDTO.getProject()))) {
            if (columnPsiType instanceof PsiClassReferenceType) {
                PsiType[] parameters = ((PsiClassReferenceType) columnPsiType).getParameters();
                if (parameters.length == 2) {
                    columnPsiType = parameters[1];
                }
            }
        }

        PsiExpression valuePsiExpression = checkerDTO.getPsiExpressions()[checkerDTO.getColumnIndex() + 1];
        PsiType valuePsiType;
        String name = checkerDTO.getMethod().getName();
        switch (name) {
            case "in":
            case "notIn":
                //List<UUID>
                valuePsiType = valuePsiExpression.tryGetPsiType();
                if (valuePsiType instanceof PsiClassType) {
                    PsiType[] parameters = ((PsiClassType) valuePsiType).getParameters();
                    if (parameters.length > 0) {
                        //UUID
                        valuePsiType = parameters[0];
                    }
                }
                break;
            default:
                valuePsiType = valuePsiExpression.tryGetPsiType();
                break;
        }

        if (Objects.equals(valuePsiExpression.getText(), "null")) {
            checkerDTO.getHolder().registerProblem(checkerDTO.getExpression(), InspectionBundle.message("inspection.platform.service.dao.problem.null.descriptor"), ProblemHighlightType.ERROR);
        }
        if (columnPsiType != null && valuePsiType != null && !PsiTypeExtension.compareTypes(columnPsiType, valuePsiType) && !"?".equals(valuePsiType.getPresentableText())) {
            checkerDTO.getHolder().registerProblem(valuePsiExpression, InspectionBundle.message("inspection.platform.service.dao.problem.type.discord.descriptor"
                    , columnPsiType.getPresentableText(), valuePsiType.getPresentableText()), ProblemHighlightType.ERROR);
        }
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                PsiMethod method = expression.resolveMethod();
                if (method == null) {
                    return;
                }
                PsiExpression[] expressions = expression.getArgumentList().getExpressions();
                if (expressions.length < 2) {
                    return;
                }

                int columnIndex = -1;
                //方法签名参数列表
                PsiParameter[] signParameters = method.getParameterList().getParameters();
                for (int index = 0; index < signParameters.length; index++) {
                    PsiType sFunctionPsiType = getSFunctionPsiType(holder.getProject());

                    if (sFunctionPsiType.compareTypes(signParameters[index].getType())) {
                        columnIndex = index;
                        break;
                    }
                }

                CheckerDTO checkerDTO = CheckerDTO.builder()
                        .project(holder.getProject())
                        .columnIndex(columnIndex)
                        .method(method)
                        .signParameters(signParameters)
                        .psiExpressions(expressions)
                        .holder(holder)
                        .isOnTheFly(isOnTheFly)
                        .expression(expression)
                        .build();

                checkerLambdaQueryWrapper(checkerDTO);
                checkerBaseMapper(checkerDTO);
            }
        };
    }

    @Data
    @Builder
    private static final class CheckerDTO {
        private Project project;
        private PsiMethod method;
        private PsiParameter[] signParameters;
        private PsiExpression[] psiExpressions;
        private int columnIndex;
        private ProblemsHolder holder;
        private boolean isOnTheFly;
        private PsiMethodCallExpression expression;
    }

    private static final class LambdaLocalQuickFix implements LocalQuickFix {

        @Override
        public @IntentionFamilyName @NotNull String getFamilyName() {
            return InspectionBundle.message("inspection.platform.service.dao.problem.lambda.use.quickfix_rename");
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement psiElement = descriptor.getPsiElement();
            if (!(psiElement instanceof PsiLambdaExpression)) {
                return;
            }

            PsiParameter[] parameters = ((PsiLambdaExpression) psiElement).getParameterList().getParameters();
            if (parameters.length < 1) {
                return;
            }
            PsiType type = parameters[0].getType();
            PsiMethodCallExpression psiMethodCallExpression = PsiTreeUtil.findChildOfType(psiElement, PsiMethodCallExpression.class);
            if (psiMethodCallExpression == null) {
                return;
            }

            PsiMethod method = psiMethodCallExpression.resolveMethod();
            if (method == null) {
                return;
            }
            String methodName = method.getName();
            String expressionStr = type.getPresentableText() + "::" + methodName;
            PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
            PsiExpression expressionFromText = elementFactory.createExpressionFromText(expressionStr, null);
            psiElement.replace(expressionFromText);
        }
    }
}
