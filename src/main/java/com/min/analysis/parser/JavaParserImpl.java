package com.min.analysis.parser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.min.analysis.entity.ClassInfo;
import com.min.analysis.entity.ClassVariable;
import com.min.analysis.util.Constants;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

@Log4j2
public class JavaParserImpl {
    ArrayList<ClassInfo> mClasses;
    String mFilepath;
    public JavaParserImpl() {
        mClasses = new ArrayList<>();
    }

    public ArrayList<ClassInfo> parse(String filePath) {
        log.error("javaparser parse start");
        File file = new File("." + filePath);
        mFilepath = filePath;
        mClasses.clear();
        FileInputStream in;
        ParseResult<CompilationUnit> cu;

        try {
            in = new FileInputStream(file);
            JavaParser parser = new JavaParser();
            cu = parser.parse(in);
            new ClassVisitor().visit(cu.getResult().get(), null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        log.error("javaparser parse end");
        return mClasses;
    }

    private class ClassVisitor extends VoidVisitorAdapter {
        @Override
        public void visit(ClassOrInterfaceDeclaration n, Object arg) {
            ClassInfo info = ClassInfo.builder()
                    .className(n.getNameAsString())
                    .filePlace(mFilepath)
                    .variables(new ArrayList<>())
                    .instance(Constants.TEMPLATE)
                    .build();

            if(n.getExtendedTypes().size() > 0) {
                ClassVariable variable = ClassVariable.builder()
                        .type(n.getExtendedTypes(0).asString())
                        .name("<PARENT>")
                        .classInfo(info)
                        .isStatic(false)
                        .build();
                info.getVariables().add(variable);
            }

            for(FieldDeclaration field : n.getFields()) {
                boolean aStatic = field.isStatic();
                String type = field.getElementType().asString();

                if(field.toString().contains("[]")) {
                    type += "[]";
                }
                NodeList<VariableDeclarator> vars = field.getVariables();
                for(VariableDeclarator var : field.getVariables()) {
                    String value = "";
                    if(var.getInitializer().isPresent()) {
                        Expression val = var.getInitializer().get();
                        if(val.isDoubleLiteralExpr() || val.isStringLiteralExpr() || val.isIntegerLiteralExpr()) {
                            value = val.toString();
                        }
                    }

                    info.getVariables().add(ClassVariable.builder()
                                    .isStatic(aStatic)
                                    .type(type)
                                    .name(var.getNameAsString())
                                    .value(value)
                                    .classInfo(info)
                                    .build()
                    );
                }
            }

            mClasses.add(info);
            super.visit(n, arg);
        }
    }
}
