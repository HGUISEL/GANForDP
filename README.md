# GANForDP
This tool uses a deep learning model "GAN" to resolve data imbalance problem occurred when using machine learning.
When user sets input directory path, the tool automatically resolves data imbalance by generating fake data with GAN.

# Generative Adversarial Networks?
A generative adversarial network (GAN) is a class of machine learning systems invented by Ian Goodfellow and his colleagues in 2014. Two neural networks contest with each other in a game (in the sense of game theory, often but not always in the form of a zero-sum game). Given a training set, this technique learns to generate new data with the same statistics as the training set.

# Why GAN?
GAN has shown a great performance on generating new image data. Thus, we thought that GAN could make some new data that can resolve the imbalance problem occurred while using maching learning for bug prediction.

# materials
- input file must be in csv format or arff format.

# How to use
- set input directory path, python execution path, GAN.py path and name of evaluation classifier.
ex) -i "./datafiles/" -e "/home/jinyi/anaconda2/envs/tensorflow/bin/python3" -p "./GAN.py" -c "weka.classifiers.bayes.NaiveBayes"



Please report any issues to jinyi1187@gmail.com
Thank you.
