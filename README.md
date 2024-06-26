# Projeto OOP Final

Este é o README do projeto OOP Final. Aqui estão as instruções de compilação e execução do projeto.

## Pré-requisitos
- Python 3.12.3
- OpenJDK JDK 22.0.1

## Compilação

Para compilar o projeto utilizando Linux e fazendo uso do comando make:
1. 
```
make all
```

Para compilar o projeto sem o uso do comando make, siga as seguintes etapas:

Em linux:
1. Compile os arquivos Java do cliente:
```
javac client/arquivos/src/com/gui/*.java -d client/arquivos/out/production/arquivos
```
2. Copie os recursos para a pasta de build:
```
cp client/arquivos/src/com/gui/resources -r client/arquivos/out/production/arquivos/com/gui/resources
```
3. Dê permissão de execução ao binário de arquivos:
```
chmod 777 server/lib/linux
```

Em Windows:
1. Compile os arquivos Java do cliente:
```
javac client/arquivos/src/com/gui/*.java -d client/arquivos/out/production/arquivos
```
2. Copie os recursos para a pasta de build:
```
robocopy client/arquivos/src/com/gui/resources client/arquivos/out/production/arquivos/com/gui/resources \E
```

## Execução

Para executar o projeto utilizando Linux e fazendo uso do comando make:
1. Para iniciar o servidor digite:
```
make run-server
```
2. Para iniciar a GUI digite:
```
make run-client
```

Caso esteja no Windows ou não queira usar make no Linux:

Para executar o cliente, utilize o seguinte comando:
```
java -cp client/arquivos/out/production/arquivos com.gui.Main
```

Para executar o servidor, utilize o seguinte comando:
```
python server/src/server.py
```

Certifique-se de executar o servidor antes de executar o cliente.
O programa em python possui argumentos de inicialização com as seguintes opções:
  -h, --help            Mostrar a mensagem de help
  -a ADDRESS, --address ADDRESS
                        Endereço do host para o servidor.
  -p PORT, --port PORT  Porta para o servidor.

Nenhum desses argumentos é obrigatório. Por padrão o endereço é 127.0.0.1 e a porta é 8080.

TODOS OS COMANDOS PRECISÃO SER EXECUTADOS A PARTIR DO DIRETÓRIO BASE DO PROJETO.