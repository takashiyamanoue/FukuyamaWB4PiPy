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
from PIL import Image, ImageFont, ImageDraw
import smbus
import time
import sys
import requests
import os
import socket
import threading
from collections import deque
import subprocess
import copy

#from PIL import Image
#import smbus
#import time
#import sys
#import requests


class RemoteCommandReader:
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    HOST = 'www.yama-lab.org'
    PORT = 8080
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
      elif words[0]=='text':
          if len(words)>=2:
             words.pop(0)
             fx  = ' '.join(words)
          else:
             fx=""
          print("text "+fx)
          self.teleport_dress.putText(fx)
      elif words[0]=='txtColor':
          fx=words[1]
          print("txtColor "+fx)
          self.teleport_dress.setTextColor(fx)
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
      print("start client")
      try:
          self.sock.connect((self.HOST, self.PORT))
          handle_thread = threading.Thread(target=self.handler, args=(self.sock,), daemon=True)
          print("success connect to "+str(self.HOST)+","+str(self.PORT))
          handle_thread.start()
      except:
          traceback.print_exec()
          rtn="error: connect failure"
          self.rcReader.pythohn2fwb(rtn)

    def handler(self,sock):
      print("start receive messsage handler")
      """サーバからメッセージを受信し、表示する"""
 
      while True:
        print("before-recv.")
        data = sock.recv(1024)
        print("[受信]{}".format(data.decode("utf-8")))
        line=data.decode("utf-8")
        self.parse(line)
      
class Teleport_Dress:

  off_x=0
  off_y=0

  imax=75
  jmax=16

#  dx=img_width/jmax
#  dy=img_height/imax

  impose_text=""
  text_color=(0,0,0)


  def __init__(self):
    print("start Teleport_Dress.__init__")
    self.font_path = './font/font_jb004_running_brush_wi.ttf'
    self.pixels=[[(0,0,0) for j in range(self.jmax)] for i in range(self.imax)]
    self.addrs = [0x30,0x31,0x32,0x33]
    self.fourpix=[0x00, 0x00, 0x04, 0,0,0, 0,0,0, 0,0,0, 0,0,0]
    self.i2c = smbus.SMBus(1) # 注:ラズパイのI2Cポート
    self.queue=deque([])
    self.show_loop_start()
    self.text_x=0

  def setTextColor(self,x):
      if x=="white":
          self.text_color=(255,255,255)
      elif x=="black":
          self.text_color=(0,0,0)
      elif x=="red":
          self.text_color=(255,0,0)
      elif x=="green":
          self.text_color=(0,255,0)
      elif x=="blue":
          self.text_color=(0,0,255)
      elif x=="yellow":
          self.text_color=(255,255,0)
      elif x=="cyan":
          self.text_color=(0,255,255)
      elif x=="magenta":
          self.text_color=(255,0,255)
      elif x=="orange":
          self.text_color=(255,128,0)
      elif x=="pink":
          self.text_color=(200,80,80)

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
      import traceback
      traceback.print_exc()
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

  def pic2imgs(self,file_name):
    print("pic2imgs("+file_name+")")
    img=[]
    #subprocess.call(["cd","~/Pictures"])
    try:
      img = Image.open("/home/pi/Pictures/"+file_name)
      #print("img open success.");
      #self.show_one_pic(img)
    except:
      import traceback
      traceback.print_exc()
      rtn="error: show "+file_name
      self.rcReader.python2fwbln(rtn)
    #fps = gif.get(cv2.CAP_PROP_FPS)  # fpsは１秒あたりのコマ数
    images = []
    i = 0
    for i in range(5):
      img2=copy.copy(img)
      #self.show_one_pic(img2)
      images.append(img2)
    #print(i)
    #print("images-len="+str(len(images)))
    rtn=('img',images)
    return rtn

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
        #ex=self.pic2imgs(xname)
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
        #ex=self.pic2imgs(xname)
        ex=('img',xname)
        self.queue.append(ex)
      elif xname.endswith('.png') or xname.endswith('.PNG'):
        ex=('img',xname)
        #ex=self.pic2imgs(xname)
        self.queue.append(ex)
      print("queue="+str(len(self.queue)))
      
  def putText(self,txt):
      self.impose_text=txt

  def show_loop_start(self):
      """show_loop_start"""
      self.handle_thread_01 = threading.Thread(target=self.handler, daemon=True)
      self.handle_thread_01.start()
 
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
                last_anime=kimg
                self.show_one_anime_x(kimg)
              elif kimg[0]=='img':
                print("handler, kimg[0]="+kimg[0]+" kimg[1]="+kimg[1])
                x=self.pic2imgs(kimg[1])
                last_anime=x
                self.show_one_anime_x(x)
              elif kimg[0]=='clear':
                last_anime=[]
                self.impose_text=""
                self.clear_pic()
          else:
             if last_anime!=[]:
                 self.show_one_anime_x(last_anime)
             else:
                time.sleep(0.1)
        except:
          if last_anime!=[]:
            self.show_one_anime_x(last_anime)
  def show_jpg_png(self,file_name):
    print("show_jpg_png("+file_name+")")
    #subprocess.call(["cd","~/Pictures"])
    try:
      img = Image.open("/home/pi/Pictures/"+file_name)
      self.show_one_pic(img)
    except:
      import traceback
      traceback.print_exc()
      rtn="error: show "+file_name
      self.rcReader.python2fwbln(rtn)
  def show_one_anime_x(self,images):
      if images[0]=='anime':
          self.show_one_anime(images[1])
      elif images[0]=='img':
          self.show_one_anime2(images[1])

  def show_one_anime(self,images):
      #print("show_one_anime() images-len="+str(len(images)))
      try:
        for t in range(len(images)):
          #cv2.imshow('test', images[t])
          #cv2.waitKey(int(1000/fps))
          self.show_one_pic(self.cv2pil(images[t]))
      except KeyboardInterrupt:
        print('stop by ctrl-c')
      except:
        import traceback
        traceback.print_exc()
        rtn="error: show gif"
        self.rcReader.python2fwbln(rtn)
        self.handle_thread_01.stop()
                
  def show_one_anime2(self,images):
      #print("show_one_anime2() images-len="+str(len(images)))
      try:
        for t in range(len(images)):
          #cv2.imshow('test', images[t])
          #cv2.waitKey(int(1000/fps))
          self.show_one_pic(images[t])
      except KeyboardInterrupt:
        print('stop by ctrl-c')
      except:
        import traceback
        traceback.print_exc()
        rtn="error: show anime2(jpg/png)"
        self.rcReader.python2fwbln(rtn)
        self.handle_thread_01.stop()

#
# command:
#   clear... 0x00
#   show ... 0x01
#   set1 ix,iy,r,g,b
#         ... 0x02  *,*,  *,*,*
#   setn ix,iy,n, r0,g0,b0, ..r(n-1),g(n-1),b(n-1)   n<=8
#         ... 0x03 *,*, *, *,*,*, ..., *,*,*
#
          
  def show_one_pic(self,img_in):
    #print('show_one_pic width:',img_width)
    #print('show_one_pic height:',img_height)
    #print("show_one_pic")
    wwx,wwy=img_in.size
    fs=int(wwy/2)
    px=self.text_x
    py=int(wwx/4)
    if self.impose_text!="":
        img = img_in.copy()
        font = ImageFont.truetype(self.font_path, fs)
        draw = ImageDraw.Draw(img)
        draw.text((px,py), self.impose_text, fill=self.text_color, font=font)
        #img=self.puttext(img_in, self.impose_text, (px,py), self.font_path, fs, (0,0,0))
        size = draw.textsize(self.impose_text, font=font)
        if self.text_x+size[0]<(wwx-wwx/3):
            self.text_x=wwx-wwx/3
        else:
            self.text_x=self.text_x-int(wwx/27)
        #print("text_size=("+str(size[0])+",",str(size[1])+")")
    else:
       img=img_in
    img_width, img_height = img.size
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
