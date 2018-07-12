import os
import tinys3
import datetime
import sys

S3_access = 'AKIAJLOIQ2NRTISWTDLA'
S3_secret = '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'


username = sys.argv[1]
print(len(sys.argv))
if (len(sys.argv) < 2 or  len(sys.argv) > 2):
    print('need username as argument (folder name of training data)')
    sys.exit(1)

conn = tinys3.Connection(S3_access, S3_secret, tls=True)

now = datetime.datetime.now()
day = str(now.month) + '_'+ str(now.day)
PATH = "training_data/" + username + "/" + day
for file in os.listdir(PATH):
    print('file: ' + file)
    filepath = PATH + "/" + file
    f = open(filepath, 'rb')
    try:
        conn.upload(file, f, bucket='ai-vj-training-data', expires='max')
    except:
        print("failed to upload to S3")

        