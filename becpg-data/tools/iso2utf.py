#!/usr/bin/env python
 
import sys
 
prov = []
 
f = file(sys.argv[1],"r")
for ligne in f.readlines():
    prov.append(unicode(ligne,'iso-8859-15').encode('utf8'))
f.close()
 
f = file(sys.argv[1],"w")
for ligne in prov:
    f.write(ligne)
f.close()
