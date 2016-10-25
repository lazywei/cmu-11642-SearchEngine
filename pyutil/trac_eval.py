from requests_toolbelt import MultipartEncoder
from requests.auth import HTTPBasicAuth
import requests
import re
import sys
import os

HWID = "HW3"
url = "http://boston.lti.cs.cmu.edu/classes/11-642/HW/HTS/tes.cgi"

teInPath = sys.argv[1]
if len(sys.argv) == 3 and sys.argv[2].lower() == "-s":
    logtype = "Summary"
else:
    logtype = "Detailed"

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

if logtype == "Summary":
    pattern = re.compile("(map|P10|P20|P30)\s+")
    print("\n".join(filter(pattern.match, r.text.split("<br>"))))
else:
    baseline = [0.0615, 0.1079, 0.0856, 0.2073,
                0.3413, 0.2441, 0.1995, 0.0007,
                0.0895, 0.4633, 0.0408, 0.1274,
                0.1168, 0.2120, 0.2632, 0.1241,
                0.0335, 0.0432, 0.2041, 0.0358, 0.0]
    winCnt = 0

    pattern = re.compile("(map|P10|P20|P30)\s+")
    lines = list(filter(pattern.match, r.text.split("<br>")))
    for i in range(0, len(lines), 4):
        qid = lines[i].split()[1]
        map_diff = float(lines[i].split()[2]) - baseline[i//4]
        print("\n{}: ==={}===".format(i // 4, qid))

        print("MAP\tP10\tP20\tP30\tDiff")
        print("\t".join(map(lambda _: _.split()[2],
                            lines[i:i+4]))+"\t{}".format(map_diff))

        if (qid != "all"):
            if float(lines[i].split()[2]) > baseline[i//4]:
                winCnt += 1

    print("Win Count: {}".format(winCnt))
