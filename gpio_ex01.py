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
#                                | GPIO
#                                v
#                          
 
#import cv2
import numpy as np
#from moviepy.editor import ImageSequenceClip
# coding: utf-8
#from PIL import Image
import smbus
import time
import sys
import requests
import os
import socket
import threading
from collections import deque
import subprocess
from PIL import Image, ImageFont, ImageDraw
 
import RPi.GPIO as GPIO
 
 
class RemoteCommandReader:
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    HOST = 'localhost'
    PORT = 9998
    pin_array ={}
    SAMPLE_RATE = {}  # {pin_no:<rate>, ...} rate:sec(float).
    SAMPLE_VALUE = {} # {pin_no:<value>, ...}
    SAMPLE_LAST = {} # {pin_no:<last-time>, ...}
    INTERVAL = {} # {pin_no:<interval>, ...} interval:sec(float).
    INTERVAL_LAST = {} # {pin_no:<last-time>, ...} interval-last:sec(float).
    def __init__(self):
        print("start RemoteCommandReader.__init__")
        self.proc=None
        self.client_start()
        GPIO.setmode(GPIO.BCM)
        self.sample_start()
        while True:
          try:
            time.sleep(0.0001)
          except:
            print("error")
 
    def python2fwbln(self,py2x_message):
      msg=py2x_message+'\n'
      self.sock.sendall(msg.encode('utf-8'))
      
    def python2fwb(self,py2x_message):
      msg=py2x_message+'\n'
      self.sock.sendall(msg.encode('utf-8'))
 
    def play_music(self):
        print("play_music")
        self.proc= subprocess.run(["/usr/bin/mpg123 /home/pi/workspace/FukuyamaWB4PiPy/edison-1min-01.mp3"], shell=True)
        
    def show_text(self,text):
        print("show_text("+text+")");
        #font = ImageFont.truetype("/usr/share/fonts/truetype/noto/NotoMono-Regular.ttf", 16)
    
        #width, ignore = font.getsize(text)
        #im = Image.new("RGB", (width + 30, 16), "black")
        #draw = ImageDraw.Draw(im)
    
        #x = 0;
        #draw.text((x, 0), text, (255,255,0), font=font)
    
        #im.save("/home/pi/message.ppm")
        #self.proc= subprocess.Popen([" /usr/bin/sudo /home/pi/rpi-rgb-led-matrix/examples-api-use/demo --led-no-hardware-pulse --led-rows=16 --led-cols=96 -D 1 -m 20 /home/pi/message.ppm"], shell=True)
 
 
    def show_kill(self):
        self.proc= subprocess.run(["/usr/bin/sudo /usr/bin/pkill demo"], shell=True)
 
 
    def parse(self,line):
      self.python2fwb(">"+line)
      print(">"+line)
      words=line.split()
      print(words)
      
      #
      #
      # gpio setmode GPIO.BCM ... defalut
      # gpio setup <pin> out .... GPIO.setup(<pin>, GPIO.OUT)
      # gpio setup <pin> in
      #       ... GPIO.setup(<pin>, GPIO.IN, pull_up_down=GPIO.PUD_UP)
      # gpio setup <pin> change
      #       ... GPIO.setup(<pin>, GPIO.IN, pull_up_down=GPIO.PUD_UP)
      #           and if there was change the input value,
      #                  return the value and its time.
      # gpio setup <pin> interval <msec> 
      #       ... GPIO.setup(<pin>, GPIO.IN, pull_up_down=GPIO.PUD_UP)
      #           and return the input value at the <msec> interval.
      # gpio input <pin> ... return GPIO.input(<pin>)
      # gpio output <pin> 1|0... GPIO.output(<pin>, True|False)
      # gpio help ... return this.
      #                         
      if words[0]=='cmd':
          print(len(words))
          words.pop(0)
          proc=subprocess.Popen(words,stdout=subprocess.PIPE)
          rtnb=proc.stdout.read()
          rtn0=rtnb.decode("utf-8")
          print(rtn0)
          rtn1=rtn0.split('=')
          rtn2=rtn1[1]
          self.python2fwbln(rtn2)
          
      if words[0]=='gpio':
          fx=words[1]
          print(fx)
          if fx=='setup':
              pin=words[2]
              mode=words[3]
              if mode=='out':
                  print("GPIO.setup("+pin+",GPIO.OUT)")
                  GPIO.setup(int(pin), GPIO.OUT)
              if mode=='in':
                  print("GPIO.setup("+pin+",GPIO.IN, pull_up_down=GPIO.PUD_UP)")
                  GPIO.setup(int(pin), GPIO.IN, pull_up_down=GPIO.PUD_UP)
              if mode=='change':
                  print("GPIO.setup("+pin+",GPIO.IN, pull_up_down=GPIO.PUD_UP)")
                  GPIO.setup(int(pin), GPIO.IN, pull_up_down=GPIO.PUD_UP)
                  self.SAMPLE_RATE[pin]=0.1
                  self.SAMPLE_VALUE[pin]=1
                  self.SAMPLE_LAST[pin]=time.time()
              if mode=='interval':
                  interval=words[4]
                  print("GPIO.setup("+pin+",GPIO.IN, pull_up_down=GPIO.PUD_UP)")
                  GPIO.setup(int(pin), GPIO.IN, pull_up_down=GPIO.PUD_UP)
                  self.INTERVAL[pin]=float(interval)
                  self.INTERVAL_LAST[pin]=time.time()
          elif fx=='input':
              pin=words[2]
              print("return GPIO.input("+pin+")")
              x=GPIO.input(int(pin))
              print(x)
              self.python2fwbln(str(x))
          elif fx=='output':
              pin=words[2]
              x=words[3]
              if x=='1':
                  tf=True
              if x=='0':
                  tf=False
              print("GPIO.output("+pin+","+str(tf)+")")
              GPIO.output(int(pin),tf)
      elif words[0]=='led-matrix':
          print("led-matrix")
          if words[1]=='show':
              print("show")
              self.show_text(words[2])
          elif words[1]=='kill':
              self.show_kill()
      elif words[0]=='play-music':
          self.play_music()
      elif words[0]=='cleanup':
          GPIO.cleanup()
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
      print("start client_start")
      try:
        self.sock.connect((self.HOST, self.PORT))
        handle_thread = threading.Thread(target=self.handler,
                                         args=(self.sock,), daemon=True)
      except:
        print("connect error? or start thread error?")
      handle_thread.start()
 
    def handler(self,sock):
      """サーバからメッセージを受信し、表示する"""
      print("start receive.")
      while True:
        data = sock.recv(1024)
        print("[受信]{}".format(data.decode("utf-8")))
        received=data.decode("utf-8")
        lines=received.splitlines()
        for line in lines:
            self.parse(line)
 
    def sample_start(self):
      """スタート sampling"""
      print("sample_start")
      sample_thread = threading.Thread(target=self.sample_handler, args=(), daemon=True)
      sample_thread.start()
 
    def sample_handler(self):
      """sampling"""
      print("sampling handler start")
      while True:
            current_time=time.time()
            for key in self.SAMPLE_RATE:
                last_time=self.SAMPLE_LAST[key]
                if last_time+self.SAMPLE_RATE[key]<current_time:
                    #print(current_time)
                    self.SAMPLE_LAST[key]=current_time
                    try:
                        current_value=GPIO.input(int(key))
                        #print("GPIO.input("+key+")=",end="")
                        #print(current_value)
                        last_value=self.SAMPLE_VALUE[key]
                        #ボタンを押したらLEDを光らせる
                        if last_value!=current_value:
                            self.SAMPLE_VALUE[key]=current_value
                            led_time=current_time+65
                            while led_time<current_time:
                               GPIO.output(26,GPIO.HIGH)
                               sleep(1)
                               print(led_time)
                               print(current_time)
                            if current_value==0:
                               returnLine="pushed"
                               print("return="+returnLine)
                               self.python2fwbln(returnLine)
                            if current_value==1:
                               returnLine=str(key)+" on"
                               print("return="+returnLine)
                               self.python2fwbln(returnLine)
                    except Exception as e:
                        print(e)
                        print("error sample port "+str(key)+"...")
            '''
            for key in self.INTERVAL:
                last_time=self.INTERVAL_LAST[key]
                if last_time+self.INTERVAL[key]<current_time:
                    self.INTERVAL_LAST[key]=current_time
                    try:
                        current_value=GPIO.input(int(key))
                        print("return GPIO.input("+key+")")
                        print(current_value)
                        returnLine=("return interval,"+
                                "pin="+key+
                                ",Date="+time.strftime('%Y/%m/%d/ %H:%M:%S')+
                                ",v=" +str(current_value))
                        self.python2fwbln(returnLine)
                    except Exception as e:
                        print(e)
                        print("error sample port "+key+"...")
            '''
            try:
              time.sleep(0.01)
            except Exception as e:
              print(e)
              print("error, sample handler")
 
        
def main():
    reader=RemoteCommandReader()
    reader.sample_start()
 
if __name__ == "__main__":
    main()
##09_switch.py
#import time
## Configure the Pi to use the BCM (Broadcom) pin names, rather than the pin pos$
#led_pin = 18
#switch_pin =23
#state_01=False
#GPIO.setup(led_pin, GPIO.OUT)
#GPIO.setup(switch_pin, GPIO.IN, pull_up_down=GPIO.PUD_UP)
#try:
#    while True:
#        if GPIO.input(switch_pin) == False:
#            state01=True
#            print("Button Pressed")
#        else:
#            state_01=False
#        GPIO.output(led_pin, state_01)
#        time.sleep(0.2)
#finally:
#    print("Cleaning up")
#    GPIO.cleanup()
