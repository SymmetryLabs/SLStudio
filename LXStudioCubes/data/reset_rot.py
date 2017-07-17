import json

fn = "cube_transforms.json"

with open(fn) as f:
	data = json.load(f)

for cube in data:
	if cube["debug"] != "79":
		continue

	t = cube["transform"]

	t[0][0] = 1
	t[0][1] = 0
	t[0][2] = 0

	t[1][0] = 0
	t[1][1] = 1
	t[1][2] = 0

	t[2][0] = 0
	t[2][1] = 0
	t[2][2] = 1

	print cube


json.dump(data, open(fn, "w"), indent=4, separators=(',', ': '))