import numpy as np
import os
import PIL
from matplotlib import pylab
import matplotlib.pyplot as plt
import matplotlib
import random as random
import sklearn.decomposition
from sklearn import decomposition
import wave
from com.chaquo.python import Python

SUBDIRECTORY_NAME = "images"
OUTPUT_IMAGE_NAME = 'output_image.jpg'
SUBDIRECTORY_NAME_AUDIO = "audio"
OUTPUT_AUDIO_IMAGE_NAME = 'output_audio_image.jpg'


# this function collects patches from black and white images
def collectPatchesBW(numPatches, patchWidth, filePath):
    maxTries = numPatches * 50
    firstPatch = 0  # the first patch number accepted from an image
    firstTry = 0  # the first attempt to take a patch from the image
    patchCount = 0  # number of collected patches
    tryCount = 0  # number of attempted collected patches
    numPixels = patchWidth * patchWidth
    patchSample = np.zeros([patchWidth, patchWidth], 'double')
    patch = np.zeros([numPixels, 1], 'double')
    imgPatches = np.zeros([numPixels, numPatches], 'double')
    # chooses the image that we're sampling from
    image = PIL.Image.open(filePath)
    imageHeight, imageWidth, imageChannels = matplotlib.pyplot.imread(filePath).shape
    image = image.convert('L')
    image = np.asarray(image, 'double').transpose()
    # normalizing the image
    image -= image.mean()
    image /= image.std()
    while patchCount < numPatches and tryCount < numPatches:
        tryCount += 1
        if (tryCount - firstTry) > maxTries / 2 or (patchCount - firstPatch) > numPatches / 2:
            # change the image sampled from to the next in the folder
            image = PIL.Image.open(filePath)
            imageHeight, imageWidth, imageChannels = matplotlib.pyplot.imread(filePath).shape
            image = image.convert('L')
            image = np.asarray(image, 'double').transpose()
            # normalizing the image
            image -= image.mean()
            image /= image.std()
            firstPatch = patchCount
            firstTry = tryCount
        # starts patch collection in a random space
        px = np.random.randint(0, imageWidth - patchWidth)
        py = np.random.randint(0, imageHeight - patchWidth)
        patchSample = image[px:px + patchWidth, py:py + patchWidth].copy()
        patchStd = patchSample.std()
        if patchStd > 0.0:  # > 0 to remove blank/uninteresting patches for speed
            # create the patch vector
            patch = np.reshape(patchSample, numPixels)
            patch = patch - np.mean(patch)
            imgPatches[:, patchCount] = patch.copy()
            patchCount += 1
    return imgPatches


# this function displays black and white image patches
def showPatchesBW(prePatches, showPatchNum=16, display=True):
    """
    Takes in the black and white patches and transforms them.
    :param prePatches:
    :param showPatchNum:
    :param display:
    :return: an image path in android where the output image is will be stored.
    """
    patches = prePatches
    totalPatches = patches.shape[1]
    dataDim = patches.shape[0]
    patchWidth = int(np.round(np.sqrt(dataDim)))
    # extract show_patch_num patches
    displayPatch = np.zeros([dataDim, showPatchNum], float)
    # NORMALIZE PATCH LUMINANCE VALUES
    for i in range(0, showPatchNum):
        # patch_i = i * totalPatches // showPatchNum
        patch_i = i
        patch = patches[:, patch_i].copy()
        pmax = patch.max()
        pmin = patch.min()
        # fix patch range from min to max to 0 to 1
        if pmax > pmin:
            patch = (patch - pmin) / (pmax - pmin)
        displayPatch[:, i] = patch.copy()
    bw = 5  # border width
    pw = patchWidth
    patchesY = int(np.sqrt(showPatchNum))
    patchesX = int(np.ceil(float(showPatchNum) / patchesY))
    patchImg = displayPatch.max() * np.ones([(pw + bw) * patchesX - bw, patchesY * (pw + bw) - bw],
                                            float)
    for i in range(0, showPatchNum):
        y_i = i // patchesY
        x_i = i % patchesY
        reshaped = displayPatch[:, i].reshape((pw, pw))
        fullPatch = np.zeros([pw, pw], float)
        fullPatch[0:pw, :] = reshaped[:, :].copy()
        patchImg[x_i * (pw + bw):x_i * (pw + bw) + pw,
        y_i * (pw + bw):y_i * (pw + bw) + pw] = fullPatch

    # Store the image into the android application.
    pylab.imshow(patchImg.T, interpolation='nearest', cmap='bone')
    pylab.axis('off')

    context = Python.getPlatform().getApplication()
    internal_storage_dir = context.getFilesDir().getAbsolutePath()
    subdirectory_name = SUBDIRECTORY_NAME
    subdirectory_path = os.path.join(internal_storage_dir, subdirectory_name)
    if not os.path.exists(subdirectory_path):
        os.makedirs(subdirectory_path)

    image_file_name = OUTPUT_IMAGE_NAME  # Replace with your desired file name
    image_path = os.path.join(subdirectory_path, image_file_name)
    pylab.savefig(image_path)

    return image_path


# Code for the Audio Processing Part
#this function collects patches from audio clips
def collectPatchesAudio(file):
    # spf = wave.open('audio/' + file + '.wav', 'r')
    spf = wave.open(file, 'r')
    # extract raw audio from .wav file
    signal = spf.readframes(-1)
    signal = np.frombuffer(signal, 'Int16')
    fs = spf.getframerate()
    print(fs)
    numSamples = len(signal)
    width = 100
    ds = 3
    numPatches = 100000
    audioPatches = np.zeros((numPatches, width))
    for i in range(numPatches):
        x_start = np.random.randint(0, numSamples - ds * width - 2)
        audioPatches[i, :] = signal[x_start:x_start + ds * width - 1:ds]

    tmp = decomposition.FastICA(n_components=25)
    fit = tmp.fit(audioPatches)
    ica_comp = fit.components_

    return (ica_comp)

#this function displays audio patches
def showPatchesAudio(patches):
    width = 100
    cnt = 0
    for cnt in range(20):
        plt.subplot(5, 5, cnt+1)
        frame = pylab.gca()
        frame.axes.get_xaxis().set_ticklabels([])
        frame.axes.get_yaxis().set_ticklabels([])
        plt.plot(range(width),patches[cnt,:])
    plt.show()


    # Save the figure
    pylab.axis('off')

    # This part is used to store the image to the android device.
    context = Python.getPlatform().getApplication()
    internal_storage_dir = context.getFilesDir().getAbsolutePath()
    subdirectory_name = SUBDIRECTORY_NAME_AUDIO
    subdirectory_path = os.path.join(internal_storage_dir, subdirectory_name)
    if not os.path.exists(subdirectory_path):
        os.makedirs(subdirectory_path)

    output_audio_image_name = OUTPUT_AUDIO_IMAGE_NAME  # Replace with your desired file name
    audio_image_path = os.path.join(subdirectory_path, output_audio_image_name)
    pylab.savefig(audio_image_path)
    plt.close()

    return audio_image_path



