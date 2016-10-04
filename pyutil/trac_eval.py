from requests_toolbelt import MultipartEncoder
from requests.auth import HTTPBasicAuth
import requests
import re
import sys
import os

HWID = "HW2"
url = "http://boston.lti.cs.cmu.edu/classes/11-642/HW/HTS/tes.cgi"

teInPath = sys.argv[1]


teInPath = os.path.realpath(
    os.path.join(
        os.getcwd(),teInPath))
teIn = os.path.basename(teInPath)

m = MultipartEncoder(
    fields={'logtype': 'Summary', 'hwid': HWID,
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

pattern = re.compile("(map|P10|P20|P30)\s+all")
print("\n".join(filter(pattern.match, r.text.split("<br>"))))
