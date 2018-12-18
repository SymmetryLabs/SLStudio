#!/bin/bash
diff <(du -h shows/pilots/*.lxp) <(cat shows/pilots/lxp_proj_sizes.txt)
