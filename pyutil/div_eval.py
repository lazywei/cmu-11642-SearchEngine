from requests_toolbelt import MultipartEncoder
from requests.auth import HTTPBasicAuth
import requests
import re
import sys
import os

HWID = "HW5"
url = "http://boston.lti.cs.cmu.edu/classes/11-642/HW/HTS/nes.cgi"

teInPath = sys.argv[1]
if len(sys.argv) == 3 and sys.argv[2].lower() == "-d":
    logtype = "Detailed"
else:
    logtype = "Summary"

teInPath = os.path.realpath(
    os.path.join(
        os.getcwd(),teInPath))
teIn = os.path.basename(teInPath)

m = MultipartEncoder(
    fields={'logtype': logtype, 'hwid': HWID,
            'infile': (teIn, open(teInPath, 'rb'), 'application/octet-stream')}
)

authFile = os.path.realpath(os.path.join(os.getcwd(), "pyutil/.auth"))
with open(authFile, "r") as f:
    auth = HTTPBasicAuth(f.readline().strip(), f.readline().strip())

r = requests.post(
    url, data=m, auth=auth,
    headers={
        'Content-Type': m.content_type,
        'Accept': "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
        'Connection': 'keep-alive',
    })


for line in r.text.split("<br>"):
    tokens = line.split(",")
    if len(tokens) > 10:
        print("{} | {} | {}".format(tokens[-5], tokens[-4], tokens[-10]))
