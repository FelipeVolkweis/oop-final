import socket
import subprocess
import os
import struct
import sys
import argparse

def start_server(host='127.0.0.1', port=8080):
    """
    Inicia o servidor na máquina local com o endereço IP e porta especificados.

    Args:
        host (str, optional): O endereço IP do servidor. O padrão é '127.0.0.1'.
        port (int, optional): A porta do servidor. O padrão é 8080.
    """
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    try:
        s.bind((host, port))
        s.listen()
        print(f"Server started and listening on {host}:{port}")

        while True:
            try:
                conn, addr = s.accept()
                handle_client(conn, addr)
            except KeyboardInterrupt:
                print("\nServer is shutting down.")
                break
            except Exception as e:
                print(f"Error accepting connection: {e}")
    finally:
        s.close()

def handle_client(conn, addr):
    """
    Handles a client connection.

    Args:
        conn (socket): The client socket connection.
        addr (tuple): The client address.

    Returns:
        None

    Raises:
        None
    """
    current_dir = os.path.dirname(os.path.abspath(__file__))

    executable = sys.platform == 'win32' and 'win.exe' or 'linux'

    exec_path = os.path.normpath(os.path.abspath(os.path.join(current_dir, '..', 'lib', executable)))
    data_dir = os.path.normpath(os.path.abspath(os.path.join(current_dir, '..', 'data')))

    with conn:
        print(f"Connected by {addr}")
        try:
            while True:
                raw_msglen = conn.recv(4)
                if not raw_msglen:
                    break
                msglen = struct.unpack('>I', raw_msglen)[0]

                data = conn.recv(msglen)
                if not data:
                    break

                print(f"Received data: {data.decode()}")
                stdin_data = data.decode()

                output = start_child_process(exec_path, stdin_data, data_dir)
                if output is not None:
                    output_len = struct.pack('>I', len(output))
                    conn.sendall(output_len)
                    conn.sendall(output)
        except Exception as e:
            print(f"Client error: {e}")

def start_child_process(exec_path, stdin_data, cwd):
    """
    Inicia um processo filho com o caminho de execução especificado.

    Args:
        exec_path (str): O caminho de execução do processo filho.
        stdin_data (str): Os dados a serem passados para o processo filho através da entrada padrão.
        cwd (str): O diretório de trabalho atual para o processo filho.

    Returns:
        str or None: A saída padrão do processo filho, se houver, caso contrário, None.

    Raises:
        Exception: Se ocorrer um erro ao executar o processo filho.

    """
    command = [exec_path]

    try:
        process = subprocess.Popen(command, cwd=cwd, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        stdout, stderr = process.communicate(input=stdin_data.encode())

        if stderr:
            print(f"Error: {stderr.decode().strip()}")

        return stdout if stdout else None
    except Exception as e:
        print(f"Error running child process: {e}")
        return None

def parse_arguments():
    """
    Função para analisar os argumentos de linha de comando.

    Retorna os argumentos analisados do programa.

    :return: Os argumentos analisados do programa.
    """
    parser = argparse.ArgumentParser(description='Iniciar o servidor socket que conversa com trabalho de arquivos')
    parser.add_argument('-a', '--address', type=str, default='127.0.0.1', help='Endereço do host para o servidor.')
    parser.add_argument('-p', '--port', type=int, default=8080, help='Porta para o servidor.')
    return parser.parse_args()

if __name__ == "__main__":
    args = parse_arguments()
    start_server(host=args.address, port=args.port)
