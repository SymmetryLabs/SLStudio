import json
import numpy as np
from pprint import pprint
from transformations import euler_from_matrix, euler_matrix, reflection_matrix

infile = open("cube_transforms.json")


raw = json.loads(infile.read())

snapped = list()

def zero_small_nums(ar):
	lim = 10 ** -10
	squash = lambda x: x if abs(x) > lim else 0
	vectored = np.vectorize(squash)
	return vectored(ar)

sum_t = np.zeros(3)


for cube in raw:
	transform = np.array(cube["transform"])
	rot = transform[:3, :3]
	t = transform[:3, 3]

	x, y, z = euler_from_matrix(transform, "rxyz")
	cube["euler"] =[x, y, z]

outfile = open("cube_transforms.json", "w")
json.dump(raw, outfile)
