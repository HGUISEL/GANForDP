# -*- coding: utf-8 -*-
"""
    Spyder Editor
    
    Revised GAN for resolving imbalance problems occurred in Bug Prediction.
    """
#%% import packages
import tensorflow as tf
import numpy as np
import csv
import sys
#from sklearn.preprocessing import MinMaxScaler
#%% data used for load

#####input file address
input_address = sys.argv[1]
print("input file name : ", sys.argv[1])
output_address = sys.argv[2]
print("output file name : ", sys.argv[2])
generating_num = (int)(sys.argv[3])
print("generating num : ", sys.argv[3])

######counting number of instances in dataset
ins_num = 0 #ins_num == lines_num


with open(input_address) as f:
    lines = f.readlines()
    

ins_num = len(lines)

#print("number of lines : ", ins_num)
######counting number of features for an instance
comma_line = lines[0].split(",")
tuple = tuple(comma_line)
feat_num = len(tuple)
#print("features : ", feat_num)

train_x = np.loadtxt(input_address, delimiter=",", dtype=np.float32)
#train_x = train_x.strip("\"")
#train_x = float(train_x)
#normalizing train data
#train_x = MinMaxScaler().fit_transform(train_x)
#print('Normalized train_x : ', train_x)
#%% hyper-parameters setting
total_epochs = 104
batch_size = ins_num
learning_rate = 0.0002

#%% Generator

def generator(z, reuse = False) :  #WHY WHY WHY????????????????????????????????????????????????
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
    #hidden = tf.Print(hidden,[hidden],"This is hidden layer of generator with relu : ")
    output = tf.nn.sigmoid(tf.matmul(hidden, gw2) + gb2)
    #output = tf.nn.swish(tf.matmul(hidden, gw2) + gb2)
    #output = tf.nn.relu(tf.matmul(hidden, gw2) + gb2)
    #output = tf.Print(output,[output],"This is output of generator without sigmoid : ")
    #output = tf.Print(output,[output],"This is output of generator with sigmoid : ")
    
    return output #[784]가짜 생성된 이미지

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
#####################################################################################값 뽑아서 확인 stddev 지원? 1 이상
            
            
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
   # hidden = tf.Print(hidden,[hidden],"This is hidden layer of discriminator with relu : ")
    # output = tf.matmul(hidden, dw2) + db2 #[-, 1] 진품인지(1) 가품인지(0)의 label 결과 값
    output = tf.nn.sigmoid(tf.matmul(hidden, dw2) + db2) #[-, 1] 진품인지(1) 가품인지(0)의 label 결과 값
   # output = tf.Print(output,[output],"This is output of sigmoid discriminator : ")


    return output

#%% module3 :random noise 생성기

def random_noise(batch_size) :
    
    return np.random.normal(size = [batch_size, 128]) #랜덤 샘플로 Gaussian 분포를 [batch_size,128]크기로 생성. output은 normal distribution의 ndarray

#%% Graph 짜기

g = tf.Graph()

with g.as_default() :
    
    #####################################################
    
        X = tf.placeholder(tf.float32, [None, feat_num])
    
        Z = tf.placeholder(tf.float32, [None, 128])
        
        #####################################################
        # 2. generator와 discriminator의 사용
        #####################################################
        
        fake_x = generator(Z)
        
        result_of_fake = discriminator(fake_x)
       # result_of_fake = tf.Print(result_of_fake,[result_of_fake],"Result of fake : ")
       # result_of_fake = tf.Print(result_of_fake,[result_of_fake],"Result of fake : ")
        result_of_real = discriminator(X, True)
       # result_of_real = tf.Print(result_of_real,[result_of_real],"Result of real : ")
       # result_of_real = tf.Print(result_of_real,[result_of_real],"Result of real : ")
            
                   
        
        
        #####################################################
        # 3. Loss(성취도 평가) : g_loss와 d_loss
        #####################################################
        
        g_loss = tf.reduce_mean(tf.log(result_of_fake + 1e-9))
        g_loss = tf.Print(g_loss,[g_loss],"g_loss : ")
        #1e-9를 더한 이유: result_of_fake의 값이 0에 가깝게 나오면,log 연산을 했을 때 값이 무한정으로 작아짐. 이를 방지하기 위해 더한 것임.
        #tf.reduce_mean: 차원을 축소하여 평균을 구하는 것. (맵리듀스의 개념)
        #g_loss는 reduce_mean을 이용하여 loss값들의 평균을 구하는 것이라고 하는데, 그렇다면 result_of_fake가 loss값들의 집합이란 이야기인가...?
        #log 사용이유: loss값이 cross entropy를 구하는 것인데, 이 수식에 log 연산이 들어 있음. 그래서 log를 사용하는 것.
        #cross entropy는 실제 값(확률)과 예측값의 차이를 구하는 것이므로 그값이 최소가 되는 것이 좋은 것이다. 따라서 g_loss는 result_of_fake값들이 각각의 loss값이라고 했을 때, 그 값들의 평균을 구해 loss값을 최소화 하는 것을 목표로 하는 것이다.  
    
        d_loss = tf.reduce_mean(tf.log(result_of_real + 1e-9) + tf.log(1 - result_of_fake + 1e-9) )
        d_loss = tf.Print(d_loss,[d_loss],"d_loss : ")
        
        #####################################################
        # 4. Train : Maximizing g_loss & d_loss
        #####################################################
        
        t_vars = tf.trainable_variables() # return list
        
        g_vars = [var for var in t_vars if "Gen" in var.name]
        # g_vars = [gw1, gb1, gw2, gb2]
        d_vars = [var for var in t_vars if "Dis" in var.name]
        # d_vars = [dw1, db1, dw2, db2]
        
        optimizer = tf.train.AdamOptimizer(learning_rate) #AdamOptimizer는 매개변수마다 업데이트 속도를 최적으로 조절하는 최적화 기법
        
        g_train = optimizer.minimize(-g_loss, var_list = g_vars)#var_list = {gw1,gw2,gb1,gb2} --> loss를 최소화 하기 위해 업데이트 시켜야 할 object. g_loss를 최소화하는 방향으로 var_list를 업데이트 시킴
        d_train = optimizer.minimize(-d_loss, var_list = d_vars)#var_list = {dw1,dw2,db1,db2} --> loss를 최소화 하기 위해 업데이트 시켜야 할 object. d_loss를 최소화하는 방향으로 var_list를 업데이트 시킴

#%% Graph Run 해서 반복 training으로 variable update

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

        if (epoch) % 1 == 0 or epoch == 1  : #if (epoch+1) % 5 == 0 or epoch == 1  :
            print("=======Epoch : ", epoch , " =======================================")
            print("Performance of Generator : " ,gl )
            print("Performance of Discriminator : " ,dl )
            print("Gen and Dis Competing...")


#        if epoch == 0 or (epoch + 1) % 10 == 0  : 
        if epoch == 0 or epoch % total_epochs == 0 : 
            sample_noise = np.random.normal(size=(generating_num, 128))
            
            generated = sess.run(fake_x , feed_dict = { Z : sample_noise})
    
            csvfile = open(output_address,'a', newline = '') #a ->> w
            csvwriter = csv.writer(csvfile)
            for row in generated:
                csvwriter.writerow(row)
          
            csvfile.close()
            


    print('Optimization Completed!')
