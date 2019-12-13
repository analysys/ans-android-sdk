package com.analysys.plugin.allgro.asm


import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

/**
 * Description:方法信息JavaBean
 * Author: fengzeyuan
 * Date: 2019-10-18 15:43
 * Version: 1.0
 */
class AnalysysMethodCell implements Opcodes {
    // ------------------ 特征方法参数 ------------------

    /**
     * 持有方法的接口或类(非必填)
     */
    String mOwner


    /**
     * 原方法名
     */
    String mName
    /**
     * 原方法描述
     */
    String mDesc

    /**
     * 原方法参数指令
     */
    List<Integer> mOpcodes

    // ------------------ 插入方法特征参数 ------------------

    /**
     * 采集数据的方法名
     */
    String mAgentName
    /**
     * 采集数据的方法描述
     */
    String mAgentDesc


    /**
     * 采集数据的方法参数起始索引（ 0：this，1+：普通参数 ）
     */
    int mParamsStart


    AnalysysMethodCell(String owner, String name, String desc, int paramsStart, List<Integer> opcodes, String agentName, String agentDesc) {
        this.mOwner = owner

        this.mName = name
        this.mDesc = desc
        this.mParamsStart = paramsStart
        this.mOpcodes = opcodes

        this.mAgentName = agentName
        this.mAgentDesc = agentDesc
    }

    MethodVisitor createMethodAndHook(ClassVisitor cv, String owner, List<Integer> agentOpcodeExt) {
        MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, mName, mDesc, null, null)
        mv.visitCode()
        visitMethodWithLoadedParams(mv, INVOKESPECIAL, owner, mName, mDesc, mParamsStart, mOpcodes)
        hookMethod(mv, agentOpcodeExt)
        mv.visitInsn(RETURN)
        int max = mOpcodes.size() + agentOpcodeExt.size()
        mv.visitMaxs(max, max)
        mv.visitEnd()
        return mv
    }

    void hookMethod(MethodVisitor mv, List<Integer> agentOpcodeExt) {
        String owner = AnalysysHookConfig.ASM_PROBE_HELP
        mv.visitMethodInsn(INVOKESTATIC, owner, "getInstance", "()L${owner};", false)
        List<Integer> agentOpcodes = new ArrayList<>(mOpcodes)
        agentOpcodes.addAll(agentOpcodeExt)
        visitMethodWithLoadedParams(mv, INVOKEVIRTUAL, owner, mAgentName, mAgentDesc, mParamsStart, agentOpcodes)

//        mv.visitMethodInsn(INVOKESTATIC, owner, "getInstance", "()L${owner};", false)
//        mv.visitVarInsn(ALOAD, 0)
//        mv.visitVarInsn(ALOAD, 1)
//        mv.visitInsn(clickAnn)
//        mv.visitMethodInsn(INVOKEVIRTUAL, owner, lambdaMethodCell.mAgentName, lambdaMethodCell.mAgentDesc, false)
    }

    private static void visitMethodWithLoadedParams(MethodVisitor mv, int opcode, String owner, String methodName, String methodDesc, int start, List<Integer> paramOpcodes) {
        for (int i = start; i < start + paramOpcodes.size(); i++) {
            int paramOpcode = paramOpcodes[i - start]
            if (paramOpcode in NOP..DCONST_1) {
                mv.visitInsn(paramOpcode)
            } else {
                mv.visitVarInsn(paramOpcode, i)
            }
        }
        mv.visitMethodInsn(opcode, owner, methodName, methodDesc, false)
    }


    void hookLambdaMethod(MethodVisitor mv, boolean isStaticMethod, int paramStart, Type[] lambdaTypes, List<Integer> agentOpcodeExt) {
        String owner = AnalysysHookConfig.ASM_PROBE_HELP
        mv.visitMethodInsn(INVOKESTATIC, owner, "getInstance", "()L${owner};", false)
        for (int i = paramStart; i < paramStart + mOpcodes.size(); i++) {
            mv.visitVarInsn(mOpcodes.get(i - paramStart), getVisitPosition(lambdaTypes, i, isStaticMethod))
        }
        for (int i = 0; i < agentOpcodeExt.size(); i++) {
            mv.visitInsn(agentOpcodeExt[i])
        }
        mv.visitMethodInsn(INVOKEVIRTUAL, owner, mAgentName, mAgentDesc, false)
    }

    /**
     * 获取方法参数下标为 index 的对应 ASM index
     * @param types 方法参数类型数组
     * @param index 方法中参数下标，从 0 开始
     * @param isStaticMethod 该方法是否为静态方法
     * @return 访问该方法的 index 位参数的 ASM index
     */
    static int getVisitPosition(Type[] types, int index, boolean isStaticMethod) {
        if (types == null || index < 0 || index >= types.length) {
            throw new Error("getVisitPosition error")
        }
        if (index == 0) {
            return isStaticMethod ? 0 : 1
        } else {
            return getVisitPosition(types, index - 1, isStaticMethod) + types[index - 1].getSize()
        }
    }

}