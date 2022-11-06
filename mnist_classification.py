from tensorflow.keras.datasets import mnist
from tensorflow.keras.utils import to_categorical
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Conv2D
from tensorflow.keras.layers import MaxPooling2D
from tensorflow.keras.layers import Dense
from tensorflow.keras.layers import Flatten
from tensorflow.keras.optimizers import SGD
from PIL import Image
import numpy as np
from PIL import ImageOps
from numpy import argmax
from tensorflow.keras.utils import load_img
from tensorflow.keras.utils import img_to_array
from keras.models import load_model
import cv2
import sys
import os.path

def load_dataset():
	# load dataset
	(trainX, trainY), (testX, testY) = mnist.load_data()
	# reshape dataset to have a single channel
	trainX = trainX.reshape((trainX.shape[0], 28, 28, 1))
	testX = testX.reshape((testX.shape[0], 28, 28, 1))
	# one hot encode target values
	trainY = to_categorical(trainY)
	testY = to_categorical(testY)
	return trainX, trainY, testX, testY

def prep_pixels(train, test):
	# convert from integers to floats
	train_norm = train.astype('float32')
	test_norm = test.astype('float32')
	# normalize to range 0-1
	train_norm = train_norm / 255.0
	test_norm = test_norm / 255.0
	# return normalized images
	return train_norm, test_norm

# def define_model():
# 	model = Sequential()
# 	model.add(Conv2D(32, (3, 3), activation='relu', kernel_initializer='he_uniform', input_shape=(28, 28, 1)))
# 	model.add(MaxPooling2D((2, 2)))
# 	model.add(Conv2D(64, (3, 3), activation='relu', kernel_initializer='he_uniform'))
# 	model.add(Conv2D(64, (3, 3), activation='relu', kernel_initializer='he_uniform'))
# 	model.add(MaxPooling2D((2, 2)))
# 	model.add(Flatten())
# 	model.add(Dense(100, activation='relu', kernel_initializer='he_uniform'))
# 	model.add(Dense(10, activation='softmax'))
# 	# compile model
# 	opt = SGD(learning_rate=0.01, momentum=0.9)
# 	model.compile(optimizer=opt, loss='categorical_crossentropy', metrics=['accuracy'])
# 	return model

def run_test_harness():
    # load dataset
    trainX, trainY, testX, testY = load_dataset()
    # prepare pixel data
    trainX, testX = prep_pixels(trainX, testX)
    # define model
    model = Sequential()
    model.add(Conv2D(32, (3, 3), activation='relu', kernel_initializer='he_uniform', input_shape=(28, 28, 1)))
    model.add(MaxPooling2D((2, 2)))
    model.add(Conv2D(64, (3, 3), activation='relu', kernel_initializer='he_uniform'))
    model.add(Conv2D(64, (3, 3), activation='relu', kernel_initializer='he_uniform'))
    model.add(MaxPooling2D((2, 2)))
    model.add(Flatten())
    model.add(Dense(100, activation='relu', kernel_initializer='he_uniform'))
    model.add(Dense(10, activation='softmax'))
    # compile model
    opt = SGD(learning_rate=0.01, momentum=0.9)
    model.compile(optimizer=opt, loss='categorical_crossentropy', metrics=['accuracy'])
    model.summary()

    # fit model
    model.fit(trainX, trainY, epochs=10, batch_size=32, verbose=0)
    # save model
    model.save('final_model.h5')

def process_InputImage(filename):
    # Load image, and make into Numpy array
    im = Image.open(filename)
    na = np.array(im.convert('L'))

    # Stretch the contrast to range 0..255 to maximize chances of splitting digits from background
    na = ((na.astype(np.float)-na.min())*255.0/(na.max()-na.min())).astype(np.uint8)

    print(f"max na value: {na.max()}")

    # Threshold image to pure black and white
    blk = np.array([0],  np.uint8)
    wht = np.array([255],np.uint8)
    thr = np.where(na>100, blk, wht)

    # Go back to PIL Image from Numpy array
    res = Image.fromarray(thr)

    # Get bounding box from thresholded image
    bbox = res.getbbox()
    print(bbox)
    y = list(bbox)
    y[0] = y[0] - 50
    y[1] = y[1] - 50
    y[2] = y[2] + 5
    y[3] = y[3] + 50
    bbox = tuple(y)
    print('Bounding box:',bbox)

    # Apply bounding box to original image and save
    result = im.crop(bbox)
    result.save('result.jpeg')

    color_image = Image.open('result.jpeg')
    
    #convert the image to black and white mode with dither set to None
    bw = color_image.convert('1', dither=Image.NONE)

    # Grayscale
    bw = color_image.convert('L')
    threshold = 90
    # # Threshold
    bw = bw.point( lambda p: 255 if p > threshold else 0 )
    bw = ImageOps.invert(bw)
    bw.save(f'sample_bw.jpg')

def process_InputImage1(filename):
  image= cv2.imread(filename)
  original_image= image

  gray= cv2.cvtColor(image,cv2.COLOR_BGR2GRAY)

  gray = cv2.medianBlur(gray,7)

  # blur = cv2.GaussianBlur(gray,(5,5),0)
  # cv2.imwrite('blur.jpg', blur)
  # ret ,thresh = cv2.threshold(blur,0,255,cv2.THRESH_BINARY+cv2.THRESH_OTSU)


  thresh = cv2.adaptiveThreshold(gray,255,cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY,7,4)
  kernel = np.ones((3,3), np.uint8)
  thresh = cv2.erode(thresh, kernel, iterations=2)

  cv2.imwrite('thresh.jpg', thresh)

  edges= cv2.Canny(thresh, 50,200)


  contours, hierarchy= cv2.findContours(edges.copy(), cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_NONE)


  sorted_contours= sorted(contours, key=cv2.contourArea, reverse= True)

  print(len(sorted_contours))

  for (i,c) in enumerate(sorted_contours):
      x,y,w,h= cv2.boundingRect(c)
      if i==0:
        k = 5
        cropped_contour= thresh[y-k:y+h+k, x-k:x+w+k]
        image_name= 'sample_bw.jpg'
        cropped_contour = cv2.bitwise_not(cropped_contour)
        cv2.imwrite(image_name, cropped_contour)
        break


def load_image(filename):
  img = load_img(filename, color_mode="grayscale", target_size=(28, 28))
  img = img_to_array(img)
  img = img.reshape(1, 28,28,1)
  img = img.astype('float32')
  img = img / 255.0
  return img
     
def run_example(file):
  if not os.path.isfile('final_model.h5'):
    print("Training the Model")
    run_test_harness()
  process_InputImage(file)
  img = load_image('sample_bw.jpg')
  model = load_model('final_model.h5')
  predict_value = model.predict(img)
  digit = argmax(predict_value)
  print(digit)
  return digit

if __name__ == "__main__":
  fileName = sys.argv[1]
  run_example(fileName)