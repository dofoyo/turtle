rem
mvn package -Dmaven.test.skip && java -jar -Dspring.profiles.active=dev2 target/turtle-0.0.1-SNAPSHOT.jar