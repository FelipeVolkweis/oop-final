all:
	javac src/*.java -d build

runjava: all
	java -cp build Main

runpython:
	python src/server.py