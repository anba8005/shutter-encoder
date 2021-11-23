/*******************************************************************************************
* Copyright (C) 2021 PACIFICO PAUL
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License along
* with this program; if not, write to the Free Software Foundation, Inc.,
* 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
* 
********************************************************************************************/

package functions;

import java.awt.Cursor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.io.FileUtils;

import application.Ftp;
import application.OverlayWindow;
import application.RecordInputDevice;
import application.Settings;
import application.Shutter;
import application.VideoPlayer;
import application.Wetransfer;
import library.BMXTRANSWRAP;
import library.DVDAUTHOR;
import library.FFMPEG;
import library.FFPROBE;
import library.TSMUXER;
import settings.AdvancedFeatures;
import settings.AudioSettings;
import settings.BitratesAdjustement;
import settings.Colorimetry;
import settings.Corrections;
import settings.FunctionUtils;
import settings.Image;
import settings.ImageSequence;
import settings.InputAndOutput;
import settings.Overlay;
import settings.Timecode;
import settings.Transitions;

/*
 * AV1
 * H.264
 * H.265
 * MPEG-1
 * MPEG-2
 * MJPEG
 * VP8
 * VP9
 * OGV
 * WMV
 * Xvid
 * Blu-ray
 * DVD
 * DV PAL
 * AVC-Intra 100
 * Apple ProRes
 * DNxHD
 * DNxHR
 * FFV1
 * GoPro CineForm
 * HAP
 * QT Animation
 * Uncompressed
 * XAVC
 * XDCAM HD422
 */

public class VideoEncoders extends Shutter {

	public static void main(boolean encode) {
		
		Thread thread = new Thread(new Runnable() {	
			
			@Override
			public void run() {
				
				if (scanIsRunning == false)
					FunctionUtils.completed = 0;
				
				lblFilesEnded.setText(FunctionUtils.completedFiles(FunctionUtils.completed));

				for (int i = 0 ; i < liste.getSize() ; i++)
				{
					File file = FunctionUtils.setInputFile(new File(liste.getElementAt(i)));		
					
					if (file == null)
						break;
					
					try {
						
						String fileName = file.getName();
						String extension =  fileName.substring(fileName.lastIndexOf("."));
									
						//Audio
						String audio = "";		
						
						//caseOPATOM
						if (caseOPATOM.isSelected())
						{
							frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
							
			            	String audioFiles = "";
			            	
							//Finding video file name
				            if (FFPROBE.FindStreams(file.toString()))
				            	audioFiles = AudioSettings.setAudioFiles(audioFiles, file);
				            else
				            	continue;
				            
				            audio = audioFiles;
				            
				            frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						}
						
						lblCurrentEncoding.setText(fileName);
						
						//Data analyze
						if (FunctionUtils.analyze(file, false) == false)
							continue;	
												
						//InOut	
						InputAndOutput.getInputAndOutput();	
						
						//Output folder
						String labelOutput = FunctionUtils.setOutputDestination("", file);
															
						//File output name
						String extensionName = "_" + comboFonctions.getSelectedItem().toString().replace(" ","_");	
						
						if (comboFilter.getSelectedItem().toString().contains(".") == false
						&& comboFilter.getSelectedItem().toString().equals(language.getProperty("aucun")) == false
						&& comboFilter.getSelectedItem().toString().contains("/") == false)
						{
							extensionName += "_" + comboFilter.getSelectedItem().toString().replace(" ","_");
						}
						
						if (Settings.btnExtension.isSelected())
						{
							extensionName = Settings.txtExtension.getText();
						}
						else
						{
							if ((OverlayWindow.caseAddTimecode.isSelected() || OverlayWindow.caseShowTimecode.isSelected()) && caseAddOverlay.isSelected())
								extensionName += "_TC";
						}
						
						//Container
						String container = comboFilter.getSelectedItem().toString();						
						switch (comboFonctions.getSelectedItem().toString())
						{	
							case "DNxHD":
							case "DNxHR":
								
								if (caseCreateOPATOM.isSelected())
								{
									container = ".mxf";
								}
								else
									container = ".mov";	
								
								break;
								
							case "XAVC":
								
								container = ".mxf";								
								break;
							
							case "FFV1":
							case "Blu-ray":
								
								container = ".mkv";								
								break;
							
							case "DVD":
								
								container = ".mpg";								
								break;
							
							case "OGV":
								
								container = ".ogv";								
								break;
								
							case "WMV":
								
								container = ".wmv";								
								break;
								
							case "MPEG-1":
								
								container = ".mpg";								
								break;
								
							case "Xvid":
								
								container = ".avi";								
								break;
							
							case "Apple ProRes":
							case "GoPro CineForm":
							case "HAP":
							case "MJPEG":
							case "QT Animation":
							case "Uncompressed":
							case "DV PAL":
								
								container = ".mov";
								break;
								
						}			
						
						//Output name
						String fileOutputName =  labelOutput.replace("\\", "/") + "/" + fileName.replace(extension, extensionName + container); 
														
						//Authoring folder
						File authoringFolder = null;
						switch (comboFonctions.getSelectedItem().toString())
						{
							case "DVD":
							case "Blu-ray":									
							
								authoringFolder = new File(labelOutput + "/" + fileName.replace(extension, ""));
								authoringFolder.mkdir();
								
								fileOutputName =  authoringFolder.toString().replace("\\", "/") + "/" + fileName.replace(extension, container); 																
								break;
						}	
										
						//File output
						File fileOut = new File(fileOutputName);				
						if (fileOut.exists())		
						{						
							fileOut = FunctionUtils.fileReplacement(labelOutput, fileName, extension, extensionName + "_", container);
							
							if (fileOut == null)
								continue;						
						}
						
						//Hardware decoding
						String hardwareDecoding = " -hwaccel " + Settings.comboGPU.getSelectedItem().toString().replace(language.getProperty("aucun"), "none");
						if (caseAccel.isSelected() && comboAccel.getSelectedItem().equals("VAAPI"))			
						{
							hardwareDecoding += " -vaapi_device /dev/dri/renderD128";
						}
						
						//Concat mode
						String concat = FunctionUtils.setConcat(file, labelOutput);					
						if (Settings.btnSetBab.isSelected() || VideoPlayer.comboMode.getSelectedItem().toString().equals(language.getProperty("removeMode")) && caseInAndOut.isSelected())
							file = new File(labelOutput.replace("\\", "/") + "/" + fileName.replace(extension, ".txt"));
						
						//Image sequence
						String sequence = ImageSequence.setSequence(file, extension);
						
						file = ImageSequence.setSequenceName(file, extension);
						
						//Loop image					
						String loop = FunctionUtils.setLoop(extension);	
						
						//Stream
						String stream = FunctionUtils.setStream();
							
						//Subtitles
						String subtitles = "";
						if (grpH264.isVisible())
						{
							subtitles = Overlay.setSubtitles(comboH264Taille, false);
						}
						else if (grpResolution.isVisible())
						{
							switch (comboFonctions.getSelectedItem().toString())
							{
								//Limit to Full HD
								case "AVC-Intra 100":
								case "DNxHD":
								case "XDCAM HD422":
									
									subtitles = Overlay.setSubtitles(comboResolution, true);
									
									break;
									
								default:
									
									subtitles = Overlay.setSubtitles(comboResolution, false);
									
									break;
							}
						}
						else if (comboFonctions.getSelectedItem().toString().equals("DVD"))
						{	
							subtitles = Overlay.setSubtitles(null, false);	
						}
						
						//Interlace
			            String interlace = AdvancedFeatures.setInterlace();	
						
						//Codec
						String codec = setCodec();
						
						//Bitrate
						String bitrate = setBitrate();						
						 
						//PixelFormat
						String pixelFormat = setPixelFormat();					

						//Level
						String profile = AdvancedFeatures.setProfile();
						
						//Tune
						String tune = AdvancedFeatures.setTune();
						
						//GOP
						String gop = AdvancedFeatures.setGOP();
												
						//Interlace
			            String options = AdvancedFeatures.setOptions();
				        
						//Resolution
						String resolution = "";						
						if (grpH264.isVisible())
						{
							resolution = BitratesAdjustement.setResolution();	
						}
						else
						{
				            switch (comboFonctions.getSelectedItem().toString())
							{
				            	//Limit to Full HD
								case "AVC-Intra 100":
								case "DNxHD":
								case "XDCAM HD422":
									
									resolution = Image.limitToFHD();									
									break;
								
								case "DV PAL":
									
									resolution = " -s 720x576";									
									break;
							}
						}
						
			            //Colorspace
			            String colorspace = Colorimetry.setColorspace();
			        
				        //Deinterlace
						String filterComplex = "";						
						switch (comboFonctions.getSelectedItem().toString())
						{
							case "AV1":
							case "H.264":
							case "H.265":
							case "MJPEG":
							case "VP8":
							case "VP9":
							case "OGV":
							case "WMV":
							case "Xvid":
							case "DNxHR":
								
								filterComplex = AdvancedFeatures.setDeinterlace(true);									
								break;
							
							case "MPEG-1":
								
								filterComplex = AdvancedFeatures.setDeinterlace(true);
								break;
								
							case "MPEG-2":
								
								filterComplex = AdvancedFeatures.setDeinterlace(false);								
								break;
							
							case "DNxHD":
								
								switch (comboFilter.getSelectedItem().toString())
					            {
					            	case "36":
					            	case "75":
					            	case "240":
					            	case "365":
					            	case "365 X":
					            	case "90":
					            	case "115":
					            	case "175":
					            	case "175 X":
					            		
					            		filterComplex = AdvancedFeatures.setDeinterlace(true);					            		
				            			break;
				            		
				            		default:
				            			
				            			filterComplex = AdvancedFeatures.setDeinterlace(false);				            			
			            				break;
					            }
								
								break;
								
							case "Apple ProRes":
							case "AVC-Intra 100":
							case "FFV1": 
							case "GoPro CineForm":
							case "HAP":
							case "QT Animation":
							case "Uncompressed":
							case "XAVC":
							case "XDCAM HD422":
								
								filterComplex = AdvancedFeatures.setDeinterlace(false);								
								break;
							
							case "Blu-ray":
							case "DVD":
								
								if (FFPROBE.entrelaced.equals("1") && caseForcerProgressif.isSelected())
								{
									filterComplex = "yadif=0:" + FFPROBE.fieldOrder + ":0"; 
								}
								
								break;
						}
											
						//Blend
						filterComplex = ImageSequence.setBlend(filterComplex);
						
						//MotionBlur
						filterComplex = ImageSequence.setMotionBlur(filterComplex);
						
						//Stabilisation
						filterComplex = Corrections.setStabilisation(filterComplex, sequence, file, fileName, concat);
						
						//LUTs
						filterComplex = Colorimetry.setLUT(filterComplex);
							
						//Levels
						filterComplex = Colorimetry.setLevels(filterComplex);
										
						//Colormatrix
						filterComplex = Colorimetry.setColormatrix(filterComplex);	
						
						//Color
						filterComplex = Colorimetry.setColor(filterComplex);		
						
						//Deflicker
						filterComplex = Corrections.setDeflicker(filterComplex);
						
						//Deband
						filterComplex = Corrections.setDeband(filterComplex);
							 
						//Details
		            	filterComplex = Corrections.setDetails(filterComplex);				
														            	
						//Denoise
			    		filterComplex = Corrections.setDenoiser(filterComplex);
			    		
			    		//Exposure
						filterComplex = Corrections.setExposure(filterComplex);	
						
						//Decimate
						filterComplex = AdvancedFeatures.setDecimate(filterComplex);
						
						//Interpolation
						filterComplex = AdvancedFeatures.setInterpolation(filterComplex);
						
						//Slow motion
						filterComplex = AdvancedFeatures.setSlowMotion(filterComplex);
											
				        //PTS
						filterComplex = AdvancedFeatures.setPTS(filterComplex);		      		                     	

						//Conform
			    		filterComplex = AdvancedFeatures.setConform(filterComplex);
								
		            	//Logo
				        String logo = Overlay.setLogo();	            			            
										
				    	//Watermark
						filterComplex = Overlay.setWatermark(filterComplex);
						
		            	//Timecode
						filterComplex = Overlay.showTimecode(filterComplex, fileName.replace(extension, ""));
				        
				    	//Crop
				        filterComplex = Image.setCrop(filterComplex);
						
						//Rotate
						filterComplex = Image.setRotate(filterComplex);
						
						//DAR
						filterComplex = Image.setDAR(filterComplex);
						
						//Padding
						if (grpH264.isVisible())
						{
							filterComplex = Image.setPad(filterComplex, true, comboH264Taille, false);
						}
						else if (grpResolution.isVisible())
						{
							switch (comboFonctions.getSelectedItem().toString())
							{
								//Limit to Full HD
								case "AVC-Intra 100":
								case "DNxHD":
								case "XDCAM HD422":
									
									if (FFPROBE.imageResolution.equals("1440x1080"))
									{
										filterComplex = Image.setPad(filterComplex, false, comboResolution, false);									
									}
									else
										filterComplex = Image.setPad(filterComplex, false, comboResolution, true);			
									
									break;
									
								default:
									
									filterComplex = Image.setPad(filterComplex, false, comboResolution, false);									
									break;
							}
						}										
						
						//Interlace50p
			            filterComplex = AdvancedFeatures.setInterlace50p(filterComplex);
			            
						//Force TFF
						filterComplex = AdvancedFeatures.setForceTFF(filterComplex);	
								
						//Overlay
						if (grpH264.isVisible())
						{
							filterComplex = Overlay.setOverlay(filterComplex, comboH264Taille, false);
						}
						else if (grpResolution.isVisible())
						{							
							switch (comboFonctions.getSelectedItem().toString())
							{
								//Limit to Full HD
								case "AVC-Intra 100":
								case "DNxHD":
								case "XDCAM HD422":
									
									filterComplex = Overlay.setOverlay(filterComplex, comboResolution, true);									
									break;
									
								default:
									
									filterComplex = Overlay.setOverlay(filterComplex, comboResolution, false);									
									break;
							}							
						}
						else if (comboFonctions.getSelectedItem().toString().equals("DVD"))
						{
							filterComplex = Overlay.setOverlay(filterComplex, null, false);	 
						}						
						
						//Limiter
						filterComplex = Corrections.setLimiter(filterComplex);
			            
						//Fade-in Fade-out
						filterComplex = Transitions.setVideoFade(filterComplex);
						
		            	//Audio
			            if (grpH264.isVisible() || comboFonctions.getSelectedItem().toString().equals("DVD"))
						{
			            	audio = AudioSettings.setAudioOutputCodecs(filterComplex, comboAudioCodec.getSelectedItem().toString());	
						}
			            else if (grpResolution.isVisible())
						{
			            	switch (comboFonctions.getSelectedItem().toString())
							{
								case "AVC-Intra 100":	
								case "XAVC":
								case "XDCAM HD422":
									
									audio = AudioSettings.setAudioBroadcastCodecs(audio);								
									break;
								
								default:
									
									audio = AudioSettings.setAudioEditingCodecs(audio);									
									break;
							}
						}
			            else if (comboFonctions.getSelectedItem().toString().equals("DV PAL"))
			            {
			            	audio = " -c:a pcm_s16le -ar 48000 -map v:0 -map a?";
			            }
			            
		            	//filterComplex
						if (grpH264.isVisible() || comboFonctions.getSelectedItem().toString().equals("DVD"))
						{
							filterComplex = FunctionUtils.setFilterComplex(filterComplex, true, audio);		
						}
						else if (grpResolution.isVisible())
						{
							switch (comboFonctions.getSelectedItem().toString())
							{
								case "AVC-Intra 100":
								case "XAVC":
								case "XDCAM HD422":
									
									filterComplex = FunctionUtils.setFilterComplexBroadcastCodecs(filterComplex, audio);								
									break;
								
								default:
									
									filterComplex = FunctionUtils.setFilterComplex(filterComplex, false, audio);									
									break;
							}
						}	
						else if (comboFonctions.getSelectedItem().toString().equals("DV PAL"))
						{
							if (filterComplex != "") 
							{
								filterComplex = " -filter_complex " + '"' + filterComplex + '"' + audio;
							}
							else
								filterComplex = audio;
						}
												
						//Timecode
						String timecode = Timecode.setTimecode();
			            
			            //Flags
			    		String flags = AdvancedFeatures.setFlags(fileName);
			    		
						//Metadatas
			    		String metadatas = FunctionUtils.setMetadatas();
			    		
			    		//OPATOM
			    		String opatom = AdvancedFeatures.setOPATOM(audio);
						
				       	//Preset
				        String preset = AdvancedFeatures.setPreset();
				        
				        //Framerate
						String frameRate = "";						
						switch (comboFonctions.getSelectedItem().toString())
						{
							case "DNxHD":
							case "DNxHR":
								
								frameRate = AdvancedFeatures.setFramerate(true);								
								break;
							
							case "DV PAL":
								
								frameRate = " -r 25";								
								break;
							
							default:
								
								frameRate = AdvancedFeatures.setFramerate(false);								
								break;							
						}

				        //2pass
				        String pass = BitratesAdjustement.setPass(fileOutputName);
				
						String output = '"' + fileOut.toString() + '"';
						String previewContainer = "matroska";
						
						if (caseStream.isSelected())
						{
							output = "-flags:v +global_header -f tee " + '"' + fileOut.toString().replace("\\", "/") + "|[f=flv]" + textStream.getText();
									
							if (caseDisplay.isSelected())
								output += "|[f=matroska]pipe:play" + '"';
							else
								output += '"';
						}
						else if (caseDisplay.isSelected())
						{
							switch (comboFonctions.getSelectedItem().toString())
							{
								case "AV1":
								case "H.264":
								case "H.265":
								case "MPEG-1":
								case "MPEG-2":
								case "MJPEG":
								case "OGV":
								case "VP8":
								case "VP9":
								case "WMV":
								case "Xvid":
								case "Blu-ray":
								case "DV PAL":
									
									output = "-flags:v +global_header -f tee " + '"' + fileOut.toString().replace("\\", "/") + "|[f=matroska]pipe:play" + '"';
									break;
								
								case "AVC-Intra 100":
								case "XAVC":
																	
									output = "-f tee " + '"' + fileOut.toString().replace("\\", "/") + "|[f=mxf]pipe:play" + '"';
									previewContainer = "mxf";
									break;
									
								case "DNxHD":
								case "DNxHR":								
								case "XDCAM HD422":
								case "Apple ProRes":
								case "FFV1":
								case "GoPro CineForm":
								case "HAP":
								case "Uncompressed":
									
									output = "-f tee " + '"' + fileOut.toString().replace("\\", "/") + "|[f=matroska]pipe:play" + '"';
									break;
							}	
						}
						
						//Command
						String cmd = FunctionUtils.silentTrack + opatom + frameRate + resolution + pass + codec + bitrate + preset + profile + tune + gop + filterComplex + interlace + pixelFormat + colorspace + options + timecode + flags + metadatas + " -y ";
										
						//Screen capture
						if (inputDeviceIsRunning)
						{	
							String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(Calendar.getInstance().getTime());	
		
							if ((liste.getElementAt(0).equals("Capture.current.screen") || System.getProperty("os.name").contains("Mac")) && RecordInputDevice.audioDeviceIndex > 0)
								cmd = cmd.replace("1:v", "2:v").replace("-map v:0", "-map 1:v").replace("0:v", "1:v");	
							
							if (encode)
								FFMPEG.run(" " + RecordInputDevice.setInputDevices() + logo + cmd + output.replace("Capture.current", timeStamp).replace("Capture.input", timeStamp));	
							else
							{
								FFMPEG.toFFPLAY(" " + RecordInputDevice.setInputDevices() + logo + cmd + " -f " + previewContainer + " pipe:play |");							
								break;
							}						
							
							fileOut = new File(fileOut.toString().replace("Capture.current", timeStamp).replace("Capture.input", timeStamp));
						}
						else if (encode) //Encoding
						{
							FFMPEG.run(hardwareDecoding + loop + stream + InputAndOutput.inPoint + sequence + concat + " -i " + '"' + file.toString() + '"' + logo + subtitles + InputAndOutput.outPoint + cmd + output);		
						}
						else //Preview
						{						
							FFMPEG.toFFPLAY(hardwareDecoding + loop + stream + InputAndOutput.inPoint + sequence + concat + " -i " + '"' + file.toString() + '"' + logo + subtitles + InputAndOutput.outPoint + cmd + " -f " + previewContainer + " pipe:play |");
							break;
						}

						do
						{
							Thread.sleep(100);
						}
						while(FFMPEG.runProcess.isAlive());
						
						if (grpH264.isVisible() && case2pass.isSelected() || comboFonctions.getSelectedItem().toString().equals("DVD") && pass !=  "")
						{						
							if (FFMPEG.cancelled == false)
								FFMPEG.run(hardwareDecoding + loop + stream + InputAndOutput.inPoint + sequence + concat + " -i " + '"' + file.toString() + '"' + logo + subtitles + InputAndOutput.outPoint + cmd.replace("-pass 1", "-pass 2") + output);	
							
							do
							{
								Thread.sleep(100);
							}
							while(FFMPEG.runProcess.isAlive());							
						}
																			
						if (FFMPEG.saveCode == false && cancelled == false && FFMPEG.error == false)
						{
							//HDR
							switch (comboFonctions.getSelectedItem().toString())
							{
								case "AV1":
								case "H.265":
								case "VP8":
								case "VP9":
									
									//HDR
									Colorimetry.setHDR(fileName, fileOut);
									
									break;
									
								//Creating VOB files
								case "DVD":
									
									lblCurrentEncoding.setText(Shutter.language.getProperty("createBurnFiles"));
									makeVOBfiles(authoringFolder, fileOutputName);
									
									break;
									
								case "Blu-ray":									
								
									lblCurrentEncoding.setText(Shutter.language.getProperty("createBurnFiles"));
									makeBDMVfiles(authoringFolder, fileOutputName);		
									
									break;
							}								

							//OPATOM creation
							if (caseCreateOPATOM.isSelected() && lblOPATOM.getText().equals("OP-Atom"))
							{							
								String key = FunctionUtils.getRandomHexString().toUpperCase();
								
								if (Settings.btnExtension.isSelected())
									key = Settings.txtExtension.getText();
								
								switch (comboFonctions.getSelectedItem().toString())
								{
									case "DNxHD":
										
										lblCurrentEncoding.setText(Shutter.language.getProperty("createOpatomFiles"));
																												
										BMXTRANSWRAP.run("-t avid -p -o " + '"' + labelOutput + "/" + fileName.replace(extension, key) + '"' + " --clip " + '"' + fileName.replace(extension, "") + '"' + " --tape " + '"' + fileName + '"' + " " + '"' + fileOut.toString() + '"');
									
										do
										{
											Thread.sleep(100);
										}
										while (BMXTRANSWRAP.isRunning);
										
										fileOut.delete();
										break;
										
									case "DNxHR":
																		
										fileOut.renameTo(new File(labelOutput + "/" + fileName.replace(extension, key + "_v1.mxf")));			
										break;
								}							
							}
							else if (caseAS10.isSelected())
							{
								switch (comboFonctions.getSelectedItem().toString())
								{
									case "XDCAM HD422":										
										
										lblCurrentEncoding.setText(Shutter.language.getProperty("createAS10Format"));
										
										BMXTRANSWRAP.run("-t as10 -p -o " + '"' + labelOutput + "/" + fileName.replace(extension, "_AS10" + comboFilter.getSelectedItem().toString()) + '"' + " --shim-name " + '"' + comboAS10.getSelectedItem().toString() + '"' + " " + '"' + fileOut.toString() + '"');
								
										do
										{
											Thread.sleep(100);
										}
										while(BMXTRANSWRAP.isRunning);
										
										fileOut.delete();
										break;
										
									case "AVC-Intra 100":
										
										lblCurrentEncoding.setText(Shutter.language.getProperty("createAS10Format").replace("10", "11"));
																				
										BMXTRANSWRAP.run("-t as11op1a -p -o " + '"' + labelOutput + "/" + fileName.replace(extension, "_AS11" + comboFilter.getSelectedItem().toString()) + '"' + " " + '"' + fileOut.toString() + '"');
									
										do
										{
											Thread.sleep(100);
										}
										while(BMXTRANSWRAP.isRunning);
										
										fileOut.delete();
										break;
								}
							}
						}
					
						//Removing temporary files
						if (pass != "")
						{						
							final File folder = new File(fileOut.getParent());
							FunctionUtils.listFilesForFolder(fileName.replace(extension, ""), folder);
						}
						
						if (comboFonctions.getSelectedItem().toString().equals("DVD") || comboFonctions.getSelectedItem().toString().equals("Blu-ray"))
						{
							final File folder = new File(fileOut.getParent());
							FunctionUtils.listFilesForFolder(null, folder);
							
							if (FFMPEG.saveCode || cancelled || FFMPEG.error)
								FileUtils.deleteDirectory(authoringFolder);	
						}
		
						if (FFMPEG.saveCode == false && btnStart.getText().equals(Shutter.language.getProperty("btnAddToRender")) == false 
						|| FFMPEG.saveCode == false && caseEnableSequence.isSelected()
						|| FFMPEG.saveCode == false && Settings.btnSetBab.isSelected()
						|| FFMPEG.saveCode == false && VideoPlayer.comboMode.getSelectedItem().toString().equals(language.getProperty("removeMode")) && caseInAndOut.isSelected())
						{
							if (lastActions(fileName, fileOut, labelOutput))
								break;
						}
					} catch (InterruptedException | IOException e) {
						FFMPEG.error  = true;
					}			
				}
				
				FunctionUtils.OPAtomFolder = null;
				FunctionUtils.silentTrack = "";

				if (btnStart.getText().equals(Shutter.language.getProperty("btnAddToRender")) == false && encode)
				{
					enfOfFunction();
				}
			}
			
		});
		thread.start();
		
    }

	private static String setCodec() {
		
		switch (comboFonctions.getSelectedItem().toString())
		{
			case "AV1":
		
				return " -c:v libsvtav1";
				
			case "Blu-ray":
				
				String interlace = "";
				if (FFPROBE.currentFPS != 24.0 && FFPROBE.currentFPS != 23.98 && caseForcerProgressif.isSelected() == false)
		        	interlace = ":tff=1";
				
				return " -c:v libx264 -pix_fmt yuv420p -tune film -level 4.1 -x264opts bluray-compat=1:force-cfr=1:weightp=0:bframes=3:ref=3:nal-hrd=vbr:vbv-maxrate=40000:vbv-bufsize=30000:bitrate=" + debitVideo.getSelectedItem().toString() + ":keyint=60:b-pyramid=strict:slices=4" + interlace + ":aud=1:colorprim=bt709:transfer=bt709:colormatrix=bt709 -r " + FFPROBE.currentFPS;
				
			case "DVD":
				
				return " -aspect 16:9 -target pal-dvd";
				
			case "DV PAL":
				
				return " -c:v dvvideo -b:v 25000k -aspect " + comboFilter.getSelectedItem().toString().replace("/", ":");
				
			case "H.264":
				
				if (caseAccel.isSelected())
				{
					if (comboAccel.getSelectedItem().equals("Nvidia NVENC"))
						return " -c:v h264_nvenc";	
					else if (comboAccel.getSelectedItem().equals("Intel Quick Sync"))
						return " -c:v h264_qsv";	
					else if (comboAccel.getSelectedItem().equals("AMD AMF Encoder"))
						return " -c:v h264_amf";
					else if (comboAccel.getSelectedItem().equals("OSX VideoToolbox"))
						return " -c:v h264_videotoolbox";
					else if (comboAccel.getSelectedItem().equals("VAAPI"))
						return " -c:v h264_vaapi";	
					else if (comboAccel.getSelectedItem().equals("V4L2 M2M"))
						return " -c:v h264_v4l2m2m";	
					else if (comboAccel.getSelectedItem().equals("OpenMAX"))
						return " -c:v h264_omx";	
				}
				else
					return " -c:v libx264";
				
				break;
			
			case "H.265":
				
				if (caseAccel.isSelected())
				{
					if (comboAccel.getSelectedItem().equals("Nvidia NVENC"))
						return " -c:v hevc_nvenc";	
					else if (comboAccel.getSelectedItem().equals("Intel Quick Sync"))
						return " -c:v hevc_qsv";	
					else if (comboAccel.getSelectedItem().equals("AMD AMF Encoder"))
						return " -c:v hevc_amf";
					else if (comboAccel.getSelectedItem().equals("OSX VideoToolbox"))
						return " -c:v hevc_videotoolbox";			
					else if (comboAccel.getSelectedItem().equals("VAAPI"))
						return " -c:v hevc_vaapi";	
				}
				else
					return " -c:v libx265";
				
				break;
			
			case "MPEG-1":
				
				return " -c:v mpeg1video";
				
			case "MPEG-2":
				
				return " -c:v mpeg2video";
			
			case "MJPEG":
				
				return " -c:v mjpeg";
				
			case "OGV":
				
				return " -c:v libtheora";
						
			case "VP8":
			
				return " -c:v libvpx";
			
			case "VP9":
				
				if (caseAccel.isSelected() && comboAccel.getSelectedItem().equals("Intel Quick Sync"))
				{
					return " -c:v vp9_qsv";
				}
		    	else if (caseAccel.isSelected() && comboAccel.getSelectedItem().equals("VAAPI"))
		    	{
		    		return " -c:v vp9_vaapi";
		    	}
		    	else
		        	return " -c:v libvpx-vp9";  
				
			case "WMV":
				
				return " -c:v wmv2";
				
			case "Xvid":
				
				return " -c:v libxvid";
				
			case "Apple ProRes":
				
				switch (comboFilter.getSelectedItem().toString())
				{					
					case "Proxy" :
						return " -c:v prores -profile:v proxy -pix_fmt yuv422p10";
					case "LT" :
						return " -c:v prores -profile:v lt -pix_fmt yuv422p10";
					case "422" :
						return " -c:v prores -profile:v std -pix_fmt yuv422p10";
					case "422 HQ" :
						return " -c:v prores -profile:v hq -pix_fmt yuv422p10";
				}
				
				break;

			case "AVC-Intra 100":
				
				return " -shortest -c:v libx264 -coder 0 -g 1 -b:v 100M -tune psnr -preset veryslow -vsync 1 -color_range 2 -avcintra-class 100 -me_method hex -subq 5 -cmp chroma -pix_fmt yuv422p10le";
			
			case "DNxHD":
			case "DNxHR":
				
				return " -c:v dnxhd ";
				
			case "FFV1":
				
				return " -c:v ffv1 -level 3";
				
			case "GoPro CineForm":
				
				String yuv = "yuv422p10";
				if (caseAlpha.isSelected())
					yuv = "gbrap12le";
				
				switch (comboFilter.getSelectedItem().toString())
				{					
					case "Low" :
						return " -c:v cfhd -quality low -pix_fmt " + yuv;
					case "Medium" :
						return " -c:v cfhd -quality medium -pix_fmt " + yuv;
					case "High" :
						return " -c:v cfhd -quality high -pix_fmt " + yuv;
					case "Film Scan" :
						return " -c:v cfhd -quality film1 -pix_fmt " + yuv;
					case "Film Scan 2" :
						return " -c:v cfhd -quality film2 -pix_fmt " + yuv;
					case "Film Scan 3" :
						return " -c:v cfhd -quality film3 -pix_fmt " + yuv;
					case "Film Scan 3+" :
						return " -c:v cfhd -quality film3+ -pix_fmt " + yuv;
				}

				break;
			
			case "HAP":
				
				if (caseChunks.isSelected())
				{
					return " -c:v hap -chunks " + (chunksSize.getSelectedIndex() + 1);
				}
				else
					return " -c:v hap -chunks 4";
				
			case "QT Animation":
				
				return " -c:v qtrle";
				
			case "Uncompressed":
				
				switch (comboFilter.getSelectedItem().toString())
				{		
					case "YUV" :
				
						if (caseColorspace.isSelected() && comboColorspace.getSelectedItem().toString().contains("10bits"))
						{
							return " -c:v v210 -pix_fmt yuv422p10le";
						}
						else						
							return " -c:v rawvideo -pix_fmt uyvy422 -vtag 2vuy";
						
					case "RGB" :						
						
						return " -c:v r210 -pix_fmt gbrp10le";
					
				}
				
				break;
				
			case "XDCAM HD422":
				
				return " -c:v mpeg2video -g 12 -pix_fmt yuv422p -color_range 1 -non_linear_quant 1 -dc 10 -intra_vlc 1 -q:v 2 -qmin 2 -qmax 12 -lmin " + '"' + "1*QP2LAMBDA" + '"' + " -rc_max_vbv_use 1 -rc_min_vbv_use 1 -b:v 50000000 -minrate 50000000 -maxrate 50000000 -bufsize 17825792 -rc_init_occupancy 17825792 -sc_threshold 1000000000 -bf 2";
		
			case "XAVC":
				
				return " -c:v libx264 -me_method tesa -subq 9 -partitions all -direct-pred auto -psy 0 -b:v " + comboFilter.getSelectedItem().toString() + "M -bufsize " + comboFilter.getSelectedItem().toString() + "M -level 5.1 -g 0 -keyint_min 0 -x264opts filler -x264opts colorprim=bt709 -x264opts transfer=bt709 -x264opts colormatrix=bt709 -x264opts force-cfr -preset superfast -tune fastdecode -pix_fmt yuv422p10le";
		}
		
		return "";		
	}

	private static String setPixelFormat() {
		
		switch (comboFonctions.getSelectedItem().toString())
		{
			case "AV1":
			case "H.264":
			case "H.265":
			case "VP9":
				
				String yuv = "yuv";
				
				if (caseAlpha.isSelected())
		        {
		        	yuv += "a";
		        }
				
		        if (caseForceOutput.isSelected() && lblNiveaux.getText().equals("0-255"))
		        {
		        	yuv += "j";
		        }		

		        if (caseForceLevel.isSelected())
		        {
		        	if (comboForceProfile.getSelectedItem().toString().contains("422"))
		        		yuv += "422p";
		        	else if (comboForceProfile.getSelectedItem().toString().contains("444"))
		        		yuv += "444p";
		        	else
		            	yuv += "420p";
		        }
		        else
		        	yuv += "420p";
		        
				if (caseColorspace.isSelected())
				{
					if (comboColorspace.getSelectedItem().toString().contains("10bits"))
					{
						yuv += "10le";
					}
					else if (comboColorspace.getSelectedItem().toString().contains("12bits"))
					{
						yuv += "12le";
					}
				}
		        
		        return " -pix_fmt " + yuv;
			
			case "VP8":

				if (caseAlpha.isSelected())
		        {
					return " -auto-alt-ref 0 -pix_fmt yuva420p";
		        }
				else				
					return " -pix_fmt yuv420p";
		        
			case "MPEG-2":
				
				if (caseColorspace.isSelected() && comboColorspace.getSelectedItem().toString().contains("4:2:2"))
				{
					return " -pix_fmt yuv422p";
				}
				
			case "MPEG-1":
			case "OGV":
			case "WMV":
			case "Xvid":
				
				return " -pix_fmt yuv420p";
				
			case "MJPEG":
				
				return " -pix_fmt yuvj422p";
				
			//The other cases are managed from setCodec
		}
		
		return "";
	}
	
	private static String setBitrate() {
		
		switch (comboFonctions.getSelectedItem().toString())
		{
			case "AV1":
			
				if (lblVBR.getText().equals("CQ"))
		        {
		        	return " -rc cqp -qp " + debitVideo.getSelectedItem().toString();  
		        }
		        else
		        	return " -rc vbr -b:v " + debitVideo.getSelectedItem().toString() + "k";

			case "DVD":
				
				float bitrate = (float) ((float) 4000000 / FFPROBE.totalLength) * 8;
				if (caseInAndOut.isSelected())
				{
					float totalIn =  Integer.parseInt(VideoPlayer.caseInH.getText()) * 3600000 + Integer.parseInt(VideoPlayer.caseInM.getText()) * 60000 + Integer.parseInt(VideoPlayer.caseInS.getText()) * 1000 + Integer.parseInt(VideoPlayer.caseInF.getText()) * VideoPlayer.inputFramerateMS;
					float totalOut = Integer.parseInt(VideoPlayer.caseOutH.getText()) * 3600000 + Integer.parseInt(VideoPlayer.caseOutM.getText()) * 60000 + Integer.parseInt(VideoPlayer.caseOutS.getText()) * 1000 + Integer.parseInt(VideoPlayer.caseOutF.getText()) * VideoPlayer.inputFramerateMS;
					
					float sommeTotal = totalOut - totalIn;
					
					bitrate = (float) ((float) 4000000 / sommeTotal) * 8;
				}
				
				NumberFormat formatter = new DecimalFormat("0000");
				
				if (bitrate > 8)
				{
					BitratesAdjustement.DVDBitrate = 8000;
					return " -b:v 8000k";
				}
				else
				{
					BitratesAdjustement.DVDBitrate = Integer.parseInt(formatter.format(bitrate * 1000));
					return " -b:v " + Integer.parseInt(formatter.format(bitrate * 1000)) + "k";
				}
				
			case "H.264":
			case "H.265":
				
				if (lblVBR.getText().equals("CQ"))
		        {
		    		String gpu = "";
					if (caseAccel.isSelected() && comboAccel.getSelectedItem().equals("Nvidia NVENC"))
					{
						
						if (comboFonctions.getSelectedItem().toString().equals("H.264"))
						{	
							gpu = " -qp " + debitVideo.getSelectedItem().toString();
						}
						else
							gpu = " -cq " + debitVideo.getSelectedItem().toString();
					}
					else if (caseAccel.isSelected() && comboAccel.getSelectedItem().equals("Intel Quick Sync"))
					{
						gpu = " -global_quality " + debitVideo.getSelectedItem().toString();
					}
					else if (caseAccel.isSelected() && comboAccel.getSelectedItem().equals("AMD AMF Encoder"))
					{
						gpu = " -qp_i " + debitVideo.getSelectedItem().toString() + " -qp_p " + debitVideo.getSelectedItem().toString() + " -qp_b " + debitVideo.getSelectedItem().toString();        			
					}
					else if (caseAccel.isSelected() && comboAccel.getSelectedItem().equals("OSX VideoToolbox"))
					{
						gpu = " -q:v " + (31 - (int) Math.ceil((Integer.parseInt(debitVideo.getSelectedItem().toString()) * 31) / 51));
					}
						
		    		return " -crf " + debitVideo.getSelectedItem().toString() + gpu;          
		        }
		        else
		        	return " -b:v " + debitVideo.getSelectedItem().toString() + "k";
				
			case "VP8":
			case "VP9":
				
				if (lblVBR.getText().equals("CQ"))
		        {
		        	return " -crf " + debitVideo.getSelectedItem().toString();   
		        }
		        else
		        	return " -b:v " + debitVideo.getSelectedItem().toString() + "k";
				
			case "MPEG-1":
			case "MPEG-2":
			case "MJPEG":
			case "OGV":
			case "WMV":
			case "Xvid":
				
			       return " -b:v " + debitVideo.getSelectedItem().toString() + "k";
				
			case "DNxHD":
				
				if (comboFilter.getSelectedItem().toString().contains("X"))
				{
					return " -b:v " + comboFilter.getSelectedItem().toString().replace(" X", "") + "M -pix_fmt yuv422p10";
				}
				else
					return " -b:v " + comboFilter.getSelectedItem().toString() + "M -pix_fmt yuv422p";
				
			case "DNxHR":	
				
				if (comboFilter.getSelectedItem().toString().equals("HQX"))
				{
					return " -profile:v dnxhr_" + comboFilter.getSelectedItem().toString().toLowerCase() + " -pix_fmt yuv422p10";
				}
				else if (comboFilter.getSelectedItem().toString().equals("444"))
				{
					return " -profile:v dnxhr_" + comboFilter.getSelectedItem().toString().toLowerCase() + " -pix_fmt yuv444p10";
				}
				else
					return " -profile:v dnxhr_" + comboFilter.getSelectedItem().toString().toLowerCase() + " -pix_fmt yuv422p";

			case "HAP":
				
				if (comboFilter.getSelectedItem().equals("Alpha"))
				{
					return " -format hap_alpha";
				}
				else if (comboFilter.getSelectedItem().equals("Q"))
				{
					return " -format hap_q";
				}

				break;
		}
		
		return "";
	}	
				
	private static void makeVOBfiles(File dvdFolder, String MPEGFile) throws IOException, InterruptedException {
		
		String PathToDvdXml = Shutter.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		if (System.getProperty("os.name").contains("Windows"))
			PathToDvdXml = PathToDvdXml.substring(1,PathToDvdXml.length()-1);
		else
			PathToDvdXml = PathToDvdXml.substring(0,PathToDvdXml.length()-1);
		
		PathToDvdXml = PathToDvdXml.substring(0,(int) (PathToDvdXml.lastIndexOf("/"))).replace("%20", " ") + "/Library/dvd.xml"; //Old XML
		String copyXML = dvdFolder.toString() + "/dvd.xml"; //New XML

		FileReader reader = new FileReader(PathToDvdXml);			
		BufferedReader oldXML = new BufferedReader(reader);
		
		FileWriter writer = new FileWriter(copyXML);
		
		String line;
		while ((line = oldXML.readLine()) != null)
		{
			if (line.contains("path"))
				writer.write(line.replace("path", MPEGFile) + System.lineSeparator());
			else					
				writer.write(line + System.lineSeparator());
		}
		
			reader.close();
			oldXML.close();
			writer.close();		
			
			if (System.getProperty("os.name").contains("Mac") || System.getProperty("os.name").contains("Linux"))
				DVDAUTHOR.run("-o " + '"' + dvdFolder.toString() + "/" + '"' + " -x " + '"' + dvdFolder.toString() + "/dvd.xml" + '"');
			else
				DVDAUTHOR.run("-o " + '"' + dvdFolder.toString() + '"' + " -x " + '"' + dvdFolder.toString() + "/dvd.xml" + '"');	
				
            do {
            	Thread.sleep(100);
            } while (DVDAUTHOR.isRunning);
	}
		
	private static void makeBDMVfiles(File blurayFolder, String MKVfile) throws IOException, InterruptedException {
		
		String PathToblurayMeta = Shutter.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		if (System.getProperty("os.name").contains("Windows"))
			PathToblurayMeta = PathToblurayMeta.substring(1,PathToblurayMeta.length()-1);
		else
			PathToblurayMeta = PathToblurayMeta.substring(0,PathToblurayMeta.length()-1);
		
		PathToblurayMeta = PathToblurayMeta.substring(0,(int) (PathToblurayMeta.lastIndexOf("/"))).replace("%20", " ") + "/Library/bluray.meta"; //Old meta
		String copymeta = blurayFolder.toString() + "/bluray.meta"; //New meta

		FileReader reader = new FileReader(PathToblurayMeta);			
		BufferedReader oldmeta = new BufferedReader(reader);
		
		FileWriter writer = new FileWriter(copymeta);
		
		String line;
		while ((line = oldmeta.readLine()) != null)
		{
			if (line.contains("file"))
				writer.write(line.replace("file", MKVfile) + System.lineSeparator());
			else					
				writer.write(line + System.lineSeparator());
		}
		
			reader.close();
			oldmeta.close();
			writer.close();		
			
			TSMUXER.run('"' + blurayFolder.toString().replace("\\", "/") + "/bluray.meta" + '"' + " " + '"' + blurayFolder.toString().replace("\\", "/") + '"');	
				
            do {
            	Thread.sleep(100);
            } while (TSMUXER.isRunning);
}
	
	private static boolean lastActions(String fileName, File fileOut, String output) {
		
		if (FunctionUtils.cleanFunction(fileName, fileOut, output))
			return true;

		//Sending processes
		FunctionUtils.sendMail(fileName);
		if (caseAS10.isSelected())
			Wetransfer.addFile(new File(output + "/" + fileName.replace(fileName.substring(fileName.lastIndexOf(".")), "_AS11" + comboFilter.getSelectedItem().toString())));
		else
			Wetransfer.addFile(fileOut);
		Ftp.sendToFtp(fileOut);
		FunctionUtils.copyFile(fileOut);
		
		//Image sequence and merge
		if (caseEnableSequence.isSelected() || Settings.btnSetBab.isSelected())
			return true;
				
		//Watch folder
		if (Shutter.scanIsRunning)
		{
			FunctionUtils.moveScannedFiles(fileName);
			VideoEncoders.main(true);
			return true;
		}
		
		return false;		
	}

}