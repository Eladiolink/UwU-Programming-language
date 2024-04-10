build:
	./gradlew build ;java -jar build/libs/UwU-1.0-SNAPSHOT.jar test.uwu
run:
	./gradlew run --args="test.uwu"
build-non-file:
	./gradlew build ;java -jar build/libs/UwU-1.0-SNAPSHOT.jar
run-non-file:
	./gradlew run
