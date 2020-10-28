import socket
import threading
import subprocess
 
HOST = ''
PORT = 9998
clients = []
 
 
def remove_conection(con, address):
    """クライアントと接続を切る"""
 
    print('[切断]{}'.format(address))
    con.close()
    clients.remove((con, address))
 
 
def server_start():
    """サーバをスタートする"""
 
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.bind((HOST, PORT))
    sock.listen(10)
    subprocess.Popen("python3 /home/pi/workspace/FukuyamaWB4PiPy/teleport_dress_ex1.py",shell=True)
    while True:
        con, address = sock.accept()
        print("[接続]{}".format(address))
        clients.append((con, address))
        handle_thread = threading.Thread(target=handler,
                                         args=(con, address),
                                         daemon=True)
        handle_thread.start()
 
 
def handler(con, address):
    """クライアントからデータを受信する"""
 
    while True:
        try:
            data = con.recv(1024)
        except ConnectionResetError:
            remove_conection(con, address)
            break
        else:
            if not data:
                remove_conection(con, address)
                break
            else:
                print("[受信]{} - {}".format(address, data.decode("utf-8")))
                for c in clients:
                    if c[0]!=con:
                        while data:
                          n = c[0].sendto(data,c[1])    
                          data = data[n:]
                       
if __name__ == "__main__":
    server_start()
