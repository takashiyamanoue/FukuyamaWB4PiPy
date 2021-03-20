import socket
import threading
import subprocess

class RemoteCommandReader:
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    HOST = 'localhost'
    PORT = 8080
    clients = []

    def __init__(self):
        """start this client"""

        print("start RemoteCommandReader.__init__")
        self.client_start()
        while True:
            try:
                time.sleep(0.01)
            except:
                print("error")

    def client_start(self):
        """start this client"""

        self.sock.connect((self.HOST, self.PORT))
        handle_thread = threading.Thread(target=self.handler, args=(self.sock,))
        handle_thread.start()
        self.send_message(self.sock)

    def handler(self, sock):
        """ receive a message from the server and display it """

        while True:
            data = sock.recv(1024)
            print("[receive]"+str(data.decode("utf-8")))
            line = data.decode("utf-8")

    def send_message(self,sock):
        while True:
            try:
                msg=input()
            except KeyboardInterrupt:
                sock.send(msg.encode('utf-8'))
            try:
                sock.send(msg.encode('utf-8'))
            except ConnectionRefuseError:
                break
            except ConnectionResetError:
                break

def main():
    RemoteCommandReader()

if __name__ == "__main__":
    main()
