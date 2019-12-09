package com.analysys.plugin

import com.analysys.plugin.allgro.AnalysysASMTransform
import com.analysys.plugin.allgro.AnalysysExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project


class AnalysysPlugin implements Plugin<Project> {

    void apply(Project project) {
        final def log = project.logger
        log.error "========================"
        log.error "欢迎使用易观方舟自动埋点插件!"
        log.error "========================"

        // 判断是否为主项目
        if (project.plugins.hasPlugin(AppPlugin)) {
//            println("------------------注册了ASMTransform----------------------");
            def android = project.extensions.findByName("android")
            AnalysysExtension extension = project.extensions.create("analysysConfig", AnalysysExtension)
            android.registerTransform(new AnalysysASMTransform(extension))
        }
    }
}