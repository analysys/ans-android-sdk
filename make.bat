
gradlew.bat clean
gradlew.bat delRelease
gradlew.bat analysys_core:uploadArchives
gradlew.bat analysys_encryption:uploadArchives
gradlew.bat analysys_push:uploadArchives
gradlew.bat analysys_visual:uploadArchives
gradlew.bat analysys_allgro:uploadArchives
gradlew.bat analysys_arkanalysys:uploadArchives
gradlew.bat analysys_arkanalysys_no_op:uploadArchives

echo mpaas
gradlew.bat analysys_mpaas:uploadArchives
echo rn
gradlew.bat analysys_react_native:uploadArchives



gradlew.bat zipMapping
gradlew.bat makeJar


gradlew.bat makeZipAnsClean