package com.analysys.plugin.allgro

import org.objectweb.asm.Opcodes

/**
 * Class检查类
 */
class ClassChecker {

    static AnalysysExtension mExtension = new AnalysysExtension()

    /** 安卓包中特殊需要的类 */
    private static final HashSet<String> specials = ['android.support.design.widget.TabLayout$ViewPagerOnTabSelectedListener',
                                                     'com.google.android.material.tabs.TabLayout$ViewPagerOnTabSelectedListener',
                                                     'android.support.v7.app.ActionBarDrawerToggle',
                                                     'androidx.appcompat.app.ActionBarDrawerToggle']

    /** 需要包含进来的 常用开源+用户定义 */
    private static final HashSet<String> include = ['butterknife.internal.DebouncingOnClickListener',
                                                    'com.jakewharton.rxbinding.view.ViewClickOnSubscribe']

    /** 需要忽略的的 安卓原生+常用开源+用户定义 */
    private static final HashSet<String> ignore = ['android.support',
                                                   'androidx',
                                                   'android.arch',
                                                   'com.google.android',
                                                   'com.bumptech.glide.manager.SupportRequestManagerFragment']


    private static final String ANALYSYS_API = 'com.analysys.AnalysysAgent.class'

    static ClassChecker create(String className) {
        return new ClassChecker(className)
    }

    public String className  // 全类名
    boolean isDataAnalysysAPI = false // 是不是易观方舟API

    private ClassChecker(String className) {
        this.className = className
        isDataAnalysysAPI = (className == ANALYSYS_API)
    }

    private boolean isSDKFile() {
        return isDataAnalysysAPI
    }


    boolean isShouldModify() {
        if (isSDKFile()) {
            return isDataAnalysysAPI
        } else if (!isAndroidGenerated()) {
            // 特殊包名
            for (pkgName in specials) {
                if (className.startsWith(pkgName)) {
                    return true
                }
            }

            // 包含的
            for (pkgName in include) {
                if (className.startsWith(pkgName)) {
                    return true
                }
            }

            // 忽略的
            for (pkgName in ignore) {
                if (className.startsWith(pkgName)) {
                    return false
                }
            }
            return true
        }
        return false
    }

    private boolean isAndroidGenerated() {
        return className.contains('R$') ||
                className.contains('R2$') ||
                className.contains('R.class') ||
                className.contains('R2.class') ||
                className.contains('BuildConfig.class')
    }


    static final void setExtension(AnalysysExtension extension) {
        mExtension = extension
        addInclude(extension.includePackage)
        addIgnore(extension.ignorePackage)
    }


    private static final void addInclude(Set<String> userInclude) {
        if (userInclude != null) {
            include.addAll(userInclude)
        }
    }

    private static final void addIgnore(Set<String> userIgnore) {
        if (userIgnore != null) {
            ignore.addAll(userIgnore)
        }
    }

    // ------------------------------- class for ASM ----------------------------

    private static final HashSet<String> tagFragmentClass = new HashSet()

    static {

        /**
         * For Android App Fragment
         */
        tagFragmentClass.add('android/app/Fragment')
        tagFragmentClass.add('android/app/ListFragment')
        tagFragmentClass.add('android/app/DialogFragment')

        /**
         * For Support V4 Fragment
         */
        tagFragmentClass.add('android/support/v4/app/Fragment')
        tagFragmentClass.add('android/support/v4/app/ListFragment')
        tagFragmentClass.add('android/support/v4/app/DialogFragment')

        /**
         * For AndroidX Fragment
         */
        tagFragmentClass.add('androidx/fragment/app/Fragment')
        tagFragmentClass.add('androidx/fragment/app/ListFragment')
        tagFragmentClass.add('androidx/fragment/app/DialogFragment')

    }


    static boolean isFragment(String superName) {
        return superName in tagFragmentClass
    }

    static boolean isPublic(int access) {
        return (access & Opcodes.ACC_PUBLIC) != 0
    }

    static boolean isStatic(int access) {
        return (access & Opcodes.ACC_STATIC) != 0
    }


}