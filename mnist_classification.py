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
    test_loss, test_acc = model.evaluate(testX, testY)
    print("test_loss : ", test_loss)
    print("test_acc : ", test_acc)
    # save model
    model.save('final_model.h5')


def process_InputImage(filename):
    # Open the image and convert into Numpy array
    im = Image.open(filename)
    na = np.array(im.convert('L'))

    # Stretch the contrast to range 0 to 255 to maximize chances of separating the digits from the background
    na = ((na.astype(np.float)-na.min())*255.0/(na.max()-na.min())).astype(np.uint8)

    print(f"max na value: {na.max()}")

    # Binarize image using a threshold value
    blk = np.array([0],  np.uint8)
    wht = np.array([255],np.uint8)
    thr = np.where(na>120, blk, wht)

    # Convert numpy array to PIL image object
    res = Image.fromarray(thr)

    # Get bounding box from binarized image
    bbox = res.getbbox()
    y = list(bbox)
    bbox = tuple(y)
    print('Bounding box:',bbox)

    # Apply bounding box to original image and save
    result = im.crop(bbox)
    result.save('result.jpeg')

    color_image = Image.open('result.jpeg')
    
    #convert the image to black and white mode with dither set to None
    bw = color_image.convert('1', dither=Image.NONE)

    # Convert to grayscale
    bw = color_image.convert('L')
    threshold = 90
    # Threshold
    bw = bw.point( lambda p: 255 if p > threshold else 0 )

    # Invert image colors so it can be read by the deep learning model
    bw = ImageOps.invert(bw)

    # Add padding to the inverted image
    bw = add_margin_to_image(bw, 50, 20, 50, 20, 0)
    bw.save(f'sample_bw.jpg')


def add_margin_to_image(image, top, right, bottom, left, color):
    width, height = image.size
    new_height = height + top + bottom
    new_width = left+ width + right
    result = Image.new(image.mode, (new_width, new_height), color)
    result.paste(image, (left, top))
    return result


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