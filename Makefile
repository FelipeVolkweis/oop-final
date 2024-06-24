all:
	javac client/arquivos/src/com/gui/*.java -d client/arquivos/out/production/arquivos

run-client: all
	java -cp client/arquivos/out/production/arquivos com.gui.Main

run-server:
	python server/src/server.py