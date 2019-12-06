package com.analysys.plugin.allgro.asm.visitor

import com.analysys.plugin.allgro.ClassChecker
import com.analysys.plugin.allgro.asm.AnalysysHookConfig
import com.analysys.plugin.allgro.asm.AnalysysMethodCell
import org.objectweb.asm.*
import org.objectweb.asm.commons.AdviceAdapter

/**
 * Description: ASM MethodVisitor
 * Author: fengzeyuan
 * Date: 2019-10-18 15:43
 * Version: 1.0
 */
class AnalysysMethodVisitor extends AdviceAdapter {

    private AnalysysClassVisitor mCv
    int mAccess
    String mName
    String mDesc
    String mNameDesc

    String mSignature
    String[] mExceptions

    //访问权限是public并且非静态
    boolean pubAndNoStaticAcc
    private boolean hasIgnoreClickAnnOnMethod// 注解忽略采集点击
    private boolean hasTrackClickAnnOnMethod// 注解自动采集点击


/**
 *
 * @param mv
 * @param access
 * @param name
 * @param desc
 */
    AnalysysMethodVisitor(AnalysysClassVisitor cv, MethodVisitor mv, int access, String name, String desc, String signature, String[] exceptions) {
        super(Opcodes.ASM6, mv, access, name, desc)
        mCv = cv
        mAccess = access
        mName = name
        mDesc = desc
        mSignature = signature
        mExceptions = exceptions
    }

    /**
     * 表示 ASM 开始扫描这个方法
     */
    @Override
    void visitCode() {
        super.visitCode()
    }

    @Override
    protected void onMethodEnter() {
        super.onMethodEnter()
        mNameDesc = mName + mDesc
        pubAndNoStaticAcc = ClassChecker.isPublic(mAccess) && !ClassChecker.isStatic(mAccess)
    }


    @Override
    void visitInvokeDynamicInsn(String name1, String desc1, Handle bsm, Object... bsmArgs) {
        super.visitInvokeDynamicInsn(name1, desc1, bsm, bsmArgs)
        if (!ClassChecker.mExtension.lambdaEnabled) {
            return
        }

        try {
            String owner = Type.getReturnType(desc1).getDescriptor()
            String desc2 = (String) bsmArgs[0]
            String nameDesc = name1 + desc2
            AnalysysMethodCell methodCell = AnalysysHookConfig.CLICK_METHODS_LAMBDA.get(owner + nameDesc)
            Handle it = (Handle) bsmArgs[1]
            if (methodCell != null) {
                mCv.mLambdaMethodCells.put(it.name + it.desc, methodCell)
            } else if ('onNavigationItemSelected(Landroid/view/MenuItem;)Z' == nameDesc) {
                methodCell = AnalysysHookConfig.CLICK_METHODS.get(nameDesc)
                if (methodCell != null) {
                    mCv.mLambdaMethodCells.put(it.name + it.desc, methodCell)
                }
            }
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    @Override
    AnnotationVisitor visitAnnotation(String s, boolean b) {
//        println("在方法上发现注解${s}")
        if (s == 'Lcom/analysys/allgro/annotations/AnalysysIgnoreTrackClick;') {
            hasIgnoreClickAnnOnMethod = true
        } else if (s == 'Lcom/analysys/allgro/annotations/AnalysysAutoTrackClick;') {
            hasTrackClickAnnOnMethod = true
        }
        return super.visitAnnotation(s, b)
    }


    @Override
    protected void onMethodExit(int opcode) {
        super.onMethodExit(opcode)

        if (ClassChecker.mExtension.lambdaEnabled) {
            AnalysysMethodCell methodCell = mCv.mLambdaMethodCells.get(mNameDesc)
            if (methodCell != null) {
                def clickAnn = mCv.hasTrackClickAnnOnClass || hasTrackClickAnnOnMethod ? ICONST_1 : ICONST_0
                Type[] types = Type.getArgumentTypes(methodCell.mDesc)
                int length = types.length
                Type[] lambdaTypes = Type.getArgumentTypes(mDesc)
                // 方法参数的下标，从 0 开始
                int paramStart = lambdaTypes.length - length
                if (paramStart < 0) {
                    return
                } else {
                    for (int i = 0; i < length; i++) {
                        if (lambdaTypes[paramStart + i].descriptor != types[i].descriptor) {
                            return
                        }
                    }
                }
                boolean isStaticMethod = ClassChecker.isStatic(mAccess)
                if (methodCell.mOwner) {
                    methodCell.hookLambdaMethod(mv, isStaticMethod, paramStart, lambdaTypes, [clickAnn])
                } else if (methodCell.mDesc == '(Landroid/view/MenuItem;)Z') {
                    mv.visitVarInsn(ALOAD, 0)
                    mv.visitVarInsn(ALOAD, AnalysysMethodCell.getVisitPosition(lambdaTypes, paramStart, isStaticMethod))
                    mv.visitInsn(clickAnn)
                    mv.visitMethodInsn(INVOKESTATIC, AnalysysHookConfig.ASM_PROBE_HELP, methodCell.mAgentName, methodCell.mAgentDesc, false)
                }
                return
            }
        }

        if (!pubAndNoStaticAcc) {
            return
        }

        if (!mCv.hasIgnorePvAnn && mCv.isFragmentClass) {
            // 统计 Fragment PV
            AnalysysMethodCell methodCell = mCv.mFragmentMethods.get(mNameDesc)
            if (methodCell != null) {
                mCv.mFragmentMethods.remove(mNameDesc)
                def pvAnn = mCv.hasTrackPvAnn ? ICONST_1 : ICONST_0
                methodCell.hookMethod(mv, [pvAnn])
                return
            }
        }

        if (canHookClickMethod()) {
            AnalysysMethodCell methodCell = AnalysysHookConfig.CLICK_METHODS.get(mNameDesc)
            def clickAnn = mCv.hasTrackClickAnnOnClass || hasTrackClickAnnOnMethod ? ICONST_1 : ICONST_0
            if (methodCell != null) {
                methodCell.hookMethod(mv, [clickAnn])
            } else {
                if (mNameDesc == 'onDrawerOpened(Landroid/view/View;)V'
                        || mNameDesc == 'onDrawerClosed(Landroid/view/View;)V') {
                    // DrawerLayout
                    mv.visitVarInsn(ALOAD, 1)
                    mv.visitInsn(mNameDesc.contains('Opened') ? ICONST_1 : ICONST_0)
                    mv.visitInsn(clickAnn)
                    mv.visitMethodInsn(INVOKESTATIC, AnalysysHookConfig.ASM_PROBE_HELP, 'trackDrawerSwitch', '(Landroid/view/View;ZZ)V', false)
                } else if (ClassChecker.mExtension.checkXMLOnClick && mDesc.startsWith('(Landroid/view/View;)')) {
                    // 检测是否是XML中绑定的点击
                    methodVisitor.visitVarInsn(ALOAD, 1)
                    mv.visitLdcInsn(mName)
                    mv.visitInsn(clickAnn)
                    mv.visitMethodInsn(INVOKEVIRTUAL, AnalysysHookConfig.ASM_PROBE_HELP, 'maybeClickInXML', '(Landroid/view/View;Ljava/lang/String;Z)V', false)
                }
            }
        }
    }

    /**
     * 表示方法输出完毕
     */
    @Override
    void visitEnd() {
        super.visitEnd()
        if (canHookClickMethod()) {
            if (ClassChecker.mExtension.lambdaEnabled && mCv.mLambdaMethodCells.containsKey(mNameDesc)) {
                mCv.mLambdaMethodCells.remove(mNameDesc)
            }
        }
    }


    private boolean canHookClickMethod() {
        if (hasIgnoreClickAnnOnMethod) {
            // 方法级别注解，忽略点击上报
            return false
        } else if (mCv.hasIgnoreClickAnnOnClass && !hasTrackClickAnnOnMethod) {
            // 页面级别注解，忽略点击上报
            return false
        }
        return true
    }
}