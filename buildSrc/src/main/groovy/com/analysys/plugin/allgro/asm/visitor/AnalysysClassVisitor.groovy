package com.analysys.plugin.allgro.asm.visitor

import com.analysys.plugin.allgro.AnalysysASMTransform
import com.analysys.plugin.allgro.ClassChecker
import com.analysys.plugin.allgro.asm.AnalysysHookConfig
import com.analysys.plugin.allgro.asm.AnalysysMethodCell
import org.objectweb.asm.*

/**
 * Description: ASM ClassVisitor
 * Author: fengzeyuan
 * Date: 2019-10-18 15:43
 * Version: 1.0
 */
class AnalysysClassVisitor extends ClassVisitor implements Opcodes {

    // 检测者
    ClassChecker mClassChecker

    private int mVer
    String[] mInterfaces
    String mClassName
    private String mSuperName

    // fragment相关
    boolean isFragmentClass// 判断是否为fragment直接子类
    boolean hasIgnorePvAnn// 注解忽略采集PV
    boolean hasTrackPvAnn// 注解自动采集PV
    Map<String, AnalysysMethodCell> mFragmentMethods = new HashMap<>()

    // 点击相关
    boolean hasIgnoreClickAnnOnClass// 注解忽略采集点击
    boolean hasTrackClickAnnOnClass// 注解自动采集点击

    // Lambda 表达式
    Map<String, AnalysysMethodCell> mLambdaMethodCells = new HashMap()

    AnalysysClassVisitor(final ClassVisitor classVisitor, ClassChecker checker) {
        super(Opcodes.ASM6, classVisitor)
        this.mClassChecker = checker
    }

    /**
     * 开始遍历字节码类
     * @param version java 版本
     * @param access 修饰符ACC
     * @param name 类的全路径名
     * @param signature 签名相关
     * @param superName 父类全路径
     * @param interfaces 父接口们
     */
    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces)
        mVer = version

        mClassName = name
        mSuperName = superName
        mInterfaces = interfaces
        super.visit(version, access, name, signature, superName, interfaces)
        isFragmentClass = ClassChecker.isFragment(mSuperName)
        if (isFragmentClass) {
            mFragmentMethods.putAll(AnalysysHookConfig.PV_METHODS)
        }
    }


    @Override
    AnnotationVisitor visitAnnotation(String desc, boolean visible) {
//        println("类注解：desc=${desc}")
        if (desc == 'Lcom/analysys/allegro/annotations/AnalysysIgnorePage;') {
            hasIgnorePvAnn = true
        } else if (desc == 'Lcom/analysys/allegro/annotations/AnalysysAutoPage;') {
            hasTrackPvAnn = true
        } else if (desc == 'Lcom/analysys/allegro/annotations/AnalysysIgnoreTrackClick;') {
            hasIgnoreClickAnnOnClass = true
        } else if (desc == 'Lcom/analysys/allegro/annotations/AnalysysAutoTrackClick;') {
            hasTrackClickAnnOnClass = true
        }
        return super.visitAnnotation(desc, visible)
    }

    /**
     * 结束class类的遍历
     */
    @Override
    void visitEnd() {
        super.visitEnd()
        if (!hasIgnorePvAnn && isFragmentClass) {
            mFragmentMethods.each {
                k, AnalysysMethodCell methodCell ->
                    def pvAnn = hasTrackPvAnn ? ICONST_1 : ICONST_0
                    methodCell.createMethodAndHook(cv, mSuperName, [pvAnn])
            }
        }
    }

    @Override
    FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        if (mClassChecker.isDataAnalysysAPI) {
            if ('SDK_VER' == name) {
                String version = (String) value
//                println("SDK_VER=${version}")
                if (AnalysysASMTransform.MIN_SDK_VER > version) {
                    String errMessage = "当前 SDK 版本 v${version}，请升级到 v${AnalysysASMTransform.MIN_SDK_VER} 及以上版本。"
                    Logger.error(errMessage)
                    throw new Error(errMessage)
                }
            } else if ('MIN_PLUGIN_VER' == name) {
                String minPluginVersion = (String) value
//                println("MIN_PLUGIN_VER=${minPluginVersion}")
                if (minPluginVersion != "" && minPluginVersion != null) {
                    String ver = AnalysysASMTransform.PLUGIN_VER
                    if (AnalysysASMTransform.PLUGIN_VER < minPluginVersion) {
                        String errMessage = "当前集成的易观方舟统计插件版本号为 v${ver}，请升级到 v${minPluginVersion} 及以上版本。"
                        Logger.error(errMessage)
                        throw new Error(errMessage)
                    }
                }
            }
        }
        return super.visitField(access, name, descriptor, signature, value)
    }


    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions)
        return mv == null ? null : new AnalysysMethodVisitor(this, mv, access, name, desc, signature, exceptions)
    }
}