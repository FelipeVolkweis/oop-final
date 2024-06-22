all:
	javac src/*.java -d build

runjava: all
	java src/Main.java

runpython:
	python src/server.py