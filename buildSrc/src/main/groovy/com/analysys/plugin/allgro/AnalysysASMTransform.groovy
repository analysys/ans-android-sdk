package com.analysys.plugin.allgro

import com.analysys.plugin.allgro.asm.visitor.AnalysysClassVisitor
import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import groovy.io.FileType
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

/**
 * Description:ASM 代码编织任务类
 * Author: fengzeyuan
 * Date: 2019-10-18 15:43
 * Version: 1.0
 */
class AnalysysASMTransform extends Transform {

    public static final String PLUGIN_VER = "1.0.0"
    public static final String MIN_SDK_VER = "4.3.7"

    // plugin 所需要的name
    private AnalysysExtension mExtension

    AnalysysASMTransform(AnalysysExtension extension) {
        mExtension = extension
    }

    @Override
    String getName() {
        return 'Analysys_ASMTransform'
    }

    /**
     * 需要处理的数据类型，有两种枚举类型
     * CLASSES 代表处理的 java 的 class 文件，RESOURCES 代表要处理 java 的资源
     * @return
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        //固定写法
        return TransformManager.CONTENT_CLASS
    }

    /**
     * Scope 有 7 种类型：
     * 1. EXTERNAL_LIBRARIES        只有外部库
     * 2. PROJECT                   只有项目内容
     * 3. PROJECT_LOCAL_DEPS        只有项目的本地依赖(本地jar)
     * 4. PROVIDED_ONLY             只提供本地或远程依赖项
     * 5. SUB_PROJECTS              只有子项目。
     * 6. SUB_PROJECTS_LOCAL_DEPS   只有子项目的本地依赖项(本地jar)。
     * 7. TESTED_CODE               由当前变量(包括依赖项)测试的代码
     * @return
     */
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        // Scope.PROJECT, Scope.SUB_PROJECTS, Scope.EXTERNAL_LIBRARIES
        return TransformManager.SCOPE_FULL_PROJECT
    }

/**
 * 设置是否支持增量编译（暂时不支持）
 * @return
 */
    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
//        println("----------执行 ASM 插件在${mProject.name}项目中----------")
        ClassChecker.extension = mExtension
        // 申明变量
        def inputs = transformInvocation.inputs
        def outputProvider = transformInvocation.outputProvider
        def context = transformInvocation.context

        // 非增量编译时，删除缓存
        if (!incremental) {
            outputProvider.deleteAll()
        }

        /**Transform 的 inputs 有两种类型，一种是目录，一种是 jar 包，要分开遍历 */
        
        inputs.each { TransformInput input ->
            

//            println("----------TransformInput start---------")

            /**遍历 jar*/
            input.jarInputs.each { JarInput jarInput ->
                String destName = jarInput.file.name
//                println("jardir=${jarInput.file.path}")
                /**截取文件路径的 md5 值重命名输出文件,因为可能同名,会覆盖*/
                def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath).substring(0, 8)
                /** 获取 jar 名字*/
                if (destName.endsWith(".jar")) {
                    destName = destName.substring(0, destName.length() - 4)
                }

                /** 获得输出文件*/
                File dest = outputProvider.getContentLocation(destName + "_" + hexName, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                // 修改jar中的字节码（关键代码入口）
                def modifiedJar = modifyJar(jarInput.file, context.getTemporaryDir(), true)
                if (modifiedJar == null) {
                    modifiedJar = jarInput.file
                }
                // 替换jar
                FileUtils.copyFile(modifiedJar, dest)
            }

            /**遍历目录*/
            input.directoryInputs.each { DirectoryInput directoryInput ->
                /**当前这个 Transform 输出目录*/
                File dir = directoryInput.file
                if (dir) {
//                    println("遍历文件夹${dir}")
                    HashMap<String, File> modifyMap = new HashMap<>()
                    /**遍历以某一扩展名结尾的文件*/
                    dir.traverse(type: FileType.FILES, nameFilter: ~/.*\.class/) {
                        File classFile ->
                            // 修改字节码文件（关键代码入口）
                            File modified = modifyClassFile(dir, classFile, context.getTemporaryDir())
                            if (modified != null) {
                                String ke = classFile.absolutePath.replace(dir.absolutePath, "")
                                // 将修改的字节码存在map内存中
                                modifyMap.put(ke, modified)
                            }
                    }

                    // 创建目标目录
                    File dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                    // 将源文件拷贝一份过去
                    FileUtils.copyDirectory(directoryInput.file, dest)
                    modifyMap.entrySet().each {
                        Map.Entry<String, File> en ->
                            // 若发现原字节码文件被修改
                            File target = new File(dest.absolutePath + en.getKey())
                            if (target.exists()) {
                                // 使用修改的字节码文件替换原字节码文件
                                FileUtils.copyFile(en.getValue(), target)
                            }
                            // 删除当前字节码修改文件
                            en.getValue().delete()
                    }
                }
            }

//            println("----------TransformInput end---------\n\n")
        }
    }

    /**
     * 修改jar中的字节码
     * @param srcFile
     * @param tempDir
     * @param isNameHex 是都需要hexName
     * @return
     */
    static File modifyJar(File srcFile, File tempDir, boolean isNameHex) {

        /**
         * 设置输出到的 jar
         */
        def hexName = ""
        if (isNameHex) {
            hexName = DigestUtils.md5Hex(srcFile.absolutePath).substring(0, 8)
        }
        def outputJar = new File(tempDir, hexName + srcFile.name)
        JarOutputStream jarOutIO = new JarOutputStream(new FileOutputStream(outputJar))


        //  读取原 jar
        def jarFile = new JarFile(srcFile, false)
        try {
            Enumeration enumeration = jarFile.entries()
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement()
                String entryName = jarEntry.getName()
                if (entryName.endsWith(".DSA") || entryName.endsWith(".SF")) {
                    //ignore
                } else {
                    // 将jar中的字节码元素流转字二进制数组
                    InputStream inputStream = null
                    try {
                        inputStream = jarFile.getInputStream(jarEntry)
                        byte[] sourceClassBytes = IOUtils.toByteArray(inputStream)

                        // 打开一个新的jar元素空间
                        JarEntry entry = new JarEntry(entryName)
                        jarOutIO.putNextEntry(entry)

                        // 执行最原来这个jar元素的字节码修改
                        byte[] modifiedClassBytes = null
                        if (entryName.endsWith(".class")) {
                            // 匹配以.class结尾的jar元素
                            String classFullName = entryName.replace("/", ".")
                            ClassChecker checker = ClassChecker.create(classFullName)
                            if (checker.isShouldModify()) {
                                // 命中修改，则修改对应的字节码二位数组
//                                println("修改jar字节码：${classFullName}")
                                modifiedClassBytes = modifyClassBytes(sourceClassBytes, checker)
                            }
                        }

                        // 如果没有修改，就使用原来的元素
                        if (modifiedClassBytes == null) {
                            modifiedClassBytes = sourceClassBytes
                        }
                        jarOutIO.write(modifiedClassBytes)
                    } finally {
                        IOUtils.closeQuietly(inputStream)
                        jarOutIO.closeEntry()
                    }
                }
            }
        } finally {
            jarOutIO.close()
            jarFile.close()
        }

        return outputJar
    }

    /**
     * 修改文件中的字节码
     * @param dir
     * @param classFile
     * @param tempDir
     * @return
     */
    static File modifyClassFile(File dir, File classFile, File tempDir) {
        File modified = null
        try {
            // 将path统一转换为命名空间方式
            String className = classFile.absolutePath
                    .replace(dir.absolutePath + File.separator, "")
                    .replace(File.separator, ".")

            ClassChecker checker = ClassChecker.create(className)
            if (checker.isShouldModify()) {
                // println("修改文件夹${classFile.absolutePath}中\n字节码：${classFullName}")

                // 将文件转字节码，存于内存中
                byte[] sourceClassBytes = IOUtils.toByteArray(new FileInputStream(classFile))
                // 修改字节码文件的二进制数组
                byte[] modifiedClassBytes = modifyClassBytes(sourceClassBytes, checker)

                if (modifiedClassBytes) {// 当修改成功
                    // 先创建缓文件容器
                    modified = new File(tempDir, className.replace('.', '') + '.class')
                    if (modified.exists()) {
                        modified.delete()
                    }
                    modified.createNewFile()
                    // 将二进制文件写入
                    new FileOutputStream(modified).write(modifiedClassBytes)
                }
            }
        } catch (Exception e) {
            e.printStackTrace()
            modified = classFile
        }
        return modified
    }


    /**
     * 修改字节码
     * @param srcClass
     * @return
     * @throws IOException
     */
    private static byte[] modifyClassBytes(byte[] srcClass, ClassChecker checker) throws IOException {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS)
        ClassReader cr = new ClassReader(srcClass)
        // 创建class遍历器
        ClassVisitor classVisitor = new AnalysysClassVisitor(cw, checker)
        cr.accept(classVisitor, ClassReader.SKIP_FRAMES)
        // 写出新的字节码二进制类
        return cw.toByteArray()
    }


}