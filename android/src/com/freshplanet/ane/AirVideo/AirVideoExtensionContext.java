//////////////////////////////////////////////////////////////////////////////////////
//
//  Copyright 2012 Freshplanet (http://freshplanet.com | opensource@freshplanet.com)
//  
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//  
//    http://www.apache.org/licenses/LICENSE-2.0
//  
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//  
//////////////////////////////////////////////////////////////////////////////////////

package com.freshplanet.ane.AirVideo;

import java.util.HashMap;
import java.util.Map;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import android.widget.MediaController;
import android.widget.VideoView;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.freshplanet.ane.AirVideo.extractor.YouTubeExtractor;
import com.freshplanet.ane.AirVideo.functions.HidePlayerFunction;
import com.freshplanet.ane.AirVideo.functions.LoadVideoFunction;
import com.freshplanet.ane.AirVideo.functions.LoadYoutubeFunction;
import com.freshplanet.ane.AirVideo.functions.ResizePlayerFunction;
import com.freshplanet.ane.AirVideo.functions.ShowPlayerFunction;

public class AirVideoExtensionContext extends FREContext implements OnCompletionListener, OnErrorListener
{
	private VideoView _videoView = null;
	
	@Override
	public void dispose() {}

	@Override
	public Map<String, FREFunction> getFunctions()
	{
		Map<String, FREFunction> functions = new HashMap<String, FREFunction>();
		
		functions.put("airVideoShowPlayer", new ShowPlayerFunction());
		functions.put("airVideoHidePlayer", new HidePlayerFunction());
		functions.put("airVideoLoadVideo", new LoadVideoFunction());
		functions.put("airVideoLoadYoutube", new LoadYoutubeFunction());
		functions.put("airVideoResizeVideo", new ResizePlayerFunction());
		
		return functions;
	}
	
	public ViewGroup getRootContainer()
	{
		return (ViewGroup)((ViewGroup)getActivity().findViewById(android.R.id.content)).getChildAt(0);
	}
	
	public VideoView getVideoView()
	{
		if (_videoView == null)
		{
			_videoView = new VideoView(getActivity());
			_videoView.setZOrderOnTop(true);
			
			_videoView.setOnPreparedListener(new OnPreparedListener()
			{
				@Override
				public void onPrepared(MediaPlayer mp)
				{
					dispatchStatusEventAsync("YOUTUBE_VIDEO_LOADED", "Video Load Successful");
				}
			});
			
			MediaController mediaController = new MediaController(getActivity());
			mediaController.setAnchorView(_videoView);
			
			_videoView.setMediaController(mediaController);
			_videoView.setOnCompletionListener(this);
			_videoView.setOnErrorListener(this);
		}
		
		return _videoView;
	}
	
	public void addToStage()
	{
		ViewGroup rootContainer = getRootContainer();
		VideoView videoContainer = getVideoView();
		if(rootContainer.indexOfChild(videoContainer) == -1)
		{
			LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.gravity = Gravity.CENTER_HORIZONTAL;
			rootContainer.addView(videoContainer, params);
		}
	}
	
	public void disposeVideoView()
	{
		ViewGroup rootContainer = AirVideoExtension.context.getRootContainer();
		rootContainer.removeView(_videoView);
		_videoView = null;
	}
	
	public void playUrl(String url)
	{
		VideoView video = getVideoView();
		
		video.setVideoURI(Uri.parse(url));
		video.start();
	}
	
	public void loadVideoFromYouTubeID( String videoId )
	{
		try 
		{
			YouTubeExtractor extractor = new YouTubeExtractor(this);
			extractor.extract( videoId );
		}
		catch (Exception e)
		{
			dispatchStatusEventAsync("YOUTUBE_EXTRACTION_ERROR", e.toString());
		}
	}
	
	public void setDisplayRect(double x, double y, double width, double height)
	{
		LayoutParams params = (LayoutParams)_videoView.getLayoutParams();
		params.leftMargin = (int)x;
		params.topMargin = (int)y;
		params.width = (int)width;
		params.height = (int)height;
		_videoView.setLayoutParams(params);
		_videoView.invalidate();
	}
	
	public void onCompletion(MediaPlayer mp)
	{
		dispatchStatusEventAsync("PLAYBACK_DID_FINISH", "OK");
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra)
	{
		dispatchStatusEventAsync("PLAYBACK_ERROR", "OK");
		return false;
	}
}
