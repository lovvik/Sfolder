#!usr/bin/env python3

import os
import sys
import errno
from fusepy.fuse import FUSE, FuseOSError, Operations, fuse_get_context
#from toycipher import *
import pdb

import encrypt

_mountpoint_ = "/tmp/fuse"


class Cfs (Operations) :
    def __init__(self, path, content) :
        print(f"chemin : {path}")
        self.root = os.path.abspath(path)
        print(f"road {self.root}")
        self.files = content
        self.isauth = True
        if(self.isauth) : print("Current state : Unlocked")
        else : print("Current State : Locked")

    def destroy(self, path) :
        path = self.rpath(path)
        encrypt.clean(path)

    def rpath (self, path) :
        res = os.path.join(self.root, path.lstrip('/'))
        #print(f" '{path}' -> '{res}' ")
        return res

    def getattr(self, path, fh=None):
        path = self.rpath(path)
        st = os.lstat(path)
        return dict((key, getattr(st, key)) for key in (
            'st_atime', 'st_ctime', 'st_gid', 'st_mode', 'st_mtime',
            'st_nlink', 'st_size', 'st_uid'))

    def readdir(self, path, fh):
        path = self.rpath(path)
        return ['.', '..'] + os.listdir(path)


    def open(self, path, flags):
        path = self.rpath(path)
        if((not self.isauth) and (not encrypt.islocked(path))) : path = encrypt.lockpath(path)
        return os.open(path,flags)

    def read(self, path, length, offset, fh):
        os.lseek(fh, offset, os.SEEK_SET)
        return os.read(fh, length)

    def write(self, path, buf, offset, fh):
        path = self.rpath(path)
        os.lseek(fh, offset, os.SEEK_SET)
        #encrypt.encfile(path)
        return os.write(fh, buf)

    def create(self, path, mode):
        path = self.rpath(path)
        return os.open(path, os.O_WRONLY | os.O_CREAT | os.O_TRUNC, mode)

    def rename(self, old, new):
        return os.rename(old, self.root + new)


def setup(path) :
    if(not os.path.isdir(_mountpoint_)) :
        try  : os.mkdir(_mountpoint_)
        except e : print(f"{e}")

    content = os.listdir(path)

    for thing in content :
        fullpath = os.path.join(os.path.abspath(path),thing)
        if(not encrypt.islocked(fullpath)) : encrypt.encfile(fullpath)



    content = os.listdir(path)
    return (os.path.abspath(path), content)

        

def main(path) :

    path, content = setup(path)
    #encrypt.clean(path)
    cfs = FUSE(Cfs(path, content), _mountpoint_, foreground = True, nonempty=True, nothreads = True)



if __name__ == '__main__' :
    print(f"pid : {os.getpid()}")
    main(sys.argv[1])

