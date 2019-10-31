# -*- coding: utf-8 -*-
#%% import packages
import tensorflow as tf
import numpy as np
import csv
import sys
#%% data used for load
#####input file address
input_address = sys.argv[1]
print("input file name : ", sys.argv[1])
output_address = sys.argv[2]
print("output file name : ", sys.argv[2])
generating_num = (int)(sys.argv[3])
print("generating num : ", sys.argv[3])

######counting number of instances in dataset
ins_num = 0 

with open(input_address) as f:
    lines = f.readlines()
    

ins_num = len(lines)

######counting number of features for an instance
comma_line = lines[0].split(",")
tuple = tuple(comma_line)
feat_num = len(tuple)

train_x = np.loadtxt(input_address, delimiter=",", dtype=np.float32)
#%% hyper-parameters setting
total_epochs = 104
batch_size = ins_num
learning_rate = 0.0002

#%% Generator

def generator(z, reuse = False) :
    if reuse == False :
        with tf.variable_scope(name_or_scope = "Gen") as scope :
            gw1 = tf.get_variable(name = "w1",
                                  shape = [128,256],
                                  initializer = tf.random_normal_initializer(
                                                                             mean = 0.0, stddev = 0.01))
                                      
            gb1 = tf.get_variable(name = "b1",
                                   shape = [256],
                                   initializer = tf.random_normal_initializer(
                                                                              mean = 0.0, stddev = 0.01))
             
            gw2 = tf.get_variable(name = "w2",
                                   shape = [256, feat_num],
                                   initializer = tf.random_normal_initializer(
                                                                              mean = 0.0, stddev = 0.01))
             
            gb2 = tf.get_variable(name = "b2",
                                   shape = [feat_num], #[None, 26]
                                   initializer = tf.random_normal_initializer(
                                                                              mean = 0.0, stddev = 0.01))
    else :
        with tf.variable_scope(name_or_scope = "Gen", reuse = True) as scope:
            
            gw1 = tf.get_variable(name = "w1",
                                  shape = [128, 256],
                                  initializer = tf.random_normal_initializer(
                                                                             mean = 0.0, stddev = 0.01))

            gb1 = tf.get_variable(name = "b1",
                                   shape = [256],
                                   initializer = tf.random_normal_initializer(
                                                                               mean = 0.0, stddev = 0.01))
             
            gw2 = tf.get_variable(name = "w2",
                                   shape = [256, feat_num],
                                   initializer = tf.random_normal_initializer(
                                                                              mean = 0.0, stddev = 0.01))
             
            gb2 = tf.get_variable(name = "b2",
                                   shape = [feat_num],
                                   initializer = tf.random_normal_initializer(
                                                                              mean = 0.0, stddev = 0.01))



    hidden = tf.nn.relu(tf.matmul(z, gw1) + gb1)
    output = tf.nn.sigmoid(tf.matmul(hidden, gw2) + gb2)
    
    return output

#%% Discriminator

def discriminator(x, reuse = False) :
    
    if(reuse == False ) :
        with tf.variable_scope(name_or_scope = "Dis") as scope :
            
            dw1 = tf.get_variable(name = "w1",
                                  shape = [feat_num, 256],
                                  initializer = tf.random_normal_initializer(0.0, 0.01))
                
            db1 = tf.get_variable(name = "b1",
                                shape = [256],
                                initializer = tf.random_normal_initializer(0.0, 0.01))
          
            dw2 = tf.get_variable(name = "w2",
                                shape = [256, 1],
                                initializer = tf.random_normal_initializer(0.0, 0.01))
          
            db2 = tf.get_variable(name = "b2",
                                shape = [1],
                                initializer = tf.random_normal_initializer(0.0, 0.01))
    else :
        with tf.variable_scope(name_or_scope = "Dis", reuse = True) as scope :
            
            dw1 = tf.get_variable(name = "w1",
                                  shape = [feat_num, 256],
                                  initializer = tf.random_normal_initializer(0.0, 0.01))

            db1 = tf.get_variable(name = "b1",
                                shape = [256],
                                initializer = tf.random_normal_initializer(0.0, 0.01))
          
            dw2 = tf.get_variable(name = "w2",
                                shape = [256, 1],
                                initializer = tf.random_normal_initializer(0.0, 0.01))
          
            db2 = tf.get_variable(name = "b2",
                                shape = [1],
                                initializer = tf.random_normal_initializer(0.0, 0.01))

    hidden = tf.nn.relu(tf.matmul(x, dw1) + db1 ) #[-, 256]
    output = tf.nn.sigmoid(tf.matmul(hidden, dw2) + db2)

    return output

#%% module3 :random noise generator

def random_noise(batch_size) :
    
    return np.random.normal(size = [batch_size, 128])

#%% Graph

g = tf.Graph()

with g.as_default() :
    
        X = tf.placeholder(tf.float32, [None, feat_num])
    
        Z = tf.placeholder(tf.float32, [None, 128])
        
        fake_x = generator(Z)
        
        result_of_fake = discriminator(fake_x)
        result_of_real = discriminator(X, True)
            
        g_loss = tf.reduce_mean(tf.log(result_of_fake + 1e-9))
        g_loss = tf.Print(g_loss,[g_loss],"g_loss : ")
    
        d_loss = tf.reduce_mean(tf.log(result_of_real + 1e-9) + tf.log(1 - result_of_fake + 1e-9) )
        d_loss = tf.Print(d_loss,[d_loss],"d_loss : ")
        
        t_vars = tf.trainable_variables() # return list
        
        g_vars = [var for var in t_vars if "Gen" in var.name]
        d_vars = [var for var in t_vars if "Dis" in var.name]
        
        optimizer = tf.train.AdamOptimizer(learning_rate)
        
        g_train = optimizer.minimize(-g_loss, var_list = g_vars)
        d_train = optimizer.minimize(-d_loss, var_list = d_vars)

#%% Graph Run

with tf.Session(graph = g) as sess :
    sess.run(tf.global_variables_initializer())

    total_batchs = int(train_x.shape[0] / batch_size)

    for epoch in range(total_epochs) :

        for batch in range(total_batchs) :
            batch_x = train_x[batch * batch_size : (batch+1) * batch_size]  # [batch_size , 784]
            noise = random_noise(batch_size)  # [batch_size, 128]

            sess.run(g_train , feed_dict = {Z : noise})
            sess.run(d_train, feed_dict = {X : batch_x , Z : noise})

            gl, dl = sess.run([g_loss, d_loss], feed_dict = {X : batch_x , Z : noise})

        if (epoch) % 1 == 0 :
            print("=======Epoch : ", epoch , " =======================================")
            print("Performance of Generator : " ,gl )
            print("Performance of Discriminator : " ,dl )
            print("Gen and Dis Competing...")


        if epoch == 0 or epoch % total_epochs == 0 : 
            sample_noise = np.random.normal(size=(generating_num, 128))
            
            generated = sess.run(fake_x , feed_dict = { Z : sample_noise})
    
            csvfile = open(output_address,'a', newline = '')
            csvwriter = csv.writer(csvfile)
            for row in generated:
                csvwriter.writerow(row)
          
            csvfile.close()
            


    print('Optimization Completed!')
