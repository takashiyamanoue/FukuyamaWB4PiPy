#!/usr/bin/python2 -x

import socket
import threading
import subprocess

HOST = ''
PORT = 8080
clients = []

def remove_connection(con, address):
	"""dis-connect from the client"""

	print('[discon]-'+str(address))
	con.close()
	clients.remove((con, address))

def server_start():
	""" start this server """
	print("server_start()")

	sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
	sock.bind((HOST, PORT))
	sock.listen(10)
	while True:
		con, address = sock.accept()
		print("[connect]-"+str(address))
		clients.append((con, address))
		handle_thread =threading.Thread(target=handler,
						args=(con, address))

		handle_thread.start()

def handler(con, address):
	""" receive data from the client """
	print("handler_start()")

	while True:
		try:
			data = con.recv(1024)
		except ConnectionResetError:
			remove_connection(con, address)
			break
		else:
			if not data:
				remove_connection(con, address)
				break
			else:
				print("[receive]-"+str(address)+"-"+str(data.decode("utf-8")))
				for c in clients:
					if c[0]!=con:
						while data:
							n = c[0].sendto(data,c[1])
							data = data[n:]

if __name__ == "__main__":
	server_start()

