import ica_helper_methods
import sklearn.decomposition
import numpy as np


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


def process_image(image_path):
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