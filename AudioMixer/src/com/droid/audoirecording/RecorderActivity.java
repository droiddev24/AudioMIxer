package com.droid.audoirecording;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.res.Resources.NotFoundException;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class RecorderActivity extends Activity implements OnClickListener {

	private MediaRecorder mediaRecorder;
	private MediaPlayer mediaPlayer;
	private MediaRecorder mRecorder = null;

	private File file;
	private float adjuster=0.0f;
	private boolean isRecording=false;
	private boolean isPlaying=false;
	private final String LOG_TAG="RecrderActivity";
	private String recordingName="";
	private long recordingDuration=0;
	private final int FREQUENCY = 44100;
	private final int CHANNEL_CONFIGURATION = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	private final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private ArrayList<Long> instrumentClickedOn=new ArrayList<Long>();
	private HashMap<Integer, Float> posToDurMap=new HashMap<Integer, Float>(); 

	private Button startRecBt;
	private Button stopRecBt;
	private Button playAudioBt;
	private Button stopPlayingBt;
	private Button mixSoundBt;
	private Button startMixingBt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recorder);

		startRecBt=(Button)findViewById(R.id.bt_start_recording);
		startRecBt.setOnClickListener(this);
		stopRecBt=(Button)findViewById(R.id.bt_stop_recording);
		stopRecBt.setOnClickListener(this);
		playAudioBt=(Button)findViewById(R.id.bt_play_audio);
		playAudioBt.setOnClickListener(this);
		stopPlayingBt=(Button)findViewById(R.id.bt_stop_playing);
		stopPlayingBt.setOnClickListener(this);
		mixSoundBt=(Button)findViewById(R.id.bt_mix_sound);
		mixSoundBt.setOnClickListener(this);
		startMixingBt=(Button)findViewById(R.id.bt_start_mixing);
		startMixingBt.setOnClickListener(this);
		
		recordingName=getFilesDir().getAbsolutePath()+"/"+
				System.currentTimeMillis()+".wav";

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(mediaRecorder!=null){
			mediaRecorder.release();
			mediaRecorder=null;
		}
		if(mediaPlayer!=null){
			mediaPlayer.release();
			mediaPlayer=null;
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.bt_start_recording:
			if(!isRecording)
//				new Runnable() {
//				public void run() {
//					recordSound();
//				}
//			};
				startRecording();
			break;
		case R.id.bt_stop_recording:
			if(isRecording)
//				isRecording=false;
				stopRecording();
			break;
		case R.id.bt_play_audio:
			if(!isPlaying)
				startPlayingAudio();
			break;
		case R.id.bt_stop_playing:
			if(isPlaying)
				stopPlayingAudio();
			break;
		case R.id.bt_mix_sound:
			instrumentClickedOn.add((long) mediaPlayer.getCurrentPosition());
			break;
		case R.id.bt_start_mixing:
			mediaPlayer.release();
			mediaPlayer=null;
			isPlaying=false;
			mixFiles();
			break;
		}
	}

	private void startPlayingAudio(){
		mediaPlayer=new MediaPlayer();
		try{
			mediaPlayer.setDataSource(recordingName);
			mediaPlayer.prepare();
			mediaPlayer.start();
			recordingDuration=mediaPlayer.getDuration();
		}
		catch(IOException e){
			Log.e(LOG_TAG, "mediaPlayer.start() failed");
			e.printStackTrace();
		}
		
		//complete position to duration map
		FileInputStream recordInputStream = null;
		try {
			recordInputStream= new FileInputStream(recordingName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		short[] recordedSound = null;
		try {
			recordedSound = FileToArrayConverter.sampleToShortArray(recordInputStream,
					false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		adjuster=(float)(mediaPlayer.getDuration()-recordedSound.length)/recordedSound.length;
		float dur=0.0f;
		for(int i=0; i<recordedSound.length; i++){
			dur=(dur+adjuster);
			posToDurMap.put(i, dur);
			dur++;
		}
		isPlaying=true;
	}

	private void stopPlayingAudio(){
		mediaPlayer.release();
		mediaPlayer=null;
		isPlaying=false;
	}
	
	private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(recordingName);
        mRecorder.setAudioSamplingRate(FREQUENCY);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
        isRecording=true;
    }
	
	private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        isRecording=false;
    }

	private void recordSound(){
		file = new File(recordingName);

		// Delete any previous recording.
		if (file.exists())
			file.delete();

		try {
			file.createNewFile();

			// Create a DataOuputStream to write the audio data into the saved file.
			OutputStream os = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(os);
			DataOutputStream dos = new DataOutputStream(bos);

			// Create a new AudioRecord object to record the audio.
			int bufferSize = AudioRecord.getMinBufferSize(FREQUENCY, CHANNEL_CONFIGURATION,
					AUDIO_ENCODING);
			AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
					FREQUENCY, CHANNEL_CONFIGURATION, AUDIO_ENCODING, bufferSize);

			short[] buffer = new short[bufferSize]; 
			audioRecord.startRecording();
			isRecording=true;

			while (isRecording) {
				int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
				for (int i = 0; i < bufferReadResult; i++)
					dos.writeShort(buffer[i]);
			}


			audioRecord.stop();
			audioRecord.release();
			dos.close();
			Log.e(LOG_TAG, buffer.toString());
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("Recording sound error", e.getMessage());
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("Recording sound error", e.getMessage());
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("Recording sound error", e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("Recording sound error", e.getMessage());
		}

	}

	private void mixFiles(){
		try {
			FileInputStream recordedFileInputStream = null;
			FileInputStream is1 = (FileInputStream) getResources().openRawResource(R.raw.test_sound);

			File recordedFile = new File(recordingName);
			try {
				 recordedFileInputStream= new FileInputStream(recordedFile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			short[] recordedSound=FileToArrayConverter.sampleToShortArray(recordedFileInputStream,
					false);
			short[] instrument=FileToArrayConverter.sampleToShortArray(is1, false);

			short[] output = new short[recordedSound.length];
			for(int i=0; i < recordedSound.length; i++){

				float samplef1;
				float samplef2;
				if(instrumentClickedOn.contains(posToDurMap.get(i))){
					samplef1=recordedSound[i] / 32768.0f;
					samplef2=instrument[i] / 32768.0f;
				}
				else{
					samplef1=recordedSound[i] / 32768.0f;
					samplef2=recordedSound[i] / 32768.0f;
				}

				float mixed = samplef1 + samplef2;
				// reduce the volume a bit:
					mixed *= 0.8;
				// hard clipping
				if (mixed > 1.0f) mixed = 1.0f;
				if (mixed < -1.0f) mixed = -1.0f;
				short outputSample = (short)(mixed * 32768.0f);


				output[i] = outputSample;
			}
			saveMixedAudio(output);
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void saveMixedAudio(short[] mixedArray){
		File finalOutputFile=new File(getFilesDir()+"/final recording.3gp");

		// Delete any previous saved file with same name.
		if (finalOutputFile.exists())
			finalOutputFile.delete();

		try {
			finalOutputFile.createNewFile();

			// Create a DataOuputStream to write the mixed audio into the saved file.
			OutputStream os = new FileOutputStream(finalOutputFile);
			BufferedOutputStream bos = new BufferedOutputStream(os);
			DataOutputStream dos = new DataOutputStream(bos);

			for(int i=0; i<mixedArray.length; i++){
				dos.writeShort(mixedArray[i]);
			}
			dos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
