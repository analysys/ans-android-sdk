#aar的maven包
./gradlew clean
./gradlew delRelease
./gradlew analysys_core:uploadArchives
./gradlew analysys_push:uploadArchives
./gradlew analysys_visual:uploadArchives
./gradlew analysys_allgro:uploadArchives
./gradlew analysys_arkanalysys:uploadArchives
./gradlew analysys_arkanalysys_no_op:uploadArchives



#./gradlew uploadArchives

#aar和jar包
./gradlew zipMapping
./gradlew makeJar

#zip包
./gradlew makeZipAnsClean