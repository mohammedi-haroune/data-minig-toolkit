import os
import sys
import arff
import matplotlib.pyplot as plt
import numpy as np
import pathlib

def boxplot(instance):
    file = arff.load(open(instance, 'r'))
    data = np.array(file['data'])
    attributes = file['attributes']
    instance_name = os.path.basename(instance)
    directory = os.path.dirname(instance)
    pathlib.Path(directory + '/boxplots/' + instance_name).mkdir(exist_ok=True)


    all = []
    names = []
    for (i, (a, t)) in enumerate(attributes): 
        print (i, a, t)
        try:
            d = data[:, i].astype(np.float)
        except:
            continue
        
        all.append(d)
        names.append(a)
        _, ax = plt.subplots()
        ax.set_title(a)
        ax.boxplot(d, showfliers=False)
        print(directory + "/boxplots/" + instance_name + "/" + a + ".png")
        plt.savefig(directory + "/boxplots/" + instance_name + "/" + a + ".png")

    _, ax = plt.subplots()
    ax.set_title(instance_name)
    ax.boxplot(all, showfliers=False, labels=names)
    print(directory + "/boxplots/" + instance_name + "/all.png")
    plt.savefig(directory + "/boxplots/" + instance_name + "/all.png")



def hist(instance):
    file = arff.load(open(instance, 'r'))
    data = np.array(file['data'])
    attributes = file['attributes']
    instance_name = os.path.basename(instance)
    directory = os.path.dirname(instance)
    pathlib.Path(directory + '/hists/' + instance_name + "/").mkdir(exist_ok=True, parents=True)

    for (i, (a, t)) in enumerate(attributes): 
        print (i, a, t)
        #try:
        #    d = data[:, i].astype(np.float)
        #except:
        #    continue
        
        _, ax = plt.subplots()
        ax.set_title(a)
        _, b, _ = ax.hist(np.sort(data[:, i]), bins='auto')
        print(b)
        plt.setp(ax.get_xticklabels(), rotation=90, horizontalalignment='right')
        print(directory + "/hists/" + instance_name + "/" + a + ".png")
        plt.savefig(directory + "/hists/" + instance_name + "/" + a + ".png")

instance = sys.argv[1]

hist(instance)