import os
import tensorflow as tf
import numpy as np
import librosa

import sounddevice as sd
import soundfile as sf
import queue, threading
from scipy.io.wavfile import write

from pydub import AudioSegment
from pydub.playback import play

import time
import serial

import tflite_runtime.interpreter as tflite

port='/dev/ttyACM0'
ser = serial.Serial(port,115200)

sr = 96000
ch = 1
subtype = 'PCM_24'
filename = 'temp.wav'

q = queue.Queue()
recorder = False
recording = False

def complicated_record():
    with sf.SoundFile("temp.wav", mode='w', samplerate=sr, subtype= subtype, channels= ch) as file:
        with sd.InputStream(samplerate=sr, dtype='int16', channels= ch, callback=complicated_save):
            while recording:
                file.write(q.get())
                
def complicated_save(indata, frames, time, status):
    q.put(indata.copy())

def start():
    global recorder
    global recording
    recording = True
    recorder = threading.Thread(target=complicated_record)
    print("start recording")
    recorder.start()

def stop():
    global recorder
    global recording
    recording = False
    recorder.join()
    print("stop recording")

start()
time.sleep(3)
stop()

#def WavToMp3(src_file, dest_file):
	#sound = AudioSegment.from_wav(src_file)
	#sound.export(dest_file, format="mp3")

#WavToMp3(src_file='temp.wav', dest_file='temp.mp3')

audio_name = os.path.join('ex_data_final.mp3')

def load_data(filename):
    audio, sample_rate = librosa.load(filename, sr=None)
    audio_16k = librosa.resample(audio, orig_sr=sample_rate, target_sr=16000)
    tensor = tf.convert_to_tensor(audio_16k, dtype=tf.float32)
    tensor = tf.reshape(tensor, shape=[-1, 1])
    tensor = tf.math.reduce_sum(tensor, axis=1) / 2
    return tensor

wav = load_data(audio_name)

avg_power_of_signal = sum(wav**2)/len(wav)

SNR_dB = 3.5

SNR_linear = 10 ** SNR_dB / 10
avg_power_of_noise = avg_power_of_signal / SNR_linear
noise = np.random.normal(0, avg_power_of_noise ** 0.5, wav.shape)
wav = (wav + noise ) * 32768.0

if len(wav) > 16000:
    sequence_stride = 16000
else:
    sequence_stride = 16000-1

def preprocess_mp3(sample, index):
    sample = sample[0]
    zero_padding = tf.zeros([16000] - tf.shape(sample), dtype=tf.float32)
    wav = tf.concat([zero_padding, sample], 0)
    spectrogram = tf.signal.stft(wav, frame_length=320, frame_step=32)
    spectrogram = tf.abs(spectrogram)
    spectrogram = tf.expand_dims(spectrogram, axis=2)
    return spectrogram

audio_slices = tf.keras.utils.timeseries_dataset_from_array(wav, wav, sequence_length=16000, sequence_stride=sequence_stride, batch_size=1)

audio_slices = audio_slices.map(preprocess_mp3)
audio_slices = audio_slices.batch(64)

interpreter = tflite.Interpreter(model_path="model.tflite")
interpreter.allocate_tensors()

input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()

data = 0
result = []

for audio_slice in audio_slices:
	for i in range(len(audio_slice)):
		input_data = audio_slice[i:i+1]
		interpreter.set_tensor(input_details[0]['index'], input_data)
		
		interpreter.invoke()
		
		output_data = interpreter.get_tensor(output_details[0]['index'])
		
		#print(output_data)
		
		yhat = [1 if prediction > 0.99 else 0 for prediction in output_data]
		
		result.extend(yhat)
		

#print(result)

if 1 in result:
	data = 1
	print("It's SNORING")
	
else:
	data = 0
	print("It's NOT SNORING")



if data == 1:
	ch = [1,1,1]
	for i in ch:
		ser.write(b'1')
		time.sleep(3)
	print("send SNORING to arduino")
else:
	ch = [0,0,0]
	for i in ch:
		ser.write(b'0')
		time.sleep(3)
	print("send NOT SNORING to arduino")


