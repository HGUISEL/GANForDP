# GANForDP
This tool uses a deep learning model "GAN" to resolve data imbalance problem occurred when using machine learning.
When user sets input directory path, the tool automatically resolves data imbalance by generating fake data with GAN.

# materials
- input file in csv format or arff format.

# How to use
- set input directory path, python execution path, GAN.py path and name of evaluation classifier.
ex) -i "./datafiles/" -e "/home/jinyi/anaconda2/envs/tensorflow/bin/python3" -p "./GAN.py" -c "weka.classifiers.bayes.NaiveBayes"



Please report any issues to jinyi1187@gmail.com
Thank you.
