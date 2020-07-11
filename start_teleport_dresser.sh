#!/bin/sh
sleep 600
cd /home/pi/workspace/FukuyamaWB4PiPy
python3 tcp_server_ex1.py&
java -jar FukuyamaWB4Pi.jar -nw
