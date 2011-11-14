package com.runninghusky.spacetracker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

public class PhotoLoggerActivity extends Activity{
	private SurfaceView preview=null;
	private SurfaceHolder previewHolder=null;
	private Camera camera=null;
	private static Bitmap resizedBitmap=null;
	private boolean takingShot = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cameralogger);
		Toast.makeText(getBaseContext(), "Tap the screen to take a picture", Toast.LENGTH_LONG).show();
		preview=(SurfaceView)findViewById(R.id.SurfaceViewPicture);
		previewHolder=preview.getHolder();
		previewHolder.addCallback(surfaceCallback);
		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		preview.setOnClickListener(onAdd);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode==KeyEvent.KEYCODE_CAMERA || keyCode==KeyEvent.KEYCODE_SEARCH || keyCode==KeyEvent.KEYCODE_DPAD_CENTER) {
			if(!takingShot) 
				takePicture();
			 return(true);
		}
		
		return(super.onKeyDown(keyCode, event));
	}
	private View.OnClickListener onAdd=new View.OnClickListener() {
		public void onClick(View v) {
			System.out.println("View.OnClickListener onAdd called");
			if(!takingShot) 
				takePicture();
		}
	};
	private void takePicture() {
		takingShot=true;
		System.out.println("takePicture()");
		try{
			camera.autoFocus(onFocus);
		}catch (RuntimeException e){
			e.printStackTrace();
			camera.takePicture(null, null, photoCallback);
		}
		
	}
	Camera.AutoFocusCallback onFocus = new Camera.AutoFocusCallback() {
		public void onAutoFocus(boolean success, Camera camera) {
			camera.takePicture(null, null, photoCallback);
			
		}
	};
	SurfaceHolder.Callback surfaceCallback=new SurfaceHolder.Callback() {
		 public void surfaceCreated(SurfaceHolder holder) {
			 camera=Camera.open();
			 
			 try {
				 camera.setPreviewDisplay(previewHolder);
			 }
			 catch (Throwable t) {
				 Log.e("Photographer",
							 "Exception in setPreviewDisplay()", t);
			 }
		 }
		 
		 public void surfaceChanged(SurfaceHolder holder,int format, int width,int height) {
			 
			 Parameters parameters=camera.getParameters();
			 List<Size> supportedSizes = parameters.getSupportedPictureSizes();
			 if(supportedSizes != null){
		 		 Size gb = supportedSizes.get(supportedSizes.size()-1);
				 height = gb.height;
				 width = gb.width;
		 	 }else{
		 		parameters.setPreviewSize(width,height);
		 	 }
			 parameters.setPictureFormat(PixelFormat.JPEG);
			 Log.i("NexusFix", "parameters: "+parameters.flatten());
			 camera.setParameters(parameters);
			 camera.startPreview();
		 }
		 
		 public void surfaceDestroyed(SurfaceHolder holder) {
			 camera.stopPreview();
             camera.setPreviewCallback(null); 
			 camera.release();
			 camera=null;
		 }
	};
	
	Camera.PictureCallback photoCallback=new Camera.PictureCallback() {
		 public void onPictureTaken(byte[] data, Camera camera) {
			 // do something with the photo JPEG (data[]) here!
			 System.out.println("starting Camera.PictureCallback");
			 String fn = null;
			 
			 BitmapFactory.Options opt =  new BitmapFactory.Options(); 
		     opt.inJustDecodeBounds = true; 
		     
			 BitmapFactory.decodeByteArray(data, 0, data.length, opt);
			 opt.inSampleSize = 1; 
			 int width = opt.outWidth; 
	         int height = opt.outHeight; 
	         System.out.println("orig:"+width+"x"+height);
//	         String q=Settings.getQuality(getBaseContext()).toString();
//	         int w;
//	         if(q.equals("Very High")){
//	        	 w = 1024;
//	         }else if(q.equals("High")){
//	        	 w = 800;
//	         }else if(q.equals("Low")){
//	        	 w = 256;
//	         }else{//default medium
//	        	 w = 512;
//	         }
	         
	         
	         
//	         System.out.println(q+"="+w+" orig:"+width+"x"+height);
	         int newWidth = 0;
	         int widthRatio = width/0;
	         int newHeight = height/widthRatio;
//	         int newWidth = w;
//	         int widthRatio = width/w;
//	         int newHeight = height/widthRatio;
	         
	         
	         
	         while ((opt.outWidth/opt.inSampleSize) > newWidth || 
                     (opt.outHeight/opt.inSampleSize) > newHeight ) { 
	        	 opt.inSampleSize <<= 1;
	        	 System.out.println("while opt.inSampleSize: "+opt.inSampleSize);
	        	 if(opt.inSampleSize == 0)
	        		 	break;
	         }
	         BitmapFactory.decodeByteArray(data, 0, data.length, opt);
	         float scaleWidth = ((float) newWidth) / opt.outWidth; 
	         float scaleHeight = ((float) newHeight) / opt.outHeight; 
	         System.out.println("scaleWidth: "+scaleWidth);
	         System.out.println("scaleHeight: "+scaleHeight);
	         Matrix matrix = new Matrix(); 
	         matrix.postScale(scaleWidth, scaleHeight); 
	         matrix.postRotate(90);
	         
	         System.out.println("optWidth: " +opt.outWidth);
	         System.out.println("outHeight: " +opt.outHeight);
	         
	         opt.inJustDecodeBounds = false; 
	         System.out.println("opt.inSampleSize: "+ opt.inSampleSize);
	         System.out.println("opt: "+ opt);
	         try{
	        	 Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length, opt);
		         resizedBitmap = Bitmap.createBitmap(bm, 0, 0,opt.outWidth, opt.outHeight, matrix, true);
		         ByteArrayOutputStream baos = new ByteArrayOutputStream();  
		         resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos); 
		         data = baos.toByteArray();  
	         }catch (Exception e) {
	            Log.e("Photographer", "Bitmap failed", e);
	         }
	         
	         try {
				 Date date = new Date();
				 SimpleDateFormat sdf = new 
				 SimpleDateFormat("yyyyMMddHHmmss");
				 String filename =  sdf.format(date);
				 File root = Environment.getExternalStorageDirectory();
				 fn = root + "/.xpenser/receipts/receipt_"+filename+".jpg";
				 File f = new File(root+"/.xpenser/receipts/");
				 f.mkdirs();
				    if (root.canWrite()){
				    	System.out.println(root);
				    	new File(fn);
				    	OutputStream out = new FileOutputStream(root + "/.xpenser/receipts/receipt_"+filename+".jpg");
				    	out.write(data);
				        out.close();
				        Log.i("Photographer", "MainDriver: successfully created file");
				    }else{
				    	fn = "cantWrite";
				    	Log.i("Photographer", "MainDriver: failed creating file");
				    }
				
	          }
	          catch (Exception e) {
	            Log.e("Photographer", "MainDriver: could not download and save PNG file", e);
	          }
	         
	          takingShot=false;
	          Intent intent = new Intent();
	          
	  		  if(fn != null){
	  			  intent.putExtra("returnImage",fn);
	  			  setResult(RESULT_OK,intent);
	  			  try {
					this.finalize();
				} catch (Throwable e) {
					e.printStackTrace();
				}
	  			  finish();
	  		  }else{
	  			camera.startPreview();
	  		  }
			 
		 }
	};

}
