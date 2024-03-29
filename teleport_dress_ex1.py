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

import cv2
import numpy as np
from moviepy.editor import ImageSequenceClip
# coding: utf-8
from PIL import Image
import smbus
import time
import sys
import requests
import os
import socket
import threading
from collections import deque
import subprocess

#from PIL import Image
#import smbus
#import time
#import sys
#import requests


class RemoteCommandReader:
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    HOST = 'localhost'
    PORT = 9998
    def __init__(self):
        print("start RemoteCommandReader.__init__")
        self.client_start()
        self.teleport_dress=Teleport_Dress()
        print(self.teleport_dress)
        self.teleport_dress.setRemoteCommandReader(self)
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

    def parse(self,line):
      self.python2fwb(">"+line)
      words=line.split()
      print(words)
      if words[0]=='show':
          fx=words[1]
          print(fx)
          print(self.teleport_dress)
          self.teleport_dress.putImage(fx)
      elif words[0]=='clear':
          self.teleport_dress.clearImage()
      elif words[0]=='ls':
          self.returnFileList()
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
        received=data.decode("utf-8")
        lines=received.splitlines()
        for line in lines:
          self.parse(line)
      
class Teleport_Dress:

  off_x=0
  off_y=0

  imax=75
  jmax=16

#  dx=img_width/jmax
#  dy=img_height/imax


  def __init__(self):
    print("start Teleport_Dress.__init__")
    self.pixels=[[(0,0,0) for j in range(self.jmax)] for i in range(self.imax)]
    self.addrs = [0x30,0x31,0x32,0x33]
    self.fourpix=[0x00, 0x00, 0x04, 0,0,0, 0,0,0, 0,0,0, 0,0,0]
    self.i2c = smbus.SMBus(1) # 注:ラズパイのI2Cポート
    self.queue=deque([])
    self.show_loop_start()

  def setRemoteCommandReader(self, x):
    self.rcReader=x

  def cv2pil(self,image):
    ''' OpenCV型 -> PIL型 '''
    new_image = image.copy()
    if new_image.ndim == 2:  # モノクロ
        pass
    elif new_image.shape[2] == 3:  # カラー
        new_image = cv2.cvtColor(new_image, cv2.COLOR_BGR2RGB)
    elif new_image.shape[2] == 4:  # 透過
        new_image = cv2.cvtColor(new_image, cv2.COLOR_BGRA2RGBA)
    new_image = Image.fromarray(new_image)
    return new_image

  def gif2img(self,file_name):
    #print("gif2img.."+file_name)
    try:
      path="/home/pi/Pictures/"+file_name
      gif = cv2.VideoCapture(path)
      print("gif=",gif)
    except:
      rtn="error: show ...no "+file_name
      self.rcReader.pythohn2fwb(rtn)
      gif = cv2.VideoCapture('/home/pi/Pictures/giphy-girl-ex1.gif')
    fps = gif.get(cv2.CAP_PROP_FPS)  # fpsは１秒あたりのコマ数
    images = []
    i = 0
    while True:
      is_success, img = gif.read()
      if not is_success:
          #print("gif.read() is not success")
          break
      #print("gif read i=",i)
      images.append(img)
      i+=1
    ex=('anime',images)
    return ex

  def clearImage(self):
    ex=('clear','x')
    print("clear")
    self.queue.append(ex)

  def putImage(self,file_name):
    print("putImage..."+file_name)
    if file_name.startswith('http://') or file_name.startswith('https://'):
      #subprocess.call(["cd ~/Pictures"],shell=True)
      cpath=os.getcwd()
      os.chdir("/home/pi/Pictures")
      wgetx = "wget -N "+file_name
      subprocess.call([wgetx],shell=True)
      #subprocess.call(["cd ~/python"],shell=True)
      os.chdir(cpath)
      path=file_name.split("/")
      plen=len(path)
      xname=path[plen-1]
      print("xname="+xname)
      if xname.endswith('.gif') or xname.endswith('.GIF'):
        ex=self.gif2img(xname)
        self.queue.append(ex)
      elif xname.endswith('.jpg') or xname.endswith('.JPG') or \
           xname.endswith('.JPEG') or xname.endswith('.jpeg'):
        ex=('img',xname)
        self.queue.append(ex)
      elif xname.endswith('.png') or xname.endswith('.PNG'):
        ex=('img',xname)
        self.queue.append(ex)
    else:
      path=file_name.split("/")
      plen=len(path)
      xname=path[plen-1]
      print("xname="+xname);
      if xname.endswith('.gif') or xname.endswith('.GIF'):
        ex=self.gif2img(xname)
        self.queue.append(ex)
      elif xname.endswith('.jpg') or xname.endswith('.JPG') or \
           xname.endswith('.JPEG') or xname.endswith('.jpeg'):
        ex=('img',xname)
        self.queue.append(ex)
      elif xname.endswith('.png') or xname.endswith('.PNG'):
        ex=('img',xname)
        self.queue.append(ex)
      print("queue="+str(len(self.queue)))

  def show_loop_start(self):
      """show_loop_start"""
      handle_thread = threading.Thread(target=self.handler, daemon=True)
      handle_thread.start()
 
  def handler(self):
      """queueからメッセージを受信し、表示する"""
      last_anime=[]
      while True:
        try:
          qlen = len(self.queue)
          if qlen>0:
              print("handler, qlen="+str(qlen))
              kimg=self.queue.popleft()
              print("handler, kimg[0]="+kimg[0])
              if kimg[0]=='anime':
                last_anime=kimg[1]
                self.show_one_anime(last_anime)
              elif kimg[0]=='img':
                last_anime=[]
                self.show_jpg_png(kimg[1])
              elif kimg[0]=='clear':
                last_anime=[]
                self.clear_pic()
          else:
             if last_anime!=[]:
                self.show_one_anime(last_anime)
             else:
                time.sleep(0.1)
        except:
          if last_anime!=[]:
            self.show_one_anime(last_anime)
  def show_jpg_png(self,file_name):
    print("show_jpg_png("+file_name+")")
    #subprocess.call(["cd","~/Pictures"])
    try:
      img = Image.open("/home/pi/Pictures/"+file_name)
      self.show_one_pic(img)
    except:
      rtn="error: show "+file_name
      self.rcReader.python2fwbln(rtn)

  def show_one_anime(self,images):
      try:
        for t in range(len(images)):
          #cv2.imshow('test', images[t])
          #cv2.waitKey(int(1000/fps))
          self.show_one_pic(self.cv2pil(images[t]))
      except KeyboardInterrupt:
        print('stop by ctrl-c')
      except:
        rtn="error: show gif"
        self.rcReader.python2fwbln(rtn)
                
#
# command:
#   clear... 0x00
#   show ... 0x01
#   set1 ix,iy,r,g,b
#         ... 0x02  *,*,  *,*,*
#   setn ix,iy,n, r0,g0,b0, ..r(n-1),g(n-1),b(n-1)   n<=8
#         ... 0x03 *,*, *, *,*,*, ..., *,*,*
#
          
  def show_one_pic(self,img):
    img_width, img_height = img.size
    #print('width:',img_width)
    #print('height:',img_height)
    #print("show_one_pic")

    dx=img_width/self.jmax
    dy=img_height/self.imax
    #print("dx=",dx)
    #print("dy=",dy)

    for i in range(self.imax):
      for j in range(self.jmax):
        x=self.off_x+j*dx
        y=self.off_y+i*dy
        rgb=img.getpixel((x,y))
        #print("rgb[",j,",",i,"]=",rgb)
        self.pixels[i][j]=rgb

    self.i2c.write_byte(self.addrs[0],0x00)
    self.i2c.write_byte(self.addrs[1],0x00)
    self.i2c.write_byte(self.addrs[2],0x00)
    self.i2c.write_byte(self.addrs[3],0x00)
    for i in range(self.imax):
      for j in range(4):
        for k in range(4):
          p=self.pixels[i][4*j+k]
          self.fourpix[3+k*3+0]=p[0]
          self.fourpix[3+k*3+1]=p[1]
          self.fourpix[3+k*3+2]=p[2]
        self.fourpix[0]=i;
        self.fourpix[1]=0;
        i2c_addr=self.addrs[j]
        try:
          self.i2c.write_i2c_block_data(i2c_addr,0x03,self.fourpix)
        except:
          print('i2c write error at:(',i,',',j,')')
    #time.sleep(0.00001)
    self.i2c.write_byte(self.addrs[0],0x01)
    self.i2c.write_byte(self.addrs[1],0x01)
    self.i2c.write_byte(self.addrs[2],0x01)
    self.i2c.write_byte(self.addrs[3],0x01)

  def clear_pic(self):
    print("clear_pic")
    self.i2c.write_byte(self.addrs[0],0x00)
    self.i2c.write_byte(self.addrs[1],0x00)
    self.i2c.write_byte(self.addrs[2],0x00)
    self.i2c.write_byte(self.addrs[3],0x00)
    time.sleep(0.001)
    self.i2c.write_byte(self.addrs[0],0x01)
    self.i2c.write_byte(self.addrs[1],0x01)
    self.i2c.write_byte(self.addrs[2],0x01)
    self.i2c.write_byte(self.addrs[3],0x01)

def main():
    RemoteCommandReader()

if __name__ == "__main__":
    main()
