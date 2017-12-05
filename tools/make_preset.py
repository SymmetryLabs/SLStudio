import json
import os
import sys

def get_presets(data, pat):
    for channel in data['engine']['channels']:
        for pattern in channel['patterns']:
            if pattern['class'].split('$')[-1] == pat:
                return get_parameters(pattern['parameters'])

def get_parameters(params):
    result = ''
    for name, value in sorted(params.items()):
        if name == 'label':
            continue
        if value in [True, False]:
            value = int(value)
        if isinstance(value, float):
            value = '%.3f' % value
        result += 'parameters.get("%s").setValue(%s);\n' % (name, value)
    return result

def get_derived_class(data, pattern, name):
    return '''\
public class %s extends %s {
  public %s(LX lx) {
    super(lx);

    %s
  }
}
''' % (name, pattern, name, get_presets(data, pattern).strip().replace('\n', '\n    '))

args = sys.argv[1:]
pattern = args.pop(0)
for path in args:
    name = pattern + os.path.basename(path).split('.')[0].capitalize()
    data = json.load(open(path))
    print(get_derived_class(data, pattern, name))
