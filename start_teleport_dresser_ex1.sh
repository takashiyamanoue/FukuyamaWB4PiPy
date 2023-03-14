#!/bin/sh
sleep 300
export DISPLAY=:0
cd /home/pi/workspace/FukuyamaWB4PiPy
python3 tcp_server_ex1.py&
/usr/bin/java -jar FukuyamaWB4Pi.jar -nw
