'''
Created on Oct 7, 2012

@author: Nguyen Huu Hiep
'''
from Tkinter import *

master = Tk()

e = Entry(master)
e.pack()

e.focus_set()

def callback():
    print e.get()

b = Button(master, text="get", width=10, command=callback)
b.pack()

mainloop()

e = Entry(master, width=50)
e.pack()

text = e.get()

def makeentry(parent, caption, width=None, **options):
    Label(parent, text=caption).pack(side=LEFT)
    entry = Entry(parent, **options)
    if width:
        entry.config(width=width)
    entry.pack(side=LEFT)
    return entry

user = makeentry(master, "User name:", 10)
password = makeentry(master, "Password:", 10, show="*")

content = StringVar()
entry = Entry(master, text="Abc", textvariable=content)

text = content.get()
content.set(text)