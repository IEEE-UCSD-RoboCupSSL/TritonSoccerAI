'''
Author: Cecilia Hong
Date: 2020-11-15 21:36:47
LastEditTime: 2020-11-15 22:53:22
Description: Python script to run java and cpp program 
FilePath: \Robo Cup\SimuBot\run.py
'''
import subprocess
import os
import sys
import getpass
import socket


def subprocess_cmd(command):
    process = subprocess.Popen(command,stdout=subprocess.PIPE, shell=True)
    proc_stdout = process.communicate()[0].strip()
    print(proc_stdout)

path = os.getcwd()
user = getpass.getuser() 
host = socket.gethostname()
print('Welcome ' + user + '! You are currently running on ' + host)

os.system('echo Starting RC-Core...')
os.chdir("RC-Core")

if len(sys.argv) > 1 and sys.argv[1] == "-i":
    os.system('mvn clean install')
else:
    os.system('mvn exec:java &')
    os.system('echo RC-Core is executed!')

os.system('echo Starting CPP program...')


