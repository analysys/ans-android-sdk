#aar的maven包
./gradlew clean
./gradlew delRelease
#./gradlew ans-sdk:analysys_core:uploadArchives
#./gradlew ans-sdk:analysys_encryption:uploadArchives
#./gradlew ans-sdk:analysys_push:uploadArchives
#./gradlew ans-sdk:analysys_visual:uploadArchives
#./gradlew ans-sdk:analysys_allgro:uploadArchives
#/gradlew ans-sdk:analysys_arkanalysys:uploadArchives
./gradlew uploadArchives

#aar和jar包
./gradlew releaseMapping
./gradlew makeJar