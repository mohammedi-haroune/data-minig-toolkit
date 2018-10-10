import os
import sys
import arff
import matplotlib.pyplot as plt
import numpy as np

instance = sys.argv[1]
directory = os.path.dirname(instance)

file = arff.load(open(instance, 'r'))
data = np.array(file['data'])
attributes = file['attributes']
os.mkdir(directory + '/boxplots')


all = []
for (i, (a, t)) in enumerate(attributes): 
    print (i, a, t)
    try:
        d = data[:, i].astype(np.float)
    except:
        continue
    all.append(d)
    _, ax = plt.subplots()
    ax.set_title(a)
    ax.boxplot(d, showfliers=False)
    print(directory + "/boxplots/" + a + ".png")
    plt.savefig(directory + "/boxplots/" + a + ".png")

_, ax = plt.subplots()
ax.set_title(os.path.basename(instance))
ax.boxplot(all, showfliers=False)
print(directory + "/boxplots/all.png")
plt.savefig(directory + "/boxplots/all.png")