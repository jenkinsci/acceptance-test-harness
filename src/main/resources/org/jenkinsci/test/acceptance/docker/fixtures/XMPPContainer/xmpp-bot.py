#!/usr/bin/python
# based on: http://xmpppy.sourceforge.net/examples/logger.py
from xmpp import *
import time,os

#BOT=(botjid,password)
BOT=('bot@localhost','bot-pw')
#CONF=(confjid,password)
CONF=('talks@conference.localhost','')
LOGDIR='/tmp/logdir'
PROXY={}
#PROXY={'host':'192.168.0.1','port':3128,'username':'luchs','password':'secret'}
#######################################

def LOG(stanza,nick,text):
    ts=stanza.getTimestamp()
    if not ts:
        ts=stanza.setTimestamp()
        ts=stanza.getTimestamp()
    tp=time.mktime(time.strptime(ts,'%Y%m%dT%H:%M:%S %Z'))+3600*3
    if time.localtime()[-1]: tp+=3600
    tp=time.localtime(tp)
    fold=stanza.getFrom().getStripped().replace('@','%')+'_'+time.strftime("%Y.%m",tp)
    day=time.strftime("%d",tp)
    tm=time.strftime("%H:%M:%S",tp)
    try: os.mkdir(LOGDIR)
    except: pass
    fName='%s/logfile.log'%(LOGDIR)
    try: open(fName)
    except:
        open(fName,'a').write(('%s'%text).encode('utf-8'))

def messageCB(sess,mess):
    nick=mess.getFrom().getResource()
    text=mess.getBody()
    LOG(mess,nick,text)

roster=[]
def presenceCB(sess,pres):
    nick=pres.getFrom().getResource()
    text=''
    if pres.getType()=='unavailable':
        if nick in roster:
            text=nick
            roster.remove(nick)
    else:
        if nick not in roster:
            text=nick
            roster.append(nick)
    if text: LOG(pres,nick,text)

if 1:
    cl=Client(JID(BOT[0]).getDomain(),debug=[])
    cl.connect(proxy=PROXY)
    cl.RegisterHandler('message',messageCB)
    cl.RegisterHandler('presence',presenceCB)
    cl.auth(JID(BOT[0]).getNode(),BOT[1])
    p=Presence(to='%s/logger'%CONF[0])
    p.setTag('x',namespace=NS_MUC).setTagData('password',CONF[1])
    p.getTag('x').addChild('history',{'maxchars':'0','maxstanzas':'0'})
    cl.send(p)
    while 1:
        cl.Process(1)
