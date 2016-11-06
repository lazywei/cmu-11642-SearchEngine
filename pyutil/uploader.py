from requests_toolbelt import MultipartEncoder
from requests.auth import HTTPBasicAuth
import requests
import re
import sys
import os

HWID = "HW4"
url = "http://boston.lti.cs.cmu.edu/classes/11-642/HW/HTS/hts.cgi"

zipPath = sys.argv[1]
zipPath = os.path.realpath(os.path.join(os.getcwd(),zipPath))
zipFile = os.path.basename(zipPath)

# testFiles = sys.argv[2].split(",")

m = MultipartEncoder(
    fields={'submissionType': 'interim', 'hwid': HWID,
            'test': 'HW4-Train-0',
                # 'HW4-Train-1',
                # 'HW4-Train-2',
                # 'HW3-Train-3',
                # 'HW3-Train-4',
                # 'HW3-Train-5',
                # 'HW3-Train-6',
                # 'HW3-Train-7',
                # 'HW3-Train-8',
                # 'HW3-Train-9',
            'infile': (zipFile, open(zipPath, 'rb'), 'application/zip')}
)

authFile = os.path.realpath(os.path.join(os.getcwd(), "pyutil/.auth"))
with open(authFile, "r") as f:
    auth = HTTPBasicAuth(f.readline().strip(), f.readline().strip())

r = requests.post(
    url, data=m, auth=auth,
    headers={
        'Content-Type': m.content_type,
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
        'Connection': 'keep-alive',
    })

# print("\n".join(r.text.split("<br />")))

pattern = re.compile("(map|P10|P20|P30)\s+all")
print("\n".join(filter(lambda x:
                       pattern.match(x) or
                       "differences" in x or
                       "%" in x or
                       "grade" in x or
                       "Test" in x, r.text.split("<br />"))))

print(r.text.split("<br />"))
