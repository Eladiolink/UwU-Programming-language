build:
	./gradlew build ;java -jar build/libs/UwU-1.0-SNAPSHOT.jar $(FILE)
run:
	./gradlew run --args="$(FILE)"
