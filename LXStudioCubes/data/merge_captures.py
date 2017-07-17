import json
import numpy as np
import sys
import cv2
from collections import Counter, defaultdict
from pprint import pprint
from copy import deepcopy
np.set_printoptions(suppress=True)
def quit(error):
	print error
	sys.exit(0)

scans = []

for fname in sys.argv[1:]:
	scans.append(json.load(open(fname)))

if len(scans) < 2:
	quit("Must provide at least 2 scans")

ids = []
cubes_by_id = defaultdict(list)

for scan in scans:
    for cube in scan:
        ids.append(cube["id"])
        cubes_by_id[cube["id"]].append(cube)


id_counter = Counter(ids)
shared = [elem for (elem, count) in id_counter.items() if count >= 2]

if len(shared) == 0:
	quit("At least 1 cube must be shared between scans")
else:
	print "Merging with %d shared cubes" % len(shared)


first_frame, second_frame = scans


merged = deepcopy(first_frame)

for cube in deepcopy(second_frame):
    if cube["id"] in shared:
        continue
        
    T = np.array(cube["transform"])
        
    avg_diff = np.zeros((4, 4), dtype=np.float64)
    
    for shared_id in shared:
        shared_cube_2 = cubes_by_id[shared_id][1]
        s = np.array(shared_cube_2["transform"])
        diff = np.linalg.solve(s, T)
        avg_diff += diff
        
    avg_diff /= len(shared)
    
    shared_cube_1 = cubes_by_id[shared_id][0]
    f = np.array(shared_cube_1["transform"])

    cube["transform"] = f.dot(avg_diff).tolist()
    
    merged.append(cube)


    
with open("merged.json", "w") as out:
    json.dump(merged, out)

print "Successfully merged %d cubes" % len(merged) 

    
