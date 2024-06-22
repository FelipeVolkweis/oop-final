import socket
import subprocess
import os
import struct

def start_server(host='127.0.0.1', port=8080):
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
    current_dir = os.path.dirname(os.path.abspath(__file__))
    exec_path = os.path.normpath(os.path.abspath(os.path.join(current_dir, '..', 'lib', 'exec')))
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

if __name__ == "__main__":
    start_server()
