import ica_helper_methods
import sklearn.decomposition
import numpy as np
from io import BytesIO
from PIL import Image
import os
from com.chaquo.python import Python



SUBDIRECTORY_NAME_TMP_IMAGES = 'temporary_images'
TMP_IMAGE_NAME = 'image_selected_from_gallery.jpg'

def process_captured_image(image_path):
    num_patches = 50000
    patch_width = 16
    num_patches_to_show = 25

    patches_bw_natural = ica_helper_methods.collectPatchesBW(num_patches, patch_width, image_path)

    fast_ica_decomposed = sklearn.decomposition.FastICA(n_components=75)
    fast_ica_fit = fast_ica_decomposed.fit(np.transpose(patches_bw_natural))
    ica_comp = fast_ica_fit.components_

    output_processed_image_path = ica_helper_methods.showPatchesBW(np.transpose(ica_comp),
                                                                   num_patches_to_show)

    return output_processed_image_path


def process_selected_image(image_bytes):
    image = Image.open(BytesIO(image_bytes))

    # This part is used to store the image to the android device.
    context = Python.getPlatform().getApplication()
    internal_storage_dir = context.getFilesDir().getAbsolutePath()
    subdirectory_path = os.path.join(internal_storage_dir, SUBDIRECTORY_NAME_TMP_IMAGES)
    if not os.path.exists(subdirectory_path):
        os.makedirs(subdirectory_path)

    image_path = os.path.join(subdirectory_path, TMP_IMAGE_NAME)
    image.save(image_path)

    num_patches = 50000
    patch_width = 16
    num_patches_to_show = 25

    patches_bw_natural = ica_helper_methods.collectPatchesBW(num_patches, patch_width, image_path)

    fast_ica_decomposed = sklearn.decomposition.FastICA(n_components=75)
    fast_ica_fit = fast_ica_decomposed.fit(np.transpose(patches_bw_natural))
    ica_comp = fast_ica_fit.components_

    output_processed_image_path = ica_helper_methods.showPatchesBW(np.transpose(ica_comp),
                                                                   num_patches_to_show)

    return output_processed_image_path


def process_audio(audio_file_path):
    audio_patches = ica_helper_methods.collectPatchesAudio(audio_file_path)
    output_processed_audio_image_path = ica_helper_methods.showPatchesAudio(audio_patches)

    return output_processed_audio_image_path