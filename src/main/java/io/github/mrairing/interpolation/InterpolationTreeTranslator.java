package io.github.mrairing.interpolation;

import com.sun.source.tree.Tree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Log;
import io.github.mrairing.interpolation.annotations.Interpolated;

import javax.tools.JavaFileObject;

class InterpolationTreeTranslator extends TreeTranslator {

    private final TreeMaker treeMaker;
    private final Log log;
    private final TaskEvent event;

    InterpolationTreeTranslator(JavacTask task, TaskEvent event) {
        this.event = event;
        Context context = ((BasicJavacTask) task).getContext();
        log = Log.instance(context);

        treeMaker = TreeMaker.instance(context);
    }

    @Override
    public void visitApply(JCTree.JCMethodInvocation tree) {
        Symbol.MethodSymbol methodSymbol = getMethodSymbol(tree.getMethodSelect());

        if (methodSymbol != null) {
            transform(tree, methodSymbol);
        }

        super.visitApply(tree);
    }

    private Symbol.MethodSymbol getMethodSymbol(JCTree.JCExpression methodSelect) {
        final Symbol symbol = TreeInfo.symbol(methodSelect);

        if (symbol == null || !(symbol instanceof Symbol.MethodSymbol)) {
            return null;
        }

        return ((Symbol.MethodSymbol) symbol);
    }

    private void transform(JCTree.JCMethodInvocation tree, Symbol.MethodSymbol symbol) {
        List<Symbol.VarSymbol> parameters = symbol.getParameters();

        if (parameters.nonEmpty() && isAnnotatedWithInterpolated(parameters.get(0))) {
            List<JCTree.JCExpression> arguments = tree.getArguments();

            if (checkForCorrectUsage(arguments) && arguments.size() == 1) {
                makeTransform(tree, arguments);
            }
        }
    }

    private void makeTransform(JCTree.JCMethodInvocation tree, List<JCTree.JCExpression> arguments) {
        JCTree.JCExpression argument = arguments.get(0);

        String strArg = (String) ((JCTree.JCLiteral) argument).getValue();

        String newStrArg = "'" + strArg + "'";

        JCTree.JCLiteral newArg = treeMaker.Literal(newStrArg);

        tree.args = arguments.append(newArg);

        System.out.println("new args: " + tree.args);
    }

    private boolean isAnnotatedWithInterpolated(Symbol.VarSymbol parameter) {
        return parameter.getAnnotation(Interpolated.class) != null;
    }

    private boolean checkForCorrectUsage(List<JCTree.JCExpression> arguments) {
        return checkFirstArgumentIsStringLiteral(arguments);
    }

    private boolean checkFirstArgumentIsStringLiteral(List<JCTree.JCExpression> arguments) {
        JCTree.JCExpression firstArgument = arguments.get(0);

        if (firstArgument.getKind() != Tree.Kind.STRING_LITERAL) {
            logError("The first argument of the interpolation method must be a string literal", firstArgument);

            return false;
        }

        return true;
    }

    private void logError(String message, JCTree tree) {
        JavaFileObject oldSource = log.useSource(event.getSourceFile());

        try {
            log.error(tree.pos(), "proc.messager", message);
        } finally {
            log.useSource(oldSource);
        }
    }
}
