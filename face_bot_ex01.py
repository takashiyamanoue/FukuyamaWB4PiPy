# coding: utf-8
#
#   
#          +---------+      +--------------+
#  Wiki <->| fwb4pi  |<---->|tcp_server_ex1|
#          +---------+      +--------------+
#                                 ^
#                                 |
#                                 v
#                           +------------------+
#                           |RemoteCommandReder|
#                           |    |             |
#                           |    v             |
#                           |   handler        |
#                           |    |             |
#                           |    v             |
#                           |   parser         |
#                           |    |             |
#                           +----|-------------+
#                                |
#                                | put
#                                v
#                           +------------------+
#                           |TeleportDresser   |
#                           |    |             |
#                           |    v             |
#                           |  handler         |
#                           +------------------+
#                          

from __future__ import division
import smbus
import time
import sys
import requests
import os
import socket
import threading
from collections import deque
import subprocess

import signal
import subprocess
from subprocess import Popen
from time import  sleep
import wiringpi as w
# Simple demo of of the PCA9685 PWM servo/LED controller library.
# This will move channel 0 from min to max position repeatedly.
# Author: Tony DiCola
# License: Public Domain
 
# Import the PCA9685 module.
import Adafruit_PCA9685


class RemoteCommandReader:
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    HOST = 'localhost'
    PORT = 9998
    def __init__(self):
        print("start Servo_Controller.__init__")
        self.client_start()
        self.servo_ctrl=Servo_Controller()
        print(self.servo_ctrl)
        while True:
          try:
            time.sleep(0.01)
          except:
            print("error")

    def python2fwbln(self,py2x_message):
      msg=py2x_message+'\n'
      self.sock.sendall(msg.encode('utf-8'))
      
    def python2fwb(self,py2x_message):
      msg=py2x_message
      self.sock.sendall(msg.encode('utf-8'))

    #while 1:
    #   if isTheSensorOn():
    #  if True:
    #    print("start action")
    #    cmd = ['omxplayer','1.mp3']
    #    proc = subprocess.Popen(cmd)
    #    print( "process id = %s" % proc.pid )
    #    ts=time.time()
    #    tc=time.time()
    #    tr=0.0
    #    move_to_center("ch0",20)
    #    move_to_center("ch1",20)
    #    move_to_center("ch2",20)
    #    for i in range(len(action_sequence)) :
    #       line=action_sequence[i]
    #        interpret(line,ts)
    #    move_to_center("ch0",20)
    #    move_to_center("ch1",20)
    #    move_to_center("ch2",20)
    #    print("stop action")
    #    try:
    #        proc.kill()
    #    except:
    #        print("killpg error")
    #    print("stop music")
    #time.sleep(0.5)
    #action.stop()
    #print("end")

    def parse(self,line):
      self.python2fwb(">"+line)
      words=line.split()
      wlen=len(words)
      if wlen<1:
          return
      print(words)
      print("wlen="+str(wlen))
      if words[0]=='face':
          #print("face...")
          if wlen>=2:
              #print("face 2nd")
              fx=words[1]
              #print(fx)
              degree=100.0
              if wlen>=3:
                  degree=float(words[2])
              print(self.servo_ctrl)
              print(words[0]+" "+fx+" degree="+str(degree))
              self.servo_ctrl.interpret(fx,degree)
      elif words[0]=='shutdown':
          self.shutdown()

    def returnFileList(self):
        cpath=os.getcwd()
        os.chdir("/home/pi/Pictures")
        filenames = [f.name for f in os.scandir()]
        filenames.sort()
        for fn in filenames:
            self.python2fwbln(fn)
        os.chdir(cpath)

    def shutdown(self):
        os.system("sudo shutdown -h now")
      
    def client_start(self):
      """クライアントのスタート"""
      self.sock.connect((self.HOST, self.PORT))
      handle_thread = threading.Thread(target=self.handler, args=(self.sock,), daemon=True)
      handle_thread.start()
 
    def handler(self,sock):
      """サーバからメッセージを受信し、表示する"""
 
      while True:
        data = sock.recv(1024)
        print("[受信]{}".format(data.decode("utf-8")))
        line=data.decode("utf-8")
        if line=="":
            return
        
        self.parse(line)
      
class Servo_Controller: 
  
  # Helper function to make setting a servo pulse width simpler.
  def set_servo_pulse(self,channel, pulse):
      self.pulse_length = 1000000    # 1,000,000 us per second
      self.pulse_length //= 60       # 60 Hz
      print('{0}us per period'.format(pulse_length))
      self.pulse_length //= 4096     # 12 bits of resolution
      print('{0}us per bit'.format(self.pulse_length))
      pulse *= 1000
      pulse //= pulse_length
      self.pwm.set_pwm(channel, 0, pulse)
  
  def home_position(self,ch):
      middle=(servo_min+servo_max)/2
      self.pwm.set_pwm(ch,0,middle)

  def __init__(self):
      # Uncomment to enable debug output.
      #import logging
      #logging.basicConfig(level=logging.DEBUG)
  
      # Initialise the PCA9685 using the default address (0x40).
      self.pwm = Adafruit_PCA9685.PCA9685()
 
      # Alternatively specify a different address and/or bus:
      #pwm = Adafruit_PCA9685.PCA9685(address=0x41, busnum=2)
  
      # Configure min and max servo pulse lengths
      self.servo_min = 150  # Min pulse length out of 4096
      self.servo_max = 600  # Max pulse length out of 4096
      self.servo_middle=(self.servo_min+self.servo_max)/2

      # Set frequency to 60hz, good for servos.
      self.pwm.set_pwm_freq(60)
      
      self.action=Action(self)
      self.move_to_center("ch0",20)
      self.move_to_center("ch1",20)
      self.move_to_center("ch2",20)
      print('Moving servo on channel 0, press Ctrl-C to quit...')
      interval_time=0.8
      noi=0
      w.wiringPiSetup()
      # pin 1, input
      w.pinMode(1,0)
      self.speed=30

  def move_left_div(self,ch,dv,speed):
      if speed <=1:
          speed = 1
      current_position=self.action.getCurrentPosition(ch)
      px=self.servo_middle-(self.servo_max-self.servo_min)/(dv*2)
      d=(px-current_position)/speed
      actionList=[]
      for i in range(speed):
          actionList.append(current_position+(i+1)*d)
      print("move_to_left_div,"+ch+"speed="+str(speed)+",dv="+str(dv)+",px="+str(px))
#     print(actionList)
      self.action.submit(ch,actionList)
 
  def move_right_div(self,ch,dv,speed):
      if speed <=1:
          speed = 1
      current_position=self.action.getCurrentPosition(ch)
      px=self.servo_middle+(self.servo_max-self.servo_min)/(dv*2)
      d=(px-current_position)/speed
      actionList=[]
      for i in range(speed):
          actionList.append(current_position+(i+1)*d)
      print("move_to_right_div,"+ch+"speed="+str(speed)+",dv="+str(dv)+",px="+str(px))
#     print(actionList)
      self.action.submit(ch,actionList)
 
  def move_to_center(self,ch,speed):
      if speed <=1:
          speed = 1
      current_position=self.action.getCurrentPosition(ch)
      print("ch="+ch+",speed="+str(speed)+",current_position="+str(current_position))
      d=(self.servo_middle-current_position)/speed
      actionList=[]
      for i in range(speed):
          actionList.append(current_position+(i+1)*d)
      print("move_to_center,"+ch+"speed="+str(speed))
      print(actionList)
      self.action.submit(ch,actionList)
 
  def interpret(self,cmd,degree):
      #print("interpret "+str(line[0])+"-"+str(line[1])+","+line[2])
      print("interpret "+cmd+" degree="+str(degree))
      xdegree=degree/50.0
      if cmd == "up":
          self.move_to_center("ch1",self.speed)
      elif cmd == "down":
          self.move_right_div("ch1",xdegree,self.speed)
      elif cmd== "left":
          self.move_right_div("ch0",xdegree,self.speed)
      elif cmd== "right":
          self.move_left_div("ch0",xdegree,self.speed)
      elif cmd== "front":
          self.move_to_center("ch0",self.speed)
      elif cmd== "slow":
          self.speed=45
      elif cmd== "normal":
          self.speed=30
      elif cmd== "fast":
          self.speed=15
    
class Action(threading.Thread):
  step=0
  channels={}
  chno={}
  positions={}
  cont=False
  def __init__(self,servo):  #servo:Servo_Controller
      self.step=0
      self.servo=servo
      self.channels={"ch0":[],"ch1":[],"ch2":[]}
      self.positions={"ch0":self.servo.servo_middle,"ch1":self.servo.servo_middle,"ch2":self.servo.servo_middle}
      self.chno={"ch0":0,"ch1":1,"ch2":2}
      self.cont=False
      threading.Thread.__init__(self)
      self.cont=True
      self.start()
  def submit(self,ch,com_list):
      print("submit ch="+ch)
      try:
        chx=self.channels[ch]
        chx.extend(com_list)
        self.channels[ch]=chx
      except:
        print( "no " + ch)
        
  def advance(self):
      for chn in ["ch0","ch1","ch2"]:
          try:
              poslist=self.channels[chn]
              if len(poslist)>0:
                  pos=poslist[0]
                  if pos>= self.servo.servo_min and pos<=self.servo.servo_max:
                      cn=self.chno[chn]
                      self.servo.pwm.set_pwm(cn,0,int(pos))
#                     print("advance "+chn+",pos="+str(pos))
                      self.positions[chn]=pos
                  poslist.pop(0)
                  self.channels[chn]=poslist
          except:
              print ('advance no list in the ' + chn)
  def getCurrentPosition(self,ch):
      try:
          rtn=self.positions[ch]
          print("getCurrentPosition("+ch+")="+str(rtn))
          return self.positions[ch]
      except:
          print ('no ' + ch)
          return self.servo.servo_middle
  def stop(self):
      self.cont=False
  def run(self):
      while self.cont:
          self.advance()
          time.sleep(0.01)
        
 
def main():
    RemoteCommandReader()

if __name__ == "__main__":
    main()
 
