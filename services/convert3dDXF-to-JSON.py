import ezdxf
import json

dwg = ezdxf.readfile("targetTetra.dxf")

lines = []


def print_entity(e):
    print("LINE on layer: %s\n" % e.dxf.layer)
    print(e.dxf.start)
    print(e.dxf.end)


def jsonify(i, e):
    lines.append({'start': e.dxf.start, 'end': e.dxf.end})

# iterate over all entities in model space
msp = dwg.modelspace()
# for e in msp:
#     if e.dxftype() == 'LINE':
#         print_entity(e)

# entity query for all LINE entities in model space
for i, e in enumerate(msp.query('LINE')):
    jsonify(i, e)

result = json.dumps({'lines': lines}, indent=4);

with open("tetrahedron.json", "w+") as f:
    f.write(result)
