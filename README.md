        _/        _/_/_/_/  _/    _/    _/_/    _/      _/  _/_/_/_/   
       _/        _/        _/    _/  _/    _/  _/_/  _/_/  _/          
      _/        _/_/_/    _/_/_/_/  _/    _/  _/  _/  _/  _/_/_/       
     _/        _/        _/    _/  _/    _/  _/      _/  _/            
    _/_/_/_/  _/_/_/_/  _/    _/    _/_/    _/      _/  _/_/_/_/       

    ================================================================

LEHome 是一套完整的开源智能家居方案。LEHome拥有以下特性：

    简单的控制命令编程
    ibeacon室内定位
    高度模块设计
    红外控制、开关控制、传感器采集
    android，web app，微信版客户端

项目地址：https://github.com/legendmohe/LEHome

**本文主要介绍该方案的Android客户端**

## 界面

主要界面有：

1. 主界面，采用对话形式向服务端发送命令。  
    ![主界面](http://i1334.photobucket.com/albums/w649/legendmohe/_zpsvtgkblrr.png)
2. 收藏界面，主要是为了更方便地输入一些自定义的命令（message）。  
    ![收藏](http://i1334.photobucket.com/albums/w649/legendmohe/_zpsinvhnfjz.png)
3. 设置界面。  
    ![设置1](http://i1334.photobucket.com/albums/w649/legendmohe/1_zpsyyf5qpbz.png)
    ![设置2](http://i1334.photobucket.com/albums/w649/legendmohe/2_zps3btsbgcy.png)
4. 语音输入界面。  
    ![语音输入](http://i1334.photobucket.com/albums/w649/legendmohe/_zpsusnzwfoc.png)

## 功能

1. 基本功能
  1. 发送命令  
    在输入框中输入命令，点击回车即可发送至服务器（连接正常情况下）。
  2. 语音发送命令  
    切换至语音模式后，可用语音输入命令（使用百度语音，使用方法类似微信）。
  3. 设置参数  
    设置一系列基本参数和增强功能的参数。
  4. 收藏命令语句  
    可以添加删除一些自定义的语句。这些语句会出现在智能提示中。

2. 增强功能
  1. 智能命令提示  
    类似于IDE的智能提示，以列表形式提示下一个合法的命令有哪些。  
    当输入字符>1个时，输入框左侧弹出智能提示（或补全）按钮，此时上划按钮看上一个提示，下划看下一个提示，左划弹出所有提示的列表。  
    智能提示的类型有：补全当前命令、下一个合法的命令、日期、时间和收藏的命令语句。  
    注意，使用此功能需要在设置菜单中下载补全数据。  
    ![智能补全](http://i1334.photobucket.com/albums/w649/legendmohe/_zpsrbyvx2oo.png)  
    ![智能补全列表](http://i1334.photobucket.com/albums/w649/legendmohe/_zpsw8bqzsgt.png)
  2. 本地、远程服务自动切换  
    启用了此功能后，当手机处于跟服务器同一局域网时，会自动切换至本地模式。本地模式是指命令的发送和接受无需通过云端转发，直接在局域网内进行。
  3. 自动连接蓝牙麦克风  
    启用了此功能后，当连接蓝牙耳机时，语音输入源路由至耳机麦克风端。

## 项目地址

[https://github.com/legendmohe/LEHomeMobile_android](https://github.com/legendmohe/LEHomeMobile_android)
