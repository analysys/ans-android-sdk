#aar的maven包
./gradlew clean
./gradlew delRelease
./gradlew analysys_core:uploadArchives
./gradlew analysys_encryption:uploadArchives
./gradlew analysys_push:uploadArchives
./gradlew analysys_visual:uploadArchives
./gradlew analysys_allgro:uploadArchives
./gradlew analysys_arkanalysys:uploadArchives
./gradlew analysys_arkanalysys_no_op:uploadArchives

#mpaas
./gradlew analysys_mpaas:uploadArchives
#rn
./gradlew analysys_react_native:uploadArchives


#./gradlew uploadArchives

#aar和jar包
./gradlew zipMapping
./gradlew makeJar

#zip包
./gradlew makeZipAnsClean