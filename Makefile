all:
	javac client/arquivos/src/com/gui/*.java -d client/arquivos/out/production/arquivos
	cp client/arquivos/src/com/gui/resources -r client/arquivos/out/production/arquivos/com/gui/resources

run-client: all
	java -cp client/arquivos/out/production/arquivos com.gui.Main

run-server:
	chmod 777 server/lib/linux
	python server/src/server.py

clean:
	rm -rf client/arquivos/out/