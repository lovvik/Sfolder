from cryptography.fernet import Fernet
import os.path


_prefix_ = "_c2ZzaXM_"


key = Fernet.generate_key()
print(f"-----> key : {key}")
fobj = Fernet(key)

def islocked(path) :
    if(path.find(_prefix_)==-1) : return False
    return True

def lockpath(path) :
    return os.path.join(os.path.dirname(path) , _prefix_ + os.path.basename(path))


def encfile(path) :
    if(islocked(path)) : return
    new = lockpath(path)
    with open(path, 'rb') as plain, open(new, 'wb') as  cypher:
        cypher.write( fobj.encrypt(plain.read()))
    os.chmod(new, 0o444)

def decfile(path) :
    new = path.replace(_prefix_, '')
    with open(path, 'rb') as cypher, open(new, 'wb') as  plain:
        plain.write( fobj.decrypt( cypher.read()))

def clean(folderpath) :
    for fichier in os.listdir(folderpath) :
        fullpath = os.path.join(os.path.abspath(folderpath),fichier)
        if(islocked(fullpath)) : os.remove(fullpath)

if (__name__=="__main__") :

    encfile("un.txt")
    decfile("_c2ZzaXM_un.txt")


