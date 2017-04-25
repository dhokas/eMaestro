import random as module

print( dir(module)) # Find functions of interest.

# For each function of interest:
help(module.interesting_function)
print (module.interesting_function.func_defaults)